# Limitations of Synchronization (synchronized) - Interview Notes

`synchronized` helps make code thread-safe, but it has several limitations.

---

## 1. Performance Overhead

Only one thread can enter a synchronized block/method at a time.

```text
Thread-1 --> Inside synchronized block

Thread-2 --> Waiting
Thread-3 --> Waiting
Thread-4 --> Waiting
```

Even if multiple CPU cores are available, only one thread can execute that critical section.

### Problem

If many threads are frequently competing for the same lock:

* Context switching increases
* Threads spend time waiting
* Overall throughput decreases

### Interview Point

> Excessive synchronization can become a performance bottleneck.

---

## 2. No Fairness Guarantee

`synchronized` does not guarantee which waiting thread will get the lock next.

Example:

```text
Thread-1 waiting
Thread-2 waiting
Thread-3 waiting
```

When the lock becomes free:

```text
Thread-3 may get it first
```

or

```text
Thread-2 may get it first
```

The JVM scheduler decides.

### Problem

Some threads may wait longer than others.

### Interview Point

> synchronized provides mutual exclusion but not fairness.

---

## 3. Risk of Deadlock

Deadlock can occur when multiple threads acquire locks in different orders.

Example:

### Thread-1

```text
Lock A
Waiting for Lock B
```

### Thread-2

```text
Lock B
Waiting for Lock A
```

Result:

```text
Thread-1 waiting forever
Thread-2 waiting forever
```

Application appears frozen.

### Interview Point

> Synchronization itself does not prevent deadlocks.

Developers must design lock acquisition carefully.

---

## 4. Blocking Nature

When a thread cannot acquire a lock, it becomes blocked.

Example:

```text
Thread-1 --> Using lock

Thread-2 --> Blocked
Thread-3 --> Blocked
```

Blocked threads cannot do any useful work until the lock becomes available.

### Problem

* Reduced responsiveness
* Wasted waiting time

### Interview Point

> Waiting threads remain blocked until the lock is released.

---

## 5. Limited Control

With synchronized, you cannot:

### Try Lock

```text
Try to acquire lock and continue if unavailable
```

### Timeout

```text
Wait only for 5 seconds
```

### Interrupt Waiting Thread

```text
Stop waiting and continue
```

### Fair Locking

```text
First Come First Serve
```

None of these are supported.

### Interview Point

> synchronized is simple but offers very limited lock management capabilities.

---

# Why ReentrantLock Was Introduced?

Java 5 introduced ReentrantLock to overcome these limitations.

Features available in ReentrantLock:

### tryLock()

```java
lock.tryLock();
```

Acquire lock only if available.

---

### Timed Lock

```java
lock.tryLock(5, TimeUnit.SECONDS);
```

Wait for a specific duration.

---

### Interruptible Lock

```java
lock.lockInterruptibly();
```

Waiting thread can be interrupted.

---

### Fair Lock

```java
new ReentrantLock(true);
```

Threads acquire lock in FIFO order.

---

# Interview Answer (30 Seconds)

synchronized provides thread safety but has several limitations. It can reduce performance because only one thread can execute the critical section at a time, it does not guarantee fairness, it can lead to deadlocks, waiting threads remain blocked, and it provides limited control over lock management. To overcome these limitations, Java introduced ReentrantLock, which supports tryLock(), timeouts, interruptible locking, and fairness policies.
-------------------------------------------------------------------------------------------------
                    BEFORE JAVA 5

                 synchronized
                        |
                        |
                        v
         Protect Shared Resource
         (Race Condition Prevention)

                        |
                        |
                        v

        +--------------------------------+
        | Problems / Limitations         |
        +--------------------------------+

        1. No Fairness
           |
           v
        A waiting
        B waiting
        C waiting

        Lock released

        C may enter before B

        -------------------------

        2. No tryLock()
           |
           v

        Resource Busy ?

        synchronized:
        Must Wait

        -------------------------

        3. No Timeout Support
           |
           v

        Wait forever

        No option like:
        "Wait only 5 sec"

        -------------------------

        4. Not Interruptible
           |
           v

        Waiting for lock

        Task Cancelled ?

        synchronized:
        Still Waiting

        -------------------------

        5. Single Wait Queue
           |
           v

        wait()
        notify()

        Everyone sits in
        same waiting room

                        |
                        |
                        v

              JAVA 5 INTRODUCED

                 ReentrantLock

                        |
                        |
                        v

        +--------------------------------+
        | Advanced Synchronization       |
        +--------------------------------+

                        |
                        |
                        v

     1. Fair Lock

     ReentrantLock(true)

     A
     ↓
     B
     ↓
     C

     FIFO order


     -----------------------------------


     2. tryLock()

     Resource Busy ?

     Yes
     ↓
     Do something else

     No waiting required


     -----------------------------------


     3. Timed Lock

     tryLock(5 sec)

     Wait max 5 sec

     Lock not available ?
     ↓
     Move on


     -----------------------------------


     4. Interruptible Lock

     lockInterruptibly()

     Waiting for lock
            ↓
     Interrupt received
            ↓
     Exit immediately


     -----------------------------------


     5. Multiple Conditions

     Condition loanQueue
     Condition paymentQueue
     Condition accountQueue

     Separate waiting rooms

     notify only required group


     -----------------------------------


     6. Reentrant Feature

     methodA()
         ↓
     lock acquired
         ↓
     methodB()
         ↓
     same thread acquires
     lock again

     Allowed

     No deadlock



=================================================

MEMORY TRICK

synchronized
=
Basic Lock

ReentrantLock
=
Smart Lock


FTIC + Conditions

F = Fairness
T = Timeout
I = Interruptible
C = tryLock Check

+
Multiple Conditions


=================================================

INTERVIEW ANSWER

synchronized provides basic thread synchronization but has limitations such as no fairness, no timeout support, no interruptible waiting, no lock availability check, and limited condition handling.

To overcome these limitations Java 5 introduced ReentrantLock, which provides fairness, tryLock(), timeout support, lockInterruptibly(), multiple Condition objects, and better control over locking.