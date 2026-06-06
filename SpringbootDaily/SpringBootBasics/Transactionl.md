# Why @Transactional Sometimes Does Not Work

## Golden Rule

```text
Client
   ↓
Spring Proxy
   ↓
@Transactional Method
```

Transaction तभी चलेगा जब method call Proxy के through आए।

---

# Problem 1: Self Invocation

## Code

```java
@Service
public class UserService {

    public void methodA() {
        methodB();
    }

    @Transactional
    public void methodB() {
        // DB Operation
    }
}
```

---

## What Developer Thinks

```text
methodA()
   ↓
methodB()
   ↓
Transaction Starts
```

---

## What Actually Happens

```text
Client
   ↓
Proxy
   ↓
methodA()
   ↓
this.methodB()
```

`methodA()` and `methodB()` same object ke andar hain.

Call directly:

```java
this.methodB();
```

ho jati hai.

Proxy bypass ho gaya.

---

## Result

```text
No Proxy
 ↓
No Transaction
```

---

## Why?

Spring Proxy sirf external calls intercept karta hai.

Internal calls intercept nahi kar sakta.

---

## Interview Answer

> Self invocation bypasses the Spring proxy because one method directly calls another method within the same class. Since the proxy is not involved, @Transactional is ignored.

---

# Problem 2: Private Method

## Code

```java
@Transactional
private void saveData() {
}
```

---

## Why It Fails

Proxy method override karke interception karta hai.

Private methods:

```text
Cannot be overridden
```

Therefore:

```text
No Interception
 ↓
No Transaction
```

---

## Interview Answer

> Private methods cannot be proxied by Spring AOP, so @Transactional on a private method has no effect.

---

# Problem 3: Object Created Using new

## Wrong

```java
UserService service =
        new UserService();
```

---

## What Developer Thinks

```text
@Transactional
 ↓
Transaction Starts
```

---

## Actual Reality

```text
new UserService()
```

Spring ke through object create hi nahi hua.

---

## No Spring Container

```text
No Bean
 ↓
No Proxy
 ↓
No Transaction
```

---

## Correct

```java
@Autowired
private UserService service;
```

or

```java
ApplicationContext.getBean(UserService.class);
```

---

## Interview Answer

> @Transactional only works on Spring-managed beans. If an object is created using the new keyword, Spring cannot create a proxy, so transaction management does not work.

---

# Problem 4: Checked Exception

## Code

```java
@Transactional
public void process() throws IOException {

    saveData();

    throw new IOException();
}
```

---

## What Developer Expects

```text
Exception
 ↓
Rollback
```

---

## Actual Result

```text
IOException
 ↓
Transaction COMMIT
```

---

## Why?

By default Spring rolls back only for:

```text
RuntimeException
Error
```

Examples:

```java
NullPointerException
IllegalArgumentException
ArithmeticException
```

---

## Not Rolled Back

```java
IOException
SQLException
Exception
```

These are checked exceptions.

---

## Fix

```java
@Transactional(
    rollbackFor = Exception.class
)
```

or

```java
@Transactional(
    rollbackFor = IOException.class
)
```

---

## Interview Answer

> By default, Spring rolls back transactions only for unchecked exceptions such as RuntimeException and Error. Checked exceptions like IOException do not trigger rollback unless rollbackFor is explicitly configured.

---

# Internal Working of @Transactional

## During Startup

Spring finds:

```java
@Transactional
```

and creates:

```text
Original Bean
      ↓
Proxy Bean
```

---

## During Method Call

```text
Client
   ↓
Proxy
   ↓
Start Transaction
   ↓
Actual Method
   ↓
Commit / Rollback
```

---

## When Does @Transactional Work?

### Must Be True

✅ Spring-managed Bean

✅ Public Method

✅ Call Must Go Through Proxy

✅ Exception Rules Must Match

---

## Memory Trick

```text
No Proxy
   =
No Transaction
```

Every @Transactional interview question ultimately comes back to this one rule.
