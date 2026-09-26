# Java 8 to Java 21 — Deep Interview Revision Notes

> **Purpose:** These notes are designed for Java backend interviews.
>
> The goal is **not** to memorize every feature number.
>
> The goal is to understand:
>
> **Problem → Why Java introduced the feature → How it works → Where I would use it → What an interviewer may ask next**

---

# 1. Java 8 → Java 21: The Big Picture

Java evolved in a very understandable direction.

Instead of thinking:

> Java 8 had X features, Java 9 had Y features...

Think:

### Java 8

Java became much more **functional and expressive**.

Main ideas:

* Lambda expressions
* Functional interfaces
* Stream API
* Optional
* Default/static interface methods
* Modern Date/Time API
* CompletableFuture
* Method references

### Java 9

Java became more **modular**.

Main ideas:

* Module System
* `module-info.java`
* Collection factory methods
* JShell
* Private interface methods

### Java 10

Java reduced **type-declaration boilerplate**.

Main idea:

* `var`

### Java 11

Java added **practical APIs** and became an important LTS release.

Main ideas:

* Standard HTTP Client
* String improvements
* File API improvements
* `var` in lambda parameters
* LTS

### Java 12–16

Java started making Java syntax much more **expressive and concise**.

Main evolution:

```text
switch expressions
       ↓
text blocks
       ↓
records
       ↓
pattern matching
```

### Java 17

Another important LTS release.

Main ideas:

* Sealed classes become permanent
* Pattern matching for `switch` starts preview
* Strong encapsulation of JDK internals

### Java 19–21

Java focused heavily on **modern concurrency and data-oriented programming**.

Main ideas:

* Virtual threads
* Record patterns
* Pattern matching for switch
* Sequenced collections

### LTS releases between Java 8 and Java 21

```text
Java 8  → LTS
Java 11 → LTS
Java 17 → LTS
Java 21 → LTS
```

That is why interviewers commonly focus on these four versions. The uploaded transcript makes the same distinction: you do not need equal depth for every release.

---

# 2. The Most Important Interview Strategy

Suppose the interviewer asks:

> "What are the major features introduced after Java 8?"

Do **not** start speaking for 10 minutes:

> Java 9 had this...
> Java 10 had this...
> Java 11 had this...

Instead start high-level:

> "After Java 8, Java introduced modularity in Java 9, local variable type inference with `var` in Java 10, several practical APIs and the standardized HTTP Client in Java 11, then more expressive language features such as switch expressions, text blocks, records and pattern matching. Java 17 introduced sealed classes as a permanent feature, and Java 21 introduced major concurrency improvements such as virtual threads along with record patterns, pattern matching for switch and sequenced collections."

Then stop.

Now let the interviewer choose what they want to discuss.

This is much more effective.

---

# 3. Java 8 — THE MOST IMPORTANT VERSION

Java 8 is one of the most important Java releases for interviews.

The major interview topics are:

1. Lambda expressions
2. Functional interfaces
3. Method references
4. Stream API
5. Optional
6. Default and static methods in interfaces
7. New Date/Time API
8. CompletableFuture
9. Important supporting API improvements

Oracle's Java 8 language documentation identifies lambda expressions, method references, default methods and the APIs built around lambdas/streams as major additions.

---

# 4. Java 8 — Lambda Expressions

## 4.1 What problem did lambda solve?

Before Java 8, Java often required a lot of boilerplate when passing a small piece of behavior.

For example:

```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```

The important part is:

```java
System.out.println("Hello");
```

But Java forces us to write:

```java
new Runnable() {
    @Override
    public void run() {
        ...
    }
}
```

That is a lot of boilerplate for a tiny piece of behavior.

Java 8 introduced:

```java
Runnable r = () -> System.out.println("Hello");
```

The lambda focuses on the **behavior** instead of the object syntax.

---

# 5. What exactly is a Lambda?

A lambda is essentially an **anonymous function-like block of behavior** that can be passed around.

Example:

```java
(a, b) -> a + b
```

Think:

```text
Input → Function → Output
```

For example:

```java
(a, b) -> a + b
```

means:

```text
take a and b
return a + b
```

Another:

```java
name -> name.toUpperCase()
```

means:

```text
take name
return uppercase name
```

---

# 6. Important Interview Question

## Can lambda expressions work with any interface?

### No.

Lambda expressions work with a **functional interface**.

Example:

```java
@FunctionalInterface
interface Calculator {
    int add(int a, int b);
}
```

Now:

```java
Calculator calculator = (a, b) -> a + b;
```

Why does this work?

Because `Calculator` has exactly **one abstract method**.

---

# 7. Functional Interface

A functional interface is an interface having:

> **Exactly one abstract method.**

Example:

```java
@FunctionalInterface
interface PaymentProcessor {
    void processPayment();
}
```

This can be represented using a lambda:

```java
PaymentProcessor processor =
        () -> System.out.println("Processing payment");
```

---

# 8. Does `default` count as an abstract method?

No.

Example:

```java
interface A {

    void execute(); // abstract

    default void log() {
        System.out.println("log");
    }
}
```

There is still only one abstract method.

Therefore `A` is functional.

Similarly, a `static` interface method does not count as the abstract method.

---

# 9. `@FunctionalInterface`

Use:

```java
@FunctionalInterface
interface Calculator {
    int add(int a, int b);
}
```

Why?

It tells the compiler:

> "This interface is supposed to remain a functional interface."

If somebody later adds another abstract method:

```java
void subtract(int a, int b);
```

the compiler reports an error.

So the annotation is a **compile-time safety check**.

---

# 10. Built-in Functional Interfaces

Java provides many functional interfaces under:

```java
java.util.function
```

The four most important ones for interviews are:

```text
Predicate
Function
Consumer
Supplier
```

---

# 11. Predicate

A `Predicate<T>`:

```text
T → boolean
```

It answers a yes/no question.

Example:

```java
Predicate<Integer> isEven =
        n -> n % 2 == 0;
```

Usage:

```java
System.out.println(isEven.test(10));
```

Output:

```text
true
```

Think:

> Predicate = condition.

Typical use:

```java
list.stream()
    .filter(n -> n > 10)
```

The lambda passed to `filter()` behaves like a predicate.

---

# 12. Function

A `Function<T, R>`:

```text
T → R
```

It takes one value and produces another value.

Example:

```java
Function<String, Integer> length =
        s -> s.length();
```

Usage:

```java
int result = length.apply("Java");
```

Output:

```text
4
```

Think:

> Function = transformation.

---

# 13. Consumer

A `Consumer<T>`:

```text
T → nothing
```

It consumes something and performs an action.

Example:

```java
Consumer<String> printer =
        s -> System.out.println(s);
```

Usage:

```java
printer.accept("Java");
```

Think:

> Consumer = use input, don't return anything.

---

# 14. Supplier

A `Supplier<T>`:

```text
nothing → T
```

Example:

```java
Supplier<Double> randomValue =
        () -> Math.random();
```

Usage:

```java
double value = randomValue.get();
```

Think:

> Supplier = give me a value.

---

# 15. Quick Functional Interface Table

| Interface           | Input | Output  | Typical meaning              |
| ------------------- | ----- | ------- | ---------------------------- |
| `Predicate<T>`      | T     | boolean | test                         |
| `Function<T,R>`     | T     | R       | transform                    |
| `Consumer<T>`       | T     | void    | consume                      |
| `Supplier<T>`       | none  | T       | supply                       |
| `UnaryOperator<T>`  | T     | T       | transform same type          |
| `BinaryOperator<T>` | T,T   | T       | combine two same-type values |

---

# 16. Lambda vs Anonymous Inner Class

Before Java 8:

```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```

Java 8:

```java
Runnable r =
        () -> System.out.println("Hello");
```

Lambda provides:

* less boilerplate
* better readability
* functional programming style
* easy composition with streams

But remember:

> Lambda does **not** create a general-purpose function object independent of a target type.

A lambda needs a compatible **functional interface target type**.

---

# 17. Method References

Method reference is a shorter form of a lambda when the lambda only calls an existing method.

Example:

```java
names.forEach(name ->
        System.out.println(name));
```

Can become:

```java
names.forEach(System.out::println);
```

Think:

```text
lambda:
x -> object.method(x)

method reference:
object::method
```

---

# 18. Types of Method References

## 1. Static method

```java
Integer::parseInt
```

Equivalent:

```java
s -> Integer.parseInt(s)
```

## 2. Instance method of a particular object

```java
System.out::println
```

Equivalent:

```java
x -> System.out.println(x)
```

## 3. Instance method of an arbitrary object of a type

```java
String::toUpperCase
```

Equivalent roughly to:

```java
s -> s.toUpperCase()
```

## 4. Constructor reference

```java
ArrayList::new
```

Equivalent:

```java
() -> new ArrayList<>()
```

---

# 19. Stream API

This is one of the biggest Java 8 interview topics.

## Why Streams?

Before streams:

```java
List<Integer> result = new ArrayList<>();

for (Integer n : numbers) {
    if (n % 2 == 0) {
        result.add(n);
    }
}
```

We describe **how** to perform the loop.

With streams:

```java
List<Integer> result =
        numbers.stream()
               .filter(n -> n % 2 == 0)
               .toList();
```

We describe:

> "I want the even numbers."

This is more **declarative**.

---

# 20. Collection vs Stream

This interview question is very common.

### Collection

A collection:

* stores data
* exists in memory
* represents the data structure

Example:

```java
List<Integer> numbers;
```

### Stream

A stream:

* processes data
* does not itself store the elements
* is usually consumed once
* supports a pipeline of operations

Think:

```text
Collection = data
Stream     = processing of data
```

---

# 21. Stream Pipeline

Typical structure:

```text
Source
  ↓
Intermediate Operations
  ↓
Terminal Operation
```

Example:

```java
numbers.stream()
       .filter(n -> n % 2 == 0)
       .map(n -> n * 2)
       .collect(Collectors.toList());
```

Here:

### Source

```java
numbers.stream()
```

### Intermediate operations

```java
filter(...)
map(...)
```

### Terminal operation

```java
collect(...)
```

---

# 22. Intermediate Operations

Examples:

```java
filter()
map()
flatMap()
distinct()
sorted()
limit()
skip()
peek()
```

Intermediate operations usually return another stream.

Example:

```java
stream.filter(...)
      .map(...)
      .sorted(...)
```

---

# 23. Terminal Operations

Examples:

```java
collect()
forEach()
reduce()
count()
min()
max()
findFirst()
findAny()
anyMatch()
allMatch()
noneMatch()
```

Once a terminal operation executes, the stream is consumed.

You generally cannot reuse it:

```java
Stream<Integer> stream =
        numbers.stream();

stream.count();

stream.forEach(System.out::println); // error
```

---

# 24. Why Are Intermediate Operations Lazy?

Consider:

```java
numbers.stream()
       .filter(n -> {
           System.out.println("filter " + n);
           return n > 10;
       });
```

Nothing meaningful happens yet because there is no terminal operation.

Add:

```java
.count();
```

Now the pipeline executes.

So:

> Intermediate operations are lazy and are evaluated when a terminal operation is requested.

This is important because it allows Java to optimize pipeline execution.

---

# 25. `filter()`

`filter()` removes elements that do not satisfy a condition.

Example:

```java
List<Integer> numbers =
        List.of(1, 2, 3, 4, 5, 6);

List<Integer> evenNumbers =
        numbers.stream()
               .filter(n -> n % 2 == 0)
               .toList();
```

Result:

```text
[2, 4, 6]
```

---

# 26. `map()`

`map()` transforms every element.

Example:

```java
List<String> names =
        List.of("java", "spring", "sql");

List<String> result =
        names.stream()
             .map(String::toUpperCase)
             .toList();
```

Result:

```text
[JAVA, SPRING, SQL]
```

Think:

```text
one input → one output
```

---

# 27. `map()` vs `flatMap()`

This is a classic interview question.

Suppose:

```java
List<List<String>> names = List.of(
        List.of("A", "B"),
        List.of("C", "D")
);
```

Using `map()`:

```java
names.stream()
     .map(List::stream);
```

You get:

```text
Stream<Stream<String>>
```

Nested streams.

Using:

```java
names.stream()
     .flatMap(List::stream);
```

You get:

```text
Stream<String>
```

Result:

```text
A, B, C, D
```

Mental model:

```text
map     = transform
flatMap = transform + flatten
```

---

# 28. `reduce()`

Used when multiple elements need to be combined into one result.

Example:

```java
int sum =
        List.of(1, 2, 3, 4)
            .stream()
            .reduce(0, Integer::sum);
```

Result:

```text
10
```

Think:

```text
many values → one value
```

Examples:

* sum
* product
* maximum
* custom aggregation

---

# 29. `collect()`

Used to gather stream results into a collection or another result structure.

Example:

```java
List<String> result =
        names.stream()
             .filter(name -> name.length() > 3)
             .collect(Collectors.toList());
```

Modern Java can also use:

```java
.toList()
```

---

# 30. `groupingBy()`

Very common in backend interviews.

Suppose:

```java
List<Employee> employees;
```

Group employees by department:

```java
Map<String, List<Employee>> result =
        employees.stream()
                 .collect(
                     Collectors.groupingBy(
                         Employee::getDepartment
                     )
                 );
```

Mental model:

> `groupingBy()` = SQL `GROUP BY` style thinking.

---

# 31. `partitioningBy()`

If there are exactly two categories:

```text
true
false
```

you can use:

```java
Map<Boolean, List<Employee>> result =
        employees.stream()
                 .collect(
                     Collectors.partitioningBy(
                         e -> e.getSalary() > 100000
                     )
                 );
```

Think:

```text
partitioningBy = split into two groups
groupingBy     = group by arbitrary key
```

---

# 32. `findFirst()` vs `findAny()`

### `findFirst()`

Returns the first element according to encounter order.

### `findAny()`

Returns any matching element.

Why can `findAny()` be useful in parallel streams?

Because it can return whichever matching element becomes available rather than preserving the first element.

So:

```text
findFirst → order matters
findAny   → any matching element is okay
```

---

# 33. Sequential Stream vs Parallel Stream

Sequential:

```java
numbers.stream()
```

Parallel:

```java
numbers.parallelStream()
```

Parallel streams can process work using multiple threads.

But:

> Parallel stream does NOT automatically mean faster.

It can hurt performance when:

* dataset is small
* operation is cheap
* task has synchronization
* operation is stateful
* thread-management overhead is greater than the work
* ordering matters
* external resources are involved

Use parallelism only when the workload benefits from it and measure the result.

---

# 34. Important Stream Rule

Avoid shared mutable state.

Bad:

```java
List<Integer> result = new ArrayList<>();

numbers.parallelStream()
       .forEach(n -> result.add(n));
```

This can create concurrency problems.

Prefer collector-based solutions:

```java
List<Integer> result =
        numbers.parallelStream()
               .filter(...)
               .toList();
```

---

# 35. Optional

`Optional<T>` represents:

```text
value may exist
OR
value may be absent
```

Example:

```java
Optional<String> name =
        Optional.ofNullable(user.getName());
```

---

# 36. Why Optional?

Without Optional:

```java
if (user != null) {
    if (user.getName() != null) {
        ...
    }
}
```

With Optional you can express absence explicitly.

Example:

```java
Optional<String> name =
        Optional.ofNullable(user.getName());

name.ifPresent(System.out::println);
```

---

# 37. Very Important Optional Interview Trap

Do **NOT** say:

> "Optional completely prevents NullPointerException."

That is wrong.

You can still write:

```java
optional.get();
```

when no value exists.

You can also have `null` elsewhere in your program.

The correct statement is:

> Optional provides an explicit API for representing and handling absence of a value and can reduce certain classes of null-related bugs.

---

# 38. Creating Optional

### `Optional.of()`

Use when value is guaranteed non-null.

```java
Optional<String> name =
        Optional.of("Java");
```

If argument is `null`, it throws `NullPointerException`.

### `Optional.ofNullable()`

Use when value may be null.

```java
Optional<String> name =
        Optional.ofNullable(value);
```

### `Optional.empty()`

```java
Optional<String> value =
        Optional.empty();
```

---

# 39. `orElse()` vs `orElseGet()`

Very common interview question.

```java
optional.orElse(createDefault());
```

versus:

```java
optional.orElseGet(() -> createDefault());
```

Main difference:

### `orElse()`

The fallback expression is evaluated eagerly.

### `orElseGet()`

The supplier is invoked only when the Optional is empty.

Therefore, if fallback creation is expensive:

```java
orElseGet(...)
```

is usually preferable.

---

# 40. `map()` vs `flatMap()` in Optional

Suppose:

```java
Optional<User> user;
```

If transformation returns a normal value:

```java
user.map(User::getName);
```

If transformation itself returns an Optional:

```java
user.flatMap(this::findAddress);
```

Mental model is same as streams:

```text
map     = transform
flatMap = transform + flatten nested container
```

---

# 41. Default Methods in Interfaces

Before Java 8, interfaces mainly represented contracts:

```java
interface Vehicle {
    void start();
}
```

Suppose a library has:

```java
interface PaymentService {
    void pay();
}
```

and thousands of classes implement it.

If Java/library designers add:

```java
void refund();
```

all implementations may break because they now have to implement the new method.

Java 8 introduced:

```java
default void refund() {
    ...
}
```

Now the interface itself supplies an implementation.

---

# 42. Why Default Methods Matter

The major motivation is **evolution of interfaces**.

Libraries can add behavior without immediately breaking every old implementation.

This was especially important for Java's own collection APIs and functional-style additions. Oracle explicitly describes default methods as allowing new functionality while preserving compatibility for existing implementations.

---

# 43. Default Method Conflict

Suppose:

```java
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    default void show() {
        System.out.println("B");
    }
}
```

Then:

```java
class C implements A, B {
}
```

Java doesn't know which implementation to use.

So you must override:

```java
@Override
public void show() {
    A.super.show();
}
```

or:

```java
@Override
public void show() {
    System.out.println("C");
}
```

---

# 44. Static Methods in Interfaces

Java 8 also allows:

```java
interface Utility {

    static void print() {
        System.out.println("Hello");
    }
}
```

Call:

```java
Utility.print();
```

Static interface methods are not inherited like instance methods.

---

# 45. Java 8 Date and Time API

Before Java 8, Java commonly used:

```java
Date
Calendar
```

These APIs had many design and usability problems.

Java 8 introduced:

```text
LocalDate
LocalTime
LocalDateTime
ZonedDateTime
Instant
Duration
Period
```

---

# 46. `LocalDate`

Date without time.

```java
LocalDate today =
        LocalDate.now();
```

Example:

```text
2026-09-26
```

Use when you care only about the date.

Examples:

* birthday
* invoice date
* bank holiday

---

# 47. `LocalTime`

Time without date.

```java
LocalTime time =
        LocalTime.now();
```

Use for things like:

* opening time
* shift time
* meeting time

---

# 48. `LocalDateTime`

Date + time, but no timezone.

```java
LocalDateTime now =
        LocalDateTime.now();
```

Important:

> `LocalDateTime` does NOT represent a globally unique point in time because it has no timezone/offset.

For example:

```text
2026-09-26 10:00
```

10 AM in Mumbai is not the same instant as 10 AM in New York.

---

# 49. `ZonedDateTime`

Date + time + timezone.

```java
ZonedDateTime now =
        ZonedDateTime.now(
            ZoneId.of("Asia/Kolkata")
        );
```

Use when timezone matters.

---

# 50. `Instant`

Represents a point on the global timeline.

```java
Instant now =
        Instant.now();
```

Very useful in distributed systems, logging and persistence when you want a common UTC-based point in time.

---

# 51. `Period` vs `Duration`

### Period

Date-based amount.

Examples:

```text
2 years
3 months
5 days
```

Use with dates.

### Duration

Time-based amount.

Examples:

```text
5 seconds
3 minutes
2 hours
```

Use with time/instant-based calculations.

---

# 52. Why Java 8 Date/Time API is Better

Most classes are:

* immutable
* easier to reason about
* thread-safe
* clearer in intent

Instead of one giant `Date`/`Calendar` concept, we express what we actually mean.

---

# 53. CompletableFuture

`CompletableFuture` is Java 8's major API for asynchronous programming.

Example:

```java
CompletableFuture<String> future =
        CompletableFuture.supplyAsync(
            () -> "Java"
        );
```

It lets us represent:

> "The result is not available yet, but eventually a result will exist."

---

# 54. Why CompletableFuture?

Suppose a backend request needs:

```text
Call service A
Call service B
Call service C
combine results
```

Instead of manually managing threads and callbacks, CompletableFuture allows composition.

---

# 55. Important CompletableFuture Methods

## `supplyAsync()`

Runs a computation that returns a value.

```java
CompletableFuture.supplyAsync(
    () -> fetchUser()
);
```

## `runAsync()`

Runs a task without returning a result.

```java
CompletableFuture.runAsync(
    () -> sendEmail()
);
```

---

# 56. `thenApply()`

Transforms the result.

```java
future
    .thenApply(User::getName);
```

Mental model:

```text
T → R
```

Similar to:

```text
map
```

---

# 57. `thenCompose()`

Used when the next async operation itself returns a CompletableFuture.

Example:

```java
getUser()
    .thenCompose(user ->
        getOrders(user.getId())
    );
```

Without `thenCompose`, you can end up with:

```java
CompletableFuture<CompletableFuture<List<Order>>>
```

With `thenCompose()`:

```java
CompletableFuture<List<Order>>
```

Mental model:

```text
thenApply  → transform
thenCompose → chain dependent async operation
```

---

# 58. `thenCombine()`

Used when two independent futures should be combined.

Example:

```java
CompletableFuture<User> userFuture = ...;
CompletableFuture<Account> accountFuture = ...;

userFuture.thenCombine(
    accountFuture,
    (user, account) -> createProfile(user, account)
);
```

Mental model:

```text
A future
+
B future
↓
combined result
```

---

# 59. Error Handling in CompletableFuture

Important methods:

```java
exceptionally()
handle()
whenComplete()
```

### `exceptionally()`

Recover from failure.

### `handle()`

Handle both success and failure and transform into another value.

### `whenComplete()`

Observe completion without generally transforming the result.

---

# 60. Important CompletableFuture Interview Trap

Do NOT say:

> "CompletableFuture means non-blocking code automatically."

Not necessarily.

You can still block:

```java
future.get();
future.join();
```

And your underlying operation may itself be blocking.

Better explanation:

> CompletableFuture provides an API for composing and coordinating asynchronous computations. Whether the overall system is non-blocking depends on the operations and executors used underneath.

---

# 61. Java 8 — Other Useful Features

You may encounter:

* Base64 API
* `forEach()` on collections
* `removeIf()`
* `replaceAll()`
* `computeIfAbsent()`
* `computeIfPresent()`
* improved `Comparator`
* repeating annotations
* type annotations
* `Spliterator`

You generally do not need to spend the same preparation time on these as on streams/lambdas.

---

# 62. Java 8 Interview Summary

If the interviewer asks:

> "What are the important features of Java 8?"

A strong answer:

> "Java 8 introduced functional-style programming into the language. The most important features are lambda expressions and functional interfaces, which reduce boilerplate and enable behavior to be passed around; the Stream API for declarative collection processing; Optional for explicit handling of potentially absent values; default and static methods in interfaces for interface evolution; the modern java.time API; and CompletableFuture for composing asynchronous operations."

---

# 63. Java 9

Java 9 is mainly remembered for:

1. Module System
2. Collection factory methods
3. JShell
4. Private interface methods
5. Try-with-resources improvements

Oracle describes the Java Platform Module System as the major architectural change of Java 9.

---

# 64. Java 9 — Module System

Before Java 9:

```text
Application
 ├── packages
 ├── classes
 └── JARs
```

Large applications could have problems with:

* dependency management
* accidental visibility
* encapsulation
* classpath conflicts

Java 9 introduced modules.

A module is a higher-level unit of organization.

---

# 65. `module-info.java`

Example:

```java
module com.example.payment {

    requires java.sql;

    exports com.example.payment.api;
}
```

### `requires`

Means:

> This module depends on another module.

### `exports`

Means:

> This package is exposed to other modules.

This gives stronger encapsulation than simply using packages.

---

# 66. Package vs Module

Think:

```text
Class
  ↓
Package
  ↓
Module
```

A package groups types.

A module groups packages and explicitly describes:

* dependencies
* exported packages
* encapsulation boundaries

---

# 67. Java 9 Collection Factory Methods

Instead of:

```java
List<String> names =
        new ArrayList<>();

names.add("Java");
names.add("Spring");
```

you can write:

```java
List<String> names =
        List.of("Java", "Spring");
```

Similarly:

```java
Set.of("Java", "Spring");
```

```java
Map.of(
    "name", "Rishabh",
    "role", "Developer"
);
```

These are convenient for creating small **unmodifiable** collections.

---

# 68. Important Collection Factory Trap

Do not think:

```java
List.of(...)
```

creates a normal mutable ArrayList.

It does not.

For example:

```java
List<String> list =
        List.of("A", "B");

list.add("C");
```

will fail because the returned collection is unmodifiable.

---

# 69. JShell

JShell is Java's interactive REPL.

Instead of:

```text
create class
↓
write main()
↓
compile
↓
run
```

you can try Java expressions directly:

```text
jshell> int x = 10;
jshell> x * 2
$2 ==> 20
```

Useful for:

* experimenting
* learning APIs
* quick testing
* interview preparation

---

# 70. Private Methods in Interfaces

Java 8 introduced default methods.

Java 9 allowed interface methods to be private so that common implementation logic can be shared internally.

Example:

```java
interface Payment {

    default void process() {
        validate();
        execute();
    }

    private void validate() {
        System.out.println("validate");
    }

    private void execute() {
        System.out.println("execute");
    }
}
```

The private methods are implementation helpers inside the interface.

---

# 71. Java 9 — Interview Answer

> "Java 9 is mainly known for the Java Platform Module System. Modules provide stronger encapsulation and explicit dependencies. It also introduced convenient collection factory methods such as `List.of`, JShell, private interface methods and a few language improvements."

---

# 72. Java 10 — `var`

This is probably the most important Java 10 concept for interviews.

Before:

```java
String name = "Java";
```

Java 10:

```java
var name = "Java";
```

The compiler infers:

```text
name → String
```

Oracle describes this as local variable type inference.

---

# 73. Does `var` make Java dynamically typed?

## NO.

This is one of the most common interview traps.

Consider:

```java
var name = "Java";
```

The compiler infers:

```java
String
```

So this will not work:

```java
name = 100;
```

The variable is still statically typed.

Think:

```text
Source code:
var name = "Java";

Compile time:
var → String

Runtime:
name is still String
```

---

# 74. Why Is `var` Called Type Inference?

Because the programmer does not explicitly write the type, but the compiler determines it.

Example:

```java
var employee = new Employee();
```

The compiler knows:

```java
Employee employee
```

---

# 75. Where Can `var` Be Used?

Primarily for **local variables**.

Example:

```java
var name = "Java";
```

Allowed in local variable declarations.

But not:

```java
class Employee {

    var name; // invalid
}
```

because fields don't have local variable type inference semantics.

---

# 76. `var` Requires an Initializer

This is invalid:

```java
var name;
```

Why?

Because the compiler has nothing from which to infer the type.

This is valid:

```java
var name = "Java";
```

---

# 77. `var` and `null`

This is invalid:

```java
var value = null;
```

Because `null` by itself does not provide enough information to infer a concrete type.

---

# 78. Java 11 — LTS

Java 11 is important because it is an **LTS release**.

Important interview topics:

* Standard HTTP Client
* String API improvements
* File API improvements
* `var` in lambda parameters
* Optional/Predicate conveniences
* Important JDK migration/removal changes

Oracle's language documentation identifies Java 11's language-level addition as local-variable syntax for lambda parameters.

---

# 79. Java 11 — String Improvements

Important methods:

```java
isBlank()
strip()
stripLeading()
stripTrailing()
repeat()
lines()
```

Example:

```java
"   ".isBlank();
```

returns:

```text
true
```

---

# 80. `trim()` vs `strip()`

This is a useful interview question.

`trim()` is an older API with simpler whitespace behavior.

`strip()` is Unicode-aware and designed for modern whitespace handling.

Example:

```java
String value = "   Java   ";

value.strip();
```

returns:

```text
"Java"
```

---

# 81. `repeat()`

Instead of:

```java
String result = "";

for (int i = 0; i < 3; i++) {
    result += "Java";
}
```

Java 11:

```java
"Java".repeat(3);
```

Result:

```text
JavaJavaJava
```

---

# 82. `lines()`

For multiline strings:

```java
String text = """
        Java
        Spring
        SQL
        """;
```

You can process lines with:

```java
text.lines()
    .forEach(System.out::println);
```

---

# 83. Java 11 — Files API

Before, reading a small text file often required more code.

Java 11:

```java
String content =
        Files.readString(Path.of("data.txt"));
```

Writing:

```java
Files.writeString(
    Path.of("data.txt"),
    "Hello Java"
);
```

This simplifies common text-file operations.

---

# 84. Java 11 — Standard HTTP Client

Before Java 11, many Java applications used third-party HTTP clients or older APIs.

Java 11 introduced a standard HTTP Client API.

Typical structure:

```java
HttpClient client =
        HttpClient.newHttpClient();

HttpRequest request =
        HttpRequest.newBuilder()
                   .uri(URI.create(url))
                   .GET()
                   .build();

HttpResponse<String> response =
        client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
```

---

# 85. HTTP Client Capabilities

The standard HTTP client supports:

* HTTP/1.1
* HTTP/2
* synchronous requests
* asynchronous requests
* WebSocket support

This is a very useful answer when the interviewer asks:

> "What was a notable API introduced in Java 11?"

Answer:

> "The standardized `java.net.http.HttpClient` API."

---

# 86. HTTP Client Async

You can use:

```java
client.sendAsync(...)
```

which returns:

```java
CompletableFuture<HttpResponse<String>>
```

This also connects nicely with your Java 8 knowledge.

Think:

```text
Java 8 → CompletableFuture
Java 11 → HTTP Client built to support async operations
```

---

# 87. Java 11 — `var` in Lambda Parameters

Java 10:

```java
var name = "Java";
```

Java 11 extended `var` to implicitly typed lambda parameters.

Example:

```java
(var a, var b) -> a + b
```

Why is that useful?

It allows annotations to be applied to lambda parameters consistently.

Example:

```java
(@Nonnull var name) -> ...
```

The feature is documented as a Java 11 language enhancement.

---

# 88. Java 11 Interview Answer

> "Java 11 is an LTS release. From an application-development perspective, some useful additions are the standardized HTTP Client, String methods such as `isBlank`, `strip` and `repeat`, `Files.readString` and `writeString`, and support for `var` in lambda parameters."

---

# 89. Java 12

Java 12 is not usually a release you need to study deeply.

The main interview concept:

## Switch Expressions — Preview

Traditional switch:

```java
int result;

switch (day) {
    case MONDAY:
        result = 1;
        break;

    case TUESDAY:
        result = 2;
        break;

    default:
        result = 0;
}
```

The problem:

* `break`
* accidental fall-through
* verbose syntax
* result assignment repeated

Java 12 previewed switch expressions.

Oracle lists switch expressions as a preview feature in Java 12.

---

# 90. Java 13

Important:

## Text Blocks — Preview

Large JSON/HTML strings used to look ugly:

```java
String json =
    "{\n" +
    "  \"name\": \"Java\",\n" +
    "  \"version\": 21\n" +
    "}";
```

Text blocks provide a multiline syntax:

```java
String json = """
        {
          "name": "Java",
          "version": 21
        }
        """;
```

Java 13 introduced text blocks as preview.

Also:

* switch expressions continued preview
* `yield` was introduced for returning values from switch expression blocks

---

# 91. Java 14

Java 14 is an important bridge release because multiple major features progressed.

## 1. Switch Expressions became permanent

## 2. Records — Preview

## 3. Pattern Matching for `instanceof` — Preview

## 4. Helpful NullPointerExceptions

Oracle's language history confirms this progression.

---

# 92. Switch Statement vs Switch Expression

### Statement

```java
switch (day) {
    case MONDAY:
        System.out.println("Working");
        break;
    default:
        System.out.println("Other");
}
```

### Expression

```java
String result = switch (day) {
    case MONDAY, TUESDAY, WEDNESDAY -> "Working";
    default -> "Other";
};
```

The key conceptual difference:

```text
statement  → performs an action
expression → produces a value
```

---

# 93. Arrow Case Labels

Instead of:

```java
case MONDAY:
    result = "Working";
    break;
```

you can write:

```java
case MONDAY -> "Working";
```

No accidental fall-through.

---

# 94. `yield`

Suppose switch block has multiple statements:

```java
String result = switch (day) {

    case MONDAY -> {
        log();
        yield "Working";
    }

    default -> "Other";
};
```

`yield` means:

> Return this value from the switch expression.

Do not confuse:

```text
return → returns from method
yield  → returns a value from switch expression
```

---

# 95. Java 15

Important points:

### Text Blocks become permanent

Java 13:

```text
preview
```

Java 14:

```text
preview
```

Java 15:

```text
final
```

### Sealed Classes — Preview

Java 15 introduced sealed classes as a preview feature.

---

# 96. Java 16

Two major language features became permanent:

1. Records
2. Pattern matching for `instanceof`

Oracle confirms both became permanent in Java 16.

---

# 97. Pattern Matching for `instanceof`

Before:

```java
if (obj instanceof String) {

    String s = (String) obj;

    System.out.println(s.length());
}
```

You perform:

```text
check type
↓
cast
↓
use
```

Java 16:

```java
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

The variable is created as part of the type check.

Think:

```text
old:
instanceof + cast

new:
instanceof + pattern variable
```

---

# 98. Why Pattern Matching Matters

It reduces repetitive code.

It also makes the relationship between:

```text
type check
+
cast
+
use
```

more explicit.

This pattern becomes much more powerful when combined with:

* records
* sealed classes
* switch pattern matching

That evolution eventually reaches Java 21.

---

# 99. Records

Records are one of the most important modern Java features.

Imagine a simple DTO:

```java
public class Employee {

    private final int id;
    private final String name;

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        ...
    }

    @Override
    public int hashCode() {
        ...
    }

    @Override
    public String toString() {
        ...
    }
}
```

Huge amount of boilerplate.

Record:

```java
public record Employee(
        int id,
        String name
) {}
```

Much smaller.

---

# 100. What Does a Record Give You?

For:

```java
record Employee(
        int id,
        String name
) {}
```

Java provides important members such as:

* canonical constructor
* accessors
* `equals()`
* `hashCode()`
* `toString()`

Accessor names are:

```java
employee.id()
employee.name()
```

not:

```java
employee.getId()
employee.getName()
```

---

# 101. Is a Record Immutable?

Better interview answer:

> Records are designed as transparent data carriers and provide final record components. They are shallowly immutable by default.

Important nuance:

```java
record Employee(
    List<String> skills
) {}
```

The record reference cannot be reassigned, but the underlying list can still be mutated.

So:

```text
record immutability ≠ deep immutability
```

---

# 102. Can a Record Extend Another Class?

No.

A record implicitly extends:

```java
java.lang.Record
```

and cannot extend another class.

It can implement interfaces.

Example:

```java
record Employee(
    int id,
    String name
) implements Comparable<Employee> {

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(id, other.id);
    }
}
```

---

# 103. When Should You Use a Record?

Great use cases:

* DTOs
* API responses
* immutable request/response data
* value-like data carriers
* projections

Not every domain object should automatically be a record.

For complex mutable business objects, entities with lifecycle/state changes, or classes requiring inheritance, a normal class may be more appropriate.

---

# 104. Java 17 — LTS

Java 17 is extremely important because:

```text
Java 8  → LTS
Java 11 → LTS
Java 17 → LTS
Java 21 → LTS
```

The headline language feature for Java 17 is:

# Sealed Classes

Oracle confirms sealed classes became permanent in Java 17.

---

# 105. What Problem Does `sealed` Solve?

Normally:

```java
class Payment {
}
```

Any accessible class can extend it:

```java
class CardPayment extends Payment {}
class UpiPayment extends Payment {}
class RandomPayment extends Payment {}
```

Sometimes that is undesirable.

Suppose your domain says:

```text
Payment has only:
    CardPayment
    UpiPayment
    BankTransferPayment
```

You want the hierarchy to be controlled.

That is where sealed classes help.

---

# 106. Sealed Class Syntax

```java
public sealed class Payment
        permits CardPayment,
                UpiPayment,
                BankTransferPayment {
}
```

Now only those permitted classes can directly extend `Payment`.

---

# 107. What Must the Child Class Be?

A direct subclass of a sealed class must specify one of:

```text
final
sealed
non-sealed
```

### `final`

No further inheritance.

```java
final class CardPayment
        extends Payment {
}
```

### `sealed`

Continue restricting the hierarchy.

```java
sealed class UpiPayment
        extends Payment
        permits GooglePayPayment, PhonePePayment {
}
```

### `non-sealed`

Open the hierarchy again.

```java
non-sealed class BankTransferPayment
        extends Payment {
}
```

---

# 108. `final` vs `sealed`

## `final`

```text
No inheritance allowed.
```

## `sealed`

```text
Only specifically permitted inheritance allowed.
```

So:

```text
final   = zero subclasses
sealed  = controlled subclasses
```

---

# 109. Why Are Sealed Classes Useful?

They are useful when the domain has a known hierarchy.

Example:

```text
Payment
 ├── CardPayment
 ├── UpiPayment
 └── BankPayment
```

This helps with:

* domain modeling
* API design
* exhaustive pattern matching
* compiler reasoning

And it works especially well with pattern matching for `switch`.

---

# 110. Java 17 — Pattern Matching for `switch`

In Java 17 this was still a **preview** feature.

It eventually became permanent in Java 21. Oracle's Java language history records Java 17 as the first preview and Java 21 as the permanent release.

So remember:

```text
Java 17 → preview
Java 18 → second preview
Java 19 → third preview
Java 20 → fourth preview
Java 21 → final
```

---

# 111. Java 17 — Other Important Points

You may also hear about:

* stronger encapsulation of JDK internals
* enhanced pseudo-random number generators
* further work on Foreign Function & Memory API
* Vector API improvements
* context-specific deserialization filters

For a typical Spring Boot backend interview, **sealed classes + LTS status** deserve much more attention than memorizing all of these.

---

# 112. Java 18

Java 18 is not usually a deep interview-preparation version.

Important things to know:

### UTF-8 became the default charset in standard Java APIs where no charset is specified.

### Simple Web Server

A lightweight command-line web server was added for development/testing.

### Pattern Matching for `switch`

Continued as a preview feature.

### Finalization

Finalization was deprecated for future removal.

For normal interview preparation:

> Remember Java 18 as a bridge/continuation release rather than a major language release.

---

# 113. Java 19

Java 19 becomes interesting again.

The headline feature:

# Virtual Threads — Preview

Java 19 was where virtual threads first appeared as a preview.

Also:

* record patterns — preview
* pattern matching for switch — third preview
* structured concurrency — incubating
* Foreign Function & Memory API — incubating

Oracle's Java language history records record patterns and pattern matching work in Java 19, while the concurrency work introduced virtual threads in preview.

---

# 114. Java 20

Mostly continuation and refinement.

Important progression:

```text
Virtual Threads
Java 19 → first preview
Java 20 → second preview
Java 21 → final
```

Similarly:

```text
Record Patterns
19 → preview
20 → second preview
21 → final
```

and:

```text
Pattern Matching for switch
17 → preview
18 → second preview
19 → third preview
20 → fourth preview
21 → final
```

Oracle's Java 21 language summary documents these preview/final timelines.

---

# 115. Java 21 — LTS

Now we reach another major LTS release.

The most important Java 21 concepts for interviews are:

1. Virtual Threads
2. Pattern Matching for `switch`
3. Record Patterns
4. Sequenced Collections

Also know that Java 21 introduced several **preview** features, such as String Templates, Unnamed Patterns and Variables, and Unnamed Classes/Instance Main Methods.

---

# 116. Java 21 — Virtual Threads

This is one of the most important modern Java interview topics.

## First understand the problem.

Traditional Java server applications use platform threads.

Conceptually:

```text
Request 1 → Platform Thread 1
Request 2 → Platform Thread 2
Request 3 → Platform Thread 3
...
```

Platform threads are backed by operating-system threads and are relatively expensive.

If your application needs huge concurrency, creating massive numbers of platform threads is expensive.

---

# 117. Why Do Backend Requests Need Many Threads?

Consider a request:

```text
Receive HTTP request
        ↓
Call database
        ↓
Wait
        ↓
Call another service
        ↓
Wait
        ↓
Return response
```

A large part of the request lifecycle may involve:

```text
WAITING
```

for:

* database I/O
* network I/O
* REST calls
* file operations
* external services

The CPU is not continuously doing computation.

---

# 118. The Idea Behind Virtual Threads

Virtual threads are lightweight threads managed by the JVM.

The goal is:

> Make it practical to use a thread-per-task programming model even when the application has very large numbers of concurrent tasks, especially I/O-heavy tasks.

This is a key concept.

Instead of forcing the developer into a highly asynchronous programming style for every I/O operation, Java can support many lightweight concurrent tasks.

---

# 119. Creating a Virtual Thread

Simple API:

```java
Thread.startVirtualThread(() -> {
    System.out.println("Running in virtual thread");
});
```

---

# 120. Virtual Thread Executor

One of the most important APIs:

```java
try (ExecutorService executor =
         Executors.newVirtualThreadPerTaskExecutor()) {

    executor.submit(() -> processRequest());
    executor.submit(() -> processAnotherRequest());
}
```

The idea:

```text
one task
    ↓
one virtual thread
```

This is especially attractive for server-side workloads.

---

# 121. Platform Thread vs Virtual Thread

| Platform Thread                    | Virtual Thread                                |
| ---------------------------------- | --------------------------------------------- |
| backed by OS thread                | managed by JVM                                |
| relatively expensive               | lightweight                                   |
| fewer practical concurrent threads | huge numbers possible                         |
| useful for normal concurrency      | excellent for high-concurrency I/O-heavy work |
| consumes more system resources     | much cheaper per concurrent task              |

---

# 122. Virtual Threads Do NOT Mean More CPU

This is a very important interview point.

Do NOT say:

> "Virtual threads make the application faster."

That is too broad.

Suppose your task is:

```text
huge matrix calculation
CPU-heavy encryption
complex CPU computation
```

The workload is CPU-bound.

Virtual threads do not magically provide more CPU cores.

The main benefit is:

```text
high concurrency
+
many waiting/blocking operations
```

---

# 123. Best Use Case for Virtual Threads

Excellent fit:

```text
10,000 requests
        ↓
most of them waiting on DB / network / external services
```

Less useful as a magic optimization for:

```text
10,000 CPU-heavy tasks
```

---

# 124. Virtual Threads and Blocking Code

One of the major advantages is that you can often write straightforward blocking-style code:

```java
User user = userRepository.findById(id);

Payment payment =
        paymentClient.getPayment(user.getId());

Notification notification =
        notificationClient.getNotification(user.getId());
```

The programming model remains easy to understand.

Under the hood, virtual threads allow many blocked tasks to coexist much more efficiently than a huge number of platform threads.

---

# 125. Carrier Threads

A virtual thread does not directly map one-to-one permanently to an operating system thread.

The JVM schedules virtual threads onto platform threads called **carrier threads**.

Conceptually:

```text
Virtual Thread A ─┐
Virtual Thread B ─┤
Virtual Thread C ─┤──→ Carrier Platform Threads
Virtual Thread D ─┤
Virtual Thread E ─┘
```

When a virtual thread performs certain blocking operations, it can be suspended and another virtual thread can use the carrier thread.

This allows much higher concurrency.

---

# 126. Important Virtual Thread Caveat — Pinning

For Java 21 interview depth, know the concept of **pinning**.

Certain operations, especially blocking while holding a monitor or executing native code, can prevent a virtual thread from being unmounted efficiently.

Conceptually:

```text
virtual thread
      ↓
enters synchronized/native section
      ↓
blocks
      ↓
carrier may remain occupied
```

Therefore:

> Virtual threads reduce the cost of concurrency, but they do not eliminate the need to reason about synchronization and blocking behavior.

---

# 127. Virtual Threads and Thread Pools

A classic platform-thread design might use:

```java
Executors.newFixedThreadPool(100);
```

Why?

Because platform threads are expensive.

With virtual threads, one common model is:

```java
Executors.newVirtualThreadPerTaskExecutor();
```

You don't generally need the same "small fixed pool because threads are expensive" strategy for virtual threads.

However, you should still limit scarce external resources.

For example:

```text
Virtual threads: many
Database connections: limited
```

If the database supports only 100 connections, creating 100,000 virtual threads does not mean you should attempt 100,000 simultaneous DB operations.

The bottleneck is now the external resource.

---

# 128. Virtual Threads vs Reactive Programming

This can be a very good interview discussion.

Traditional blocking model:

```text
request
  ↓
thread
  ↓
wait for DB
  ↓
response
```

Reactive style often tries to avoid blocking and represent work as asynchronous pipelines.

Virtual threads allow another approach:

```text
request
  ↓
virtual thread
  ↓
blocking-style DB call
  ↓
resume
  ↓
response
```

This can make application code simpler while still supporting very high concurrency for the right workload.

---

# 129. Java 21 — Pattern Matching for `switch`

Before modern pattern matching, you might write:

```java
if (obj instanceof String) {
    String s = (String) obj;
    ...
} else if (obj instanceof Integer) {
    Integer i = (Integer) obj;
    ...
}
```

Java 21 lets switch work naturally with type patterns.

Example:

```java
static String describe(Object obj) {

    return switch (obj) {

        case String s ->
            "String: " + s;

        case Integer i ->
            "Integer: " + i;

        default ->
            "Other";
    };
}
```

Pattern matching for switch became permanent in Java 21.

---

# 130. Why Is Pattern Matching Better?

Old style:

```text
check
↓
cast
↓
use
```

New style:

```text
match type
↓
bind variable
↓
use directly
```

This reduces boilerplate.

---

# 131. Pattern Guard with `when`

You can make the condition more specific.

Example:

```java
static String classify(Object obj) {

    return switch (obj) {

        case Integer i when i > 0 ->
            "Positive";

        case Integer i ->
            "Zero or negative";

        default ->
            "Other";
    };
}
```

The same type can have multiple cases because the `when` condition separates them.

---

# 132. `null` in Pattern Switch

Modern switch can explicitly handle `null`:

```java
return switch (value) {

    case null -> "null";

    case String s -> "string";

    default -> "other";
};
```

This is useful because traditional switch semantics historically treated null differently.

---

# 133. Exhaustive Switch

A major benefit appears when working with sealed hierarchies.

Example:

```java
sealed interface Payment
        permits CardPayment, UpiPayment {
}

record CardPayment(String card) implements Payment {}
record UpiPayment(String upi) implements Payment {}
```

Then:

```java
static String type(Payment payment) {

    return switch (payment) {

        case CardPayment c -> "CARD";

        case UpiPayment u -> "UPI";
    };
}
```

Because the hierarchy is sealed and all permitted cases are covered, the compiler can reason about exhaustiveness.

This is one reason:

```text
sealed classes
+
pattern matching
```

work so well together.

---

# 134. Java 21 — Record Patterns

Record patterns are the natural next step after:

* records
* instanceof pattern matching
* switch pattern matching

Suppose:

```java
record Employee(
    String name,
    int age
) {}
```

Traditional:

```java
if (obj instanceof Employee employee) {

    String name = employee.name();
    int age = employee.age();

    System.out.println(name);
    System.out.println(age);
}
```

Record pattern:

```java
if (obj instanceof Employee(String name, int age)) {

    System.out.println(name);
    System.out.println(age);
}
```

The record is being **deconstructed**.

---

# 135. What Is Deconstruction?

With ordinary pattern matching:

```java
Employee e
```

means:

> "If this object is an Employee, bind the whole Employee to `e`."

With a record pattern:

```java
Employee(String name, int age)
```

means:

> "If this object is an Employee, also extract its components."

So:

```text
type matching
+
component extraction
```

---

# 136. Nested Record Patterns

Suppose:

```java
record Address(String city, String country) {}

record Employee(
    String name,
    Address address
) {}
```

You can reason about nested data using patterns.

Conceptually:

```java
if (obj instanceof Employee(
        String name,
        Address(String city, String country)
)) {

    ...
}
```

This becomes very powerful when used with `switch`.

---

# 137. Java 21 — Sequenced Collections

Java collections have often had the idea of encounter order, but APIs were not always consistent about:

```text
first element
last element
reverse order
```

Java 21 introduced common interfaces:

```text
SequencedCollection
SequencedSet
SequencedMap
```

Oracle lists these among the major Java 21 collection changes.

---

# 138. Why Sequenced Collections?

Before this improvement, different collection types had different APIs.

For example:

```text
List
Deque
SortedSet
LinkedHashSet
```

may all have some notion of order, but not always through the same method names.

Java 21 introduces a common abstraction for collections with a defined encounter order.

---

# 139. Important Sequenced Collection Methods

You should know the idea behind methods such as:

```java
getFirst()
getLast()
addFirst()
addLast()
reversed()
```

Example:

```java
SequencedCollection<String> values =
        new ArrayList<>();

values.addLast("A");
values.addLast("B");
values.addFirst("START");

System.out.println(values.getFirst());
System.out.println(values.getLast());
```

---

# 140. `reversed()`

A useful conceptual improvement:

```java
values.reversed();
```

This provides a reverse-ordered view rather than forcing every caller to reinvent reverse traversal logic.

The key interview idea:

> Java 21 standardizes APIs for collections that have a defined encounter order.

---

# 141. Java 21 — String Templates

This needs special care in interviews.

## String Templates were a PREVIEW feature in Java 21.

They were **not a final permanent Java 21 feature**.

Oracle explicitly lists String Templates as a preview feature in Java 21.

The general idea was to combine:

```text
literal text
+
embedded expressions
+
template processor
```

Example syntax from the Java 21 preview:

```java
String name = "Rishabh";

String message =
        STR."Hello \{name}";
```

---

# 142. Important String Template Interview Correction

Do **not** say:

> "String Templates automatically prevent SQL injection."

That is too strong.

A template mechanism itself is not a replacement for safe database APIs.

For SQL, you should still use:

```java
PreparedStatement
```

or your framework's parameterized query mechanism.

The important concept is:

> A template processor can define how embedded values are interpreted, escaped or transformed.

So string templates were designed as a flexible mechanism, not simply as a "string concatenation replacement that automatically makes everything secure."

---

# 143. Java 21 — Unnamed Patterns and Variables

This was also a preview feature.

The idea was:

> Sometimes you must provide a variable because the syntax requires it, but you don't actually use the variable.

Java 21 introduced `_` for unnamed patterns/variables in preview contexts.

Conceptually:

```text
"I need to match this component,
but I don't care about its value."
```

This reduces noise.

Oracle lists Unnamed Patterns and Variables as preview in Java 21.

---

# 144. Java 21 — Unnamed Classes and Instance Main Methods

Another Java 21 preview feature was simplifying very small Java programs.

Traditional:

```java
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```

The preview feature explored a much simpler style for beginner/small single-source programs.

This is more about:

* reducing ceremony
* beginner experience
* small programs

than backend architecture.

Treat it as a **preview feature**, not a core Java 21 production concept.

---

# 145. Java 21 — Scoped Values

Java 21 also had Scoped Values as a preview feature.

The high-level problem:

Traditional thread-local data uses:

```java
ThreadLocal<T>
```

But modern systems with huge numbers of virtual threads raise questions around context propagation.

Scoped values provide a mechanism for:

> sharing immutable context within a bounded dynamic scope.

Examples of context:

* request ID
* security identity
* tracing information

Important:

> In Java 21, Scoped Values were preview, not final.

---

# 146. Java 21 — Structured Concurrency

Structured concurrency was also a preview/incubating direction around Java 19–21.

The idea:

Instead of creating unrelated concurrent tasks and manually tracking them, treat related concurrent work as one structured unit.

For example:

```text
Request
 ├── call user service
 ├── call payment service
 └── call recommendation service
```

The lifecycle of child tasks should relate clearly to the parent task.

Benefits include easier reasoning about:

* cancellation
* error propagation
* task lifetime
* observability

Again:

> In Java 21 it was still a preview feature.

---

# 147. Java 21 — Foreign Function & Memory API

Java 21 also continued the evolution of the Foreign Function & Memory API.

Its goal is to allow Java applications to interact more safely with:

* native functions
* off-heap memory

without relying on the older JNI model for every use case.

For a normal Spring Boot interview, this is usually far less important than:

```text
Virtual Threads
Pattern Matching
Record Patterns
Sequenced Collections
```

---

# 148. Java 21 — One Big Picture

Think of Java 21 as four major themes:

### 1. Concurrency

```text
Virtual Threads
```

### 2. Data-Oriented Programming

```text
Records
+
Record Patterns
```

### 3. Type-Oriented Control Flow

```text
Pattern Matching for switch
+
Sealed Classes
```

### 4. Collection API Consistency

```text
Sequenced Collections
```

And then a set of preview features explored future directions.

---

# 149. Preview Features — Very Important Interview Concept

Java sometimes introduces a feature as:

```text
Preview
```

instead of immediately making it permanent.

Why?

Because the feature can be tested by developers and refined based on feedback before becoming a permanent language/API commitment.

That is why you see sequences like:

```text
switch expressions
Java 12 preview
Java 13 preview
Java 14 final
```

Similarly:

```text
records
Java 14 preview
Java 15 second preview
Java 16 final
```

and:

```text
sealed classes
Java 15 preview
Java 16 second preview
Java 17 final
```

Oracle explicitly tracks these permanent/preview transitions.

---

# 150. Feature Evolution Timeline

## Switch Expressions

```text
Java 12 → preview
Java 13 → preview
Java 14 → final
```

## Text Blocks

```text
Java 13 → preview
Java 14 → preview
Java 15 → final
```

## Records

```text
Java 14 → preview
Java 15 → second preview
Java 16 → final
```

## Pattern Matching for `instanceof`

```text
Java 14 → preview
Java 15 → second preview
Java 16 → final
```

## Sealed Classes

```text
Java 15 → preview
Java 16 → second preview
Java 17 → final
```

## Pattern Matching for `switch`

```text
Java 17 → preview
Java 18 → second preview
Java 19 → third preview
Java 20 → fourth preview
Java 21 → final
```

## Record Patterns

```text
Java 19 → preview
Java 20 → second preview
Java 21 → final
```

## Virtual Threads

```text
Java 19 → preview
Java 20 → second preview
Java 21 → final
```

Oracle's Java language summaries document these timelines.

---

# 151. Important Comparison — Record vs Normal Class

| Record                             | Normal Class                         |
| ---------------------------------- | ------------------------------------ |
| designed for data carriers         | general-purpose object               |
| concise syntax                     | more boilerplate                     |
| component-based accessors          | traditional getters/setters possible |
| generated equals/hashCode/toString | you implement/control them           |
| cannot extend another class        | can extend class                     |
| can implement interfaces           | can implement interfaces             |
| ideal for DTO/value-like data      | better for complex behavior/state    |

---

# 152. Important Comparison — Sealed vs Final vs Abstract

| Keyword          | Meaning                                               |
| ---------------- | ----------------------------------------------------- |
| `final class`    | nobody can extend                                     |
| `sealed class`   | only explicitly permitted classes can extend          |
| `abstract class` | cannot instantiate directly; designed for inheritance |
| `non-sealed`     | reopens inheritance under a sealed hierarchy          |

Mental model:

```text
final    → stop inheritance
sealed   → control inheritance
abstract → enable/define inheritance contract
```

---

# 153. Important Comparison — `var` vs Dynamic Typing

### Java

```java
var value = "Java";
```

Compiler infers:

```java
String
```

Later:

```java
value = 100; // compile error
```

### Dynamic language idea

A variable may change runtime type.

That is NOT what Java `var` does.

So:

> Java remains statically typed.

---

# 154. Important Comparison — `map()` vs `flatMap()`

### `map`

```text
one input
   ↓
one output
```

### `flatMap`

```text
one input
   ↓
multiple/nested result
   ↓
flatten
```

Examples:

```java
Stream<String>
    .map(...)
```

versus:

```java
Stream<List<String>>
    .flatMap(...)
```

The same concept appears in:

* Stream API
* Optional
* CompletableFuture-like composition patterns

---

# 155. Important Comparison — `thenApply()` vs `thenCompose()`

### `thenApply`

Use when:

```text
A → B
```

Example:

```java
future.thenApply(User::getName);
```

### `thenCompose`

Use when:

```text
A → Future<B>
```

Example:

```java
getUser()
    .thenCompose(user ->
        getOrders(user.id())
    );
```

Think:

```text
thenApply  = transformation
thenCompose = async chaining
```

---

# 156. Important Comparison — `thenCombine()`

Suppose you have:

```text
Future<A>
Future<B>
```

and want:

```text
Future<C>
```

Use:

```java
futureA.thenCombine(
    futureB,
    (a, b) -> combine(a, b)
);
```

Think:

```text
two independent async branches
              ↓
        combine result
```

---

# 157. Important Comparison — `Optional.orElse()` vs `orElseGet()`

### `orElse`

```java
optional.orElse(expensiveCall());
```

The fallback expression may be evaluated even if the value exists.

### `orElseGet`

```java
optional.orElseGet(() -> expensiveCall());
```

The supplier runs only if needed.

Remember:

```text
orElse    → eager fallback evaluation
orElseGet → lazy fallback evaluation
```

---

# 158. Important Comparison — Stream vs Parallel Stream

Normal:

```java
list.stream()
```

Parallel:

```java
list.parallelStream()
```

Do not assume parallel is faster.

Ask:

```text
Is the dataset large?
Is the operation CPU-heavy?
Is the operation independent?
Is there synchronization?
Does ordering matter?
Is the source splittable efficiently?
```

If not, sequential processing may be better.

---

# 159. Important Comparison — Platform Thread vs Virtual Thread

### Platform thread

```text
relatively expensive
OS-backed
limited practical quantity
```

### Virtual thread

```text
very lightweight
JVM-managed
huge concurrency
excellent for I/O-heavy workloads
```

But:

```text
virtual threads ≠ more CPU
```

---

# 160. Important Comparison — Record Pattern vs `instanceof` Pattern

### `instanceof` pattern

```java
if (obj instanceof Employee e) {
    System.out.println(e.name());
}
```

You extract the object.

### Record pattern

```java
if (obj instanceof Employee(String name, int age)) {
    ...
}
```

You extract the components.

So:

```text
instanceof pattern = bind object
record pattern      = deconstruct object
```

---

# 161. Important Comparison — `switch` Statement vs `switch` Expression

### Statement

```java
switch (x) {
    case 1:
        doSomething();
        break;
}
```

Its purpose is control flow.

### Expression

```java
String result =
        switch (x) {
            case 1 -> "one";
            default -> "other";
        };
```

Its purpose includes producing a value.

Think:

```text
statement  = action
expression = value
```

---

# 162. Java Version Timeline — Interview Revision

```text
Java 8
├── Lambda
├── Functional Interfaces
├── Streams
├── Optional
├── Default Methods
├── java.time
└── CompletableFuture

Java 9
├── Modules
├── List.of / Set.of / Map.of
├── JShell
└── Private interface methods

Java 10
└── var

Java 11
├── LTS
├── HTTP Client
├── String API improvements
├── Files.readString/writeString
└── var in lambda parameters

Java 12
└── switch expressions preview

Java 13
├── Text blocks preview
└── switch/yield evolution

Java 14
├── switch expressions final
├── records preview
├── instanceof pattern preview
└── helpful NPE

Java 15
├── Text blocks final
└── sealed classes preview

Java 16
├── Records final
└── instanceof pattern final

Java 17
├── LTS
└── Sealed classes final

Java 18
├── UTF-8 default charset
└── Simple Web Server

Java 19
├── Virtual threads preview
├── Record patterns preview
└── Structured concurrency incubating

Java 20
├── Virtual threads second preview
├── Record patterns second preview
└── switch pattern fourth preview

Java 21
├── LTS
├── Virtual Threads final
├── Pattern Matching for switch final
├── Record Patterns final
├── Sequenced Collections
└── several preview features
```

Oracle's official Java 21 language summary confirms the release-by-release status of these language features.

---

# 163. The Four LTS Releases You Should Immediately Remember

```text
8 → 11 → 17 → 21
```

A simple memory chain:

```text
Java 8
Functional Programming

Java 11
Modern APIs + HTTP Client

Java 17
Sealed Classes

Java 21
Virtual Threads + Pattern Matching
```

---

# 164. 30-Second Java 8 Interview Answer

> "Java 8 was a major shift toward functional programming. It introduced lambda expressions and functional interfaces, which reduced boilerplate and allowed behavior to be passed around. The Stream API enabled declarative processing of collections, Optional provided explicit handling of absent values, interfaces gained default and static methods, Java introduced the modern `java.time` API, and CompletableFuture made asynchronous computation easier to compose."

---

# 165. 30-Second Java 11 Interview Answer

> "Java 11 is an LTS release. Important practical additions include the standardized `HttpClient` API with HTTP/2 and async support, String methods such as `isBlank`, `strip` and `repeat`, easier text file operations through `Files.readString` and `writeString`, and support for `var` in implicitly typed lambda parameters."

---

# 166. 30-Second Java 17 Interview Answer

> "Java 17 is another LTS release. The most important language feature is sealed classes, which let us explicitly restrict which classes can extend a class or implement an interface. Java 17 also previewed pattern matching for switch, which later became final in Java 21. Records and pattern matching for instanceof were already permanent by this point."

---

# 167. 30-Second Java 21 Interview Answer

> "Java 21 is an LTS release and is especially important for modern backend development because virtual threads became permanent, making high-concurrency I/O-heavy workloads easier to handle with a thread-per-task programming model. Java 21 also finalized pattern matching for switch and record patterns, and introduced sequenced collection interfaces for a common ordered-collection API."

---

# 168. If Interviewer Asks: "What Changed From Java 8 to Java 17?"

A strong answer:

> "The major evolution was from functional programming in Java 8 toward more expressive type-safe syntax and better modeling. Java 8 gave us lambdas, streams, Optional and CompletableFuture. Java 9 added modules, Java 10 added `var`, Java 11 provided the standardized HTTP Client and useful API improvements, Java 14 introduced switch expressions as a standard feature and started the evolution of records and pattern matching, Java 15 finalized text blocks, Java 16 finalized records and pattern matching for instanceof, and Java 17 finalized sealed classes. So the overall direction was reducing boilerplate, improving readability, making data modeling more concise and enabling stronger control over type hierarchies."

---

# 169. If Interviewer Asks: "What Changed From Java 8 to Java 21?"

Use this structure:

```text
Java 8
Functional programming

Java 9
Modularity

Java 10
Type inference

Java 11
Modern standard APIs + LTS

Java 12–16
More expressive syntax:
switch
text blocks
records
pattern matching

Java 17
Sealed classes + LTS

Java 19–20
Virtual threads / record patterns as previews

Java 21
Virtual threads final
pattern matching for switch final
record patterns final
sequenced collections
+ other preview features
```

Then let the interviewer choose where to go deeper.

---

# 170. Most Common Java 8 Interview Questions

Be ready for:

1. What is a lambda expression?
2. What is a functional interface?
3. Can an interface have multiple default methods?
4. What is the difference between Predicate, Function, Consumer and Supplier?
5. What is a method reference?
6. What is the difference between map and flatMap?
7. What are intermediate and terminal operations?
8. Why are streams lazy?
9. Stream vs Collection?
10. Sequential stream vs parallel stream?
11. What is Optional?
12. Does Optional eliminate NullPointerException?
13. `orElse()` vs `orElseGet()`?
14. `findFirst()` vs `findAny()`?
15. `reduce()` vs `collect()`?
16. What is `groupingBy()`?
17. Why were default methods introduced?
18. What are the Java 8 Date/Time classes?
19. `LocalDateTime` vs `ZonedDateTime`?
20. `thenApply()` vs `thenCompose()`?
21. `thenCompose()` vs `thenCombine()`?
22. How does CompletableFuture handle exceptions?
23. What is the difference between `runAsync()` and `supplyAsync()`?

---

# 171. Most Common Java 11 Questions

Be ready for:

1. Why is Java 11 important?
2. What is the Java 11 HTTP Client?
3. HTTP/1.1 vs HTTP/2 support?
4. How does `sendAsync()` relate to CompletableFuture?
5. What String methods were added?
6. `strip()` vs `trim()`?
7. What are `readString()` and `writeString()`?
8. What changed with `var` in Java 11?
9. Why use `var` in lambda parameters?

---

# 172. Most Common Java 17 Questions

Be ready for:

1. Why is Java 17 important?
2. What are sealed classes?
3. Sealed vs final?
4. What is `permits`?
5. What can a permitted subclass be?
6. What is `non-sealed`?
7. How do sealed classes work with pattern matching?
8. Was pattern matching for switch final in Java 17?
9. When did records become final?
10. When did pattern matching for instanceof become final?

---

# 173. Most Common Java 21 Questions

Be ready for:

1. What are virtual threads?
2. Platform thread vs virtual thread?
3. Why are virtual threads useful?
4. Are virtual threads faster?
5. Are virtual threads useful for CPU-heavy workloads?
6. What is a carrier thread?
7. What is pinning?
8. How do you create a virtual thread?
9. What is `newVirtualThreadPerTaskExecutor()`?
10. What is pattern matching for switch?
11. What are record patterns?
12. What are sequenced collections?
13. What were the preview features in Java 21?
14. Were String Templates final in Java 21?
15. What is structured concurrency?
16. What are scoped values?

---

# 174. Common Interview Traps

## Trap 1

> "`var` makes Java dynamically typed."

Wrong.

Correct:

> "`var` is compile-time local variable type inference. Java remains statically typed."

---

## Trap 2

> "Optional eliminates NullPointerException."

Wrong.

Correct:

> "Optional makes absence explicit and helps reduce certain null-related mistakes."

---

## Trap 3

> "Parallel stream is always faster."

Wrong.

Correct:

> "Parallel streams can help for suitable workloads, but overhead, synchronization, ordering and dataset size must be considered."

---

## Trap 4

> "Virtual threads make all applications faster."

Wrong.

Correct:

> "Virtual threads primarily improve scalability for high-concurrency workloads with many blocking/waiting operations."

---

## Trap 5

> "Records are completely immutable."

Too strong.

Correct:

> "Records provide final record components and are designed as shallowly immutable data carriers, but referenced mutable objects can still change."

---

## Trap 6

> "Sealed class means nobody can extend it."

Wrong.

That describes:

```java
final
```

Sealed means:

> only explicitly allowed subclasses can directly extend it.

---

## Trap 7

> "Pattern matching for switch came in Java 21."

Incomplete.

Better:

> "It was previewed starting in Java 17 and became permanent in Java 21."

---

## Trap 8

> "Records came in Java 16."

Incomplete.

Better:

> "Records were first previewed in Java 14 and became permanent in Java 16."

---

## Trap 9

> "Sealed classes came in Java 17."

Better:

> "Sealed classes were first previewed in Java 15 and became permanent in Java 17."

---

## Trap 10

> "String Templates are a Java 21 feature."

Needs clarification.

Correct:

> "String Templates were introduced as a preview feature in Java 21; they were not a permanent Java 21 language feature."

---

# 175. Mental Model for the Whole Evolution

You should be able to see Java's evolution like this:

```text
Java 8
"What if Java could work with behavior as data?"
        ↓
Lambdas
Functional Interfaces
Streams
CompletableFuture

Java 9
"What if Java applications were more modular?"
        ↓
Modules

Java 10
"What if we don't repeat obvious types?"
        ↓
var

Java 11
"What common backend APIs are still too verbose?"
        ↓
HTTP Client
String APIs
File APIs

Java 12–16
"Can Java syntax become more expressive?"
        ↓
switch expressions
text blocks
records
pattern matching

Java 17
"Can we control our type hierarchy?"
        ↓
sealed classes

Java 19–21
"How do we handle massive concurrency
and structured data more naturally?"
        ↓
virtual threads
record patterns
pattern matching for switch
sequenced collections
```

---

# 176. One-Page Final Revision

## Java 8

```text
Lambda
Functional Interface
Method Reference
Stream API
Optional
Default Methods
java.time
CompletableFuture
```

Remember:

```text
Java 8 = Functional Programming
```

---

## Java 9

```text
Module System
List.of / Set.of / Map.of
JShell
Private interface methods
```

Remember:

```text
Java 9 = Modularity
```

---

## Java 10

```text
var
```

Remember:

```text
Java 10 = Local variable type inference
```

---

## Java 11

```text
LTS
HTTP Client
String improvements
Files.readString/writeString
var in lambda parameters
```

Remember:

```text
Java 11 = LTS + practical APIs
```

---

## Java 12

```text
switch expressions preview
```

---

## Java 13

```text
text blocks preview
switch/yield evolution
```

---

## Java 14

```text
switch expressions final
records preview
instanceof pattern preview
helpful NPE
```

---

## Java 15

```text
text blocks final
sealed classes preview
```

---

## Java 16

```text
records final
instanceof pattern final
```

---

## Java 17

```text
LTS
sealed classes final
switch pattern preview
```

Remember:

```text
Java 17 = LTS + Sealed Classes
```

---

## Java 18

```text
UTF-8 default charset
Simple Web Server
switch pattern second preview
```

---

## Java 19

```text
Virtual Threads preview
Record Patterns preview
Structured Concurrency
```

---

## Java 20

```text
Virtual Threads second preview
Record Patterns second preview
Switch Pattern fourth preview
```

---

## Java 21

```text
LTS

Virtual Threads final
Pattern Matching for switch final
Record Patterns final
Sequenced Collections

Preview:
String Templates
Unnamed Patterns/Variables
Unnamed Classes
Scoped Values
Structured Concurrency
```

---

# 177. The Best Answer to "Which Java Version Do You Use?"

Do not say only:

> Java 17.

Say:

> "My primary project environment is Java 17. I’m comfortable with the major Java 8 features such as streams, lambdas and CompletableFuture, and I’m also familiar with the modern Java evolution through Java 21, including records, sealed classes, pattern matching and virtual threads."

Then only discuss features you genuinely understand.

---

# 178. Final Interview Mindset

Do not memorize:

```text
JEP 123
JEP 456
Java 14
Java 15
Java 16
...
```

unless the role specifically requires that depth.

Instead remember the story:

```text
Java 8
→ functional programming

Java 9
→ modules

Java 10
→ var

Java 11
→ LTS + useful APIs

Java 12–16
→ more expressive syntax

Java 17
→ sealed classes + LTS

Java 19–20
→ modern concurrency/data-pattern previews

Java 21
→ virtual threads + pattern matching + record patterns
```

When you understand that story, the individual features become much easier to remember.

---

# 179. Final "Tell Me About Java 8 to 21" Answer

> "The biggest evolution from Java 8 to Java 21 is that Java has gradually become more expressive, concise and concurrency-friendly while remaining backward compatible and statically typed. Java 8 introduced functional programming through lambdas, functional interfaces and streams, along with Optional, the modern date/time API and CompletableFuture. Java 9 introduced the module system, Java 10 introduced local variable type inference with `var`, and Java 11, another LTS release, added APIs such as the standard HTTP Client and improved String and File operations. From Java 12 through 16, Java introduced and finalized features such as switch expressions, text blocks, records and pattern matching for instanceof. Java 17, another LTS release, finalized sealed classes. Java 19 and 20 previewed major concurrency and pattern features, and Java 21 finalized virtual threads, pattern matching for switch and record patterns, while also introducing sequenced collections. Overall, Java has been moving toward less boilerplate, better data modeling, safer type handling and scalable concurrency."

---

# 180. Final Priority Order for Interview Preparation

If you have limited preparation time, study in this order:

## Priority 1 — Java 8

```text
Lambda
Functional Interfaces
Streams
Optional
CompletableFuture
Default Methods
Date/Time
```

## Priority 2 — Java 17

```text
Sealed Classes
Records
Pattern Matching
Switch Expressions
```

## Priority 3 — Java 21

```text
Virtual Threads
Pattern Matching for switch
Record Patterns
Sequenced Collections
```

## Priority 4 — Java 11

```text
HTTP Client
String APIs
Files API
var in lambda
```

## Priority 5 — Java 9/10

```text
Modules
var
Collection factory methods
JShell
```

## Priority 6 — Remaining versions

Know the **feature + version + evolution timeline**, but don't spend equal time on them.

---

# 181. Final Memory Map

```text
                         JAVA 8 → JAVA 21
                                |
        ------------------------------------------------
        |                      |                       |
   EXPRESSIVENESS          DATA MODELING          CONCURRENCY
        |                      |                       |
      Java 8                 Records               CompletableFuture
      Lambdas                Sealed Classes        Virtual Threads
      Streams                Pattern Matching      Structured Concurrency
      switch                 Record Patterns
      Text Blocks
        |
        ------------------------------------------------
                                |
                        MODERN JAVA MINDSET
                                |
              ----------------------------------
              |                |               |
          Less Boilerplate   Safer Types   Better Scalability
              |                |               |
             var           Records/Sealed   Virtual Threads
          Text Blocks      Pattern Matching
```

---

# 182. Final Rule for Interviews

For every Java feature, answer in this sequence:

```text
1. What problem existed?
2. What did Java introduce?
3. How does it work?
4. Show a small example.
5. What is the practical use?
6. What is the common trap?
```

For example:

### Virtual Threads

```text
Problem:
Too many platform threads are expensive.

Feature:
Virtual threads.

How:
Lightweight JVM-managed threads.

Use:
High-concurrency I/O-heavy workloads.

Trap:
They don't magically make CPU-bound workloads faster.
```

### Records

```text
Problem:
DTO/data carrier classes have boilerplate.

Feature:
Records.

How:
Compact declaration + generated members.

Use:
DTOs/value-like data.

Trap:
Record immutability is shallow, not deep.
```

### Sealed Classes

```text
Problem:
Inheritance hierarchy is too open.

Feature:
sealed + permits.

How:
Only approved subclasses can directly extend.

Use:
Controlled domain hierarchies.

Trap:
sealed ≠ final.
```

### Streams

```text
Problem:
Collection processing is verbose.

Feature:
Stream API.

How:
Source → intermediate operations → terminal operation.

Use:
Declarative data processing.

Trap:
Streams are not collections and parallel streams are not always faster.
```

### `var`

```text
Problem:
Repeated obvious local types.

Feature:
Local variable type inference.

How:
Compiler infers type from initializer.

Use:
Reduce local-variable boilerplate.

Trap:
var ≠ dynamic typing.
```

### Pattern Matching

```text
Problem:
instanceof + cast + manual extraction.

Feature:
Pattern matching.

How:
Match and bind in one construct.

Use:
Cleaner type-based logic.

Trap:
Understand which release made each pattern feature permanent.
```

---

# End

**Core memory chain:**

```text
8  → Functional
9  → Modular
10 → var
11 → LTS + APIs
12 → switch preview
13 → text blocks preview
14 → switch final + records/instanceof preview
15 → text blocks final + sealed preview
16 → records + instanceof final
17 → LTS + sealed final
18 → UTF-8 / web server
19 → virtual threads preview
20 → second previews
21 → LTS + virtual threads + patterns
```

**LTS chain:**

```text
8 → 11 → 17 → 21
```

**Most important interview concepts:**

```text
Java 8:
Lambda
Functional Interface
Stream
Optional
CompletableFuture

Java 11:
HTTP Client
String/File APIs
var in lambda

Java 17:
Sealed Classes

Java 21:
Virtual Threads
Pattern Matching for switch
Record Patterns
Sequenced Collections
```

**The most important idea is not remembering feature names.**

Understand the evolution:

```text
Less boilerplate
        ↓
More expressive code
        ↓
Better type modeling
        ↓
Better concurrency
        ↓
Better scalability
```
