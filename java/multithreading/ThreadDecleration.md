# What are the different ways to create a Thread in Java?

### 1. Extend Thread Class

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Thread running");
    }
}

new MyThread().start();
```

**Use:** Simple but not preferred because Java doesn't support multiple inheritance.

---

### 2. Implement Runnable Interface ⭐ (Most Common)

```java
class MyTask implements Runnable {
    public void run() {
        System.out.println("Task running");
    }
}

new Thread(new MyTask()).start();
```

**Use:** Preferred because class can extend another class.

---

### 3. Implement Callable Interface ⭐

```java
Callable<Integer> task = () -> 10;
Future<Integer> future = executor.submit(task);
```

**Use:** When thread needs to return a result or throw checked exceptions.

---

### 4. ExecutorService ⭐⭐⭐ (Most Used in Real Projects)

```java
ExecutorService executor = Executors.newFixedThreadPool(5);
executor.submit(() -> System.out.println("Task"));
```

**Use:** Thread pool management, better performance, production applications.

---

# Interview One-Liner

| Method          | Returns Result? | Used Today?   |
| --------------- | --------------- | ------------- |
| Thread Class    | ❌               | Rare          |
| Runnable        | ❌               | Yes           |
| Callable        | ✔               | Yes           |
| ExecutorService | ✔               | ⭐ Most Common |

### Interview Answer (30 seconds)

> Java threads can be created by extending the Thread class, implementing Runnable, implementing Callable, and using ExecutorService. In modern applications, ExecutorService is preferred because it manages thread pools efficiently, while Callable is used when a task needs to return a result. Runnable is the most common basic approach, and extending Thread is rarely used.
