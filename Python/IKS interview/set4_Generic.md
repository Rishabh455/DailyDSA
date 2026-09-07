# 30 High-Probability Interview Questions Based on My Resume

## Python

### 1. Why did you choose Python for backend development?

**Answer:**

Python provides a clean syntax, strong ecosystem and libraries for web development, automation, data processing and AI/ML.

For my projects, Python works particularly well because the same ecosystem supports FastAPI/Flask for backend services and also integrates easily with AI/ML components.

---

### 2. What is the difference between a list, tuple, set and dictionary in Python?

**Answer:**

* **List** → ordered and mutable collection.
* **Tuple** → ordered and immutable collection.
* **Set** → unordered collection of unique elements.
* **Dictionary** → key-value collection.

For example, I would use a dictionary when I need fast key-based lookup and a tuple when the data should not be modified.

---

### 3. What is the difference between `is` and `==` in Python?

**Answer:**

`==` compares **values**, whereas `is` checks whether two variables refer to the **same object in memory**.

```python
a == b
```

means value comparison.

```python
a is b
```

means identity comparison.

---

### 4. What are decorators in Python? Where would you use them in a backend application?

**Answer:**

A decorator modifies or extends the behavior of a function without changing its actual implementation.

In backend development, decorators can be useful for concerns such as:

* Logging
* Authentication
* Authorization
* Timing
* Validation
* Caching

FastAPI itself also uses decorators to define API routes.

---

### 5. What is the difference between shallow copy and deep copy?

**Answer:**

A shallow copy creates a new outer object but nested objects may still be shared.

A deep copy recursively creates independent copies of nested objects.

This matters when working with nested mutable data structures because modifying a shared nested object in a shallow copy can affect the original object.

---

### 6. How does Python handle memory management?

**Answer:**

Python manages memory automatically.

It primarily uses **reference counting** along with a garbage collector for cyclic references.

As a developer, I still need to be careful about unnecessarily retaining large objects, global references, caches and resources because poor application design can still cause memory issues.

---

## FastAPI / Backend

### 7. What is dependency injection in FastAPI?

**Answer:**

Dependency Injection allows common functionality to be provided to API endpoints instead of creating everything directly inside each endpoint.

For example:

```text
Request
 ↓
Authentication Dependency
 ↓
API Endpoint
 ↓
Service
```

It improves code reuse, separation of concerns and testability.

---

### 8. What is middleware in FastAPI?

**Answer:**

Middleware is code that runs around the request/response lifecycle.

It can be used for things such as:

* Logging
* Request tracing
* CORS
* Authentication-related processing
* Measuring request duration

For example:

```text
Request
 ↓
Middleware
 ↓
API Endpoint
 ↓
Middleware
 ↓
Response
```

---

### 9. What is Pydantic and why is it important in your project?

**Answer:**

Pydantic is used for data validation and parsing based on Python type hints.

In my FastAPI services, I use Pydantic models to define request and response contracts.

For example, a transaction request can define:

```python
class Transaction(BaseModel):
    amount: float
    account_id: str
    device_id: str
```

If the incoming request doesn't match the expected structure or types, validation happens before the business logic processes it.

---

### 10. How would you version an API?

**Answer:**

For APIs where breaking changes are expected, I would use explicit versioning such as:

```text
/api/v1/transactions
/api/v2/transactions
```

The objective is to allow existing consumers to continue using the older contract while newer consumers migrate to the new version.

I would avoid breaking an existing API contract without a proper migration/deprecation strategy.

---

### 11. What HTTP status codes do you commonly use in REST APIs?

**Answer:**

Some important ones are:

* `200` → successful request
* `201` → resource created
* `204` → successful request with no response body
* `400` → bad request
* `401` → unauthenticated
* `403` → unauthorized/forbidden
* `404` → resource not found
* `409` → conflict
* `422` → validation error
* `500` → internal server error
* `503` → service temporarily unavailable

I choose the status code based on the actual semantics of the failure.

---

### 12. What is CORS and why is it required in a React + FastAPI application?

**Answer:**

CORS stands for Cross-Origin Resource Sharing.

If the React frontend and FastAPI backend are running on different origins, the browser applies same-origin restrictions.

CORS configuration tells the browser which origins are allowed to communicate with the backend.

I would configure it with the required trusted origins rather than allowing every origin using `*` in a sensitive production application.

---

## Flask / Backend Design

### 13. You have worked with both Flask and FastAPI. How would you migrate a Flask API to FastAPI?

**Answer:**

I wouldn't perform a direct code conversion blindly.

I would first understand:

* Existing API contracts
* Dependencies
* Authentication
* Database interactions
* Business logic
* Error handling
* Consumers

Then I would separate business logic from the framework and gradually move the API layer to FastAPI.

I would also maintain backward compatibility and run regression/API tests during migration.

---

### 14. How do you separate business logic from API logic?

**Answer:**

I prefer a layered structure:

```text
API / Controller
      ↓
Service Layer
      ↓
Repository / Integration
      ↓
Database / External Service
```

The API layer handles HTTP-specific concerns.

The service layer handles business rules.

The repository/integration layer handles persistence or external communication.

This makes the business logic easier to test independently.

---

## SQL / Database

### 15. What is normalization and why is it useful?

**Answer:**

Normalization organizes relational data to reduce unnecessary duplication and improve consistency.

For example, instead of repeatedly storing customer information in every transaction record, customer data can be maintained separately and referenced through a relationship.

However, I wouldn't blindly normalize everything. In some read-heavy scenarios, controlled denormalization can improve performance.

---

### 16. What is the difference between INNER JOIN and LEFT JOIN?

**Answer:**

**INNER JOIN** returns only records having matching values in both tables.

**LEFT JOIN** returns all records from the left table and matching records from the right table. If there's no match, the right-side values are `NULL`.

The choice depends on whether I need only matching records or all records from the primary dataset.

---

### 17. What is a database transaction?

**Answer:**

A transaction groups multiple database operations into a logical unit.

The important properties are represented by **ACID**:

* Atomicity
* Consistency
* Isolation
* Durability

For banking applications, transactions are especially important because partial updates can lead to inconsistent financial data.

---

### 18. What is connection pooling and why is it useful?

**Answer:**

Opening a new database connection for every request is expensive.

Connection pooling maintains a pool of reusable database connections.

```text
API Requests
    ↓
Connection Pool
 ↓   ↓   ↓
DB Connections
```

This reduces connection-creation overhead and helps efficiently handle concurrent requests.

---

## React / TypeScript

### 19. What is the Virtual DOM in React?

**Answer:**

The Virtual DOM is an in-memory representation of the UI.

When state changes, React creates a new representation, compares it with the previous one through reconciliation, determines the required changes, and updates the actual DOM accordingly.

This helps React efficiently update the UI.

---

### 20. What is the difference between `useState` and `useEffect`?

**Answer:**

`useState` is used to manage component state.

`useEffect` is used to perform side effects such as:

* API calls
* Subscriptions
* Timers
* Synchronizing with external systems

For example:

```text
useState → manage data
useEffect → perform side effect
```

---

### 21. Why use TypeScript instead of plain JavaScript?

**Answer:**

TypeScript adds static typing to JavaScript.

It helps catch many errors during development, improves IDE support and makes large applications easier to maintain.

For enterprise applications, TypeScript also makes API contracts and data structures clearer.

---

### 22. How would you optimize a slow React application?

**Answer:**

First, I would identify the actual bottleneck using browser profiling and React performance tools.

Depending on the issue, I might use:

* Component memoization
* `useMemo`
* `useCallback` where justified
* Lazy loading
* Code splitting
* Pagination
* Avoiding unnecessary re-renders
* Optimizing API calls

I wouldn't blindly add memoization everywhere because it also introduces complexity.

---

## Testing

### 23. What is the difference between unit testing and integration testing?

**Answer:**

**Unit testing** tests a small isolated component, such as a function or service.

**Integration testing** verifies that multiple components work together.

For example:

```text
Unit:
Service function → Test

Integration:
API → Service → Database → Test
```

I use unit tests for business logic and integration/API tests for validating interactions between components.

---

### 24. How do you mock an external service in tests?

**Answer:**

I would mock the external dependency instead of making a real external call during a unit test.

For example:

```text
Test
 ↓
FastAPI Service
 ↓
Mocked Fraud/External Service
```

This makes tests faster, deterministic and independent of external service availability.

For integration testing, I would test against an appropriate test environment or controlled dependency.

---

### 25. A developer says "all my tests are passing", but production still fails. Why?

**Answer:**

Passing tests don't guarantee production correctness.

Possible reasons include:

* Missing test scenarios
* Environment differences
* Incorrect configuration
* Integration issues
* Data differences
* External service behavior
* Concurrency issues
* Production-scale load

That's why testing needs multiple levels: unit, integration, API, regression and appropriate environment/UAT validation.

---

## CI/CD / DevOps

### 26. Explain a CI/CD pipeline for your FastAPI application.

**Answer:**

A typical pipeline could be:

```text
Developer Commit
      ↓
Build
      ↓
Lint / Static Checks
      ↓
Unit Tests
      ↓
Integration/API Tests
      ↓
Build Docker Image
      ↓
Security Checks
      ↓
Deploy to Environment
      ↓
Smoke Tests
```

The exact stages depend on the organization's CI/CD platform and deployment strategy.

---

### 27. Why do we use Docker for backend applications?

**Answer:**

Docker packages the application together with its runtime dependencies into a consistent container image.

This reduces the common:

> "It works on my machine."

problem.

It also makes deployment and scaling more consistent across environments.

---

### 28. Your deployment succeeds but the application immediately starts returning 500 errors. What do you check?

**Answer:**

I would investigate in this order:

1. Application/container logs
2. Environment variables
3. Secrets
4. Database connectivity
5. External service connectivity
6. Dependency/version issues
7. Health checks
8. Resource limits
9. Recent code/configuration changes

I would first identify the root cause before rolling back or making infrastructure changes.

---

## GenAI / RAG

### 29. What is the difference between traditional search and vector search?

**Answer:**

Traditional keyword search generally looks for matching words or terms.

Vector search represents text as embeddings and compares semantic similarity.

For example, a query like:

> "How can I reset my password?"

can retrieve a document containing:

> "Procedure for changing your account credentials"

even though the exact words are different.

This is why vector search is useful for semantic retrieval in RAG systems.

---

### 30. How would you reduce hallucinations in an enterprise RAG application?

**Answer:**

I would not assume that simply adding RAG eliminates hallucinations.

I would improve the complete pipeline:

* Better document chunking
* Better retrieval
* Metadata filtering
* Appropriate top-K selection
* Strong prompts
* Grounding responses in retrieved context
* Instructing the model not to answer when sufficient evidence isn't available
* Evaluation of retrieval and generation quality
* Access control for retrieved documents

For enterprise use, I would also log and evaluate responses so the system can continuously improve.

---

# ⭐ Resume-Based Questions You Should Be Ready For

There are also several questions hidden inside your resume statements that the interviewer can pick at any moment:

### "You have 3+ years of experience. Why should I consider you for a senior role?"

Be prepared to demonstrate **ownership, architecture understanding, debugging, production experience and decision-making**, not just years of experience.

### "You worked on 20+ production releases. Tell me about one difficult release."

Have **one real story** ready.

### "You resolved 60+ production/UAT issues. Give me your most difficult issue."

Have **one real debugging story** ready with:

```text
Problem
→ Investigation
→ Root Cause
→ Fix
→ Testing
→ Production Validation
→ Prevention
```

### "You have AI/GenAI on your resume. How deeply have you actually worked with it?"

Be honest about your exact level of ownership.

### "You worked on a banking application. What makes banking systems different from normal applications?"

Talk about:

**Security + consistency + auditability + reliability + data sensitivity + failure handling.**

### "Which part of the full stack are you strongest in?"

Your strongest positioning should be:

**Python/FastAPI backend + API/service development**, while demonstrating that you can work across React, SQL and integration layers.

---

# 🔥 Final Preparation Priority

If your interview is very soon, I would prepare your resume in this order:

**1. Every single bullet on your resume**

**2. Your current banking project**

**3. Fraud Detection architecture**

**4. FastAPI + Python**

**5. SQL + your 20% optimization claim**

**6. Production issue + debugging story**

**7. React + TypeScript**

**8. Testing/PyTest**

**9. CI/CD + Docker**

**10. GenAI/RAG**

The most dangerous interviewer question isn't necessarily a difficult technical question.

It's:

> **"You have mentioned this on your resume. Tell me exactly what YOU did."**

For every technology and project bullet on your resume, you should be able to answer that question in **30–60 seconds with a concrete example**.
