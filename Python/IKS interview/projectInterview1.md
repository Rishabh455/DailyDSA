Absolutely. Because the interviewer is an **Enterprise Architect with 18+ years of experience**, including banking, CBS, SOA, microservices, event-driven architecture, cloud and architecture reviews, I would expect him to go beyond “write a FastAPI API” and challenge your **architecture decisions, failure handling, scalability, security, and trade-offs**.

Your JD also explicitly emphasizes FastAPI, React/TypeScript, REST APIs, authentication/authorization, SQL/NoSQL, microservices, cloud-native architecture, design patterns, scalability, reliability and security. 

So below are **50 scenario-based questions**, ordered roughly from medium → very deep.

---

# 50 Scenario-Based KYC Interview Questions & Answers

## A. Business & Architecture Scenarios

### 1. Customer uploads a passport. Explain the complete flow.

**Answer:**

> “The React application allows the customer to select the document and performs basic client-side validation such as file type and size. The file is sent using multipart/form-data to the FastAPI onboarding service over HTTPS. FastAPI authenticates the request, validates the application and document type, stores the file in secure object storage, and creates document metadata in the relational database. I would avoid running OCR synchronously inside the upload request. Instead, I would create a document-processing job and publish it to a queue. A worker performs document classification and OCR, extracts the required fields and stores the extraction result. The KYC workflow then performs validation and external identity/compliance checks. Depending on the result, the application is approved, rejected, or sent for manual review.”

---

### 2. Why don't you perform OCR directly inside the FastAPI API?

**Answer:**

> “OCR can be computationally expensive and may also involve external service latency. If I perform it synchronously, API workers can remain occupied for a long time and user response time becomes unpredictable. I prefer separating ingestion from processing: FastAPI accepts and stores the document, creates a job, and returns an acknowledgement such as 202 Accepted. A worker processes the document asynchronously.”

```text
Upload
  ↓
FastAPI
  ↓
Storage
  ↓
Queue
  ↓
Worker
  ↓
OCR
```

---

### 3. What happens if 10,000 customers upload documents at the same time?

**Answer:**

> “I would decouple API traffic from document processing using a queue. The FastAPI layer can scale horizontally behind a load balancer, while document workers can scale independently based on queue depth. Object storage handles the large document payloads, and the database stores metadata rather than large binary files. This prevents a sudden upload spike from directly overwhelming the OCR layer.”

---

### 4. Why did you choose FastAPI?

**Answer:**

> “FastAPI is a strong fit because it provides type-hint-based validation through Pydantic, automatic OpenAPI documentation, good async support and an ASGI-based architecture. For an API-heavy application with multiple external service integrations and I/O operations, this makes the backend clean and scalable. However, I wouldn't claim FastAPI is the only suitable technology. Java Spring Boot, .NET, Node.js and Go can also implement this architecture.”

---

### 5. Why Python? Why not Java?

**Answer:**

> “The whole system does not have to be Python. Python is particularly attractive when the solution includes OCR, NLP, machine learning or document-processing capabilities because of its ecosystem in those areas. FastAPI also gives us a clean API layer. Java would be equally valid for enterprise API development, especially in organizations with a strong Java ecosystem. The technology decision should depend on the existing platform, team expertise, integration ecosystem and non-functional requirements.”

That is the answer an architect is more likely to respect than “Python is faster.”

---

### 6. What would you keep synchronous and what would you make asynchronous?

**Answer:**

I would make the initial user-facing operation synchronous:

```text
Create Application
Upload Document
Return Document ID
```

And expensive/long-running work asynchronous:

```text
OCR
Document Classification
NLP Extraction
Large external verification workflows
Risk calculations where latency is high
Notifications
```

The key principle is:

> “Keep the API responsive and move long-running work to asynchronous processing.”

---

### 7. Would you make this one monolith or multiple microservices?

**Answer:**

> “I would not introduce microservices just because the application is large. I would initially identify bounded business capabilities. A reasonable decomposition could be Onboarding, Document Processing, KYC/Verification and Notification. If there are independent scaling requirements, ownership boundaries or release cycles, those are good reasons to separate services. Otherwise, a modular monolith may be simpler.”

This is an important answer for an architect-level interviewer.

---

### 8. Draw the high-level architecture.

**Answer:**

```text
                    React
                      |
                   HTTPS
                      |
                Load Balancer
                      |
                 API Gateway
                      |
             FastAPI Onboarding
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
      DB          Object Storage   Queue
                                      |
                                      v
                              Document Worker
                                 /       \
                                /         \
                              OCR         NLP
                                \         /
                                 \       /
                                   KYC
                                    |
                      +-------------+-------------+
                      |             |             |
                      v             v             v
                 Identity       AML/Risk      Audit
                 Provider       Services
```

---

## B. File Upload & Document Scenarios

### 9. User uploads a 100 MB file. What do you do?

**Answer:**

> “I would reject it at the API boundary based on configured limits. I would also validate the MIME type and file signature, not only the filename. Large file uploads should ideally go directly to object storage using a controlled upload mechanism rather than unnecessarily passing large payloads through every backend component.”

---

### 10. User renames `malware.exe` to `passport.pdf`. How do you detect it?

**Answer:**

> “I don't trust the extension or Content-Type header supplied by the client. I would validate the file signature/magic bytes, file structure, size and then run an antivirus or malware-scanning step before making the document available for downstream processing.”

---

### 11. Two documents are uploaded with the same filename. Is that a problem?

**Answer:**

> “No. Filename is not a unique identifier. The system should generate its own document ID and storage key, for example using application ID and document UUID. We store the original filename only as metadata.”

---

### 12. The document upload succeeds but the database insert fails. What happens?

**Answer:**

This is a distributed consistency problem.

I would avoid assuming the database and object store participate in one transaction.

For example:

```text
Upload Object
   ↓
DB Insert fails
```

Now you have an orphan file.

I would use a workflow such as:

```text
Create metadata/status
       ↓
Store document
       ↓
Update status = STORED
```

and have a cleanup/reconciliation process for failed or orphaned objects.

For stronger guarantees, an outbox/event-based workflow can be considered.

---

### 13. Database succeeds but object storage fails?

**Answer:**

> “I would mark the document as an incomplete/failed state and retry the storage operation. I wouldn't expose it to OCR until storage is confirmed. A retry mechanism plus reconciliation job helps recover from transient failures.”

---

### 14. Customer uploads the same passport five times. What would you do?

**Answer:**

> “I would distinguish between duplicate requests and legitimate re-upload attempts. At the API level, I'd use an idempotency key for repeated submission requests. At the document level, I could additionally use a cryptographic hash to identify identical files, depending on business requirements.”

---

### 15. Why store documents in object storage instead of MySQL?

**Answer:**

> “Relational databases are excellent for metadata, relationships and transactional information, while object storage is better suited for large binary objects such as PDFs and images. So I would store document metadata and lifecycle information in SQL and the actual document in secure object storage.”

---

## C. OCR / AI Scenarios

### 16. OCR extracts the wrong date of birth. How do you prevent bad customer data?

**Answer:**

> “OCR output is an extraction result, not a trusted source. I would attach confidence scores to extracted fields and validate them against the customer's entered information and, where appropriate, an independent identity-verification source. Low-confidence or inconsistent results can be routed to manual review.”

---

### 17. OCR confidence is 60%. What should the system do?

**Answer:**

```text
Confidence
    |
    +-- High → Continue automated processing
    |
    +-- Medium → Additional validation
    |
    +-- Low → Manual review / re-upload
```

> “The actual thresholds should be configurable by document type and business policy, rather than hard-coded universally.”

---

### 18. OCR service goes down. What happens?

**Answer:**

> “The API should not fail the customer's entire application immediately. The document remains in a processing-pending or retry state. The processing service can retry transient failures with exponential backoff. After the retry threshold, the job can move to a dead-letter queue or manual-review state.”

---

### 19. OCR takes 30 seconds. Will the customer wait 30 seconds?

**Answer:**

> “No. I would make document processing asynchronous.”

React could show:

```text
Document uploaded ✓

Processing document...
Status: PROCESSING
```

and retrieve status through:

```http
GET /applications/{id}/documents/{id}/status
```

or receive an event/websocket update where appropriate.

---

### 20. Why use a queue?

**Answer:**

> “The queue provides buffering between document ingestion and processing. It absorbs spikes, decouples services, supports retries, and allows worker capacity to scale independently from the API layer.”

---

### 21. What if the same queue message is processed twice?

**Answer:**

> “I would design the document-processing operation to be idempotent. Before updating the final state, the worker checks whether the document has already reached a terminal processing state. Database constraints and unique processing identifiers also help prevent duplicate effects.”

---

### 22. What if OCR succeeds but NLP extraction fails?

**Answer:**

```text
OCR = SUCCESS
Extraction = FAILED
```

> “I would persist the OCR result independently so we don't repeat expensive OCR unnecessarily. Then the extraction stage can be retried independently.”

This shows **stage-level fault isolation**.

---

### 23. What if the AI extracts an income of SAR 250,000 instead of SAR 25,000?

**Answer:**

> “I would not blindly trust the model. I would validate the extracted value against expected formats, business limits and document context. Confidence and consistency checks can identify suspicious results. For a financial field, a low-confidence or anomalous value should go through additional verification or manual review.”

---

### 24. Would you use an LLM to directly decide KYC approval?

**Answer:**

> “I would not make an LLM the final authority for a high-impact compliance decision. An LLM can assist with document understanding, extraction or summarization, but deterministic validation and bank-approved policy rules should control the actual KYC decision. The final decision should be explainable and auditable.”

This is an **excellent question to prepare** given your AI claims.

---

## D. KYC / Compliance Scenarios

### 25. OCR says the customer's name is "Mohammed Ali", but application says "Mohammad Aly". What happens?

**Answer:**

> “I wouldn't automatically reject based on a simple string comparison. I would normalize the values, potentially use approved matching logic, and compare against the independent identity-verification result. If the confidence remains insufficient, I would route it to review.”

---

### 26. Customer's document is expired. What happens?

**Answer:**

```text
Document extracted
       ↓
Expiry validation
       ↓
Expired
       ↓
DOCUMENT_INVALID
       ↓
Request valid document
```

The application shouldn't proceed as though the expired document were valid.

---

### 27. Customer submits a document that isn't the required document type.

**Answer:**

> “The classification layer can detect the document type. If the customer uploaded a salary certificate where a national ID is required, the system should mark the document as invalid or mismatched and ask the user to upload the required document.”

---

### 28. What if a high-risk customer passes basic KYC?

**Answer:**

> “Passing identity verification doesn't necessarily mean straight-through approval. The risk engine can route higher-risk cases to enhanced due diligence and manual compliance review. So identity verification and risk-based decisioning are separate stages.”

---

### 29. What is the difference between KYC and AML?

**Answer:**

> “KYC is primarily about identifying and verifying the customer and understanding the relationship. AML is broader and focuses on financial-crime risk, including screening, monitoring and suspicious activity-related controls. In the architecture, KYC data becomes an input into broader compliance and risk workflows.”

---

### 30. Compliance officer rejects the customer manually. How do you protect the decision?

**Answer:**

> “Manual decisions should be authorized by role, recorded with the reviewer identity, timestamp and reason, and captured in an immutable or controlled audit trail. The system should also prevent unauthorized modification of the decision.”

---

## E. FastAPI Scenarios

### 31. Your FastAPI endpoint starts returning 500 errors in production. What do you do?

**Answer:**

```text
Check monitoring
    ↓
Identify affected endpoint
    ↓
Check application logs
    ↓
Check correlation/request ID
    ↓
Check downstream dependencies
    ↓
Reproduce if possible
    ↓
Identify root cause
    ↓
Fix + test
    ↓
Deploy
    ↓
Monitor
```

Your resume already states experience resolving high-priority production/UAT issues through API debugging, SQL optimization and root-cause analysis. 

---

### 32. Your API becomes slow only during peak hours.

**Answer:**

> “I'd first identify whether the bottleneck is CPU, memory, database, external services, connection pools or queue backlog. I'd use metrics and traces rather than guessing. If the API layer is the bottleneck, I can scale horizontally. If the database query is the bottleneck, scaling the API alone won't solve it.”

This is exactly the type of answer an architect wants.

---

### 33. Why not use `async` for everything?

**Answer:**

> “Async is beneficial primarily for I/O-bound operations. It doesn't automatically make CPU-intensive tasks such as OCR or heavy data processing faster. CPU-heavy work should typically be moved to workers or another processing mechanism.”

---

### 34. What happens if your FastAPI service calls three external services?

Example:

```text
Identity
AML
Risk
```

**Answer:**

> “I would define appropriate timeout policies, error handling, retries only for transient errors, circuit-breaking where appropriate, and clear dependency statuses. I would also avoid retrying non-idempotent operations blindly.”

---

### 35. An external identity API takes 20 seconds. What do you do?

**Answer:**

> “Set a reasonable timeout. If the operation doesn't need to block the HTTP request, make it asynchronous. Persist the verification state as PENDING and process it separately. Don't let one slow dependency consume API resources indefinitely.”

---

### 36. FastAPI service restarts while processing a document.

**Answer:**

> “The processing job should be durable outside the API process. The queue should retain the message until successfully acknowledged, and the worker should be idempotent. That way, a process restart doesn't lose the job.”

---

## F. Database Scenarios

### 37. KYC application table has 50 million records and the search API is slow.

**Answer:**

> “I'd first inspect the actual query and execution plan. Then I'd examine indexes, filtering patterns, pagination strategy and whether the query is retrieving unnecessary columns. I would add or adjust indexes based on actual access patterns, not blindly index every column.”

---

### 38. Why not use `SELECT *`?

**Answer:**

> “It retrieves columns the client may not need, increases I/O and can make queries and responses heavier. I prefer selecting only the fields required by the use case.”

---

### 39. What indexes would you consider?

For example:

```text
application_id
customer_id
document_status
created_at
```

Potential composite index:

```text
(customer_id, created_at)
```

But:

> “The exact index depends on query patterns and execution plans.”

---

### 40. Two workers update the same KYC application simultaneously. What happens?

**Answer:**

This is a concurrency-control problem.

Possible solutions include:

```text
Database transaction
Row-level locking
Optimistic locking/version column
State-transition validation
```

For example:

```text
version = 5

Worker A → update version 5 → 6 ✓
Worker B → update version 5 → fails
```

The exact strategy depends on the database and workflow.

---

## G. Security Scenarios

### 41. Customer uploads a passport. How do you protect it?

**Answer:**

> “TLS protects the transfer. The document should be encrypted at rest, access should be controlled through authorization, storage access should be tightly scoped, sensitive data shouldn't appear in logs, and all significant access can be audited. The application should also validate and scan uploaded files.”

---

### 42. A normal customer tries to access another customer's passport.

**Answer:**

The API must not simply check:

```text
Is user logged in?
```

It needs:

```text
Authenticated?
      +
Authorized for THIS application/document?
```

So:

```text
GET /documents/1001
```

must verify that document 1001 belongs to an application the caller is authorized to access.

This is **object-level authorization**.

---

### 43. JWT contains `role=customer`, but someone modifies it.

**Answer:**

> “A properly signed token cannot simply be modified without invalidating its signature. The backend must validate the token signature, issuer, audience, expiry and relevant claims. Authorization should still be enforced server-side.”

---

### 44. Why can't frontend authorization be trusted?

**Answer:**

> “Frontend checks are for user experience, not security. A malicious user can bypass the UI and call the API directly. Authorization must always be enforced on the backend.”

---

### 45. Developer accidentally logs passport numbers.

**Answer:**

> “Sensitive PII should be masked or excluded from application logs. I'd introduce structured logging with a sensitive-field sanitization policy and review logging at code-review level. In production, logs should contain identifiers useful for troubleshooting without exposing sensitive customer data.”

---

## H. Reliability / Distributed Systems

### 46. Identity verification succeeds, but the response is lost due to a network timeout. What does your system do?

This is a very good architect question.

**Answer:**

> “The client timeout doesn't necessarily mean the external operation failed. I would use a correlation/reference ID and make the verification request idempotent where the provider supports it. The backend can query the status or safely retry according to the provider's contract. I would never assume a timeout means failure.”

This demonstrates understanding of:

**unknown outcome / distributed systems.**

---

### 47. KYC is approved, but account creation in the core banking system fails.

**Answer:**

Don't roll back history like:

```text
KYC approved → delete everything
```

Instead:

```text
KYC_APPROVED
      ↓
ACCOUNT_CREATION_PENDING
      ↓
Core Banking Failure
      ↓
RETRY
      ↓
SUCCESS
```

If repeated failure:

```text
MANUAL_REVIEW / OPERATIONS
```

The system should preserve the audit trail.

---

### 48. How would you prevent duplicate customer creation?

**Answer:**

> “I'd use business-level uniqueness constraints where appropriate, such as a verified customer identifier, plus idempotency at the API/workflow level. Application-level checks alone are not sufficient because two requests can pass the check concurrently. The database constraint should act as the final protection.”

This is a very strong database answer.

---

## I. Advanced Architecture Scenarios

### 49. Your architect says: "Why should I approve your microservice architecture?"

This is exactly the sort of question your interviewer could ask.

**Answer:**

> “I wouldn't justify microservices simply because they are modern. I'd justify each service boundary based on business capability, ownership, deployment independence and scaling characteristics. Document processing is a particularly good candidate because it has a different resource profile from the onboarding API. It may require independent worker scaling and failure isolation. At the same time, I would avoid unnecessarily splitting every CRUD operation into a separate service because that increases operational and distributed-system complexity.”

Excellent answer.

---

### 50. Design this entire system for 10 million customers. What changes?

**Answer:**

I'd structure my answer around **scale, reliability and isolation**:

```text
                    CDN/WAF
                      |
               Load Balancer
                      |
                API Gateway
                      |
             Stateless FastAPI
                /          \
               /            \
          Cache             DB
                              |
                       Read Replicas
                              |
                           Storage

Upload
  ↓
Object Storage
  ↓
Queue
  ↓
Auto-scaled Workers
  ↓
OCR / NLP

External APIs
  ↓
Timeout
Retry
Circuit Breaker
```

Then explain:

> “The API layer remains stateless so it can scale horizontally. Documents go to object storage rather than the relational database. Long-running processing is decoupled through queues and horizontally scalable workers. Database access is optimized using indexes, connection pooling, partitioning or replicas where justified. External integrations are isolated behind adapters and protected with timeouts and resilience mechanisms. Observability, security and auditability need to scale along with the system.”

---

# The 10 questions I would expect your interviewer to drill into hardest

Because his background is architecture-heavy, I would **especially memorize the reasoning behind these**, not just their answers:

| Priority | Question                                                    |
| -------- | ----------------------------------------------------------- |
| 🔴 1     | Why FastAPI instead of Java/Spring Boot?                    |
| 🔴 2     | Why asynchronous document processing?                       |
| 🔴 3     | What happens when OCR fails?                                |
| 🔴 4     | How do you guarantee idempotency?                           |
| 🔴 5     | How do you handle two workers processing the same document? |
| 🔴 6     | How do you handle external API timeout?                     |
| 🔴 7     | How do you secure uploaded documents?                       |
| 🔴 8     | How do you prevent unauthorized document access?            |
| 🔴 9     | Monolith vs microservices — why?                            |
| 🔴 10    | How would you scale this to millions of customers?          |

---

# One very important trick for YOUR interview

Because you have **3+ years**, don't answer every architecture question as:

> “I implemented Kafka, Kubernetes, Redis, Kubernetes autoscaling, event sourcing…”

That can become dangerous when the architect asks:

> “Why Kafka partitioning?”

> “How many partitions?”

> “What was your consumer strategy?”

> “How did you handle offset management?”

> “Why Kafka instead of RabbitMQ?”

You don't need to fake that experience.

Use this distinction:

### When you actually implemented it

Say:

> **“In my project, I implemented…”**

### When you understand the design but didn't personally own it

Say:

> **“From an architecture perspective, I would design it as…”**

### When you know the concept but it wasn't part of your project

Say:

> **“We didn't use that in my particular module, but the reason I would consider it is…”**

That makes you look **honest and technically mature**, especially in front of an Enterprise Architect.

---

# The answer framework you should use for almost every scenario

When he gives you a new situation, don't immediately jump into code.

Use:

```text
1. Understand the problem
        ↓
2. Identify functional requirement
        ↓
3. Identify non-functional requirement
        ↓
4. Choose architecture
        ↓
5. Explain API/service flow
        ↓
6. Explain database/storage
        ↓
7. Explain failure handling
        ↓
8. Explain security
        ↓
9. Explain scalability
        ↓
10. Explain trade-off
```

For example, instead of:

> “I'll use Kafka.”

Say:

> “The problem is long-running asynchronous document processing. I don't want the upload API blocked, so I'd decouple ingestion and processing. A queue gives us buffering, retry and independent worker scaling. Kafka is one possible implementation, but the choice would depend on message volume, ordering requirements, replay requirements and the organization's existing infrastructure.”

**That is the level at which you should prepare for this interviewer.**

Your JD specifically asks for scalable APIs, end-to-end frontend/backend development, databases, authentication/authorization, cloud-native architecture, Docker/CI/CD, design patterns and optimization. 

The next strongest preparation would be a **mock interview in the same style as this interviewer**: I ask you one scenario at a time like an Enterprise Architect, you answer, and I then tell you exactly what was weak, what follow-up question he would likely ask, and how to improve the answer.
