
# STORY 2 — DUPLICATE ONBOARDING REQUEST / IDEMPOTENCY

## 1. Core Story

### Interview Question
**Tell me about a problem you faced with duplicate requests in your onboarding application and how you resolved it.**

### Answer

One issue we encountered in the onboarding workflow was duplicate requests for the same business operation.

For example, during identity verification, the same request could reach the backend more than once because of a repeated submission, frontend retry, or network-related retry. If the same business operation was executed multiple times, it could cause duplicate downstream processing and unnecessary side effects.

Initially, we did not have an idempotency mechanism for that operation. I identified that we needed a way to distinguish a new business request from a retry of a request that had already been submitted.

We introduced an idempotency key for the logical operation and persisted it with the processing record. We also added a database unique constraint on the idempotency key.

When the same request arrives again with the same idempotency key, the service checks the existing processing record. If the operation has already completed, we return the existing result. If it is still processing, we return the current processing status instead of starting the operation again.

This made the operation idempotent and prevented duplicate business processing.

---

# 2. The Problem in Simple Terms

The important point is:

> **The problem was not simply that two HTTP requests arrived. The real problem was that the same business operation could be executed twice.**

Example:

```text
Request A
   ↓
Identity Verification
   ↓
Business Operation
   ↓
Success


Request B — same logical operation
   ↓
Identity Verification
   ↓
Business Operation AGAIN ❌
````

Without idempotency, Request B may repeat the same side effect.

With idempotency:

```text
Request A
   ↓
idempotencyKey = K123
   ↓
Process


Request B
   ↓
idempotencyKey = K123
   ↓
Already processed?
   ↓
Reuse existing result
```

---

# 3. What Is an Idempotency Key?

### Question

**What exactly do you mean by an idempotency key?**

### Answer

An idempotency key is a unique identifier for one logical business operation.

The important idea is:

> **Different HTTP requests can represent the same business operation.**

The idempotency key gives those requests a common identity.

Example:

```text
POST /identity-verification

Idempotency-Key: K123
```

If the client sends the same logical request again:

```text
Idempotency-Key: K123
```

the backend knows that it is a retry or duplicate of the same operation.

---

# 4. Example From Our Onboarding Flow

Suppose the onboarding orchestrator initiates identity verification.

```text
Mobile Verification
        ↓
Onboarding Orchestrator
        ↓
Identity Verification Service
```

The orchestrator propagates a unique idempotency key:

```text
sessionId = S100
idempotencyKey = K123
```

The Identity Verification service persists that key against its processing record.

Now:

```text
First request
K123
↓
No existing record
↓
Process


Second request
K123
↓
Existing record found
↓
Do NOT process again
```

---

# 5. What Happens for Different Processing States?

### Question

**What happens if the same idempotency key is received again?**

### Answer

We need to consider the current state of the existing operation.

```text
Existing key not found
        ↓
Process the request
```

```text
Existing key + COMPLETED
        ↓
Return the existing result
```

```text
Existing key + PROCESSING
        ↓
Do not start another operation
        ↓
Return current processing status
```

This is important because idempotency is not simply:

> "If key exists, always return success."

The existing state matters.

---

# 6. Why Do We Need a Database Unique Constraint?

### Question

**Why not just check the idempotency key in application code?**

### Answer

A simple application-level check is vulnerable to a race condition.

Suppose two requests arrive at exactly the same time:

```text
Request A → SELECT → key not found
Request B → SELECT → key not found
```

Both can then try to insert the same key.

So we add:

```text
UNIQUE(idempotency_key)
```

at the database level.

Then:

```text
Request A → INSERT K123 → SUCCESS
Request B → INSERT K123 → UNIQUE CONSTRAINT VIOLATION
```

The database becomes the final protection against duplicate processing records.

---

# 7. Why Application Check + DB Constraint?

### Question

**So why do you need both application-level idempotency checking and a unique constraint?**

### Answer

The application-level check lets us return a meaningful existing result or processing status.

The database unique constraint provides the final consistency guarantee under concurrent requests.

So the responsibilities are:

```text
Application check
→ Business decision

Database unique constraint
→ Final data-integrity protection
```

---

# 8. Where Is the Idempotency Key Stored?

### Question

**Where exactly do you store the idempotency key?**

### Answer

The orchestrator can propagate the key, but the service that owns the business operation should persist and enforce it.

For example:

```text
document_processing
--------------------------
id
session_id
document_id
idempotency_key UNIQUE
status
retry_count
version
created_at
updated_at
```

The downstream service becomes responsible for enforcing the idempotency of its own business operation.

---

# 9. Two Same-Key Requests Arriving Simultaneously

### Question

**What happens if two requests with the same idempotency key arrive at exactly the same time?**

### Answer

For a first-time request, optimistic locking alone is not enough because there may not even be a database row yet.

The initial race is handled by the unique constraint:

```text
Request A ─┐
           ├── K123
Request B ─┘

A → INSERT K123 → SUCCESS
B → INSERT K123 → UNIQUE CONSTRAINT FAILURE
```

The second request can then fetch the already-created processing record and use its current state/result.

For updates to an existing record, optimistic locking can protect against stale concurrent updates.

---

# 10. Idempotency vs Optimistic Locking

### Question

**What is the difference between idempotency and optimistic locking?**

### Answer

They solve different problems.

```text
Idempotency
→ Have I already processed this logical request?

Optimistic Locking
→ Has this database record changed since I read it?
```

Example:

```text
Duplicate request
→ Idempotency

Two requests updating the same existing row
→ Optimistic locking
```

---

# 11. Why Not Pessimistic Locking?

### Question

**Why would you prefer optimistic locking here instead of pessimistic locking?**

### Answer

If the operation involves an external service such as Azure Document Intelligence, we should avoid holding a database lock while waiting for a network call.

With pessimistic locking:

```text
DB lock
  ↓
External API call
  ↓
Wait
  ↓
Release lock
```

A slow external service could keep a database lock open for a long time.

Optimistic locking doesn't hold the row lock during the external call. It detects a conflicting update when the record is written back.

Therefore, for this type of workflow, optimistic locking can be a better fit for concurrent database updates.

---

# 12. Document Processing Table

### Question

**What would your document-processing table look like?**

### Answer

Conceptually:

```text
document_processing
----------------------------
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

### Meaning

```text
id
→ Primary key

session_id
→ Onboarding session

document_id
→ Uploaded document

idempotency_key
→ Duplicate request detection

status
→ Current business processing state

retry_count
→ Number of processing attempts

version
→ Optimistic locking

azure_operation_id
→ Track Azure's asynchronous operation, if applicable
```

---

# 13. Why Both Status and Version?

### Question

**Why do you need both status and version? Can't status alone prevent concurrent processing?**

### Answer

Status and version have different purposes.

Status represents the business state:

```text
UPLOADED
PROCESSING
COMPLETED
FAILED
```

Version is a technical field for optimistic concurrency control.

So:

```text
STATUS
"What state is the document in?"

VERSION
"Has someone modified this record since I read it?"
```

For a narrow state transition, status can sometimes be enough if we do an atomic conditional update:

```sql
UPDATE document_processing
SET status = 'PROCESSING'
WHERE id = ?
  AND status = 'UPLOADED';
```

But version gives general stale-write protection when several parts of the application can update the same entity.

---

# 14. How Does Optimistic Locking Work?

Suppose:

```text
status = UPLOADED
version = 5
```

Both Request A and Request B read version 5.

Request A updates first:

```text
version 5 → 6
```

Request B still has version 5.

Its update becomes:

```sql
UPDATE document_processing
SET status = 'PROCESSING',
    version = version + 1
WHERE id = ?
  AND version = 5;
```

Since the actual version is already 6:

```text
Rows updated = 0
```

This tells us that another request modified the record.

---

# 15. Can We Do It Without a Version Column?

### Question

**Can you implement this without optimistic locking?**

### Answer

Yes.

For a narrowly defined state transition, an atomic conditional update can be sufficient:

```sql
UPDATE document_processing
SET status = 'PROCESSING'
WHERE id = ?
  AND status = 'UPLOADED';
```

Only one request should successfully perform the transition.

However, a version field is useful when we need broader protection against stale updates to the same entity.

---

# 16. What Happens When Optimistic Locking Fails?

### Question

**What do you do if optimistic locking fails?**

### Answer

It depends on the operation.

For a workflow transition, I would first re-read the latest state.

If the requested transition is still valid, we can retry using the latest version.

If the transition is no longer valid, we should return an appropriate conflict/current-state response rather than blindly retrying.

---

# 17. Kafka in Story 2

### Question

**How does Kafka fit into this idempotency story?**

### Answer

Kafka can be used for the asynchronous portion of the workflow.

For example:

```text
Identity Verification
        ↓
Kafka Event
        ↓
Document Processing Consumer
        ↓
Azure Document Intelligence
```

Kafka allows the processing to happen asynchronously and decouples the producer from the downstream consumer.

---

# 18. Why Kafka Instead of REST?

### Question

**Could you have done this using REST instead?**

### Answer

Yes.

REST is technically possible, especially for a simpler system.

Kafka is useful when the operation is asynchronous or long-running because it provides:

* decoupling
* buffering
* independent scaling
* reliable event delivery

So the important statement is:

> **Kafka was useful for the asynchronous processing requirement; it wasn't the only technically possible solution.**

---

# 19. Could We Just Use Asynchronous REST?

### Question

**Why not just return 202 from REST and process asynchronously?**

### Answer

That is possible.

However, the services still have a direct runtime dependency on each other:

```text
Service A
   ↓ HTTP
Service B
```

Kafka provides stronger decoupling:

```text
Service A
   ↓
Kafka
   ↓
Service B
```

The producer does not require the consumer to be immediately available.

---

# 20. What Goes Into Kafka?

### Question

**Do you send the entire document through Kafka?**

### Answer

Not necessarily.

A better design is to publish an event containing references and metadata such as:

```json
{
  "eventType": "DOCUMENT_UPLOADED",
  "sessionId": "S100",
  "documentId": "D123",
  "documentType": "PASSPORT"
}
```

The actual document can remain in appropriate document storage.

Only claim this exact implementation if it matches the actual project.

---

# 21. Duplicate Kafka Events

### Question

**Can Kafka deliver the same event twice?**

### Answer

Yes.

In an at-least-once processing model, duplicate delivery is possible.

For example:

```text
Kafka Event
   ↓
Consumer processes it
   ↓
Business transaction commits
   ↓
Consumer crashes
   ↓
Kafka offset was not committed
   ↓
Same event is delivered again
```

This is why the consumer must be idempotent.

---

# 22. Exactly-Once vs At-Least-Once

### Question

**Are you using exactly-once processing?**

### Answer

The safer explanation is:

> Kafka delivery can be at-least-once, so duplicate events are possible. Instead of trying to eliminate duplicates completely, we make the business operation idempotent so duplicate events do not create duplicate business effects.

Do not say:

> "At-least-once gives exactly-once."

Instead:

```text
Kafka
→ At-least-once delivery

Application
→ Idempotent business processing
```

---

# 23. Idempotent Kafka Consumer

### Question

**How do you prevent the same Kafka event from executing the business operation twice?**

### Answer

We can use an Inbox/idempotent-consumer pattern.

The consumer persists the event ID or business idempotency key in an Inbox table with a unique constraint.

Example:

```text
inbox_event
------------------------
id
event_id UNIQUE
idempotency_key
session_id
event_type
processed_at
created_at
```

When an event arrives:

```text
Event
  ↓
Check Inbox
  ↓
Already processed?
```

If yes:

```text
Skip business operation
Commit offset
```

If no:

```text
Process operation
Store processing state
Commit offset only after successful processing
```

---

# 24. Event ID vs Idempotency Key vs Partition Key

### Question

**Are these three things the same?**

### Answer

No.

```text
event_id
→ Identifies the message/event

idempotency_key
→ Identifies the logical business operation

partition_key
→ Determines Kafka partitioning and helps maintain ordering for messages with the same key
```

A partition key is **not** a duplicate-detection mechanism.

---

# 25. Inbox Table Design

A conceptual Inbox table can be:

```text
inbox_event
------------------------
id
event_id UNIQUE
idempotency_key
session_id
event_type
status
processed_at
created_at
```

Possible states:

```text
RECEIVED
PROCESSING
PROCESSED
FAILED
```

---

# 26. What If the Consumer Inserts the Inbox Record but the Business Operation Fails?

### Question

**What happens if you insert the event in Inbox and then the business operation fails?**

### Answer

We should not mark the event as successfully processed before the business operation succeeds.

For database-backed business operations, we can use one transaction:

```text
Kafka Event
   ↓
BEGIN TRANSACTION
   ↓
Insert Inbox record
   ↓
Business DB operation
   ↓
Mark Inbox = PROCESSED
   ↓
COMMIT
```

If the business operation fails:

```text
ROLLBACK
```

The Inbox record is not committed as successfully processed, so the event can be retried.

---

# 27. Why Not Commit Kafka Offset Before Business Processing?

### Question

**Why don't you commit the Kafka offset before executing the business operation?**

### Answer

Because if we commit the offset first and then the business operation fails, Kafka considers the message consumed and normally will not redeliver it.

The business operation could therefore be lost.

A safer sequence is:

```text
Consume event
   ↓
Business processing
   ↓
DB transaction succeeds
   ↓
Commit Kafka offset
```

This gives us at-least-once processing.

Important:

> **Kafka offset and database transaction are not one atomic transaction.**

The database can rollback its own changes, but it cannot rollback Kafka automatically.

---

# 28. What If DB Commit Succeeds but Kafka Offset Commit Fails?

### Question

**What happens if the database transaction succeeds but the Kafka offset is not committed?**

### Answer

Kafka may redeliver the same event.

That is okay if the consumer is idempotent.

Flow:

```text
Kafka
  ↓
Consumer
  ↓
Business DB transaction
  ↓
DB COMMIT
  ↓
Application crashes
  ↓
Offset not committed
  ↓
Kafka redelivers event
  ↓
Inbox says PROCESSED
  ↓
Skip business operation
  ↓
Commit offset
```

This is a classic at-least-once + idempotent-consumer design.

---

# 29. Crash Recovery with Inbox

### Question

**How does Inbox help with crash recovery?**

### Answer

Suppose:

```text
Kafka
   ↓
Consumer
   ↓
Business transaction succeeds
   ↓
DB COMMIT
   ↓
Application crashes
   ↓
Kafka offset not committed
```

Kafka delivers the event again.

The consumer checks Inbox:

```text
event_id already exists
status = PROCESSED
```

So it skips the business operation and only commits the offset.

This prevents duplicate business effects.

Key statement:

> **Kafka handles delivery; Inbox handles duplicate business processing.**

---

# 30. Outbox Pattern

### Question

**Where would you use Outbox in this workflow?**

### Answer

Outbox is useful on the producer side when we need to reliably publish an event after a database state change.

For example:

```text
Database transaction
       │
       ├── document_processing update
       │
       └── outbox_event insert
```

Both happen in the same DB transaction.

After commit:

```text
Outbox Publisher
      ↓
Kafka
```

---

# 31. Outbox Table

A simple conceptual table:

```text
outbox_event
------------------------
id
event_type
status
created_at
published_at
```

The key idea is not the exact columns; it is that the event is stored reliably with the business transaction.

---

# 32. Outbox Crash Scenario

```text
DB Transaction
   ├── document_processing = PROCESSING
   └── outbox_event = PENDING
          ↓
       COMMIT
          ↓
   💥 Application Crash
          ↓
Application Recovers
          ↓
Outbox Publisher
          ↓
Kafka
```

The event wasn't lost because it already exists in the database.

---

# 33. What Does Outbox Actually Guarantee?

### Question

**What problem does Outbox solve?**

### Answer

Outbox protects against losing an event between the database transaction and Kafka publishing.

It gives us:

```text
Business DB Update
      +
Outbox Event
      ↓
Same DB Transaction
      ↓
Both commit
OR
Both rollback
```

---

# 34. What Does Outbox NOT Guarantee?

### Question

**Does Outbox guarantee that Azure will be called exactly once?**

### Answer

No.

Outbox provides reliable event persistence/publication.

It does not make an external API call exactly-once.

For external side effects, we still need an idempotency/reconciliation strategy.

---

# 35. Outbox vs Inbox

### Question

**What is the difference between Outbox and Inbox?**

### Answer

```text
OUTBOX
→ Producer side
→ Ensures an event is reliably published

INBOX
→ Consumer side
→ Prevents duplicate processing of the same event
```

Conceptually:

```text
Producer
   ↓
Outbox
   ↓
Kafka
   ↓
Inbox
   ↓
Consumer
```

---

# 36. Crash After Azure Accepts the Request

### Question

**What if Azure successfully accepts the document, but the application crashes before saving the Azure operation ID?**

### Answer

This is an important external-side-effect failure window.

For example:

```text
Consumer
   ↓
Azure
   ↓
202 Accepted
   ↓
💥 Crash
   ↓
azure_operation_id was not persisted
```

Now our database cannot be certain whether the external operation was accepted.

We should not simply assume that the operation failed and blindly submit the document again.

We need an appropriate idempotency or reconciliation mechanism for the external service.

If the Azure operation ID has already been persisted, recovery can continue against that existing operation.

Key point:

> **Outbox and Inbox do not automatically make an external API exactly-once.**

---

# 37. Azure Operation ID

### Question

**Why store the Azure operation ID?**

### Answer

Azure Document Intelligence analysis can be asynchronous.

The initial request can return an operation location identifying the processing operation.

We can persist the corresponding operation identifier so that after a restart or retry, the application can continue tracking the existing Azure operation instead of blindly starting another one.

---

# 38. If the Same Idempotency Key Arrives After a Crash

### Question

**Suppose Request A starts Azure processing and the application crashes. Request B arrives with the same idempotency key. What happens?**

### Answer

We first retrieve the existing processing record.

If the record already contains the Azure operation ID:

```text
same idempotency key
        ↓
existing processing record
        ↓
Azure operation ID exists
        ↓
continue/retrieve existing Azure operation
```

If the operation is complete, return the existing outcome.

If it is still processing, return the current processing state.

We should not blindly submit another operation.

---

# 39. Critical External Call Limitation

### Question

**Does an idempotency key guarantee Azure will only receive one request?**

### Answer

Not automatically.

Our application-level idempotency key protects our own business processing.

An external API call is a separate side effect.

Therefore, we need an appropriate external idempotency or reconciliation mechanism if we need stronger guarantees at that boundary.

---

# 40. The Complete Story 2 Architecture

```text
                         ONBOARDING
                         ORCHESTRATOR
                              │
                     idempotencyKey = K123
                              │
                              ▼
                  IDENTITY VERIFICATION
                              │
                              ▼
                 DOCUMENT PROCESSING
                              │
                 ┌────────────┴────────────┐
                 │                         │
                 ▼                         ▼
      document_processing             outbox_event
      -------------------             ------------
      idempotency_key                 event_type
      status                          status
      retry_count
      version
      azure_operation_id
                 │
                 ▼
               KAFKA
                 │
                 ▼
             CONSUMER
                 │
                 ▼
               INBOX
                 │
        ┌────────┴────────┐
        │                 │
    PROCESSED          NEW EVENT
        │                 │
        ▼                 ▼
   Skip operation      Process
                          │
                          ▼
                       AZURE
                          │
                          ▼
                 Result / Status Update
```

---

# 41. Complete Crash Recovery Picture

```text
Request
   ↓
DB Transaction
   ├── Processing State
   └── Outbox Event
   ↓
COMMIT
   ↓
💥 Crash
   ↓
Outbox survives
   ↓
Kafka
   ↓
Consumer
   ↓
Inbox / Idempotency Check
   ↓
Business Processing
   ↓
Azure
   ↓
Persist Result
   ↓
Commit Kafka Offset
```

If the offset is not committed:

```text
Kafka redelivers
      ↓
Inbox sees PROCESSED
      ↓
Skip business operation
      ↓
Commit offset
```

---

# 42. Most Important Conceptual Distinctions

Remember these rather than memorizing long answers.

```text
Idempotency
→ Duplicate business request

Unique Constraint
→ Database-level duplicate protection

Optimistic Locking
→ Concurrent stale updates

Atomic State Transition
→ Valid state change happens as one DB operation

Kafka
→ Asynchronous event delivery

Offset
→ Consumer progress

Inbox
→ Duplicate event/business processing protection

Outbox
→ Reliable event publication

Retry
→ Recover from transient failure

Azure Operation ID
→ Track an asynchronous external operation

Reconciliation
→ Detect/recover from uncertain external state
```

---

# 43. Most Important Senior-Level Distinction

### Do NOT say:

> "Outbox gives exactly-once."

Say:

> **"Outbox provides reliable event persistence/publication."**

### Do NOT say:

> "Kafka guarantees exactly-once business processing."

Say:

> **"Kafka can operate with at-least-once delivery, so the consumer needs to be idempotent."**

### Do NOT say:

> "Optimistic locking locks the row."

Say:

> **"Optimistic locking detects stale concurrent updates using a version."**

### Do NOT say:

> "Idempotency prevents all concurrent requests."

Say:

> **"Idempotency prevents duplicate processing of the same logical operation."**

### Do NOT say:

> "Offset commit means the business operation is exactly once."

Say:

> **"The offset represents consumer progress; business correctness comes from transactional state management and idempotent processing."**

---

# 44. Final 60–90 Second Story

If the interviewer asks you to explain the problem from beginning to end:

> **"One issue we encountered in the onboarding workflow was duplicate processing of the same business request. For example, the identity verification request could reach the backend more than once because of retries or repeated submission. Without idempotency, the same downstream business operation could be executed multiple times.**
>
> **We addressed this by introducing an idempotency key for the logical operation and persisting it with the processing record, with a database unique constraint as the final duplicate-protection mechanism. If the same request arrives again, we look up the existing record. If the operation is completed, we return the existing result; if it is still processing, we return its current state instead of starting another operation.**
>
> **For concurrent updates to an existing processing record, we use optimistic locking with a version field. For asynchronous processing, Kafka can deliver messages at least once, so we use an Inbox/idempotent-consumer pattern to prevent duplicate business effects. On the producer side, the Outbox pattern ensures that important events are not lost between a database transaction and Kafka publication.**
>
> **This gives us reliable asynchronous processing while keeping the business operations idempotent and safe against retries, duplicate events, and application crashes."**

````

## Story 2 — What You Actually Need to Understand

You do **not** need to memorize all 44 sections.

Understand this one chain:

```text
Duplicate request
      ↓
Idempotency Key
      ↓
Unique DB Constraint
      ↓
Existing Operation State
      ↓
Kafka can redeliver
      ↓
Inbox / Idempotent Consumer
      ↓
DB transaction succeeds
      ↓
Offset commits
      ↓
Crash before offset?
      ↓
Kafka redelivers
      ↓
Inbox says already processed
      ↓
Business logic NOT executed twice
````

And the four most important distinctions:

> **Idempotency → duplicate request**

> **Optimistic locking → concurrent update**

> **Outbox → reliable event publication**

> **Inbox → duplicate event processing**

That is the conceptual backbone of **Story 2**.
