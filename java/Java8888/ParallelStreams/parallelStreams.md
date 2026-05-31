# Parallel Stream - Interview Notes

---

# What is Parallel Stream?

Parallel Stream is used to process data using multiple threads simultaneously.

Normal Stream:

```text
One Thread
```

Parallel Stream:

```text
Multiple Threads
```

Goal:

```text
Improve performance for large datasets
```

---

# Sequential Stream

```java
List<Integer> numbers =
        Arrays.asList(1,2,3,4,5);

numbers.stream()
       .forEach(System.out::println);
```

Execution:

```text
1
2
3
4
5
```

Single Thread.

---

# Parallel Stream

```java
List<Integer> numbers =
        Arrays.asList(1,2,3,4,5);

numbers.parallelStream()
       .forEach(System.out::println);
```

Output:

```text
3
1
5
2
4
```

Order may change.

Multiple threads are used.

---

# How to Create Parallel Stream?

## Method 1

```java
list.parallelStream()
```

Example:

```java
numbers.parallelStream()
       .forEach(System.out::println);
```

---

## Method 2

```java
stream.parallel()
```

Example:

```java
numbers.stream()
       .parallel()
       .forEach(System.out::println);
```

Both are equivalent.

---

# Internal Working

Parallel Streams use:

```java
ForkJoinPool.commonPool()
```

Internally.

```text
Data
 |
Split into chunks
 |
Multiple Worker Threads
 |
Process in Parallel
 |
Combine Results
```

---

# Example

```java
List<Integer> numbers =
        Arrays.asList(
                1,2,3,4,5,6,7,8
        );

numbers.parallelStream()
       .forEach(n ->
           System.out.println(
               Thread.currentThread()
                       .getName()
               + " : " + n
           )
       );
```

Possible Output:

```text
ForkJoinPool-worker-1 : 1
ForkJoinPool-worker-2 : 5
ForkJoinPool-worker-3 : 7
main : 2
```

Notice:

```text
Multiple threads are working
```

---

# Interview Question

## Which framework is used internally?

Answer:

```text
ForkJoin Framework
```

Specifically:

```java
ForkJoinPool.commonPool()
```

---

# Parallel Stream Example

Sum of Numbers

```java
int sum =
        IntStream.rangeClosed(1,100)
                 .parallel()
                 .sum();

System.out.println(sum);
```

Output:

```text
5050
```

---

# When Should We Use Parallel Streams?

Good For:

```text
Large Dataset
CPU Intensive Work
Independent Tasks
Mathematical Calculations
Data Processing
```

Example:

```text
10 million records
Image processing
Report generation
```

---

# When Should We Avoid Parallel Streams?

Bad For:

```text
Small Dataset
Database Calls
API Calls
Shared Mutable Data
Order Dependent Operations
```

Example:

```text
10 records only
```

Overhead may make it slower.

---

# Ordering Issue

Sequential Stream:

```java
numbers.stream()
       .forEach(System.out::println);
```

Output:

```text
1
2
3
4
5
```

Guaranteed.

---

Parallel Stream:

```java
numbers.parallelStream()
       .forEach(System.out::println);
```

Output:

```text
3
1
5
2
4
```

Order NOT guaranteed.

---

# Preserve Order

Use:

```java
forEachOrdered()
```

Example:

```java
numbers.parallelStream()
       .forEachOrdered(
           System.out::println
       );
```

Output:

```text
1
2
3
4
5
```

Order maintained.

---

# Thread Safety Problem

Wrong:

```java
List<Integer> result =
        new ArrayList<>();

numbers.parallelStream()
       .forEach(result::add);
```

Problem:

```text
ArrayList is not thread-safe
```

May produce incorrect results.

---

# Safe Alternative

Use collectors.

```java
List<Integer> result =
        numbers.parallelStream()
               .collect(
                   Collectors.toList()
               );
```

Safe.

---

# Parallel Stream vs Stream

| Stream | Parallel Stream |
|----------|----------|
| Single Thread | Multiple Threads |
| Ordered | Order not guaranteed |
| Less Overhead | More Overhead |
| Better for small data | Better for large data |

---

# Parallel Stream vs Multithreading

Parallel Stream:

```text
Easy
Less Code
Automatic Thread Management
```

ExecutorService:

```text
More Control
Custom Thread Pool
Better for complex scenarios
```

---

# Real Project Example

Suppose:

```text
10 lakh employees
```

Need to calculate bonus.

Sequential:

```java
employees.stream()
         .map(...)
```

Parallel:

```java
employees.parallelStream()
         .map(...)
```

Multiple CPU cores process employees simultaneously.

---

# Frequently Asked Interview Questions

## Q1. What is Parallel Stream?

A Stream that processes data using multiple threads.

---

## Q2. How do you create a Parallel Stream?

```java
list.parallelStream();
```

or

```java
stream.parallel();
```

---

## Q3. Which framework is used internally?

```text
ForkJoin Framework
```

---

## Q4. Which pool is used internally?

```java
ForkJoinPool.commonPool()
```

---

## Q5. Is ordering guaranteed?

No.

```java
forEach()
```

does not guarantee order.

---

## Q6. How to preserve order?

```java
forEachOrdered()
```

---

## Q7. Is Parallel Stream always faster?

No.

For small datasets it can be slower because of thread management overhead.

---

## Q8. When should we use Parallel Streams?

```text
Large Data
CPU Intensive Tasks
Independent Calculations
```

---

## Q9. When should we avoid Parallel Streams?

```text
Small Data
Database Calls
API Calls
Shared Mutable Objects
```

---

## Q10. Can Parallel Streams cause thread safety issues?

Yes.

Example:

```java
ArrayList
HashMap
StringBuilder
```

are not thread-safe.

---

# Most Important Interview Answer

## How does Parallel Stream work internally?

Answer:

Parallel Streams internally use the ForkJoin Framework and ForkJoinPool.commonPool(). The data is split into smaller tasks, processed by multiple worker threads in parallel, and the results are combined automatically.

---

# Quick Revision

```text
parallelStream()
stream().parallel()

Internal Framework:
ForkJoin Framework

Internal Pool:
ForkJoinPool.commonPool()

Order:
forEach()         -> No Guarantee
forEachOrdered()  -> Maintains Order

Good For:
Large Dataset
CPU Intensive Tasks

Avoid For:
Small Dataset
Database Calls
API Calls

Risk:
Thread Safety Issues
```

---

# Memory Trick

```text
Stream
=
One Worker

Parallel Stream
=
Many Workers

ForkJoinPool
=
Restaurant Manager

Worker Threads
=
Waiters

Data
=
Customer Orders
```