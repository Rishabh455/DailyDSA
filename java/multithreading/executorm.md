# ExecutorService, Runnable, Callable & Future - Interview Notes

# 1. What is ExecutorService?

ExecutorService is a Java framework used to execute asynchronous tasks using a pool of reusable threads.

Instead of creating threads manually using:

```java
new Thread(() -> {
    // task
}).start();
```

we use:

```java
ExecutorService executor =
        Executors.newFixedThreadPool(20);
```

Benefits:

* Thread Reuse
* Better Performance
* Controlled Concurrency
* Easy Lifecycle Management
* Easy Shutdown

---

# 2. What is a Thread Pool?

A thread pool is a collection of pre-created worker threads that execute submitted tasks.

Example:

```text
Thread Pool Size = 3

Request1 -> Thread1
Request2 -> Thread2
Request3 -> Thread3

Request4 -> Waiting Queue
Request5 -> Waiting Queue
```

When a thread becomes free, it picks the next task from the queue.

---

# 3. Why ExecutorService Instead of new Thread()?

Interview Answer:

Creating threads manually is expensive because every thread consumes memory and CPU resources.

ExecutorService reuses existing threads and provides better performance, scalability, and resource management.

---

# 4. Runnable vs Callable vs Future

## Runnable

Used when:

* No result is required
* Fire-and-forget operation

Method:

```java
void run();
```

Example:

```java
Runnable task = () -> {
    sendEmail(user);
};
```

Cannot:

* Return a value
* Throw checked exceptions

---

## Callable

Used when:

* Result is required
* Exception handling is required

Method:

```java
V call() throws Exception;
```

Example:

```java
Callable<String> task = () -> {
    updatePasswordInAD(user);
    return "SUCCESS";
};
```

Can:

* Return value
* Throw checked exceptions

---

## Future

Future represents the result of an asynchronous task.

Example:

```java
Future<String> future =
        executor.submit(task);
```

Retrieve result:

```java
String result = future.get();
```

---

# 5. Runnable vs Callable

| Feature           | Runnable        | Callable    |
| ----------------- | --------------- | ----------- |
| Method            | run()           | call()      |
| Return Value      | No              | Yes         |
| Checked Exception | No              | Yes         |
| Use Case          | Fire-and-forget | Need Result |

---

# 6. Difference Between execute() and submit()

## execute()

```java
executor.execute(runnable);
```

Returns:

```java
Nothing
```

Used when result is not required.

---

## submit()

```java
Future<?> future =
        executor.submit(task);
```

Returns:

```java
Future
```

Supports Runnable and Callable.

---

# 7. Why Not Call Future.get() Immediately?

Bad:

```java
Future<Result> future =
        executor.submit(task);

future.get();
```

Problem:

Main thread blocks until task completes.

Asynchronous benefit is lost.

Interview Answer:

Calling Future.get() immediately blocks the calling thread. We should avoid blocking the request thread when implementing asynchronous processing.

---

# 8. Project Story 1 - New User Onboarding Application

## Problem

After a new user was onboarded, a welcome email needed to be sent.

Email delivery depended on SMTP servers and could take several seconds.

We did not want the onboarding API to wait for email completion.

---

## Solution

We used ExecutorService with Runnable.

Reason:

* No result required
* Fire-and-forget operation
* User creation already completed

---

## Flow

```text
User Registration
      |
      v
Create User
      |
      v
Save User Details
      |
      v
Return Success Response
      |
      v
Background Email Notification
```

---

## Code

```java
ExecutorService executor =
        Executors.newFixedThreadPool(10);

public void onboardUser(User user) {

    createUser(user);

    executor.submit(() -> {
        emailService.sendWelcomeEmail(user);
    });

    return;
}
```

---

## If Email Fails

```java
executor.submit(() -> {

    try {

        emailService.sendWelcomeEmail(user);

    } catch(Exception ex) {

        logger.error(
            "Email failed for user {}",
            user.getId(),
            ex);
    }
});
```

---

## Why Runnable?

Interview Answer:

Email notification was a background activity. We did not need any return value. Therefore Runnable was sufficient.

---

# 9. Project Story 2 - Password Management Application

## Problem

Users reported that password changes were taking approximately 30 minutes before becoming effective.

After investigation we found:

* Password reset requests were already asynchronous
* Thread pool size was too small
* Requests accumulated in the queue during peak load
* Audit tracking queries were slow

---

## Existing Flow

```text
User
  |
  v
Spring Boot Application
  |
  +--> Audit Database
  |
  +--> Active Directory
```

---

## Why Callable?

Password updates could:

* Succeed
* Fail
* Timeout
* Throw AD exceptions

We needed:

* Success/Failure Status
* Audit Logging
* Exception Handling
* Monitoring

Therefore Callable was more appropriate.

---

## Code

```java
ExecutorService executor =
        Executors.newFixedThreadPool(20);

Callable<PasswordResetResult> task = () -> {

    try {

        updatePasswordInAD(user);

        auditRepository.saveSuccess(user);

        return new PasswordResetResult(
                "SUCCESS");

    } catch(Exception e) {

        auditRepository.saveFailure(
                user,
                e.getMessage());

        return new PasswordResetResult(
                "FAILED");
    }
};

Future<PasswordResetResult> future =
        executor.submit(task);
```

---

## Important Note

We did NOT immediately call:

```java
future.get();
```

because that would block the request thread.

Instead, the task executed in the background and updated audit information.

---

## Optimization Done

### Before bb

```java
ExecutorService executor =
        Executors.newFixedThreadPool(2);
```

Large queue buildup.

---

### After

```java
ExecutorService executor =
        Executors.newFixedThreadPool(20);
```

Multiple password reset requests processed concurrently.

---

## Database Optimization

Frequently used query:

```sql
SELECT *
FROM PASSWORD_REQUEST
WHERE STATUS='PENDING';
```

Added index:

```sql
CREATE INDEX IDX_STATUS
ON PASSWORD_REQUEST(STATUS);
```

Result:

* Faster lookup
* Faster request pickup
* Reduced queue waiting time

---

## Active Directory Changes

No changes were made in Active Directory itself.

Optimization was done in the application layer and request processing layer.

---

## Final Result

Password reflection time:

```text
30 Minutes
     ↓
5 Minutes
```

More than 80% improvement.

---

# 10. Most Important Interview Questions

## Why ExecutorService?

To manage reusable thread pools and process requests concurrently.

---

## Why not new Thread()?

Thread creation is expensive and difficult to manage.

---

## Why Callable?

Need result, exception handling, and status tracking.

---

## Why not Runnable in Password Reset?

Runnable cannot return status or easily propagate checked exceptions.

---

## What is Future?

Future is a handle representing the result of an asynchronous task.

---

## Can Future make a task asynchronous?

No.

ExecutorService executes tasks asynchronously.

Future only represents the result.

---

## What happens if Future.get() is called?

The caller blocks until task completion.

---

## What happens when all threads are busy?

New tasks are stored in the waiting queue.

---

## What if tasks arrive faster than processing?

Queue grows and may create memory pressure.

---

## What is RejectedExecutionException?

Occurs when ExecutorService cannot accept more tasks.

---

## Why FixedThreadPool instead of CachedThreadPool?

FixedThreadPool provides controlled resource usage.

CachedThreadPool may create too many threads during traffic spikes.

---

## What happens if shutdown() is not called?

Thread pool remains active and may cause resource leaks.

---

## How would you monitor ExecutorService in production?

* Active Thread Count
* Queue Size
* Completed Task Count
* CPU Usage
* Memory Usage
* Task Execution Time

---

# One-Line Summary

New User Onboarding Application:

* Runnable + ExecutorService
* Fire-and-forget Email Notifications

Password Management Application:

* Callable + ExecutorService + Future
* Password Status Tracking, Audit Logging, Exception Handling
* Password Reflection Time Reduced from 30 Minutes to 5 Minutes
