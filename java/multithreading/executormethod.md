# ExecutorService Methods - Interview Notes

## 1. submit()

Most commonly used method.

Used to submit Runnable or Callable tasks.

```java
Future<String> future =
        executor.submit(callableTask);
```

```java
Future<?> future =
        executor.submit(runnableTask);
```

### Interview Answer

submit() is used to execute asynchronous tasks and returns a Future object that can be used to track task completion or retrieve results.

### Project Usage

#### New User Onboarding

```java
executor.submit(() -> {
    emailService.sendWelcomeEmail(user);
});
```

#### Password Management

```java
Future<PasswordResetResult> future =
        executor.submit(callableTask);
```

---

## 2. execute()

Used to execute Runnable tasks.

```java
executor.execute(runnableTask);
```

Returns:

```java
void
```

### Interview Answer

execute() executes a Runnable task but does not return any Future object.

### Difference Between execute() and submit()

| execute()          | submit()                 |
| ------------------ | ------------------------ |
| Returns Nothing    | Returns Future           |
| Runnable Only      | Runnable + Callable      |
| No Result Tracking | Result Tracking Possible |

### Interview Preferred Answer

If task completion tracking is required, use submit(). Otherwise execute() is sufficient.

---

## 3. shutdown()

Gracefully shuts down ExecutorService.

```java
executor.shutdown();
```

### What Happens?

* New tasks are rejected
* Existing tasks continue execution
* Running tasks complete normally

### Interview Answer

shutdown() allows currently running tasks to complete while preventing new task submissions.

---

## 4. shutdownNow()

Forcefully shuts down ExecutorService.

```java
executor.shutdownNow();
```

### What Happens?

* Attempts to interrupt running tasks
* Removes waiting tasks from queue

### Interview Answer

shutdownNow() should be used carefully because running tasks may be interrupted before completion.

---

## 5. awaitTermination()

Waits for thread pool termination.

```java
executor.shutdown();

executor.awaitTermination(
        60,
        TimeUnit.SECONDS);
```

### Interview Answer

Used after shutdown() when we want to wait for all tasks to complete before application exits.

---

## 6. isShutdown()

Checks whether shutdown() was called.

```java
executor.isShutdown();
```

Returns:

```java
true / false
```

---

## 7. isTerminated()

Checks whether all tasks have completed after shutdown.

```java
executor.isTerminated();
```

Returns:

```java
true / false
```

---

## 8. invokeAll()

Executes multiple Callable tasks.

```java
List<Future<String>> futures =
        executor.invokeAll(tasks);
```

### Interview Use Case

Batch Processing

Examples:

* Generate multiple reports
* Process multiple files
* Bulk data processing

---

## 9. invokeAny()

Executes multiple Callable tasks.

Returns first successful result.

```java
String result =
        executor.invokeAny(tasks);
```

### Interview Use Case

Multiple service calls where first successful response is sufficient.

---

# Most Important Methods For Interviews

Focus on these:

```java
submit()
execute()
shutdown()
shutdownNow()
awaitTermination()
```

These cover almost 90% of ExecutorService interview discussions.

---

# Project Mapping

## New User Onboarding Application

Used:

```java
Executors.newFixedThreadPool(10)

submit(Runnable)
```

Purpose:

* Asynchronous email notifications
* Fire-and-forget processing

---

## Password Management Application

Used:

```java
Executors.newFixedThreadPool(20)

submit(Callable)
```

Purpose:

* Password reset processing
* AD status tracking
* Audit logging
* Exception handling

---

# One-Line Interview Summary

submit() is the most commonly used ExecutorService method because it supports both Runnable and Callable and returns a Future for tracking asynchronous task execution.
