## 21. You mentioned you worked on 30+ business enhancements. Can you explain one enhancement end-to-end?

**Answer:**

One example is a backend enhancement where I was responsible for understanding the requirement, modifying the FastAPI/Flask API layer, adding request validation using Pydantic, implementing the required business logic, integrating with the database or downstream service, and writing unit/API tests.

I also handled exception scenarios and supported the change through UAT and production deployment.

The important part was not just coding the feature but ensuring that the change didn't impact existing functionality.

---

## 22. How do you design a REST API from scratch?

**Answer:**

I generally follow these steps:

1. Understand the business requirement.
2. Identify resources and operations.
3. Define API endpoints and HTTP methods.
4. Define request/response schemas using Pydantic.
5. Implement validation and business logic.
6. Add authentication and authorization.
7. Handle exceptions consistently.
8. Integrate with DB/downstream services.
9. Add unit and API tests.
10. Document the API using OpenAPI/Swagger.

For example, in the fraud service, the transaction API receives transaction details, validates them, processes the risk evaluation flow, and returns the appropriate decision.

---

## 23. What is the difference between Flask and FastAPI, and when would you choose each?

**Answer:**

Flask is a lightweight and flexible Python web framework, while FastAPI is designed around modern Python type hints and provides features such as Pydantic validation, automatic OpenAPI documentation, and strong support for asynchronous programming.

I would choose FastAPI when building modern APIs or microservices where validation, API documentation and I/O concurrency are important.

Flask is still a good choice for simpler applications or existing systems where its ecosystem and simplicity are sufficient.

---

## 24. How do you handle exception handling in FastAPI?

**Answer:**

I avoid exposing raw exceptions to clients.

I generally use:

* `try/except` for expected application-level failures.
* Custom exceptions for business-specific errors.
* FastAPI exception handlers for consistent error responses.
* Appropriate HTTP status codes.
* Logging with correlation/request IDs.

For example, if a downstream fraud or risk service is unavailable, I would return a controlled response rather than exposing an internal stack trace.

---

## 25. How would you structure a large FastAPI project?

**Answer:**

I would avoid putting everything inside a single `main.py`.

A typical structure would be:

```text
app/
├── main.py
├── api/
│   └── routes/
├── schemas/
├── services/
├── repositories/
├── models/
├── core/
├── exceptions/
├── dependencies/
└── tests/
```

The API layer handles HTTP concerns, the service layer contains business logic, repositories handle database operations, and schemas handle request/response validation.

This improves maintainability and testability.

---

## 26. You mentioned React.js and TypeScript. How does your frontend communicate with FastAPI?

**Answer:**

The React frontend communicates with FastAPI through REST APIs.

The typical flow is:

```text
React Component
      ↓
API/Service Layer
      ↓
FastAPI Endpoint
      ↓
Validation
      ↓
Business Logic
      ↓
Database / Downstream Service
      ↓
JSON Response
      ↓
React UI
```

I generally keep API communication separate from UI components so that components remain focused on presentation and state management.

---

## 27. How do you handle API errors on the React side?

**Answer:**

I handle errors at the API/service layer and then provide appropriate feedback to the UI.

For example:

* `400` → validation/input issue
* `401` → authentication issue
* `403` → authorization issue
* `404` → resource not found
* `500` → server-side issue
* Timeout/network error → connectivity issue

The UI should show a user-friendly message instead of exposing technical backend details.

---

## 28. You mentioned improving database performance by around 20%. How did you achieve it?

**Answer:**

I first identified the slow queries using query analysis and application-level observations.

Then I worked on areas such as:

* Optimizing SQL queries.
* Adding or improving indexes.
* Avoiding unnecessary data retrieval.
* Reducing redundant database calls.
* Reviewing joins and filtering conditions.
* Improving connection handling.

After the changes, I compared the query/application performance against the previous implementation and observed approximately a 20% improvement for the relevant operations.

I would clarify that the improvement was workload-specific rather than claiming a universal 20% improvement.

---

## 29. How do you decide whether to use SQL or NoSQL?

**Answer:**

I choose based on the data and access patterns rather than simply choosing one technology.

**SQL** is preferable when:

* Data has strong relationships.
* Transactions and consistency are important.
* Complex queries and joins are required.

**NoSQL** can be useful when:

* Schema flexibility is important.
* Data is semi-structured.
* Very high-scale distributed access is required.

For banking transactions and core business data, a relational database is generally a strong fit because consistency and transactional integrity are critical.

---

## 30. How would you make your fraud detection API highly scalable?

**Answer:**

I would keep the FastAPI service stateless and horizontally scalable.

A possible architecture would be:

```text
Clients
   ↓
Load Balancer
   ↓
Multiple FastAPI Instances
   ↓
Queue / Downstream Services
   ↓
Database
```

Then I would use:

* Horizontal scaling.
* Database connection pooling.
* Proper indexes.
* Asynchronous I/O where appropriate.
* Queue-based processing for heavy/background workloads.
* Caching where applicable.
* Timeouts and circuit breakers.
* Monitoring and alerting.

For CPU-intensive ML inference, I would consider separating the model-serving workload from the API layer so that both can scale independently.

---

## 31. What is the difference between authentication and authorization?

**Answer:**

**Authentication** answers:

> "Who are you?"

For example, validating a JWT or other credentials.

**Authorization** answers:

> "What are you allowed to do?"

For example, an authenticated user may be allowed to view transactions but not modify risk configurations.

So:

```text
Authentication → Identity
Authorization  → Permissions
```

Both are important in banking applications.

---

## 32. How would you secure a banking/Fraud Detection API?

**Answer:**

I would consider security at multiple layers:

* TLS/HTTPS for data in transit.
* Strong authentication.
* Role-based authorization.
* Input validation using Pydantic.
* Least-privilege access to databases and services.
* Secure secrets management.
* Encryption for sensitive data where required.
* No sensitive information in application logs.
* Audit logging.
* Rate limiting where appropriate.
* Dependency and container security.
* Proper monitoring and alerting.

I would also follow the organization's security and compliance requirements rather than implementing security decisions independently.

---

## 33. You mentioned GenAI, embeddings and RAG. Explain how RAG works.

**Answer:**

RAG stands for Retrieval-Augmented Generation.

Instead of directly asking an LLM to answer from its existing knowledge, we first retrieve relevant information from a knowledge base.

The flow is:

```text
User Query
    ↓
Embedding
    ↓
Vector Search
    ↓
Relevant Documents
    ↓
Context + User Query
    ↓
LLM
    ↓
Final Answer
```

The advantage is that the LLM gets relevant external/contextual information, which can improve accuracy and allow the system to work with domain-specific data.

---

## 34. What challenges did you face while implementing RAG/GenAI use cases?

**Answer:**

Some important challenges are:

* Retrieving the correct documents.
* Chunking documents appropriately.
* Choosing useful embedding strategies.
* Controlling irrelevant context.
* Managing context size.
* Reducing hallucinations.
* Evaluating response quality.
* Handling sensitive enterprise information.
* Managing latency and cost.

I would address these using better chunking, metadata filtering, retrieval evaluation, prompt design, and appropriate access controls.

---

## 35. Tell me about a production issue you handled and how you debugged it.

**Answer:**

My approach is systematic rather than immediately changing code.

First, I reproduce or understand the issue and identify whether it is coming from the frontend, API, database, or downstream service.

Then I check:

```text
Request
   ↓
Application Logs
   ↓
API/Business Logic
   ↓
Database
   ↓
Downstream Services
```

I use logs, request/correlation IDs, API responses and database information to identify the root cause.

After fixing it, I test the scenario, verify that existing functionality is not affected, and then support the deployment/UAT or production release.

This approach helped me handle high-priority production and UAT issues in my current project.

---

### ⭐ Architect-Level Follow-up You Should Expect

For many of the above questions, the interviewer may immediately ask:

**"Why?"**

For example:

> Why FastAPI?

Then:

> Why async?

Then:

> What happens if the downstream service fails?

Then:

> How will you scale it?

Then:

> How will you monitor it?

So don't prepare these answers as isolated definitions. Prepare the **decision → reason → trade-off → failure scenario** for each major technology.

### 🔥 Highest Priority From These 15

If you have limited preparation time, focus first on:

**Q21** — Your actual contribution
**Q22** — REST API design
**Q25** — FastAPI project structure
**Q28** — DB performance improvement
**Q30** — Scaling fraud API
**Q32** — API security
**Q33** — RAG
**Q35** — Production issue/debugging

These are especially important because they connect directly to claims already present on your resume.
