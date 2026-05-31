# Concurrent Collections - Interview Cheat Sheet

---

# Why Concurrent Collections?

Problem:

```java
List<Integer> list =
        new ArrayList<>();

Thread-1 -> add()
Thread-2 -> remove()
```

Multiple threads access collection simultaneously.

Result:

```text
Race Condition
ConcurrentModificationException
Data Corruption
```

Solution:

```text
Concurrent Collections
```

Thread-safe collections designed for multithreading.

---

# Main Concurrent Collections

```java
ConcurrentHashMap

CopyOnWriteArrayList

CopyOnWriteArraySet

ConcurrentLinkedQueue

ConcurrentLinkedDeque

BlockingQueue
```

---

# 1. ConcurrentHashMap

Most Important Interview Topic

Thread-safe version of HashMap.

```java
Map<Integer,String> map =
        new ConcurrentHashMap<>();
```

Example:

```java
map.put(1,"Java");
map.put(2,"Spring");
```

---

## Why Not HashMap?

HashMap:

```text
Not Thread Safe
```

ConcurrentHashMap:

```text
Thread Safe
High Performance
```

---

## Internal Working

Java 7:

```text
Segment Locking
```

Java 8:

```text
Bucket Level Locking
CAS
```

Improved Performance.

---

## Interview Question

### Difference Between Hashtable and ConcurrentHashMap?

| Hashtable | ConcurrentHashMap |
|------------|------------|
| Entire Map Locked | Partial Locking |
| Slower | Faster |
| Legacy | Modern |

---

# 2. CopyOnWriteArrayList

Thread-safe ArrayList.

```java
List<String> list =
        new CopyOnWriteArrayList<>();
```

---

## Internal Working

Whenever modification happens:

```java
list.add("Java");
```

Java:

```text
Creates New Copy
Updates New Copy
Replaces Old Copy
```

Hence:

```text
Copy On Write
```

---

## Best For

```text
Many Reads
Few Writes
```

Examples:

```text
Configuration Data
Application Settings
Cache
```

---

## Interview Question

### Why is it called CopyOnWrite?

Because every write operation creates a new copy of the underlying array.

---

# 3. CopyOnWriteArraySet

Thread-safe Set.

```java
Set<String> set =
        new CopyOnWriteArraySet<>();
```

Uses:

```text
CopyOnWriteArrayList internally
```

---

# 4. ConcurrentLinkedQueue

Thread-safe Queue.

```java
Queue<String> queue =
        new ConcurrentLinkedQueue<>();
```

---

## Operations

```java
offer()
poll()
peek()
```

Example:

```java
queue.offer("Java");

queue.poll();
```

---

## Characteristics

```text
Non Blocking
Lock Free
High Performance
```

---

# 5. ConcurrentLinkedDeque

Thread-safe Double Ended Queue.

```java
Deque<String> deque =
        new ConcurrentLinkedDeque<>();
```

Can insert/remove from:

```text
Front
Back
```

---

# 6. BlockingQueue

Most Asked Interview Topic

Used in:

```text
Producer Consumer Problem
ExecutorService
ThreadPool
```

---

## Implementation

```java
ArrayBlockingQueue

LinkedBlockingQueue

PriorityBlockingQueue
```

---

## Example

```java
BlockingQueue<String> queue =
        new LinkedBlockingQueue<>();
```

Producer:

```java
queue.put("Java");
```

Consumer:

```java
queue.take();
```

---

## Magic

If queue is empty:

```java
take()
```

automatically waits.

If queue is full:

```java
put()
```

automatically waits.

No need:

```java
wait()
notify()
notifyAll()
```

---

# Concurrent Collections vs Synchronized Collections

---

## Old Style

```java
List<String> list =
Collections.synchronizedList(
        new ArrayList<>()
);
```

Thread Safe.

But:

```text
Entire Collection Locked
```

Performance Poor.

---

## Modern Style

```java
List<String> list =
        new CopyOnWriteArrayList<>();
```

or

```java
Map<Integer,String> map =
        new ConcurrentHashMap<>();
```

Better Performance.

---

# ConcurrentHashMap vs HashMap

| HashMap | ConcurrentHashMap |
|----------|----------|
| Not Thread Safe | Thread Safe |
| Faster Single Thread | Better Multi Thread |
| Null Allowed | Null Not Allowed |

---

# ConcurrentHashMap Interview Questions

### Q1. Is ConcurrentHashMap thread-safe?

```text
Yes
```

---

### Q2. Does ConcurrentHashMap allow null keys?

```text
No
```

---

### Q3. Does ConcurrentHashMap allow null values?

```text
No
```

---

### Q4. Why is ConcurrentHashMap faster than Hashtable?

```text
Fine Grained Locking
CAS
```

instead of locking entire map.

---

# CopyOnWriteArrayList Interview Questions

### Q5. Best Use Case?

```text
Read Heavy Applications
```

---

### Q6. Why not use it for write-heavy workloads?

Because every modification creates a new copy.

Expensive.

---

# BlockingQueue Interview Questions

### Q7. Where is BlockingQueue used?

```text
Producer Consumer
Thread Pools
ExecutorService
```

---

### Q8. Difference Between Queue and BlockingQueue?

Queue:

```text
Returns immediately
```

BlockingQueue:

```text
Can wait automatically
```

---

# Most Asked Collection Interview Question

### Which Concurrent Collection Do You Use Most?

Answer:

```text
ConcurrentHashMap
```

Most common in real projects.

---

# Quick Revision

```text
ConcurrentHashMap
=
Thread Safe HashMap

CopyOnWriteArrayList
=
Thread Safe ArrayList

CopyOnWriteArraySet
=
Thread Safe Set

ConcurrentLinkedQueue
=
Thread Safe Queue

ConcurrentLinkedDeque
=
Thread Safe Deque

BlockingQueue
=
Producer Consumer
```

---

# Interview One-Liners

ConcurrentHashMap
=
Thread-safe HashMap with better performance than Hashtable.

CopyOnWriteArrayList
=
Thread-safe ArrayList where every write creates a new copy.

BlockingQueue
=
Thread-safe queue that automatically blocks producer/consumer threads when required.