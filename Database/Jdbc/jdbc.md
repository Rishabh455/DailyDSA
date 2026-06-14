//jdbc
JDBC में **Connection Handling** का मतलब होता है Java application और database के बीच connection को create, use, manage और close करना।

Flow कुछ ऐसा होता है:

```text
Java Application  ---> JDBC API ---> JDBC Driver ---> Database
```

## JDBC Connection Handling Steps

### 1. Import Packages

```java
import java.sql.*;
```

---

### 2. Load Driver (Old way)

Java 6/7 में manually driver load करते थे:

```java
Class.forName("com.mysql.cj.jdbc.Driver");
```

अब modern JDBC में mostly automatic loading हो जाता है अगर dependency मौजूद हो।

---

### 3. Create Connection

सबसे important step:

```java
Connection con = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/testdb",
    "root",
    "password"
);
```

यहाँ:

* `jdbc:mysql://` → protocol
* `localhost` → server
* `3306` → MySQL port
* `testdb` → database name

`Connection` interface represents actual DB connection.

---

## Full Example

```java
import java.sql.Connection;
import java.sql.DriverManager;

public class JdbcDemo {

    public static void main(String[] args) {

        try {

            // Load Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create Connection
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/testdb",
                    "root",
                    "password"
            );

            System.out.println("Connection Successful");

            // Close connection
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

# Important JDBC Interfaces

## 1. Connection

Database connection represent करता है.

Important methods:

```java
con.commit();
con.rollback();
con.close();
con.setAutoCommit(false);
```

---

## 2. Statement

SQL query execute करने के लिए।

```java
Statement st = con.createStatement();
```

---

## 3. PreparedStatement

Precompiled query + SQL Injection protection.

```java
PreparedStatement ps =
con.prepareStatement("select * from user where id=?");
```

Most preferred in real projects.

---

## 4. ResultSet

Data fetch करने के लिए।

```java
ResultSet rs = ps.executeQuery();
```

---

# Connection Lifecycle

```text
1. Open Connection
2. Execute Query
3. Process Result
4. Commit/Rollback
5. Close Connection
```

---

# Why Closing Connection Important?

अगर connection close नहीं किया:

* memory leak
* DB connection exhaustion
* application slow
* server crash possibility

इसलिए always:

```java
con.close();
```

या better:

# Try-With-Resources (Best Practice)

```java
try (
    Connection con = DriverManager.getConnection(url, user, pass);
    PreparedStatement ps = con.prepareStatement("select * from users");
) {

    ResultSet rs = ps.executeQuery();

    while(rs.next()) {
        System.out.println(rs.getString("name"));
    }

} catch(Exception e) {
    e.printStackTrace();
}
```

Auto close हो जाता है।

---

# AutoCommit in JDBC

Default:

```java
con.getAutoCommit(); // true
```

मतलब हर query automatically commit हो जाती है।

Transaction handling के लिए:

```java
con.setAutoCommit(false);

con.commit();
con.rollback();
```

---

# Connection Pooling

Real applications में हर request पर new connection बनाना expensive होता है।

इसलिए use करते हैं:

* HikariCP
* Apache DBCP
* C3P0

Spring Boot by default HikariCP use करता है.

Benefit:

* Better performance
* Faster DB access
* Reuse existing connections

---

# Interview Explanation (Short)

> JDBC connection handling is the process of establishing, managing, and closing connections between a Java application and a relational database using JDBC APIs.
> We usually use DriverManager to create connections and PreparedStatement to execute queries.
> Proper resource closing and connection pooling are important for performance and avoiding memory leaks.

---

# Common Interview Questions

## Difference between Statement and PreparedStatement?

| Statement              | PreparedStatement      |
| ---------------------- | ---------------------- |
| Dynamic query          | Precompiled query      |
| Slow                   | Faster                 |
| SQL Injection possible | Prevents SQL Injection |

---

## Why PreparedStatement preferred?

* Security
* Performance
* Query precompilation

---

## What is connection pooling?

Reuse DB connections instead of creating new ones repeatedly.

---

## What happens if connection not closed?

* Resource leak
* DB overload
* Performance issues


JDBC में Transaction का मतलब होता है — multiple SQL operations को एक single unit की तरह treat करना।

मतलब:
या तो सारे operations successful होंगे,
या अगर बीच में कोई failure आया तो सारे changes rollback हो जाएंगे।

Real-world example:
Suppose बैंक में पैसे transfer हो रहे हैं:

1. Account A से पैसा debit
2. Account B में पैसा credit

अगर debit हो गया लेकिन credit fail हो गया तो data inconsistent हो जाएगा।

इसलिए transaction use करते हैं।

---

## JDBC में by default क्या होता है?

By default JDBC में:

```java
connection.setAutoCommit(true);
```

मतलब हर query automatically commit हो जाती है।

Example:

```java
stmt.executeUpdate(query1); // auto commit
stmt.executeUpdate(query2); // auto commit
```

अगर दूसरी query fail हुई,
तो पहली वाली already save हो चुकी होगी।

---

# Manual Transaction Management

इसलिए production में अक्सर auto commit बंद करते हैं:

```java
connection.setAutoCommit(false);
```

अब commit manually करना पड़ेगा।

---

# Complete Flow

```java
Connection con = null;

try {

    con = DriverManager.getConnection(url, user, pass);

    con.setAutoCommit(false);

    PreparedStatement debit =
        con.prepareStatement(
        "UPDATE account SET balance=balance-1000 WHERE id=1");

    PreparedStatement credit =
        con.prepareStatement(
        "UPDATE account SET balance=balance+1000 WHERE id=2");

    debit.executeUpdate();
    credit.executeUpdate();

    con.commit();

}
catch(Exception e){

    con.rollback();

}
finally{

    con.close();

}
```

---

# Important Concepts

## 1. commit()

Permanent save changes in database.

```java
con.commit();
```

---

## 2. rollback()

Failure होने पर previous changes undo.

```java
con.rollback();
```

---

## 3. setAutoCommit(false)

Manual transaction start करने के लिए।

---

## 4. Savepoint

Transaction के बीच checkpoint create कर सकते हैं।

```java
Savepoint sp = con.setSavepoint();

con.rollback(sp);
```

Partial rollback possible होता है।

---

# Interview में ऐसे explain कर सकते हो

“JDBC transaction is used to maintain data consistency and integrity.
By default auto-commit mode is enabled, where every query is committed automatically.
In real-world applications we disable auto-commit and manage transactions manually using commit() and rollback().
If all queries execute successfully we commit the transaction, otherwise rollback is performed to maintain consistency.”

---

# Interview Follow-up Questions

They may ask:

* ACID properties
* Difference between commit and flush
* Transaction isolation levels
* Dirty read / phantom read
* Spring `@Transactional`
* Distributed transactions
* Deadlock

ये next important topics हैं after JDBC transactions for Java backend interviews.
## Commit in JDBC

`commit()` का मतलब होता है:

➡️ जो changes transaction में किए गए हैं उनको permanently database में save करना।

Example:

```java id="gm7b5y"
con.commit();
```

Suppose:

```sql
UPDATE account SET balance = balance - 1000 WHERE id=1;
UPDATE account SET balance = balance + 1000 WHERE id=2;
```

अगर दोनों queries successful हो गईं,
तो:

```java id="4g3v1q"
con.commit();
```

अब changes permanently database में save हो जाएंगे।

---

# Rollback in JDBC

`rollback()` का मतलब होता है:

➡️ transaction में किए गए सारे changes undo कर देना।

Example:

```java id="zqjv6m"
con.rollback();
```

Suppose:

* पैसे debit हो गए
* लेकिन credit query fail हो गई

तो rollback करेगा:

* debit भी cancel
* database previous consistent state में वापस

---

# Real Flow

```java id="aq5y7w"
Connection con = null;

try {

    con = DriverManager.getConnection(url,user,pass);

    con.setAutoCommit(false);

    PreparedStatement ps1 =
        con.prepareStatement(
        "UPDATE account SET balance=balance-1000 WHERE id=1");

    PreparedStatement ps2 =
        con.prepareStatement(
        "UPDATE account SET balance=balance+1000 WHERE id=2");

    ps1.executeUpdate();
    ps2.executeUpdate();

    con.commit();

}
catch(Exception e){

    con.rollback();

}
finally{

    con.close();

}
```

---

# Important Interview Point

By default:

```java id="r22j6s"
autoCommit = true
```

मतलब हर query automatically commit हो जाती है।

Production applications में usually:

```java id="q52n8s"
con.setAutoCommit(false);
```

ताकि multiple operations को single transaction की तरह handle कर सकें।

---

# One-line Interview Definition

### Commit

“Commit permanently saves all database changes made during the transaction.”

### Rollback

“Rollback cancels all uncommitted changes and restores the database to its previous consistent state.”

---

# Interviewer ये भी पूछ सकता है

## What happens after commit?

* Data permanently save
* Locks release हो जाते हैं
* Rollback नहीं कर सकते

---

## What happens after rollback?

* Uncommitted changes revert
* Database consistent state में वापस

---

## Can rollback happen after commit?

❌ No

Once committed,
changes permanently save हो चुके होते हैं।

---

## Difference between auto commit and manual commit

| Auto Commit                         | Manual Commit               |
| ----------------------------------- | --------------------------- |
| Every query auto save               | Developer controls commit   |
| Less control                        | Better transaction handling |
| Not suitable for complex operations | Used in enterprise apps     |



# Connection Pooling in JDBC

Connection Pooling का मतलब होता है:

➡️ Database connections को बार-बार create और destroy करने की बजाय उन्हें reuse करना।

क्योंकि database connection बनाना बहुत expensive operation होता है।

---

# Problem Without Connection Pooling

Suppose 1000 users request भेज रहे हैं।

अगर हर request में:

```java id="cjlwmj"
DriverManager.getConnection()
```

से नया connection बनेगा:

* performance slow होगी
* memory usage बढ़ेगा
* DB server overload हो सकता है

---

# Solution → Connection Pooling

Application startup पर कुछ connections create करके pool में रख दिए जाते हैं।

जब request आती है:

* pool से connection मिलता है
* काम होने के बाद close नहीं होता
* वापस pool में चला जाता है reuse के लिए

---

# Important Point

Connection actually destroy नहीं होता।

```java id="jlwmhx"
connection.close();
```

pooling में इसका मतलब होता है:

➡️ connection वापस pool में return करना।

---

# Flow

```text id="6aj1rp"
Client Request
      ↓
Connection Pool
      ↓
Available Connection
      ↓
Database
      ↓
Return to Pool
```

---

# Benefits

## 1. Better Performance

बार-बार connection create नहीं करना पड़ता।

---

## 2. Faster Response Time

Ready-made connections available रहते हैं।

---

## 3. Better Resource Management

Database overload नहीं होता।

---

## 4. Scalability

Large enterprise applications efficiently handle होती हैं।

---

# Common Connection Pool Libraries

## 1. HikariCP

सबसे popular और fastest।

Spring Boot default यही use करता है।

HikariCP

---

## 2. Apache DBCP

Apache का connection pool।

Apache Commons DBCP

---

## 3. C3P0

पुरानी लेकिन famous library।

c3p0

---

# Example Using DataSource

```java id="9n7lwb"
HikariDataSource ds = new HikariDataSource();

ds.setJdbcUrl(url);
ds.setUsername(user);
ds.setPassword(pass);

Connection con = ds.getConnection();
```

---

# Interview Answer (Important)

“Connection pooling is a technique where a pool of pre-created database connections is maintained and reused instead of creating a new connection for every database request. It improves performance, scalability, and resource utilization.”

---

# Interview Follow-up Questions

They may ask:

## Why is JDBC connection creation expensive?

Because:

* Network communication
* Authentication
* Session creation
* DB resource allocation

सब time लेते हैं।

---

## Difference between DriverManager and DataSource

| DriverManager                     | DataSource                |
| --------------------------------- | ------------------------- |
| Creates new connection every time | Reuses pooled connections |
| Slower                            | Faster                    |
| Not preferred in enterprise apps  | Preferred                 |
| No pooling                        | Supports pooling          |

---

## Spring Boot में कौन सा pool default है?

➡️ HikariCP

---

## What if all connections are busy?

* Request wait करेगी
* Timeout हो सकता है
* Pool size tuning करनी पड़ती है

---

ये JDBC के most important enterprise-level topics में से एक है।

# Stored Procedures in JDBC

Stored Procedure मतलब:

➡️ Database के अंदर stored/precompiled SQL program।

मतलब SQL queries database में पहले से save रहती हैं और Java application उन्हें call करती है।

---

# Why Stored Procedures?

Benefits:

* Performance better
* Reusable logic
* Network calls कम
* Security improve
* Business logic DB side पर रख सकते हैं

---

# Example

Suppose database में procedure है:

```sql id="g2gg0h"
CREATE PROCEDURE getEmployee(IN empId INT)
BEGIN
   SELECT * FROM employee WHERE id = empId;
END;
```

अब Java से इसे call करेंगे।

---

# JDBC में कौन use होता है?

➡️ `CallableStatement`

तीन important interfaces:

| Interface         | Purpose               |
| ----------------- | --------------------- |
| Statement         | Normal SQL            |
| PreparedStatement | Parameterized query   |
| CallableStatement | Stored Procedure call |

---

# Example in JDBC

```java id="m6sx1y"
Connection con =
DriverManager.getConnection(url,user,pass);

CallableStatement cs =
con.prepareCall("{call getEmployee(?)}");

cs.setInt(1,101);

ResultSet rs = cs.executeQuery();

while(rs.next()){

   System.out.println(rs.getString("name"));
}
```

---

# Important Methods

## prepareCall()

Stored procedure prepare करने के लिए।

```java id="1tks3j"
con.prepareCall()
```

---

## execute()

Used when:

* procedure may return multiple results

---

## executeQuery()

SELECT type result.

---

## executeUpdate()

INSERT/UPDATE/DELETE type procedure.

---

# IN, OUT, INOUT Parameters

Very important interview topic.

---

## 1. IN Parameter

Input pass करते हैं।

```java id="hkt1mp"
cs.setInt(1,101);
```

---

## 2. OUT Parameter

Procedure value return करेगी।

Example:

```sql id="xyn3pt"
CREATE PROCEDURE getCount(OUT total INT)
```

Java:

```java id="9kq9k7"
cs.registerOutParameter(1, Types.INTEGER);

cs.execute();

int count = cs.getInt(1);
```

---

## 3. INOUT Parameter

Input भी और output भी।

---

# Advantages

## 1. Faster Execution

DB side precompiled.

---

## 2. Reduced Network Traffic

Multiple SQL queries DB side execute हो जाती हैं।

---

## 3. Better Security

Direct table access hide कर सकते हैं।

---

## 4. Centralized Logic

Business logic DB में maintain।

---

# Disadvantages

## 1. Vendor Dependency

Oracle/MySQL/Postgres syntax अलग हो सकता है।

---

## 2. Harder Version Control

Application code जैसा easy manage नहीं होता।

---

## 3. Debugging Difficult

---

# Interview Answer

“Stored Procedures are precompiled SQL programs stored inside the database and executed using CallableStatement in JDBC. They improve performance, reusability, and reduce network overhead.”

---

# Important Interview Questions

They may ask:

* Difference between PreparedStatement and CallableStatement
* Why stored procedures are faster?
* IN vs OUT parameter
* Can stored procedures return ResultSet?
* What is function vs procedure?
* Are stored procedures database dependent?
* How transactions work inside stored procedures?

---

# Function vs Procedure

| Function                | Procedure             |
| ----------------------- | --------------------- |
| Returns value           | May or may not return |
| Used in SQL expressions | Used independently    |
| Mostly computation      | Business operations   |

---

# One Very Important Point

Nowadays in modern Spring Boot microservices:

* heavy stored procedure usage कम हो गया है
* business logic mostly Java service layer में रखते हैं

BUT:

Banking/legacy enterprise systems में अभी भी बहुत use होता है।

So interview में balanced answer देना अच्छा रहता है।
हाँ, especially 2–3 years experienced Java developer role में interviewer HikariCP के बारे में depth में पूछ सकता है — खासकर अगर तुम्हारे resume में Spring Boot, microservices, enterprise apps, high traffic systems या performance tuning लिखा है।

तुम्हें कम से कम ये topics अच्छे से आने चाहिए:

---

# 1. HikariCP क्या है?

HikariCP एक high-performance JDBC connection pool library है।

Spring Boot में by default यही connection pool use होता है।

Goal:

* Fast connection management
* Low latency
* Better throughput
* Lightweight pool

---

# 2. Why do we need HikariCP?

Without pooling:

```java id="26h2wv"
DriverManager.getConnection()
```

हर request पर नया DB connection बनेगा।

Problem:

* Slow
* Expensive
* DB overload

HikariCP:

* connections pre-create करता है
* reuse करता है
* performance improve करता है

---

# 3. Spring Boot में default क्यों?

क्योंकि यह:

* बहुत fast है
* low memory use करता है
* optimized architecture है
* benchmark में अच्छा perform करता है

---

# 4. Important HikariCP Properties

ये बहुत important हैं interview में।

## maximumPoolSize

Maximum कितने connections pool में होंगे।

```properties id="4l3l3s"
spring.datasource.hikari.maximum-pool-size=10
```

---

## minimumIdle

Minimum idle connections maintain करेगा।

```properties id="j8e0ut"
spring.datasource.hikari.minimum-idle=5
```

---

## connectionTimeout

कितना wait करेगा connection मिलने के लिए।

```properties id="u7g7e6"
spring.datasource.hikari.connection-timeout=30000
```

30 sec बाद exception।

---

## idleTimeout

Idle connection कितनी देर बाद remove होगा।

---

## maxLifetime

एक connection maximum कितनी देर तक alive रहेगा।

Important because:

DB side stale connection issues avoid होते हैं।

---

# 5. Internal Working

Interviewer पूछ सकता है:

“Internally HikariCP works कैसे करता है?”

High level answer:

* startup पर connections create करता है
* pool maintain करता है
* thread-safe queue use करता है
* available connection borrow/return होता है
* lightweight synchronization use करता है

बस इतना काफी है generally।

---

# 6. What happens on connection.close() ?

VERY IMPORTANT QUESTION

Pooling में:

```java id="jlwm0s"
connection.close();
```

actual DB connection बंद नहीं करता।

बस connection pool में वापस return होता है reuse के लिए।

---

# 7. What if pool exhaust हो जाए?

Suppose:

* maxPoolSize = 10
* 11th request आ गई

तो:

* request wait करेगी
* timeout तक wait
* फिर exception:

```text id="10jlwm"
Connection is not available, request timed out
```

---

# 8. Common Production Issues

ये experienced level questions हैं।

## Connection Leak

Connection लिया लेकिन close नहीं किया।

Result:

* pool exhaust
* application hang

HikariCP leak detection support करता है।

---

## Wrong Pool Size

Too small:

* slow app

Too large:

* DB overload

---

# 9. HikariCP vs DBCP vs C3P0

| Feature      | HikariCP     | DBCP   | C3P0  |
| ------------ | ------------ | ------ | ----- |
| Speed        | Very Fast    | Medium | Slow  |
| Memory       | Low          | Medium | High  |
| Modern Usage | Most Popular | Older  | Older |

---

# 10. Interview Answer You Can Say

“HikariCP is a high-performance JDBC connection pooling library used by Spring Boot by default. It improves application performance by reusing database connections instead of creating new ones repeatedly. It manages connection lifecycle, pool sizing, timeout handling, and resource optimization efficiently.”

---

# Most Important Questions They Can Ask

* Why connection pooling needed?
* Why HikariCP preferred?
* Difference between maxPoolSize and minimumIdle?
* What happens if pool exhausted?
* What is connection leak?
* Why connection.close() doesn’t actually close?
* How to tune pool size?
* Why DB connections are expensive?

अगर ये confidently explain कर दोगे तो interviewer को लगेगा कि तुम्हें enterprise-level backend understanding है।
