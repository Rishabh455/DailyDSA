Bilkul bhai. Neeche **30 database interview questions ke saath interview-ready answers** de raha hoon—answers ko tum interview mein **naturally bol sakte ho**, ratta-style nahi. Main tumhare resume/JD ke according answers frame kar raha hoon. Tumhare resume mein PostgreSQL/MySQL, joins, indexing, stored procedures aur query optimization explicitly mentioned hain, aur onboarding project mein SQL/database optimization ka experience bhi hai.  

---

# 🔥 30 Database Questions + Interview-Ready Answers

## 1. Which database have you used in your project?

**Answer:**

> "In my projects, I have primarily worked with relational databases such as MySQL and PostgreSQL. In the onboarding and KYC application, we stored structured business data such as customer information, application details, document metadata, application status and other workflow-related information in SQL tables.
>
> The actual PDF or image documents were maintained in secure object storage, while their metadata and references were maintained in the database."

This answer directly aligns with your project description. 

---

# 2. Why did you use SQL instead of MongoDB?

**Answer:**

> "The onboarding and KYC domain has highly structured and relational data. For example, a customer can have multiple applications, and an application can have multiple documents and verification records.
>
> Because we need relationships, referential integrity, transactional consistency and complex queries across these entities, a relational database is a natural choice.
>
> MongoDB would be more suitable when the data is highly flexible or schema changes frequently, but for the core transactional data I would prefer SQL."

### Follow-up:

**Interviewer:** "So SQL is always better?"

> "No. The choice depends on the access pattern and consistency requirements. SQL is strong for structured transactional workloads, while NoSQL can be useful for flexible schemas, very high-scale distributed workloads, or specific access patterns."

---

# 3. Explain normalization with your project example.

**Answer:**

> "Normalization is the process of organizing data to reduce redundancy and improve data integrity.
>
> For example, instead of storing customer information repeatedly for every application, I would maintain a Customer table and reference it from an Application table using customer_id.
>
> Similarly, documents can reference application_id rather than duplicating application information.
>
> This gives us cleaner data and avoids update inconsistencies."

Example:

```text
Customer
---------
customer_id
name
email

Application
-----------
application_id
customer_id
status

Document
--------
document_id
application_id
document_type
status
```

---

# 4. What is denormalization and when would you use it?

**Answer:**

> "Denormalization means intentionally storing some redundant data to improve read performance.
>
> For example, if a reporting API frequently needs customer name, application status and risk status together, instead of performing several joins every time, we might maintain a read-optimized representation.
>
> But I wouldn't denormalize by default because it introduces data duplication and consistency challenges."

---

# 5. Primary Key vs Foreign Key vs Unique Key?

**Answer:**

> "A primary key uniquely identifies each row in a table and cannot contain NULL values.
>
> A foreign key establishes a relationship between two tables by referencing a key in another table.
>
> A unique key ensures that values in a column or combination of columns are unique, although its NULL behavior depends on the database."

Example:

```sql
customers
---------
customer_id PRIMARY KEY

applications
------------
application_id PRIMARY KEY
customer_id FOREIGN KEY
```

---

# 6. Explain INNER JOIN and LEFT JOIN.

**Answer:**

> "INNER JOIN returns only matching records from both tables.
>
> LEFT JOIN returns all records from the left table and matching records from the right table. If there is no match, the right-side columns become NULL."

For example:

> "If I want all customers who have applications, I can use INNER JOIN. If I want all customers including those who haven't submitted any application, I would use LEFT JOIN."

---

# 7. WHERE vs HAVING?

**Answer:**

> "WHERE filters individual rows before grouping, whereas HAVING filters groups after GROUP BY and aggregation."

Example:

```sql
SELECT customer_id, COUNT(*)
FROM applications
WHERE status = 'APPROVED'
GROUP BY customer_id
HAVING COUNT(*) > 2;
```

Here:

```text
WHERE  → filters applications
GROUP BY → creates customer groups
HAVING → filters those groups
```

---

# 8. DELETE vs TRUNCATE vs DROP?

**Answer:**

> "DELETE removes selected rows and can use a WHERE condition.
>
> TRUNCATE removes all rows from a table and is generally faster for removing the complete dataset.
>
> DROP removes the table itself, including its structure."

Simple:

```text
DELETE   → remove rows
TRUNCATE → remove all rows
DROP     → remove table
```

---

# 9. You mentioned 20% query optimization. How exactly did you achieve it?

🔥 **Very important for your interview.**

**Answer:**

> "I first identified the queries contributing to the performance issue rather than directly adding indexes.
>
> I analyzed the query execution and looked for things like unnecessary full table scans, inefficient joins, missing indexes and unnecessary data retrieval.
>
> Based on that analysis, I optimized the SQL queries and database access patterns, including improving filtering and indexing where appropriate.
>
> I then compared the query/data-processing performance before and after the changes. Overall, this contributed to approximately 20% improvement in the relevant query and data-processing efficiency."

Your resume explicitly claims approximately 20% improvement, so expect the interviewer to challenge you on the **exact measurement and bottleneck**. 

### If he asks:

**"What was the exact query?"**

You should use the **actual query from your project** rather than inventing one.

---

# 10. How would you troubleshoot a query taking 5 seconds?

**Answer:**

> "First I would reproduce the issue and identify the exact query.
>
> Then I would inspect its execution plan using EXPLAIN or EXPLAIN ANALYZE.
>
> I would check whether there is a full table scan, whether indexes are being used, how joins are executed, how many rows are being processed, and whether sorting or filtering is expensive.
>
> Based on that, I could optimize the query, add or modify indexes, reduce unnecessary joins or columns, or change the data-access pattern.
>
> Finally, I would benchmark the optimized query and monitor it in production."

---

# 11. What is an index?

**Answer:**

> "An index is a data structure that helps the database locate rows faster without scanning the entire table.
>
> For example, if applications are frequently searched by customer_id, an index on customer_id can significantly improve lookup performance."

```sql
CREATE INDEX idx_application_customer
ON applications(customer_id);
```

But:

> "Indexes also consume storage and increase write overhead, because inserts and updates may need to update the index."

---

# 12. When should you NOT create an index?

**Answer:**

> "I wouldn't blindly create indexes on every column.
>
> Indexes are less useful for columns with very low selectivity, columns that are rarely used in filtering or joining, or tables where there are extremely frequent writes and the index doesn't provide enough read benefit.
>
> I would look at the actual query patterns and execution plans before adding an index."

---

# 13. What is a composite index?

**Answer:**

> "A composite index is an index created on multiple columns."

Example:

```sql
CREATE INDEX idx_app_customer_status
ON applications(customer_id, status);
```

This can help queries such as:

```sql
WHERE customer_id = 100
AND status = 'PENDING'
```

### Tricky follow-up:

**"Will it help if I search only by status?"**

> "Generally, an index beginning with customer_id is not ideal for a query filtering only on status because of the leftmost-prefix principle."

---

# 14. What is EXPLAIN ANALYZE?

**Answer:**

> "EXPLAIN shows the query execution plan chosen by the database, including operations such as sequential scans, index scans and joins.
>
> EXPLAIN ANALYZE actually executes the query and provides runtime information, such as actual execution time and rows processed.
>
> I use it to identify where the query is spending time and whether the optimizer is choosing an efficient execution plan."

---

# 15. What is a full table scan?

**Answer:**

> "A full table scan means the database examines rows across the table to find matching records rather than using an efficient index lookup.
>
> For a small table this may be perfectly acceptable, but for millions of records it can become expensive, especially for frequently executed queries."

---

# 16. What is ACID?

**Answer:**

> "ACID represents Atomicity, Consistency, Isolation and Durability.
>
> Atomicity means the transaction is all-or-nothing.
>
> Consistency means the database remains in a valid state.
>
> Isolation controls how concurrent transactions interact.
>
> Durability means committed data survives failures."

### Banking example:

> "For a banking transaction, I don't want the debit operation to succeed while the corresponding credit operation fails. Both operations should succeed or the transaction should roll back."

---

# 17. Explain transaction isolation levels.

**Answer:**

> "The common isolation levels are READ UNCOMMITTED, READ COMMITTED, REPEATABLE READ and SERIALIZABLE.
>
> They provide increasing levels of isolation but generally come with increasing concurrency/locking trade-offs."

Know these problems:

```text
READ UNCOMMITTED → Dirty reads possible
READ COMMITTED   → Dirty reads prevented
REPEATABLE READ  → Non-repeatable reads prevented
SERIALIZABLE     → Highest isolation
```

---

# 18. What is a dirty read?

**Answer:**

> "A dirty read occurs when one transaction reads data that another transaction has modified but not yet committed.
>
> If the second transaction rolls back, the first transaction has read data that never actually became permanent."

---

# 19. What is a deadlock?

**Answer:**

> "A deadlock occurs when two or more transactions are waiting for resources locked by each other."

Example:

```text
Transaction A
locks Customer
waits for Application

Transaction B
locks Application
waits for Customer
```

Both are waiting.

> "We can reduce deadlocks by keeping transactions short, acquiring locks in a consistent order, using appropriate indexes and handling deadlock retries where appropriate."

---

# 20. What happens if two requests update the same application simultaneously?

**Answer:**

> "This is a concurrency problem.
>
> Depending on the business requirement, I could use database transactions with appropriate isolation, pessimistic locking, or optimistic concurrency control.
>
> For example, with optimistic locking I could maintain a version column and update the record only if the version hasn't changed."

Example:

```sql
UPDATE applications
SET status = 'APPROVED',
    version = version + 1
WHERE application_id = 101
AND version = 5;
```

If zero rows are updated, another request may have already modified it.

---

# 21. Optimistic vs pessimistic locking?

**Answer:**

> "Optimistic locking assumes conflicts are relatively rare. We allow concurrent access and detect conflicts during update, typically using a version number.
>
> Pessimistic locking acquires a lock before modifying the data so that other transactions cannot modify it simultaneously.
>
> I would prefer optimistic locking where conflicts are relatively uncommon and high concurrency is important, while pessimistic locking can be appropriate when conflicting updates must be prevented immediately."

---

# 22. What is a stored procedure?

**Answer:**

> "A stored procedure is a predefined set of SQL statements stored and executed within the database.
>
> It can encapsulate business or data-processing logic and reduce repeated SQL from the application layer.
>
> However, I would avoid putting too much business logic into stored procedures because it can make application logic harder to version, test and maintain."

Since **stored procedures are explicitly on your resume**, prepare a concrete example from your actual experience. 

---

# 23. What is database connection pooling?

**Answer:**

> "Opening a new database connection for every request is expensive.
>
> Connection pooling maintains a pool of reusable database connections. When an API request needs the database, it obtains an available connection from the pool and returns it after completing the operation.
>
> This reduces connection-creation overhead and improves performance under concurrent traffic."

### FastAPI follow-up:

> "For a FastAPI application handling concurrent requests, I would configure an appropriate DB connection pool size rather than allowing unlimited connections."

---

# 24. In microservices, should every service have its own database?

🔥 **Very likely with your interviewer.**

**Answer:**

> "Ideally, in a microservice architecture, each service should own its data and expose it through APIs or events rather than allowing other services to directly modify its database.
>
> For example, the Document Service should own document-related data, while the Onboarding Service owns onboarding lifecycle data.
>
> This reduces coupling and allows services to evolve independently."

Your project is explicitly described as a microservices-based onboarding/KYC platform with separate capabilities including Onboarding, Document, KYC/Verification and AML/Risk. 

---

# 25. If each microservice has its own database, how do you maintain consistency?

**Answer:**

> "We shouldn't necessarily try to maintain strong ACID consistency across every service.
>
> Instead, we can use asynchronous events and eventual consistency where the business process allows it.
>
> For workflows requiring multiple services, patterns such as Saga can be used, where each service performs its local transaction and failures are handled using compensating actions."

---

# 26. What if KYC fails after onboarding data has already been saved?

**Answer:**

> "I would first define whether the workflow requires strong consistency or can tolerate eventual consistency.
>
> In a microservices architecture, I would generally avoid a distributed database transaction across all services.
>
> Instead, the workflow can be modeled as a Saga. Each service commits its local transaction, and if a later step fails, a compensating action can bring the overall business workflow to an appropriate state."

Example:

```text
Create Application
       ↓
Upload Document
       ↓
Document Processing
       ↓
KYC Verification
       ↓
KYC FAILED
       ↓
Application → REVIEW / REJECTED
```

---

# 27. Why not use one database transaction across all microservices?

**Answer:**

> "A distributed transaction such as two-phase commit introduces strong coupling between services and can increase latency and reduce availability.
>
> It also becomes more complicated when services fail independently.
>
> In a microservices architecture, I would generally prefer local transactions combined with events, eventual consistency and Saga-style compensation where appropriate."

---

# 28. Where would you use SQL vs NoSQL in your fraud system?

**Answer:**

> "For structured transactional information such as customer, transaction and decision records, a relational database can be a good choice because of consistency and relational querying.
>
> For highly flexible or rapidly changing data, such as certain dynamic feature payloads, event-oriented data or semi-structured information, a NoSQL solution could be considered.
>
> However, I would make the decision based on actual access patterns, scale, consistency and latency requirements rather than choosing NoSQL simply because the system is high-volume."

Your resume describes transaction attributes including amount, frequency, device, location, customer and merchant information, with a FastAPI service integrating with risk-scoring components. 

---

# 29. Your fraud system needs thousands of transactions per second. How would you design the database layer?

🔥 **Senior-level question.**

**Answer:**

> "I wouldn't design the system around a single database receiving every operation synchronously.
>
> First, I would identify which data needs to be strongly consistent and immediately available for the decision versus which data can be processed asynchronously.
>
> For the real-time path, I would optimize database access using connection pooling, appropriate indexes, efficient queries and possibly caching for frequently accessed data.
>
> For high-volume historical or event data, I would consider partitioning, asynchronous event processing and appropriate storage optimized for analytics.
>
> I would also monitor database latency, connection utilization and query performance to identify bottlenecks."

Your resume specifically says the fraud service is being designed for **low-latency real-time evaluation** and uses FastAPI async capabilities for I/O-bound communication with downstream services, databases and APIs. 

---

# 30. Production database suddenly becomes slow. What will you do?

🔥🔥 **MOST IMPORTANT SCENARIO**

**Answer:**

> "I would first determine whether the problem is actually database-related or whether the application or downstream service is causing the latency.
>
> I would check application and database metrics, connection pool utilization, active connections, CPU, memory and disk I/O.
>
> Then I would identify the slow or expensive queries and inspect their execution plans using EXPLAIN ANALYZE.
>
> I would also check for locks, blocking queries and deadlocks.
>
> I would compare the current behavior with recent deployments or database changes.
>
> For immediate mitigation, depending on the root cause, I might terminate a problematic query, adjust traffic, roll back a problematic deployment or temporarily scale resources.
>
> After mitigation, I would fix the root cause—for example, query optimization, indexing, connection-pool tuning or data-access changes—and then monitor the system after deployment."

---

# 🚨 5 Questions Where This Interviewer Can Trap You

Because the interviewer is an **Enterprise Architecture leader**, don't stop after the first answer. He may go deeper:

### 1. "You said you optimized the query. Show me exactly what you changed."

Be prepared with:

```text
BEFORE
↓
Problem
↓
EXPLAIN
↓
Root cause
↓
Change
↓
AFTER
↓
Measurement
```

### 2. "Why SQL? Why not MongoDB?"

Don't answer:

> "SQL is faster."

Instead discuss:

**consistency + relationships + transactions + schema + access pattern.**

---

### 3. "Every microservice has its own DB. How do you JOIN data?"

Correct direction:

> **You generally don't directly JOIN across service databases.**

Use:

```text
API composition
Events
Read models
CQRS where justified
Data synchronization
```

---

### 4. "How will your fraud system handle 10,000 TPS?"

Don't immediately say:

> "Use async FastAPI."

Async helps with **I/O concurrency**, but it doesn't magically make the database capable of 10,000 TPS.

Talk about:

```text
Connection pooling
Caching
Indexes
Partitioning
Read/write separation
Event-driven processing
Horizontal scaling
Database capacity
```

---

### 5. "What exactly did YOU do with the database?"

This is **extremely important**.

Your resume says you worked on database operations and SQL queries and contributed to approximately 20% improvement. 

So be ready to clearly separate:

> **"I personally implemented/analyzed..."**

from

> **"The overall architecture/team handled..."**

Don't accidentally claim database architecture decisions that you didn't actually make.
