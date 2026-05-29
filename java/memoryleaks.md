## What is a Memory Leak in Java?

A **memory leak** occurs when objects are no longer needed by the application but are still reachable in memory, so the **Garbage Collector (GC)** cannot remove them.

As a result:

* Heap memory keeps increasing.
* Frequent Full GC occurs.
* Application becomes slow.
* Eventually, you may get **`OutOfMemoryError: Java heap space`**.

---

## Simple Interview Definition

> A memory leak in Java happens when unused objects remain referenced and cannot be garbage collected, causing heap memory consumption to grow over time.

---

## Common Causes of Memory Leaks

### 1. Static Collections

```java
public class UserCache {

    private static List<String> users = new ArrayList<>();

    public void addUser(String user) {
        users.add(user);
    }
}
```

Problem:

* Static variables live for the entire application lifecycle.
* Objects keep accumulating.

---

### 2. Unclosed Resources

```java
FileInputStream fis = new FileInputStream("data.txt");
```

If not closed:

```java
fis.close();
```

resources remain allocated.

Use:

```java
try (FileInputStream fis = new FileInputStream("data.txt")) {
    // logic
}
```

---

### 3. Cache Growing Indefinitely

```java
Map<String, User> cache = new HashMap<>();
```

If entries are never removed, memory usage continuously grows.

---

### 4. Event Listeners Not Removed

Objects remain referenced by listeners even after they are no longer needed.

---

## Real Project Interview Answer

Since you've worked on enterprise applications:

> In one of our applications, we observed increasing heap usage and frequent Full GCs. We generated a heap dump using JVM tools and analyzed it. We found that objects were being retained in a collection longer than required. After cleaning up references and improving object lifecycle management, heap utilization stabilized and GC activity reduced significantly.

---

## How Do You Detect Memory Leaks?

### JVM Tools

* Heap Dump
* Thread Dump
* GC Logs

### Analysis Tools

* Eclipse Memory Analyzer (MAT)
* VisualVM
* JConsole
* Java Mission Control

---

## Heap Dump vs Thread Dump

### Heap Dump

Shows:

* Objects in memory
* Memory usage
* Potential memory leaks

### Thread Dump

Shows:

* Running threads
* Deadlocks
* Blocked threads

Interviewers often ask this together.

---

## How to Prevent Memory Leaks?

1. Remove unnecessary references.
2. Avoid unbounded caches.
3. Close files, streams, and DB connections.
4. Use try-with-resources.
5. Monitor heap usage regularly.
6. Review static collections carefully.

---

## Interview Question: How did you identify a memory leak?

### Good Answer

> We noticed increasing heap memory usage and frequent Full GCs in production. We captured a heap dump and analyzed it using Eclipse MAT. The analysis showed that certain objects were being retained by a collection. After removing stale references and optimizing cleanup logic, memory consumption returned to normal.

---

## Rapid-Fire Questions

### What error can a memory leak cause?

**OutOfMemoryError**

---

### Can Garbage Collector prevent memory leaks?

**No.** GC can only remove objects that are unreachable. If references still exist, GC cannot clean them.

---

### What tool have you used?

For your background, a strong answer is:

> I have worked with heap dumps and JVM logs for production issue analysis and used heap dump analysis to investigate memory-related problems.

This answer is usually sufficient for a 3-year Java developer interview.
