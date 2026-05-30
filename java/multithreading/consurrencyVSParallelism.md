# Concurrency vs Parallelism - Interview Notes

## Concurrency

Concurrency means handling multiple tasks at the same time by switching between them.

The tasks may not actually run simultaneously.

Example:

```text
Task A
  |
  v
Task B
  |
  v
Task A
  |
  v
Task B
```

Single CPU Core:

```text
CPU
 |
 +--> Task A
 |
 +--> Task B
```

The CPU rapidly switches between tasks.

### Real-Life Example

One person cooking food while also answering phone calls.

The person is doing multiple tasks, but not exactly at the same instant.

---

## Parallelism

Parallelism means executing multiple tasks at the same time.

Requires multiple CPU cores.

Example:

```text
Core 1 --> Task A

Core 2 --> Task B
```

Both tasks execute simultaneously.

### Real-Life Example

Two people cooking in the same kitchen.

Both tasks happen at the same time.

---

# Key Difference

| Concurrency                | Parallelism                             |
| -------------------------- | --------------------------------------- |
| Multiple tasks in progress | Multiple tasks executing simultaneously |
| May use a single CPU core  | Requires multiple CPU cores             |
| Context Switching          | True Simultaneous Execution             |
| Improves Responsiveness    | Improves Performance                    |
| Focus on Managing Tasks    | Focus on Speed                          |

---

# Java Examples

## Concurrency Example

```java
ExecutorService executor =
        Executors.newFixedThreadPool(2);

executor.submit(task1);
executor.submit(task2);
```

Tasks are managed concurrently.

---

## Parallelism Example

```java
ForkJoinPool pool =
        new ForkJoinPool();
```

Tasks can execute simultaneously across multiple CPU cores.

---

# Project Mapping

## New User Onboarding Application

Used Concurrency

Reason:

```text
User Creation
Email Notification
```

Email runs asynchronously while the main request continues.

ExecutorService provides concurrency.

---

## Password Management Application

Used Concurrency

Reason:

```text
Password Reset Request 1
Password Reset Request 2
Password Reset Request 3
```

Multiple requests are processed concurrently using ExecutorService.

---

## Did We Use Parallelism?

No.

Reason:

The workload was mostly:

* AD Calls
* Database Operations
* Network Communication

These are I/O-bound operations.

ExecutorService concurrency was sufficient.

---

# Most Important Interview Questions

## What is Concurrency?

Concurrency is the ability to handle multiple tasks by making progress on each task without necessarily executing them simultaneously.

---

## What is Parallelism?

Parallelism is the execution of multiple tasks simultaneously using multiple CPU cores.

---

## Can Concurrency Exist Without Parallelism?

Yes.

A single-core CPU can run multiple tasks concurrently using context switching.

---

## Can Parallelism Exist Without Concurrency?

No.

Parallel execution is a subset of concurrent execution.

---

## ExecutorService Provides What?

Primarily Concurrency.

---

## ForkJoin Framework Provides What?

Parallelism using multiple CPU cores and work-stealing.

---

# Interview One-Liner

Concurrency is about dealing with multiple tasks at the same time, whereas parallelism is about executing multiple tasks at the exact same time using multiple CPU cores.
