
# STORY 1 — OCR DATA MISMATCH / VALIDATION ISSUE

## 1. Core Interview Story

### Question: Tell me about a technical problem you faced in your project and how you resolved it.

**Answer:**

> One issue I faced was during the identity verification flow where we were using Azure AI Document Intelligence for document processing. The OCR extraction itself was successful, but in some cases the extracted fields, especially date-related fields such as DOB, were returned in a format different from what our backend validation expected.
>
> Initially, this caused a valid document to fail our validation and move to a `REVIEW_REQUIRED` state. I debugged the issue by tracing the value from the Azure response through the mapping layer and finally to the backend validation logic.
>
> I found that the problem was not incorrect OCR extraction. The extracted information was correct, but its representation did not match our application's expected format.
>
> To resolve this, we introduced a normalization step between the external OCR response and the business validation layer. The extracted date was converted into our application's canonical format before validation.
>
> This kept the business validation logic independent of the external service's response format and prevented valid documents from incorrectly moving to `REVIEW_REQUIRED` because of formatting differences.

---

# 2. Problem in One Line

> **Azure Document Intelligence extracted the correct information, but the external data representation did not match the format expected by our backend validation layer.**

---

# 3. Root Cause

> **The root cause was a data-format mismatch between the external OCR response and our application's internal validation contract.**

Example:

```text
External OCR response
        ↓
Different date representation
        ↓
Backend expected canonical format
        ↓
Validation failure
        ↓
REVIEW_REQUIRED
```

---

# 4. How I Identified the Problem

### Question: How did you identify that the problem was in the date-format handling and not Azure OCR itself?

**Answer:**

> I identified the issue from the `REVIEW_REQUIRED` status. I traced the DOB value through the flow, starting from the Azure Document Intelligence response, then the mapping layer, and finally the backend validation logic.
>
> I found that the customer's DOB was correct, and Azure had also extracted the correct date, but the representation was different from what our backend expected.
>
> So the problem was not incorrect OCR extraction; it was a format mismatch between the external service response and our internal validation contract.

### Key sentence

> **The problem was not incorrect OCR extraction; it was a format mismatch between the external service response and our internal validation contract.**

---

# 5. Why Did You Introduce a Normalization Layer?

### Question: Why didn't you simply modify the existing validation logic to support multiple formats?

**Answer:**

> We could have modified the validation logic to handle multiple date formats, but we preferred to isolate the external-format transformation from the existing business validation logic.
>
> The normalization layer acts as a boundary between the external Azure response and our internal business logic. It converts incoming data into a canonical format, and then the existing validation logic works with a consistent input.
>
> This avoids adding multiple format-specific `if-else` conditions inside the business validation layer and keeps the code easier to maintain and extend.

### Strong principle

> **Normalization handles external data representation; validation handles business rules.**

---

# 6. Open/Closed Principle

### Question: Which design principle does this approach follow?

**Answer:**

> This is consistent with the Open/Closed Principle — software should be open for extension but closed for modification. In our case, instead of adding external-format-specific conditions into existing validation rules, we extended the processing flow with a normalization layer.

### Important clarification

Do **not** say:

> “We cannot modify existing code because of OCP.”

Say:

> **“We preferred to isolate the external-format transformation from the existing business validation logic.”**

---

# 7. Why Normalization Is Better Here

### Question: What is the benefit of normalization?

**Answer:**

> It provides a clean boundary between external data and internal business logic. The external service can return data in its own representation, while the rest of our application works with a consistent canonical representation.

### Flow

```text
Azure Response
      ↓
Normalization
      ↓
Canonical Data
      ↓
Business Validation
      ↓
Workflow Decision
```

---

# 8. How Would You Implement the Normalization Layer?

### Question: How would you design the normalization layer so it can support multiple date formats?

**Answer:**

> I would keep the normalization logic centralized in a dedicated component or service. It would accept the external value, parse the supported formats, convert the value into a common Java representation such as `LocalDate`, and then pass that normalized value to the validation layer.
>
> If a new supported format is introduced, I only need to update the normalization component rather than changing the business validation logic.

### Conceptual structure

```text
AzureDocumentClient
        ↓
DocumentResponseMapper
        ↓
DocumentNormalizer
        ↓
IdentityValidator
        ↓
Workflow Decision
```

### Conceptual Java implementation

```java
public LocalDate normalizeDob(String dob) {

    List<DateTimeFormatter> formatters = List.of(
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    for (DateTimeFormatter formatter : formatters) {
        try {
            return LocalDate.parse(dob, formatter);
        } catch (DateTimeParseException ignored) {
        }
    }

    throw new InvalidDocumentDataException("Unsupported DOB format");
}
```

Then:

```java
LocalDate dob = normalizer.normalizeDob(extractedDob);
validationService.validateDob(dob, customerDob);
```

### Important

Do not claim that your actual production implementation looked exactly like this unless you actually wrote it this way. This is the **defensible design approach**.

---

# 9. What If a New Date Format Comes Tomorrow?

### Question: What happens if another date format is introduced later?

**Answer:**

> We would add the new supported format in the normalization component only. The downstream business validation logic would remain unchanged because it works with the canonical representation.

```text
New external format
        ↓
Normalization layer updated
        ↓
Canonical representation unchanged
        ↓
Validation unchanged
```

---

# 10. What If the Date Is Ambiguous?

### Question: What if the OCR returns something like `05-06-1998`, where the format itself is ambiguous?

**Answer:**

> We should not guess the meaning. The accepted format should be determined from the document type or upstream contract. If the format is ambiguous or unsupported, we should reject the value or move the case to `REVIEW_REQUIRED` rather than silently interpreting it incorrectly.

---

# 11. OCR vs Document Authenticity

### Question: Does Azure AI Document Intelligence determine whether the document is genuine or fake?

**Answer:**

> No. OCR or document extraction does not by itself prove document authenticity. Azure Document Intelligence primarily performs document analysis and information extraction.
>
> Our application then performs its own validation, and the identity/KYC verification process is responsible for the final verification decision.

### Key sentence

> **Successful OCR does not mean successful identity verification.**

---

# 12. What Does the Azure Confidence Score Mean?

### Question: What does the confidence score from Azure mean?

**Answer:**

> The confidence score indicates how confident the model is about the extracted prediction or field. It is an extraction-confidence measure; it is not a document-authenticity score.

### Important distinction

```text
Confidence Score
      ↓
"How confident is the model about the extraction?"

NOT

"How confident is the system that the document is genuine?"
```

---

# 13. Can 90% Confidence Mean the Document Is Genuine?

### Question: If Azure gives 90% confidence, can you consider the document valid?

**Answer:**

> No. A 90% confidence score means Azure is confident about the extraction. It does not prove that the document itself is genuine. We still need application-level validation and identity/KYC verification.

---

# 14. What Happens When Confidence Is Below the Threshold?

### Question: What do you do when confidence is below your configured threshold?

**Answer:**

> If the confidence score for a critical extracted field is below our configured threshold, we do not automatically accept the extraction. We can mark the document verification as requiring further action and either ask the customer to upload the document again or route it for manual review, depending on the scenario.

Flow:

```text
Azure Extraction
      ↓
Confidence Check
      ↓
≥ Configured Threshold
      ↓
Application Validation
      ↓
Continue Workflow
```

```text
< Configured Threshold
      ↓
REVIEW_REQUIRED / REUPLOAD
      ↓
Process Again
```

---

# 15. What About the 85% Threshold?

### Question: You mentioned 85%. Is that an Azure-defined threshold?

**Answer:**

> No. The 85% threshold is an application-level business rule configured by the application based on its validation requirements. It should not be described as a universal Azure authenticity threshold.

### If your actual project used 85%

Say:

> **“In our application, we configured an 85% confidence threshold for certain extracted fields. Below that threshold, we would not automatically accept the extraction and would route it for retry or review.”**

---

# 16. What If the Customer Fails Three Times?

### Question: What happens if the customer uploads a document three times and all attempts are below the threshold?

**Answer:**

> After the configured number of unsuccessful attempts, we can move the onboarding session to `REVIEW_REQUIRED` and route it to the operations or manual-review team. We should not automatically assume that the customer is fraudulent only because the extraction confidence was low.

### Example state

```text
attempt_count = 3
identity_status = REVIEW_REQUIRED
failure_reason = LOW_OCR_CONFIDENCE
```

Then:

```text
REVIEW_REQUIRED
      ↓
Operations / Manual Review
      ↓
APPROVE / REJECT
```

---

# 17. Idempotency vs Attempt Limit

### Question: Does the idempotency key prevent the customer from uploading the same document three times?

**Answer:**

> No. Idempotency and attempt limits solve different problems. Idempotency prevents the same request from being processed multiple times, while the attempt limit is a business rule controlling how many verification attempts the customer is allowed to make.

### Key sentence

> **Idempotency prevents duplicate processing; attempt limits control repeated business attempts.**

---

# 18. How Do You Handle Concurrent Upload Requests?

### Question: What if two requests arrive concurrently for the same document/session?

**Answer:**

> We maintain an idempotency key for the request and use database-level concurrency control. A unique constraint can prevent the same idempotency key from being inserted multiple times, while optimistic locking with a version field can protect concurrent updates to the same processing record.
>
> We can also perform the state transition atomically so that only one request can successfully acquire the required state transition.

---

# 19. Idempotency vs Optimistic Locking

### Question: What is the difference between idempotency and optimistic locking?

**Answer:**

> Idempotency handles duplicate requests. Optimistic locking handles concurrent modifications to the same database record.

```text
Idempotency
→ Have I already processed this request?

Optimistic Locking
→ Has somebody modified this record since I read it?
```

---

# 20. Document Processing Table

### Question: What would you store in the document-processing table?

**Answer:**

```text
document_processing

id
session_id
document_id
idempotency_key
status
retry_count
version
azure_operation_id
created_at
updated_at
```

Possible statuses:

```text
UPLOADED
PROCESSING
COMPLETED
FAILED
REVIEW_REQUIRED
```

### Why each field?

| Field                | Purpose                                     |
| -------------------- | ------------------------------------------- |
| `id`                 | Primary key                                 |
| `session_id`         | Identify onboarding session                 |
| `document_id`        | Identify uploaded document                  |
| `idempotency_key`    | Detect duplicate request                    |
| `status`             | Current business-processing state           |
| `retry_count`        | Number of processing attempts               |
| `version`            | Optimistic concurrency control              |
| `azure_operation_id` | Track asynchronous Azure operation, if used |
| `created_at`         | Audit                                       |
| `updated_at`         | Audit                                       |

---

# 21. Why Both Status and Version?

### Question: Why do you need both `status` and `version`? Can't status alone prevent concurrency?

**Answer:**

> Status and version serve different purposes. Status represents the business state of the document, such as `UPLOADED`, `PROCESSING`, `COMPLETED`, or `FAILED`. Version is a technical field used for optimistic concurrency control.
>
> For a specific state transition, status itself can sometimes be used in an atomic conditional update. However, the version field provides general stale-write protection when multiple requests can update the same record.

### Example

Initial:

```text
status = UPLOADED
version = 5
```

Request A:

```text
version 5 → update succeeds
version becomes 6
```

Request B:

```text
still has version 5
→ version mismatch
→ update fails
```

### Key distinction

> **Status answers “What state is the document in?” while version answers “Has someone modified this record since I read it?”**

---

# 22. Can Status Alone Handle Concurrency?

### Question: Can you implement concurrency control without a version column?

**Answer:**

> Yes. For narrowly defined state transitions, an atomic conditional update based on status can be sufficient.

Example:

```sql
UPDATE document_processing
SET status = 'PROCESSING'
WHERE id = ?
  AND status = 'UPLOADED';
```

If only one request updates the row from `UPLOADED` to `PROCESSING`, then only that request succeeds.

However:

> We use optimistic locking with a version field when we need more general protection against stale updates to the entity.

---

# 23. What Happens When Optimistic Locking Fails?

### Question: If optimistic locking fails, what do you do?

**Answer:**

> It depends on the operation. For a workflow state transition, I would first re-read the latest state and check whether the requested transition is still valid. If it is a transient conflict, we can retry with the latest version. Otherwise, we return an appropriate conflict result instead of blindly retrying.

---

# 24. Atomic State Transition

### Question: What do you mean by atomic state transition?

**Answer:**

> It means the state change is performed as one database operation where the current state is part of the update condition, so only one valid request can successfully perform that transition.

Example:

```sql
UPDATE document_processing
SET status = 'PROCESSING',
    version = version + 1
WHERE id = ?
  AND status = 'UPLOADED'
  AND version = ?;
```

If:

```text
Rows updated = 1
```

the transition succeeded.

If:

```text
Rows updated = 0
```

then another request may already have changed the record or the transition is no longer valid.

---

# 25. Outbox Pattern in This Story

### Question: Why would you use the Outbox pattern?

**Answer:**

> We use the Outbox pattern to reliably persist the business state change and the event that needs to be published. The document-processing update and the outbox event are stored in the same database transaction, so either both commit or both roll back.

Architecture:

```text
                         DATABASE
                             │
              ┌──────────────┴──────────────┐
              ↓                             ↓
   ┌───────────────────┐          ┌─────────────────┐
   │ document_processing│          │   outbox_event  │
   │-------------------│          │-----------------│
   │ id                │          │ id              │
   │ status            │          │ event_type      │
   │ version           │          │ status=PENDING  │
   └─────────┬─────────┘          └────────┬────────┘
             │                             │
             └──────────────┬──────────────┘
                            │
                          COMMIT
                            │
                            ▼
                    APPLICATION CRASH
                            │
                            ▼
                 outbox_event still exists
                            │
                            ▼
                   OUTBOX PUBLISHER
                            │
                            ▼
                          KAFKA
                            │
                            ▼
                        CONSUMER
                            │
                            ▼
                         AZURE
                            │
                            ▼
                    RESULT / STATUS
```

---

# 26. What Exactly Does Outbox Solve?

### Question: What problem does Outbox solve in this scenario?

**Answer:**

> Outbox protects against losing an event between the database transaction and Kafka publishing. If the database transaction commits and the application crashes before publishing the event, the event is still stored in the outbox table and can be published after recovery.

### Core guarantee

```text
Business DB Update
       +
Outbox Event
       ↓
Same transaction
       ↓
Both commit
      OR
Both rollback
```

---

# 27. What Does Outbox NOT Solve?

### Question: Does Outbox guarantee that Azure will never receive the request twice?

**Answer:**

> No. Outbox does not provide exactly-once execution for an external API call. It guarantees reliable event persistence/publication. Duplicate execution of the downstream operation still needs to be handled using idempotency and processing-state management.

### Key distinction

```text
Outbox
→ Reliable event publication

Idempotency
→ Duplicate business-operation protection
```

---

# 28. Crash Scenario After Database Commit

### Question: What happens if the application crashes after the database transaction commits?

**Answer:**

> If the outbox event was committed in the same transaction, the event remains in the outbox table. After the application recovers, the outbox publisher reads the pending event and publishes it to Kafka.

```text
DB Transaction
   ↓
document_processing = PROCESSING
outbox_event = PENDING
   ↓
COMMIT
   ↓
💥 APPLICATION CRASH
   ↓
APPLICATION RECOVERS
   ↓
OUTBOX PUBLISHER
   ↓
KAFKA
```

---

# 29. Crash After Azure Successfully Processes the Document

### Question: What if Azure succeeds but the application crashes before updating our database?

**Answer:**

> This is a different failure scenario. Outbox alone does not solve it because Azure is an external side effect. We need idempotency and processing-state tracking for the document-processing operation. On recovery or retry, we should determine whether the operation has already been submitted or completed and avoid blindly repeating the business operation.

### Important distinction

```text
DB → Kafka reliability
        ↓
       OUTBOX

External Azure duplicate protection
        ↓
    IDEMPOTENCY
```

---

# 30. Idempotency Table Design

### Question: How would you implement idempotency for document processing?

**Answer:**

> We would persist an idempotency key against the document-processing operation and enforce a unique constraint on that key. When a request arrives, we first check whether that operation already exists. If it has already been processed, we return the existing state/result instead of executing the operation again.

Conceptually:

```text
document_processing

id
session_id
document_id
idempotency_key UNIQUE
status
retry_count
version
```

---

# 31. Why Not Pass Idempotency Key to Azure?

### Question: Do you pass your application's idempotency key to Azure and rely on Azure to prevent duplicates?

**Answer:**

> Not necessarily. The application-level idempotency key primarily belongs to our own processing flow. We use it in our application and database to prevent duplicate business processing. We should not assume that an external service will enforce our application's business idempotency semantics.

---

# 32. Kafka and This Story

### Question: Where does Kafka fit into this OCR flow?

**Answer:**

> Kafka can be used to decouple the long-running document-processing workflow from the synchronous customer request. The application publishes a document-processing event, and a consumer processes the document and interacts with Azure asynchronously.

Flow:

```text
Identity Verification
        ↓
Kafka
        ↓
Document Processing Consumer
        ↓
Azure Document Intelligence
        ↓
Result
        ↓
Identity Validation
```

---

# 33. Why Kafka Instead of Synchronous REST?

### Question: Why not just call the document-processing service using REST?

**Answer:**

> REST could work for a simpler design. Kafka is useful here because document processing is asynchronous and potentially long-running. It decouples the producer from the consumer, provides buffering, and allows independent scaling and recovery of the downstream processing.

### Important

Do not say:

> “Kafka was mandatory.”

Say:

> **“REST was technically possible, but Kafka was more suitable for this asynchronous and decoupled processing flow.”**

---

# 34. Why Not Simply Return 202 and Continue With REST?

### Question: Can't you just make the REST API asynchronous and return 202?

**Answer:**

> Yes, that is technically possible. However, the services would still have a direct runtime dependency on each other. Kafka provides stronger decoupling because the producer can publish the event without requiring the consumer to be immediately available.

---

# 35. What Should Go Into Kafka?

### Question: Do you send the actual document through Kafka?

**Answer:**

> Not necessarily. A better design is generally to send an event containing the session ID, document ID, document type, and other processing metadata, while keeping the actual document in the appropriate storage mechanism. The consumer can then retrieve the document using the reference.

Example:

```json
{
  "eventType": "DOCUMENT_UPLOADED",
  "sessionId": "S100",
  "documentId": "D123",
  "documentType": "PASSPORT"
}
```

**Only claim this specific design if it matches the actual implementation.**

---

# 36. Very Important Fake Document Question

### Question: If the customer uploads a fake document, does OCR detect it?

**Answer:**

> Not by OCR alone. OCR extracts information from the document; it does not by itself prove that the document is genuine. The extracted information is then used by application validation and the identity/KYC verification process to make the verification decision.

---

# 37. What If the Fake Document Is Perfectly Readable?

### Question: What if someone uploads a forged document and OCR extracts everything correctly?

**Answer:**

> OCR can still successfully extract the information because its responsibility is document analysis and extraction. A sophisticated forged document cannot be considered authentic just because OCR succeeded. Authenticity requires the appropriate identity verification, KYC, fraud-detection, authoritative verification, or manual-review mechanism available in the banking workflow.

---

# 38. What Is the Correct Workflow for Identity Verification?

```text
Customer
   ↓
Document Upload
   ↓
Identity Verification
   ├── Document Processing
   │       ↓
   │   Azure OCR / Analysis
   │       ↓
   │   Extracted Data
   │       ↓
   │   Application Validation
   │
   ├── Identity / Authenticity Verification
   │
   ↓
Identity VERIFIED
   ↓
KYC
   ↓
IAM / Customer Provisioning
   ↓
Activation
```

### Key distinction

```text
Azure
→ Extract

Identity Verification
→ Verify identity/document

KYC
→ Compliance verification

IAM
→ Identity provisioning/authentication

Orchestrator
→ Workflow coordination
```

---

# 39. What If OCR Data Is Correct but the Document Is Fake?

### Question: How would your application handle that?

**Answer:**

> If the extracted information is syntactically correct but the identity or authenticity verification fails, the identity verification step should move to `FAILED` or `REVIEW_REQUIRED`, depending on the business rules. The onboarding orchestrator should not move the customer to the next stage until the required verification is successfully completed.

---

# 40. Review State

Possible states:

```text
UPLOADED
PROCESSING
VERIFIED
FAILED
REVIEW_REQUIRED
```

Example:

```text
Document
   ↓
Processing
   ↓
Validation
   ↓
REVIEW_REQUIRED
   ↓
Manual Review
   ↓
APPROVED / REJECTED
```

---

# 41. Most Important Corrections From Your Original Answers

### Don't say:

> “Azure's decision engine tells us whether the document is genuine.”

### Say:

> **“Azure provides extraction confidence; authenticity is a separate verification concern.”**

---

### Don't say:

> “85% is Azure's threshold.”

### Say:

> **“85% is an application-level threshold configured based on business requirements.”**

---

### Don't say:

> “Optimistic locking locks the row.”

### Say:

> **“Optimistic locking uses a version value to detect stale concurrent updates.”**

---

### Don't say:

> “Outbox knows Azure already succeeded.”

### Say:

> **“Outbox provides reliable event persistence/publication; external side effects still require idempotency and state tracking.”**

---

### Don't say:

> “Idempotency prevents every concurrent request.”

### Say:

> **“Idempotency prevents duplicate processing of the same logical request; concurrency control protects concurrent modifications.”**

---

### Don't say:

> “Same idempotency key prevents the customer from uploading the same document again.”

### Say:

> **“Idempotency prevents the same request from being processed repeatedly; re-upload limits are a separate business rule.”**

---

# 42. 30-Second Version of the Entire Story

If the interviewer wants a very short answer:

> **“I faced an issue in the identity verification flow where Azure Document Intelligence successfully extracted the customer's DOB, but the representation reaching our backend did not match the format expected by our validation logic. I traced the value from the Azure response through the mapping and validation layers and identified the mismatch. We introduced a normalization layer at the integration boundary so external values were converted into a canonical format before business validation. This kept the validation logic independent of the external response format and prevented valid documents from incorrectly moving to `REVIEW_REQUIRED`.”**

---

# 43. Story 1 — Final Mental Model

```text
                 IDENTITY VERIFICATION
                         │
                  Document Upload
                         │
                         ▼
               Azure Document Intelligence
                         │
                Extracted Information
                         │
                         ▼
                  Normalization
                         │
                Canonical Data
                         │
                         ▼
                Business Validation
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
           SUCCESS             REVIEW_REQUIRED
              │                     │
              ▼                     ▼
             KYC                Re-upload /
                                  Manual Review
```

### The one-line story to remember

> **“The external OCR output was correct, but its representation did not match our internal validation contract, so I helped resolve it by normalizing the external data before it entered the business-validation layer.”**

---

# 44. Story 1 — Keywords to Remember

```text
Azure AI Document Intelligence
OCR
Document Analysis
Extraction Confidence
Data Contract
Format Mismatch
Normalization
Canonical Representation
Validation Layer
Separation of Concerns
Open/Closed Principle
Identity Verification
REVIEW_REQUIRED
Idempotency
Optimistic Locking
Atomic State Transition
Kafka
Outbox Pattern
Crash Recovery
Retry
```

**Story 1 is now ready to be kept as a single Markdown section.**
