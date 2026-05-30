# ForkJoin Framework - Interview Notes

## What is ForkJoin Framework?

ForkJoin Framework was introduced in Java 7 for parallel processing.

It follows the Divide and Conquer approach.

A large task is divided into smaller subtasks, processed in parallel, and then combined to produce the final result.

---

# Why ForkJoin Framework?

Traditional Thread Pool:

```text
Task
 |
 v
Single Worker Thread
```

ForkJoin Framework:

```text
Large Task
     |
     v
Split into Subtasks
     |
     v
Process in Parallel
     |
     v
Combine Results
```

Result:

* Better CPU Utilization
* Faster Processing
* Improved Parallelism

---

# Core Components

## ForkJoinPool

Pool that manages worker threads.

```java
ForkJoinPool pool =
        new ForkJoinPool();
```

Interview Answer:

ForkJoinPool is a specialized thread pool designed for divide-and-conquer parallel processing.

---

## ForkJoinTask

Base class for all ForkJoin tasks.

Two important subclasses:

```text
RecursiveTask
RecursiveAction
```

---

## RecursiveTask

Used when a result needs to be returned.

```java
class SumTask
extends RecursiveTask<Integer>
```

Example:

```java
protected Integer compute() {
    return result;
}
```

---

## RecursiveAction

Used when no result is required.

```java
class PrintTask
extends RecursiveAction
```

Example:

```java
protected void compute() {
    // perform action
}
```

---

# Fork vs Join

## fork()

Splits task and submits it for execution.

```java
leftTask.fork();
```

Meaning:

```text
Execute this task asynchronously
```

---

## join()

Waits for result.

```java
leftTask.join();
```

Meaning:

```text
Wait and collect result
```

---

# Simple Example

```java
class SumTask extends RecursiveTask<Integer> {

    private int start;
    private int end;

    public SumTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {

        if(end - start <= 10) {

            int sum = 0;

            for(int i=start;i<=end;i++) {
                sum += i;
            }

            return sum;
        }

        int mid = (start + end) / 2;

        SumTask left =
            new SumTask(start, mid);

        SumTask right =
            new SumTask(mid + 1, end);

        left.fork();

        return right.compute()
                + left.join();
    }
}
```

Execution:

```java
ForkJoinPool pool =
        new ForkJoinPool();

int result =
        pool.invoke(
            new SumTask(1,100));
```

---

# Work Stealing Algorithm

Most Important Interview Question

Interview Answer:

Each worker thread maintains its own queue.

If one thread becomes idle, it steals tasks from another busy thread's queue.

This improves CPU utilization and load balancing.

Example:

```text
Thread-1 -> 20 Tasks
Thread-2 -> 0 Tasks

Thread-2 steals work
from Thread-1
```

This is called Work Stealing.

---

# ForkJoinPool vs ExecutorService

| ForkJoinPool        | ExecutorService  |
| ------------------- | ---------------- |
| Divide & Conquer    | Task Execution   |
| Work Stealing       | No Work Stealing |
| CPU Intensive Tasks | General Purpose  |
| Parallel Algorithms | Async Tasks      |

---

# When To Use ForkJoin Framework?

Use For:

* Large Array Processing
* Sorting Algorithms
* Searching Algorithms
* Recursive Problems
* Parallel Computation
* Data Processing

Examples:

* Merge Sort
* Quick Sort
* File Processing
* Big Data Calculations

---

# When NOT To Use ForkJoin Framework?

Do Not Use For:

* Database Calls
* REST API Calls
* Email Sending
* Kafka Processing
* Network Operations

Reason:

ForkJoin is designed for CPU-bound tasks, not I/O-bound tasks.

---

# Project Mapping

## New User Onboarding Application

NOT USED

Reason:

Email sending is an I/O operation.

ExecutorService is better.

---

## Password Management Application

NOT USED

Reason:

AD updates and database operations are I/O-bound tasks.

ExecutorService is the correct choice.

---

# Most Important Interview Questions

## What is ForkJoin Framework?

ForkJoin Framework is a parallel processing framework introduced in Java 7 that uses divide-and-conquer and work-stealing algorithms to improve performance.

---

## What is Work Stealing?

Idle threads steal tasks from busy threads to improve resource utilization.

---

## Difference Between RecursiveTask and RecursiveAction?

RecursiveTask returns a result.

RecursiveAction does not return a result.

---

## Difference Between ForkJoinPool and ExecutorService?

ForkJoinPool is optimized for CPU-intensive divide-and-conquer tasks using work stealing.

ExecutorService is a general-purpose thread pool for asynchronous task execution.

---

## Is ForkJoinPool Better Than ExecutorService?

No.

Choice depends on use case.

CPU-bound → ForkJoinPool

I/O-bound → ExecutorService

---

## Why Not Use ForkJoin Framework In Your Projects?

Both password reset and email notification workflows are I/O-bound operations involving AD, database, SMTP, and network calls.

ExecutorService is more suitable than ForkJoin Framework.

---

# 30-Second Interview Summary

ForkJoin Framework is a Java 7 parallel processing framework that uses divide-and-conquer and work stealing. It is best suited for CPU-intensive computations such as sorting, searching, and large data processing. For I/O-bound operations like password resets, database calls, email notifications, and REST APIs, ExecutorService is generally the better choice.
