# Exception Handling in Multithreading - Interview Notes
------------------------------------------------------
Restro analogy to understand the flow

Ye sab ko ek hi smooth restaurant story me yaad kar:

```text
Restaurant Owner
       |
       v
ExecutorService (Manager)
       |
       +----------------------------+
       |                            |
       v                            v

Runnable                    Callable
(Work Only)               (Work + Result)

"Water serve karo"        "Pizza banao aur result batao"

No result needed          Result needed
```

---

# Step 1: Runnable

```text
Waiter:
"Table 5 ko water serve kar do"
```

Kitchen:

```text
Theek hai, kar deta hu.
```

Bas kaam ho gaya.

Koi result nahi.

```text
Runnable
=
Work Only
=
Fire and Forget
```

### Exception

Glass toot gaya.

```text
Kitchen khud handle karega.
```

Isliye:

```text
Runnable
→ try-catch inside task
```

Main thread ko automatically pata nahi chalega. 

---

# Step 2: Callable

Ab waiter bolta hai:

```text
Pizza banao aur mujhe batao ready hua ya nahi.
```

Ab result chahiye.

```text
Callable
=
Work + Result
```

Return:

```text
PIZZA READY
SUCCESS
FAILED
```

### Exception

Pizza banate waqt oven kharab.

```text
Exception immediately nahi milega.
```

Woh Future me pack ho jayega. 

---

# Step 3: Future

Waiter ko token mila:

```text
Token #101
```

Ye token hi:

```text
Future
```

hai.

Baad me waiter poochta hai:

```text
Mera order ready hua?
```

Ye hai:

```java
future.get()
```

---

### Agar Success

```text
Pizza Ready
```

mil jayega.

---

### Agar Failure

```text
Future bolega:

ExecutionException
```

Andar original reason chhupa hoga:

```java
ex.getCause()
```

```text
ExecutionException
=
Wrapper Exception
```



---

# Step 4: CompletableFuture

Normal Future:

```text
Har baar counter pe jaake poochna padega:

"Mera pizza ready hua?"
```

---

CompletableFuture:

```text
Pizza ready ho jaaye
to mujhe phone kar dena.
```

Smart Future.

```text
CompletableFuture
=
Future + Automation
```

Can:

```text
Run Async
Return Result
Chain Tasks
Handle Errors
```



---

# Exception Handling in CompletableFuture

## exceptionally()

```text
Pizza fail ho gaya?
To burger de do.
```

Fallback.

```text
exceptionally()
=
Fallback Value
```

---

## handle()

```text
Success hua?
Failure hua?

Dono cases me handle karo.
```

```text
handle()
=
Success + Failure Both
```



---

# One Complete Memory Map

```text
Runnable
↓
Work Only

Callable
↓
Work + Result

Future
↓
Token For Result

future.get()
↓
Ask For Result

ExecutionException
↓
Task Failed

CompletableFuture
↓
Smart Future

exceptionally()
↓
Fallback

handle()
↓
Success + Failure Both
```

# 30-Second Interview Revision

```text
Runnable
→ Run task only

Callable
→ Run task and return result

Future
→ Holds result of Callable

future.get()
→ Retrieves result

ExecutionException
→ Wrapper exception from failed Callable

CompletableFuture
→ Advanced Future with async processing

exceptionally()
→ Fallback on failure

handle()
→ Handle both success and failure
```

Ye sequence yaad rakho:

```text
Runnable → Callable → Future → CompletableFuture
```

Aur samjho:

```text
Kaam → Kaam+Result → Result Token → Smart Result Token
```



# Why Normal try-catch is Not Enough?

In multithreading, exceptions occurring inside worker threads do not automatically propagate to the main thread.

Example:

```java id="ig57so"
new Thread(() -> {

    throw new RuntimeException(
            "Something went wrong");

}).start();
```

The exception occurs in the worker thread.

Main thread is unaware of it.

---

# 1. Exception Handling with Runnable

```java id="oz3v9u"
executor.submit(() -> {

    try {

        updatePasswordInAD(user);

    } catch(Exception ex) {

        logger.error(
            "Password reset failed",
            ex);
    }
});
```

### Interview Answer

For Runnable tasks, exceptions should be caught and handled inside the task because Runnable does not return a result.

---

# 2. Exception Handling with Callable

```java id="52vd9m"
Callable<String> task = () -> {

    if(true) {
        throw new RuntimeException(
                "AD Connection Failed");
    }

    return "SUCCESS";
};
```

Submit:

```java id="g7m9eh"
Future<String> future =
        executor.submit(task);
```

Retrieve:

```java id="3rvynq"
try {

    future.get();

} catch(ExecutionException ex) {

    System.out.println(
        ex.getCause().getMessage());
}
```

### Interview Answer

Exceptions thrown from Callable are wrapped inside ExecutionException and can be retrieved through Future.get().

---

# 3. Future Exception Handling

```java id="30n9ij"
try {

    Future<String> future =
            executor.submit(task);

    future.get();

} catch(ExecutionException ex) {

    logger.error(
            "Task Failed",
            ex.getCause());
}
```

Most Common Interview Question.

---

# 4. UncaughtExceptionHandler

Used to catch uncaught exceptions from threads.

```java id="n9owls"
Thread thread =
        new Thread(task);

thread.setUncaughtExceptionHandler(
        (t, ex) -> {

            System.out.println(
                "Exception: "
                + ex.getMessage());
        });
```

### Interview Answer

UncaughtExceptionHandler provides a centralized mechanism to handle uncaught exceptions from worker threads.

---

# 5. CompletableFuture Exception Handling

## exceptionally()

```java id="gdb5fo"
CompletableFuture<String> future =
        CompletableFuture
                .supplyAsync(() -> {

                    throw new RuntimeException(
                            "AD Failure");

                })
                .exceptionally(ex ->
                        "FAILED");
```

---

## handle()

```java id="0jq11m"
future.handle((result, ex) -> {

    if(ex != null) {

        return "FAILED";
    }

    return result;
});
```

---

# Project Mapping

## New User Onboarding Application

Email Notification

```java id="37bl4u"
executor.submit(() -> {

    try {

        emailService
                .sendWelcomeEmail(user);

    } catch(Exception ex) {

        logger.error(
            "Email failed",
            ex);
    }
});
```

### Why?

Email failure should not impact user onboarding.

---

## Password Management Application

```java id="m3sv7g"
Callable<PasswordResetResult> task = () -> {

    try {

        updatePasswordInAD(user);

        auditRepository
                .saveSuccess(user);

        return SUCCESS;

    } catch(Exception ex) {

        auditRepository
                .saveFailure(
                        user,
                        ex.getMessage());

        return FAILED;
    }
};
```

### Why?

Need:

* Failure Tracking
* Audit Logging
* AD Error Handling
* Monitoring

---

# Most Important Interview Questions

## Can exceptions from one thread affect another thread?

No.

Exception in one thread does not stop other threads.

---

## How are exceptions handled in Runnable?

Using try-catch inside the task.

---

## How are exceptions handled in Callable?

Through Future.get() which throws ExecutionException.

---

## What is ExecutionException?

A wrapper exception thrown by Future.get() when the underlying task fails.

---

## What is UncaughtExceptionHandler?

A handler used to catch uncaught exceptions from threads.

---

## How do you handle exceptions in ExecutorService?

Use:

* try-catch inside task
* Future.get()
* Logging
* Monitoring

---

## How do you handle exceptions in CompletableFuture?

Use:

```java id="p2c6gp"
exceptionally()
handle()
whenComplete()
```

---

# Interview One-Liner

In multithreading, exceptions do not automatically propagate to the main thread. Runnable tasks typically use try-catch blocks, Callable exceptions are retrieved through Future.get() as ExecutionException, and CompletableFuture provides methods such as exceptionally() and handle() for asynchronous exception handling.
------------------------------------------------------------------------