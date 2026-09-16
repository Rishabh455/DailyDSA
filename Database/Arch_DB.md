When a 20-year experience Architect interviews a 3-year experience developer, they are not going to test your syntax or ask you to write simple SELECT queries. They want to see if you understand how databases work under the hood and if you can make smart architectural decisions that prevent production crashes.
As a Java/Backend developer, you must bridge the gap between Java (Hibernate/JPA) and the actual database engine.
Here are the non-negotiable database concepts you must master, categorized logically for your interview prep:
------------------------------
## 1. Indexing Deep Dive (The "Make it Fast" Concept)
An architect will heavily grill you on performance. You must know more than just "indexes make queries faster."

* B-Trees vs. B+ Trees: Know that almost all relational databases (PostgreSQL, MySQL) use B+ Trees for indexing. Understand why: because leaf nodes are linked, making range queries (WHERE age BETWEEN 20 AND 30) incredibly fast.
* Clustered vs. Non-Clustered Indexes:
* Clustered: The actual table data is stored in the leaf nodes of the index (usually the Primary Key). You can have only one per table.
   * Non-Clustered: A separate structure that points to the clustered index/data row.
* Composite Indexes & The Left-to-Right Rule: If you create an index on (first_name, last_name), a query filtering only by last_name will not use the index. It must follow the exact left-to-right order.
* Index Scan vs. Index Seek: A "Seek" is lightning fast (navigating the tree structure). A "Scan" means the DB is reading the entire index tree from top to bottom because your query structure is poor (e.g., using LIKE '%text').

## 2. Transactions & Concurrency Control (The "Data Integrity" Concept)
This is where 90% of mid-level developers fail. You need to know how databases handle thousands of users hitting the same row.

* ACID Properties: Don't just list them; explain how they are enforced. For example, Atomicity and Durability are achieved using the Write-Ahead Log (WAL) or Redo Logs.
* Transaction Isolation Levels: You must memorize the 4 levels and the anomalies they prevent:
* Read Uncommitted (Allows Dirty Reads)
   * Read Committed (Prevents Dirty Reads, allows Non-Repeatable Reads)
   * Repeatable Read (Prevents Non-Repeatable Reads, allows Phantom Reads — MySQL default)
   * Serializable (Prevents all, worst performance)
* Pessimistic vs. Optimistic Locking:
* Optimistic Locking: Uses a @Version column in Java/Hibernate. No database locks are held; it throws an exception at commit time if someone else updated the data. Good for high-read, low-write systems.
   * Pessimistic Locking: Uses SELECT ... FOR UPDATE. Explicitly locks the rows at the DB level. Good for critical financial transactions.

## 3. Database Scaling & Architecture (The "System Design" Concept)
An architect will test if you can think beyond a single database server.

* Database Sharding vs. Partitioning:
* Horizontal Partitioning (Sharding): Splitting data across different physical servers (e.g., Users A-M on Server 1, N-Z on Server 2).
   * Vertical Partitioning: Splitting columns of a table into different tables/databases (e.g., keeping BLOB image data away from user text metadata).
* Read Replicas & Replication Lag: Master-Slave architecture. All writes go to the Master; reads go to Slaves. You must mention Replication Lag: “If a user updates their profile and instantly clicks refresh, they might see old data because the slave database hasn't synced yet.”
* CAP Theorem: You must understand the trade-off between Consistency and Availability when a network partition occurs, especially when choosing between SQL (Strong Consistency) and NoSQL (Eventual Consistency).

## 4. Java/Hibernate Integration Pitfalls (The "Java-Specific" Concept)
Since you are a Java developer, they will expect you to know how Hibernate translates code into database pressure.

* The N+1 Query Problem: This is the most common Hibernate interview question. Explain how fetching a list of Orders with their Items can trigger 1 query for orders + N separate queries for items. Explain how to fix it using JOIN FETCH, Entity Graphs, or DTO projections.
* Connection Pooling Optimization: Explain how tools like HikariCP work. An architect loves a developer who knows how to size a connection pool (Formula: Connections = ((core_count * 2) + effective_spindle_count)).
* Detached Entities & First/Second Level Caching: Know the difference between Session cache (L1) and global Cache (L2 like Ehcache/Redis) and why letting Hibernate manage complex caches blindly can lead to stale data.

## 5. Query Optimization & Diagnostics (The "Troubleshooting" Concept)

* EXPLAIN Plan: If the architect asks, "A production query suddenly slowed down, what do you do?" Your immediate first answer must be: "I will run an EXPLAIN or EXPLAIN ANALYZE on the query." Talk about looking for sequential scans, nested loops, and missing indexes.
* Connection Starvation: Understand what happens when your Java code holds a DB connection open while making a slow, external 3rd-party HTTP API call. (Hint: It kills the database connection pool).

------------------------------
## 💡 Pro-Tip for the Interview:
When answering, use phrases that show you respect database resources.

* Instead of saying: "I will just add an index."
* Say: "I will evaluate the write-to-read ratio of the table before adding an index, because while an index speeds up reads, it introduces overhead on writes and updates due to tree rebalancing."

Would you like to practice a mock scenario? For instance, how would you design the database structure and locking mechanism for a flash-sale ticketing system where 10,000 users try to buy the last 5 tickets simultaneously?
Let's break down this entire document about Database Locking into absolute basics. We will completely ignore coding syntax and look at how this applies to the real world at a Banque Saudi Fransi (BSF) branch.
------------------------------
## 🚨 The Core Problem: The Race Condition
Imagine a customer sits down at a BSF branch kiosk to open a digital bank account (Session S100). The current screen shows Part 1: Identity Verification.
The customer gets impatient and accidentally taps the "Submit" button twice at almost the exact same millisecond.

* Request A goes to Server 1.
* Request B goes to Server 2.

Both servers read the database at the exact same moment. Both see that the customer's current status is PENDING.

* Server 1 calls Nafath to verify the ID.
* Server 2 also calls Nafath to verify the ID.

This is a Race Condition. It causes duplicate API calls to Nafath, wastes bank resources, and risks corrupting the database. To stop this, we use Database Locking.
------------------------------
## 🏛️ Concept 1: Pessimistic Locking vs. Optimistic Locking
There are two major mindsets to solve this race condition.
## 1. Pessimistic Locking (Mindset: "Trust No One, Lock Everything")

* The Logic: The system assumes that collisions and double-clicks will happen.
* The Action: The very moment Request A reads the customer's record (S100), it immediately places a heavy physical lock (🔒) on that specific row in the database.
* The Result: When Request B arrives a millisecond later, it sees the lock. Request B is forced to stop and wait until Request A is completely finished processing and unlocks the row.
* SQL Term: This is executed using SELECT ... FOR UPDATE. It is strictly a pessimistic lock.

## 2. Optimistic Locking (Mindset: "Everything Will Be Fine, Just Double-Check At The End")

* The Logic: The system assumes that double-clicks are rare, so it does not block anyone or use physical locks.
* The Action: The database adds a version number column to the customer's data (e.g., Version = 5). Both Request A and Request B read the data and see Version = 5.
* The Update:
* Request A finishes processing first. It updates the database status to VERIFIED and bumps the version to 6, but only if the version is still 5. This succeeds!
   * Request B finishes a millisecond later. It tries to update the database where version is 5. However, the version is now 6. The database says: "0 rows updated."
* The Result: Request B immediately knows someone else altered the data while it was processing. The application can now gracefully reject or retry Request B.

------------------------------
## 🔄 Concept 2: FOR UPDATE vs. SKIP LOCKED
What is the difference between these two SQL mechanisms?
## FOR UPDATE (Used for Customer Requests)

* Analogy: Imagine a single-person bathroom at the bank branch.
* Behavior: If Customer A is inside, the door is locked (FOR UPDATE). Customer B must stand in line outside the door and wait until Customer A leaves.
* Best Used For: Individual customer sessions (like our onboarding flow) where operations must happen one after the other.

## FOR UPDATE SKIP LOCKED (Used for Background Workers/Publishers)

* Analogy: Imagine a stack of 5 files on a desk that need processing. Bank Worker A walks up and grabs the first 2 files, locking them. Bank Worker B walks up right behind them.
* Behavior: Worker B does not stand there waiting for Worker A to finish. Instead, Worker B skips the locked files and immediately grabs the remaining unlocked files.
* Best Used For: Background applications (like our Outbox Publisher or Kafka workers) where multiple servers are competing to process a large queue of records concurrently without clashing.

------------------------------
## ⚠️ The Major Architect Trap: Network Calls Inside DB Transactions
Look at this specific workflow diagrammed in your project notes:

BEGIN DB Transaction ➡️ Lock Row (FOR UPDATE) ➡️ Call External Nafath API ➡️ Wait 3 Seconds ➡️ Update DB ➡️ COMMIT

If an interviewer asks you about this design, it is a trap.
The Problem: Keeping a database transaction and row-level lock open while making a slow, external network call to a government API (like Nafath) is a massive performance risk. If Nafath slows down or takes 5 seconds to respond, your database connection pool will exhaust, and the entire BSF onboarding application will crash.
The Solution: You must separate your database transaction boundaries. You read the data, close the transaction, make the external API call, and then open a brand-new transaction to update the database state using Optimistic Locking (version) to ensure no one else modified it in the meantime.
------------------------------
## 📊 Summary Checklist for the Interview

| Lock Type | Core Mechanism | Best Used For | Major Trade-off |
|---|---|---|---|
| Pessimistic (FOR UPDATE) | Places a physical row lock; forces concurrent requests to wait. | High-collision, critical customer steps. | High lock contention; can slow down the DB. |
| Pessimistic (SKIP LOCKED) | Places a lock but tells concurrent workers to skip and move on. | Background queue workers (like the Outbox Publisher). | Only works well for batch processing queues. |
| Optimistic (version column) | No physical lock. Checks the version number at the exact moment of update. | Low-collision environments; avoiding locks during long API calls. | The application must write logic to handle update failures. |

------------------------------
Now that the difference between Optimistic, Pessimistic (FOR UPDATE), and SKIP LOCKED is clear, would you like to structure a specific English interview response on how you would defend against the "Network Call inside a DB Lock" trap?

Absolutely. Let's make **MVCC notes as a thinking framework**, not a list to memorize.

# MVCC — Concept Notes

## 1. First question: Why does PostgreSQL need MVCC?

Imagine two requests are working on the same customer:

```text
S100 → ID_VERIFICATION
```

Request A is reading S100.

At almost the same time, Request B wants to update S100.

The database needs to answer:

> **What should A see while B is changing the row?**

If the database simply blocked every reader whenever someone writes, concurrency would become poor.

**MVCC solves this by allowing PostgreSQL to maintain visibility of different row versions.**

Think:

```text
S100
 │
 ├── old version → ID_VERIFICATION
 │
 └── new version → ADDRESS_VERIFICATION
```

The important idea:

> **MVCC is mainly about deciding which version of data a transaction is allowed to see.**

---

# 2. Is MVCC the same as Optimistic Locking?

**No. This is extremely important.**

### MVCC

Database mechanism for:

> "Which version of this row can my query see?"

### Optimistic locking

Application/database design technique for:

> "Did somebody modify this row after I read it?"

Example:

```sql
UPDATE onboarding_session
SET current_step = 'ADDRESS_VERIFICATION',
    version = version + 1
WHERE session_id = 'S100'
AND version = 5;
```

If another transaction already changed version 5 → 6:

```text
UPDATE affects 0 rows
```

Your application knows:

> Someone changed this record. My update is stale.

So remember the distinction:

```text
MVCC
 ↓
visibility of row versions

Optimistic Locking
 ↓
detect conflicting modification using version/check
```

**MVCC ≠ optimistic locking.**

---

# 3. Does PostgreSQL use only MVCC?

No.

PostgreSQL uses **both MVCC and locks**.

Think of two different problems:

```text
MVCC
"What data can I SEE?"

Locks
"Who can MODIFY this data right now?"
```

This distinction will make the rest of DB concurrency much easier.

---

# 4. What happens with a normal SELECT?

Suppose:

```text
S100 = ID_VERIFICATION
```

Transaction A:

```sql
BEGIN;

SELECT current_step
FROM onboarding_session
WHERE session_id = 'S100';
```

A sees:

```text
ID_VERIFICATION
```

Now B executes:

```sql
UPDATE onboarding_session
SET current_step = 'ADDRESS_VERIFICATION'
WHERE session_id = 'S100';

COMMIT;
```

B has now committed.

If A executes:

```sql
SELECT current_step
FROM onboarding_session
WHERE session_id = 'S100';
```

under the default **READ COMMITTED** isolation:

```text
A sees ADDRESS_VERIFICATION
```

Why?

Because **READ COMMITTED gives each statement its own snapshot**.

---

# 5. What does "each statement gets its own snapshot" actually mean?

This is the concept you should understand rather than memorize.

Transaction A:

```text
BEGIN

SELECT       ← Snapshot 1
   ↓
ID_VERIFICATION

B updates + COMMIT

SELECT       ← Snapshot 2
   ↓
ADDRESS_VERIFICATION
```

The transaction is still the same transaction.

But the two SELECT statements don't necessarily use the same visibility snapshot under READ COMMITTED.

Therefore:

> **READ COMMITTED does not mean "I see whatever was committed when my transaction started."**

It means roughly:

> **For each statement, see data committed before that statement's snapshot.**

---

# 6. Then why is it called READ COMMITTED?

Because a query doesn't normally see another transaction's **uncommitted** change.

Example:

```text
A:
S100 = ID_VERIFICATION

B:
UPDATE S100 → ADDRESS_VERIFICATION
```

But B hasn't committed yet.

If A runs a normal SELECT, it doesn't simply see:

```text
ADDRESS_VERIFICATION
```

from B's uncommitted work.

A sees a version that is visible according to its snapshot.

So:

```text
UNCOMMITTED change
        ↓
not visible normally

COMMITTED change
        ↓
can become visible to later statements
```

---

# 7. READ COMMITTED vs REPEATABLE READ

This is a classic interview question.

### READ COMMITTED

```text
A SELECT #1
    ↓
ID_VERIFICATION

B UPDATE + COMMIT

A SELECT #2
    ↓
ADDRESS_VERIFICATION
```

Because each statement gets a new snapshot.

### REPEATABLE READ

Conceptually:

```text
A starts transaction
    ↓
snapshot created

A SELECT #1
    ↓
ID_VERIFICATION

B UPDATE + COMMIT

A SELECT #2
    ↓
ID_VERIFICATION
```

A continues working from its transaction's consistent snapshot.

So the mental model is:

```text
READ COMMITTED
→ fresh visibility per statement

REPEATABLE READ
→ consistent snapshot across transaction
```

---

# 8. Does MVCC mean PostgreSQL literally keeps two normal rows?

Don't think of it as:

```text
Row 1
Row 2
```

in the normal logical-table sense.

Instead, PostgreSQL internally maintains **row versions/tuple information** that allows it to determine whether a particular version is visible to a transaction.

Conceptually you can visualize:

```text
Old version
S100 → ID_VERIFICATION
       ↓
       replaced by
       ↓
New version
S100 → ADDRESS_VERIFICATION
```

The important thing for you is not memorizing PostgreSQL's internal tuple fields yet.

Understand:

> **An UPDATE doesn't simply make every transaction immediately see one magical new value. PostgreSQL's MVCC visibility rules determine what each transaction sees.**

---

# 9. Where do locks come into the picture?

Now suppose two requests **both want to modify S100**.

```text
A → UPDATE S100
B → UPDATE S100
```

They cannot both freely modify the same row at exactly the same time without coordination.

PostgreSQL uses locking mechanisms to coordinate conflicting writes.

So:

```text
SELECT
   ↓
MVCC determines visible version

UPDATE
   ↓
MVCC + locking/concurrency control
```

That's why:

> **MVCC does not eliminate locks.**

---

# 10. What is `SELECT FOR UPDATE`?

Normal:

```sql
SELECT *
FROM onboarding_session
WHERE session_id = 'S100';
```

means roughly:

> "Give me the row I can see."

But:

```sql
SELECT *
FROM onboarding_session
WHERE session_id = 'S100'
FOR UPDATE;
```

means:

> "Give me this row, and I'm going to modify it; lock it so conflicting updates have to coordinate with me."

This is **pessimistic locking**.

---

# 11. Why is `FOR UPDATE` called pessimistic?

Because you're assuming:

> "There may be another transaction trying to modify this row, so I'll protect the row before I perform my critical update."

Conceptually:

```text
Transaction A
     ↓
SELECT FOR UPDATE S100
     ↓
row locked
     ↓
modify S100
     ↓
COMMIT
     ↓
lock released
```

If B tries to perform a conflicting operation while A holds the lock, B may have to wait.

---

# 12. `FOR UPDATE` vs Optimistic Locking

This distinction is worth keeping in a small table:

| Approach           | Basic idea                                                      |
| ------------------ | --------------------------------------------------------------- |
| MVCC               | Which row version can I see?                                    |
| `FOR UPDATE`       | Lock row before modifying                                       |
| Optimistic locking | Try update only if version is still what I expected             |
| Idempotency        | Prevent same business operation from being processed repeatedly |

These solve **different problems**.

---

# 13. Why not use `FOR UPDATE` everywhere?

Because locks have a cost.

Imagine:

```text
BEGIN

SELECT FOR UPDATE S100

        ↓

Call Identity Service
        ↓
network
        ↓
OCR
        ↓
5 seconds

UPDATE S100

COMMIT
```

You've potentially held a database transaction/row lock while waiting on a network dependency.

That's dangerous at scale.

Imagine 1,000 requests doing this.

You can get:

```text
long transactions
      ↓
rows locked longer
      ↓
connections occupied
      ↓
connection pool pressure
      ↓
requests wait
      ↓
latency increases
```

Therefore:

> **Keep database transactions short, especially around external network calls.**

This is a very important architectural principle.

---

# 14. What is `FOR UPDATE SKIP LOCKED`?

Suppose ten background workers are processing jobs.

Worker A has locked:

```text
Job 1
```

Worker B doesn't necessarily want to wait for Job 1.

With:

```sql
SELECT ...
FOR UPDATE SKIP LOCKED;
```

B can skip locked rows and take another available row.

Conceptually:

```text
Jobs:

1 → LOCKED
2 → AVAILABLE
3 → AVAILABLE

Worker B
   ↓
SKIP LOCKED
   ↓
takes Job 2
```

This is especially useful for **worker/job/outbox-style processing**.

Don't automatically use it for normal customer API requests.

---

# 15. MVCC vs Idempotency

Another important distinction:

### MVCC

Protects/controls **data visibility and concurrency**.

### Idempotency

Protects against **repeated execution of the same business operation**.

Example:

Customer clicks:

```text
"Verify Identity"
```

Request reaches server.

Network times out.

Customer retries.

Now you may have:

```text
Request 1 → Identity verification
Request 2 → same identity verification
```

Idempotency answers:

> "Are these actually the same business operation?"

MVCC doesn't solve that.

---

# 16. The complete mental model

When you see a concurrent database problem, ask these questions in order:

```text
1. What data is each transaction trying to READ?
             ↓
        MVCC / isolation
             ↓
2. What data is each transaction trying to MODIFY?
             ↓
        Locks / concurrency control
             ↓
3. Can the same business request arrive again?
             ↓
        Idempotency
             ↓
4. Are multiple services involved?
             ↓
        Distributed consistency / Saga
             ↓
5. Is DB + Kafka involved?
             ↓
        Outbox / event consistency
```

This is much better than memorizing individual keywords.

---

# 17. Interview-ready answers

### Q: What is MVCC?

> **MVCC is PostgreSQL's mechanism for managing concurrent access by maintaining row-version visibility, allowing transactions to see the appropriate committed version of data according to their isolation level.**

---

### Q: Is MVCC optimistic locking?

> **No. MVCC is a database concurrency and visibility mechanism. Optimistic locking is an explicit conflict-detection technique, usually implemented using a version column and conditional update.**

---

### Q: What happens under READ COMMITTED?

> **Each SQL statement gets its own snapshot, so a later SELECT in the same transaction can see a change committed by another transaction after the first SELECT.**

---

### Q: Why use `SELECT FOR UPDATE`?

> **When I need to serialize a short critical section involving a particular row, I can pessimistically lock that row so conflicting modifications have to coordinate.**

---

### Q: Why shouldn't I hold `FOR UPDATE` while calling another service?

> **Because the external call can be slow or unavailable, causing the database transaction and lock to remain open unnecessarily, increasing contention and connection-pool pressure.**

---

### Q: What's the biggest conceptual distinction?

Remember this:

```text
MVCC
"What version can I SEE?"

FOR UPDATE
"Who gets to MODIFY this row now?"

Optimistic locking
"Did someone MODIFY it since I read it?"

Idempotency
"Have I already PROCESSED this business request?"

Isolation level
"How consistent should my view of concurrent changes be?"
```

If these five questions are clear in your head, **you don't need to memorize the definitions** — you can derive the answers during an interview.


why no network call inside a BD
On paper, keeping the PostgreSQL transaction open during the external call seems safer because it gives us strict local consistency. But in practice, I try to avoid holding database transactions across network boundaries.

Here’s why: an external API call to the Identity Service introduces unpredictable network latency. If that service slows down or goes offline, our database transaction stays open. That means we’re holding onto active row locks and hogging a connection from our DB connection pool. Under high traffic, this quickly leads to lock contention, connection pool exhaustion, and eventually brings down the entire application.

Because of that, I prefer to keep local database transactions as short and fast as possible.

To handle cross-service consistency, I separate the two operations. I’ll perform a quick local transaction first, make the external API call outside of that transaction, and then handle any failure or inconsistency asynchronously—using patterns like idempotency, retries, compensating transactions, or background reconciliation."

some concepts top remember
MVCC
→ database visibility

SELECT FOR UPDATE
→ pessimistic row locking

Optimistic locking
→ detect stale modification

Idempotency
→ safely retry same business operation

Saga
→ coordinate distributed business operation

Compensation
→ reverse/neutralize a completed operation when necessary

Reconciliation
→ repair state when outcome remains uncertain
                    FAILURE
                       │
        ┌──────────────┼──────────────┐
        ↓              ↓              ↓
   NOT COMPLETED     COMPLETED      UNKNOWN
        │              │              │
        ↓              ↓              ↓
      RETRY       COMPENSATION    RECONCILE /
                                  IDEMPOTENT RETRY

                 Distributed Failure
                      ↓
              What happened?
                      ↓
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
   Not completed   Completed      Unknown
        ↓             ↓             ↓
      Retry       Compensation   Idempotent
                                  retry/status
                                      ↓
                                still unresolved
                                      ↓
                                 Reconciliation          


                                 hat happens if your Saga Orchestrator crashes halfway through the onboarding workflow?"

Don't say:

"We'll compensate everything."

Instead:

"The Orchestrator should not rely only on in-memory state. The workflow state and progress need to be durably persisted, for example in the onboarding database. If the Orchestrator crashes, another instance or a recovery process can read incomplete workflows, determine the last durable state, and resume the Saga. If the outcome of the previous distributed operation is ambiguous, we use idempotent retry, status checking, or reconciliation. Compensation is used only when a previously completed business action needs to be reversed or neutralized."                     
Why did you choose orchestration instead of choreography?

You can say:

"For a multi-step onboarding workflow with several synchronous and asynchronous services and explicit failure-handling requirements, orchestration gives us a centralized view of the workflow. It makes state transitions, sequencing, retries and compensating actions easier to reason about and operate. With choreography, as the number of services and event-driven dependencies grows, the workflow logic becomes distributed across services, which can make the end-to-end flow harder to understand and modify. The trade-off is that the orchestrator itself needs to be highly available and its workflow state must be persisted so that it doesn't become a single point of failure."

"Identity takes 8 seconds. Would you make the call asynchronous?"

A strong answer is:

"I wouldn't choose asynchronous communication solely because the call takes eight seconds. I'd first determine whether the onboarding workflow requires the identity result immediately. If the customer cannot proceed without the result, synchronous communication can still be appropriate with bounded timeouts and proper failure handling. If identity verification can happen independently and the workflow can remain in a pending state, I'd prefer asynchronous processing. In that case, Onboarding can persist the pending state and publish an identity-verification request, Identity can process it independently, and the result can later update the workflow. This reduces long-lived request occupancy, but introduces eventual consistency, retries, idempotency and failure-recovery concerns."

"How do you solve the problem where DB commit succeeds but Kafka publish fails?"

Say:

"I'd use a Transactional Outbox. In the same local database transaction, I update the business state and insert an outbox record representing the event that needs to be published. After commit, a separate publisher reads the durable outbox record and publishes it to Kafka. If Kafka is unavailable, the event remains in the outbox and can be retried later, so the original database transaction doesn't depend on Kafka availability. Since a publisher can crash after publishing but before marking the outbox record as sent, duplicate delivery is still possible, so consumers should be idempotent."

Interview-Ready Script
If the interviewer asks: "What if the external API call succeeds, but the application crashes before the local DB transaction commits?"

You can answer:

"This is a classic 'Dual-Write' problem because we cannot atomically commit a local database transaction alongside an external network call.

I would address this in two ways:

Idempotent External APIs: Pass the event_id as an Idempotency Key to the external provider. If a crash occurs and Kafka redelivers the event, the external provider detects the duplicate key and returns the previously computed result without re-executing side effects.

Decouple Network Calls from DB Transactions: Avoid synchronous external HTTP calls inside local database transactions. Instead, persist the Inbox record and a pending job state in a short local transaction first, then process the external network call asynchronously via a background worker or Outbox pattern."