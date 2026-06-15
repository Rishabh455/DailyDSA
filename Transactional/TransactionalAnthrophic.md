# Spring Transaction Management — Complete Interview-Ready Notes (3 YOE)

> 30-minute revision covering: What is a transaction, @Transactional, all 7 Propagation types, all 5 Isolation levels — with e-commerce examples, internals, interview Q&A, traps.

---

# PART 1 — WHY TRANSACTIONS EXIST (The Foundation)

## 1.1 The Problem (E-commerce Use Case)

**Scenario:** Place an order for a laptop involves 3 DB operations across services:
1. Save order to **Order DB** (order_id, product_id, quantity, total_price)
2. Update stock in **Inventory DB** (reduce available quantity)
3. Save record in **Payment DB**

**What goes wrong without transaction:**
- Order #102 (5 laptops) gets saved successfully to Order DB.
- While updating Inventory DB, a network glitch/DB error occurs.
- Result: Order record exists, but inventory stock is NOT reduced.
- Now Catalog Service shows 8 laptops available — but in reality only 3 should remain.
- Customer orders, gets a failure ("out of stock"), gets frustrated → **overselling problem** + **data inconsistency**.

## 1.2 The Solution — Transaction Principle

> **"If anything fails in the flow → roll back everything. If everything succeeds → commit everything."**

This is **atomicity** — all-or-nothing execution.

### Real-world analogy (Banking)
Transferring ₹10,000 from Account A to Account B:
- Step 1: Debit ₹10,000 from A
- Step 2: Credit ₹10,000 to B

If Step 2 fails after Step 1 succeeds, money vanishes from the system. Transaction ensures **both happen or neither happens**.

---

## A. Interview Answer (30-60 sec) — "What is a transaction and why do we need it?"

> "A transaction is a group of database operations that must execute as a single atomic unit — either all succeed (commit) or all fail (rollback). We need it because real-world business flows like placing an order involve multiple DB writes across tables (order, inventory, payment). If one operation fails midway without transaction management, we end up with partial/stale data causing inconsistency — like an order being saved but inventory not updated, leading to overselling. Spring provides `@Transactional` to handle this declaratively using AOP proxies."

## B. Cross Questions
1. Is a transaction only for multiple tables, or can it apply to a single table?
2. What happens if `@Transactional` is on a private method?
3. Does `@Transactional` work for SELECT queries?

## C. Strong Answers
1. Even a single multi-statement operation on one table benefits from transactions (e.g., update + audit log insert). But it's especially critical across multiple tables/repositories.
2. **No** — Spring uses proxy-based AOP. Private/final methods aren't proxied, so `@Transactional` is silently ignored. This is a classic trap.
3. Yes, but for read-only operations use `@Transactional(readOnly = true)` for performance optimization (Hibernate skips dirty checking).

## D. Common Follow-ups
- "What's the difference between `@Transactional` from `jakarta.transaction` vs `org.springframework.transaction.annotation`?"
  → Always use **Spring's** annotation (`org.springframework.transaction.annotation.Transactional`) because it supports propagation, isolation, rollback rules — `jakarta`'s is JTA-standard and more limited.

## E. Senior-Level Discussion Points
- Transactions add overhead — don't wrap read-heavy methods unnecessarily.
- In microservices, a single `@Transactional` **cannot** span multiple services/databases (no distributed ACID) — this is why **Saga pattern** exists.

---

# PART 2 — @Transactional ANNOTATION DEEP DIVE

## 2.1 Purpose
Declarative way to wrap a method in a database transaction without writing manual `begin/commit/rollback` JDBC code.

## 2.2 Key Attributes

@Transactional important attributes:

1. readOnly (default = false)
   - false → Full transaction (SELECT, INSERT, UPDATE, DELETE allowed)
   - true → Used for read operations only.
   - Improves performance by telling Spring/DB that no data modification is expected.

2. propagation (default = REQUIRED)
   - Decides how the current transaction behaves with an existing transaction.
   - REQUIRED → Join existing transaction, otherwise create a new one.
   - REQUIRES_NEW → Always create a new transaction.

3. isolation (default = DEFAULT)
   - Defines how one transaction can see data changes made by another transaction.
   - Helps avoid concurrency issues like Dirty Read, Non-Repeatable Read, and Phantom Read.
   - Commonly used: READ_COMMITTED.

4. rollbackFor
   - Specifies which exceptions should trigger transaction rollback.
   - By default, Spring rolls back only on unchecked exceptions (RuntimeException).

5. noRollbackFor
   - Specifies exceptions for which rollback should NOT happen.
   - Transaction will commit even if that exception occurs.
readOnly = What operations are allowed
propagation = Which transaction to use
isolation = How transactions see each other's data
rollbackFor = When to rollback
noRollbackFor = When NOT to rollback
## 2.3 Internal Working — Proxy/AOP Mechanism

**Pseudocode of what Spring generates internally:**
```
method callerMethod() {
    proxyObject.placeOrder(orderRequest)   // NOT calling real object directly!
}

class ProxyOrderProcessingService {
    placeOrder(orderRequest) {
        transactionManager.beginTransaction()
        try {
            realService.placeOrder(orderRequest)
            transactionManager.commit()
        } catch (Exception e) {
            transactionManager.rollback()
            throw e
        }
    }
}
```

**Execution flow:**
1. Spring creates a **proxy** (CGLIB or JDK dynamic proxy) around your bean.
2. When you call `orderProcessingService.placeOrder(...)`, you're actually calling the **proxy**, not your real class.
3. Proxy calls `PlatformTransactionManager.getTransaction()` → opens a DB connection/transaction (or reuses existing one based on propagation).
4. Proxy delegates to your actual method.
5. If method completes normally → proxy calls `commit()`.
6. If method throws a **runtime/unchecked exception** → proxy calls `rollback()`.
7. Checked exceptions do **NOT** trigger rollback by default (must configure `rollbackFor`).

### ⚠️ Common Mistakes (Critical Interview Traps)

1. **Self-invocation problem**: Calling a `@Transactional` method from another method *within the same class* bypasses the proxy → annotation is ignored.
   ```
   class OrderService {
       void methodA() {
           this.methodB();  // ❌ direct call, NOT through proxy — @Transactional on methodB ignored
       }
       @Transactional
       void methodB() { ... }
   }
   ```
2. `@Transactional` on `private` methods → silently ignored (CGLIB proxies can't override private methods).
3. Forgetting `filterChain.doFilter()`-style equivalent — forgetting that checked exceptions don't roll back by default.
4. Using `@Transactional(readOnly = true)` but performing writes → runtime exception.

---

## A. Interview Answer — "How does @Transactional work internally?"

> "@Transactional is implemented using Spring AOP via proxies. When a bean has this annotation, Spring wraps it in a proxy (CGLIB by default for class-based proxying). Every call to the annotated method actually goes through the proxy first, which uses `PlatformTransactionManager` to start a transaction before the real method executes, and commits or rolls back after, depending on whether an exception was thrown. By default, only unchecked (RuntimeException and its subclasses) exceptions trigger a rollback."

## B. Cross Questions
1. Why doesn't `@Transactional` work when called from the same class?
2. What is `PlatformTransactionManager`? What implementations exist?
3. Does `@Transactional` work on a method called via `@Async`?

## C. Strong Answers
1. Because Spring proxies wrap the *bean*, not the *class instance*. A call from `this.method()` inside the same class bypasses the proxy entirely and calls the real object directly — so no transaction interception happens.
2. It's an interface abstraction for transaction management. Common implementations: `JpaTransactionManager` (Hibernate/JPA), `DataSourceTransactionManager` (plain JDBC), `JtaTransactionManager` (distributed/XA transactions).
3. No — `@Async` methods run on a different thread, and `@Transactional` relies on `ThreadLocal`-based context (`SecurityContextHolder`-style). The transaction context doesn't propagate to the async thread automatically.

## D. Follow-ups
- "Can you have two `@Transactional` methods calling each other across different beans?" → Yes, this works correctly because cross-bean calls always go through the proxy.

## E. Senior-Level Discussion Points
- In production, a common bug source is "transaction silently not applied" due to self-invocation — fix by injecting `self` reference (`@Lazy @Autowired OrderService self`) or splitting into separate beans.
- Always log `org.springframework.transaction` at DEBUG in non-prod environments to visually verify transaction creation/commit/suspend — exactly as demonstrated in the video.

---

# PART 3 — TRANSACTION PROPAGATION (All 7 Types)

## 3.1 What is Propagation?

> Propagation defines **how a transactional method behaves when called from within another transactional method** — does it join the existing transaction, create a new one, suspend the existing one, or throw an error?

**Setup for understanding:** M1 (outer method, has `@Transactional`) calls M2 (inner method, has `@Transactional`). Propagation on M2 decides the relationship.

---

## 3.2 PROPAGATION.REQUIRED (Default)

### Definition
> "Join an existing transaction. If none exists, create a new one."

### How it works internally
- Outer method (`placeOrder`) starts → creates Transaction T1.
- Inner methods (`saveOrder`, `updateInventoryStock`) also marked `REQUIRED` → they detect T1 is active → **participate in T1** (no new transaction created).
- If any inner method throws an exception → entire T1 rolls back (order save + inventory update both undone).

### Proof from logs (as shown in transcript)
```
Creating new transaction with name [placeOrder]   ← outer, T1 created
Participating in existing transaction              ← saveOrder joins T1
Participating in existing transaction              ← updateInventoryStock joins T1
[on error] → Transaction rolled back               ← everything undone
```

### Real-world example (E-commerce)
Place order = save order + update inventory. Both must succeed or both must fail — classic `REQUIRED` use case.

### Pseudocode
```
@Transactional(propagation = REQUIRED)
placeOrder(orderRequest):
    product = getProduct(orderRequest.productId)
    validateStock(product, orderRequest.quantity)
    order.totalPrice = quantity * product.price
    saveOrder(order)              // joins same TXN
    updateInventoryStock(product) // joins same TXN
    // if updateInventoryStock throws → saveOrder is ALSO rolled back
```

### A. Interview Answer
> "REQUIRED is the default propagation. It means: if a transaction is already active when this method is called, reuse it; if not, start a new one. This is used when multiple operations must succeed or fail together — like saving an order and updating inventory stock in the same business transaction."

### B. Cross Questions
1. What if `placeOrder` itself has NO `@Transactional` — what happens to `saveOrder` (which is `REQUIRED`)?
2. Does REQUIRED create a new DB connection for each inner method?

### C. Strong Answers
1. Since no active transaction exists, `saveOrder` will create its own NEW transaction (because REQUIRED falls back to creating one if none exists).
2. No — it reuses the same connection/transaction; that's the whole point of "participating in existing transaction."

### D. Follow-ups
- "If `updateInventoryStock` fails, does `saveOrder`'s data get persisted to DB at all?" → No, because they're in the same transaction — rollback undoes both, even though `saveOrder` executed "successfully" in memory/staging.

---

## 3.3 PROPAGATION.REQUIRES_NEW

### Definition
> "Always create a NEW transaction, suspending any existing transaction (if present) until the new one completes."

### Real-world example: Audit Logging
You want to log "order placement succeeded/failed" **regardless of whether the main transaction commits or rolls back**. If you used `REQUIRED`, a failure in `placeOrder` would roll back the audit log entry too — defeating its purpose.

### Internal flow (from logs)
```
Creating new transaction [placeOrder]              ← T1 (outer, REQUIRED)
Participating in existing transaction               ← saveOrder, updateInventory (T1)
Suspending current transaction                      ← T1 paused
Creating new transaction [auditLogHandler.logAuditDetails]  ← T2 (independent)
Committing T2 (audit log committed permanently)
Resuming suspended transaction (T1)
[T1 rolls back due to error] → saveOrder + updateInventory undone
                              → BUT audit log entry STAYS in DB
```

### Pseudocode
```
@Transactional(propagation = REQUIRED)
placeOrder(orderRequest):
    try:
        saveOrder(order)
        updateInventoryStock(product)
        auditLogHandler.logAuditDetails(order, "SUCCEEDED")  // REQUIRES_NEW
    catch (Exception e):
        auditLogHandler.logAuditDetails(order, "FAILED")     // REQUIRES_NEW — still persists!
        throw e

@Transactional(propagation = REQUIRES_NEW)
logAuditDetails(order, action):
    auditLog.save(order, action, timestamp)  // commits independently
```
placeOrder()

T1 Start
│
├── saveOrder()           ✅
├── updateInventory()     ✅
│
├── logAudit()
│      │
│      ├── Suspend T1
│      │
│      ├── T2 Start
│      ├── saveAudit()
│      ├── T2 Commit      ✅
│      │
│      └── Resume T1
│
└── Exception

T1 Rollback ❌

Final Result:

Order       ❌
Inventory   ❌
Audit Log   ✅
### A. Interview Answer
> "REQUIRES_NEW always starts a fresh, independent transaction — if a transaction is already running, it gets suspended until the new one finishes. This is used when you need an operation's outcome to persist regardless of the outer transaction's result — classic example is audit logging or notification tracking. Even if the main order placement rolls back, the audit log entry recording the failure remains committed."

### B. Cross Questions
1. What's the performance cost of REQUIRES_NEW?
2. Can REQUIRES_NEW cause deadlocks?
3. If the inner (REQUIRES_NEW) transaction fails, does it affect the outer transaction?

### C. Strong Answers
1. Each REQUIRES_NEW opens a **new physical DB connection/transaction** — more connections from the pool, more commit overhead. Use sparingly.
2. Yes, potentially — if the suspended outer transaction holds row-level locks that the inner transaction needs, you can get contention/deadlock depending on isolation level.
3. By default — if the inner transaction throws an exception and it propagates up uncaught, yes it can also cause the outer to roll back. But if caught and handled (like in the audit log try-catch pattern), the outer continues independently.

### D. Follow-ups
- "Where else is REQUIRES_NEW used in real production systems?" → Payment gateway retry logging, sending emails/SMS notifications that must be tracked even on failure, writing to a separate analytics/event table.

### E. Senior-Level Discussion Points
- REQUIRES_NEW is a double-edged sword: great for independent audit trails, but overuse leads to **connection pool exhaustion** under high load — each suspended+new transaction ties up 2 connections momentarily.

---

## 3.4 PROPAGATION.MANDATORY

### Definition
> "Must run within an existing active transaction. If none exists, throw `IllegalTransactionStateException`."

### Real-world example: Payment Validation
`validatePayment()` should only ever run as part of the larger `placeOrder` transaction — it should never run standalone. If a failure happens in `validatePayment`, it should NOT roll back the entire order (only logs the issue), but it absolutely **requires** an existing transaction context to operate within.

### Pseudocode
```
@Transactional(propagation = MANDATORY)
validatePayment(order):
    if order.totalPrice > 100000:
        log("Payment validation failed")
        // doesn't throw — just logs, doesn't break outer flow
```

### Behavior proof (from transcript)
- Called from within `placeOrder` (which has active T1) → works fine, participates in T1.
- Called standalone (no active transaction) → throws:
  ```
  IllegalTransactionStateException: No existing transaction found for 
  transaction marked with propagation 'mandatory'
  ```

### A. Interview Answer
> "MANDATORY enforces that the method MUST be called within an existing transaction — it never creates its own and never runs standalone. If there's no active transaction, Spring throws `IllegalTransactionStateException`. It's used to guarantee a method is always part of a larger business transaction — e.g., a payment validation step that should always execute as part of order placement, never independently."

### B. Cross Questions
1. How is MANDATORY different from REQUIRED?
2. When would you actually use this in real projects?

### C. Strong Answers
1. REQUIRED creates a new transaction if none exists (flexible/fallback). MANDATORY throws an exception if none exists (strict enforcement) — it never creates a new transaction itself.
2. Used as a "guard rail" for internal helper methods that should architecturally never be called outside a transactional context — enforces correct usage at runtime, catching developer mistakes early.

### D. Follow-ups
- "Is MANDATORY commonly used in real projects?" → Honestly, rarely — REQUIRED and REQUIRES_NEW dominate. MANDATORY is more of a defensive/architectural-enforcement tool.

---

## 3.5 PROPAGATION.NEVER

### Definition
> "Method must run WITHOUT any transaction. If an active transaction is found, throw `IllegalTransactionStateException`."

### Real-world example: Sending Notifications
Sending an order confirmation SMS/email should happen **outside** any transaction. Why? If it's inside the transaction and the transaction retries (e.g., 3 retries on failure), the user could receive duplicate notifications ("Your order failed" x3). Keeping it transaction-free and calling it *after* the transaction completes avoids this.

### Pseudocode
```
@Transactional(propagation = NEVER)
sendOrderConfirmationNotification(order):
    print("Sending notification for order: " + order.id)
    // throws IllegalTransactionStateException if called from within @Transactional method
```

### Correct usage pattern
```
processOrder(orderRequest):              // NO @Transactional on this wrapper
    placedOrder = placeOrder(orderRequest)   // @Transactional REQUIRED — runs in TXN
    notificationHandler.sendOrderConfirmationNotification(placedOrder)  // OUTSIDE txn — NEVER
```

### A. Interview Answer
> "NEVER ensures a method executes completely outside any transactional context — if it's called while a transaction is active, Spring throws an exception. A real use case is sending notifications: if notification-sending happens inside a transaction that gets retried on failure, the user could receive duplicate messages. By marking it NEVER and calling it after the main transaction completes, we guarantee it runs exactly once, transaction-free."

### B. Cross Questions
1. How is NEVER different from NOT_SUPPORTED?
2. What's a practical risk of using NEVER incorrectly?

### C. Strong Answers
1. NEVER throws an exception if a transaction exists. NOT_SUPPORTED simply **suspends** the existing transaction and runs without one — no exception.
2. If you mistakenly call a NEVER method from inside a transactional flow, you get a runtime exception that breaks the entire flow — must carefully structure the call hierarchy (call it from a non-transactional wrapper method).

---

## 3.6 PROPAGATION.NOT_SUPPORTED

### Definition
> "Execute the method WITHOUT a transaction, suspending any existing transaction if found (then resume it after)."

### Real-world example: Product Recommendations
After placing an order, show "you might also like..." recommendations — a pure read operation that doesn't need transactional guarantees. Whether the order succeeds or fails, recommendations should still be fetched/shown.

### Internal flow (from logs)
```
[Active transaction: placeOrder]
Suspending current transaction
→ getRecommendations() executes WITHOUT transaction
Resuming suspended transaction
[continues placeOrder flow]
```
- Works the same in BOTH happy path and failure path — recommendations always execute.

### Pseudocode
```
@Transactional(propagation = NOT_SUPPORTED)
getRecommendations(customerId):
    return hardcodedRecommendedProducts  // pure read, no transaction needed
```

### A. Interview Answer
> "NOT_SUPPORTED means the method runs without a transaction — if one is currently active, Spring suspends it for the duration of this method, then resumes it afterward. It's ideal for read-only operations like fetching product recommendations, logging, or any non-critical side operation that shouldn't be tied to (or slowed down by) the main transaction's lock/connection."

### B. Cross Questions
1. Why suspend rather than just running it within the existing transaction?
2. Performance implication?

### C. Strong Answers
1. Running it within the existing transaction would unnecessarily extend the transaction's lifetime/locks for an operation that doesn't need transactional guarantees — suspending releases that overhead temporarily.
2. Slight overhead of suspend/resume, but net positive — avoids holding DB locks longer than necessary, improving concurrency.

---

## 3.7 PROPAGATION.SUPPORTS

### Definition
> "Join the existing transaction if one exists; otherwise, execute without a transaction. Either way is fine — no enforcement."

### Real-world example: Fetching Customer Details
`getCustomerDetails()` — a simple lookup that works fine with or without a transaction. If called from within `placeOrder` (transactional), it joins. If called standalone, it just runs normally.

### A. Interview Answer
> "SUPPORTS is the most flexible propagation — the method participates in a transaction if one exists, but runs perfectly fine without one too. It doesn't enforce or require anything. Useful for simple lookups/utility methods that may be called from both transactional and non-transactional contexts, like fetching customer address details."

### B. Cross Questions
1. How is SUPPORTS different from REQUIRED?
2. Why not just use NOT_SUPPORTED or REQUIRED everywhere — why have SUPPORTS at all?

### C. Strong Answers
1. REQUIRED *guarantees* a transaction exists (creates one if needed). SUPPORTS makes NO guarantee either way — purely opportunistic.
2. SUPPORTS gives flexibility for utility/shared methods called from many different contexts (some transactional, some not) without forcing a specific behavior — reduces coupling.

---

## 3.8 PROPAGATION.NESTED

### Definition
> "Executes within a nested transaction (using savepoints). If the nested transaction fails, only it rolls back — the outer transaction is unaffected. If the outer transaction rolls back, the nested one rolls back too."

### ⚠️ CRITICAL CAVEAT (from transcript)
> **Spring Data JPA (Hibernate) does NOT support NESTED propagation** — it throws:
> ```
> NestedTransactionNotSupportedException: JPA dialect does not support savepoints
> ```
> NESTED requires JDBC savepoint support — works with plain JDBC + `DataSourceTransactionManager`, NOT with `JpaTransactionManager`.

### Real-world example (conceptual)
`placeOrder` (outer) calls `validatePayment` (nested). If `validatePayment` fails, only the payment validation rolls back — order save and inventory update are unaffected. But if the OUTER transaction (`placeOrder`) rolls back for some other reason, the nested payment validation also rolls back.

### A. Interview Answer
> "NESTED creates a child transaction within the parent transaction using database savepoints. If the nested transaction fails, it rolls back to the savepoint without affecting the parent — but if the parent rolls back, the nested transaction rolls back too. Important caveat: this requires savepoint support, which Spring Data JPA/Hibernate does NOT provide — it only works with `DataSourceTransactionManager` using plain JDBC."

### B. Cross Questions
1. Why doesn't Hibernate support NESTED?
2. What's the practical alternative if you need this "partial rollback" behavior with JPA?

### C. Strong Answers
1. Hibernate's session/flush-based architecture and most JPA dialects don't expose savepoint APIs in a way Spring's transaction abstraction can use — it's a dialect-level limitation, not a Spring limitation.
2. Use `REQUIRES_NEW` for the sub-operation (fully independent transaction) combined with try-catch in the outer method to handle the sub-operation's failure gracefully without rolling back the outer transaction.

---

## 3.9 PROPAGATION SUMMARY TABLE

| Propagation | New Txn? | Existing Txn Behavior | Throws if Missing? | Real Use Case |
|---|---|---|---|---|
| REQUIRED (default) | Creates if none | Joins existing | No | Order + inventory update (all-or-nothing) |
| REQUIRES_NEW | Always creates | Suspends existing | No | Audit logging (persists regardless of outcome) |
| MANDATORY | Never creates | Must join existing | **Yes** | Payment validation (must be part of order flow) |
| NEVER | Never creates | Must have NONE | **Yes** (if exists) | Sending notifications (must run outside txn) |
| NOT_SUPPORTED | Runs without txn | Suspends existing | No | Product recommendations (pure read) |
| SUPPORTS | Doesn't enforce | Joins if exists, else none | No | Fetching customer details (flexible) |
| NESTED | Savepoint-based child | Rolls back independently (mostly) | No | ⚠️ Not supported in JPA/Hibernate |


| Propagation       | Existing Transaction?    | No Transaction?         | Typical Use Case          |
| ----------------- | ------------------------ | ----------------------- | ------------------------- |
| **REQUIRED**      | Join Existing            | Create New              | Normal CRUD Operations    |
| **REQUIRES_NEW**  | Suspend Old + Create New | Create New              | Audit Logs, Error Logs    |
| **MANDATORY**     | Join Existing            | Exception ❌             | Payment Processing Step   |
| **NEVER**         | Exception ❌              | Run Normally            | Notifications, SMS, Email |
| **NOT_SUPPORTED** | Suspend Existing         | Run Normally            | Recommendations, Reports  |
| **SUPPORTS**      | Join Existing            | Run Without Transaction | Read Operations           |

placeOrder()

T1 Start
│
├── saveOrder()                 [REQUIRED]
│      → T1 join
│
├── getCustomerDetails()        [SUPPORTS]
│      → T1 join
│      (T1 na hota to normal run karta)
│
├── logAudit()                  [REQUIRES_NEW]
│      │
│      ├── Suspend T1
│      ├── T2 Start
│      ├── saveAudit()
│      ├── T2 Commit ✅
│      └── Resume T1
│
└── RuntimeException

T1 Rollback ❌



---

# PART 4 — TRANSACTION ISOLATION (All 4 Practical Levels + Default)

## 4.1 What is Isolation?

> Isolation controls **visibility of uncommitted/changed data** between concurrent transactions — i.e., what one transaction "sees" while another transaction is in progress.

---

## 4.2 ISOLATION.DEFAULT
- Falls back to whatever isolation level is configured at the database level (e.g., MySQL InnoDB default = `REPEATABLE_READ`, PostgreSQL default = `READ_COMMITTED`).
- Can be checked via: `SELECT @@transaction_isolation;` (MySQL)

---

## 4.3 ISOLATION.READ_UNCOMMITTED

### Definition
> Allows reading **uncommitted (dirty)** data from other in-progress transactions.

### The Problem: Dirty Read

**Real-world analogy (Movie Tickets):**
- 50 tickets available.
- Rahul (Transaction A) starts booking 45 tickets — UPDATEs the count to 5, but hasn't committed yet.
- You and your partner (Transaction B) check availability — see **5 tickets** (Rahul's uncommitted change).
- Rahul changes his mind and **rolls back** — actual count goes back to 50.
- But you already saw "5" and may have made a decision based on stale/incorrect data → **data inconsistency**.

### Proof from logs
```
Transaction A: updating stock 50 → 5 (NOT COMMITTED YET, sleeping 5 sec)
Transaction B: reads stock = 5   ← DIRTY READ (uncommitted value)
Transaction A: rolls back → DB value reverts to 50
[Transaction B already used the value "5" — inconsistent with final DB state]
```

### A. Interview Answer
> "READ_UNCOMMITTED is the lowest isolation level — it allows a transaction to read data that another transaction has modified but not yet committed. This causes 'dirty reads' — if the writing transaction later rolls back, the reading transaction has already acted on data that never actually existed in the database. It's rarely used in production due to this severe consistency risk."

### B. Cross Questions
1. When (if ever) would READ_UNCOMMITTED be acceptable?
2. Which databases even support this level?

### C. Strong Answers
1. Extremely rare — maybe for rough analytics/dashboards where approximate, non-critical numbers are acceptable and performance (no locking) matters more than accuracy.
2. MySQL InnoDB supports it but discourages it; some DBs (like PostgreSQL) treat READ_UNCOMMITTED the same as READ_COMMITTED internally.

---

## 4.4 ISOLATION.READ_COMMITTED

### Definition
> A transaction can only read data that has been **committed** by other transactions — eliminates dirty reads.

### How it solves the problem
```
Transaction A: updating stock 50 → 5 (NOT COMMITTED YET, sleeping)
Transaction B: reads stock = 50   ← gets LAST COMMITTED value, not the in-progress "5"
Transaction A: commits → stock now = 5
[Transaction B got a clean, consistent value — no dirty read]
```

### The Remaining Problem: Non-Repeatable Read
If Transaction B reads the value **twice** during its lifetime, and Transaction A commits an update in between:
- B's 1st read: 50
- A commits: stock = 10
- B's 2nd read: 10 ← **different value within the same transaction!**

This inconsistency *within a single transaction's lifetime* is called a **non-repeatable read**.

### A. Interview Answer
> "READ_COMMITTED ensures a transaction only ever sees committed data from other transactions — solving the dirty read problem. However, it doesn't guarantee that re-reading the same row twice within the same transaction returns the same value, because another transaction could commit a change in between — this is called a 'non-repeatable read'."

### B. Cross Questions
1. Is READ_COMMITTED good enough for most applications?
2. What's the default isolation in PostgreSQL vs MySQL?

### C. Strong Answers
1. Yes — it's the **most commonly used** isolation level in production (default for PostgreSQL, Oracle, SQL Server) — good balance between consistency and performance.
2. PostgreSQL/Oracle/SQL Server default = READ_COMMITTED. MySQL InnoDB default = REPEATABLE_READ.

---

## 4.5 ISOLATION.REPEATABLE_READ

### Definition
> If a transaction reads the same row multiple times, it will see the **same data** every time — even if another transaction modifies and commits changes to that row in between.

### How it solves Non-Repeatable Read
```
Transaction B: 1st read → stock = 40 (sleeps 3 sec)
Transaction A: updates stock 40 → 5, commits
Transaction B: 2nd read → stock = 40  ← SAME as 1st read (consistent snapshot)
```

### What it does NOT solve: Phantom Read
- Repeatable read guarantees the **same rows** return the same values.
- But if Transaction A **inserts a NEW row** matching Transaction B's query criteria (e.g., a new product), Transaction B's *re-execution of a range query* (e.g., "SELECT all products under ₹5000") might suddenly show this new row — a "phantom" that wasn't there before.

### A. Interview Answer
> "REPEATABLE_READ guarantees that if a transaction reads a row multiple times, it always sees the same data — preventing both dirty reads and non-repeatable reads. Internally, the transaction works against a consistent snapshot taken at its start. However, it doesn't prevent 'phantom reads' — new rows inserted by other transactions that match your query's WHERE clause can still appear in subsequent range queries."

### B. Cross Questions
1. What's the difference between non-repeatable read and phantom read?
2. Is REPEATABLE_READ the MySQL default — any implications?

### C. Strong Answers
1. Non-repeatable read = same row, different value on re-read. Phantom read = same query criteria, different SET of rows (new rows appear) on re-execution.
2. Yes, MySQL InnoDB defaults to REPEATABLE_READ — it actually mitigates many phantom-read scenarios too via MVCC (Multi-Version Concurrency Control), unlike the SQL-standard definition, but it's still not 100% phantom-proof for all query types.

---

## 4.6 ISOLATION.SERIALIZABLE

### Definition
> The **strictest** level — transactions execute as if they ran **sequentially, one after another**, even if started concurrently. Achieved via locking.

### How it solves everything
```
Transaction A starts → acquires lock → updates stock, commits → releases lock
Transaction B starts (was waiting) → now reads the CORRECT, fully-updated value
[No dirty read, no non-repeatable read, no phantom read]
```

### Proof from logs
```
Transaction A executes COMPLETELY first (update + commit)
THEN Transaction B executes (read reflects A's committed change)
[Strictly sequential — no interleaving]
```

### Trade-off
- **Pros:** Complete consistency — zero anomalies.
- **Cons:** Major **performance hit** under load — transactions queue up waiting for locks; not suitable for high-throughput systems.

### Real-world example: Seat/Ticket Booking Systems
Flight/movie ticket booking — must use SERIALIZABLE (or equivalent locking like `SELECT ... FOR UPDATE`) to prevent double-booking the same seat.

### A. Interview Answer
> "SERIALIZABLE is the highest isolation level — it guarantees transactions behave as if executed one at a time in sequence, completely eliminating dirty reads, non-repeatable reads, and phantom reads. It achieves this through aggressive locking, which means transactions that would normally run concurrently now queue up — leading to potential performance bottlenecks under high load. It's best reserved for critical, low-concurrency operations like seat/ticket booking where correctness is non-negotiable, even at the cost of throughput."

### B. Cross Questions
1. How does SERIALIZABLE differ from using explicit row locks (`SELECT FOR UPDATE`)?
2. What's a real production scenario where you'd choose SERIALIZABLE despite the performance cost?

### C. Strong Answers
1. `SELECT FOR UPDATE` gives fine-grained, targeted row-level locking only where you need it — SERIALIZABLE applies broad transaction-level guarantees, often locking more than strictly necessary, hence the bigger performance impact.
2. Banking fund transfers, seat allocation systems, inventory for extremely limited high-demand items (flash sales) — where even rare inconsistency is unacceptable.

---

## 4.7 ISOLATION SUMMARY TABLE

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Performance |
|---|---|---|---|---|
| READ_UNCOMMITTED | ❌ Possible | ❌ Possible | ❌ Possible | Fastest |
| READ_COMMITTED | ✅ Prevented | ❌ Possible | ❌ Possible | Fast (most common) |
| REPEATABLE_READ | ✅ Prevented | ✅ Prevented | ❌ Possible | Moderate |
| SERIALIZABLE | ✅ Prevented | ✅ Prevented | ✅ Prevented | Slowest |

**Memory trick:** Each level going down the table "ages" — fixes one more problem but costs more performance. **"Dirty → Non-Repeatable → Phantom → None"** is the order of anomalies eliminated.

---

# MUST REMEMBER FOR 3-YEAR JAVA DEVELOPER INTERVIEW

1. **Q: What does @Transactional do internally?**
   A: Creates a proxy (AOP) that wraps the method with begin/commit/rollback logic via `PlatformTransactionManager`.

2. **Q: Why doesn't @Transactional work on a method called from within the same class?**
   A: Self-invocation bypasses the proxy — call goes directly to the real object, skipping AOP interception.

3. **Q: Does @Transactional roll back on checked exceptions?**
   A: No, by default only unchecked (RuntimeException). Use `rollbackFor = Exception.class` to include checked.

4. **Q: Default propagation and isolation?**
   A: Propagation = REQUIRED. Isolation = DEFAULT (DB-specific; MySQL = REPEATABLE_READ, PostgreSQL = READ_COMMITTED).

5. **Q: Difference between REQUIRED and REQUIRES_NEW?**
   A: REQUIRED joins/reuses existing transaction. REQUIRES_NEW always suspends current and starts a fresh, independent one.

6. **Q: When to use REQUIRES_NEW in real projects?**
   A: Audit logs, failure tracking — operations that must persist regardless of the main transaction's outcome.

7. **Q: Difference between MANDATORY and NEVER?**
   A: MANDATORY requires an existing transaction (throws if absent). NEVER requires NO existing transaction (throws if present).

8. **Q: Why doesn't NESTED propagation work with Spring Data JPA?**
   A: Requires savepoints, which Hibernate/JPA dialects don't support — works only with plain JDBC + `DataSourceTransactionManager`.

9. **Q: What's a dirty read?**
   A: Reading another transaction's uncommitted (possibly-to-be-rolled-back) data.

10. **Q: What's a non-repeatable read?**
    A: Re-reading the same row within a transaction gives different values because another transaction committed a change in between.

11. **Q: What's a phantom read?**
    A: Re-running the same query returns a different SET of rows (new rows appear/disappear) due to concurrent inserts/deletes.

12. **Q: Which isolation level eliminates all three anomalies?**
    A: SERIALIZABLE — but at the cost of performance (fully sequential execution via locking).

13. **Q: `@Transactional(readOnly = true)` — what's its purpose?**
    A: Hints Hibernate to skip dirty-checking/flush operations for read-only methods — performance optimization. Throws error if you try to write.

14. **Q: `@Transactional` on a `private` method?**
    A: Ignored — CGLIB proxies can't override private methods.

---

# 90% INTERVIEW REVISION NOTES (Quick Recall)

## Transaction Basics
- Transaction = ACID unit — all DB operations succeed together or fail together.
- Spring annotation: `@Transactional` (from `org.springframework.transaction.annotation`, NOT `jakarta.transaction`).
- Default: `propagation = REQUIRED`, `isolation = DEFAULT`, `readOnly = false`.
- Rollback default: only on **unchecked** exceptions.

## AOP/Proxy Mechanics — "PRSC" memory trick
- **P**roxy wraps the bean.
- **R**eal method called only after `PlatformTransactionManager.getTransaction()`.
- **S**elf-invocation = no proxy = annotation ignored.
- **C**ommit on success / Rollback on RuntimeException.

## Propagation — "RRMNNS-N" memory trick (7 types)
| Code | Name | One-liner |
|---|---|---|
| R | REQUIRED | Join or create (default) |
| R | REQUIRES_NEW | Always new, suspend old (audit logs) |
| M | MANDATORY | Must have existing, else throw |
| N | NEVER | Must have NONE, else throw (notifications) |
| N | NOT_SUPPORTED | Run without txn, suspend if exists (recommendations) |
| S | SUPPORTS | Join if exists, else run without (flexible) |
| N | NESTED | Savepoint child — ❌ not supported in JPA |

**Memory phrase:** *"Required Requires Mandatory Never Not-supports Supports Nested"* → "**R-R-M-N-N-S-N**"

## Isolation — "DRRS" memory trick (4 levels + default)
| Level | Prevents | Real Risk |
|---|---|---|
| READ_UNCOMMITTED | Nothing | Dirty Read (movie ticket Rahul example) |
| READ_COMMITTED | Dirty Read | Non-Repeatable Read |
| REPEATABLE_READ | + Non-Repeatable Read | Phantom Read |
| SERIALIZABLE | + Phantom Read | Performance (locks, sequential) |

**Memory phrase:** *"D**irty** disappears at Read Committed, **N**on-repeatable disappears at Repeatable Read, **P**hantom disappears at Serializable"* → **D-N-P** elimination order.

## Key Production Traps
1. Self-invocation kills `@Transactional` silently.
2. Private/final methods → `@Transactional` ignored.
3. Checked exceptions don't roll back by default — must set `rollbackFor`.
4. `REQUIRES_NEW` overuse → connection pool exhaustion.
5. NESTED doesn't work with Hibernate/JPA — use `REQUIRES_NEW` + try-catch instead.
6. SERIALIZABLE = correctness over performance — only for critical low-concurrency ops (ticket/seat booking, fund transfer).
7. `@Async` + `@Transactional` on same method = transaction context does NOT propagate to new thread.

---

# SENIOR-LEVEL / PRODUCTION SCENARIO DISCUSSION

## Debugging Scenario: "My @Transactional isn't rolling back!"
**Checklist to walk through in interview:**
1. Is the method called via the proxy (external bean call) or self-invocation? → Fix with separate bean or `AopContext.currentProxy()`.
2. Is the exception checked or unchecked? → Add `rollbackFor = Exception.class` if checked.
3. Is the method `private`? → Make it `public`/`protected`.
4. Is the exception being caught and swallowed somewhere in the call chain without rethrowing? → Rollback only happens if exception propagates out of the proxied method.
5. Is `readOnly = true` mistakenly set on a write method? → Causes Hibernate to skip flush — looks like "rollback" but is actually "never wrote."

## Production Scenario: Microservices & Transactions
- `@Transactional` works **within a single JVM/DB connection** — it CANNOT span across microservices (Order Service DB + Inventory Service DB are different databases/connections).
- For cross-service consistency, use the **Saga pattern** (orchestration or choreography) — each service commits its own local transaction, and compensating transactions undo prior steps if a later step fails.
- This directly connects propagation concepts (REQUIRES_NEW for independent audit/compensation logs) to distributed system design — a strong senior-level talking point.

## Real Production Example: E-commerce Order Flow (Full Picture)
```
@Transactional (REQUIRED) placeOrder()
 ├── saveOrder()                    [REQUIRED — joins]
 ├── updateInventoryStock()         [REQUIRED — joins; if fails, rolls back saveOrder too]
 ├── auditLogHandler.log(...)       [REQUIRES_NEW — persists regardless]
 ├── validatePayment(order)         [MANDATORY — must be inside placeOrder's txn]
 └── (after txn completes)
       ├── sendOrderConfirmation()  [NEVER — runs outside any transaction]
       └── getRecommendations()     [NOT_SUPPORTED — pure read, suspends txn if any]
```
This single diagram covers REQUIRED, REQUIRES_NEW, MANDATORY, NEVER, and NOT_SUPPORTED in one coherent real-world flow — exactly how a senior interviewer expects you to connect theory to architecture.