Haan. **Ye approach better hai.** Abhi actual project ke contradictory DB details ko side mein rakhte hain aur pehle tumhe **clean, fundamentally correct microservice architecture + database ownership model** samjhayenge.

Phir usi foundation ke upar tumhare actual project ko map karenge.

Ek important distinction: neeche jo design hai, woh **learning/reference architecture** hai — ise abhi tumhare actual implementation ka claim nahi karna hai.

# PHASE 2 — MICROSERVICE → DATABASE ARCHITECTURE

Hum isko 2 parts mein karenge:

**Part A — Microservice architecture**

> Service boundary kaise decide hoti hai, services kaise communicate karti hain, ownership kya hoti hai.

**Part B — Database architecture**

> Har service ka DB kya hai, data ownership kya hai, cross-service data kaise handle hota hai, transactions kaise work karti hain.

Aaj pehle **Part A**.

---

# PART A — Clean Microservice Architecture

Imagine hum banking onboarding system ko **proper microservice architecture** mein design kar rahe hain.

High-level:

```text
                         CUSTOMER
                            │
                            ▼
                       Angular UI
                            │
                            ▼
                      API Gateway
                            │
                            ▼
                    Onboarding Service
                     /      |       \
                    /       |        \
                   ▼        ▼         ▼
             OTP Service  Identity   Address
                           Service    Service
                              │          │
                              ▼          ▼
                             OCR      Saudi Post
                            Provider   Provider
                   
                    Onboarding
                         │
                         ▼
                    IAM Service
                         │
                         ▼
                      Keycloak

                    Onboarding
                         │
                         ▼
                       Kafka
                         │
                    ┌────┴────┐
                    ▼         ▼
                   KYC       AML
```

Notification ko bhi separate capability maan sakte hain:

```text
Onboarding
     │
     ▼
Notification Service
     │
   ┌─┴──┐
   ▼    ▼
 Email SMS
```

---

# 2.1 Sabse important service: Onboarding Service

Onboarding Service ka kaam **har cheez khud karna nahi hai**.

Its responsibility:

> **Coordinate the onboarding workflow.**

For example:

```text
Onboarding Service
       │
       ├── Start onboarding
       ├── Maintain workflow state
       ├── Decide next step
       ├── Coordinate Identity
       ├── Coordinate Address
       ├── Coordinate IAM
       └── Publish KYC/AML event
```

Notice:

**Onboarding doesn't become the owner of Identity, Address, IAM, etc.**

It is the **workflow/orchestration owner**.

---

# 2.2 OTP Service

Its responsibility:

```text
OTP Service
   │
   ├── Generate OTP
   ├── Verify OTP
   ├── Expiry
   ├── Retry limits
   └── Rate limiting
```

Potential storage:

```text
OTP Service
     │
     ▼
   Redis
```

Why Redis?

OTP is temporary data.

You don't necessarily need a permanent relational record for every OTP attempt.

---

# 2.3 Identity Service

Responsibility:

```text
Identity Service
       │
       ├── Receive document
       ├── OCR integration
       ├── Extract identity information
       ├── Validate identity
       └── Return verification result
```

External OCR:

```text
Identity Service
       │
       ▼
   OCR Provider
```

Important principle:

> **Identity Service owns identity-verification capability.**

It doesn't mean OCR provider owns your identity domain.

---

# 2.4 Address Service

Responsibility:

```text
Address Service
       │
       ├── Address validation
       ├── Provider integration
       ├── Provider response mapping
       ├── Timeout/retry handling
       └── Address verification result
```

External dependency:

```text
Address Service
      │
      ▼
 Saudi Post / Provider
```

This is a classic example of **integration encapsulation**.

Onboarding doesn't need to know:

```text
Saudi Post API format
Saudi Post error codes
Saudi Post authentication
Saudi Post retry logic
```

It only needs:

> "Please verify this address."

---

# 2.5 IAM Service

Responsibility:

```text
IAM Service
     │
     ├── User identity
     ├── Authentication
     ├── Credentials
     ├── Access management
     └── Identity lifecycle
```

Potential implementation:

```text
IAM Service
     │
     ▼
 Keycloak
```

Onboarding says:

> "Create a user."

IAM handles the actual identity/access-management mechanics.

---

# 2.6 KYC / AML

These are compliance capabilities.

For example:

```text
Onboarding
     │
     │ Event
     ▼
   Kafka
     │
     ├─────────────┐
     ▼             ▼
    KYC            AML
```

Why Kafka?

Because onboarding doesn't necessarily need to keep the original HTTP request open until compliance processing finishes.

It can say:

```text
PENDING_KYC
```

and processing happens independently.

---

# 2.7 Now the most important concept: DATABASE OWNERSHIP

Here's where your previous confusion disappears.

In a clean microservice architecture:

```text
OTP Service
     │
     ▼
OTP Data Store


Identity Service
     │
     ▼
Identity DB


Address Service
     │
     ▼
Address DB


Onboarding Service
     │
     ▼
Onboarding DB


AML Service
     │
     ▼
AML DB
```

### Golden rule:

> **The service that owns a domain capability should be the authoritative owner of its data.**

For example:

```text
Identity Service
      ↓
Identity DB
```

Other services should **not directly query/update Identity DB**.

---

# 2.8 BAD architecture

Suppose:

```text
Onboarding Service
        │
        ├─────────────┐
        ▼             ▼
 Onboarding DB    Identity DB
```

and onboarding directly executes:

```sql
SELECT * FROM identity_user;
```

This creates tight coupling.

Now Identity Service wants to change:

```text
identity_user
```

to:

```text
customer_identity
```

Onboarding breaks.

Why?

Because Onboarding knows Identity's database schema.

That's exactly what microservices are trying to avoid.

---

# 2.9 GOOD architecture

Instead:

```text
Onboarding
     │
     │ API
     ▼
Identity Service
     │
     ▼
Identity DB
```

Onboarding doesn't know:

```text
identity_db
tables
indexes
columns
```

It knows only Identity Service's contract.

For example conceptually:

```text
POST /identity/verify
```

Response:

```json
{
  "verified": true,
  "referenceId": "ID123"
}
```

Now Identity can internally change:

```text
PostgreSQL
→
different schema
→
different indexing
```

without changing the onboarding contract.

---

# 2.10 But what about shared information?

This is where things get interesting.

Suppose Identity Service has:

```text
Identity DB
────────────
identity_id
national_id
name
dob
...
```

Onboarding needs to know:

```text
Is identity verified?
```

Should Onboarding query Identity DB?

### ❌ No.

Instead:

```text
Onboarding
     │
     ▼
Identity Service
     │
     ▼
Verification Result
```

Or, if asynchronous:

```text
Identity Service
      │
      ▼
    Kafka
      │
      ▼
Onboarding
```

---

# 2.11 Data ownership vs data usage

This is a **VERY important interview concept.**

Suppose:

```text
Identity Service
       ↓
Identity DB
       ↓
national_id = X
```

Onboarding may need:

```text
identity_verified = true
```

That doesn't mean Onboarding owns the complete identity data.

Think:

```text
OWNER
  ↓
Identity Service

CONSUMER
  ↓
Onboarding
```

A service can **consume another service's information without owning that domain.**

---

# 2.12 What if Onboarding needs a copy?

Sometimes a service may store a **snapshot/reference**.

For example:

```text
Identity Service
      │
      │ verification result
      ▼
Onboarding Service
      │
      ▼
onboarding_identity_snapshot
```

This can be valid.

But the key question becomes:

> **Which one is the source of truth?**

For example:

```text
Identity DB
     ↓
SOURCE OF TRUTH
```

while:

```text
Onboarding DB
     ↓
Workflow-specific snapshot
```

Then Onboarding shouldn't start modifying the authoritative identity record.

---

# 2.13 This gives us the clean architecture

```text
                  ┌──────────────────┐
                  │ Onboarding       │
                  │ Service          │
                  │                  │
                  │ Workflow owner   │
                  └────────┬─────────┘
                           │
          ┌────────────────┼─────────────────┐
          │                │                 │
          ▼                ▼                 ▼
     OTP Service      Identity Service   Address Service
          │                │                 │
          ▼                ▼                 ▼
        Redis          Identity DB       Address DB


                           │
                           ▼
                      IAM Service
                           │
                           ▼
                        Keycloak


                           │
                           ▼
                         Kafka
                           │
                    ┌──────┴──────┐
                    ▼             ▼
                   KYC            AML
                    │             │
                    ▼             ▼
                 KYC DB         AML DB
```

---

# 2.14 Now one very important thing: "Database per service"

**Database-per-service doesn't necessarily mean every service must use a completely different database technology.**

It means:

> **Data ownership is isolated.**

You could have:

```text
Identity → PostgreSQL
Address  → PostgreSQL
AML      → PostgreSQL
```

but logically:

```text
identity_db
address_db
aml_db
```

with controlled ownership.

Or:

```text
Identity → PostgreSQL
Address  → MongoDB
OTP      → Redis
```

depending on requirements.

So:

> **Database-per-service is primarily about ownership and isolation, not about forcing every service to use a different database technology.**

---

# 2.15 Now let's connect this to transactions

Suppose:

```text
Onboarding
     ↓
Identity Service
```

Identity Service internally:

```text
BEGIN
   ↓
UPDATE identity_db
   ↓
COMMIT
```

Onboarding cannot say:

```text
BEGIN

UPDATE onboarding_db

UPDATE identity_db

COMMIT
```

because these are independently owned databases.

Now we've reached the **real reason distributed systems become difficult.**

A single business operation can span:

```text
Onboarding DB
      +
Identity DB
      +
IAM
      +
Kafka
```

There is no single simple local DB transaction covering all of them.

And that's exactly why later we need:

```text
Saga
Compensation
Eventual Consistency
Outbox
Idempotency
Reconciliation
```

---

# 🧠 The architecture you should visualize

Whenever you hear **microservice**, think:

```text
        SERVICE
           │
     ┌─────┴─────┐
     ↓           ↓
 Responsibility  Data
     │           │
     ↓           ↓
 Business      Own DB
 Capability
```

Then communication:

```text
Service A
    │
    ├── REST → Service B
    │
    └── Kafka → Service C
```

And **never**:

```text
Service A
    ↓
Service B's DB
```

unless there is an exceptional, explicitly designed shared-data architecture—which is not the clean default.

---

# 🎯 Part A complete

Ab tumhe 6 fundamental things clear honi chahiye:

1. **Onboarding = workflow/orchestration responsibility**
2. **Identity = identity capability**
3. **Address = address capability + provider integration**
4. **IAM = identity/access capability**
5. **AML/KYC = compliance capability**
6. **Each service owns its authoritative data**

---

# PART B — Ab DB ko Microservice ke context mein padhenge

Ab next hum **generic DB theory** nahi karenge.

Hum ye karenge:

### Service by service:

```text
1. Onboarding DB
2. OTP data store
3. Identity DB
4. Address DB
5. IAM/Keycloak data
6. AML DB
```

Aur har ek ke liye:

> **What data does it own?**

> **Why does it own that data?**

> **What tables would exist?**

> **Which columns/indexes?**

> **Who can read/write them?**

> **How does another service get the data?**

> **What happens if another service needs that information?**

Then we'll take **one customer `S100`** and trace:

```text
S100
 ↓
Onboarding DB
 ↓
Identity Service
 ↓
Identity DB
 ↓
Address Service
 ↓
Address DB
 ↓
IAM
 ↓
Kafka
 ↓
AML
```

**Uske baad hi hum actual SQL, PK/FK, normalization, indexes, transactions aur locking mein jayenge.**

That's the right sequence for your interview preparation.
