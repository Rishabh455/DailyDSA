## Race Condition vs Deadlock (Interview Quick Revision)

| Race Condition                                            | Deadlock                                             |
| --------------------------------------------------------- | ---------------------------------------------------- |
| Multiple threads access/modify shared data simultaneously | Multiple threads wait forever for each other's locks |
| Causes inconsistent/wrong data                            | Causes application to hang                           |
| Threads are running                                       | Threads are blocked                                  |
| Fix: Synchronization, Atomic Classes                      | Fix: Consistent lock ordering                        |

---

### Race Condition

**Definition:**

> When multiple threads access shared data at the same time and the final result depends on thread execution order.

**Example:**

```java
count++;
```

Two threads increment the same variable.

Expected:

```text
count = 2
```

Actual:

```text
count = 1
```

❌ Wrong result

**Memory Trick:**

```text
Race Condition = Wrong Data
```

---

### Deadlock

**Definition:**

> When two or more threads hold locks and wait forever for each other to release them.

**Example:**

```text
Thread-1 has Lock-A, waiting for Lock-B

Thread-2 has Lock-B, waiting for Lock-A
```

Result:

```text
Application Stuck
```

❌ No progress

**Memory Trick:**

```text
Deadlock = No Data / No Progress
```

---

## 10-Second Interview Answer

> **Race Condition** occurs when multiple threads concurrently modify shared data, leading to inconsistent results. **Deadlock** occurs when multiple threads wait indefinitely for each other's locks, causing the application to freeze.

### Ultimate One-Liner

```text
Race Condition = Wrong Result

Deadlock = No Result
```

🚀 Easy to remember and enough for most interviews.
