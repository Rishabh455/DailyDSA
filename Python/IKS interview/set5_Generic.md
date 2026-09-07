# 30 More High-Probability Interview Questions

## 1. Walk me through one feature you developed from requirement to production.

**Answer:**

I start by understanding the business requirement and identifying the impacted frontend, backend, database and downstream components.

Then I design the API contract, implement the FastAPI/Flask changes, add Pydantic validation and business logic, integrate with the required database or service, and write unit/API tests.

After testing, I support UAT, resolve issues and validate the change after production deployment.

---

## 2. How do you understand a requirement when the business requirement is unclear?

**Answer:**

I don't start coding immediately.

I clarify:

* Expected business behavior
* Input/output
* Edge cases
* Error scenarios
* Existing system behavior
* Dependencies
* Performance/security requirements

Then I document or confirm the expected behavior with the relevant stakeholder before implementation.

---

## 3. How do you estimate the effort for a new feature?

**Answer:**

I break the feature into smaller technical tasks:

```text
Requirement
 ↓
API changes
 ↓
Business logic
 ↓
Database changes
 ↓
Frontend changes
 ↓
Integration
 ↓
Testing
 ↓
Deployment
```

Then I identify dependencies and risks before estimating.

I also distinguish between development effort and uncertainty caused by external dependencies.

---

## 4. How do you make sure a new change doesn't break existing functionality?

**Answer:**

I use regression testing along with unit and integration/API tests.

Before implementation, I understand which existing flows are affected.

After implementation, I test:

* New functionality
* Existing related functionality
* Positive scenarios
* Negative scenarios
* Edge cases

For critical banking functionality, regression testing is particularly important.

---

## 5. What do you do when you receive a production bug that you cannot reproduce locally?

**Answer:**

I compare the environments and gather production evidence.

I would check:

* Logs
* Request/correlation ID
* Input data
* Configuration
* Database state
* Dependency versions
* External service responses
* Timing/concurrency conditions

I avoid changing code based on assumptions. First I try to establish the root cause using production evidence.

---

# Python — Deeper Questions

## 6. What is the GIL in Python?

**Answer:**

The Global Interpreter Lock allows only one thread at a time to execute Python bytecode within a CPython process.

This means Python threads are not generally useful for achieving CPU-bound parallelism.

However, threads can still be useful for **I/O-bound operations**, because the thread can wait for I/O while another thread executes.

For CPU-heavy workloads, multiprocessing or separate worker processes/services can be considered.

---

## 7. What is the difference between concurrency and parallelism?

**Answer:**

**Concurrency** means multiple tasks can make progress during overlapping periods.

**Parallelism** means tasks actually execute simultaneously, typically using multiple CPU cores.

For example, asynchronous FastAPI processing is useful for handling many I/O-bound operations concurrently, while CPU-heavy workloads may benefit from parallel execution using separate processes.

---

## 8. When would you use `asyncio` in Python?

**Answer:**

I would use asynchronous programming mainly for I/O-bound operations such as:

* Calling external APIs
* Database operations
* Network operations
* File/network I/O

It allows the application to work on other tasks while waiting for I/O.

I wouldn't claim that async automatically improves CPU-heavy processing.

---

## 9. What happens if you call blocking code inside an async FastAPI endpoint?

**Answer:**

Blocking code can prevent the event loop from handling other tasks efficiently.

For example, if an async endpoint performs a long blocking operation, it can reduce concurrency and increase response time.

I would identify blocking operations and either use appropriate async libraries or move heavy/blocking work to a worker/process where appropriate.

---

## 10. What is the difference between multiprocessing and multithreading?

**Answer:**

**Multithreading** uses multiple threads within a process and is useful particularly for I/O-bound work.

**Multiprocessing** uses separate processes and can achieve true CPU parallelism, which can be useful for CPU-heavy workloads.

For a FastAPI application, I would choose based on whether the workload is I/O-bound or CPU-bound.

---

# API / Backend

## 11. What is REST and what makes an API RESTful?

**Answer:**

REST is an architectural style for designing networked APIs.

Important principles include:

* Resource-oriented endpoints
* HTTP methods
* Stateless communication
* Standard HTTP status codes
* Representation of resources
* Separation between client and server

For example:

```text
GET    /transactions/123
POST   /transactions
PUT    /transactions/123
DELETE /transactions/123
```

---

## 12. PUT vs PATCH — what's the difference?

**Answer:**

`PUT` is generally used to replace the complete representation of a resource.

`PATCH` is used for partial updates.

For example, if I only want to update a customer's phone number, PATCH would typically be more appropriate.

---

## 13. What does idempotent API mean?

**Answer:**

An operation is idempotent if performing it multiple times has the same intended effect as performing it once.

For example, a properly designed `PUT` operation is generally idempotent.

For business operations such as transaction processing, I may need an explicit idempotency key to prevent duplicate processing.

---

## 14. How do you handle pagination in an API?

**Answer:**

For large datasets, I wouldn't return everything in a single response.

I could use pagination parameters such as:

```text
?page=1&limit=50
```

or cursor-based pagination for very large or frequently changing datasets.

The choice depends on the data size and access pattern.

---

## 15. How would you implement rate limiting for an API?

**Answer:**

Rate limiting restricts how many requests a client can make within a given period.

It can help protect the API from abuse and excessive traffic.

For a distributed application, I would typically use a shared mechanism such as Redis or an API gateway rather than maintaining counters only inside individual FastAPI instances.

---

# Database — Interviewer Drill

## 16. What is a primary key and why is it important?

**Answer:**

A primary key uniquely identifies each record in a table.

It ensures that records can be uniquely referenced and also provides an important basis for relationships with other tables.

For transaction systems, a unique transaction identifier is particularly important for traceability and idempotency.

---

## 17. What is a foreign key?

**Answer:**

A foreign key establishes a relationship between tables.

For example:

```text
Customer
customer_id

Transaction
transaction_id
customer_id
```

The transaction's `customer_id` can reference the customer table.

This helps maintain referential integrity.

---

## 18. What is a deadlock?

**Answer:**

A deadlock occurs when two or more transactions are waiting for resources held by each other.

For example:

```text
Transaction A → locks Row 1 → waits for Row 2

Transaction B → locks Row 2 → waits for Row 1
```

Neither can proceed.

I would investigate transaction ordering, lock duration, indexes and query behavior to reduce the possibility of deadlocks.

---

## 19. What is database indexing actually doing?

**Answer:**

An index creates a data structure that allows the database to locate records more efficiently without scanning the entire table in many cases.

However, indexes have a trade-off: they consume storage and can increase the cost of writes.

So indexes should be designed based on actual query patterns.

---

## 20. If your database has 100 million transaction records, how would you improve performance?

**Answer:**

I would first identify the actual bottleneck.

Potential approaches include:

* Proper indexing
* Query optimization
* Partitioning
* Archiving old data
* Pagination
* Connection pooling
* Read replicas where appropriate
* Caching for suitable read-heavy workloads

I wouldn't immediately choose partitioning or caching without understanding the workload.

---

# React / Full Stack

## 21. A React page is making the same API call multiple times. How would you debug it?

**Answer:**

I would check:

* `useEffect` dependencies
* Component re-rendering
* Parent-child rendering
* React Strict Mode behavior during development
* Whether the API call is triggered from multiple components
* State updates causing re-renders

Then I would use browser network tools and React profiling to identify the exact source.

---

## 22. What causes unnecessary re-renders in React?

**Answer:**

Common causes include:

* State changes
* Parent component re-rendering
* Changing object/function references
* Incorrect effect dependencies
* Context updates

I would first profile the application and then use techniques such as component decomposition, memoization or stable references where they actually provide benefit.

---

## 23. How would you handle a large list of 100,000 records in React?

**Answer:**

I wouldn't render all 100,000 records at once.

I would consider:

* Server-side pagination
* Filtering/searching on the backend
* Virtualized lists
* Sorting on the backend
* Loading data incrementally

This reduces browser memory usage and rendering overhead.

---

## 24. How do you manage sensitive data on the frontend?

**Answer:**

I avoid storing sensitive information unnecessarily in browser storage.

Authentication/session design should follow the organization's security architecture.

I also ensure:

* HTTPS
* Proper authorization on the backend
* No sensitive data in frontend logs
* No secrets embedded in frontend code
* Proper handling of tokens/session information

Most importantly, frontend checks should never be treated as the final security boundary. Authorization must be enforced by the backend.

---

# Testing / Quality

## 25. What would you test for a Fraud Detection API?

**Answer:**

I would test:

### Functional

* Valid transaction
* Invalid transaction
* Different risk categories
* Approve/review/block scenarios

### Validation

* Missing fields
* Wrong data types
* Invalid values

### Failure scenarios

* ML service unavailable
* Database unavailable
* Timeout
* Invalid downstream response

### Security

* Unauthorized request
* Invalid credentials
* Access-control scenarios

### Performance

* Concurrent requests
* Response latency under expected load

---

## 26. What makes a good unit test?

**Answer:**

A good unit test should be:

* Focused
* Deterministic
* Independent
* Easy to understand
* Fast
* Repeatable

I generally follow the pattern:

```text
Arrange
 ↓
Act
 ↓
Assert
```

The test should clearly establish what behavior is being verified.

---

## 27. What is mocking and why is it useful?

**Answer:**

Mocking replaces a real dependency with a controlled test double.

For example, instead of calling a real external risk service during a unit test, I can mock its response.

This allows me to test my service logic independently and also simulate scenarios such as:

* Success
* Timeout
* HTTP 500
* Invalid response

---

# Production / DevOps

## 28. What information should you NOT put in application logs for a banking application?

**Answer:**

I would avoid logging sensitive information such as:

* Passwords
* Authentication tokens
* Secrets
* Credentials
* Full sensitive personal/financial information

Instead, I would use correlation IDs, transaction/reference IDs where appropriate, timestamps, service information and sanitized error details.

Logging must follow organizational security and compliance policies.

---

## 29. Your application memory keeps increasing over time. How would you investigate?

**Answer:**

I would first determine whether it is an actual memory leak or simply expected memory growth.

I would monitor:

* Process memory
* Request patterns
* Object growth
* Cache size
* Large in-memory collections
* Long-lived references
* Background tasks
* Connection/resource handling

Then I would reproduce the behavior under controlled load and use Python profiling tools to identify objects that aren't being released as expected.

---

## 30. Tell me about a technical decision you made that improved the application.

**Answer:**

One example from my experience is optimizing database operations.

Instead of accepting the existing query behavior, I analyzed the database access pattern and worked on query optimization and indexing where appropriate.

This resulted in approximately a **20% performance improvement for the relevant operations**.

The broader lesson was that I prefer measuring the bottleneck first and then making a targeted optimization rather than prematurely changing the architecture.

---

# 🚨 10 Resume Cross-Questions You Must Prepare

These aren't separate technical topics—they are questions the interviewer can ask **directly from your resume**:

1. **"You wrote 30+ business enhancements. Name three."**

2. **"Which one was technically the most difficult?"**

3. **"You mentioned 10+ functional modules. Which module did you own?"**

4. **"Show me the architecture of that module."**

5. **"You claim 20% database improvement. What was the query before and after?"**

6. **"You resolved 60+ production/UAT issues. Tell me about the hardest one."**

7. **"You have 20+ production releases. What exactly was your responsibility during a release?"**

8. **"You mentioned GenAI and RAG. What did YOU implement versus what was already available?"**

9. **"You call yourself a Full Stack Developer. Which part are you strongest in?"**

10. **"If I remove AI/GenAI from your resume, what backend expertise do you still bring?"**

---

# ⭐ How You Should Position Yourself

Based on your resume, don't try to present yourself as someone who independently architected the entire banking platform.

Your strongest and most defensible positioning is:

```text
Python / FastAPI Backend
        +
REST API Development
        +
Business Logic
        +
SQL / Database Optimization
        +
React / TypeScript
        +
Integration
        +
Testing
        +
Production Support
        +
GenAI / RAG Exposure
```

Your **backend/API ownership** should be the center of the interview.

When the interviewer goes deep into ML, OCR, cloud infrastructure or architecture components you didn't personally implement, use:

> **"That's part of the overall solution, but my direct responsibility was..."**

Then immediately explain your actual contribution technically.

That will sound much stronger than claiming ownership of the entire architecture.
