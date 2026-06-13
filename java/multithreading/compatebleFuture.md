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
-----------------------------------------------------------------------------------------------------------
Ek simple real-world example lete hain: **Order Processing System**

* `supplyAsync()` → Non-blocking execution
* `thenApply()` → Task chaining
* `thenAccept()` → Callback processing
* `exceptionally()` → Exception handling
* `thenCombine()` → Combining multiple async tasks

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureDemo {

    public static void main(String[] args) {

        // Async Task 1: Fetch User
        CompletableFuture<String> userFuture =
                CompletableFuture.supplyAsync(() -> {
                    sleep(2000);
                    System.out.println("Fetching User...");
                    return "Rishabh";
                });

        // Async Task 2: Fetch Order
        CompletableFuture<String> orderFuture =
                CompletableFuture.supplyAsync(() -> {
                    sleep(3000);
                    System.out.println("Fetching Order...");
                    return "Laptop";
                });

        // Task Chaining + Exception Handling
        CompletableFuture<String> processedUser =
                userFuture
                        .thenApply(user -> {
                            System.out.println("Processing User...");
                            return user.toUpperCase();
                        })
                        .exceptionally(ex -> {
                            System.out.println("Error: " + ex.getMessage());
                            return "DEFAULT_USER";
                        });

        // Combine Multiple Async Tasks
        CompletableFuture<String> finalResult =
                processedUser.thenCombine(orderFuture,
                        (user, order) ->
                                "User: " + user + " ordered " + order);

        // Callback Processing
        finalResult.thenAccept(System.out::println);

        System.out.println("Main thread is free...");

        // Wait only for demo
        finalResult.join();
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### Execution Flow

```text
Main Thread
     |
     |---- supplyAsync(User) ---------
     |                                |
     |---- supplyAsync(Order) ------  |
                                      |
Fetching User...                      |
     |                                |
thenApply() -> RISHABH               |
                                      |
Fetching Order... --------------------
                                      |
thenCombine()
(User + Order)
      |
thenAccept()
      |
Print Result
```

### Mapping to Features

| Feature                | Code              |
| ---------------------- | ----------------- |
| Non-blocking Execution | `supplyAsync()`   |
| Task Chaining          | `thenApply()`     |
| Callback Processing    | `thenAccept()`    |
| Exception Handling     | `exceptionally()` |
| Combining Tasks        | `thenCombine()`   |

Output:

```text
Main thread is free...
Fetching User...
Processing User...
Fetching Order...
User: RISHABH ordered Laptop
```

Interview me agar koi puche **"Why CompletableFuture over Future?"** to ek line:

> `Future` sirf result la sakta hai (`get()` se blocking), jabki `CompletableFuture` result aane ke baad automatically next task execute, combine, callback aur exception handling kar sakta hai bina main thread ko block kiye.
