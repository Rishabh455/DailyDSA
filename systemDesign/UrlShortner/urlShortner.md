यह पूरा transcript **URL Shortener System Design** पर है, लेकिन असल में इसमें कई core System Design concepts छिपे हैं—**requirements gathering, capacity estimation, storage estimation, caching, Base62, collision handling, distributed ID generation, replication, partitioning, consistency और read-heavy architecture**.

नीचे मैं इसे **interview perspective से, zero से** समझा रहा हूँ। जहाँ transcript में कोई approximation या थोड़ी confusing बात है, वहाँ उसे अलग से साफ कर रहा हूँ।

---

# 1. Problem क्या है?

हमारे पास एक लंबा URL है:

```text
https://www.example.com/products/category/mobile/iphone/iphone-17-pro...
```

हमें इसे छोटा बनाना है:

```text
https://short.ly/aB91xK2
```

और जब कोई user short URL hit करे:

```text
short.ly/aB91xK2
```

तो उसे original URL पर redirect कर देना है।

इसलिए system का basic काम है:

```text
Long URL
   ↓
Generate Short Key
   ↓
Store Mapping
   ↓
Short URL

Short URL
   ↓
Lookup
   ↓
Original URL
   ↓
HTTP Redirect
```

Transcript भी शुरुआत में इसी basic functionality से शुरू करता है और फिर requirements, capacity, design और scaling तक जाता है। 

---

# 2. सबसे पहले Functional Requirements

System design interview में coding शुरू नहीं करते।

पहले पूछते हैं:

> System को करना क्या है?

Transcript में मुख्य functional requirements हैं:

### 1. Shorten URL

```text
POST /shorten
```

Input:

```json
{
  "longUrl": "https://example.com/very/long/url"
}
```

Output:

```json
{
  "shortUrl": "https://short.ly/aB91xK2"
}
```

### 2. Redirect

```text
GET /aB91xK2
```

System database/cache में lookup करेगा:

```text
aB91xK2 → https://example.com/very/long/url
```

और redirect करेगा।

### 3. Expiration

हर URL forever नहीं रहना चाहिए।

Example:

```text
createdAt = 2026-09-26
expiry = 2036-09-26
```

10 years बाद URL expire।

### 4. Delete

User अपनी shortened URL delete कर सकता है।

### 5. User management

Transcript में signup/login भी mentioned है:

```text
Signup
Login
Logout
```

क्यों?

क्योंकि short URL के साथ:

```text
userId
```

associate किया जा सकता है। 

---

# 3. Non-Functional Requirements

अब interviewer पूछ सकता है:

> यह system कैसा perform करना चाहिए?

Transcript में चार important requirements हैं।

### High Availability

अगर एक server down हो जाए तो भी URL redirect होना चाहिए।

```text
Server 1 ❌
Server 2 ✅
Server 3 ✅
```

User को service मिलती रहे।

---

### Low Latency

User short URL click करे और जल्दी redirect मिले।

इसलिए:

```text
Cache
+
Read replicas
+
Efficient key lookup
```

important होंगे।

---

### Unpredictable URL

यह नहीं होना चाहिए:

```text
short.ly/1001
short.ly/1002
short.ly/1003
```

क्योंकि कोई attacker URLs guess कर सकता है।

Better:

```text
short.ly/aB91xK2
short.ly/kP72LmQ
```

Random-looking IDs।

Transcript explicitly predictability avoid करने की बात करता है। 

---

### Scalability

Traffic future में बढ़ सकता है।

इसलिए architecture ऐसा होना चाहिए कि:

```text
10 servers
→
100 servers
```

scale किया जा सके।

---

# 4. Read-heavy System क्या होता है?

यह बहुत important interview concept है।

URL shortener में कौन सा operation ज्यादा होगा?

### Write

जब कोई नया URL shorten करता है।

### Read

जब कोई short URL खोलता है।

Example:

```text
100 new URLs

लेकिन

10,000 redirects
```

तो system:

```text
Read-heavy
```

है।

Transcript example देता है:

```text
100 : 1
```

मतलब:

```text
100 reads
:
1 write
```

इसलिए architecture को **read optimized** रखना चाहिए। 

---

# 5. Capacity Estimation

System Design में यह बहुत important है।

मान लो interviewer कहता है:

> हर महीने 1 million नए URLs आते हैं।

तो:

```text
1 million / month
```

और expiry:

```text
10 years
```

Total URLs:

```text
1M × 12 × 10
= 120M URLs
```

यानी लगभग:

```text
120 million records
```

Transcript इसी approach से storage calculate करता है। 

---

# 6. Storage Calculation

मान लो एक record approximately:

```text
500 bytes
```

तो:

```text
120M × 500 bytes
```

लगभग:

```text
60 GB
```

इसलिए database storage करीब 60 GB होगी।

Transcript भी इसी approximation से ~60 GB निकालता है। 

### Interview formula

याद रखो:

```text
Total Storage
=
Number of Records × Average Record Size
```

---

# 7. QPS क्या है?

QPS:

> Queries Per Second

मतलब:

```text
एक second में कितनी requests?
```

अगर:

```text
100M reads/month
```

तो roughly:

```text
100,000,000
----------------
30 × 24 × 60 × 60
```

≈

```text
38.6 requests/sec
```

Transcript इसे rough estimate के तौर पर **~50 QPS** मानता है। 

Interview में exact calculation करने की बजाय:

> “Approximately 40–50 reads/sec”

बोलना ठीक रहेगा।

---

# 8. Cache की जरूरत क्यों?

यह सबसे important concepts में से एक है।

System read-heavy है:

```text
100 reads
:
1 write
```

हर request database तक जाएगी तो database unnecessarily load होगा।

इसलिए:

```text
Client
 ↓
Cache
 ↓ miss
Database
```

### Example

पहली बार:

```text
aB91xK2
```

आया।

Cache miss:

```text
Cache ❌
DB ✅
```

DB से:

```text
aB91xK2 → original URL
```

मिला।

अब cache में डाल दो:

```text
Cache:
aB91xK2 → original URL
```

अगली request:

```text
Cache ✅
```

Database तक जाने की जरूरत नहीं।

---

# 9. Cache Hit / Cache Miss

### Cache Hit

Data cache में मिला।

```text
Request
 ↓
Cache ✅
 ↓
Response
```

Fast.

### Cache Miss

Data cache में नहीं मिला।

```text
Request
 ↓
Cache ❌
 ↓
Database
 ↓
Cache
 ↓
Response
```

---

# 10. 25% Cache Calculation

Transcript roughly मानता है:

```text
~50 reads/sec
```

One day:

```text
50 × 86,400
≈ 4.32 million
```

Approx:

```text
5 million requests/day
```

अगर केवल 25% cache करना है:

```text
5M × 25%
=
1.25M
```

तो लगभग 1.25 million entries cache में होंगी। 

अगर प्रत्येक entry ~500 bytes:

```text
1.25M × 500
=
625 MB
```

इसलिए around:

```text
~1 GB RAM
```

का cache रखना reasonable approximation है।

---

# 11. Primary Memory vs Secondary Memory

यह terminology interview में आ सकती है।

### Primary Memory

RAM

Fast but volatile.

Caching के लिए:

```text
Redis
Memcached
```

### Secondary Storage

Disk / SSD

Persistent.

Database इसी category में आता है।

इस problem में:

```text
Database → persistent storage
Cache    → RAM
```

Transcript इसी distinction का उपयोग करता है। 

---

# 12. High-Level Architecture

अब system कुछ ऐसा दिखेगा:

```text
                 ┌───────────────┐
                 │    Client     │
                 └───────┬───────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Load Balancer   │
                └────────┬────────┘
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
        ┌───────────┐         ┌───────────┐
        │ App Server│         │ App Server│
        └─────┬─────┘         └─────┬─────┘
              │                     │
              └──────────┬──────────┘
                         ▼
                    ┌─────────┐
                    │ Cache   │
                    └────┬────┘
                         │
                      cache miss
                         │
                         ▼
                    ┌─────────┐
                    │ MongoDB │
                    └─────────┘
```

इसके बाद scaling के लिए:

```text
Mongo Primary
     │
 ┌───┼────┐
 ▼   ▼    ▼
Read Replica
Read Replica
Read Replica
```

---

# 13. MongoDB क्यों?

Transcript में "मैंगो..." बोला गया है, context से यह **MongoDB** है।

MongoDB का फायदा:

* horizontally scale कर सकता है
* document based है
* high availability support करता है
* read-heavy workloads के लिए replicas इस्तेमाल कर सकते हैं

Transcript availability को consistency से ऊपर रखता है और eventual consistency को acceptable मानता है। 

---

# 14. Eventual Consistency क्या है?

मान लो MongoDB में:

```text
Primary
```

और कई:

```text
Replicas
```

हैं।

Write primary पर हुई:

```text
aB91xK2 → URL-A
```

Replica पर update पहुँचने में थोड़ा delay हो सकता है।

तो कुछ milliseconds के लिए:

```text
Primary → URL-A

Replica → old state
```

हो सकता है।

इसे:

> Eventual Consistency

कहते हैं।

कुछ समय बाद सभी replicas same data पर आ जाते हैं।

---

# 15. Strong Consistency vs Eventual Consistency

### Strong Consistency

हर read latest value देखे।

```text
Write
 ↓
All readers immediately see new data
```

लेकिन ज्यादा expensive हो सकता है।

### Eventual Consistency

Immediately latest data guarantee नहीं।

लेकिन थोड़ी देर बाद सभी nodes sync हो जाते हैं।

URL shortener में यह अक्सर acceptable हो सकता है क्योंकि:

```text
create URL
```

के तुरंत बाद कुछ milliseconds तक replica lag tolerate किया जा सकता है।

---

# 16. Database Schema

Transcript दो collections की बात करता है।

## URL Collection

Example:

```json
{
  "_id": "...",
  "originalUrl": "https://example.com/...",
  "shortUrl": "aB91xK2",
  "userId": "U123",
  "expiryDate": "2036-09-26",
  "createdAt": "2026-09-26"
}
```

## User Collection

```json
{
  "userId": "U123",
  "name": "Rishabh",
  "email": "r@example.com",
  "apiKey": "..."
}
```

Transcript इन्हीं fields का basic structure बताता है। 

---

# 17. सबसे important हिस्सा — Short URL कैसे generate करेंगे?

अब असली System Design problem।

हमें:

```text
Long URL
```

को:

```text
aB91xK2
```

में convert करना है।

Requirements:

```text
Short
+
Unique
+
Fast
+
Hard to predict
```

---

# 18. पहला तरीका — Hash Map

सबसे simple idea:

```text
Map<LongURL, ShortURL>
```

Example:

```text
https://abc.com/123
        ↓
      aB91xK2
```

लेकिन distributed system में problem आएगी।

कई application servers हैं:

```text
Server 1
Server 2
Server 3
```

और shared state manage करना पड़ेगा।

इसलिए transcript इसे scalable solution नहीं मानता। 

---

# 19. दूसरा तरीका — MD5

Idea:

```text
longURL
   ↓
MD5
   ↓
128-bit hash
```

MD5 output बड़ा होता है:

```text
128 bits
```

Transcript इसी problem की ओर point करता है। 

हमें सिर्फ:

```text
6–8 characters
```

चाहिए।

तो MD5 के पहले 7 characters ले लिए:

```text
MD5
 ↓
abcdef...
 ↓
abcdef1
```

लेकिन अब collision हो सकता है।

---

# 20. Collision क्या है?

मान लो:

```text
URL A → abc1234

URL B → abc1234
```

दो अलग URLs को same short key मिल गई।

इसे:

> Hash Collision

कहते हैं।

URL shortener में यह unacceptable है।

Transcript इसी reason से MD5 के कुछ शुरुआती characters लेने को unsafe बताता है। 

---

# 21. Base62 क्या है?

यह पूरा topic बहुत important है।

हम short URL में characters use कर सकते हैं:

```text
a-z = 26
A-Z = 26
0-9 = 10
```

Total:

```text
26 + 26 + 10 = 62
```

इसलिए:

> Base62

---

# 22. Base62 क्यों?

मान लो हमें length = 4 चाहिए।

अगर केवल numbers use करें:

```text
0-9
```

तो combinations:

```text
10^4 = 10,000
```

लेकिन Base62:

```text
62^4
```

≈

```text
14.7 million
```

बहुत ज्यादा combinations।

इसलिए:

```text
More combinations
→
Less collision probability
```

Transcript इसी comparison को explain करता है। 

---

# 23. Formula याद रखो

अगर:

```text
Base = 62
Length = N
```

तो possible short URLs:

```text
62^N
```

Example:

```text
N = 1
62

N = 2
62²

N = 7
62⁷
```

62⁷ ≈ **3.5 trillion combinations**.

इसलिए 7-character Base62 काफी बड़ा namespace देता है। Transcript भी इसी order की संख्या बताता है। 

---

# 24. Base62 Encoding कैसे काम करती है?

मान लो हमें number मिला:

```text
125
```

Base62 converter उसे कुछ string में convert करेगा:

```text
125 → cb
```

Exact characters implementation पर depend करते हैं।

Concept:

```text
Integer ID
   ↓
Base62 Encode
   ↓
Short String
```

और reverse:

```text
Short String
   ↓
Base62 Decode
   ↓
Integer ID
```

---

# 25. सबसे important realization

Base62 itself ID generate नहीं करता।

यह सिर्फ:

> number को compact string में convert करता है।

Example:

```text
ID = 100001
```

Base62:

```text
100001 → bX9k
```

तो हमें पहले **unique number** generate करना पड़ेगा।

यहीं से अगला बड़ा problem आता है।

---

# 26. Unique Number कैसे generate करें?

Simplest:

```text
1
2
3
4
5
6
...
```

Counter.

हर नई URL:

```text
counter++
```

और उस number का Base62 बना दो।

Example:

```text
1 → b
2 → c
3 → d
...
1001 → gH
```

Problem?

Distributed servers.

---

# 27. Distributed Counter Problem

मान लो:

```text
Server A
Server B
Server C
```

तीनों एक ही counter पढ़ रहे हैं।

अगर current value:

```text
100
```

तो:

```text
Server A → 100
Server B → 100
```

हो सकता है।

अब:

```text
100 → same short URL
```

Collision.

Transcript इसी distributed counter problem को explain करता है। 

---

# 28. Single Counter Database

एक centralized counter रख दो:

```text
Counter = 1000
```

हर request:

```text
increment
```

Problem:

```text
All servers
    ↓
One counter
```

यह central bottleneck बन सकता है।

इसे:

> Single Point of Failure / Bottleneck

कहा जा सकता है।

---

# 29. Range Allocation

एक clever solution:

Counter को ranges में divide कर दो।

Example:

```text
Server 1 → 1 to 1,000,000
Server 2 → 1,000,001 to 2,000,000
Server 3 → 2,000,001 to 3,000,000
```

अब Server 1 बिना central counter को हर request पर hit किए IDs generate कर सकता है।

यह approach:

> ID range allocation

है।

---

# 30. इसका फायदा

Server 1:

```text
1
2
3
4
...
```

Server 2:

```text
1,000,001
1,000,002
...
```

Overlap नहीं।

इसलिए IDs unique।

---

# 31. Problem with Range Allocation

मान लो future में:

```text
Server 4
```

add करना है।

तो ranges correctly assign करनी होंगी।

गलत allocation:

```text
Server 1 → 1–1M

Server 2 → 1M–2M

Server 3 → 1.5M–2.5M
```

Overlap!

फिर collisions।

Transcript इसी range-management problem को discuss करता है। 

---

# 32. ZooKeeper कहाँ आता है?

Transcript में "Apache ... Zoo..." जैसा reference है; context से यह **Apache ZooKeeper** है।

ZooKeeper distributed systems में coordination के लिए use हो सकता है।

इस scenario में:

```text
Application Servers
       ↓
ZooKeeper
       ↓
Range Assignment
```

Example:

```text
Server A → range 1–1M
Server B → range 1M–2M
Server C → range 2M–3M
```

ZooKeeper coordination में मदद करता है ताकि multiple nodes conflicting ownership न लें।

---

# 33. दूसरा approach — Random Number

Range allocation के बजाय:

```text
Random number
   ↓
Base62
   ↓
Short URL
```

Example:

```text
3827191
 ↓
Base62
 ↓
aB91xK2
```

लेकिन random number दोबारा generate हो सकता है।

---

# 34. Duplicate Random Number

मान लो:

```text
Request 1 → 123456
Request 2 → 123456
```

दोनों:

```text
Base62(123456)
```

same result देंगे।

इसलिए हमें check करना होगा:

```text
Does short key already exist?
```

अगर हाँ:

```text
Generate another random number
```

यह retry loop है।

Transcript इसी approach को discuss करता है। 

---

# 35. Random + Database Unique Constraint

Real production design में इसे मजबूत बनाने के लिए database में:

```text
UNIQUE(shortKey)
```

constraint/index रखना अच्छा होता है।

फिर race condition में भी database final protection देता है।

Concept:

```text
Application uniqueness check
+
Database unique constraint
```

दोनों मिलकर safer design देते हैं।

---

# 36. Race Condition क्या है?

मान लो दो requests simultaneously आईं:

```text
Request A → abc123

Request B → abc123
```

दोनों check करती हैं:

```text
Does abc123 exist?
```

दोनों को जवाब मिला:

```text
NO
```

फिर दोनों insert करने चली गईं।

अगर database unique constraint नहीं है:

```text
Duplicate
```

हो सकता है।

इसीलिए सिर्फ application-level check पर्याप्त नहीं।

---

# 37. Idempotency नहीं, Uniqueness

यह distinction interview में useful है।

### Uniqueness

दो अलग records को same key नहीं मिलनी चाहिए।

### Idempotency

Same request दोबारा आने पर unwanted duplicate operation नहीं होना चाहिए।

Example:

```text
POST shorten
```

same request दो बार accidentally आ गई।

Idempotency key हो तो दोनों requests same result दे सकती हैं।

---

# 38. Counter + Base62 Architecture

सबसे clean conceptual flow:

```text
Long URL
   ↓
Generate unique numeric ID
   ↓
Base62 encode
   ↓
Short Key
   ↓
Store mapping
```

Example:

```text
ID = 123456789

Base62(ID)
     ↓
aZ91kP
```

Database:

```text
aZ91kP → https://example.com/...
```

---

# 39. URL Redirect Flow

अब reverse direction:

User hits:

```text
short.ly/aZ91kP
```

Flow:

```text
Client
 ↓
Load Balancer
 ↓
Application Server
 ↓
Cache
```

### Cache hit:

```text
aZ91kP → original URL
```

directly return:

```text
HTTP 301/302
```

### Cache miss:

```text
Cache
 ↓
MongoDB
 ↓
original URL
 ↓
populate cache
 ↓
redirect
```

---

# 40. 301 vs 302

यह transcript explicitly detail नहीं करता, लेकिन redirect concept समझने के लिए important है।

### 301

Permanent redirect.

### 302

Temporary redirect.

URL shortener implementation में requirement के हिसाब से दोनों use किए जा सकते हैं।

---

# 41. Database Read Replicas

System read-heavy है।

इसलिए:

```text
1 Primary
+
Multiple Read Replicas
```

Architecture:

```text
                 Primary
                    │
            ┌───────┼───────┐
            ▼       ▼       ▼
         Replica Replica Replica
```

Write:

```text
Primary
```

Read:

```text
Replicas
```

इससे read traffic distribute होता है।

Transcript explicitly read replicas की बात करता है। 

---

# 42. Replication क्यों?

अगर database का एक node down हो गया:

```text
Primary ❌
```

तो replica promote हो सकती है।

इससे:

> High Availability

मिलती है।

---

# 43. Partitioning क्या है?

जब data बहुत बड़ा हो जाता है तो उसे logical pieces में divide करते हैं।

Example:

```text
Partition 1
Partition 2
Partition 3
Partition 4
```

हर partition subset of data रखता है।

इससे:

```text
Data load distribute
Storage distribute
Queries distribute
```

हो सकती हैं।

---

# 44. Sharding vs Partitioning

Interview में distinction पूछ सकते हैं।

### Partitioning

Data को logical partitions में divide करना।

### Sharding

Generally data को multiple database nodes/machines में distribute करना।

Simple interview language:

> Sharding is horizontal partitioning across multiple machines.

---

# 45. Hash Partitioning

Suppose:

```text
shortKey = aB91xK2
```

Hash निकालें:

```text
hash(shortKey)
```

फिर:

```text
hash % numberOfPartitions
```

से partition choose कर सकते हैं।

Example:

```text
hash("aB91xK2") % 4 = 2
```

तो:

```text
Partition 2
```

---

# 46. Consistent Hashing

Transcript अंत में **consistent hash partitioning** की तरफ जाता है। 

यह distributed systems में बहुत important concept है।

Normal hashing:

```text
hash(key) % N
```

मान लो:

```text
N = 4
```

फिर nodes:

```text
4 → 5
```

कर दिए।

तो लगभग बहुत सारे keys का partition बदल सकता है।

इसका मतलब:

```text
massive data movement
```

---

# 47. Consistent Hashing क्यों?

Consistent hashing इस problem को कम करता है।

Nodes और keys को एक logical ring पर map करते हैं:

```text
             Node A
          /           \
       Key 1          Key 2

     Node D             Node B

          \           /
             Node C
```

अगर Node B add/remove हो:

```text
पूरे data का mapping नहीं बदलता
```

सिर्फ आसपास के keys move होते हैं।

---

# 48. Virtual Nodes

Real systems में consistent hashing के साथ अक्सर virtual nodes use होते हैं।

एक physical server:

```text
Server A
```

के कई logical positions:

```text
A1
A2
A3
A4
...
```

ring पर हो सकते हैं।

इससे load distribution better होता है।

---

# 49. LRU क्या है?

Transcript का final phrase:

> Recently Used

इशारा **LRU = Least Recently Used** की तरफ है। 

Cache की capacity limited होती है।

मान लो cache में 1 million entries आ सकती हैं।

नई entry आने पर जगह नहीं है।

तो कौन हटे?

LRU कहता है:

> जो item सबसे लंबे समय से use नहीं हुआ है, उसे remove करो।

Example:

```text
A → recently used
B → recently used
C → not used for long
D → recently used
```

New entry:

```text
E
```

तो:

```text
C
```

evict हो सकता है।

---

# 50. Cache Eviction Policies

कुछ important policies:

### LRU

Least Recently Used

### LFU

Least Frequently Used

### FIFO

First In First Out

URL shortener के लिए LRU common/simple choice है।

---

# 51. Complete Architecture

अब सारे concepts जोड़ो:

```text
                         CLIENT
                           │
                           ▼
                   ┌───────────────┐
                   │ Load Balancer │
                   └───────┬───────┘
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
        App Server     App Server    App Server
             │             │             │
             └─────────────┼─────────────┘
                           │
                    ┌──────▼──────┐
                    │    Cache    │
                    │ Redis-like  │
                    └──────┬──────┘
                           │
                       Cache Miss
                           │
                           ▼
                    ┌─────────────┐
                    │   MongoDB   │
                    │   Primary   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
         Read Replica  Read Replica  Read Replica
```

URL generation side:

```text
          Shorten Request
                 │
                 ▼
          Unique ID Generator
                 │
          ┌──────┴──────┐
          │ Counter /   │
          │ Random ID   │
          └──────┬──────┘
                 │
                 ▼
             Base62
                 │
                 ▼
             Short Key
                 │
                 ▼
             MongoDB
```

---

# 52. पूरा Request Flow — Shorten

मान लो:

```text
https://google.com/something/long
```

आया।

### Step 1

Request:

```text
POST /shorten
```

### Step 2

Authentication/user validation.

### Step 3

Unique numeric ID generate:

```text
123456789
```

### Step 4

Base62:

```text
123456789 → aB91xK2
```

### Step 5

Database:

```text
{
   shortKey: "aB91xK2",
   originalUrl: "...",
   userId: "U123",
   expiryDate: "2036..."
}
```

### Step 6

Response:

```text
https://short.ly/aB91xK2
```

---

# 53. पूरा Request Flow — Redirect

User:

```text
GET /aB91xK2
```

### Step 1

Load balancer request को application server पर भेजता है।

### Step 2

Application cache check:

```text
aB91xK2
```

### Step 3 — Cache Hit

```text
original URL
```

मिल गया।

### Step 4

Redirect.

अगर cache miss:

```text
Cache
 ↓
MongoDB
 ↓
URL
 ↓
Cache populate
 ↓
Redirect
```

---

# 54. Expiry कैसे handle करेंगे?

Database record:

```text
expiryDate
```

रखेगा।

Lookup के समय:

```java
if (now > expiryDate) {
    return expired;
}
```

साथ में background cleanup भी हो सकता है।

MongoDB में TTL index जैसे mechanisms भी practical implementation में useful हो सकते हैं, लेकिन transcript इस implementation detail तक नहीं जाता।

---

# 55. Security

Transcript सिर्फ basic security mention करता है:

> हर कोई arbitrary URLs generate न करे।

Practical concerns:

```text
Authentication
Authorization
Rate limiting
Abuse prevention
Malicious URL detection
API keys
```

लेकिन ये transcript का core हिस्सा नहीं हैं।

---

# 56. Rate Limiting क्यों जरूरी हो सकता है?

मान लो एक attacker:

```text
1 second में 1 million shorten requests
```

भेज रहा है।

System overload हो सकता है।

इसलिए:

```text
User → 100 requests/minute
```

जैसा limit लगा सकते हैं।

यह अलग system-design topic है लेकिन URL shortener में naturally आता है।

---

# 57. Transcript का सबसे important System Design Thought Process

पूरे वीडियो का essence यह है:

```text
Requirement
    ↓
Traffic Estimation
    ↓
Storage Estimation
    ↓
Read vs Write Analysis
    ↓
High Availability / Consistency
    ↓
High-Level Architecture
    ↓
ID Generation
    ↓
Collision Handling
    ↓
Caching
    ↓
Replication
    ↓
Partitioning
    ↓
Scaling
```

यही approach तुम्हें **Parking Lot, Rate Limiter, Instagram, Uber, TinyURL** जैसे दूसरे System Design questions में भी काम आएगी। 

---

# 58. Interview में 2–3 मिनट में कैसे explain करना है?

तुम ऐसा बोल सकते हो:

> “I would design the URL shortener as a read-heavy, highly available system. The main APIs would be URL shortening and redirection, with optional expiration and deletion.
>
> Assuming around 1 million new URLs per month and a 10-year retention period, we would store around 120 million records. With an average record size of approximately 500 bytes, storage would be around 60 GB.
>
> Since redirects are much more frequent than writes, I would use a distributed cache to reduce database reads and MongoDB with replication for scalability and availability.
>
> For short URL generation, instead of truncating MD5 because of collision risk, I would generate a unique numeric ID and encode it using Base62. A distributed ID-generation mechanism such as allocated ID ranges can avoid a centralized counter bottleneck.
>
> For scalability, I would use multiple application servers behind a load balancer, read replicas for database reads, and partitioning/sharding as the data grows. Cache eviction can use LRU.”

---

# 59. तुम्हें अभी कौन-कौन से concepts properly समझने चाहिए?

इस एक URL Shortener question के पीछे ये **core System Design concepts** हैं:

| Concept                     | तुम्हें क्या समझना है                 |
| --------------------------- | ------------------------------------- |
| Functional Requirements     | System क्या करेगा                     |
| Non-Functional Requirements | System कितना fast/reliable होना चाहिए |
| Read vs Write               | किस operation का load ज्यादा है       |
| QPS                         | Requests per second                   |
| Capacity Estimation         | Storage/RAM/traffic calculation       |
| Caching                     | DB load और latency कम करना            |
| Cache Hit/Miss              | Cache lookup behavior                 |
| Base62                      | Integer → short string                |
| Hashing                     | Data → fixed hash                     |
| Collision                   | दो inputs का same output              |
| Unique ID                   | प्रत्येक URL के लिए unique identifier |
| Counter                     | Sequential ID generation              |
| Distributed Counter         | Multiple servers में counter          |
| Range Allocation            | Servers को ID blocks देना             |
| ZooKeeper                   | Distributed coordination              |
| Replication                 | Multiple DB copies                    |
| Read Replica                | Read traffic distribute करना          |
| Eventual Consistency        | Replicas eventually sync              |
| Strong Consistency          | Reads latest data देखें               |
| Partitioning                | Data split करना                       |
| Sharding                    | Data across machines                  |
| Consistent Hashing          | Node changes पर कम data movement      |
| LRU                         | Cache eviction                        |
| Load Balancer               | Traffic distribute करना               |
| High Availability           | Failure के बावजूद service             |
| Low Latency                 | Fast response                         |
| Scalability                 | Growing traffic handle करना           |

---

# 60. एक बात ध्यान रखना — transcript के numbers

Transcript में कुछ numbers deliberately approximate हैं।

उदाहरण:

```text
1M new URLs/month
100:1 read/write
~50 reads/sec
~60 GB storage
~1 GB cache
```

इनका उद्देश्य **capacity estimation समझाना** है, exact production sizing नहीं। उदाहरण के लिए 100M reads/month को exact 30-day month पर calculate करें तो ~38.6 QPS आता है, जबकि video ~50 QPS round-off करता है। इसलिए interview में कहना बेहतर है:

> “Approximately 40–50 QPS.”

इसी तरह cache calculation भी rough sizing है। इसे exact hardware requirement मत मानना। Transcript में ये values estimation exercise के रूप में आती हैं। 

---

## सबसे छोटा mental model

बस यह diagram दिमाग में बैठा लो:

```text
                URL SHORTENER

        ┌─────────────────────────┐
        │ Functional Requirements │
        │ shorten / redirect      │
        │ expiry / delete         │
        └────────────┬────────────┘
                     ↓
             Capacity Planning
                     ↓
          Read Heavy → Cache
                     ↓
        Generate Unique Numeric ID
                     ↓
                Base62
                     ↓
             Short Key
                     ↓
               MongoDB
                     ↓
             Read Replicas
                     ↓
             Partition / Shard
                     ↓
          High Availability
```

और **सबसे important distinction**:

```text
Hashing       → key बनाने की एक technique
Base62        → number को short string में encode करना
Unique ID     → actual uniqueness देना
Cache         → reads fast करना
Replication   → availability/read scaling
Partitioning  → data scale करना
Load Balancer → application traffic distribute करना
```

यही distinction समझ गया तो इस पूरे वीडियो का System Design काफी clear हो जाता है।
