# 20 Brutally Tricky Scenario-Based Questions

## 1. Your Fraud API receives the same transaction twice. What will you do?

**Answer:**

I would handle this using **idempotency**.

The client or transaction gateway should send a unique transaction or idempotency key. The service checks whether that key has already been processed.

If it already exists, I return the previously generated result instead of processing the transaction again.

I would also enforce uniqueness at the database level to protect against race conditions.

**Architect follow-up:**
"What if two identical requests arrive at exactly the same time?"

**Answer:**

That's why I wouldn't rely only on an application-level check. I would use a **database unique constraint** or atomic operation so that concurrent requests cannot create duplicate records.

---

# 2. Your ML/risk service takes 10 seconds, but your API normally responds in 500 ms. What would you do?

**Answer:**

I wouldn't allow a long-running operation to unnecessarily block the API request.

First, I would determine whether the business requirement actually needs a synchronous fraud decision.

If the decision is required before the transaction can continue, I would use controlled synchronous communication with:

* Timeout
* Retry only where safe
* Circuit breaker
* Proper monitoring

If the operation can be asynchronous, I would use a **message queue and worker architecture**.

For CPU-heavy ML processing, I would also consider separating model serving from the FastAPI application so both components can scale independently.

---

# 3. What happens if your Fraud Detection service goes down while a transaction is being processed?

**Answer:**

The first thing is to avoid cascading failure.

I would have:

**Timeout → Controlled retry → Circuit breaker → Fallback**

The exact fallback depends on the banking business requirement.

For example, the transaction could be sent for **manual review** or held temporarily rather than automatically approving a transaction when the risk decision is unavailable.

I would never blindly choose "approve" or "block" without understanding the business risk and compliance requirement.

---

# 4. You have 10,000 transactions arriving simultaneously. Your FastAPI service starts becoming slow. What do you check first?

**Answer:**

I wouldn't immediately just add more servers.

I would first identify the bottleneck.

I would check:

1. CPU utilization
2. Memory
3. API response time
4. Database latency
5. Connection pool usage
6. Downstream service latency
7. ML inference time
8. Network latency
9. Error rate

Then I would scale the actual bottleneck.

If the application is stateless and CPU/memory is the bottleneck, I can horizontally scale multiple FastAPI instances behind a load balancer.

---

# 5. Your API has 5 FastAPI instances. Where should you store session/state information?

**Answer:**

I would avoid storing important session state in the individual FastAPI instance because requests can go to different instances.

For shared state, I would use an external store such as Redis or a database depending on the requirement.

The goal is to keep the API instances **stateless**, which makes horizontal scaling easier.

---

# 6. Your database suddenly becomes slow. The API itself is healthy. How would you debug it?

**Answer:**

I would isolate the database layer first.

I would check:

* Slow query logs
* Query execution plans
* Missing indexes
* Lock contention
* Connection pool exhaustion
* Number of concurrent queries
* CPU/memory of the database
* Large table scans
* Recent schema/query changes

If I identify a slow query, I would optimize the query and indexes rather than simply increasing infrastructure.

In my previous experience, I worked on query optimization and observed around **20% improvement for relevant operations**.

---

# 7. You add an index and the query becomes faster. Why shouldn't you put indexes on every column?

**Answer:**

Because indexes aren't free.

They improve read performance but increase:

* Storage
* INSERT/UPDATE/DELETE overhead
* Index maintenance
* Potentially write latency

So I would create indexes based on actual query patterns and workload rather than indexing everything.

---

# 8. Your API receives malformed transaction data. Where should validation happen?

**Answer:**

I would validate as early as possible at the API boundary.

In FastAPI, I can use **Pydantic models** for:

* Data types
* Required fields
* Format validation
* Constraints

But validation shouldn't stop there.

Business validation should happen in the service layer.

For example:

```text
Pydantic
   ↓
Schema validation
   ↓
Business validation
   ↓
Fraud processing
```

Pydantic can tell me that `amount` is numeric, but business logic determines whether the amount is valid according to the application's rules.

---

# 9. Suppose your downstream service returns HTTP 500. Would you retry?

**Answer:**

Not blindly.

I would determine whether the failure is transient and whether the operation is safe to retry.

For transient failures, controlled retries with **exponential backoff** can be useful.

But if the operation isn't idempotent, retries can potentially create duplicate processing.

That's why I combine:

**Timeout + controlled retry + idempotency + circuit breaker**

where appropriate.

---

# 10. What if the downstream service responds successfully, but your API crashes before saving the response?

**Answer:**

This is a distributed-system consistency problem.

I would design the workflow so that the operation can safely be retried.

Idempotency is important here.

For more complex workflows, I would consider patterns such as an **outbox/event-driven approach** or workflow/state tracking depending on the business requirement.

The key principle is that a retry should not create duplicate business operations.

---

# 11. Your Fraud API is receiving 1 million requests per day. How would you scale it?

**Answer:**

I would design it for horizontal scalability.

A high-level architecture could be:

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

Then I would consider:

* Stateless services
* Horizontal scaling
* Database connection pooling
* Proper indexes
* Caching where appropriate
* Queue-based processing
* Independent ML model scaling
* Monitoring and autoscaling

I would also perform load testing before deciding the actual number of instances.

---

# 12. Your API works perfectly in development but fails in production. What would you investigate?

**Answer:**

I would compare the environments systematically.

I would check:

* Configuration differences
* Environment variables
* Secrets
* Database connectivity
* Network/security rules
* Dependency versions
* External service URLs
* Resource limits
* Authentication configuration
* Logs and traces

I wouldn't assume that the application code itself is the only problem.

This is where containerization and CI/CD help maintain consistency between environments.

---

# 13. Your Docker container works locally but fails in Kubernetes/cloud. What do you check?

**Answer:**

I would check:

1. Container logs
2. Environment variables
3. Secrets/configuration
4. Port configuration
5. Health checks
6. Resource requests/limits
7. Network connectivity
8. Service configuration
9. Container startup command
10. Dependency availability

The container should be environment-independent as much as possible, while environment-specific configuration should be injected externally.

---

# 14. Your React application sends the same API request multiple times because the user clicks the button repeatedly. How would you prevent it?

**Answer:**

I would handle this at both frontend and backend levels.

On the frontend:

* Disable the button while the request is processing.
* Maintain loading state.
* Prevent duplicate submissions.

But frontend protection alone isn't sufficient.

The backend should use **idempotency** for operations where duplicate processing has business impact.

This provides defense in depth.

---

# 15. A user uploads a passport in your KYC system, but OCR fails. What should happen?

**Answer:**

I wouldn't simply return a generic 500 error.

The workflow should recognize OCR as a downstream processing step.

For example:

```text
Document Upload
      ↓
Storage
      ↓
OCR
      ↓
OCR Failure
      ↓
Retry / Manual Review
```

Depending on the business requirement, we can retry transient failures and eventually route the document to manual review.

The document itself should remain safely stored so we don't ask the customer to upload it again unnecessarily.

---

# 16. Suppose 10,000 KYC documents are uploaded simultaneously. Would you process them inside the FastAPI request?

**Answer:**

No, I would avoid doing heavy document processing directly inside the API request.

The API should primarily:

1. Authenticate the request.
2. Validate the document.
3. Store the document.
4. Create a processing record.
5. Publish a message/job.

Then background workers can perform:

```text
OCR
 ↓
NLP/Extraction
 ↓
Identity Verification
 ↓
AML/Risk
```

This separates request handling from heavy processing and allows workers to scale independently.

---

# 17. Your RAG system retrieves the wrong document and the LLM gives a confident wrong answer. Who is responsible—the LLM or retrieval system?

**Answer:**

I would treat this as an end-to-end RAG quality problem rather than blaming only the LLM.

The pipeline is:

```text
Query
 ↓
Embedding
 ↓
Retrieval
 ↓
Context
 ↓
Prompt
 ↓
LLM
```

If retrieval provides irrelevant context, even a good LLM can generate a poor answer.

So I would evaluate:

* Chunking
* Embeddings
* Similarity search
* Metadata filtering
* Top-K selection
* Prompt construction
* Grounding
* Response evaluation

The important point is that **RAG quality depends on both retrieval and generation**.

---

# 18. Your interviewer says: "You claim GenAI/RAG experience. Build me a RAG system right now. What architecture would you propose?"

**Answer:**

I would propose:

```text
User
 ↓
React UI
 ↓
FastAPI
 ↓
Query Processing
 ↓
Embedding Model
 ↓
Vector Database
 ↓
Relevant Chunks
 ↓
Prompt + Context
 ↓
LLM
 ↓
Response
```

For document ingestion:

```text
Documents
 ↓
Parsing
 ↓
Chunking
 ↓
Embeddings
 ↓
Vector Database
```

I would also add authentication, access control, logging, monitoring and evaluation.

For an enterprise system, I would ensure users only retrieve documents they are authorized to access.

---

# 19. The interviewer asks: "You personally didn't build the ML model/OCR. So what exactly did you contribute?"

**Answer:**

I would be very clear about my ownership.

My primary contribution is on the **Python/FastAPI service layer**.

I worked on areas such as:

* API development
* Pydantic request/response models
* Input validation
* Business logic
* Exception handling
* Downstream service integration
* Database/API integration
* Testing
* Production/UAT support

For the fraud solution, the ML model is a downstream component in the overall architecture. My responsibility is primarily the service layer that receives the transaction, validates it, prepares the required data and integrates with the risk-processing flow.

I don't claim ownership of components I didn't personally implement.

**This answer is extremely important.**

---

# 20. The interviewer says: "Your architecture sounds good on paper. Tell me one design decision you would NOT make."

**Answer:**

I would avoid putting every component into microservices just because microservices are considered scalable.

For example, if a functionality is small, tightly coupled and doesn't require independent scaling or deployment, I would consider keeping it within a modular monolith.

Microservices introduce additional complexity such as:

* Network communication
* Distributed failures
* Monitoring
* Deployment complexity
* Data consistency challenges

So I would choose microservices when the benefits of **independent scaling, deployment and ownership** justify that complexity.

The architecture should follow the business and operational requirements, not the other way around.

---

# 🔥 5 Brutal Follow-Up Chains You Should Practice

The interviewer may not stop after your first answer.

### Chain 1 — FastAPI

**Interviewer:** Why FastAPI?

↓

**You:** Validation + async I/O + OpenAPI + type hints.

↓

**Interviewer:** Does async make everything faster?

**You:** No. It's primarily beneficial for I/O-bound concurrency.

↓

**Interviewer:** What about CPU-heavy ML inference?

**You:** I would consider separating model serving/workers so CPU-heavy processing doesn't block API workers.

---

### Chain 2 — Database

**Interviewer:** Query is slow. What do you do?

↓

Optimize query.

↓

**Interviewer:** How do you know what to optimize?

↓

Execution plan / slow query analysis.

↓

**Interviewer:** Why not add an index?

↓

Indexes improve reads but add write/storage/maintenance cost.

---

### Chain 3 — Microservices

**Interviewer:** Why microservices?

↓

Independent scaling/deployment/ownership.

↓

**Interviewer:** What is the downside?

↓

Network failures, operational complexity, distributed consistency.

↓

**Interviewer:** So why not a monolith?

↓

A modular monolith may be better when independent scaling/deployment isn't required.

---

### Chain 4 — Fraud Detection

**Interviewer:** ML service is down. What do you do?

↓

Timeout + controlled retry + circuit breaker + business-defined fallback.

↓

**Interviewer:** Should you approve the transaction?

↓

Not automatically. The fallback decision must be based on business risk/compliance requirements; manual review or temporary hold may be safer.

---

### Chain 5 — Your Actual Experience

**Interviewer:** Did you build the ML model?

↓

**You:** My primary responsibility was the FastAPI/service layer and integration; I did not claim ownership of the ML model itself.

↓

**Interviewer:** Then what exactly did you write?

↓

API endpoints → Pydantic schemas → validation → business logic → exception handling → integrations → tests.

↓

**Interviewer:** Show me how you'd structure it.

↓

```text
API Layer
   ↓
Service Layer
   ↓
Repository / Integration Layer
   ↓
DB / External Services
```

This is where **honesty + technical depth** will make your answers much stronger than pretending you built the entire system.
