Absolutely. Below is a **single copy-paste-ready Markdown file** covering **Q1–Q15**, including the important cross-questions and the conceptual explanation behind each answer.

I’ve deliberately written it so you can **understand the concept instead of memorising interview scripts**.

````md
# Java + Spring Boot Backend Interview Preparation
## Scenario-Based Questions, Answers & Cross-Questions
### Banking Onboarding Project — Q1 to Q15

---

# Q1 — Concurrent Identity Verification + Idempotency + Database Concurrency

## Scenario

Two requests A and B simultaneously call:

```text
POST /onboarding/101/identity-verification
````

Current state:

```text
ONB-101 → MOBILE_VERIFIED
```

Requirement:

> Identity verification should be started only once.

---

## Question

How would you prevent both requests from executing identity verification concurrently?

## Core Concept

There are actually **two different problems**:

```text
Idempotency
    ↓
Same logical request repeated
    ↓
Don't perform the business operation twice

Concurrency control
    ↓
Different concurrent requests
    ↓
Don't allow conflicting state transitions
```

An idempotency key alone is not enough for every concurrency problem.

---

## Solution

Use an atomic state transition:

```sql
UPDATE onboarding
SET status = 'IDENTITY_IN_PROGRESS'
WHERE id = :id
AND status = 'MOBILE_VERIFIED';
```

Then check affected rows.

```java
int updated = repository.markIdentityInProgress(id);

if (updated == 0) {
    throw new ConflictException(
        "Identity verification already in progress"
    );
}
```

If request A executes first:

```text
affected rows = 1
```

A wins.

Request B then executes:

```text
affected rows = 0
```

because the status is no longer `MOBILE_VERIFIED`.

---

## Why @Transactional Alone Is Not Enough

This is NOT enough:

```java
@Transactional
public void verify(Long id) {
    Onboarding o = repository.findById(id).get();

    if (o.getStatus() == MOBILE_VERIFIED) {
        o.setStatus(IDENTITY_IN_PROGRESS);
    }
}
```

Two requests can both do:

```text
A → READ MOBILE_VERIFIED
B → READ MOBILE_VERIFIED

A → CHECK → true
B → CHECK → true
```

The read and update are separate operations.

Therefore we need an atomic concurrency guard.

---

## Pessimistic Lock

Another approach:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Onboarding> findById(Long id);
```

This can translate conceptually to:

```sql
SELECT *
FROM onboarding
WHERE id = ?
FOR UPDATE;
```

The second transaction waits for the first transaction's lock.

However, do NOT hold this DB lock while calling a slow external service.

Bad:

```text
DB transaction starts
    ↓
SELECT FOR UPDATE
    ↓
External Identity Service (5 sec)
    ↓
DB update
    ↓
Commit
```

Better:

```text
SHORT DB TX
    ↓
MOBILE_VERIFIED → IDENTITY_IN_PROGRESS
    ↓
COMMIT

External Identity Service
    ↓

SHORT DB TX
    ↓
IDENTITY_VERIFIED
    ↓
COMMIT
```

---

## Optimistic Locking

JPA can use:

```java
@Version
private Long version;
```

If A and B both read:

```text
version = 5
```

A updates:

```text
version 5 → 6
```

B later tries to update using version 5.

Hibernate detects that the row has changed and throws an optimistic locking exception.

---

## Unique Constraint vs Optimistic Locking

These solve different problems.

### Unique Constraint

```sql
UNIQUE(idempotency_key)
```

Prevents duplicate records with the same logical operation key.

### @Version

Detects concurrent modification of an existing entity.

```text
Unique constraint
→ Duplicate operation protection

@Version
→ Concurrent update detection
```

---

# Q1 Cross-Question — Why Not Hold Pessimistic Lock During External API?

## Question

Identity Service takes 5 seconds. Should we keep:

```text
SELECT FOR UPDATE
```

open for 5 seconds?

## Answer

No.

A long DB transaction can:

* hold DB connections
* hold row locks
* increase lock contention
* increase transaction duration
* reduce throughput
* increase risk of deadlocks/timeouts

Instead:

```text
TX-1:
MOBILE_VERIFIED
→ IDENTITY_IN_PROGRESS
→ COMMIT

External API

TX-2:
IDENTITY_VERIFIED
→ COMMIT
```

If the application crashes while `IDENTITY_IN_PROGRESS`, use retry/reconciliation/outbox depending on architecture.

---

# Q2 — @Transactional + Self Invocation

## Scenario

```java
@Transactional
public void completeOnboarding(Long id) {

    updateOnboardingStatus(id);

    identityService.verify(id);

    saveAuditLog(id);
}

@Transactional
public void updateOnboardingStatus(Long id) {
    ...
}

@Transactional
public void saveAuditLog(Long id) {
    ...
}
```

Identity Service throws a RuntimeException.

---

## Question

Will the database update roll back?

## Answer

Yes, if `completeOnboarding()` is executed through Spring's transactional proxy and the RuntimeException propagates out of the method.

The whole operation participates in the same transaction:

```text
TX-1
 |
 +-- update status
 |
 +-- identity verification
 |
 +-- audit
 |
 RuntimeException
 |
 ROLLBACK
```

---

## Self Invocation Trap

If:

```java
this.updateOnboardingStatus(id);
```

is called from the same class, Spring's transactional proxy is bypassed.

Why?

Spring's normal `@Transactional` behaviour is proxy-based.

Conceptually:

```text
External caller
     ↓
Spring Proxy
     ↓
@Transactional method
```

But:

```text
same object
     ↓
this.method()
```

does not go through the proxy again.

Therefore the inner `@Transactional` annotation does not independently create/apply another transaction.

The method still participates in the existing outer transaction.

---

# Q2 Cross-Question — Transaction Around External API

## Question

What is wrong with:

```java
@Transactional
public void completeOnboarding(Long id) {

    updateStatus(id);

    identityService.verify(id); // 5 seconds

    saveResult(id);
}
```

## Answer

The transaction remains open during the external call.

This can:

* hold DB connection/resources
* increase transaction duration
* increase contention
* reduce connection pool availability
* increase timeout/deadlock risk

Better:

```java
public void completeOnboarding(Long id) {

    stateService.markInProgress(id);

    IdentityResponse response =
        identityService.verify(id);

    stateService.updateResult(id, response);
}
```

Where:

```text
markInProgress()
→ short transaction

external call
→ outside DB transaction

updateResult()
→ short transaction
```

Prefer separate Spring beans if transactional proxy behaviour is required.

---

# Q3 — Constructor Injection + Circular Dependency

## Question

Why is constructor injection preferred?

## Answer

Constructor injection makes dependencies explicit.

```java
@Service
public class OnboardingService {

    private final IdentityService identityService;

    public OnboardingService(
            IdentityService identityService) {
        this.identityService = identityService;
    }
}
```

Advantages:

* dependencies are visible
* supports immutable references
* easier unit testing
* object cannot be created without required dependencies
* works naturally with `final`

---

## Circular Dependency

Suppose:

```text
OnboardingService
      ↓
IdentityService
      ↓
OnboardingService
```

Spring tries:

```text
Create OnboardingService
    ↓
Needs IdentityService
    ↓
Create IdentityService
    ↓
Needs OnboardingService
    ↓
Cycle
```

With constructor injection, this dependency cycle becomes explicit and normally prevents successful bean creation.

---

## Correct Design

If OnboardingService is the workflow orchestrator:

```text
OnboardingService
        ↓
IdentityService
```

IdentityService should normally not call back into OnboardingService.

If both genuinely need common functionality:

```text
OnboardingService ──┐
                    ↓
             CommonService
                    ↑
                    │
IdentityService ────┘
```

The solution is to fix the responsibility/design rather than simply hiding the cycle.

---

# Q4 — REQUIRED vs REQUIRES_NEW

## Scenario

```java
@Transactional
public void completeOnboarding(Long id) {

    onboardingRepository.updateStatus(id, "ACTIVE");

    auditService.saveAudit(id);

    throw new RuntimeException();
}
```

Audit:

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAudit(Long id) {
    auditRepository.save(...);
}
```

---

## What Happens?

Outer transaction:

```text
TX-1
```

Audit starts:

```text
TX-1 suspended
       ↓
TX-2 starts
       ↓
Audit saved
       ↓
TX-2 COMMIT
       ↓
TX-1 resumes
```

Then outer method throws RuntimeException:

```text
TX-1 → ROLLBACK
```

Final state:

```text
Onboarding update → ROLLBACK
Audit             → COMMITTED
```

---

## REQUIRED

Default propagation:

```java
@Transactional
```

means:

```text
If transaction exists:
    join it

Otherwise:
    create one
```

So:

```text
TX-1
 |
 +-- onboarding update
 |
 +-- audit
 |
 RuntimeException
 |
 ROLLBACK BOTH
```

---

## REQUIRES_NEW

Means:

```text
Suspend existing transaction
Create completely new transaction
```

Mental model:

```text
REQUIRED
→ "Existing transaction hai to join karo."

REQUIRES_NEW
→ "Old transaction ko suspend karo, naya transaction start karo."
```

---

# Q5 — Global Exception Handling

## Question

How do you avoid writing try/catch in every controller?

## Answer

Use:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdentityVerificationException.class)
    public ResponseEntity<ErrorResponse> handleIdentityError(...) {
        ...
    }
}
```

This centralizes exception-to-HTTP-response mapping.

---

## Common HTTP Status Mapping

### 400 Bad Request

Invalid client input.

```text
Malformed JSON
Invalid field
Missing required data
```

### 404 Not Found

Resource does not exist.

```text
ONB-101 not found
```

### 409 Conflict

Request conflicts with current resource state.

```text
Identity verification already in progress
```

### 503 Service Unavailable

External dependency temporarily unavailable.

```text
Identity Service unavailable
```

### 500 Internal Server Error

Unexpected server-side error.

```text
Unexpected application bug
```

Do not simply convert every exception to 500.

---

# Q6 — @Async + @Transactional

## Scenario

```java
@Transactional
public void verifyIdentity(Long id) {

    onboardingRepository.updateStatus(
        id,
        IN_PROGRESS
    );

    identityService.verifyAsync(id);
}
```

```java
@Async
public void verifyAsync(Long id) {

    IdentityResponse response =
        externalIdentityClient.verify(id);

    onboardingRepository.updateStatus(
        id,
        VERIFIED
    );
}
```

---

## Does the outer transaction propagate into @Async?

No.

Why?

`@Async` runs the method on another thread.

Spring transaction context is normally thread-bound.

```text
Main Thread
    |
    TX-1
    |
    +---- @Async ----> Async Thread
                           |
                           no TX-1
```

If the async method itself has:

```java
@Async
@Transactional
```

then it can create a NEW transaction on the async thread.

```text
Main Thread
TX-1
 ↓
IN_PROGRESS
 ↓
COMMIT

Async Thread
TX-2
 ↓
External API
 ↓
VERIFIED
 ↓
COMMIT
```

---

## Important Point

If async processing fails:

```text
TX-2 fails
```

it cannot roll back a TX-1 that has already committed.

---

## @Async Self Invocation

This can be problematic:

```java
this.verifyAsync(id);
```

because the call bypasses Spring's async proxy.

Prefer invoking the async method through another Spring-managed bean/proxy.

---

# Q7 — HikariCP Connection Pool

## Scenario

```text
100 concurrent requests
Hikari max pool size = 10
```

## What happens?

Only up to 10 DB connections can actively execute DB work concurrently.

The other requests that need DB connections wait for an available connection.

If they wait longer than:

```properties
spring.datasource.hikari.connection-timeout
```

connection acquisition can fail.

---

## Important Correction

HikariCP is a:

```text
Connection Pool
```

not a request-processing queue.

Don't say:

> "It processes exactly 10 requests at a time."

Instead say:

> "Only 10 connections can be actively used for DB operations at a time; other requests needing connections may wait."

---

## Should We Increase Pool Size to 100?

Not automatically.

Database capacity matters.

Too many connections can cause:

* DB CPU contention
* lock contention
* context switching
* memory usage
* worse throughput

Pool sizing depends on:

* DB capacity
* query latency
* transaction duration
* CPU/I/O
* number of application instances
* production metrics

Important distributed point:

```text
5 application instances
×
pool size 20
=
up to 100 DB connections
```

Pool size is per application instance.

---

# Q8 — JPA/Hibernate N+1 Query Problem

## Scenario

Fetch 100 onboardings:

```java
List<Onboarding> onboardings =
    repository.findByStatus(PENDING);
```

Then:

```java
for (Onboarding o : onboardings) {
    o.getCustomer().getName();
    o.getDocuments().size();
}
```

---

## What is N+1?

Initial query:

```sql
SELECT *
FROM onboarding
WHERE status = 'PENDING';
```

That's 1 query.

Then accessing relationships may trigger:

```text
100 customer queries
+
100 document queries
```

Potentially:

```text
1 + 100 + 100 = 201 queries
```

Exact count depends on mappings, cache and persistence context.

---

## Does LAZY solve N+1?

No.

This is a very common mistake.

`LAZY` means:

> Load the relationship when it is accessed.

If you access it in a loop:

```text
LAZY
 ↓
Loop
 ↓
100 accesses
 ↓
100 additional queries
```

LAZY can therefore expose the N+1 problem.

---

## EAGER Does Not Automatically Solve It

EAGER means the relationship should be loaded eagerly, but it does not guarantee an efficient query plan.

It can cause excessive data loading.

---

## Solutions

### Fetch Join

```java
@Query("""
    SELECT o
    FROM Onboarding o
    JOIN FETCH o.customer
    WHERE o.status = :status
""")
```

Good when the use case needs the full entity + relationship.

Be careful with large collections and multiple collection fetch joins because result rows can multiply.

---

### EntityGraph

```java
@EntityGraph(attributePaths = {"customer"})
List<Onboarding> findByStatus(Status status);
```

Useful when you want to specify which associations should be fetched for a particular repository operation.

---

### DTO Projection

If you only need:

```text
id
customerName
status
createdAt
```

don't load the entire entity graph.

Use a DTO:

```java
public record OnboardingSummary(
    Long id,
    String customerName,
    Status status,
    LocalDateTime createdAt
) {}
```

Concept:

```text
Need full entity + relation
→ Fetch Join / EntityGraph

Need selected fields
→ DTO Projection
```

---

# Q9 — Pagination on 10 Million Records

## Scenario

```text
10 million onboarding records

GET /onboardings?page=50000&size=20
```

Using:

```java
Page<Onboarding> findByStatus(
    Status status,
    Pageable pageable
);
```

---

## Problem With Offset Pagination

Conceptually:

```sql
SELECT *
FROM onboarding
WHERE status = 'PENDING'
ORDER BY created_at DESC
LIMIT 20
OFFSET 999980;
```

Deep offsets can become expensive because the DB may need to scan/process many preceding rows.

---

## Keyset / Cursor Pagination

Instead of:

```text
page = 50000
```

send the last item from the previous page:

```text
lastCreatedAt
lastId
```

Query:

```sql
SELECT *
FROM onboarding
WHERE status = 'PENDING'
AND (
    created_at < :lastCreatedAt
    OR (
        created_at = :lastCreatedAt
        AND id < :lastId
    )
)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

---

## Why Include ID?

`created_at` may not be unique.

So:

```text
created_at + id
```

provides a deterministic ordering.

---

## Index

Potential index:

```sql
CREATE INDEX idx_onboarding_status_created_id
ON onboarding (
    status,
    created_at DESC,
    id DESC
);
```

Always validate with:

```sql
EXPLAIN ANALYZE
```

---

## Offset vs Keyset

```text
Offset
→ Simple
→ Random page jumps possible
→ Deep pages can become expensive

Keyset
→ Efficient for sequential navigation
→ Excellent for large datasets
→ Doesn't naturally support "jump to page 50,000"
```

---

# Q10 — REST API Idempotency

## Scenario

Frontend calls:

```http
POST /onboardings/101/identity-verification
Idempotency-Key: ABC123
```

The request times out.

Frontend retries with:

```http
Idempotency-Key: ABC123
```

---

## Goal

The second request should not create a second business effect.

---

## Basic Design

Store:

```text
idempotency_key
status
response
```

with:

```sql
UNIQUE(idempotency_key)
```

Flow:

```text
First request
    ↓
ABC123 doesn't exist
    ↓
Process operation
    ↓
Save result for ABC123
```

Retry:

```text
ABC123 exists
    ↓
Return previous result
    ↓
Don't execute operation again
```

---

## Important Crash Window

Suppose:

```text
Request A
 ↓
External Identity Service succeeds
 ↓
Local DB updated
 ↓
Application crashes
 ↓
Idempotency record NOT saved
```

Retry B:

```text
Check idempotency table
 ↓
ABC123 doesn't exist
 ↓
Potentially calls Identity Service again
```

Therefore local idempotency storage alone is not always enough.

---

## Better Design

Use the same stable operation ID with the external service if it supports idempotency:

```text
ONB-101-IDENTITY
```

Then:

```text
First call
→ verification executed

Retry
→ external service recognizes same operation
→ returns previous result
```

If external service doesn't support idempotency:

```text
Status lookup
+
Reconciliation
```

may be needed.

---

## Important Distinction

```text
Idempotency
→ Same logical operation repeated safely

Retry
→ Try again after transient failure

Outbox
→ Durable record of operation/event intent

Reconciliation
→ Detect and repair inconsistent state
```

---

# Q11 — External API Success + Application Crash

## Scenario

```java
@Transactional
public void completeIdentityVerification(Long id) {

    updateStatus(id, IN_PROGRESS);

    IdentityResponse response =
        identityClient.verify(id);

    updateStatus(id, VERIFIED);
}
```

External Identity Service succeeds, but application crashes before:

```text
IN_PROGRESS → VERIFIED
```

---

## Problem

Now:

```text
Local DB
→ IN_PROGRESS

External service
→ VERIFIED
```

This is a distributed consistency problem.

---

## Why @Retryable Alone Is Not Enough

`@Retryable` can retry transient failures.

But if the application crashes permanently between operations, an in-memory retry mechanism cannot guarantee durable recovery.

---

## Outbox

Use:

```text
DB Transaction
    |
    +-- status = IN_PROGRESS
    |
    +-- outbox = IDENTITY_VERIFICATION_REQUIRED
    |
    COMMIT
```

Then worker:

```text
Outbox
 ↓
Identity Service
 ↓
Result
 ↓
Update local DB
```

---

## Crash Scenario

Worker calls external service:

```text
External → SUCCESS
```

Then worker crashes before marking outbox complete.

Outbox still looks pending.

Worker retries.

Therefore external service should ideally support the same idempotency key:

```text
ONB-101-IDENTITY
```

Retry:

```text
Same operation ID
→ external service returns existing result
→ no duplicate business effect
```

---

## Key Mental Model

```text
Outbox
→ durable intent

Idempotency
→ prevents duplicate effect

Retry
→ handles transient failures

Reconciliation
→ repairs unknown/inconsistent states
```

---

# Q12 — Optimistic Locking with @Version

## Scenario

Database:

```text
id = 101
status = MOBILE_VERIFIED
version = 5
```

A and B both read version 5.

A updates:

```text
version 5 → 6
```

B then tries to update.

---

## What Happens?

Hibernate effectively generates something like:

```sql
UPDATE onboarding
SET remarks = ?,
    version = 6
WHERE id = 101
AND version = 5;
```

But DB currently has:

```text
version = 6
```

Therefore:

```text
affected rows = 0
```

Hibernate detects that the entity is stale.

Typical exceptions include:

```text
OptimisticLockException
```

and through Spring's exception translation:

```text
ObjectOptimisticLockingFailureException
```

---

## What If @Version Is Removed?

Hibernate no longer has the version-based concurrency check.

B could update the row even though it loaded stale data.

This can cause a lost update.

---

## 409 or 500?

Optimistic concurrency conflicts can be represented as:

```http
409 Conflict
```

because the request conflicts with the current resource state.

500 generally represents an unexpected server-side failure.

---

## Bonus Trap

Is @Version enough to prevent duplicate external calls?

No.

Example:

```text
A → call Identity Service → SUCCESS
B → call Identity Service → SUCCESS

A → DB update succeeds
B → DB update fails due to @Version
```

`@Version` protected the DB update.

It did NOT prevent B from already calling the external service.

Therefore:

```text
@Version
→ DB concurrency

Idempotency
→ External business operation duplication
```

---

# Q13 — Checked Exception + @Transactional

## Scenario

```java
@Transactional
public void approveOnboarding(Long id) throws Exception {

    updateStatus(id);

    saveAudit(id);

    sendNotification(id);

    throw new Exception("Failure");
}
```

The final exception is a checked `Exception`.

---

## Does Spring Roll Back?

By default:

```text
RuntimeException
→ Rollback

Error
→ Rollback

Checked Exception
→ Usually NO automatic rollback
```

Therefore the DB changes normally remain committed if the checked exception escapes and no other rollback rule applies.

---

## How To Roll Back Checked Exceptions?

Use:

```java
@Transactional(
    rollbackFor = Exception.class
)
```

Now:

```text
Checked Exception
       ↓
rollbackFor matches
       ↓
ROLLBACK
```

---

## Important Correction

`rollbackFor` is NOT propagation.

These are separate concepts.

### Propagation

```text
REQUIRED
REQUIRES_NEW
NESTED
```

Controls transaction participation/creation.

### rollbackFor

Controls which exceptions trigger rollback.

---

## External Notification Trap

Suppose:

```text
DB status updated
 ↓
Audit saved
 ↓
External notification SUCCESS
 ↓
Checked exception
 ↓
DB rollback
```

Final state can be:

```text
Database
→ Rolled back

External notification
→ Already sent
```

Spring cannot automatically undo an external HTTP call.

Therefore:

```text
@Transactional
≠
Rollback everything in the distributed system
```

It mainly provides transactional atomicity for resources participating in that transaction.

---

# Q14 — Spring Singleton + Thread Safety

## Scenario

```java
@Service
public class OnboardingService {

    private String currentOnboardingId;

    public void process(String onboardingId) {

        currentOnboardingId = onboardingId;

        // processing

        System.out.println(
            currentOnboardingId
        );
    }
}
```

Concurrent requests:

```text
A → ONB-101
B → ONB-202
C → ONB-303
```

---

## Default Scope

`@Service` is singleton-scoped by default.

Therefore:

```text
ONE Spring bean instance
```

is normally shared by multiple request threads.

---

## Problem

Request A:

```text
currentOnboardingId = ONB-101
```

Request B:

```text
currentOnboardingId = ONB-202
```

Now A resumes and reads:

```text
currentOnboardingId
```

It may get:

```text
ONB-202
```

This is a race condition caused by **shared mutable instance state**.

---

## Important Interview Correction

Don't say:

> "Spring singleton services are not thread-safe."

Better:

> "Spring singleton beans can be accessed concurrently, so they should generally be designed to be stateless. The singleton scope itself is not the problem; mutable shared instance state is."

---

## Correct Design

Keep request-specific state in method parameters/local variables:

```java
@Service
public class OnboardingService {

    public void process(String onboardingId) {

        String currentId = onboardingId;

        // processing
    }
}
```

Now:

```text
Thread A
local id → ONB-101

Thread B
local id → ONB-202
```

Each invocation has its own local state.

---

## Does final Solve It?

Not necessarily.

This:

```java
private final String currentOnboardingId;
```

only means the reference cannot be reassigned after initialization.

It doesn't make an object thread-safe.

Example:

```java
private final List<String> ids =
    new ArrayList<>();
```

The reference is final, but:

```java
ids.add(...)
```

is still mutable and `ArrayList` is not thread-safe.

Therefore:

```text
final
→ reference cannot be reassigned

final
≠
object automatically immutable

final
≠
object automatically thread-safe
```

---

# Q14 Cross-Question — Stateless Service But Same DB Row

## Scenario

10 concurrent requests all operate on:

```text
ONB-101
```

All attempt:

```text
MOBILE_VERIFIED
→ IDENTITY_IN_PROGRESS
```

---

## Important Clarification

Different HTTP requests do not necessarily represent different business operations.

These 10 requests can be:

```text
Same business operation × 10 concurrent attempts
```

Goal:

```text
10 requests
     ↓
Only ONE successfully transitions state
```

---

## Don't Simply Do This

```java
Onboarding o = repository.findById(id);

if (o.getStatus() == MOBILE_VERIFIED) {
    o.setStatus(IN_PROGRESS);
}
```

Why?

Because:

```text
R1 → READ → MOBILE_VERIFIED
R2 → READ → MOBILE_VERIFIED
R3 → READ → MOBILE_VERIFIED
...
R10 → READ → MOBILE_VERIFIED
```

All may believe they are allowed to proceed.

---

## Better: Atomic Conditional Update

```sql
UPDATE onboarding
SET status = 'IDENTITY_IN_PROGRESS'
WHERE id = 101
AND status = 'MOBILE_VERIFIED';
```

Results:

```text
R1 → affected rows = 1
R2 → affected rows = 0
R3 → affected rows = 0
...
R10 → affected rows = 0
```

Java:

```java
int updated = repository.markIdentityInProgress(id);

if (updated == 0) {
    throw new ConflictException(
        "Identity verification already started"
    );
}
```

---

## Java vs Database Responsibility

### Java/Spring

```text
Receive request
    ↓
Validate
    ↓
Call service
    ↓
Request atomic state transition
    ↓
Check affected rows
    ↓
1 → continue
0 → conflict
```

### Database

```text
Receive UPDATE
    ↓
Check ID + current status
    ↓
Perform atomic update
    ↓
Return affected row count
```

---

## Why Database-Level Atomicity?

Because this:

```text
READ
 ↓
CHECK
 ↓
UPDATE
```

is vulnerable to race conditions.

Whereas:

```text
UPDATE ... WHERE current_state = expected_state
```

makes the state check and modification one atomic DB operation.

---

## Important Layers

```text
Application concurrency
→ Multiple request threads

Database concurrency
→ Multiple threads modifying shared DB state

Business duplication
→ Same operation attempted multiple times

Distributed failure
→ Local DB and external service disagree
```

Each layer may need a different mechanism.

---

# Q15 — JPA Bulk Update + Persistence Context

## Scenario

Database:

```text
ONB-101 → PENDING
ONB-102 → PENDING
ONB-103 → PENDING
```

First:

```java
List<Onboarding> onboardings =
    repository.findByStatus(PENDING);
```

Then:

```java
repository.bulkUpdateStatus(
    PENDING,
    PROCESSING
);
```

Bulk query:

```java
@Modifying
@Query("""
    UPDATE Onboarding o
    SET o.status = :newStatus
    WHERE o.status = :oldStatus
""")
```

---

## What Does the Loop Print?

Potentially:

```text
PENDING
PENDING
PENDING
```

even though the DB now contains:

```text
PROCESSING
PROCESSING
PROCESSING
```

---

# Why?

When the initial query executes:

```text
Database
    ↓
SELECT
    ↓
Hibernate
    ↓
Persistence Context
    ↓
Java entities
```

The persistence context contains:

```text
ONB-101 → PENDING
ONB-102 → PENDING
ONB-103 → PENDING
```

Then the bulk update executes directly against the database:

```text
JPQL bulk UPDATE
      ↓
SQL UPDATE
      ↓
Database
```

The database changes:

```text
ONB-101 → PROCESSING
ONB-102 → PROCESSING
ONB-103 → PROCESSING
```

But Hibernate's already-managed Java entities can still contain:

```text
ONB-101 → PENDING
ONB-102 → PENDING
ONB-103 → PENDING
```

Therefore the persistence context is stale.

---

# What Is Persistence Context?

The persistence context is the set of managed entity instances associated with the current EntityManager/session.

Conceptually:

```text
@Transactional
     ↓
EntityManager
     ↓
Persistence Context
     ↓
Managed Entities
```

It acts as Hibernate's **first-level cache**.

---

# Why Bulk Update Causes the Problem

Normal entity update:

```java
entity.setStatus(PROCESSING);
```

Hibernate tracks the managed entity and later flushes its changes.

Conceptually:

```text
Java Entity
    ↓
Persistence Context
    ↓
flush
    ↓
Database
```

Bulk update:

```text
JPQL UPDATE
    ↓
Database directly
```

It doesn't synchronize every already-managed Java object.

Therefore:

```text
Database = PROCESSING
Java object = PENDING
```

can temporarily coexist.

---

# How To Fix It

## Option 1 — clear()

```java
entityManager.clear();
```

This clears managed entities from the persistence context.

Then a new query fetches fresh state.

```text
bulk update
    ↓
DB updated
    ↓
clear()
    ↓
Persistence Context emptied
    ↓
new query
    ↓
fresh DB state
```

---

## Option 2 — clearAutomatically

Spring Data:

```java
@Modifying(clearAutomatically = true)
@Query("""
    UPDATE Onboarding o
    SET o.status = :newStatus
    WHERE o.status = :oldStatus
""")
```

The persistence context is cleared automatically after the modifying query.

---

## Option 3 — refresh()

For a specific entity:

```java
entityManager.refresh(onboarding);
```

This reloads the entity from the database.

```text
Java = PENDING

refresh()

Database = PROCESSING

       ↓

Java = PROCESSING
```

---

# flush() vs clear() vs refresh()

## flush()

```java
entityManager.flush();
```

Means:

```text
Persistence Context
       ↓
Synchronize pending changes
       ↓
Database
```

Think:

> Java → DB

---

## clear()

```java
entityManager.clear();
```

Means:

```text
Remove managed entities
from persistence context
```

Think:

> Forget managed Java entities.

---

## refresh()

```java
entityManager.refresh(entity);
```

Means:

```text
Database
   ↓
Reload
   ↓
Specific Java entity
```

Think:

> DB → Java

---

# Important Memory Trick

```text
flush()
→ Java → DB

clear()
→ Remove Java entities from Persistence Context

refresh()
→ DB → Java
```

---

# Why Bulk Updates Are Dangerous

Suppose:

```text
Java entity:
status = PENDING

Database:
status = PROCESSING
```

Later the stale Java entity is modified and flushed.

This can create confusing or unexpected state because the persistence context does not automatically know that the bulk update changed the database behind its back.

Therefore:

> Be careful when mixing bulk JPQL updates with already-managed entities.

---

# FINAL INTERVIEW CHEAT SHEET

## Concurrency

```text
Idempotency
→ Same logical operation repeated safely

Optimistic Locking
→ Detect concurrent DB modification

Pessimistic Locking
→ Lock DB row while transaction operates

Atomic UPDATE
→ Check state + update in one DB operation

Unique Constraint
→ Prevent duplicate DB records
```

---

## Transactions

```text
@Transactional
→ Defines transaction boundary

REQUIRED
→ Join existing transaction or create one

REQUIRES_NEW
→ Suspend old transaction and create new one

rollbackFor
→ Define exceptions that trigger rollback

RuntimeException
→ Rollback by default

Checked Exception
→ Usually no rollback by default
```

---

## Async

```text
@Async
→ Different thread

Transaction context
→ Thread-bound

@Async does NOT automatically inherit caller transaction

@Async + @Transactional
→ Async method can have its own transaction
```

---

## JPA/Hibernate

```text
Persistence Context
→ Managed entities for current EntityManager

First-Level Cache
→ Persistence-context-level cache

LAZY
→ Load association when accessed

EAGER
→ Load association eagerly

N+1
→ 1 parent query + N child queries

Fetch Join
→ Fetch required relationship in query

EntityGraph
→ Define fetch plan

DTO Projection
→ Fetch only required fields
```

---

## Pagination

```text
Offset
→ LIMIT + OFFSET
→ Simple
→ Deep pages can become expensive

Keyset
→ Use last seen ordering values
→ Efficient for large datasets
→ Great for sequential navigation
```

---

## Distributed Operations

```text
Outbox
→ Durable operation/event intent

Idempotency
→ Prevent duplicate business effect

Retry
→ Recover from transient failures

Reconciliation
→ Repair inconsistent local/external state
```

---

## Spring Singleton Thread Safety

```text
@Service
→ Singleton by default

Singleton
≠
Not thread-safe

Problem:
Shared mutable instance state

Good:
Stateless service
+
local variables
+
final dependencies

final
≠
automatically thread-safe
```

---

# MASTER MENTAL MODEL

When an interviewer gives you a tricky production scenario, don't immediately jump to a technology.

First ask:

```text
1. What exactly is being duplicated?
        ↓
2. Is the problem in Java memory?
        ↓
3. Is the problem in database state?
        ↓
4. Is there an external side effect?
        ↓
5. What happens if the application crashes?
        ↓
6. What happens if the request is retried?
        ↓
7. What happens if two requests execute concurrently?
        ↓
8. Where should the transaction start and end?
```

Then choose the mechanism:

```text
Java shared state
→ Stateless design / concurrency primitives

Concurrent DB update
→ @Version / atomic UPDATE / pessimistic lock

Duplicate request
→ Idempotency

Duplicate DB record
→ Unique constraint

Long external API
→ Don't hold DB transaction unnecessarily

Durable async operation
→ Outbox

Transient failure
→ Retry

Unknown distributed state
→ Reconciliation

External duplicate side effect
→ External idempotency / stable operation ID
```

## Most Important Principle

> **Don't use one mechanism to solve every problem.**
>
> `@Transactional`, `@Version`, idempotency, locks, outbox, retry and reconciliation solve different failure/concurrency problems.

```text
                REQUEST
                   │
                   ▼
          Spring Boot Service
                   │
          Stateless processing
                   │
                   ▼
          Atomic DB transition
                   │
                   ▼
           External Service
                   │
                   ▼
              Final State
```

And around that flow:

```text
Concurrency
→ Locking / @Version / atomic update

Duplicate request
→ Idempotency

Crash recovery
→ Outbox / reconciliation

Transient failure
→ Retry

Database consistency
→ Transaction / constraints
```

```
```
