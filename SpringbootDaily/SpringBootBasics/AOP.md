# Spring AOP Interview Notes (3-5 Years Experience)

# What is AOP?

AOP (Aspect Oriented Programming) is a programming paradigm used to separate cross-cutting concerns from business logic.

Cross-cutting concerns:

* Logging
* Security
* Transactions
* Caching
* Auditing
* Validation
* Monitoring

Without AOP:

```java
public void saveEmployee() {

    log();

    startTransaction();

    businessLogic();

    commitTransaction();
}
```

Business code becomes polluted.

With AOP:

```java
saveEmployee();
```

Logging and transaction handling happen automatically through aspects.

---

# Why AOP?

Typical Spring Application:

```text
Controller Layer
       ↓
Service Layer
       ↓
Repository Layer
```

Common concerns needed in all layers:

```text
Logging
Security
Auditing
Monitoring
Transactions
```

Instead of writing them everywhere, use AOP.

---

# How Spring AOP Works?

```text
Client
   |
   V
Proxy Object
   |
   V
Target Object
```

Flow:

```text
Before Advice
      ↓
Target Method
      ↓
After Advice
```

Spring AOP is Proxy-Based.

---

# Core AOP Terminologies

## Aspect

Class containing cross-cutting logic.

```java
@Aspect
@Component
public class LoggingAspect {
}
```

Examples:

```text
LoggingAspect
SecurityAspect
TransactionAspect
```

---

## Advice

Actual code executed at a join point.

Examples:

```java
@Before
@After
@AfterReturning
@AfterThrowing
@Around
```

---

## Join Point

Point where interception can occur.

Example:

```java
saveEmployee()
updateEmployee()
deleteEmployee()
```

In Spring AOP:

```text
Method Execution
```

is the primary join point.

---

## Pointcut

Expression deciding where advice applies.

```java
execution(* com.app.service.*.*(..))
```

Meaning:

```text
All methods
inside service package
```

---

# Advice Types

## @Before

Runs before method execution.

```java
@Before("execution(* com.app.service.*.*(..))")
public void logBefore() {
}
```

Use Cases:

* Logging
* Validation
* Security Checks

Flow:

```text
Before Advice
      ↓
Method
```

---

## @After

Runs regardless of success or failure.

Equivalent to:

```java
finally {}
```

Flow:

```text
Method
   ↓
After Advice
```

Use Case:

```text
Resource Cleanup
```

---

## @AfterReturning

Runs only when method completes successfully.

```java
@AfterReturning(
    value = "execution(* save(..))",
    returning = "employee"
)
```

Example:

```text
Log Success
Audit Success
```

Flow:

```text
Method Success
      ↓
AfterReturning
```

---

## @AfterThrowing

Runs only when exception occurs.

```java
@AfterThrowing(
    value = "execution(* save(..))",
    throwing = "ex"
)
```

Use Cases:

```text
Error Logging
Alerting
Monitoring
```

Flow:

```text
Method Exception
      ↓
AfterThrowing
```

---

# @Around (MOST IMPORTANT)

Interview Favorite.

Most powerful advice.

Can:

* Execute before method
* Execute after method
* Modify arguments
* Modify return value
* Handle exceptions
* Skip method execution completely

---

## Syntax

```java
@Around("execution(* com.app.service.*.*(..))")
public Object aroundAdvice(
    ProceedingJoinPoint pjp
) throws Throwable {

    return pjp.proceed();
}
```

---

# What is ProceedingJoinPoint?

Used only with:

```java
@Around
```

It represents the target method.

Actual execution happens here:

```java
pjp.proceed();
```

---

# Interview Trap #1

What happens if proceed() is NOT called?

```java
@Around(...)
public Object advice(
    ProceedingJoinPoint pjp
) {

    return "Blocked";
}
```

Target method never executes.

Flow:

```text
Client
   ↓
Around Advice
   ↓
Return
```

Method skipped.

Perfectly legal.

---

# Real Example

Security Check

```java
@Around(...)
public Object check(
    ProceedingJoinPoint pjp
) throws Throwable {

    if(!authorized){
        return "Access Denied";
    }

    return pjp.proceed();
}
```

---

# Interview Trap #2

Can proceed() be called multiple times?

YES.

Example:

```java
pjp.proceed();

pjp.proceed();
```

Target method executes twice.

Use Cases:

```text
Retry Logic
Performance Testing
```

Flow:

```text
Method
Method Again
```

---

# Interview Trap #3

Can Around Advice modify method arguments?

YES.

Example:

```java
Object[] args = pjp.getArgs();

args[0] = "Dummy User";

return pjp.proceed(args);
```

Original argument replaced.

---

# Interview Trap #4

Can Around Advice modify return value?

YES.

Example:

```java
Object result = pjp.proceed();

return "Modified Response";
```

Caller receives:

```text
Modified Response
```

not original result.

---

# Interview Trap #5

Can Around Advice handle exceptions?

YES.

```java
try {
    return pjp.proceed();
}
catch(Exception e) {
    return "Fallback Response";
}
```

Common in:

```text
Circuit Breakers
Fallback Logic
Retry Logic
```

---

# Interview Trap #6

Difference Between @After and @AfterReturning

@After:

```text
Runs Always
```

Success:

```text
YES
```

Exception:

```text
YES
```

---

@AfterReturning:

```text
Runs Only On Success
```

Success:

```text
YES
```

Exception:

```text
NO
```

---

# Interview Trap #7

Difference Between @AfterThrowing and @After

@AfterThrowing:

```text
Only On Exception
```

@After:

```text
Always Executes
```

Like finally block.

---

# Real Logging Aspect Example

```java
@Aspect
@Component
public class LoggingAspect {

    @Before(
      "execution(* com.app.service.*.*(..))"
    )
    public void before() {

        System.out.println(
            "Method Started"
        );
    }

    @After(
      "execution(* com.app.service.*.*(..))"
    )
    public void after() {

        System.out.println(
            "Method Finished"
        );
    }
}
```

---

# Most Asked Proxy Question

Why does AOP sometimes not work?

### Self Invocation Problem

```java
@Service
public class UserService {

    public void methodA() {

        methodB();
    }

    @Transactional
    public void methodB() {
    }
}
```

Call:

```java
methodA();
```

Transaction does NOT start.

Reason:

```text
Internal call
bypasses proxy
```

Proxy only intercepts:

```text
External Calls
```

---

# Features Built On AOP

```java
@Transactional
@Cacheable
@Retryable
@Async
@PreAuthorize
@Secured
```

All use proxies internally.

---

# What Does NOT Use AOP?

```java
@ControllerAdvice
@ExceptionHandler
DispatcherServlet
```

These use Spring MVC infrastructure.

Not AOP.

---

# 30-Second Interview Answer

"AOP is a programming paradigm used to separate cross-cutting concerns such as logging, security, transactions, and caching from business logic. Spring AOP is proxy-based, where Spring creates a proxy object around the target bean and intercepts method calls. Core concepts are Aspect, Advice, Pointcut, and JoinPoint. The most powerful advice is @Around because it can execute code before and after a method, modify arguments, modify return values, handle exceptions, or even prevent method execution. Common interview topics include self-invocation issues, proceed() behavior, and how @Transactional internally relies on AOP proxies."
