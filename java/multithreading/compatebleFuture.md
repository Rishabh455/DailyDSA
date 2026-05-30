# CompletableFuture - Interview Notes

## What is CompletableFuture?

CompletableFuture is an advanced asynchronous programming API introduced in Java 8.

It is an enhanced version of Future that supports:

* Non-blocking execution
* Callback processing
* Task chaining
* Exception handling
* Combining multiple asynchronous tasks

---

# Future vs CompletableFuture

| Future                 | CompletableFuture         |
| ---------------------- | ------------------------- |
| Java 5                 | Java 8                    |
| Blocking               | Non-Blocking              |
| Limited Features       | Rich Async API            |
| No Chaining            | Supports Chaining         |
| Basic Async Processing | Advanced Async Processing |

---

# Why CompletableFuture Over Future?

Problem with Future:

```java id="9y3vcv"
Future<String> future =
        executor.submit(task);

String result =
        future.get();
```

Issue:

* get() blocks the calling thread.
* Asynchronous benefit is reduced.

Interview Answer:

CompletableFuture provides non-blocking processing and callback-based execution, unlike Future which often requires blocking through get().

---

# Most Important Methods

## supplyAsync()

Used when a result is required.

```java id="1kr2si"
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> {
        return "SUCCESS";
    });
```

Equivalent to:

```text id="g7r2hv"
Callable + Future
```

---

## runAsync()

Used when no result is required.

```java id="7x8e6z"
CompletableFuture.runAsync(() -> {
    sendEmail();
});
```

Equivalent to:

```text id="g7wmmz"
Runnable
```

---

## thenApply()

Transforms the result.

```java id="3pql1k"
CompletableFuture<String> future =
    CompletableFuture
        .supplyAsync(() -> "Rishabh")
        .thenApply(name ->
                "Hello " + name);
```

---

## thenAccept()

Consumes the result.

```java id="hzw4uk"
future.thenAccept(System.out::println);
```

---

## exceptionally()

Handles exceptions.

```java id="mivfzt"
CompletableFuture<String> future =
    CompletableFuture
        .supplyAsync(() -> {
            throw new RuntimeException();
        })
        .exceptionally(ex -> "FAILED");
```

---

# Project Mapping

## New User Onboarding Application

Used for:

* Asynchronous Email Notifications

```java id="c7rkhj"
CompletableFuture.runAsync(() -> {
    emailService.sendWelcomeEmail(user);
});
```

Reason:

* No return value required
* Fire-and-forget operation

---

## Password Management Application

Used for:

* Password Reset Processing
* AD Update
* Audit Logging
* Exception Handling

```java id="kk6j2j"
CompletableFuture<PasswordStatus> future =
    CompletableFuture.supplyAsync(() -> {

        updatePasswordInAD(user);

        auditRepository.saveSuccess(user);

        return PasswordStatus.SUCCESS;
    });
```

Reason:

* Result required
* Exception handling required
* Better than Future.get() based approach

---

# Runnable vs Callable vs CompletableFuture

| Feature            | Runnable | Callable | CompletableFuture |
| ------------------ | -------- | -------- | ----------------- |
| Return Value       | No       | Yes      | Yes               |
| Checked Exceptions | No       | Yes      | Yes               |
| Async Processing   | Yes      | Yes      | Yes               |
| Chaining Support   | No       | No       | Yes               |
| Callback Support   | No       | No       | Yes               |
| Java Version       | 1.0      | 5        | 8                 |

---

# Most Important Interview Questions

## Why use CompletableFuture instead of Future?

CompletableFuture supports non-blocking execution, callbacks, chaining, exception handling, and combining multiple asynchronous tasks.

---

## What is the difference between supplyAsync() and runAsync()?

supplyAsync() returns a result.

runAsync() does not return a result.

---

## Does CompletableFuture require ExecutorService?

No.

By default it uses ForkJoinPool.commonPool().

Custom ExecutorService can also be provided.

---

## Can CompletableFuture handle exceptions?

Yes.

Using:

```java id="dxuvtq"
exceptionally()
handle()
whenComplete()
```

---

# One-Line Interview Summary

CompletableFuture is the preferred modern Java asynchronous programming API because it provides non-blocking execution, task chaining, callback processing, and exception handling, making it more powerful than Future.
