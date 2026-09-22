Absolutely. This version is better for revision because it is concept-first, not "memorise this paragraph and say it."

I’ve structured every question as:

Scenario → What interviewer is testing → Think step-by-step → Answer → Follow-up traps → Concept to remember

You can copy the entire block directly into your .md file.

# Java + Spring Boot Backend Interview Revision
## Scenario-Based Q&A — Q1 to Q15

> Goal: Understand the concept and reasoning instead of memorising interview answers.

---

# Q1. Concurrent Requests + Idempotency + Database Concurrency

## Scenario

Two requests simultaneously call:

```text
POST /onboardings/101/identity-verification

Current state:

ONB-101
Status = MOBILE_VERIFIED

Requirement:

> Identity verification should be started only once.




---

What is the interviewer testing?

Three different concepts:

1. Idempotency


2. Concurrency control


3. Database state transition



These are related but NOT the same thing.


---

First understand the problem

Suppose:

Request A → ONB-101
Request B → ONB-101

Both execute:

Onboarding o = repository.findById(101L).get();

if (o.getStatus() == MOBILE_VERIFIED) {
    o.setStatus(IDENTITY_IN_PROGRESS);
}

Possible execution:

A → READ → MOBILE_VERIFIED
B → READ → MOBILE_VERIFIED

A → CHECK → allowed
B → CHECK → allowed

Both requests believe they are allowed to proceed.

This is a race condition.


---

Why @Transactional alone is not enough

@Transactional gives us a transaction boundary, but it does not automatically mean:

> "Only one request can execute this code."



Two transactions can still read the same old value.

Therefore:

@Transactional
≠
Automatic concurrency control


---

Solution: Atomic state transition

Instead of:

READ
↓
CHECK
↓
UPDATE

make the DB perform:

UPDATE onboarding
SET status = 'IDENTITY_IN_PROGRESS'
WHERE id = :id
AND status = 'MOBILE_VERIFIED';

Then check affected rows.

int updated = repository.markIdentityInProgress(id);

if (updated == 0) {
    throw new ConflictException(
        "Identity verification already in progress"
    );
}

Result:

Request A → affected rows = 1 → WINNER
Request B → affected rows = 0 → CONFLICT


---

Why does this work?

Because the DB performs the state check and update atomically.

Initially:

ONB-101 → MOBILE_VERIFIED

A executes:

UPDATE ...
WHERE status = 'MOBILE_VERIFIED'

It succeeds:

affected rows = 1

ONB-101 → IDENTITY_IN_PROGRESS

B executes the same operation:

UPDATE ...
WHERE status = 'MOBILE_VERIFIED'

But the current status is already:

IDENTITY_IN_PROGRESS

Therefore:

affected rows = 0


---

Where does idempotency fit?

Idempotency answers a slightly different question:

> "What happens if the same logical operation is submitted again?"



For example:

Idempotency-Key: ABC123

If the frontend times out and retries:

Request 1 → ABC123
Request 2 → ABC123

the server should not create a duplicate business effect.

So:

Concurrency
→ Prevent conflicting simultaneous state changes

Idempotency
→ Make repeated logical requests safe


---

Follow-up: Can we use @Version?

Yes.

@Version
private Long version;

Suppose:

A → version 5
B → version 5

A updates:

5 → 6

B later tries to update using version 5.

Hibernate effectively does:

UPDATE onboarding
SET ...
WHERE id = 101
AND version = 5;

No row matches.

Hibernate detects stale data and throws an optimistic locking exception.


---

Follow-up: Can we use pessimistic locking?

Yes.

Conceptually:

SELECT *
FROM onboarding
WHERE id = 101
FOR UPDATE;

This locks the row until the transaction completes.

But do NOT keep the DB lock while making a slow external API call.

Bad:

BEGIN TX
↓
SELECT FOR UPDATE
↓
Identity Service (5 seconds)
↓
UPDATE
↓
COMMIT

Better:

TX-1
↓
MOBILE_VERIFIED → IN_PROGRESS
↓
COMMIT

External Identity Service

TX-2
↓
IN_PROGRESS → VERIFIED
↓
COMMIT


---

Concept to remember

Idempotency
→ Same logical operation repeated safely

Optimistic locking
→ Detect concurrent DB modifications

Pessimistic locking
→ Prevent concurrent modification by locking

Atomic UPDATE
→ Check state + update in one DB operation


---

Q2. @Transactional + Self Invocation

Scenario

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

Identity Service throws RuntimeException.


---

Question

Will the database changes roll back?

Answer

Yes, assuming completeOnboarding() is called through Spring's transactional proxy and the exception propagates.

The outer method starts one transaction:

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

The inner methods normally participate in the same transaction because the default propagation is REQUIRED.


---

What is self-invocation?

Suppose:

this.updateOnboardingStatus(id);

is called from the same class.

Spring's proxy is bypassed.

Spring's normal transaction mechanism works conceptually like:

Caller
 ↓
Spring Proxy
 ↓
@Transactional method

But:

Same object
 ↓
this.method()

does not pass through the proxy again.

Therefore the inner @Transactional annotation does not independently create/apply another transaction.

It still executes inside the existing outer transaction.


---

Follow-up: Why is this important?

Because developers sometimes expect:

@Transactional
methodA()

@Transactional
methodB()

to automatically mean:

TX-A
TX-B

That's not true.

The transaction behaviour depends on propagation and how the method is invoked.


---

Follow-up: What if methodB is in another Spring bean?

Example:

@Service
class OnboardingService {
    private final AuditService auditService;

    public void complete() {
        auditService.saveAudit();
    }
}

Now the call can go through Spring's proxy for AuditService.

Therefore its transaction propagation rules can be applied.


---

Concept to remember

@Transactional
→ Transaction boundary

Propagation
→ What happens when another transactional method is called?

Self invocation
→ Bypasses Spring proxy

External bean invocation
→ Can go through Spring proxy


---

Q3. Constructor Injection + Circular Dependency

Question

Why is constructor injection preferred?

Example:

@Service
public class OnboardingService {

    private final IdentityService identityService;

    public OnboardingService(
            IdentityService identityService) {
        this.identityService = identityService;
    }
}


---

Answer

Constructor injection makes dependencies explicit.

Benefits:

dependency is visible

required dependency cannot be omitted during construction

easier unit testing

supports final references

promotes cleaner design



---

Circular Dependency

Suppose:

OnboardingService
       ↓
IdentityService
       ↓
OnboardingService

Spring tries:

Create OnboardingService
↓
Needs IdentityService
↓
Create IdentityService
↓
Needs OnboardingService
↓
Cycle

The real problem is not constructor injection.

The real problem is:

> The service responsibilities depend on each other cyclically.



Constructor injection simply makes the cycle explicit.


---

Better design

If OnboardingService is the workflow orchestrator:

OnboardingService
       ↓
IdentityService
       ↓
External Identity System

IdentityService normally should not call back into OnboardingService.

If common functionality is needed:

OnboardingService ──┐
                    ↓
              CommonService
                    ↑
                    │
IdentityService ────┘


---

Concept to remember

Constructor injection is not merely a style preference.

It helps expose:

What does this class actually depend on?

And it makes dependency cycles easier to detect.


---

Q4. REQUIRED vs REQUIRES_NEW

Scenario

@Transactional
public void completeOnboarding(Long id) {

    onboardingRepository.updateStatus(id, "ACTIVE");

    auditService.saveAudit(id);

    throw new RuntimeException();
}

Audit:

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAudit(Long id) {
    auditRepository.save(...);
}


---

What happens?

Outer transaction:

TX-1

Audit uses:

REQUIRES_NEW

Therefore:

TX-1
 ↓
suspend TX-1
 ↓
start TX-2
 ↓
save audit
 ↓
commit TX-2
 ↓
resume TX-1
 ↓
RuntimeException
 ↓
rollback TX-1

Final result:

Onboarding update → ROLLBACK
Audit             → COMMITTED


---

What if audit used REQUIRED?

Default:

@Transactional

Then:

TX-1
 |
 +-- onboarding update
 |
 +-- audit
 |
 RuntimeException
 |
 ROLLBACK

Both roll back.


---

Mental model

REQUIRED
→ "Existing transaction hai to join karo."

REQUIRES_NEW
→ "Existing transaction suspend karo, naya transaction start karo."


---

Concept to remember

Propagation controls:

> "How does this method participate in transactions?"



It does NOT decide which exceptions cause rollback.

That is a separate concept: rollbackFor.


---

Q5. Global Exception Handling + HTTP Status Codes

Question

How do you avoid writing try/catch in every controller?

Answer

Use:

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdentityVerificationException.class)
    public ResponseEntity<ErrorResponse> handleIdentityError(
            IdentityVerificationException ex) {

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(...);
    }
}

This centralizes exception-to-HTTP mapping.


---

Common status codes

400

Client sent invalid input.

Invalid field
Malformed JSON
Missing required input

404

Resource doesn't exist.

ONB-101 not found

409

Request conflicts with current resource state.

Identity verification already in progress

503

Dependency is temporarily unavailable.

Identity Service unavailable

500

Unexpected server-side failure.

Unexpected application bug


---

Concept to remember

Don't think:

Exception → 500

Think:

What actually happened?
        ↓
What does that mean for the API consumer?
        ↓
Choose appropriate HTTP status


---

Q6. @Async + @Transactional

Scenario

@Transactional
public void verifyIdentity(Long id) {

    onboardingRepository.updateStatus(
        id,
        IN_PROGRESS
    );

    identityService.verifyAsync(id);
}

@Async
public void verifyAsync(Long id) {

    IdentityResponse response =
        externalIdentityClient.verify(id);

    onboardingRepository.updateStatus(
        id,
        VERIFIED
    );
}


---

Does the transaction propagate to @Async?

No.

Why?

Because @Async runs on another thread.

Spring's transaction context is normally thread-bound.

Main Thread
   |
   TX-1
   |
   +---- @Async ----> Async Thread
                           |
                           X TX-1

If the async method itself has:

@Async
@Transactional

it can create a new transaction:

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


---

Follow-up

If async processing fails, can it roll back TX-1?

No, not if TX-1 has already committed.

This is why async processing often requires:

durable state

retry

outbox

reconciliation

idempotency


depending on the workflow.


---

Another trap: self invocation

This:

this.verifyAsync(id);

may bypass Spring's @Async proxy.

Prefer calling the async method through another Spring-managed bean.


---

Concept to remember

@Transactional
→ Transaction context

@Async
→ Different thread

Different thread
→ Caller transaction does not automatically follow


---

Q7. HikariCP Connection Pool

Scenario

100 concurrent requests
Hikari maximum pool size = 10


---

What happens?

Only 10 connections can actively be used for DB operations at once.

The remaining requests that need a DB connection wait for one to become available.

If the wait exceeds:

spring.datasource.hikari.connection-timeout

connection acquisition can fail.


---

Important correction

Don't say:

> "Hikari processes 10 requests at a time."



Hikari manages:

DATABASE CONNECTIONS

not HTTP requests.

The HTTP requests can exist on many threads, but only requests that need DB access compete for connections.


---

Should we increase pool size to 100?

Not necessarily.

A bigger pool can actually make things worse.

The DB itself has finite:

CPU

memory

I/O

connection capacity

lock capacity


Too many connections can cause:

DB contention
CPU contention
context switching
lock contention


---

Multi-instance trap

If:

5 application instances
pool size = 20

then potentially:

5 × 20 = 100 DB connections

The DB sees the combined load.


---

Concept to remember

Connection pool size should be based on:

DB capacity
+
query latency
+
transaction duration
+
application instance count
+
production metrics

Not simply:

number of incoming requests


---

Q8. JPA/Hibernate N+1 Query Problem

Scenario

You fetch 100 onboardings:

List<Onboarding> onboardings =
    repository.findByStatus(PENDING);

Then:

for (Onboarding o : onboardings) {
    o.getCustomer().getName();
    o.getDocuments().size();
}


---

What is N+1?

Initial query:

SELECT *
FROM onboarding
WHERE status = 'PENDING';

That's one query.

Then accessing relationships may generate:

100 customer queries
+
100 document queries

Potentially:

1 + 100 + 100 = 201 queries

Exact number depends on mappings, cache and persistence context.


---

Does LAZY solve N+1?

No.

This is an important distinction.

LAZY means:

> Don't load the relationship until it is accessed.



So:

LAZY
 ↓
Loop
 ↓
Access customer
 ↓
Query

can actually produce N+1.


---

What about EAGER?

EAGER doesn't guarantee an efficient SQL strategy.

It can load more data than required.

Therefore don't think:

LAZY = bad
EAGER = good

Instead:

Default mapping
→ Usually LAZY for collections/associations

Specific use case
→ Explicitly define what needs to be fetched


---

Fetch Join

@Query("""
    SELECT o
    FROM Onboarding o
    JOIN FETCH o.customer
    WHERE o.status = :status
""")

Use when you need:

full entity + required association


---

EntityGraph

@EntityGraph(attributePaths = {"customer"})
List<Onboarding> findByStatus(Status status);

Useful when you want to specify a fetch plan without writing custom JPQL.


---

DTO Projection

If you only need:

id
customerName
status
createdAt

use a DTO projection instead of loading the whole entity graph.

Concept:

Full entity + relation
→ Fetch Join / EntityGraph

Only selected fields
→ DTO Projection


---

Concept to remember

N+1 is not fundamentally a "LAZY problem."

It is a:

> "How many SQL queries does my access pattern actually generate?"



problem.

Always inspect SQL and measure.


---

Q9. Pagination on Millions of Records

Scenario

Database contains:

10 million onboarding records

API:

GET /onboardings?page=50000&size=20


---

Offset pagination

Conceptually:

SELECT *
FROM onboarding
WHERE status = 'PENDING'
ORDER BY created_at DESC
LIMIT 20
OFFSET 999980;

Deep offsets can become expensive because the database may have to process/skip many preceding rows.


---

Keyset pagination

Instead of saying:

Give me page 50,000

say:

Give me the next 20 records after this last record.

Example:

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


---

Why include ID?

Because:

created_at

may not be unique.

Use:

created_at + id

to create deterministic ordering.


---

Index

Potential index:

CREATE INDEX idx_onboarding_status_created_id
ON onboarding(status, created_at DESC, id DESC);

Validate with:

EXPLAIN ANALYZE


---

Offset vs Keyset

Offset
→ Easy
→ Supports random page jumps
→ Deep pages can become expensive

Keyset
→ Excellent for large datasets
→ Efficient sequential navigation
→ Doesn't naturally support arbitrary page jumps


---

Q10. REST API Idempotency

Scenario

Frontend sends:

POST /onboardings/101/identity-verification
Idempotency-Key: ABC123

Request times out.

Frontend retries:

POST /onboardings/101/identity-verification
Idempotency-Key: ABC123


---

Why do we need idempotency?

Because the timeout does NOT necessarily mean the operation failed.

Possible situation:

Frontend
   ↓
Request
   ↓
Backend
   ↓
Identity Service SUCCESS
   ↓
Response lost
   ↓
Frontend timeout

Frontend doesn't know whether the operation happened.

So it retries.

Without idempotency:

Identity verification
→ executed twice


---

Basic design

Store:

idempotency_key
operation_status
response

with:

UNIQUE(idempotency_key)

Flow:

First request
 ↓
Key not found
 ↓
Process
 ↓
Store result

Retry:

Key exists
 ↓
Return stored result
 ↓
Don't execute again


---

Crash window

Problem:

External API succeeds
 ↓
DB updated
 ↓
Application crashes
 ↓
Idempotency record not saved

Retry sees:

No idempotency record

and could call the external service again.

Therefore local idempotency alone isn't always sufficient.


---

Stronger solution

Use a stable operation ID:

ONB-101-IDENTITY

and pass it to the external service if it supports idempotency.

Then:

First request
→ execute

Retry
→ same operation ID
→ return previous result

If external idempotency isn't available:

status lookup
+
reconciliation

may be required.


---

Concept to remember

Idempotency
→ Same logical request can safely happen multiple times.

Timeout
→ Communication problem; doesn't tell us whether operation happened.

Retry
→ Reattempt operation.

Idempotency
→ Makes retry safe.


---

Q11. External API Success + Application Crash

Scenario

@Transactional
public void completeIdentityVerification(Long id) {

    updateStatus(id, IN_PROGRESS);

    IdentityResponse response =
        identityClient.verify(id);

    updateStatus(id, VERIFIED);
}

External Identity Service succeeds.

Application crashes before:

IN_PROGRESS → VERIFIED


---

Result

Local system:

IN_PROGRESS

External system:

VERIFIED

This is distributed state inconsistency.


---

Why @Retryable isn't enough

@Retryable helps with transient exceptions.

But suppose the application process crashes completely.

An in-memory retry mechanism may disappear.

Therefore we need a durable mechanism for operations that must survive process failure.


---

Outbox pattern

Within a DB transaction:

BEGIN
 ↓
Set status = IN_PROGRESS
 ↓
Create outbox record:
IDENTITY_VERIFICATION_REQUIRED
 ↓
COMMIT

Now the intent is durable.

A worker can process the outbox.

Outbox
 ↓
Worker
 ↓
Identity Service
 ↓
Result
 ↓
Update DB


---

Crash after external success

Suppose:

Worker
 ↓
Identity Service SUCCESS
 ↓
CRASH

The outbox may still look unprocessed.

Worker retries.

If the external service supports idempotency:

Operation ID = ONB-101-IDENTITY

the retry doesn't create another business effect.


---

Concept to remember

Outbox
→ Durable intent

Retry
→ Handle transient failures

Idempotency
→ Prevent duplicate business effects

Reconciliation
→ Repair unknown/inconsistent states


---

Q12. Optimistic Locking + @Version

Scenario

Database:

ONB-101
status = MOBILE_VERIFIED
version = 5

Request A and B both load:

version = 5

A updates successfully:

version 5 → 6

B then tries to update.


---

What does Hibernate do?

Conceptually:

UPDATE onboarding
SET remarks = ?,
    version = 6
WHERE id = 101
AND version = 5;

But DB now has:

version = 6

Therefore:

affected rows = 0

Hibernate knows the entity was modified by someone else.

It can throw:

OptimisticLockException

or through Spring:

ObjectOptimisticLockingFailureException


---

Why is @Version useful?

It implements optimistic concurrency control.

Instead of locking the row beforehand:

"I'll lock this row."

it says:

"I'll update it only if nobody changed it since I read it."


---

What happens without @Version?

Hibernate loses this version check.

Two requests can overwrite each other's changes.

This can create lost updates.


---

Should API return 409?

Generally yes for a concurrency conflict:

409 Conflict

because the client's operation conflicts with the current resource state.


---

Critical follow-up

Does @Version prevent duplicate external calls?

No.

Example:

A → Identity Service SUCCESS
B → Identity Service SUCCESS

A → DB update succeeds
B → @Version update fails

B's database update failed, but B already called the external service.

Therefore:

@Version
→ DB concurrency protection

Idempotency
→ External/business operation duplication protection


---

Q13. Checked Exception + @Transactional

Scenario

@Transactional
public void approveOnboarding(Long id) throws Exception {

    updateStatus(id);

    saveAudit(id);

    sendNotification(id);

    throw new Exception("Notification failed");
}

The final exception is checked.


---

What happens by default?

Spring's default rollback rules are approximately:

RuntimeException
→ Rollback

Error
→ Rollback

Checked Exception
→ Usually NO rollback

Therefore the DB changes may remain committed.


---

How do we rollback for checked exceptions?

@Transactional(
    rollbackFor = Exception.class
)

Now:

Checked Exception
 ↓
rollbackFor matches
 ↓
Rollback transaction


---

Important distinction

Don't confuse:

rollbackFor

with:

propagation

Propagation

REQUIRED
REQUIRES_NEW
NESTED

controls transaction participation.

rollbackFor

controls which exceptions trigger rollback.


---

External notification trap

Suppose:

DB update
 ↓
Audit saved
 ↓
External notification SUCCESS
 ↓
Checked exception
 ↓
DB rollback

Result:

Database
→ Rolled back

External notification
→ Already sent

Spring cannot magically undo an HTTP call.

Therefore:

@Transactional
≠
Distributed transaction


---

Q14. Spring Singleton + Thread Safety

Scenario

@Service
public class OnboardingService {

    private String currentOnboardingId;

    public void process(String onboardingId) {

        currentOnboardingId = onboardingId;

        // processing

        System.out.println(currentOnboardingId);
    }
}

Requests:

A → ONB-101
B → ONB-202
C → ONB-303


---

Default @Service scope

Singleton.

That means:

ONE bean instance

is normally shared across requests.

But requests execute on different threads.

Thread A ─┐
Thread B ─┼──→ SAME OnboardingService object
Thread C ─┘


---

Why is this code unsafe?

Because:

private String currentOnboardingId;

is shared mutable state.

Possible execution:

Thread A:
currentOnboardingId = ONB-101

Thread B:
currentOnboardingId = ONB-202

Thread A:
read currentOnboardingId

Result:
ONB-202

A may print the wrong request's ID.


---

Important interview correction

Don't say:

> "Spring singleton beans are not thread-safe."



Say:

> "Spring singleton beans can be accessed concurrently, so they should generally be stateless. The problem is shared mutable instance state."




---

Correct design

Use method parameters/local variables:

@Service
public class OnboardingService {

    public void process(String onboardingId) {

        String currentId = onboardingId;

        // processing
    }
}

Now each thread has its own local variable.

Thread A → local ID = ONB-101
Thread B → local ID = ONB-202
Thread C → local ID = ONB-303


---

Does final make it thread-safe?

No.

Example:

private final List<String> ids = new ArrayList<>();

The reference cannot be reassigned, but the list itself is mutable.

Therefore:

final
→ Reference cannot be reassigned

final
≠
Object immutable

final
≠
Object thread-safe


---

Q14 Follow-up. Same Service Is Stateless, But Same DB Row Is Updated

Suppose:

10 concurrent requests
       ↓
same ONB-101
       ↓
same operation
       ↓
MOBILE_VERIFIED → IDENTITY_IN_PROGRESS


---

What do we want?

Only one request should win.

10 requests
     ↓
1 winner
     ↓
9 conflicts


---

Bad approach

Onboarding o = repository.findById(id);

if (o.getStatus() == MOBILE_VERIFIED) {
    o.setStatus(IN_PROGRESS);
    repository.save(o);
}

Possible:

R1 → READ MOBILE_VERIFIED
R2 → READ MOBILE_VERIFIED
R3 → READ MOBILE_VERIFIED
...
R10 → READ MOBILE_VERIFIED

All can think:

> "I am allowed."




---

Better approach

Atomic DB update:

UPDATE onboarding
SET status = 'IDENTITY_IN_PROGRESS'
WHERE id = 101
AND status = 'MOBILE_VERIFIED';

Results:

R1 → affected = 1 → winner
R2 → affected = 0
R3 → affected = 0
...
R10 → affected = 0

Java:

int updated =
    repository.markIdentityInProgress(id);

if (updated == 0) {
    throw new ConflictException(
        "Identity verification already started"
    );
}


---

Why database-level protection?

Because the database owns the shared persistent state.

We want:

CHECK STATE
+
UPDATE STATE

to happen atomically.


---

Application vs Database concurrency

Java level

Multiple threads
↓
Avoid shared mutable state
↓
Stateless service

Database level

Multiple transactions
↓
Same row
↓
Atomic update / @Version / lock

These are two different concurrency problems.


---

Q15. JPA Bulk Update + Persistence Context

Scenario

Initial DB:

ONB-101 → PENDING
ONB-102 → PENDING
ONB-103 → PENDING

First:

List<Onboarding> onboardings =
    repository.findByStatus(PENDING);

Then:

repository.bulkUpdateStatus(
    PENDING,
    PROCESSING
);


---

What does the database contain after bulk update?

ONB-101 → PROCESSING
ONB-102 → PROCESSING
ONB-103 → PROCESSING

But what might the Java objects contain?

ONB-101 → PENDING
ONB-102 → PENDING
ONB-103 → PENDING


---

Why?

Because Hibernate already loaded these entities into the persistence context.

Think:

Database
   ↓
SELECT
   ↓
Hibernate
   ↓
Persistence Context
   ↓
Java objects

At this point:

Java object → PENDING

Then the bulk JPQL update goes directly to the DB:

JPQL bulk UPDATE
       ↓
SQL UPDATE
       ↓
Database

It doesn't automatically update every existing managed Java object.

Therefore:

Database = PROCESSING
Java     = PENDING

The persistence context is now stale.


---

What is Persistence Context?

The persistence context is the set of managed entity instances associated with the current EntityManager.

Conceptually:

@Transactional
      ↓
EntityManager
      ↓
Persistence Context
      ↓
Managed entities

It also acts as Hibernate's first-level cache.


---

Why is it called First-Level Cache?

Within a persistence context, Hibernate tracks managed entities.

For example:

Onboarding a = repository.findById(101L).get();
Onboarding b = repository.findById(101L).get();

Hibernate can maintain the same managed entity instance for ID 101 within that persistence context.

This helps avoid unnecessary repeated database loads.


---

Fix 1 — clear()

entityManager.clear();

This removes managed entities from the persistence context.

Then a new query fetches fresh data.

Bulk UPDATE
     ↓
DB changed
     ↓
clear()
     ↓
Persistence Context cleared
     ↓
new SELECT
     ↓
fresh data


---

Fix 2 — clearAutomatically

Spring Data:

@Modifying(clearAutomatically = true)
@Query("""
    UPDATE Onboarding o
    SET o.status = :newStatus
    WHERE o.status = :oldStatus
""")

Spring can automatically clear the persistence context after the bulk update.


---

Fix 3 — refresh()

If only one entity needs refreshing:

entityManager.refresh(onboarding);

This reloads its state from the DB.

Java = PENDING

refresh()

DB = PROCESSING

↓

Java = PROCESSING


---

flush() vs clear() vs refresh()

flush()

entityManager.flush();

Means:

Persistence Context
        ↓
Synchronize pending changes
        ↓
Database

Mental model:

flush = Java → DB


---

clear()

entityManager.clear();

Means:

Remove managed entities
from persistence context

Mental model:

clear = Forget managed Java entities


---

refresh()

entityManager.refresh(entity);

Means:

Database
   ↓
Reload
   ↓
Specific Java entity

Mental model:

refresh = DB → Java


---

Why Bulk Updates Are Dangerous

Suppose:

Java entity:
status = PENDING

Database:
status = PROCESSING

Then the stale Java entity is modified:

entity.setRemarks("Verified");

and eventually flushed.

Now the persistence context and database have different views of the entity.

This can lead to confusing or unexpected behaviour.

Therefore:

> Be careful when mixing bulk JPQL updates with already-managed entities.




---

MASTER CONCEPT MAP

1. Application Concurrency

Problem:

Multiple threads
+
Shared mutable state

Solution:

Stateless service
Local variables
Immutable/final dependencies
Concurrency primitives when genuinely required


---

2. Database Concurrency

Problem:

Multiple transactions
+
Same DB row

Solutions:

@Version
Pessimistic locking
Atomic conditional UPDATE
Database constraints


---

3. Duplicate Requests

Problem:

Frontend timeout
+
Retry

Solution:

Idempotency key
+
Unique constraint
+
Stored result


---

4. External Side Effects

Problem:

Database transaction
+
External API

Important:

DB rollback
≠
External API rollback

Solutions may include:

Outbox
Idempotency
Retry
Reconciliation
Saga


---

5. Crash Recovery

Problem:

External operation succeeds
+
Application crashes
+
Local DB isn't updated

Solutions:

Durable outbox
+
Idempotent external operation
+
Retry
+
Reconciliation


---

6. Transaction Management

@Transactional
→ Transaction boundary

REQUIRED
→ Join existing transaction

REQUIRES_NEW
→ New independent transaction

rollbackFor
→ Rollback rules


---

7. Hibernate Persistence Context

Entity loaded
     ↓
Persistence Context
     ↓
Managed Java object

Bulk update:

JPQL UPDATE
     ↓
Database directly

Therefore:

DB state
can differ temporarily from
managed Java object state

Remember:

flush()
→ Java → DB

clear()
→ Remove managed entities

refresh()
→ DB → Java


---

MASTER INTERVIEW THINKING FRAMEWORK

Whenever an interviewer gives you a production scenario, don't immediately jump to:

> "Use @Transactional."



Instead ask yourself these questions:

Step 1 — What is being protected?

Java memory?
Database row?
External API?
Entire business operation?


---

Step 2 — Is it concurrent?

Multiple threads?
Multiple HTTP requests?
Multiple DB transactions?
Multiple application instances?


---

Step 3 — Is it duplicated?

Same request retry?
Different concurrent requests?
Same business operation?


---

Step 4 — Is there an external side effect?

HTTP API?
Payment?
Identity verification?
Email?
Message?

If yes:

@Transactional alone is NOT enough.


---

Step 5 — What if the application crashes?

Always ask:

What if we crash here?

For example:

DB updated
↓
External call
↓
CRASH
↓
What is the state now?

This question leads you toward:

Outbox
Retry
Idempotency
Reconciliation


---

THE MOST IMPORTANT DIFFERENCES TO MEMORISE CONCEPTUALLY

@Version vs Idempotency

@Version
→ "Did somebody modify this DB record after I read it?"

Idempotency
→ "Has this logical business operation already been performed?"


---

Lock vs Atomic Update

Pessimistic Lock
→ Lock first, then work

Atomic UPDATE
→ Let DB check condition + modify in one operation


---

Transaction vs Idempotency

Transaction
→ Atomicity within participating transactional resources

Idempotency
→ Repeated requests don't create duplicate business effects


---

Retry vs Idempotency

Retry
→ Try the operation again

Idempotency
→ Make trying again safe

Usually you need both.


---

Outbox vs Idempotency

Outbox
→ Durable record that work needs to happen

Idempotency
→ Repeating the work doesn't duplicate its business effect

They solve different problems and are often used together.


---

flush vs clear vs refresh

flush
→ Java state → DB

clear
→ Remove managed objects

refresh
→ DB state → Java object


---

Stateless Service vs Database Concurrency

Stateless service
→ Protects application memory from shared request-specific state

DB concurrency control
→ Protects persistent shared state

A service can be perfectly stateless and you can still have database race conditions.


---

FINAL BANKING ONBOARDING FLOW

A production-oriented identity verification flow can conceptually look like:

HTTP REQUEST
                         │
                         ▼
                Spring Controller
                         │
                         ▼
                OnboardingService
                         │
                         │
                         ▼
             Atomic State Transition
                         │
          MOBILE_VERIFIED → IN_PROGRESS
                         │
                  ┌──────┴──────┐
                  │             │
               success        failure
                  │             │
                  ▼             ▼
             Continue        409 Conflict
                  │
                  ▼
          External Identity API
                  │
                  ▼
             Verification
                  │
          ┌───────┴────────┐
          │                │
       SUCCESS           FAILURE
          │                │
          ▼                ▼
    Short DB TX        Retry / Recovery
          │
          ▼
    IDENTITY_VERIFIED

Around this flow:

Concurrent requests
→ Atomic state transition / @Version

Repeated request
→ Idempotency key

Slow external API
→ Don't hold DB transaction unnecessarily

Crash after external success
→ Outbox + idempotency + reconciliation

Unexpected errors
→ Global exception handler

Large data
→ Pagination / keyset pagination

Large entity graph
→ Fetch Join / EntityGraph / DTO projection

Multiple threads
→ Stateless Spring service

Database connections
→ HikariCP sizing based on DB capacity


---

FINAL RULE

When solving a Spring Boot production problem, think in this order:

WHAT CAN GO WRONG?
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
       Threads          DB          External API
          │              │              │
          ▼              ▼              ▼
    Shared state      Concurrent      Duplicate
                      updates         side effects
          │              │              │
          ▼              ▼              ▼
      Stateless       @Version       Idempotency
      service         Atomic SQL
                      Lock
                         │
                         └──────────────┐
                                        ▼
                                  Application crash
                                        │
                                        ▼
                                Outbox / Retry /
                                Reconciliation

> The goal is not to remember which annotation to use.

First identify where the failure or race exists, then choose the mechanism that solves that specific problem.