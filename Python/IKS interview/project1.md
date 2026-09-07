Yes. **This is actually a much better module for your IKS interview** than a generic “customer CRUD” module because it naturally combines **React + FastAPI + Python + document processing + databases + security + asynchronous processing + external verification + audit/compliance**.

One important correction first: **do not tell the interviewer that FastAPI is used “only because Python handles file uploads concurrently” or that this module can only be built in Python.** Java, Node.js, .NET, Go, etc. can also implement it. The stronger story is that **Python is particularly useful for the document/AI processing layer because of its mature OCR, NLP and ML ecosystem**, while FastAPI provides a clean, typed, high-performance API layer.

Also, for a Saudi banking client, the module needs to fit KYC/CDD/AML requirements. SAMA guidance requires customer identity verification using reliable and independent sources, risk-based due diligence, beneficial-owner identification for applicable entities, ongoing due diligence, and enhanced checks for higher-risk cases. ([SAMA Rulebook][1]) SAMA also requires records and due-diligence documentation to be retained for at least ten years in the applicable circumstances. ([SAMA Rulebook][2])

# 1. First understand: what is this module?

Call it:

## Digital Customer Onboarding & KYC Document Processing Module

Imagine a customer wants to open a bank account digitally.

Instead of:

```text
Customer visits branch
        ↓
Bank employee collects documents
        ↓
Employee manually enters data
        ↓
Employee verifies documents
        ↓
Back office reviews
        ↓
Account opened
```

the bank wants:

```text
Customer
   ↓
Digital application
   ↓
Upload documents
   ↓
Document classification
   ↓
OCR
   ↓
Data extraction
   ↓
Document validation
   ↓
Identity verification
   ↓
AML / sanctions / PEP checks
   ↓
Risk assessment
   ↓
Manual review if required
   ↓
KYC approved
   ↓
Customer/account creation
```

That is the module.

For your interview, don't describe it as simply **“PDF upload + OCR.”**

The real business problem is:

> **How do we digitally establish that a customer is who they claim to be, that the submitted information is genuine and sufficiently complete, and that the customer can safely be onboarded according to the bank's KYC/AML policies?**

That is much more senior.

---

# 2. Why does a bank need this?

Suppose a customer wants to open a bank account.

The bank may need information such as:

```text
Identity
Name
Date of birth
Nationality
National ID / identity number
Address

Financial information
Employment
Income
Source of funds
Source of wealth

For business customers
Company registration
Company address
Legal structure
Authorized signatory
Directors
Beneficial owners
```

SAMA's due-diligence guidance distinguishes requirements for natural persons and legal persons, including identity information and information concerning legal status, incorporation, managers/senior executives and beneficial ownership. ([SAMA Rulebook][3])

The problem is that customers may upload:

```text
Passport.pdf
National_ID.jpg
Salary_Certificate.pdf
Bank_Statement.pdf
Company_CR.pdf
Address_Proof.pdf
Tax_Document.pdf
```

A human manually reading thousands of documents is:

```text
Slow
Expensive
Error-prone
Difficult to scale
Difficult to audit
```

So your system automates as much of that process as possible.

---

# 3. Now imagine TCS → Saudi banking client

Your interview story could be:

> “I worked on a digital onboarding and KYC workflow for a banking application. The objective was to reduce manual effort during customer onboarding by allowing customers to submit identity and supporting financial documents digitally. The backend accepted the documents through FastAPI, stored them securely, processed them through OCR/document extraction services, validated the extracted information against onboarding data and external verification systems, and routed the application either to automatic approval or manual review depending on validation and risk outcomes.”

That's a very good opening.

But now let's go **inside the module**.

---

# 4. Complete architecture

This is the architecture I recommend you learn.

```text
                         CUSTOMER
                            │
                            ▼
                  React / TypeScript UI
                            │
                       HTTPS / REST
                            │
                            ▼
                   API Gateway / WAF
                            │
                            ▼
              Digital Onboarding Service
                     FastAPI
                            │
          ┌─────────────────┼────────────────┐
          │                 │                │
          ▼                 ▼                ▼
   Application         Document          KYC Workflow
   Management          Service           / Orchestrator
          │                 │                │
          │                 ▼                │
          │          Object Storage           │
          │       PDF / JPG / PNG             │
          │                                   │
          │                 ▼                 │
          │        Document Processing        │
          │                 │                 │
          │           ┌─────┴─────┐           │
          │           ▼           ▼           │
          │          OCR         NLP/ML       │
          │           │           │            │
          │           └─────┬─────┘            │
          │                 ▼                 │
          │          Extracted Data           │
          │                 │                 │
          └────────────┬────┴─────────────────┘
                       ▼
                Validation Engine
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
      ID Verify    AML/Sanctions   Risk
          │            │            │
          └────────────┼────────────┘
                       ▼
                 Decision Engine
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
          APPROVE             MANUAL REVIEW
             │                   │
             ▼                   ▼
        Core Banking       Operations Team
             │
             ▼
        Customer Created
```

Notice something important:

**FastAPI is not the OCR engine.**

FastAPI is primarily the **API/application layer** connecting all these components.

---

# 5. What happens when a customer uploads a document?

Let's take:

```text
National_ID.pdf
```

### Step 1 — React frontend

Customer sees:

```text
Upload National ID

[Choose File]

Supported:
PDF / JPG / PNG

Maximum size: 10 MB

[Upload]
```

React handles:

```text
file selection
file preview
file validation
upload progress
loading state
errors
retry
```

You should be able to explain that.

---

# 6. React → FastAPI file upload

React sends something like:

```http
POST /api/onboarding/applications/123/documents
Content-Type: multipart/form-data
```

with:

```text
application_id = 123
document_type = NATIONAL_ID
file = national_id.pdf
```

In FastAPI, conceptually:

```python
@app.post("/applications/{application_id}/documents")
async def upload_document(
    application_id: int,
    document: UploadFile,
):
    ...
```

`UploadFile` is important.

You should understand why rather than simply memorizing the class name.

---

# 7. Why `UploadFile` instead of reading the entire file into memory?

Suppose:

```text
10,000 users
```

are uploading:

```text
5 MB documents
```

at approximately the same time.

If your application eagerly loads every file completely into memory, memory consumption can become a serious problem.

With `UploadFile`, file handling is designed for uploads without you manually treating the whole file as a giant Python byte array from the outset.

And because FastAPI is ASGI-based, you can build non-blocking request handling for I/O-heavy operations.

But remember:

### Very important interview distinction

**Async does not magically make OCR faster.**

If OCR itself is CPU-heavy, you don't simply write:

```python
await ocr(document)
```

and expect magical concurrency.

You generally want:

```text
FastAPI
   ↓
accept request
   ↓
store document
   ↓
create processing job
   ↓
return response
   ↓
background worker processes OCR
```

That's the more realistic enterprise architecture.

---

# 8. The biggest design improvement: asynchronous document processing

This is where you can sound much more senior.

Do **not** design:

```text
POST /upload

Upload
 ↓
OCR
 ↓
NLP
 ↓
AML
 ↓
response
```

because OCR + document analysis + external calls could take seconds or much longer.

Instead:

```text
POST /documents
       ↓
Validate upload
       ↓
Store document
       ↓
Create document record
       ↓
Publish processing job
       ↓
Return 202 Accepted
```

Then:

```text
                Message Queue
                     │
                     ▼
             Document Worker
                     │
              ┌──────┴──────┐
              ▼             ▼
             OCR          Classifier
              │
              ▼
        NLP / Extraction
              │
              ▼
          Validation
              │
              ▼
       KYC Decision Engine
```

Possible technologies in a real implementation could include:

```text
RabbitMQ
Kafka
Cloud Pub/Sub
Celery
Cloud Tasks
```

But **don't claim one unless you actually used it**.

You can say:

> “For a production-scale implementation, I would decouple document processing from the synchronous API using a queue and worker model.”

That's an architecture answer, not a false experience claim.

---

# 9. Where is Python actually valuable?

This is the question you asked:

> "Why Python and not another language?"

The correct answer is:

### FastAPI itself

FastAPI gives you:

```text
Python
+
type hints
+
Pydantic validation
+
ASGI
+
async support
+
automatic OpenAPI documentation
```

### But Python becomes particularly valuable in the processing layer

Because Python has a huge ecosystem around:

```text
OCR
NLP
Machine Learning
Computer Vision
Document processing
Data transformation
LLMs
Embeddings
```

For example:

```text
PDF
 ↓
OCR
 ↓
Extracted text
 ↓
Python processing
 ↓
Entity extraction
 ↓
Validation
```

Potential technologies could be:

```text
Tesseract
OpenCV
Pandas
spaCy
Transformers
PyTorch
Azure Document Intelligence
AWS Textract
Google Document AI
```

Again, those are **possible implementations**, not things you should claim unless they match your actual environment.

### Excellent interview answer

> “The solution isn't inherently Python-only. Other technologies can build the API and processing pipeline. Python was a strong fit because FastAPI gives us a performant asynchronous API layer, while Python also has mature libraries and SDKs for OCR, NLP, computer vision and machine learning, which are central to document processing.”

That answer is much stronger than:

> “FastAPI can upload files faster.”

---

# 10. OCR step

Suppose customer uploads:

```text
National_ID.jpg
```

OCR converts:

```text
IMAGE
```

into:

```text
TEXT
```

Example:

```text
Name: Mohammed Abdulrahman Ali
ID Number: 1234567890
Date of Birth: 15/04/1990
Nationality: Saudi
```

The OCR layer may also return confidence values.

Example:

```json
{
  "id_number": {
    "value": "1234567890",
    "confidence": 0.99
  },
  "date_of_birth": {
    "value": "1990-04-15",
    "confidence": 0.97
  }
}
```

Now you have something interesting to discuss.

---

# 11. What if OCR reads the document incorrectly?

Suppose the actual ID is:

```text
1234567890
```

but OCR produces:

```text
12345678O0
```

where:

```text
O ≠ 0
```

Your system shouldn't blindly trust OCR.

So introduce:

## Confidence threshold

```text
OCR Confidence
      ↓
 > 95%
      ↓
Continue
```

while:

```text
< 95%
   ↓
Manual review / re-upload
```

The actual threshold would be defined by the bank's rules and document type; don't present 95% as a universal banking standard.

---

# 12. Document classification

Customer could upload five files.

Your system needs to understand:

```text
Document 1 → National ID
Document 2 → Salary Certificate
Document 3 → Bank Statement
Document 4 → Address proof
Document 5 → Passport
```

So:

```text
Uploaded File
      ↓
Document Classifier
      ↓
Document Type
```

This prevents users from uploading:

```text
salary.pdf
```

into the:

```text
National ID
```

slot.

---

# 13. Data extraction

After OCR:

```text
Raw text
   ↓
Extraction
   ↓
Structured fields
```

For example:

```json
{
  "full_name": "Mohammed Abdulrahman Ali",
  "national_id": "1234567890",
  "date_of_birth": "1990-04-15",
  "nationality": "Saudi"
}
```

For a salary certificate:

```json
{
  "employee_name": "...",
  "employer": "...",
  "monthly_income": 25000,
  "currency": "SAR",
  "employment_status": "ACTIVE"
}
```

For a company customer:

```json
{
  "company_name": "...",
  "registration_number": "...",
  "legal_form": "...",
  "registered_address": "...",
  "directors": [...]
}
```

That maps nicely to SAMA's distinction between natural-person and legal-person due diligence requirements. ([SAMA Rulebook][3])

---

# 14. Now comes the REALLY important part: validation

This is where you should tell the interviewer:

> “OCR extraction is not verification.”

Very important.

You extract:

```text
ID = 1234567890
```

but now you need to verify:

```text
Does this ID belong to this person?
Is the ID valid?
Is it expired?
Does the name match?
Does the date of birth match?
```

So:

```text
Customer entered data
        +
OCR extracted data
        +
Independent verification
        ↓
Validation result
```

SAMA guidance emphasizes identity verification using reliable and independent sources rather than simply relying on information supplied by the customer. ([SAMA Rulebook][3])

---

# 15. Saudi-specific verification layer

For your **Saudi bank scenario**, you can explain a generic external-government/approved identity verification integration.

Don't say:

> “I directly queried government databases.”

Instead say:

> “The bank's onboarding service can integrate with approved identity-verification services to independently validate customer identity.”

SAMA guidance specifically recognizes electronic authentication services approved by the National Information Center in appropriate circumstances. ([SAMA Rulebook][3])

Your architecture becomes:

```text
FastAPI
   ↓
Identity Verification Adapter
   ↓
Approved External Verification Service
   ↓
Verification Result
```

This is also a good place to introduce the **Adapter pattern**.

```text
IdentityVerificationService
          │
    ┌─────┴──────┐
    ▼            ▼
Provider A    Provider B
```

Your business logic doesn't need to know provider-specific API details.

---

# 16. KYC validation engine

Now combine everything.

```text
Application Data
      +
OCR Data
      +
Identity Verification
      +
Document Validity
      +
Customer Risk Information
```

Then:

```text
KYC Decision Engine
```

Possible outputs:

```text
KYC_APPROVED
KYC_REJECTED
KYC_REVIEW_REQUIRED
DOCUMENT_REUPLOAD_REQUIRED
```

Example:

```text
Name Match          = PASS
DOB Match           = PASS
ID Verification     = PASS
Document Expiry     = PASS
OCR Confidence      = PASS
Sanctions Check     = PASS
Risk Score          = LOW

                    ↓

              KYC APPROVED
```

---

# 17. AML / sanctions / PEP screening

This is **not the same thing as OCR**.

Your onboarding system may send customer information to the relevant compliance screening systems.

Conceptually:

```text
Customer
   ↓
KYC
   ↓
AML screening
   ↓
Sanctions screening
   ↓
PEP screening
   ↓
Risk assessment
```

SAMA guidance says ongoing due diligence should consider whether a customer is a politically exposed person and reassess customer risk based on activities and transactions. ([SAMA Rulebook][3])

The important thing to communicate:

> KYC establishes and verifies identity; AML/compliance checks use that identity information to assess financial crime risk.

Don't confuse the two.

---

# 18. Risk-based onboarding

Not every customer should have exactly the same process.

You could have:

```text
LOW RISK
   ↓
Straight-through processing
```

versus:

```text
HIGH RISK
   ↓
Enhanced Due Diligence
   ↓
Additional documents
   ↓
Manual compliance review
```

SAMA's rules explicitly describe risk-based due diligence and enhanced measures for higher-risk customers. ([SAMA Rulebook][1])

That's a very important concept for your interview.

---

# 19. Manual review queue

Suppose OCR says:

```text
Name = Mohammed Ali
```

but customer entered:

```text
Mohammad Aly
```

and identity verification isn't an exact match.

Don't necessarily reject immediately.

Instead:

```text
KYC_CHECK
   ↓
Mismatch
   ↓
REVIEW_REQUIRED
   ↓
Operations / Compliance Officer
```

React dashboard:

```text
KYC Review Queue

Application #10023

Customer:
Mohammed Ali

Issue:
Name mismatch

OCR Confidence:
91%

Identity Verification:
Partial Match

[Approve]
[Reject]
[Request Document]
```

This introduces a very realistic **maker-checker/manual review** workflow.

---

# 20. Database design

You need to be prepared to draw these tables.

### Application

```text
onboarding_application
----------------------
application_id PK
customer_id
product_type
status
risk_level
created_at
updated_at
```

### Document

```text
kyc_document
------------
document_id PK
application_id FK
document_type
storage_reference
file_name
mime_type
file_size
document_status
uploaded_at
```

### OCR Result

```text
document_extraction
-------------------
extraction_id PK
document_id FK
field_name
field_value
confidence_score
created_at
```

### Verification

```text
identity_verification
---------------------
verification_id PK
application_id FK
verification_type
status
reference_id
verified_at
failure_reason
```

### KYC Decision

```text
kyc_decision
------------
decision_id PK
application_id FK
decision
risk_score
reason
reviewed_by
created_at
```

### Audit

```text
audit_log
---------
audit_id PK
application_id
actor
action
timestamp
details
```

---

# 21. Where should actual documents be stored?

Don't store massive PDFs as normal relational-table rows unless there's a specific reason.

A common architecture is:

```text
Database
   ↓
metadata only

Object Storage
   ↓
actual PDF/image
```

Example:

```text
DB:
document_id = 1001
application_id = 5001
storage_key = kyc/2026/09/5001/doc1001.pdf
status = VERIFIED
```

Actual file:

```text
Object Storage
└── kyc
    └── 2026
        └── 09
            └── 5001
                └── doc1001.pdf
```

This also makes your architecture scalable.

---

# 22. Security is HUGE here

You are dealing with:

```text
National IDs
Passports
Income information
Bank statements
Addresses
Personal information
```

So you need to discuss:

```text
TLS in transit
Encryption at rest
Authentication
Authorization
Role-based access
Secure object storage
Short-lived access URLs where appropriate
Secrets management
Audit logs
Data masking
Malware scanning
File-type validation
File-size limits
Access logging
```

And critically:

### Never trust the uploaded file.

The backend should validate:

```text
Extension
MIME type
File signature/magic bytes
File size
Corruption
Potential malware
```

For example, don't trust:

```text
something.pdf
```

just because the filename ends in `.pdf`.

---

# 23. Audit trail

Imagine the compliance team asks:

> “Who approved this customer's KYC?”

You need:

```text
Application 10023
    ↓
Document uploaded
    ↓
OCR processed
    ↓
Identity checked
    ↓
AML screening
    ↓
Manual review
    ↓
Approved
```

Every significant action should be auditable.

SAMA's documentation/record-keeping requirements make this especially relevant in a Saudi banking context. ([SAMA Rulebook][4])

You should say:

> “We maintained an audit trail for important onboarding and verification events so that the lifecycle of the application could be reconstructed.”

That's a strong enterprise statement.

---

# 24. What does FastAPI actually do?

This is the mental model I want you to memorize:

```text
                      React
                        │
                        ▼
                     FastAPI
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
      Application    Document       KYC
       APIs           APIs          APIs
          │             │             │
          ▼             ▼             ▼
       Database     Storage      External services
                        │
                        ▼
                   Processing
                        │
                  ┌─────┴─────┐
                  ▼           ▼
                 OCR         NLP
```

FastAPI is responsible for things like:

```text
Accept requests
Validate inputs
Authenticate users
Authorize actions
Upload metadata
Generate application IDs
Create document records
Trigger processing
Expose status APIs
Return results
```

It isn't necessarily doing:

```text
OCR itself
```

or:

```text
ML itself
```

---

# 25. Example API design

Prepare these.

### Create onboarding application

```http
POST /api/v1/onboarding/applications
```

### Get application

```http
GET /api/v1/onboarding/applications/{application_id}
```

### Upload document

```http
POST /api/v1/onboarding/applications/{application_id}/documents
```

### Get documents

```http
GET /api/v1/onboarding/applications/{application_id}/documents
```

### Get KYC status

```http
GET /api/v1/onboarding/applications/{application_id}/kyc-status
```

### Verify document

```http
POST /api/v1/documents/{document_id}/verify
```

### Manual review decision

```http
POST /api/v1/onboarding/applications/{application_id}/review
```

---

# 26. Example end-to-end flow

This is probably the **most important thing to practice**.

Imagine I am the interviewer:

> “Walk me through what happens when a customer uploads a passport.”

You should answer approximately:

> “The React frontend validates the basic file constraints such as type and size and sends the document using a multipart/form-data request to our FastAPI backend. FastAPI authenticates the user, validates the application and document type, and stores the document in secure object storage while persisting document metadata in the relational database. Rather than performing OCR synchronously within the upload request, the backend creates a processing job and returns an acknowledgement to the client. A document-processing worker then classifies the document and sends it through OCR and extraction logic. The extracted fields, along with confidence scores, are persisted. The KYC workflow then validates the extracted information against the customer's submitted data and approved external identity-verification services. The application can subsequently go through AML/sanctions/PEP and risk checks. Based on the configured rules, the application is either automatically approved, rejected, or moved to a manual-review queue. Throughout the process, important state changes are recorded in an audit trail.”

That's a **very strong answer**.

---

# 27. What exactly did YOU do?

This is the most important part for your interview.

You cannot say:

> “I built OCR, AML, government verification, object storage, machine learning and the entire banking platform.”

That will create trouble.

Instead, based on your resume, your strongest defensible ownership areas are:

```text
FastAPI backend APIs
REST API integration
Request validation
Exception handling
React/TypeScript workflows
SQL/database operations
Testing
API debugging
Performance optimization
Production/UAT issue resolution
CI/CD
AI/LLM-related workflows
```

Your resume explicitly says you worked on FastAPI/Flask services, request validation, exception handling, API integration, React interfaces, SQL optimization, PyTest/API/integration testing, CI/CD and production support. 

So position yourself as:

```text
                     MODULE
                        │
              ┌─────────┴─────────┐
              │                   │
          Business            AI/Document
          Workflow            Processing
              │                   │
              ▼                   ▼
          YOU OWNED          Specialized
                              Processing
```

For example:

> “My responsibility was primarily on the application/backend integration side. I worked on FastAPI endpoints, request validation, business workflow integration, database operations and React integration. The specialized OCR/AI capability was exposed as a processing service, and my service integrated with that workflow.”

That's much more believable.

---

# 28. Why not Flask?

This is another question I expect.

Don't say:

> “Flask cannot do it.”

Incorrect.

Say:

> “Flask can absolutely support this use case. FastAPI was a good fit when we needed strongly typed request models, automatic API documentation and asynchronous API handling. Flask is still perfectly capable for many of the same workloads.”

And because your resume contains **both FastAPI and Flask**, this gives you a nice opportunity to explain architectural choices rather than pretending one framework is universally superior. 

---

# 29. Why microservice?

You can split this system logically:

```text
Onboarding Service
Document Service
KYC Service
Identity Verification Service
Notification Service
```

Why?

Because each has different responsibilities and scaling patterns.

For example:

```text
Document Processing
```

may be CPU/AI-heavy.

While:

```text
Onboarding API
```

is more request/response oriented.

You can scale them independently.

```text
               API
                │
       ┌────────┴────────┐
       ▼                 ▼
Onboarding             KYC
  5 pods                3 pods
                         │
                         ▼
                     Processing
                       10 workers
```

Again, use this as your **architecture understanding**, not as a claim about your exact TCS deployment unless it was actually used.

---

# 30. What happens if OCR fails?

Very common interview question.

```text
OCR
 ↓
FAILED
 ↓
Retry?
 ├── YES → retry
 └── NO
       ↓
Manual Review
```

You can discuss:

```text
retry policy
timeout
dead-letter queue
failure status
observability
manual intervention
```

---

# 31. What happens if external verification is unavailable?

Suppose:

```text
Bank → Identity Verification Provider
```

and provider is down.

**Don't mark customer as approved.**

Instead:

```text
Provider unavailable
       ↓
Temporary failure
       ↓
Retry / circuit breaker
       ↓
If still unavailable
       ↓
PENDING_VERIFICATION
```

This is a very good enterprise-system answer.

---

# 32. Status machine

I strongly recommend you learn this because it makes your module feel real.

```text
APPLICATION_CREATED
        ↓
DOCUMENT_PENDING
        ↓
DOCUMENT_UPLOADED
        ↓
PROCESSING
        ↓
EXTRACTED
        ↓
VERIFICATION_PENDING
        ↓
VERIFIED
        ↓
KYC_REVIEW
        ↓
       ┌┴─────────────┐
       ▼              ▼
   APPROVED         REJECTED
```

Also:

```text
PROCESSING
    ↓
PROCESSING_FAILED
    ↓
RETRY
```

Your database should store the current state.

---

# 33. React interview depth

The interviewer may switch suddenly from architecture to:

> “How did you manage the upload in React?”

You should be able to talk about:

```text
<input type="file" />

File validation
        ↓
FormData
        ↓
Axios/fetch
        ↓
Upload API
        ↓
Progress state
        ↓
Response
        ↓
Polling / status update
```

And UI states:

```text
IDLE
UPLOADING
PROCESSING
SUCCESS
ERROR
```

That's excellent React state-management discussion.

---

# 34. Database interview depth

They can ask:

> “What happens when OCR produces multiple fields?”

You can store a JSON extraction result, or a normalized field table, depending on requirements.

For example:

```text
document_extraction
-------------------
document_id
field_name
field_value
confidence
```

or:

```text
document
  +
extraction_json
```

You should be prepared to explain the trade-off:

```text
Normalized
→ easier querying/reporting

JSON
→ flexible extraction schema
```

---

# 35. Performance discussion

This module gives you many performance opportunities.

### Don't process 20 MB document inside every API worker.

Instead:

```text
API
 ↓
Storage
 ↓
Queue
 ↓
Workers
```

### Don't repeatedly query huge document metadata tables.

Use:

```text
indexes
pagination
appropriate projections
```

### Don't send massive OCR output to every frontend response.

Return:

```json
{
  "application_id": 123,
  "status": "PROCESSING"
}
```

and provide a status endpoint.

---

# 36. Security question: "What are the biggest risks?"

Your answer:

```text
Malicious file upload
Unauthorized document access
PII exposure
Broken authorization
Sensitive data in logs
Credential leakage
Document tampering
Replay/duplicate submission
Third-party integration compromise
```

Then explain mitigations.

That is exactly the sort of security thinking expected from someone presenting a banking system.

---

# 37. The architecture I want you to remember

Put this on paper and practice it:

```text
                      ┌──────────────┐
                      │    React     │
                      │ TypeScript   │
                      └──────┬───────┘
                             │
                           HTTPS
                             │
                             ▼
                  ┌────────────────────┐
                  │   FastAPI Gateway  │
                  │ Auth + Validation  │
                  └──────────┬─────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
        Onboarding API   Document API    KYC API
              │              │              │
              ▼              ▼              ▼
             DB         Object Storage   KYC Workflow
                             │              │
                             ▼              │
                       Message Queue        │
                             │              │
                             ▼              │
                     Document Worker        │
                       │          │         │
                       ▼          ▼         │
                      OCR        NLP        │
                       │          │         │
                       └────┬─────┘         │
                            ▼               │
                       Extraction          │
                            │               │
                            └──────┬────────┘
                                   ▼
                         Identity Verification
                                   │
                                   ▼
                             AML / Risk
                                   │
                            ┌──────┴──────┐
                            ▼             ▼
                         APPROVE       MANUAL REVIEW
                            │
                            ▼
                      Core Banking
```

---

# 38. And here's the most important interview positioning

Don't call yourself:

> **“The person who developed an OCR system.”**

Call yourself:

> **“The Python full-stack engineer who worked on the digital onboarding/KYC workflow, particularly the API, application workflow, database integration, frontend integration, validation, testing and production support.”**

That aligns with your stated experience and the IKS requirement for end-to-end work across frontend, backend, APIs, databases and cloud infrastructure. 

And this module is particularly valuable because you can demonstrate **all of these in one story**:

```text
Python
FastAPI
Pydantic
REST API
Async programming
React
TypeScript
File upload
Database
SQL
Microservices
External API integration
OCR
NLP
AI/LLM concepts
Authentication
Authorization
Security
Async workers
Queues
Error handling
Testing
CI/CD
Performance
Audit
KYC
AML
Risk
Production support
```

One caution: the specific Udemy course link you pasted is a **course/example source**, not evidence that SNB/TCS implemented that exact architecture. For interview preparation, use the architecture above as a realistic Saudi-banking design, but phrase actual hands-on claims according to what you genuinely worked on.

For Saudi context, the strongest regulatory points to know are that CDD occurs during onboarding and may need to be enhanced for higher-risk customers, identity should be verified through reliable independent sources, electronic authentication can be used in appropriate cases, and relevant due-diligence/transaction records have long retention requirements. ([SAMA Rulebook][1])

Next, the best preparation step is to turn this into a **concrete interview project**: I'll lay out the exact **React screens → FastAPI folder structure → Pydantic models → API request/response JSON → database schema → document upload code → async processing → OCR service → KYC state machine → error handling → PyTest cases**, so you can actually understand and defend every part rather than just memorize the architecture.

[1]: https://rulebook.sama.gov.sa/en/422-customer-due-diligence?utm_source=chatgpt.com "4.2.2 Customer Due Diligence | SAMA Rulebook"
[2]: https://rulebook.sama.gov.sa/en/article-12-24?utm_source=chatgpt.com "Article 12 | SAMA Rulebook"
[3]: https://www.rulebook.sama.gov.sa/en/due-diligence-measures-0?utm_source=chatgpt.com "A. Due Diligence Measures | SAMA Rulebook"
[4]: https://rulebook.sama.gov.sa/en/instructions-documentation-and-record-keeping?utm_source=chatgpt.com "Instructions on Documentation and Record Keeping | SAMA Rulebook"
