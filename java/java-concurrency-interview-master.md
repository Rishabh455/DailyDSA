# Java Concurrency & Multithreading — Interview Master Revision

> **Goal:** Understand the concepts, not memorize definitions.
>
> **How to revise:** Follow the flow `Concurrency → Threads → Shared State → Synchronization → ExecutorService → Futures → Coordination → Concurrent Collections → ForkJoin/Parallelism → Project Mapping → Interview Traps`.

---

# 0. The Master Mental Model

```text
CONCURRENCY
│
├── Multiple tasks are in progress during the same period
│
├── MULTITHREADING
│   └── Multiple threads execute/manage those tasks
│
├── EXECUTION MODEL
│   ├── Single core → interleaving / context switching
│   └── Multiple cores → possible true parallel execution
│
├── SHARED STATE PROBLEM
│   ├── Race condition
│   ├── Visibility problem
│   ├── Atomicity problem
│   └── Ordering problem
│
├── SAFETY / CONTROL
│   ├── synchronized
│   ├── Lock / ReentrantLock
│   ├── volatile
│   ├── AtomicInteger / AtomicReference
│   └── Concurrent collections
│
├── TASK EXECUTION
│   ├── Runnable
│   ├── Callable<V>
│   ├── ExecutorService
│   ├── ThreadPoolExecutor
│   └── Future<V>
│
├── ASYNC COMPOSITION
│   └── CompletableFuture<V>
│
├── THREAD COORDINATION
│   ├── wait / notify / notifyAll
│   ├── BlockingQueue
│   ├── CountDownLatch
│   ├── CyclicBarrier
│   ├── Semaphore
│   └── Phaser
│
└── PARALLEL COMPUTATION
    ├── ForkJoinPool
    ├── RecursiveTask / RecursiveAction
    └── parallelStream
```

## The single most important distinction

```text
Concurrency = multiple tasks are in progress.

Parallelism = multiple tasks are actually executing at the same time.

Context switching = CPU switches from one runnable thread to another.

synchronized / Lock = protects shared mutable state.

ExecutorService = manages and executes tasks using a thread pool.

Future = handle to the result/status of an asynchronous task.

CompletableFuture = Future + asynchronous composition.

ForkJoinPool = specialized execution model for parallel / divide-and-conquer work.
```

---

# 1. Concurrency vs Multithreading vs Parallelism

## What is concurrency?

Concurrency means a system can make progress on multiple tasks during the same period. The tasks do **not** need to execute at the exact same instant.

```text
Time →
Task A: ███    ████
Task B:    ████    ███
```

The CPU can interleave execution using context switching.

## What is multithreading?

Multithreading means using multiple threads within a process so multiple tasks can be progressed concurrently.

```text
Process
│
├── Thread-1
├── Thread-2
├── Thread-3
└── Thread-4
```

Multithreading is a **mechanism** for concurrency. It is not the definition of concurrency.

## What is parallelism?

Parallelism means multiple tasks execute simultaneously, typically on multiple CPU cores.

```text
Core 1 → Task A
Core 2 → Task B
Core 3 → Task C
```

### Interview trap

**Concurrency and parallelism are not synonyms.**

- Concurrency = managing multiple tasks in progress.
- Parallelism = actual simultaneous execution.
- A single-core system can have concurrency without parallel execution.
- Multi-core execution can provide parallelism.

---

# 2. Thread Creation and Thread Lifecycle

## Ways to create/execute work

### A. Extend Thread

```java
class MyThread extends Thread {
    @Override
    public void run() {
        // task
    }
}

new MyThread().start();
```

### B. Implement Runnable

```java
Runnable task = () -> {
    // task
};

new Thread(task).start();
```

`Runnable` is generally preferred over extending `Thread` because it separates **what the task is** from **how the task is executed**.

### C. Callable + ExecutorService

```java
Callable<String> task = () -> "SUCCESS";

ExecutorService executor = Executors.newFixedThreadPool(5);

Future<String> future = executor.submit(task);
```

This is usually the better abstraction for enterprise applications.

---

## `start()` vs `run()`

### `start()`

```java
thread.start();
```

Creates a new thread of execution and causes `run()` to execute on that new thread.

```text
Current Thread
      │
      └── start()
            │
            v
      New Thread
            │
            └── run()
```

### `run()`

```java
thread.run();
```

Just calls a normal method on the current thread.

```text
Current Thread
      │
      └── run()
            │
            └── normal method call
```

### Interview answer

> `start()` creates a new thread and schedules `run()` on that thread. Calling `run()` directly does not create a new thread; it is just a normal method call.

---

## Important trap: starting the same Thread twice

```java
Thread t = new Thread(task);

t.start();
t.start();   // IllegalThreadStateException
```

A `Thread` object can be started only once.

To run the same task again, create a new `Thread` object or submit the task again to an executor.

---

## One Runnable instance can be used by multiple threads

```java
Runnable task = new MyTask();

new Thread(task).start();
new Thread(task).start();
```

The `Runnable` object is shared.

If it contains mutable state:

```java
class MyTask implements Runnable {
    int count = 0;

    @Override
    public void run() {
        count++;
    }
}
```

then the same `count` may be accessed concurrently and requires a proper thread-safety mechanism.

---

# 3. Java Thread States — Important Correction for Interviews

The official `Thread.State` enum has these states:

```text
NEW
RUNNABLE
BLOCKED
WAITING
TIMED_WAITING
TERMINATED
```

Do **not** say that Java exposes a separate `RUNNING` enum state.

For mental understanding, you can say:

```text
NEW
  ↓ start()
RUNNABLE
  ↓ CPU scheduled
executing
  ↓
BLOCKED / WAITING / TIMED_WAITING
  ↓
RUNNABLE
  ↓
TERMINATED
```

But in Java's API, "running" is represented under `RUNNABLE`.

### Typical transitions

- `NEW` → before `start()`
- `RUNNABLE` → eligible to run / may be executing
- `BLOCKED` → waiting to acquire a monitor lock
- `WAITING` → waiting indefinitely, e.g. `wait()` or `join()`
- `TIMED_WAITING` → waiting with a timeout, e.g. `sleep()` or timed `join()`
- `TERMINATED` → `run()` has finished

---

# 4. The Real Problem: Shared Mutable State

Concurrency becomes difficult when multiple threads touch the same mutable data.

```java
int counter = 0;

counter++;
```

Many people think `counter++` is one operation. It is not.

Conceptually:

```text
READ counter
    ↓
ADD 1
    ↓
WRITE counter
```

Two threads can interleave these steps.

```text
counter = 5

T1 reads 5
T2 reads 5

T1 writes 6
T2 writes 6

Expected: 7
Actual:   6
```

This is a **race condition** / lost update.

---

# 5. Three Core Thread-Safety Problems

## 5.1 Atomicity

Atomicity means an operation behaves as one indivisible unit.

```java
counter++;
```

is not atomic.

Possible solutions:

- `synchronized`
- `Lock`
- `AtomicInteger`

---

## 5.2 Visibility

Visibility asks:

> If Thread A changes a value, will Thread B reliably see the new value?

`volatile` can provide visibility for a shared variable.

```java
volatile boolean running = true;
```

But:

```java
volatile int counter;

counter++;
```

does **not** become atomic.

---

## 5.3 Ordering

Ordering asks:

> What execution order is guaranteed between operations in different threads?

Synchronization tools establish ordering relationships through the Java Memory Model.

### Interview trap

`synchronized` does not mean:

> "Thread 1 will always run before Thread 2."

It provides mutual exclusion plus visibility/happens-before guarantees around lock acquisition/release. It does not provide arbitrary execution ordering.

---

# 6. Race Condition

## Definition

A race condition occurs when the result depends on the timing/interleaving of concurrent operations on shared state.

Typical example:

```java
class Counter {
    private int count;

    void increment() {
        count++;
    }
}
```

## Fix 1 — synchronized

```java
public synchronized void increment() {
    count++;
}
```

## Fix 2 — AtomicInteger

```java
AtomicInteger count = new AtomicInteger();

count.incrementAndGet();
```

## Fix 3 — explicit Lock

```java
lock.lock();
try {
    count++;
} finally {
    lock.unlock();
}
```

### When to choose what

```text
Simple shared counter
    → AtomicInteger

Small critical section
    → synchronized

Need timeout / tryLock / interruptible waiting / fairness / Condition
    → ReentrantLock

Complex concurrent data structure
    → Concurrent collections
```

---

# 7. `synchronized`

`synchronized` provides mutual exclusion around a monitor.

```java
synchronized(lock) {
    // critical section
}
```

Only one thread at a time can own that monitor.

## Where can it be used?

### Synchronized method

```java
public synchronized void update() {
    // critical section
}
```

For an instance method, the monitor is the current object (`this`).

### Synchronized static method

```java
public static synchronized void update() {
    // critical section
}
```

The monitor is the `Class` object.

### Synchronized block

```java
synchronized(lockObject) {
    // critical section
}
```

The block form is usually better when you want to protect only a small part.

---

## Advantages

- Simple
- Built into Java
- Automatic lock release when leaving the synchronized region
- Reentrant

## Limitations

- No `tryLock()`
- No explicit timeout
- No `lockInterruptibly()`
- No explicit fairness configuration
- Contention can reduce throughput
- Poor lock design can still cause deadlock

### Interview trap

`synchronized` does **not** automatically prevent all concurrency problems.

It prevents simultaneous entry into the same monitor-protected critical section, but the overall design can still have:

- deadlock
- starvation
- poor throughput
- bad lock granularity
- incorrect condition logic

---

# 8. ReentrantLock

```java
ReentrantLock lock = new ReentrantLock();
```

Usage:

```java
lock.lock();

try {
    // critical section
} finally {
    lock.unlock();
}
```

## Why "reentrant"?

The same thread can acquire the same lock multiple times without deadlocking itself.

```text
Thread T
  ↓
acquires lock
  ↓
calls method B
  ↓
acquires same lock again
  ↓
allowed
```

## Why use ReentrantLock instead of synchronized?

Because it gives explicit control.

### `tryLock()`

```java
if (lock.tryLock()) {
    try {
        // got lock
    } finally {
        lock.unlock();
    }
}
```

### Timed attempt

```java
lock.tryLock(5, TimeUnit.SECONDS);
```

### Interruptible acquisition

```java
lock.lockInterruptibly();
```

### Fair lock

```java
new ReentrantLock(true);
```

### Conditions

```java
Condition condition = lock.newCondition();
```

Useful when you need multiple independent waiting conditions.

---

# 9. `volatile`

```java
volatile boolean shutdown = false;
```

## What volatile gives

- visibility across threads
- ordering guarantees needed for volatile access

## What volatile does NOT give

- compound-operation atomicity
- mutual exclusion

This is unsafe:

```java
volatile int count = 0;

count++; // still not atomic
```

### Interview answer

> `volatile` is mainly a visibility and ordering mechanism. It does not make compound operations such as `count++` atomic.

Use `AtomicInteger` or locking for atomic updates.

---

# 10. Atomic Classes and CAS

Important classes:

```text
AtomicInteger
AtomicLong
AtomicBoolean
AtomicReference
```

Example:

```java
AtomicInteger counter = new AtomicInteger();

counter.incrementAndGet();
```

## CAS — Compare And Swap

Conceptually:

```text
Expected = old value
New      = updated value

If current == Expected
    update to New
else
    retry / fail
```

This lets simple state updates happen without a traditional mutual-exclusion lock.

### Important limitation

Atomic classes are excellent for isolated atomic state transitions.

They are not a replacement for locking an arbitrary multi-step business operation.

Example:

```text
check balance
  ↓
deduct money
  ↓
update audit
  ↓
publish result
```

If all of those must be one consistent critical section, one `AtomicInteger` is not enough.

---

# 11. Runnable vs Callable

## Runnable

```java
@FunctionalInterface
public interface Runnable {
    void run();
}
```

Use when:

- no result is needed
- task is fire-and-forget
- simple background work

Example:

```java
Runnable task = () -> {
    sendEmail();
};
```

## Callable<V>

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

Use when:

- a result is required
- checked exceptions may need to be propagated through task execution
- task outcome matters

Example:

```java
Callable<String> task = () -> {
    updatePassword();
    return "SUCCESS";
};
```

## Interview table

| Feature | Runnable | Callable |
|---|---|---|
| Method | `run()` | `call()` |
| Result | No | Yes |
| Checked exception | Not declared | Yes |
| Typical use | fire-and-forget | result-producing task |
| Executor submission | `execute()` / `submit()` | `submit()` |

### Trap

A `Runnable` submitted through `submit()` can return a `Future<?>`, but there is still no useful result from `run()`; successful completion generally yields `null`.

---

# 12. ExecutorService — The Main Task Execution Abstraction

Instead of doing this repeatedly:

```java
new Thread(task).start();
```

use a thread pool:

```java
ExecutorService executor =
    Executors.newFixedThreadPool(10);
```

## Why?

Because thread creation is not free.

ExecutorService gives:

- thread reuse
- bounded/constrained concurrency when configured appropriately
- queueing
- lifecycle management
- task submission
- task completion tracking
- controlled shutdown

### Mental model

```text
                ExecutorService
                     │
          ┌──────────┴──────────┐
          ↓                     ↓
     Worker Threads          Task Queue
       T1 T2 T3                 │
          │                     │
          └────────────┬────────┘
                       ↓
                Execute tasks
```

---

# 13. Thread Pool

A thread pool is a group of worker threads that repeatedly execute submitted tasks.

Example:

```text
Pool size = 3

Task 1 → Worker 1
Task 2 → Worker 2
Task 3 → Worker 3
Task 4 → Queue
Task 5 → Queue
```

When a worker becomes free:

```text
Worker 2 completes
       ↓
takes Task 4
```

## Why pool instead of a new thread per task?

```text
new Thread per request
    ↓
many thread objects
    ↓
memory + scheduling overhead
    ↓
poor control under load
```

Thread pool:

```text
fixed/reasonable workers
      +
queue
      +
task reuse
```

---

# 14. Common Executor Factories

## `newFixedThreadPool(n)`

```java
Executors.newFixedThreadPool(10);
```

Good mental model:

- fixed worker count
- tasks wait in a queue
- useful when you want controlled concurrency

**Important trap:** the standard fixed thread pool uses an unbounded `LinkedBlockingQueue`. A huge backlog can therefore create memory pressure. "Fixed" controls thread count, not total queued work.

---

## `newCachedThreadPool()`

- creates/reuses threads as needed
- good for many short-lived asynchronous tasks in suitable workloads
- can create a large number of threads under sustained load

### Trap

Do not describe cached pool as "always faster."

---

## `newSingleThreadExecutor()`

One worker thread:

```text
Task A → T1
Task B → T1
Task C → T1
```

Useful when you need tasks processed one at a time.

This gives serialized task execution through one worker, not general parallelism.

---

## `newScheduledThreadPool(n)`

For:

- delayed tasks
- periodic tasks

```java
ScheduledExecutorService scheduler =
    Executors.newScheduledThreadPool(2);
```

---

# 15. ExecutorService Core Methods

## `execute(Runnable)`

```java
executor.execute(task);
```

- Runnable only
- returns `void`
- no Future for tracking

Use when you only need task execution.

---

## `submit(...)`

```java
Future<?> future = executor.submit(runnable);

Future<String> future = executor.submit(callable);
```

Supports:

- Runnable
- Callable

Returns:

- Future

### Interview answer

> `submit()` is preferred when I need task completion/result tracking, cancellation, or exception retrieval through the Future.

---

## `shutdown()`

```java
executor.shutdown();
```

Meaning:

```text
Stop accepting new tasks
+
allow already submitted tasks to finish
```

---

## `shutdownNow()`

```java
executor.shutdownNow();
```

Attempts to:

- interrupt running tasks
- return tasks that were waiting in the queue

### Critical trap

"Forcefully shuts down" does **not** mean the JVM can magically kill arbitrary running code.

Interruption is cooperative.

A task must respond properly to interruption.

---

## `awaitTermination()`

```java
executor.shutdown();

executor.awaitTermination(
    60, TimeUnit.SECONDS);
```

Used when you need to wait for pool termination.

Typical lifecycle:

```text
shutdown()
    ↓
wait using awaitTermination()
    ↓
if still not terminated
    ↓
possibly shutdownNow()
```

---

## `isShutdown()`

Checks whether shutdown has been initiated.

## `isTerminated()`

Checks whether shutdown has happened **and** all tasks have completed.

### Trap

`isShutdown() == true` does NOT necessarily mean every task is finished.

---

# 16. `invokeAll()` vs `invokeAny()`

## `invokeAll()`

```java
List<Future<String>> futures =
    executor.invokeAll(tasks);
```

- accepts multiple Callable tasks
- waits for the batch
- returns Futures for the submitted tasks

Typical use:

```text
Generate many reports
Process many files
Run multiple independent calculations
```

## `invokeAny()`

```java
String result =
    executor.invokeAny(tasks);
```

It returns when one task successfully produces a result; other unfinished tasks are cancelled by the executor contract.

Typical use:

```text
Call multiple equivalent services
↓
take first successful response
```

---

# 17. Future

`Future<V>` represents the pending/completed result of an asynchronous computation.

```java
Future<String> future =
    executor.submit(callable);
```

## Core methods

```java
future.get();
future.get(2, TimeUnit.SECONDS);

future.isDone();
future.isCancelled();

future.cancel(true);
future.cancel(false);
```

## What `Future` does

- tracks completion
- retrieves result
- supports timeout
- supports cancellation
- exposes status

## What `Future` does NOT do

A `Future` does not itself execute the task.

```text
ExecutorService
    ↓
executes task

Future
    ↓
represents the task's result/state
```

### Very important interview trap

```java
Future<String> f = executor.submit(task);
String result = f.get();
```

If you immediately call `get()`, the **calling thread blocks** until the task finishes.

So:

```text
submit()
+
immediate get()
=
asynchronous task execution
+
synchronous waiting
```

You may still have a worker thread doing the task, but the caller is blocked.

---

# 18. Future Exceptions

For a `Callable`:

```java
Callable<String> task = () -> {
    throw new RuntimeException("AD failed");
};
```

Then:

```java
Future<String> future =
    executor.submit(task);

try {
    future.get();
} catch (ExecutionException e) {
    Throwable cause = e.getCause();
}
```

### Why `ExecutionException`?

The exception thrown by the task is surfaced through `Future.get()` as an `ExecutionException` wrapper.

Also remember:

```text
future.get()
    ↓
may throw
├── InterruptedException
├── ExecutionException
└── TimeoutException (for timed get)
```

---

# 19. `FutureTask`

`FutureTask<V>` is both:

```text
Runnable
+
Future<V>
```

Useful when you want a task object that can be:

- executed
- queried for result
- cancelled
- tracked

Conceptually:

```java
FutureTask<String> task =
    new FutureTask<>(callable);

executor.execute(task);

String result = task.get();
```

Interview trap:

> Do not confuse `Future` with `FutureTask`. `Future` is an interface; `FutureTask` is a concrete implementation that can act as a Runnable and hold a Future result.

---

# 20. CompletableFuture

`CompletableFuture<V>` extends the Future model and supports asynchronous composition.

Think:

```text
Future
  ↓
"Give me the result when it's ready."

CompletableFuture
  ↓
"When it is ready, automatically run the next stage."
```

## Why it is powerful

- async execution
- chaining
- combining
- error handling
- timeout handling
- non-blocking composition
- explicit executor support

---

# 21. Core CompletableFuture Methods

## Start work

### `runAsync`

No result:

```java
CompletableFuture<Void> f =
    CompletableFuture.runAsync(() -> {
        sendEmail();
    });
```

### `supplyAsync`

Produces a result:

```java
CompletableFuture<String> f =
    CompletableFuture.supplyAsync(() -> {
        return "SUCCESS";
    });
```

---

## Transform a result

### `thenApply`

```java
future.thenApply(result -> result.toUpperCase());
```

Think:

```text
A → transform → B
```

### `thenApplyAsync`

Runs the next stage asynchronously.

You can also pass an explicit executor:

```java
future.thenApplyAsync(
    value -> transform(value),
    executor
);
```

---

## Consume a result without changing it

### `thenAccept`

```java
future.thenAccept(result -> {
    log(result);
});
```

Returns `CompletableFuture<Void>`.

---

## Run something after completion without consuming the result

### `thenRun`

```java
future.thenRun(() -> {
    audit();
});
```

---

## Combine dependent stages

### `thenCompose`

Use when stage B depends on the result of stage A and B itself returns another CompletableFuture.

```text
A → Future<B>
         ↓ flatten
       Future<B>
```

Mental model:

> `thenCompose` avoids nested futures.

---

## Combine independent stages

### `thenCombine`

```text
Future<A> + Future<B>
        ↓
     combine
        ↓
      Future<C>
```

Use when both asynchronous operations are independent and the final result needs both.

---

## Wait for all

### `allOf`

```java
CompletableFuture.allOf(f1, f2, f3);
```

Completes when all supplied futures complete.

### Trap

`allOf()` returns `CompletableFuture<Void>`. It does not directly give you a typed `List<T>` result. You collect results yourself.

---

## First completion

### `anyOf`

```java
CompletableFuture.anyOf(f1, f2, f3);
```

Completes when one supplied future completes.

### Trap

`anyOf` is about **first completion**, not necessarily first successful business result.

---

# 22. CompletableFuture Error Handling

## `exceptionally`

Use when you want a fallback value after failure.

```java
future.exceptionally(ex -> {
    log.error("Failed", ex);
    return "FALLBACK";
});
```

Mental model:

```text
Failure → fallback
```

---

## `handle`

Receives both result and exception.

```java
future.handle((result, ex) -> {
    if (ex != null) {
        return "FAILED";
    }
    return result;
});
```

Mental model:

```text
Success ─┐
         ├→ handle()
Failure ─┘
```

---

## `whenComplete`

Use mainly for side effects such as:

- logging
- metrics
- auditing

It observes completion but generally keeps the original result/exception flowing.

---

## Timeout helpers

Useful modern methods include:

```java
orTimeout(...)
completeOnTimeout(...)
```

Mental model:

```text
orTimeout
    → fail if timeout occurs

completeOnTimeout
    → return fallback/default on timeout
```

---

# 23. `get()` vs `join()` in CompletableFuture

`get()`:

- blocking
- checked exceptions

`join()`:

- blocking
- wraps failures in `CompletionException`
- no checked exception declaration

### Trap

Calling `join()` everywhere is still blocking. CompletableFuture is not magically non-blocking if you keep calling terminal blocking methods.

---

# 24. Exception Handling Mental Map

```text
Runnable
   │
   └── task-level try/catch is often needed

Callable
   │
   └── exception captured by Future
             ↓
         future.get()
             ↓
      ExecutionException

CompletableFuture
   │
   ├── exceptionally()
   ├── handle()
   ├── whenComplete()
   └── timeout/fallback methods

Raw Thread
   │
   └── UncaughtExceptionHandler for uncaught exceptions
```

### Project-style principle

A background task should usually:

- log failures
- update business/audit status when required
- preserve interruption
- avoid silently swallowing exceptions

---

# 25. Thread Pool Internals — ThreadPoolExecutor

At interview depth, understand this flow:

```text
                submit(task)
                    │
                    v
          Is worker count < corePoolSize?
              /                  \
            YES                   NO
             │                     │
             v                     v
      create/activate         enqueue task
         worker                    │
                                   v
                    queue capacity available?
                              /             \
                            YES              NO
                             │                │
                             v                v
                         wait in queue    worker count
                                         < maxPoolSize?
                                            /      \
                                          YES       NO
                                           │         │
                                           v         v
                                       add worker   reject
```

This is the mental model that matters.

## Main parameters

```text
corePoolSize
maximumPoolSize
keepAliveTime
BlockingQueue
ThreadFactory
RejectedExecutionHandler
```

### Interview trap

With an unbounded queue, `maximumPoolSize` may not be reached in the way candidates expect because tasks keep entering the queue.

This is one reason bounded queues are often important in production designs.

---

# 26. Rejection

When the executor cannot accept more work, the `RejectedExecutionHandler` decides what happens.

Common policies:

- `AbortPolicy` → throw `RejectedExecutionException`
- `CallerRunsPolicy` → caller thread runs the task
- `DiscardPolicy` → silently discard
- `DiscardOldestPolicy` → discard oldest queued task, then retry submission

### Interview insight

A rejection policy is not just an API detail. It is part of your **backpressure strategy**.

---

# 27. Thread Pool Sizing

There is no universal magic number.

Think based on workload:

## CPU-bound

Examples:

- calculation
- compression
- parsing
- CPU-heavy transformations

Too many threads can increase context switching and hurt performance.

A pool near available CPU parallelism is a common starting point.

## I/O-bound

Examples:

- database calls
- REST calls
- file I/O
- AD/LDAP
- SMTP

Threads can spend time waiting on I/O, so more concurrency may be useful.

But too many threads can still overload:

- DB connection pool
- downstream service
- memory
- network
- CPU

### Interview answer

> I would size a pool based on workload characteristics and downstream capacity, then validate with production metrics rather than choosing an arbitrary number.

---

# 28. Queueing Is Part of the Design

```text
Workers busy
    ↓
new tasks
    ↓
queue
    ↓
queue grows
    ↓
latency grows
    ↓
memory/backpressure/rejection risk
```

### Important distinction

Increasing thread count is not always the solution.

If a database can handle only 20 concurrent requests and you create 200 worker threads, you may simply move the bottleneck to the database.

---

# 29. Cancellation and Interruption

`Thread.interrupt()` does not forcibly kill a thread.

It is a **request** for interruption.

A task must cooperate.

Typical blocking APIs may respond by throwing `InterruptedException`.

### Good pattern

```java
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

### Interview trap

Do not silently swallow `InterruptedException`.

---

# 30. `wait()`, `notify()`, `notifyAll()`

These are thread communication mechanisms tied to an object's monitor.

## `wait()`

```java
synchronized(lock) {
    lock.wait();
}
```

The current thread:

1. releases the monitor
2. enters waiting state
3. waits to be notified / interrupted / otherwise resumed

## `notify()`

```java
synchronized(lock) {
    lock.notify();
}
```

Wakes one waiting thread.

## `notifyAll()`

```java
synchronized(lock) {
    lock.notifyAll();
}
```

Wakes all waiting threads associated with that monitor.

### Critical trap

`notifyAll()` does **not** mean all threads immediately run.

They wake and then compete for the monitor.

---

# 31. Why must wait/notify be inside synchronized?

Because the thread must own the target object's monitor when invoking them.

Otherwise:

```text
IllegalMonitorStateException
```

## Why methods are in Object, not Thread

Because waiting/notification is associated with an **object monitor**.

```text
Object
  └── monitor
       ├── wait()
       ├── notify()
       └── notifyAll()
```

---

# 32. `wait()` vs `sleep()`

| `wait()` | `sleep()` |
|---|---|
| `Object` method | `Thread` method |
| Used for coordination | Used for delay |
| Releases the monitor when waiting | Does not release a monitor |
| Must be called while owning the relevant monitor | No synchronized requirement |
| Can be indefinite/timed | Always time-based |

### Interview trap

If a thread sleeps while holding a lock, other threads may remain blocked on that lock.

---

# 33. Always Use a Condition Loop with wait()

Do not write:

```java
if (queue.isEmpty()) {
    queue.wait();
}
```

Prefer:

```java
while (queue.isEmpty()) {
    queue.wait();
}
```

Why?

- conditions can change before the thread gets the lock again
- waiting can resume without your logical condition being true
- `while` re-checks the business condition safely

This is a common interview trap.

---

# 34. BlockingQueue — Better Producer/Consumer Abstraction

Instead of manually combining:

```text
synchronized
+
wait()
+
notify()
```

a `BlockingQueue` often gives a cleaner producer/consumer design.

```java
BlockingQueue<String> queue =
    new LinkedBlockingQueue<>();

// producer
queue.put("Java");

// consumer
String item = queue.take();
```

## Core behavior

- `put()` can block when the queue is full
- `take()` can block when the queue is empty

This naturally creates backpressure.

### Common implementations

```text
ArrayBlockingQueue
LinkedBlockingQueue
PriorityBlockingQueue
DelayQueue
```

### Interview insight

BlockingQueue is also closely related to executor/thread-pool design because task queues are used to hold submitted work.

---

# 35. Concurrent Collections

When many threads access a collection, regular collections can be unsafe.

Common tools:

```text
ConcurrentHashMap
CopyOnWriteArrayList
CopyOnWriteArraySet
ConcurrentLinkedQueue
ConcurrentLinkedDeque
BlockingQueue
```

---

# 36. ConcurrentHashMap

Think:

```text
HashMap
   +
concurrent access support
```

```java
Map<Integer, String> map =
    new ConcurrentHashMap<>();
```

## Important characteristics

- thread-safe
- designed for concurrent access
- does not allow `null` keys or values
- supports atomic compound operations such as:

```java
putIfAbsent()
computeIfAbsent()
compute()
merge()
replace()
```

### Java 7 vs Java 8+ interview trap

Older Java versions used segment-based locking.

Modern implementations use finer-grained synchronization/CAS around bins and related mechanisms rather than one global lock.

### Hashtable vs ConcurrentHashMap

```text
Hashtable
    → legacy synchronized map

ConcurrentHashMap
    → modern concurrent map designed for better scalability
```

### Very common interview question

> Why is ConcurrentHashMap safer than HashMap under concurrent writes?

Because it is designed with concurrency control. Plain HashMap does not provide the required thread-safety guarantees for unsynchronized concurrent mutation.

---

# 37. CopyOnWriteArrayList

```java
List<String> list =
    new CopyOnWriteArrayList<>();
```

On a write, a new underlying array is created and updated.

```text
many readers
few writers
     ↓
CopyOnWriteArrayList
```

## Best use cases

- configuration-like data
- read-heavy listeners/subscriber lists
- mostly-read collections

## Bad use case

Write-heavy workloads.

Every mutation copies the array, which is expensive in terms of time and memory.

### Interview trap

Its thread safety comes with a **write cost**.

---

# 38. CopyOnWriteArraySet

A set built on copy-on-write behavior.

Use when:

```text
reads are frequent
writes are rare
uniqueness is required
```

---

# 39. ConcurrentLinkedQueue / Deque

Non-blocking concurrent structures.

```java
ConcurrentLinkedQueue<String> q =
    new ConcurrentLinkedQueue<>();
```

Typical methods:

```text
offer()
poll()
peek()
```

Good for high-throughput queueing where you do not need blocking semantics.

### Difference from BlockingQueue

```text
ConcurrentLinkedQueue
    → non-blocking

BlockingQueue
    → may block producer/consumer
```

---

# 40. Synchronized Collections vs Concurrent Collections

Example:

```java
Collections.synchronizedList(
    new ArrayList<>()
);
```

This provides thread-safe individual operations through synchronization.

But it can create more contention and does not have the same specialized concurrency behavior as collections designed for concurrent access.

Examples of specialized concurrent structures:

```text
ConcurrentHashMap
CopyOnWriteArrayList
ConcurrentLinkedQueue
BlockingQueue
```

### Interview answer

> Use a collection designed for the access pattern instead of blindly wrapping every collection in synchronization.

---

# 41. Deadlock, Starvation, Livelock

## Deadlock

Two or more threads wait forever for each other's locks.

```text
T1 holds A → waits for B
T2 holds B → waits for A

     DEADLOCK
```

### Prevention ideas

- consistent lock ordering
- avoid nested locks where possible
- use `tryLock()` with timeout when appropriate
- reduce lock scope

---

## Starvation

A thread waits excessively because other threads keep getting the resource.

Possible contributors:

- unfair locking
- overloaded executors
- priority/scheduling patterns
- long critical sections

---

## Livelock

Threads are not blocked, but they keep reacting to each other and make no useful progress.

```text
T1 reacts to T2
T2 reacts to T1
both active
no progress
```

### Memory trick

```text
Deadlock  = stuck waiting
Starvation = not getting a chance
Livelock   = active but not progressing
```

---

# 42. Synchronization Does Not Guarantee Thread Ordering

This is a frequent interview trap.

```java
synchronized(lock) {
    // one thread at a time
}
```

It guarantees mutual exclusion for that critical section.

It does not guarantee:

```text
T1 gets lock before T2
```

If strict order is required, use a coordination mechanism such as:

- `join()`
- `CountDownLatch`
- a single-thread executor
- a fair `ReentrantLock` where appropriate
- explicit sequencing logic

---

# 43. CountDownLatch

Use when one or more threads must wait until a count reaches zero.

Example:

```text
Main thread
   |
   +-- Service A
   +-- Service B
   +-- Service C
          |
          v
    count reaches 0
          |
          v
      continue
```

Good for:

- startup coordination
- waiting for multiple tasks to complete once

### Important

A CountDownLatch is one-shot. Once the count reaches zero, it cannot be reset.

---

# 44. CyclicBarrier

Used when a group of threads must reach the same synchronization point.

```text
T1 ─────┐
T2 ─────┼── barrier ── continue
T3 ─────┘
```

Different mental model from CountDownLatch:

- Latch = wait for events/countdown.
- Barrier = participants meet at a common point.

A CyclicBarrier can be reused for subsequent rounds.

---

# 45. Semaphore

Controls how many threads may enter a resource-limited section.

```java
Semaphore semaphore = new Semaphore(10);
```

Mental model:

```text
10 permits
    ↓
at most 10 concurrent users
```

Use cases:

- limiting access to a scarce resource
- controlling concurrency to a downstream dependency

Example:

```text
DB/External API supports ~10 concurrent calls
        ↓
Semaphore(10)
```

---

# 46. ExecutorCompletionService

Useful when you submit many tasks but want results **as they finish**, rather than waiting for a fixed order.

Mental model:

```text
Task A ────── 5 sec
Task B ── 1 sec  → result first
Task C ───── 3 sec

CompletionService
      ↓
receive B
receive C
receive A
```

This is especially useful for fan-out/fan-in patterns.

---

# 47. ForkJoin Framework

ForkJoin is designed around divide-and-conquer parallel computation.

```text
Large Task
    ↓
split
 ┌──┴──┐
 A     B
 ↓     ↓
split split
...
 ↓     ↓
results
  \   /
   join
```

## Core classes

```text
ForkJoinPool
ForkJoinTask
RecursiveTask<V>
RecursiveAction
```

---

# 48. RecursiveTask vs RecursiveAction

## RecursiveTask<V>

Returns a result.

```java
class SumTask
    extends RecursiveTask<Integer> {
}
```

Use for:

```text
sum
sort result
calculate result
```

## RecursiveAction

No result.

```java
class ProcessTask
    extends RecursiveAction {
}
```

Use when the task performs an action rather than returning a value.

---

# 49. `fork()` and `join()`

## `fork()`

Schedules a subtask for asynchronous execution.

## `join()`

Waits for the subtask's result/completion.

Typical pattern:

```java
left.fork();

int rightResult = right.compute();
int leftResult = left.join();

return leftResult + rightResult;
```

This is often preferred over forking every single subtask blindly.

---

# 50. Work Stealing

Most important ForkJoin concept.

Each worker has a deque/queue of tasks.

```text
Worker 1 → [A B C D]
Worker 2 → []
Worker 3 → [E F]
```

Worker 2 becomes idle.

It can steal work from another worker:

```text
Worker 2 steals D
```

This helps load balancing for recursive parallel tasks.

### Interview answer

> ForkJoinPool uses work stealing so idle workers can take available work from busy workers' queues, improving utilization for divide-and-conquer workloads.

---

# 51. ExecutorService vs ForkJoinPool

| ExecutorService | ForkJoinPool |
|---|---|
| General-purpose task execution | Parallel divide-and-conquer |
| Independent tasks | Recursive/subdividable tasks |
| Runnable/Callable | ForkJoinTask / RecursiveTask / RecursiveAction |
| Common in enterprise async processing | Common in CPU-heavy parallel computation |
| Pool behavior depends on chosen implementation | Work-stealing is a core feature |

### Important correction

Do not memorize:

```text
ExecutorService = concurrency
ForkJoinPool = parallelism
```

as an absolute rule.

Both are executor frameworks and both can execute multiple tasks. The better distinction is **workload and execution strategy**.

---

# 52. Parallel Stream

```java
list.parallelStream()
    .map(...)
    .toList();
```

Parallel streams usually use the common ForkJoinPool.

### Interview traps

- Parallel does not automatically mean faster.
- Small data sets may be slower.
- Blocking I/O inside parallel streams is often a poor design.
- Shared mutable state inside a parallel stream is dangerous.
- Pool usage can interact with other common-pool work.

---

# 53. CompletableFuture + Executor

Do not blindly use the common pool for every async operation.

You can provide an executor:

```java
ExecutorService executor =
    Executors.newFixedThreadPool(20);

CompletableFuture
    .supplyAsync(() -> callExternalService(), executor);
```

This lets you isolate workloads.

### Enterprise principle

Different workload types may deserve different executors.

```text
CPU-heavy pool
I/O-heavy pool
scheduled pool
bounded downstream pool
```

The important thing is to design around capacity and isolation.

---

# 54. Exception Handling Across Execution Models

```text
Raw Thread
   ↓
UncaughtExceptionHandler

Runnable + submit()
   ↓
Future
   ↓
exception visible through get()

Callable
   ↓
Future
   ↓
ExecutionException

CompletableFuture
   ↓
exceptionally / handle / whenComplete

ExecutorService
   ↓
also requires lifecycle + logging + monitoring
```

### Critical trap

`submit()` and `execute()` behave differently around task exceptions.

- `submit()` captures the exception in the Future.
- `execute()` does not give you a Future; an uncaught task exception can reach the thread's uncaught-exception handling path.

---

# 55. Monitoring a Thread Pool

In production, do not just look at "number of threads."

Useful metrics include:

```text
active thread count
queue size
pool size
completed task count
task execution time
task wait/queue time
rejected task count
CPU usage
memory usage
downstream latency/error rate
```

### Why?

A growing queue can be a signal that:

```text
arrival rate > processing rate
```

That is a capacity problem, not automatically a "need more threads" problem.

---

# 56. Project Mapping — What Your Notes Actually Support

The attached notes contain two explicit project stories:

1. **New User Onboarding Application**
2. **Password Management Application**

These are the safest stories to use because they are directly represented in the notes.

---

# 57. Project Story — New User Onboarding

## Problem

User registration should complete without waiting for welcome-email delivery.

Email was an external/I/O operation and could take seconds.

## Design

```text
User Registration
      ↓
Create / Save User
      ↓
Return success
      ↓
ExecutorService
      ↓
Background email task
```

Example:

```java
ExecutorService executor =
    Executors.newFixedThreadPool(10);

executor.submit(() -> {
    try {
        emailService.sendWelcomeEmail(user);
    } catch (Exception e) {
        logger.error("Email failed", e);
    }
});
```

## Why Runnable?

Because the business flow does not need a return value from the email task.

```text
Need result?  NO
        ↓
Runnable
```

## Why not Future.get()?

Because:

```java
Future<?> future = executor.submit(task);
future.get();
```

would block the calling thread until the email work finishes.

That defeats the purpose of making the email asynchronous.

## Interview follow-ups

### Q: Where is concurrency here?

> Multiple tasks can be in progress at the same time. The application can complete the main onboarding flow while the email task is processed by a worker thread.

### Q: Is this necessarily parallelism?

> Not necessarily. ExecutorService provides concurrent task execution. Whether the tasks run in true parallel depends on the available CPU cores and scheduling.

### Q: Why ExecutorService instead of new Thread()?

> We wanted reusable worker threads and controlled task execution instead of creating a new thread for every email.

### Q: What if email fails?

> Catch/log the failure and update monitoring or retry state as appropriate. Do not let a non-critical notification failure incorrectly report the main onboarding transaction as failed.

### Q: Is fire-and-forget always safe?

> No. If the email is business-critical, blindly submitting a task and ignoring its outcome can lose work. A durable queue/outbox mechanism may be more appropriate.

---

# 58. Project Story — Password Management

## Problem described in the notes

Password requests were delayed under load.

The notes attribute the slowdown to:

- small thread pool
- queue buildup
- slow audit queries

The notes describe an optimization from roughly:

```text
30 minutes → 5 minutes
```

through application-level concurrency and database query optimization.

## Design

```text
Password Request
      ↓
ExecutorService
      ↓
Worker Thread
      ↓
AD update
      ↓
Audit update
      ↓
Result/status
```

Example:

```java
ExecutorService executor =
    Executors.newFixedThreadPool(20);

Callable<PasswordResetResult> task = () -> {
    try {
        updatePasswordInAD(user);
        auditRepository.saveSuccess(user);
        return new PasswordResetResult("SUCCESS");
    } catch (Exception e) {
        auditRepository.saveFailure(user, e.getMessage());
        return new PasswordResetResult("FAILED");
    }
};
```

## Why Callable?

Because the task outcome matters:

```text
SUCCESS
FAILED
TIMEOUT / ERROR
```

and the result needs to be represented to the rest of the application.

## Important interview nuance

The notes describe `Callable`, but also emphasize that `future.get()` was not called immediately because that would block the request thread.

So the important concept is:

```text
Callable
    ↓
Future represents result
    ↓
application decides when/how to consume it
```

---

# 59. Project Story — Queue Bottleneck

The notes contain this mental model:

```text
Too few workers
     ↓
tasks arrive faster
     ↓
queue grows
     ↓
latency increases
```

If the downstream system is the real bottleneck, simply increasing the pool size can make things worse.

For interview answers, distinguish:

```text
CPU bottleneck
I/O bottleneck
DB connection pool bottleneck
external service bottleneck
executor queue bottleneck
```

---

# 60. Project Story — Database Optimization

The notes mention a frequent query similar to:

```sql
SELECT *
FROM PASSWORD_REQUEST
WHERE STATUS = 'PENDING';
```

and adding an index on `STATUS`.

Interview lesson:

> Thread-pool tuning alone was not enough; concurrency improvements often expose or amplify the next bottleneck, such as a database query.

This is a very strong systems-thinking point.

---

# 61. How to Talk About a Banking Onboarding Project Without Overclaiming

Your attached notes do **not** provide enough evidence to claim a specific `ExecutorService`, thread pool size, or multithreading implementation for the banking onboarding project itself.

So use this distinction:

```text
Microservices concurrency
        ≠
Java thread-pool implementation
```

For example:

```text
Many users calling onboarding service
        → concurrent requests

Many Kafka messages consumed
        → consumer concurrency

Multiple Java worker threads in a service
        → thread-level concurrency

Parallel calls to independent services
        → application-level parallelism
```

### Interview-safe answer if your banking onboarding flow was mainly synchronous

> "The onboarding workflow itself was primarily synchronous and user-driven. We used concurrency where independent background or message-processing workloads benefited from it, but I would not claim thread-level parallelism for every microservice just because the system was microservices-based."

### If you actually implemented parallel external calls

Use this only if it is true in your code:

```text
Onboarding request
      │
      ├── Identity call
      ├── Address call
      └── another independent call
             ↓
        combine results
```

Then a good design discussion is:

> "Because these calls were independent, we could execute them concurrently and reduce overall latency, but the pool and downstream limits had to be respected."

Do not say "Kafka means multithreading" or "microservices means parallelism."

---

# 62. Interview Trap Matrix

| Question / Trap | Correct mental model |
|---|---|
| Is synchronized = concurrency? | No. It is a synchronization mechanism. |
| Is concurrency = parallelism? | No. Parallelism is actual simultaneous execution. |
| Is context switching = concurrency? | It is one mechanism used to support concurrency. |
| Does Future execute a task? | No. Executor executes; Future represents the result/state. |
| Does `get()` make a task synchronous? | It blocks the caller; the task still runs in a worker. |
| Runnable vs Callable? | Runnable = no result; Callable = result + checked exception declaration. |
| execute vs submit? | execute = void/Runnable; submit = Future/Runnable or Callable. |
| Does shutdownNow kill a thread? | No. It attempts interruption; interruption is cooperative. |
| Does volatile make `count++` safe? | No. Visibility ≠ atomicity. |
| Does synchronized guarantee thread order? | No. Mutual exclusion ≠ ordering. |
| Does notifyAll run all threads at once? | No. It wakes them; they still compete for the monitor. |
| Does wait() keep the lock? | No. It releases the monitor while waiting. |
| Does sleep() release the lock? | No. |
| Is ConcurrentHashMap just synchronized HashMap? | No. It is a specialized concurrent implementation. |
| Is CopyOnWrite good for frequent writes? | No. Writes copy the array. |
| Is ForkJoin always better than ExecutorService? | No. Workload determines the choice. |
| Is more thread count always faster? | No. It can increase contention and overload dependencies. |
| Is a fixed thread pool fully bounded? | Threads are fixed, but the default queue is unbounded. |
| Is `run()` multithreading? | No. `start()` creates the new execution thread. |
| Can a Thread object be restarted? | No. Starting twice throws `IllegalThreadStateException`. |
| Is AtomicInteger a universal locking replacement? | No. Great for atomic variables; not for arbitrary multi-step transactions. |
| Is parallelStream automatically faster? | No. Work size and overhead matter. |
| Is CompletableFuture automatically non-blocking? | Only if you use it as a non-blocking pipeline; `get()`/`join()` block the caller. |

---

# 63. Interview Cross-Question Chain — ExecutorService

## Q: Why did you use ExecutorService?

**Answer:**

> To manage reusable worker threads and execute asynchronous tasks in a controlled way instead of creating a new thread for every task.

### Follow-up: Why not `new Thread()`?

> Manual thread creation makes lifecycle, resource control and scaling harder. A pool reuses workers and centralizes task execution.

### Follow-up: What happens when all workers are busy?

> New tasks follow the executor's queue/rejection policy. With a typical fixed thread pool, tasks wait in the queue.

### Follow-up: What if the queue keeps growing?

> Latency and memory usage can increase. The real issue may be that arrival rate is higher than processing capacity.

### Follow-up: Would you just increase the pool size?

> Not blindly. I would check CPU, task type, downstream capacity, DB connections, queue size and rejection metrics.

---

# 64. Interview Cross-Question Chain — Runnable/Callable/Future

## Q: Why Runnable?

> No result was needed.

### Follow-up: Can Runnable return a value?

> No, `run()` returns `void`.

### Follow-up: Can Runnable throw checked exceptions?

> `run()` cannot declare checked exceptions, so they must be handled or wrapped inside the task.

---

## Q: Why Callable?

> The task produced a result and its outcome needed to be represented.

### Follow-up: How do you get the result?

> Submit the Callable and use the returned Future.

### Follow-up: What happens if the task throws?

> `Future.get()` exposes the task failure through `ExecutionException`.

### Follow-up: What is the downside of immediate `get()`?

> The caller blocks until completion.

---

# 65. Interview Cross-Question Chain — CompletableFuture

## Q: Why CompletableFuture instead of Future?

> Future is good for representing a pending result, but CompletableFuture provides richer asynchronous composition, chaining, combining and exception handling.

### Follow-up: `thenApply` vs `thenCompose`?

> `thenApply` transforms a value. `thenCompose` chains another asynchronous operation and avoids nested futures.

```text
thenApply:
A → B

thenCompose:
A → Future<B>
```

### Follow-up: `thenApply` vs `thenApplyAsync`?

> `thenApply` may execute the continuation in the thread completing the previous stage, while `thenApplyAsync` schedules the continuation asynchronously, optionally using a supplied executor.

### Follow-up: `handle` vs `exceptionally`?

> `exceptionally` is focused on recovery from failure. `handle` receives both result and exception and can transform either outcome.

---

# 66. Interview Cross-Question Chain — Synchronization

## Q: Why synchronized?

> To protect a critical section and provide mutual exclusion around shared state.

### Follow-up: Why not volatile?

> Volatile provides visibility and ordering, not mutual exclusion or compound-operation atomicity.

### Follow-up: Why not AtomicInteger?

> AtomicInteger is ideal for individual atomic state updates, but complex multi-step logic may need a lock or synchronized block.

### Follow-up: Why ReentrantLock?

> When I need features such as `tryLock`, timeout, interruptible acquisition, fairness or multiple conditions.

---

# 67. Interview Cross-Question Chain — wait/notify

## Q: Why wait/notify?

> For thread coordination where a thread must wait for a condition and another thread signals a state change.

### Follow-up: Why not sleep/poll?

> Polling wastes CPU and adds latency. `wait()` lets the thread wait efficiently.

### Follow-up: Why while, not if?

> The condition must be re-checked after the thread wakes up.

### Follow-up: Why use BlockingQueue in modern producer-consumer code?

> It encapsulates the queueing and blocking semantics and is easier to reason about than hand-written wait/notify code.

---

# 68. Interview Cross-Question Chain — ForkJoin

## Q: Why not use ForkJoin for your I/O-heavy application?

> ForkJoin is optimized for parallel computation and divide-and-conquer workloads. Blocking I/O can reduce worker utilization and interfere with the intended work-stealing model.

### Follow-up: What is work stealing?

> An idle worker can steal work from another worker's queue.

### Follow-up: RecursiveTask vs RecursiveAction?

> RecursiveTask returns a value; RecursiveAction does not.

---

# 69. The Most Important "Why" Map

```text
Need to run a task?
    ↓
Runnable / Callable

Need a pool?
    ↓
ExecutorService

Need result tracking?
    ↓
Future

Need rich async chaining?
    ↓
CompletableFuture

Need shared-state protection?
    ↓
synchronized / Lock

Need simple atomic variable?
    ↓
AtomicInteger / AtomicLong / AtomicReference

Need only visibility?
    ↓
volatile

Need producer-consumer waiting?
    ↓
BlockingQueue

Need custom object-monitor coordination?
    ↓
wait/notify

Need limit concurrent access?
    ↓
Semaphore

Need wait for N tasks once?
    ↓
CountDownLatch

Need a reusable rendezvous point?
    ↓
CyclicBarrier

Need divide-and-conquer CPU work?
    ↓
ForkJoinPool

Need thread-safe collection?
    ↓
ConcurrentHashMap / CopyOnWrite... / ConcurrentLinked... / BlockingQueue
```

---

# 70. 60-Second Final Revision

Say this mentally before the interview:

```text
Concurrency
= multiple tasks are in progress.

Parallelism
= tasks actually execute simultaneously.

Thread
= execution unit.

Runnable
= task, no result.

Callable
= task + result.

ExecutorService
= executes/manages tasks using worker threads.

ThreadPoolExecutor
= pool sizing + queue + rejection + lifecycle.

Future
= handle for async result/status.

CompletableFuture
= async composition + chaining + combining + error handling.

Race condition
= result depends on concurrent timing.

Atomicity
= indivisible update.

Visibility
= one thread sees another thread's update.

volatile
= visibility/ordering, NOT compound atomicity.

AtomicInteger
= atomic variable operations using CAS-style mechanisms.

synchronized
= monitor-based mutual exclusion.

ReentrantLock
= explicit lock control.

wait/notify
= monitor-based thread coordination.

BlockingQueue
= producer-consumer abstraction with blocking behavior.

Concurrent collections
= data structures designed for safe concurrent access.

Deadlock
= threads wait forever on each other.

ForkJoinPool
= divide-and-conquer + work stealing.

Parallel stream
= parallel collection processing, usually backed by common ForkJoinPool.

Main interview principle
= choose the concurrency tool based on workload, shared state, latency, CPU/I/O profile, downstream capacity and lifecycle requirements.
```

---

# 71. Final Interview Checklist

Before the interview, make sure you can explain these **without memorizing definitions**:

## Fundamentals
- [ ] Concurrency vs multithreading vs parallelism
- [ ] Context switching
- [ ] CPU-bound vs I/O-bound
- [ ] Thread lifecycle
- [ ] `start()` vs `run()`
- [ ] Why a Thread cannot be started twice

## Shared state
- [ ] Race condition
- [ ] Atomicity
- [ ] Visibility
- [ ] Ordering / happens-before concept
- [ ] `volatile`
- [ ] `AtomicInteger`
- [ ] CAS

## Synchronization
- [ ] synchronized method/block
- [ ] monitor
- [ ] reentrancy
- [ ] ReentrantLock
- [ ] tryLock
- [ ] fairness
- [ ] interruptible lock
- [ ] Condition

## Task execution
- [ ] Runnable
- [ ] Callable
- [ ] ExecutorService
- [ ] ThreadPoolExecutor
- [ ] execute vs submit
- [ ] Future
- [ ] FutureTask
- [ ] invokeAll vs invokeAny
- [ ] shutdown vs shutdownNow
- [ ] awaitTermination
- [ ] rejection policies
- [ ] pool sizing
- [ ] queue/backpressure

## Async
- [ ] CompletableFuture
- [ ] runAsync
- [ ] supplyAsync
- [ ] thenApply
- [ ] thenCompose
- [ ] thenCombine
- [ ] thenAccept
- [ ] thenRun
- [ ] allOf
- [ ] anyOf
- [ ] exceptionally
- [ ] handle
- [ ] whenComplete
- [ ] get vs join
- [ ] timeout/fallback

## Coordination
- [ ] wait / notify / notifyAll
- [ ] wait vs sleep
- [ ] while-loop condition check
- [ ] BlockingQueue
- [ ] CountDownLatch
- [ ] CyclicBarrier
- [ ] Semaphore

## Concurrent collections
- [ ] ConcurrentHashMap
- [ ] CopyOnWriteArrayList
- [ ] CopyOnWriteArraySet
- [ ] ConcurrentLinkedQueue
- [ ] ConcurrentLinkedDeque
- [ ] BlockingQueue
- [ ] concurrent vs synchronized collections

## Advanced
- [ ] Deadlock
- [ ] Starvation
- [ ] Livelock
- [ ] ForkJoinPool
- [ ] RecursiveTask
- [ ] RecursiveAction
- [ ] Work stealing
- [ ] parallelStream

## Project stories
- [ ] Why Runnable in onboarding/email
- [ ] Why Callable in password processing
- [ ] Why not call Future.get immediately
- [ ] Thread-pool bottleneck / queue buildup
- [ ] DB query bottleneck
- [ ] Failure handling
- [ ] Monitoring
- [ ] Distinguish service-level concurrency from Java thread-level concurrency

---

# 72. Golden Rule

> **Do not answer a concurrency interview question by naming a class first.**
>
> First identify the problem:
>
> ```text
> What is the task?
> Is it CPU-bound or I/O-bound?
> Is there shared mutable state?
> Do I need a result?
> Do I need ordering?
> Do I need cancellation?
> Do I need coordination?
> How much concurrency can the downstream system handle?
> ```
>
> Then choose the tool.

```text
Problem
   ↓
Requirement
   ↓
Concurrency model
   ↓
Tool
   ↓
Trade-off
```

That reasoning pattern is more valuable than memorizing API definitions.
