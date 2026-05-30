# What happens internally when we call `run()` instead of `start()`?

### Interview Answer

When we call **`start()`**, JVM creates a **new thread** and then automatically invokes the `run()` method inside that new thread.

When we call **`run()` directly**, it behaves like a normal method call and **no new thread is created**.

---

## Example

### Using `start()`

```java id="2z4v3f"
MyThread t = new MyThread();
t.start();
```

Internal Flow:

```text
Main Thread
     |
     v
 JVM creates New Thread
     |
     v
 run() executes in New Thread
```

Result:

```text
Multithreading happens
```

---

### Using `run()`

```java id="iqtwku"
MyThread t = new MyThread();
t.run();
```

Internal Flow:

```text
Main Thread
     |
     v
 run() called like normal method
```

Result:

```text
No new thread created
No multithreading
```

---

## Easy Interview Memory Trick

### `start()`

```text
start() = Create New Worker + Execute run()
```

### `run()`

```text
run() = Just execute method normally
```

---

## Interview One-Liner

> `start()` creates a new thread and then invokes `run()` internally, whereas calling `run()` directly executes it as a normal method in the current thread without creating any new thread.


### Short Answer

**Yes, a class can extend `Thread` and implement `Runnable` together**, but it is **redundant and rarely used**.

```java
class MyThread extends Thread implements Runnable {
    @Override
    public void run() {
        System.out.println("Running...");
    }
}
```

---

### Interview Follow-up

**Why is it redundant?**

Because `Thread` class already implements `Runnable`.

```java
public class Thread implements Runnable
```

So when you extend `Thread`, your class automatically becomes a `Runnable`.

Adding `implements Runnable` again gives no extra benefit.

---

### Interview Answer

> Yes, a class can extend Thread and implement Runnable simultaneously because Thread already implements Runnable. However, it is redundant and not considered a good design. In practice, we either extend Thread or implement Runnable, with Runnable being the preferred approach.

# Can one Runnable instance be used by multiple threads?

### Short Answer

✅ **Yes, one Runnable instance can be shared by multiple threads.**

---

### Example

```java
Runnable task = new MyTask();

Thread t1 = new Thread(task);
Thread t2 = new Thread(task);

t1.start();
t2.start();
```

Here:

```text
Runnable Object (Shared)
       |
   ----------
   |        |
 Thread1  Thread2
```

Both threads execute the same `Runnable` object.

---

### Important Interview Point

If the `Runnable` contains **shared mutable data**, then both threads access the same data.

```java
class MyTask implements Runnable {
    int count = 0;

    public void run() {
        count++;
    }
}
```

Now `count` is shared between threads.

This can lead to:

```text
Race Condition
Data Inconsistency
Thread Safety Issues
```

unless synchronization is used.

---

### Why is this useful?

Sharing one Runnable instance allows:

* Shared state between threads
* Less object creation
* Better memory usage

---

### Interview Answer (30 seconds)

> Yes, a single Runnable instance can be used by multiple threads. Each thread executes the same `run()` method of that shared Runnable object. If the Runnable contains shared mutable state, synchronization or other thread-safety mechanisms are required to avoid race conditions.

# What happens if `start()` is called twice on the same thread?

### Short Answer

❌ **A thread can be started only once.**

If `start()` is called a second time on the same thread object, JVM throws:

```java id="yhbg7v"
java.lang.IllegalThreadStateException
```

---

### Example

```java id="2l1krm"
Thread t = new Thread();

t.start();  // Valid
t.start();  // Exception
```

Output:

```text id="81mw7u"
Exception in thread "main"
java.lang.IllegalThreadStateException
```

---

## Why?

Thread Lifecycle:

```text id="jz1hgm"
NEW
 |
start()
 |
RUNNABLE
 |
RUNNING
 |
TERMINATED
```

A thread object can move from:

```text id="y7jux4"
NEW → RUNNABLE
```

only once.

After a thread has started (or even finished execution), it **cannot be restarted**.

JVM does not allow:

```text id="ff2zvr"
TERMINATED → RUNNABLE
```

---

## If you want to run again?

Create a new Thread object.

```java id="cw8jmf"
Thread t1 = new Thread(task);
t1.start();

Thread t2 = new Thread(task);
t2.start();
```

---

## Interview One-Liner

> Calling `start()` twice on the same Thread object throws `IllegalThreadStateException` because a Java thread can be started only once and cannot be restarted after it leaves the NEW state.


````md

# Thread Lifecycle in Java



A thread goes through the following states during its lifetime:



```text

NEW

 |

start()

 |

RUNNABLE

 |

CPU Assigned

 |

RUNNING

 |

sleep(), wait(), join(), I/O

 |

BLOCKED / WAITING / TIMED_WAITING

 |

Back to RUNNABLE

 |

Task Completed

 |

TERMINATED

````



## 1. NEW



Thread object is created but start() is not called yet.



```java

Thread t = new Thread();

```



---



## 2. RUNNABLE



After calling start(), the thread becomes runnable and waits for CPU.



```java

t.start();

```



---



## 3. RUNNING



CPU schedules the thread and it starts executing the run() method.



---



## 4. BLOCKED / WAITING / TIMED_WAITING



Thread is temporarily not executing.



Examples:



```java

Thread.sleep(1000); // TIMED_WAITING



obj.wait();         // WAITING



t.join();           // WAITING

```



After the condition is satisfied, the thread moves back to RUNNABLE.



---



## 5. TERMINATED (DEAD)



The run() method completes or thread execution ends.



The thread cannot be restarted.



---



# Interview One-Liner



A Java thread starts in the NEW state, moves to RUNNABLE after start(), becomes RUNNING when CPU is assigned, may enter BLOCKED/WAITING/TIMED_WAITING during execution, and finally reaches TERMINATED when the run() method completes.



```

```
````md
# If two threads are trying to update a counter variable, what will happen and how can we solve it?

If two threads update the same counter variable simultaneously, a **Race Condition** can occur.

Example:

```java
counter++;
````

This is not an atomic operation. Internally it happens in 3 steps:

```text
1. Read counter value
2. Increment value
3. Write updated value back
```

Suppose:

```text
counter = 5
```

Thread-1 and Thread-2 both read the value 5 at the same time.

```text
Thread-1 reads 5
Thread-2 reads 5

Thread-1 updates to 6
Thread-2 updates to 6
```

Expected Result:

```text
7
```

Actual Result:

```text
6
```

One update is lost.

This is called a **Race Condition**.

## How to Solve It?

### 1. Using synchronized

```java
public synchronized void increment() {
    counter++;
}
```

Only one thread can update the counter at a time.

---

### 2. Using AtomicInteger (Preferred)

```java
AtomicInteger counter = new AtomicInteger(0);

counter.incrementAndGet();
```

Uses CAS (Compare And Swap) internally and is more efficient.

---

### Interview Answer

If two threads update the same counter variable simultaneously, a race condition may occur because `counter++` is not atomic. This can lead to lost updates and incorrect results. We can solve it using synchronization, locks, or AtomicInteger to ensure thread-safe updates.

```
```


# Can synchronization guarantee thread ordering?

### Short Answer

❌ **No, synchronization does not guarantee thread ordering.**

Synchronization only ensures:

* Mutual Exclusion (one thread at a time)
* Data Consistency
* Thread Safety

It does **not** guarantee which thread will acquire the lock first.

---

### Example

```java
synchronized(lock) {
    // critical section
}
```

Suppose:

```text
Thread-1 waiting
Thread-2 waiting
Thread-3 waiting
```

When the lock becomes free, JVM/OS scheduler decides who gets it.

Possible order:

```text
Thread-2
Thread-1
Thread-3
```

or

```text
Thread-3
Thread-2
Thread-1
```

There is no guarantee.

---

### How to Guarantee Ordering?

Use:

* `join()`
* `CountDownLatch`
* `Semaphore`
* `ReentrantLock(true)` (fair lock)
* `ExecutorService` with a single thread

Example:

```java
t1.start();
t1.join();

t2.start();
```

Here `t2` starts only after `t1` completes.

---

### Interview One-Liner

> No, synchronization guarantees thread safety and mutual exclusion, but it does not guarantee the order in which threads acquire the lock. Thread scheduling is controlled by the JVM and operating system.
Why doesn't volatile guarantee atomicity?

volatile only provides visibility, not atomicity.

It ensures that changes made by one thread are immediately visible to other threads by reading and writing directly from main memory.

However, operations like count++ are not atomic because they involve three steps:

1. Read value
2. Modify value
3. Write value

If multiple threads execute these steps simultaneously, race conditions can occur and updates may be lost.

Therefore, volatile cannot guarantee atomicity.

For atomic operations, we should use:
- synchronized
- ReentrantLock
- AtomicInteger

------------------------------------------------------------------------------------------
यह समझने के लिए हमें थोड़ा यथार्थ पर आना होगा। हां, AtomicInteger जैसी क्लास छोटे-छोटे ऑपरेशन्स (जैसे increment, decrement) को बिना लॉक के atomic तरीके से कर देती है। लेकिन हर स्थिति में समस्या इतनी सरल नहीं होती।

पहला, अगर आपका डेटा सिर्फ एक integer नहीं, बल्कि कोई जटिल ऑब्जेक्ट है या आपको कई स्टेट-चेंज ऑपरेशन्स एक साथ करने हैं, तब AtomicInteger काम नहीं करेगा। आपको critical section में कई स्टेट-चेंज ऑपरेशन्स को सुरक्षित रखना होगा।

दूसरा, कई बार आपको सिर्फ डेटा को increment/decrement नहीं, बल्कि पूरे ब्लॉक या बिज़नेस लॉजिक को एक साथ synchronize करना पड़ता है। ऐसे में synchronized या ReentrantLock ही flexibility देते हैं।

आखिर में, जब आपको fairness (FIFO order), interruptible locking, या multiple conditions की ज़रूरत हो, तब ReentrantLock जैसी advanced सुविधाएं जरूरी हो जाती हैं।

सार यही है: अगर काम simple atomic operations तक सीमित है, तो AtomicInteger काफी है। लेकिन जैसे ही complex operations या multiple steps की जरूरत होती है, तब synchronized या ReentrantLock की भूमिका आती है।