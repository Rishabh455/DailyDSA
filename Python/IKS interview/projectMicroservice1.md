Exactly. Since you've clarified that **your actual TCS banking project was microservices-based**, let's reduce the whole KYC architecture into the **exact content you should memorize and speak in the interview**.

I’ll keep it focused on:

**Microservice → responsibility → why separate service → what you worked on → how services communicate.**

Your resume supports that you worked with FastAPI/Flask, REST APIs, Pydantic, async programming, microservices, React, SQL, API integration, testing and production support. 

# KYC Microservices — Interview Cheat Sheet

## 1. First memorize this overall architecture

```text
                         React / TypeScript
                                |
                                v
                         API Gateway
                                |
              +-----------------+-----------------+
              |                 |                 |
              v                 v                 v
       Onboarding MS       Document MS        KYC MS
              |                 |                 |
              |                 v                 |
              |            Object Storage        |
              |                 |                 |
              |                 v                 |
              |              Queue                |
              |                 |                 |
              |                 v                 |
              |         Document Processing MS    |
              |                 |                 |
              |                 v                 |
              |             OCR / NLP             |
              |                 |                 |
              +-----------------+-----------------+
                                |
                                v
                       Verification MS
                                |
                 +--------------+--------------+
                 |                             |
                 v                             v
           Identity Check                 AML / Risk
                 |
                 v
             KYC Decision
                 |
          +------+------+
          |             |
          v             v
       APPROVED       MANUAL REVIEW
          |
          v
     Core Banking
```

The **6 core services** you should know are:

1. **Onboarding Service**
2. **Document Service**
3. **Document Processing Service**
4. **KYC/Verification Service**
5. **Identity Verification Service**
6. **AML/Risk Service**

Notification and Audit can be additional supporting services.

---

# 2. Onboarding Microservice

## What does it do?

This is the **main customer onboarding workflow service**.

It manages:

```text
Create application
Update application
Customer information
Application status
Product selection
Onboarding workflow
```

Example:

```http
POST /api/v1/onboarding/applications
GET  /api/v1/onboarding/applications/{id}
PUT  /api/v1/onboarding/applications/{id}
GET  /api/v1/onboarding/applications/{id}/status
```

### Why is this a separate microservice?

Because onboarding is a **business capability by itself**.

It should not know how OCR works or how AML screening works.

Its responsibility is:

```text
"What is the current onboarding application?"
```

not:

```text
"How do I process a passport?"
```

### Memorize this answer

> **“The Onboarding microservice manages the overall customer application lifecycle. It creates the application, stores customer-provided onboarding information, maintains application status and orchestrates the onboarding journey. It is separated because onboarding is an independent business capability and should not be tightly coupled to document processing or compliance services.”**

---

# 3. Document Management Microservice

## What does it do?

It manages everything related to documents.

```text
Upload
Metadata
Document type
Document status
Document version
Document retrieval
Document lifecycle
```

For example:

```text
Passport
National ID
Salary Certificate
Address Proof
Bank Statement
```

API:

```http
POST /applications/{id}/documents
GET  /applications/{id}/documents
GET  /documents/{id}
```

### Database

```text
document
--------
document_id
application_id
document_type
file_name
storage_path
mime_type
status
created_at
```

Actual PDF/image goes to:

```text
Object Storage
```

while metadata goes to:

```text
SQL Database
```

### Why separate?

Because document management has a different responsibility and lifecycle.

```text
Onboarding
   ↓
"I need a passport"

Document Service
   ↓
"I store/manage the passport"
```

### Memorize

> **“The Document microservice is responsible for document ingestion and lifecycle management. It stores document metadata in the database and the actual files in secure object storage. We separate it from onboarding because document handling has different storage, security and scaling requirements.”**

---

# 4. Document Processing Microservice ⭐

This is the **important Python service**.

## What does it do?

```text
Document
   ↓
Classification
   ↓
OCR
   ↓
Text Extraction
   ↓
Field Extraction
   ↓
Confidence Score
```

Example:

```text
Passport
↓
OCR
↓
Name
DOB
Passport Number
Nationality
Expiry Date
```

### Why separate?

This is probably your **best microservice justification**.

Document processing can be:

```text
CPU intensive
memory intensive
long running
AI/ML heavy
```

while the onboarding API is:

```text
request/response
low latency
I/O oriented
```

So if document processing becomes heavy:

```text
Document Workers
3 → 20
```

without scaling:

```text
Onboarding API
3 → 20
```

### Memorize

> **“We separated document processing because OCR and extraction are long-running and potentially CPU-intensive workloads. Keeping them separate allows independent scaling and prevents heavy document-processing workloads from affecting customer-facing APIs.”**

---

# 5. Why Python for Document Processing?

This is where your Python story becomes strong.

```text
Python
 ↓
OCR libraries
 ↓
NLP libraries
 ↓
ML libraries
 ↓
Document processing
```

Possible technologies include:

```text
OCR engines
OpenCV
spaCy
Transformers
PyTorch
Cloud document AI APIs
```

But mention only technologies you actually used.

### Memorize:

> **“Python was a strong fit for the document-processing service because the ecosystem provides mature libraries and SDKs for OCR, NLP, computer vision and machine learning. The API can be built in FastAPI while specialized document-processing logic runs behind the service.”**

---

# 6. KYC / Verification Microservice ⭐

This is the **business-rule service**.

Document processing gives you:

```text
name
DOB
ID
nationality
```

But that doesn't mean the customer is verified.

The KYC service takes:

```text
Customer Data
+
Extracted Data
+
Verification Result
+
Compliance Result
```

and determines:

```text
APPROVED
REJECTED
REVIEW_REQUIRED
```

### Example

```text
Customer entered:
Mohammed Ali

OCR:
Mohammed Ali

Identity:
MATCH

Document:
VALID

AML:
CLEAR

Risk:
LOW

        ↓

KYC APPROVED
```

### Why separate?

Because:

**Document Processing = extracting information**

while:

**KYC = deciding whether the information satisfies business rules**

This separation is extremely important.

### Memorize:

> **“The KYC service is responsible for the actual business validation and decisioning. The document service extracts information, while the KYC service combines customer data, extracted data and verification results to determine whether the onboarding should be approved, rejected or sent for manual review.”**

---

# 7. Identity Verification Microservice

This service talks to an external identity provider.

Architecture:

```text
KYC
 ↓
Identity Verification Service
 ↓
External Provider
 ↓
Verification Result
```

Example:

```text
ID Number
Name
DOB
```

go to external verification.

Response:

```text
MATCH
NO_MATCH
PENDING
```

### Why a separate service?

Because external providers can change.

Imagine:

```text
Provider A
Provider B
Provider C
```

If provider-specific code is written directly inside KYC, coupling becomes high.

Instead:

```text
KYC
 ↓
Identity Verification Interface
 ↓
Provider Adapter
```

This is where you can mention the **Adapter design pattern**.

### Memorize:

> **“The Identity Verification microservice isolates third-party identity-provider integrations from the KYC business logic. This reduces coupling and allows us to change or add providers without modifying the core KYC decisioning logic.”**

---

# 8. AML / Risk Microservice

After identity verification:

```text
Customer
   ↓
AML / Screening
   ↓
Risk Evaluation
```

It can integrate with compliance screening systems for relevant checks.

The output could be conceptually:

```text
AML = CLEAR
Risk = LOW
```

or:

```text
AML = POTENTIAL_MATCH
Risk = HIGH
```

### Why separate?

AML/compliance logic changes independently from document processing.

Also, it can have:

```text
different rules
different integrations
different access controls
different audit requirements
```

### Memorize:

> **“AML and risk processing is separated because compliance screening is a distinct business capability with its own external integrations and rule changes. The KYC service consumes the result instead of implementing all compliance-specific logic itself.”**

---

# 9. Notification Microservice

This is supporting, but know it.

It handles:

```text
Email
SMS
Push notifications
```

Example:

```text
KYC Approved
       ↓
Notification Event
       ↓
Notification Service
       ↓
SMS / Email
```

### Why separate?

Because KYC should not have code like:

```python
send_email()
send_sms()
send_push()
```

inside its main business flow.

### Memorize:

> **“Notification is separated so that business services only publish notification events and don't need to know provider-specific email or SMS implementation details.”**

---

# 10. Audit Microservice

For banking, this is important.

Track:

```text
APPLICATION_CREATED
DOCUMENT_UPLOADED
OCR_COMPLETED
IDENTITY_VERIFIED
KYC_APPROVED
KYC_REJECTED
MANUAL_REVIEW
```

Example:

```text
Who?
What?
When?
Which application?
Which request?
```

### Why?

Because you need traceability.

### Memorize:

> **“The audit capability records important business and security events so that the lifecycle of an onboarding application can be reconstructed for operational and compliance purposes.”**

Whether this was physically a separate microservice in your exact implementation is something you should only claim if it was.

---

# 11. Now understand the difference between these three

This is **very important**.

An interviewer might intentionally confuse you:

### Document Service

```text
"Give me the document."
```

### Document Processing Service

```text
"Read the document."
```

### KYC Service

```text
"Decide what the document information means for KYC."
```

That's the clean boundary.

---

# 12. One complete example

Customer uploads:

```text
passport.pdf
```

### Step 1

React:

```text
Upload passport
```

### Step 2

Document Service:

```text
Store passport
```

### Step 3

Queue:

```text
DOCUMENT_UPLOADED
```

### Step 4

Document Processing Service:

```text
OCR
 ↓
Name
DOB
Passport No
Expiry
```

### Step 5

KYC Service:

```text
Compare customer details
 ↓
validate document
 ↓
request identity verification
```

### Step 6

Identity Service:

```text
External identity provider
 ↓
MATCH
```

### Step 7

AML/Risk:

```text
Screening
 ↓
CLEAR
```

### Step 8

KYC:

```text
ALL CHECKS PASS
 ↓
APPROVED
```

### Step 9

Onboarding:

```text
Application status = KYC_APPROVED
```

### Step 10

Notification:

```text
"Your application has been approved."
```

---

# 13. How services communicate

You should memorize this distinction.

## REST for synchronous communication

```text
React
 ↓
API Gateway
 ↓
Onboarding
```

Example:

```http
POST /applications
```

Or:

```text
KYC → Identity Service
```

when immediate response is required.

---

## Event/Queue for asynchronous communication

```text
Document Service
       ↓
DOCUMENT_UPLOADED
       ↓
Queue
       ↓
Document Processing
```

Then:

```text
DOCUMENT_PROCESSED
       ↓
KYC Service
```

Why?

```text
Loose coupling
Retry
Independent scaling
Failure isolation
```

---

# 14. One thing an architect will ask you

### "Why don't all microservices share one database?"

Your answer:

> **“Each service should ideally own the data for its business capability rather than allowing every service to directly modify every table. This reduces coupling and allows independent evolution. However, we need to consider consistency and operational complexity, so I wouldn't blindly introduce a separate database for every service.”**

That's a very mature answer.

---

# 15. Another likely question

### "Why don't you make OCR itself a microservice?"

Answer:

> **“It depends on the workload and organizational boundaries. If OCR is a reusable capability, has independent scaling requirements, or needs a different deployment lifecycle, separating it makes sense. Otherwise, it can remain part of the document-processing service. The important principle is that service boundaries should follow business and operational characteristics rather than creating a microservice for every small function.”**

**Memorize this.**

---

# 16. Your exact "Why Microservices?" answer

I want you to memorize this almost word-for-word:

> **“The banking application was based on microservice architecture because different business capabilities had different scaling, deployment and reliability requirements. In the KYC workflow, onboarding, document management, document processing, verification and compliance had clear business boundaries. Document processing in particular had a much heavier workload than normal APIs, so separating it allowed independent scaling and fault isolation. Microservices also reduced coupling, allowed independent deployments and made third-party integrations easier to isolate.”**

---

# 17. Your exact "Why these microservices?" answer

Memorize this table:

| Microservice              | What it does                    | Why separate                         |
| ------------------------- | ------------------------------- | ------------------------------------ |
| **Onboarding**            | Application lifecycle           | Independent business capability      |
| **Document**              | Upload/store/manage documents   | Different storage/security lifecycle |
| **Document Processing**   | OCR/classification/extraction   | CPU/AI-heavy, independent scaling    |
| **KYC**                   | Business validation & decision  | Own KYC business rules               |
| **Identity Verification** | External identity checks        | Isolate third-party integrations     |
| **AML/Risk**              | Screening & risk assessment     | Independent compliance capability    |
| **Notification**          | Email/SMS/push                  | Decouple communication providers     |
| **Audit**                 | Record business/security events | Traceability and audit requirements  |

---

# 18. What YOU need to say about FastAPI

Don't say:

> “Every microservice uses FastAPI.”

Say what you actually know from your project.

A strong generic answer is:

> **“We used Python-based FastAPI services for API-driven backend capabilities. FastAPI handled routing, request/response validation, authentication and API integration, while the business logic was organized into service layers. For longer-running document processing, the API layer was decoupled from background processing rather than keeping the HTTP request open.”**

Your resume specifically supports FastAPI, Pydantic, async programming, microservices, REST API integration and scalable service workflows. 

---

# 19. The most important architecture principle

This is what your interviewer is likely testing:

### Don't think:

```text
1 function = 1 microservice
```

Think:

```text
Business Capability
       ↓
Service Boundary
       ↓
Independent Responsibility
       ↓
Independent Scaling
       ↓
Independent Deployment
```

So:

```text
Document Processing
```

is a good service boundary.

But:

```text
ExtractDateOfBirth()
```

is **not** a reason to create another microservice.

---

# 20. Your final memorization sheet

Before the interview, you should be able to say this without looking anywhere:

```text
PROJECT:
Digital Customer Onboarding + KYC

ARCHITECTURE:
Microservices

FRONTEND:
React + TypeScript

API:
FastAPI / REST

SERVICES:

1. Onboarding
   → application lifecycle

2. Document
   → upload + metadata + storage

3. Document Processing
   → classification + OCR + extraction

4. KYC
   → validation + decisioning

5. Identity Verification
   → external identity verification

6. AML/Risk
   → compliance screening + risk

7. Notification
   → email/SMS/push

8. Audit
   → traceability

COMMUNICATION:

Synchronous:
REST

Asynchronous:
Queue/events

STORAGE:

SQL:
business data + metadata

Object Storage:
PDF/images

MAIN REASON FOR MICROSERVICES:

Different business capabilities
+
Independent scaling
+
Fault isolation
+
Independent deployment
+
Loose coupling

MOST IMPORTANT SERVICE:

Document Processing

WHY?

CPU/AI-heavy
long-running
independent scaling
```

And remember this **one-liner**:

> **“Document Service stores the document, Document Processing Service understands the document, and KYC Service decides what that information means for the customer’s onboarding.”**

That sentence alone will help you keep the service boundaries straight when the architect starts drilling down.
