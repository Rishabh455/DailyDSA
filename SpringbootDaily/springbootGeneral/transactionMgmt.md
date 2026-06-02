For a **3-year Spring Boot developer**, transactions par interview usually 3 levels mein questions poochta hai:

# Level 1: Basic

---

## 1. What is a transaction?

A transaction is a group of database operations executed as a single unit.

Example:

```java
withdrawMoney();
depositMoney();
```

Either both succeed or both fail.

---

## 2. What are ACID properties?

**Atomicity**

* All or nothing

**Consistency**

* Database remains valid

**Isolation**

* Transactions don't interfere

**Durability**

* Once committed, data persists

---

## 3. What is @Transactional?

```java
@Transactional
public void transferMoney() {
}
```

Spring automatically:

```text
Start Transaction
Execute Method
Commit / Rollback
```

---

## 4. Which layer should have @Transactional?

Usually:

```java
@Service
```

Not Controller.

Not Repository (in most cases).

---

# Level 2: Internal Working

---

## 5. How does @Transactional work internally?

Interview favorite.

Answer:

```text
Spring AOP
+
Proxy Pattern
+
TransactionInterceptor
+
PlatformTransactionManager
```

Flow:

```text
Controller
   |
Proxy
   |
Service Method
```

Proxy starts transaction before method execution.

---

## 6. Who actually commits the transaction?

```java
PlatformTransactionManager
```

Examples:

```java
JpaTransactionManager
DataSourceTransactionManager
```

---

## 7. Does Spring modify my code?

No.

Spring creates a proxy around your bean.

---

## 8. Why must the class be a Spring Bean?

Because Spring creates the proxy.

Without bean:

```java
new UserService();
```

No proxy.

No transaction.

---

# Level 3: Rollback Questions

---

## 9. Which exceptions trigger rollback?

Default:

```java
RuntimeException
Error
```

---

## 10. Does checked exception rollback?

```java
throw new IOException();
```

No.

Transaction commits.

---

## 11. How to rollback checked exceptions?

```java
@Transactional(
    rollbackFor = Exception.class
)
```

---

## 12. noRollbackFor?

```java
@Transactional(
    noRollbackFor = RuntimeException.class
)
```

Even RuntimeException won't rollback.

---

# Level 4: Proxy Questions (VERY IMPORTANT)

---

## 13. Why doesn't @Transactional work on private methods?

```java
@Transactional
private void save() {
}
```

Proxy cannot intercept private methods.

---

## 14. Does @Transactional work on static methods?

No.

Proxy cannot intercept static methods.

---

## 15. Self Invocation Problem

```java
public void methodA() {
    methodB();
}
```

```java
@Transactional
public void methodB() {
}
```

Proxy bypassed.

Transaction never starts.

---

## 16. How to solve self invocation?

Option 1:

```java
Move method to another service
```

Option 2:

```java
Self Injection
```

---

# Level 5: Propagation (MOST ASKED)

---

## 17. What is Propagation?

Defines behavior when one transactional method calls another transactional method.

---

## 18. REQUIRED

Default.

```java
@Transactional
```

Join existing transaction.

Create new if none exists.

---

## 19. REQUIRES_NEW

Suspends current transaction.

Creates completely new transaction.

---

### Scenario

```java
saveOrder();
saveAuditLog();
```

Order fails.

Audit should still save.

Use:

```java
REQUIRES_NEW
```

---

## 20. SUPPORTS

Uses transaction if available.

Otherwise runs normally.

---

## 21. MANDATORY

Existing transaction required.

Otherwise:

```text
IllegalTransactionStateException
```

---

## 22. NEVER

Must not run inside transaction.

Throws exception if transaction exists.

---

## 23. NOT_SUPPORTED

Suspends transaction.

Runs without transaction.

---

## 24. NESTED

Uses savepoints.

Partial rollback possible.

---

# Level 6: Isolation Level Questions

---

## 25. What is Dirty Read?

Transaction B reads uncommitted data from Transaction A.

---

## 26. What is Non Repeatable Read?

Same row gives different values during same transaction.

---

## 27. What is Phantom Read?

New rows appear between queries.

---

## 28. Default Isolation Level in MySQL?

```text
REPEATABLE_READ
```

---

## 29. Default Isolation Level in PostgreSQL?

```text
READ_COMMITTED
```

---

## 30. Highest Isolation Level?

```text
SERIALIZABLE
```

Most safe.

Slowest.

---

# Level 7: Scenario Based Questions

---

## 31. Payment Success, Order Save Failed. What happens?

Inside same transaction:

```text
Rollback everything
```

---

## 32. Order Saved, Email Failed. Should order rollback?

Usually:

```text
No
```

Email is secondary.

Use:

```java
REQUIRES_NEW
```

or

```java
@Async
```

---

## 33. Why shouldn't long-running operations be inside transactions?

Example:

```java
API Call
File Upload
Email
```

Transaction remains open.

Locks held longer.

Performance issue.

---

# Level 8: Advanced Questions

---

## 34. What happens if transaction method calls another transaction method?

Depends on propagation.

Most common:

```java
REQUIRED
```

Jo transaction chal raha hai usi mein join karega.

---

## 35. Can transactions work across multiple databases?

Single DB:

```text
Easy
```

Multiple DB:

```text
Distributed Transaction
JTA
XA Transaction
```

Rarely used nowadays.

---

## 36. What is Transaction Boundary?

Usually Service Layer.

```java
Controller
   |
Service (@Transactional)
   |
Repository
```

---

## 37. What is readOnly=true?

```java
@Transactional(readOnly = true)
```

Used for:

```text
Search
Reports
GET APIs
```

Optimizations possible.

---

## 38. Can readOnly transaction update data?

Technically yes in many databases.

But should never do it.

Interview answer:

```text
It is intended only for read operations.
```

---

# Level 9: Super Tricky Questions

---

## 39. Why does transaction not work if I create object manually?

```java
UserService service = new UserService();
```

Because Spring Proxy not involved.

---

## 40. Why does transaction not work in constructor?

Proxy not fully initialized.

---

## 41. What if transaction method is final?

With CGLIB:

```java
final methods cannot be overridden
```

Proxy cannot intercept.

Transaction won't work.

---

## 42. Difference between REQUIRED and REQUIRES_NEW?

### REQUIRED

```text
Join existing transaction
```

### REQUIRES_NEW

```text
Suspend existing transaction
Start brand new transaction
```

---

## 43. What happens if outer transaction rolls back but inner REQUIRES_NEW commits?

```text
Inner commit stays.
Outer rollback happens.
```

Very common audit-log interview question.

---

# Ultimate Interview Answer

If interviewer asks:

**"Explain @Transactional internally."**

Answer:

> Spring implements transactions using AOP proxies. When a transactional method is called through the proxy, Spring's TransactionInterceptor starts a transaction using PlatformTransactionManager. After method execution, Spring commits the transaction if successful or rolls it back if an exception occurs. By default, rollback happens for RuntimeException and Error. Internal method calls (self-invocation), private methods, static methods, and manually created objects bypass the proxy, so transactions do not work in those cases.

Agar tum ye 43 questions confidently answer kar pao, to 3-5 years Spring Boot interviews mein transaction topic almost fully covered maan sakte ho.
