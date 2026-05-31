# wait(), notify() and notifyAll() - Interview Notes

---

# Why do we need wait/notify?

Used for thread communication.

Example:

```text
Producer creates data.

Consumer consumes data.

Consumer should wait if data is not available.
```

Instead of continuously checking:

```java
while(dataNotAvailable){
    // keep checking
}
```

we can use:

```java
wait()
notify()
```

to efficiently coordinate threads.

---

# wait()

Puts the current thread into waiting state.

The thread:

```text
Releases the lock
Moves to Waiting State
Waits until another thread wakes it up
```

Example:

```java
synchronized(lock){

    lock.wait();

}
```

---

# notify()

Wakes up ONE waiting thread.

Example:

```java
synchronized(lock){

    lock.notify();

}
```

---

# notifyAll()

Wakes up ALL waiting threads.

Example:

```java
synchronized(lock){

    lock.notifyAll();

}
```

---

# Visual Example

Suppose:

```text
Waiting Queue

Thread-1
Thread-2
Thread-3
```

---

## notify()

```java
lock.notify();
```

Result:

```text
Thread-1  -> Wakes up

Thread-2  -> Still waiting

Thread-3  -> Still waiting
```

Only ONE thread wakes up.

Which thread?

```text
JVM decides.
No guarantee.
```

---

## notifyAll()

```java
lock.notifyAll();
```

Result:

```text
Thread-1 -> Wake up

Thread-2 -> Wake up

Thread-3 -> Wake up
```

All waiting threads wake up.

But:

```text
Only one gets the lock first.

Others wait again for lock.
```

---

# Important Interview Point

notifyAll() does NOT mean:

```text
All threads run simultaneously.
```

It means:

```text
All threads move from WAITING
to BLOCKED/RUNNABLE state.
```

Then they compete for the monitor lock.

---

# Example

## Waiting Thread

```java
class Shared {

    public synchronized void consume()
            throws Exception {

        System.out.println(
                "Waiting..."
        );

        wait();

        System.out.println(
                "Resumed"
        );
    }
}
```

---

## Notifier Thread

```java
class Main {

    public static void main(
            String[] args)
            throws Exception {

        Shared s = new Shared();

        new Thread(() -> {

            try {
                s.consume();
            }
            catch(Exception e){}

        }).start();

        Thread.sleep(2000);

        synchronized(s){

            s.notify();

        }
    }
}
```

Output:

```text
Waiting...
Resumed
```

---

# Interview Question

## Why must wait(), notify(), notifyAll() be called inside synchronized block?

Because they operate on the object's monitor lock.

Otherwise:

```java
lock.wait();
```

throws:

```text
IllegalMonitorStateException
```

---

# wait() vs sleep()

| wait() | sleep() |
|----------|----------|
| Releases lock | Does not release lock |
| Used for communication | Used for delay |
| Must be inside synchronized | Can be called anywhere |
| Object class method | Thread class method |

---

# notify() vs notifyAll()

| notify() | notifyAll() |
|-----------|-------------|
| Wakes one waiting thread | Wakes all waiting threads |
| More efficient | More expensive |
| Risk of wrong thread waking up | Safer |
| Used when only one thread should proceed | Used when multiple threads may proceed |

---

# Producer Consumer Example

Suppose:

```text
Consumers Waiting

Consumer-1
Consumer-2
Consumer-3
```

Producer adds one item.

Use:

```java
notify();
```

Why?

```text
Only one consumer needed.
```

---

Producer adds many items.

Use:

```java
notifyAll();
```

Why?

```text
Multiple consumers can process data.
```

---

# Most Asked Interview Question

## Why is notifyAll() generally preferred?

Because notify() may wake the wrong thread.

Example:

```text
5 threads waiting

Only one specific thread can proceed

notify() wakes wrong thread

Thread checks condition

Goes back to waiting
```

Application may get stuck.

notifyAll() avoids this issue.

---

# Thread States

```text
RUNNABLE
   |
wait()
   v
WAITING
   |
notify()/notifyAll()
   v
BLOCKED/RUNNABLE
   |
Gets Lock
   v
RUNNING
```

---

# Frequently Asked Interview Questions

## Q1. What is wait()?

Suspends thread and releases monitor lock.

---

## Q2. What is notify()?

Wakes one waiting thread.

---

## Q3. What is notifyAll()?

Wakes all waiting threads.

---

## Q4. Which class contains wait(), notify(), notifyAll()?

```java
java.lang.Object
```

---

## Q5. Why are they in Object class and not Thread class?

Because synchronization is performed on objects (monitors), not threads.

---

## Q6. Can wait() be called outside synchronized block?

No.

Throws:

```text
IllegalMonitorStateException
```

---

## Q7. Difference between sleep() and wait()?

sleep():

```text
Pause thread
Lock not released
```

wait():

```text
Pause thread
Lock released
```

---

# Quick Revision

```text
wait()
=
Pause thread
Release lock

notify()
=
Wake ONE waiting thread

notifyAll()
=
Wake ALL waiting threads

Must be inside:
synchronized

Class:
Object

Exception:
IllegalMonitorStateException

wait()
releases lock

sleep()
does not release lock
```

---

# Memory Trick

```text
wait()
=
"I'll wait"

notify()
=
"Wake one person"

notifyAll()
=
"Wake everyone"
```