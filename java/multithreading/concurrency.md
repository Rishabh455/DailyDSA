Java Interview ke liye **Synchronization** me ye topics sabse important hain. Inme se 80-90% questions interview me cover ho jate hain.

# 1. What is Synchronization?

* Multiple threads jab same resource access karte hain to data inconsistency ho sakti hai.
* Synchronization ensure karta hai ki ek time par sirf ek thread critical section execute kare.

Example:

```java
count++;
```

Do threads same time execute kare to incorrect value aa sakti hai.

---

# 2. Race Condition

**Definition:**
Jab multiple threads shared data ko simultaneously modify kare aur result unpredictable ho.

Example:

```java
balance = balance - 100;
```

2 threads ek hi balance update kar rahe hain.

Interview Question:

> What is Race Condition and how do you prevent it?

Answer:

> Using synchronization, locks, atomic classes, concurrent collections.

---

# 3. synchronized Keyword

## Synchronized Method

```java
public synchronized void withdraw() {
}
```

Object lock acquire hota hai.

---

## Synchronized Block

```java
synchronized(this) {
}
```

Preferred because lock scope chhota rehta hai.

Interview:

> Why synchronized block is preferred over synchronized method?

Answer:

> Better performance because only critical code is locked.

---

# 4. Object Lock vs Class Lock

## Object Lock

```java
public synchronized void method() {
}
```

Lock on current object.

---

## Class Lock

```java
public static synchronized void method() {
}
```

Lock on Class object.

Interview favorite.

---

# 5. Monitor Lock

Har Java object ke paas intrinsic lock (monitor) hota hai.

```java
synchronized(obj 2) {
}
```

Thread monitor acquire karta hai.

---

# 6. Thread Safety

Thread-safe class multiple threads ke saath safely work karti hai.

Examples:

```java
StringBuffer
ConcurrentHashMap
AtomicInteger
```

Not Thread Safe:

```java
ArrayList
HashMap
StringBuilder
```

---

# 7. volatile Keyword

Ensures visibility.

```java
private volatile boolean running;
```

Ek thread change kare to doosre thread ko immediately visible hoga.

Important:

❌ Atomicity nahi deta

✔ Visibility deta

Interview:

> Difference between volatile and synchronized?

| volatile   | synchronized           |
| ---------- | ---------------------- |
| Visibility | Visibility + Atomicity |
| No Lock    | Uses Lock              |
| Faster     | Slower                 |

---

# 8. Atomic Classes

Package:

```java
java.util.concurrent.atomic
```

Examples:

```java
AtomicInteger
AtomicLong
AtomicBoolean
```

```java
counter.incrementAndGet();
```

Interview:

> Why use AtomicInteger instead of synchronized?

Answer:

> Lock-free and faster.

---

# 9. Deadlock

Situation:

Thread-1:

```java
Lock A
Lock B
```

Thread-2:

```java
Lock B
Lock A
```

Both wait forever.

Interview favorite.

Prevention:

* Lock ordering
* Timeout
* Fewer locks

---

# 10. Livelock

Threads active hain but progress nahi kar rahe.

Example:
2 people hallway me ek dusre ko rasta dete rahe.

---

# 11. Starvation

Low priority thread ko CPU hi nahi mil raha.

---

# 12. wait()

Thread lock release karke waiting state me jata hai.

```java
obj.wait();
```

Must be inside synchronized block.

---

# 13. notify()

Ek waiting thread ko wake up karta hai.

```java
obj.notify();
```

---

# 14. notifyAll()

Sab waiting threads ko wake up karta hai.

```java
obj.notifyAll();
```

Interview:

> Difference between notify and notifyAll?

---

# 15. Producer Consumer Problem

Classic synchronization question.

Uses:

```java
wait()
notify()
```

Ya modern approach:

```java
BlockingQueue
```

---

# 16. Reentrant Lock

```java
ReentrantLock
```

Features:

* Explicit lock
* Fairness
* tryLock()
* lockInterruptibly()

Interview:

> Difference between synchronized and ReentrantLock?

---

# 17. ReadWriteLock

```java
ReadWriteLock
```

* Multiple readers allowed
* Single writer allowed

Used when reads >> writes.

---

# 18. Concurrent Collections

Important classes:

```java
ConcurrentHashMap
CopyOnWriteArrayList
BlockingQueue
ConcurrentLinkedQueue
```

Interview favorite:

> Difference between HashMap and ConcurrentHashMap?

---

# 19. BlockingQueue

Producer Consumer ka easiest solution.

```java
ArrayBlockingQueue
LinkedBlockingQueue
```

No need for wait/notify.

---

# 20. CAS (Compare And Swap)

Atomic classes internally CAS use karti hain.

```java
AtomicInteger
```

Lock-free synchronization.

Interview:

> How AtomicInteger works internally?

Answer:

> CAS + CPU instructions.

---

---------------------------------------------------------------------------------------
English

# Java Synchronization - Complete Interview Notes (Deep Explanation)

## 1. What is Synchronization?

Synchronization is a mechanism used to control access to shared resources when multiple threads are executing concurrently.

Its main purpose is to prevent data inconsistency and ensure that only one thread executes a critical section of code at a time.

### Problem Without Synchronization

Suppose two threads are incrementing the same counter:

```java
count++;
```

Internally this operation consists of:

```text
1. Read count
2. Increment value
3. Write value back
```

If two threads execute these steps simultaneously, one update may overwrite the other, resulting in incorrect data.

### Benefits

* Prevents race conditions
* Ensures data consistency
* Makes code thread-safe

---

## 2. Race Condition

A Race Condition occurs when multiple threads access and modify the same shared resource simultaneously and the final result depends on the order of execution.

### Example

```java
balance = balance - 100;
```

Assume:

```text
Balance = 1000
```

Two threads withdraw ₹100 at the same time.

Expected Result:

```text
800
```

Actual Result:

```text
900
```

Both threads read the same value before either updates it.

### How to Prevent Race Conditions?

* synchronized keyword
* ReentrantLock
* Atomic Classes
* Concurrent Collections

---

## 3. synchronized Keyword

The synchronized keyword provides mutual exclusion.

Only one thread can execute synchronized code protected by the same lock at a time.

---

### Synchronized Method

```java
public synchronized void withdraw() {
}
```

Lock acquired:

```text
Current Object (this)
```

Only one thread can execute any synchronized method on that object at a time.

---

### Synchronized Block

```java
synchronized(this) {
    // critical section
}
```

Lock acquired only around the required code.

### Why is synchronized block preferred?

Instead of locking the entire method, only the critical section is locked.

Benefits:

* Better performance
* Smaller lock scope
* Higher concurrency

---

## 4. Object Lock vs Class Lock

### Object Lock

```java
public synchronized void method() {
}
```

Equivalent to:

```java
synchronized(this) {
}
```

Lock is acquired on the current object.

Different objects can execute simultaneously.

---

### Class Lock

```java
public static synchronized void method() {
}
```

Equivalent to:

```java
synchronized(MyClass.class) {
}
```

Lock is acquired on the Class object.

Only one thread across all instances can execute that method.

### Interview Difference

Object Lock:

```text
Lock belongs to an object instance.
```

Class Lock:

```text
Lock belongs to the class itself.
```

---

## 5. Monitor Lock (Intrinsic Lock)

Every Java object has an internal lock called a Monitor Lock or Intrinsic Lock.

When a thread enters:

```java
synchronized(obj) {
}
```

The thread acquires the monitor lock associated with obj.

Other threads trying to acquire the same lock must wait.

### Important

Every object in Java has:

```text
Monitor Lock
Wait Set
Entry Set
```

These are managed by JVM.

---

## 6. Thread Safety

A class is thread-safe if multiple threads can access it simultaneously without causing incorrect behavior or data corruption.

### Thread-Safe Classes

```java
StringBuffer
ConcurrentHashMap
AtomicInteger
Vector
```

### Not Thread-Safe

```java
ArrayList
HashMap
StringBuilder
```

### Ways to Achieve Thread Safety

* Synchronization
* Immutable Objects
* Atomic Classes
* Concurrent Collections

---

## 7. volatile Keyword

volatile ensures visibility of changes across threads.

```java
private volatile boolean running;
```

When one thread modifies running, all other threads immediately see the updated value.

### What volatile Guarantees

✔ Visibility

✔ Prevents CPU caching issues

✔ Prevents instruction reordering (to some extent)

### What volatile Does NOT Guarantee

❌ Atomicity

Example:

```java
count++;
```

Still not thread-safe.

---

### volatile vs synchronized

| volatile                       | synchronized             |
| ------------------------------ | ------------------------ |
| Visibility only                | Visibility + Atomicity   |
| No locking                     | Uses locking             |
| Faster                         | Slower                   |
| No mutual exclusion            | Mutual exclusion         |
| Cannot prevent race conditions | Prevents race conditions |

---

## 8. Atomic Classes

Package:

```java
java.util.concurrent.atomic
```

Common Classes:

```java
AtomicInteger
AtomicLong
AtomicBoolean
AtomicReference
```

### Example

```java
AtomicInteger counter = new AtomicInteger(0);

counter.incrementAndGet();
```

### Why Atomic Classes?

Traditional synchronization uses locks.

Atomic classes use:

```text
CAS (Compare And Swap)
```

which is lock-free and generally faster.

### Advantages

* Better performance
* Lock-free
* Highly scalable

---

## 9. Deadlock

A Deadlock occurs when two or more threads wait indefinitely for resources held by each other.

### Example

Thread-1:

```text
Lock A
Waiting for Lock B
```

Thread-2:

```text
Lock B
Waiting for Lock A
```

Neither thread can proceed.

### Conditions for Deadlock

1. Mutual Exclusion
2. Hold and Wait
3. No Preemption
4. Circular Wait

### Prevention

* Consistent lock ordering
* Lock timeout
* Minimize nested locks
* Use tryLock()

---

## 10. Livelock

In a Livelock, threads are not blocked.

They are actively running but continuously reacting to each other and making no progress.

### Example

Two people trying to pass each other in a hallway and both keep moving aside repeatedly.

### Difference

Deadlock:

```text
Stopped forever
```

Livelock:

```text
Running forever but making no progress
```

---

## 11. Starvation

Starvation occurs when a thread never gets CPU time or resources because higher-priority threads continuously consume them.

### Causes

* Thread priorities
* Unfair locking
* Resource monopolization

### Solution

Use fair locks:

```java
new ReentrantLock(true);
```

---

## 12. wait()

Used for inter-thread communication.

```java
obj.wait();
```

### Behavior

* Releases the lock
* Moves thread to Waiting State
* Must be called inside synchronized block

### Use Case

Producer-Consumer problem.

---

## 13. notify()

```java
obj.notify();
```

Wakes up one waiting thread.

The awakened thread still needs to reacquire the lock before continuing.

### Important

Does NOT release lock immediately.

Lock is released only after synchronized block exits.

---

## 14. notifyAll()

```java
obj.notifyAll();
```

Wakes up all waiting threads.

They compete again for the lock.

### notify vs notifyAll

| notify                      | notifyAll         |
| --------------------------- | ----------------- |
| Wakes one thread            | Wakes all threads |
| More efficient              | Safer             |
| Risk of wrong thread waking | No such risk      |

---

## 15. Producer Consumer Problem

One thread produces data.

Another thread consumes data.

### Producer

Creates tasks.

### Consumer

Processes tasks.

### Challenge

Producer may be faster than Consumer.

or

Consumer may be faster than Producer.

### Traditional Solution

```java
wait()
notify()
```

### Modern Solution

```java
BlockingQueue
```

---

## 16. ReentrantLock

Part of:

```java
java.util.concurrent.locks
```

Provides explicit locking.

### Features

```java
lock()
unlock()
tryLock()
lockInterruptibly()
Fairness
```

### Advantages over synchronized

* Timeout support
* Fair scheduling
* Interruptible lock acquisition
* Better control

---

### synchronized vs ReentrantLock

| synchronized      | ReentrantLock        |
| ----------------- | -------------------- |
| JVM managed       | Explicit lock        |
| Simpler           | More flexible        |
| No timeout        | Supports timeout     |
| No fairness       | Fairness available   |
| Automatic release | Must unlock manually |

---

## 17. ReadWriteLock

Used when:

```text
Reads >> Writes
```

Provides:

### Read Lock

Multiple readers allowed simultaneously.

### Write Lock

Only one writer allowed.

### Benefit

Higher performance in read-heavy applications.

Examples:

```text
Caching systems
Configuration data
Reference data
```

---

## 18. Concurrent Collections

Thread-safe collections designed for concurrent access.

### Examples

```java
ConcurrentHashMap
CopyOnWriteArrayList
ConcurrentLinkedQueue
BlockingQueue
ConcurrentSkipListMap
```

Benefits:

* Better performance than synchronized collections
* Fine-grained locking
* High scalability

---

## 19. BlockingQueue

A queue designed for Producer-Consumer scenarios.

### Popular Implementations

```java
ArrayBlockingQueue
LinkedBlockingQueue
PriorityBlockingQueue
DelayQueue
```

### Features

If queue is full:

```text
Producer waits automatically
```

If queue is empty:

```text
Consumer waits automatically
```

No need for wait()/notify().

### Commonly Used In

* Thread Pools
* Messaging Systems
* Task Scheduling

---

## 20. CAS (Compare And Swap)

CAS stands for Compare And Swap.

A low-level atomic CPU instruction used heavily by Atomic Classes.

### Working

Suppose:

```text
Current Value = 10
Expected Value = 10
New Value = 11
```

CAS checks:

```text
Is Current Value still 10?
```

If yes:

```text
Replace with 11
```

Otherwise:

```text
Retry
```

### Advantages

* Lock-free
* Fast
* High scalability

### Used By

```java
AtomicInteger
AtomicLong
ConcurrentHashMap
ForkJoinPool
```

---

