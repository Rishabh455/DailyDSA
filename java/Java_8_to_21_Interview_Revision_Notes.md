# Java 8 → Java 21 Interview Revision Notes

> **Purpose:** One-file revision notes for Java interviews. The goal is not to memorize feature names. For every feature, first understand **the problem it solved**, then remember the syntax only as a consequence.
>
> **Scope:** Java 8 through Java 21, with emphasis on language features, common core-library additions, concurrency, JVM/runtime changes that are interview-relevant, and the differences between permanent, preview, and incubator features.
>
> **Source basis:** A transcript of a video titled **“Java New Features : Java 10 to Java 21”**. The video directly explains switch expressions, `var`, text blocks, string templates, sealed classes, and record classes; the transcript is supplemented here with the missing releases/features so the notes cover the complete interview-relevant Java 8–21 evolution.
>
> **Important version rule:** A feature being demonstrated in a release does **not** always mean it became a permanent Java feature in that release. “Preview” and “Incubator” matter in interviews.

---

## 0. The 30-second mental model of Java evolution

Think of modern Java in four phases:

1. **Java 8 — make Java expressive**
   - Lambdas, functional interfaces, Stream API, `Optional`, modern date/time API.
   - The big shift: from “tell Java every step” toward “describe what you want done.”

2. **Java 9–13 — make the platform easier to evolve and the syntax cleaner**
   - Modules, better APIs, `var`, HTTP Client, String/Files improvements, switch-expression previews, text-block preview.

3. **Java 14–17 — make everyday code less boilerplate-heavy and more type-aware**
   - Switch expressions become permanent, records, pattern matching for `instanceof`, sealed classes, stronger JDK encapsulation.

4. **Java 19–21 — modern concurrency + pattern/data modeling**
   - Virtual threads, record patterns, pattern matching for `switch`, sequenced collections, plus several preview features.

### The evolution story in one line

**Java 8:** behavior becomes easier to pass around → **Java 10:** type inference removes noise → **Java 14–17:** data and type checks become concise → **Java 21:** concurrency and pattern-based data processing become much more expressive.

---

# 1. Java 8 — the foundation of modern Java

Java 8 is still the most important “modern Java” version for interviews because many features introduced here are used every day.

## 1.1 Lambda expressions

### Problem before Java 8

To pass behavior, Java traditionally needed an anonymous class.

```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Running");
    }
};
```

### Java 8 idea

A lambda is a compact way to represent an implementation of a **functional interface**.

```java
Runnable r = () -> System.out.println("Running");
```

### Mental model

A lambda is **behavior as a value**.

```text
Data as value      -> int x = 10
Behavior as value  -> x -> x * 2
```

### Interview points

- Lambda does not create a new general-purpose “function type” in Java; it targets a **functional interface**.
- A functional interface has exactly one abstract method.
- It can still have `default` and `static` methods.
- `@FunctionalInterface` is optional but useful because the compiler verifies the contract.

Common functional interfaces:

```java
Predicate<T>      // T -> boolean
Function<T, R>    // T -> R
Consumer<T>       // T -> void
Supplier<T>       // () -> T
UnaryOperator<T>  // T -> T
BinaryOperator<T> // (T, T) -> T
```

### Quick memory hook

```text
Predicate  = test something
Function   = transform something
Consumer   = consume something
Supplier   = provide something
```

---

## 1.2 Method references

If the lambda only forwards its arguments to an existing method, method reference is often cleaner.

```java
names.forEach(name -> System.out.println(name));
```

becomes:

```java
names.forEach(System.out::println);
```

Common forms:

```text
ClassName::staticMethod
object::instanceMethod
ClassName::instanceMethod
ClassName::new
```

Example:

```java
List<String> names = List.of("A", "B");
names.forEach(System.out::println);
```

### Mental model

**Lambda = write the behavior inline.**

**Method reference = reuse an existing method as that behavior.**

---

## 1.3 Default and static methods in interfaces

### Problem

Suppose an interface is already implemented by thousands of classes. You want to add a method without breaking all old implementations.

Java 8 introduced `default` methods so the interface can provide an implementation.

```java
interface Vehicle {
    default void start() {
        System.out.println("Starting...");
    }

    static void info() {
        System.out.println("Vehicle interface");
    }
}
```

### Why this matters

It enabled Java library designers to evolve interfaces while preserving compatibility for existing implementations.

### Interview trap

A default method is **inherited**, while an interface static method is called using the interface name:

```java
Vehicle.info();
```

not through an implementing object.

---

## 1.4 Stream API

### Problem

Collection processing often becomes loop-heavy and mixes:

- what data we want,
- how we iterate,
- how we transform it,
- how we collect it.

Streams separate the **pipeline** from the source collection.

```java
List<String> result = names.stream()
        .filter(name -> name.length() > 3)
        .map(String::toUpperCase)
        .toList();
```

### Mental model

A stream is a **pipeline of computation over data**, not a data structure itself.

```text
Source -> intermediate operations -> terminal operation
```

Example:

```text
List
 ↓
filter   (lazy)
 ↓
map      (lazy)
 ↓
toList   (terminal)
```

### Intermediate vs terminal

**Intermediate operations** return another stream:

- `filter`
- `map`
- `flatMap`
- `distinct`
- `sorted`
- `limit`
- `skip`
- `peek`

**Terminal operations** finish the pipeline:

- `collect`
- `toList`
- `forEach`
- `reduce`
- `count`
- `findFirst`
- `findAny`
- `anyMatch`
- `allMatch`
- `noneMatch`

### Why streams are lazy

This:

```java
stream.filter(...).map(...).findFirst();
```

does not process the entire collection first. The pipeline can stop as soon as `findFirst()` gets an answer.

That is why short-circuiting operations matter:

```text
anyMatch / allMatch / noneMatch / findFirst / findAny / limit
```

### Stream does not store data

A `List` stores elements.

A `Stream` describes a computation over elements.

### Stream vs Collection

```text
Collection = data
Stream     = computation on data
```

### Parallel streams

```java
list.parallelStream()
```

can execute work in parallel, but that does **not** mean it is automatically faster.

For interview answers, remember:

- good for large, independent, CPU-bound operations in suitable situations;
- poor fit for blocking I/O;
- shared mutable state inside stream operations is dangerous;
- ordering and splitting costs matter.

---

## 1.5 Optional

### Problem

Repeated nullable-return logic often creates:

```java
if (value != null) {
    ...
}
```

`Optional<T>` makes “value may be absent” explicit in an API.

```java
Optional<String> name = Optional.of("Rishabh");
```

Common methods:

```java
Optional.of(value)
Optional.ofNullable(value)
Optional.empty()
isPresent()
ifPresent(...)
orElse(...)
orElseGet(...)
orElseThrow(...)
map(...)
flatMap(...)
filter(...)
```

### Important interview difference

```java
optional.orElse(expensiveMethod())
```

can evaluate the fallback even when the value is present.

```java
optional.orElseGet(() -> expensiveMethod())
```

calculates the fallback lazily.

### Do not oversell Optional

`Optional` is primarily useful for expressing **possibly-absent return values**. It is not a replacement for every nullable field, parameter, or every null check in an application.

---

## 1.6 New Date/Time API (`java.time`)

Java 8 introduced the modern date/time API based on clearer types and immutability.

Important types:

```text
LocalDate       -> date only
LocalTime       -> time only
LocalDateTime   -> date + time, no zone
ZonedDateTime   -> date + time + zone
Instant         -> point on global timeline
Duration        -> time-based amount
Period          -> date-based amount
```

Example:

```java
LocalDate today = LocalDate.now();
LocalDate nextWeek = today.plusWeeks(1);
```

### Interview trap

`LocalDateTime` does **not** contain time-zone information.

For distributed systems, timestamps often map naturally to `Instant`, while user-facing local time may need `ZonedDateTime`.

---

## 1.7 CompletableFuture

Java 8 added the standard API for composing asynchronous stages.

```java
CompletableFuture.supplyAsync(() -> getUser())
        .thenApply(User::getName)
        .thenAccept(System.out::println);
```

Mental model:

```text
Future                    -> “I will have a value later.”
CompletableFuture          -> “I can compose asynchronous stages.”
```

Important methods:

```text
thenApply     -> transform result
thenAccept    -> consume result
thenCompose   -> chain another async operation
thenCombine   -> combine two independent futures
exceptionally -> fallback for failure
handle        -> inspect result + exception
allOf         -> wait for multiple futures
```

### `thenApply` vs `thenCompose`

```text
thenApply:
T -> R

thenCompose:
T -> CompletableFuture<R>
```

Use `thenCompose` when the next operation is itself asynchronous and you want to avoid nested futures.

---

## 1.8 Other Java 8 features worth knowing

- Base64 API: `java.util.Base64`
- Repeatable annotations
- Type annotations
- Nashorn JavaScript engine (later removed, not a current interview focus)
- More useful APIs across collections and concurrency

---

# 2. Java 9 — modules + API quality improvements

## 2.1 Java Platform Module System (JPMS / Project Jigsaw)

### Problem

The classpath becomes difficult to manage in large systems:

- dependency conflicts;
- weak encapsulation;
- accidental exposure of internal packages;
- difficult runtime composition.

Java 9 introduced **modules**.

A module explicitly describes:

- what it needs;
- what it exposes;
- optionally what services it provides/uses.

Example:

```java
module com.example.app {
    requires java.sql;
    exports com.example.api;
}
```

Important directives:

```text
requires   -> dependency on another module
exports    -> package accessible to other modules
opens      -> reflective access to package
uses       -> consumes a service
provides   -> provides an implementation of a service
```

### Mental model

```text
Classpath:
“Here are many classes; good luck resolving everything.”

Module system:
“Here is a named component with declared dependencies and boundaries.”
```

### Interview point

`exports` and `opens` are not the same.

- `exports` is about normal access to public types.
- `opens` is primarily about deep reflection.

---

## 2.2 Collection factory methods

Java 9 added concise factory methods:

```java
List<String> list = List.of("A", "B");
Set<String> set = Set.of("A", "B");
Map<String, Integer> map = Map.of("A", 1, "B", 2);
```

These produce **unmodifiable** collections.

Typical interview properties:

- no `add`/`remove`/`put` mutation;
- `List.of`, `Set.of`, `Map.of` reject `null` elements/keys/values;
- duplicate set elements or duplicate map keys are rejected by the factory.

### `List.of(...)` vs `Arrays.asList(...)`

```text
List.of(...)        -> unmodifiable
Arrays.asList(...)  -> fixed-size list backed by array; set() is allowed
```

---

## 2.3 More concise try-with-resources

If the resource is already final/effectively final, Java 9 allows:

```java
BufferedReader br = Files.newBufferedReader(path);

try (br) {
    ...
}
```

instead of redeclaring it inside `try(...)`.

### Mental model

The resource still needs to be `AutoCloseable`; Java 9 simply removes duplication.

---

## 2.4 Private methods in interfaces

A `default` method can share implementation through a private interface method.

```java
interface Logger {
    default void logInfo(String msg) {
        log("INFO", msg);
    }

    default void logError(String msg) {
        log("ERROR", msg);
    }

    private void log(String level, String msg) {
        System.out.println(level + ": " + msg);
    }
}
```

Purpose: avoid duplicated helper logic inside multiple default methods.

---

## 2.5 Stream / Optional improvements worth remembering

Java 9 added useful APIs such as:

```text
Stream.takeWhile(...)
Stream.dropWhile(...)
Stream.iterate(seed, hasNext, next)
Optional.ifPresentOrElse(...)
Optional.or(...)
Optional.stream()
```

The important idea is that these make pipelines and absent-value handling easier without inventing custom loops.

---

## 2.6 JShell

`jshell` is an interactive Java REPL.

Useful for:

- quickly trying APIs;
- checking syntax;
- experimenting without creating a full class/project.

---

## 2.7 Process API improvements

Java 9 made working with OS processes more useful via `ProcessHandle` and related APIs.

Interview relevance: moderate, but know the theme:

```text
Java code can inspect/manage operating-system processes more cleanly.
```

---

# 3. Java 10 — `var` and local variable type inference

## 3.1 `var`

### Problem

Sometimes the type is obvious from the right side, so repeating it creates noise.

Before:

```java
BufferedReader reader = new BufferedReader(...);
ArrayList<String> names = new ArrayList<>();
```

Java 10:

```java
var reader = new BufferedReader(...);
var names = new ArrayList<String>();
```

### The key concept

`var` means **the compiler infers the static type**.

It does **not** mean Java becomes dynamically typed.

```java
var number = 10;
```

`number` is still statically typed as `int`.

You cannot later do:

```java
number = "hello"; // compile-time error
```

### Why?

The compiler sees the initializer and determines the type at compile time.

```text
var number = 10
       ↓
compiler infers int
       ↓
static type remains int
```

### Rules

`var`:

- can be used for local variables;
- needs an initializer;
- cannot be used for fields as a general type-inference mechanism;
- cannot be used as a method return type;
- cannot be used as a method parameter type;
- cannot be initialized with just `null` because the type cannot be inferred.

Invalid:

```java
var x;          // no initializer
var y = null;   // cannot infer type
```

### Important correction for interviews

Do not say:

> “`var` is dynamic typing.”

Say:

> “`var` is local-variable type inference; Java remains statically typed and the inferred type is fixed at compile time.”

### Good vs bad usage

Good:

```java
var customer = customerService.findById(id);
```

when the right side makes the type obvious.

Potentially poor:

```java
var result = service.doSomething();
```

when the returned type is unclear to the reader.

### Mental model

`var` removes **repetition**, not **type safety**.

---

## 3.2 `List.copyOf`, `Set.copyOf`, `Map.copyOf`

Java 10 introduced convenient unmodifiable copies:

```java
List<String> copy = List.copyOf(source);
```

This is useful when an API wants to expose a snapshot-like unmodifiable view/value rather than a mutable collection.

### Interview distinction

`Collections.unmodifiableList(list)` wraps the original list.

`List.copyOf(list)` creates an unmodifiable copy (or can reuse an already suitable immutable collection).

Think:

```text
unmodifiable wrapper -> changes in source can be visible
copy                -> separate immutable result semantics
```

---

# 4. Java 11 — a major practical LTS release

Java 11 is important because it is an LTS release and added several APIs developers use directly.

## 4.1 Local-variable syntax for lambda parameters

You could already write:

```java
(x, y) -> x + y
```

Java 11 allows `var` in lambda parameters:

```java
(var x, var y) -> x + y
```

Why is that useful? Mainly when you need annotations on lambda parameters or want consistent declaration style.

You cannot mix:

```java
(var x, y) -> ... // invalid
```

Use `var` for all parameters if using this form.

---

## 4.2 Standard HTTP Client

Java 11 standardized the modern `java.net.http.HttpClient` API.

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://example.com"))
        .GET()
        .build();

HttpResponse<String> response =
        client.send(request, HttpResponse.BodyHandlers.ofString());
```

It supports HTTP/1.1 and HTTP/2 configuration, synchronous `send`, and asynchronous `sendAsync`.

### Interview comparison

Older code often uses:

- `HttpURLConnection`
- third-party clients

Java 11 provides a first-class modern HTTP client in the JDK.

---

## 4.3 Useful String methods

Java 11 added:

```java
"   ".isBlank();
"  hello  ".strip();
"  hello  ".stripLeading();
"  hello  ".stripTrailing();
"a\nb\nc".lines();
"ha".repeat(3);
```

### `isBlank()` vs `isEmpty()`

```text
isEmpty() -> length == 0
isBlank() -> empty or only Unicode whitespace
```

### `strip()` vs `trim()`

For interview purposes:

- `trim()` is the older API with narrower character semantics.
- `strip()` uses Unicode-aware whitespace handling.

---

## 4.4 Files convenience methods

Java 11 added concise methods such as:

```java
String content = Files.readString(path);
Files.writeString(path, content);
```

This removes boilerplate for common whole-file text operations.

---

## 4.5 Important interview point: Java 8 vs 11

If asked “what changed from Java 8 to Java 11?” a compact answer can mention:

```text
Modules (9)
var (10)
private interface methods + useful collection/stream APIs (9)
HTTP Client (11)
String/Files convenience APIs (11)
var in lambda parameters (11)
```

Then add that Java 11 is an LTS release.

---

# 5. Java 12 — switch expressions begin their journey

## 5.1 Switch expressions (preview in 12)

The fundamental idea is:

> A `switch` can produce a value.

Traditional statement:

```java
int result;
switch (day) {
    case MONDAY:
        result = 1;
        break;
    default:
        result = 0;
}
```

Expression style:

```java
int result = switch (day) {
    case MONDAY -> 1;
    default -> 0;
};
```

### Statement vs expression

A **statement** performs an action.

An **expression** produces a value.

Mental model:

```text
switch statement -> “do this”
switch expression -> “calculate this value”
```

This is why `switch` became much more useful for assignments and return values.

---

## 5.2 The fall-through problem

Old switch:

```java
switch (day) {
    case SATURDAY:
    case SUNDAY:
        time = "6 AM";
        break;
    default:
        time = "7 AM";
}
```

Without `break`, execution can fall into the next case.

Arrow labels solve this by default:

```java
switch (day) {
    case SATURDAY, SUNDAY -> time = "6 AM";
    default -> time = "7 AM";
}
```

The video transcript uses exactly this problem—traditional `break`/fall-through versus arrow syntax—as the intuition for switch expressions.

---

## 5.3 `yield`

Sometimes an arrow arm needs multiple statements:

```java
int result = switch (value) {
    case 1 -> 10;
    case 2 -> {
        int x = calculate();
        yield x * 2;
    }
    default -> 0;
};
```

`yield` means:

> “Produce this value from this switch expression arm.”

### `break` vs `yield`

```text
break -> leave old-style switch flow
yield -> provide a value from a switch expression
```

---

# 6. Java 13 — text blocks begin + switch refinement

## 6.1 Text blocks (preview in 13)

### Problem

Large JSON/XML/HTML/multiline SQL strings are painful with escaped quotes and `+` concatenation.

Before:

```java
String json = "{\n" +
        "  \"name\": \"Rishabh\"\n" +
        "}";
```

Text block:

```java
String json = """
        {
          "name": "Rishabh"
        }
        """;
```

### Mental model

Triple quotes do not create a new data type. A text block is still a `String`.

It mainly makes **multiline String literals readable**.

---

# 7. Java 14 — important language features become practical

Java 14 is a key release because switch expressions became permanent.

## 7.1 Switch expressions become permanent

```java
String result = switch (status) {
    case 200 -> "OK";
    case 404 -> "NOT_FOUND";
    default -> "OTHER";
};
```

### Exhaustiveness

A switch expression must cover all possible inputs, typically using a `default` unless the compiler can prove all possibilities are covered (for example with certain enum/sealed hierarchies in later Java releases).

---

## 7.2 Pattern matching for `instanceof` (preview in 14)

Before:

```java
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}
```

Pattern matching:

```java
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

### Mental model

Old code does two jobs:

```text
1. check the type
2. cast to that type
```

Pattern matching combines the two:

```text
check + extract variable
```

---

## 7.3 Helpful NullPointerException messages

Modern Java can identify the expression component that was `null`, making NPE debugging much faster.

Example conceptually:

```java
order.getCustomer().getAddress().getCity()
```

A useful NPE message can identify which dereference failed instead of just saying `NullPointerException`.

### Interview value

Not a language syntax feature, but an important JVM/runtime usability improvement introduced in Java 14.

---

# 8. Java 15 — text blocks and sealed classes mature

## 8.1 Text blocks become permanent

Now you can confidently use:

```java
String sql = """
        SELECT id, name
        FROM users
        WHERE active = true
        """;
```

### Typical uses

- JSON
- SQL
- HTML/XML
- long documentation text
- multiline test data

### Interview trap

Text block is **still a String**.

---

## 8.2 Sealed classes (preview in 15)

### Problem

Java traditionally gives you:

```text
abstract -> anyone can extend
final    -> nobody can extend
```

But sometimes the real requirement is:

> “Only these specific types are allowed to extend this abstraction.”

That is exactly what `sealed` provides.

```java
sealed interface Payment permits CardPayment, CashPayment {
}

final class CardPayment implements Payment {
}

final class CashPayment implements Payment {
}
```

### Mental model

```text
abstract -> open hierarchy
final    -> closed hierarchy
sealed   -> controlled hierarchy
```

### Rules for direct subclasses

A direct subclass of a sealed class/interface must declare one of:

```text
final
sealed
non-sealed
```

Example:

```java
sealed class Vehicle permits Car, Bike {
}

final class Car extends Vehicle {
}

non-sealed class Bike extends Vehicle {
}
```

`non-sealed` means:

> “This branch is now open for further inheritance.”

### Why it matters

Sealed hierarchies make domain models explicit and allow the compiler to reason about all permitted subtypes.

That becomes especially useful with pattern matching for `switch` in Java 21.

---

# 9. Java 16 — records + pattern matching become permanent

## 9.1 Record classes

### Problem

Data-carrier classes often require huge amounts of boilerplate:

```java
private final int id;
private final String name;

constructor
getters

equals
hashCode
toString
```

A record expresses the same intent directly:

```java
public record User(int id, String name) {
}
```

### Mental model

A **record is a concise declaration of a data carrier**.

You are telling the compiler:

> “The identity/data of this object is represented by these components.”

The compiler provides the standard machinery such as:

- private final component fields;
- canonical constructor;
- accessors named exactly after components;
- `equals`;
- `hashCode`;
- `toString`.

For:

```java
record User(int id, String name) {}
```

you use:

```java
User u = new User(1, "Rishabh");
u.id();
u.name();
```

not `getId()` / `getName()` unless you manually add such methods.

### Record immutability — important nuance

Records are designed as immutable **data carriers**, and their components are final.

But this is generally **shallow immutability**.

Example:

```java
record User(List<String> roles) {}
```

The reference to the list is final, but the list itself can still be mutable.

So:

```text
record -> immutable references / value-style API
not automatically deep immutable object graphs
```

### Can a record extend a class?

No. A record implicitly extends `java.lang.Record`, so it cannot extend another class.

A record **can implement interfaces**.

### Can a record have methods?

Yes.

```java
record User(String name) {
    boolean isLongName() {
        return name.length() > 10;
    }
}
```

### Compact constructor

Useful for validation/normalization:

```java
record User(String name, int age) {
    User {
        if (age < 0) {
            throw new IllegalArgumentException("age < 0");
        }
    }
}
```

You do not repeat the component parameters in the compact form.

### Record vs Lombok DTO / POJO

Record is useful when the type is naturally a **value/data carrier** and you want concise, explicit semantics.

It is not a universal replacement for every entity class, especially mutable ORM entities or objects with complex lifecycle/state behavior.

---

## 9.2 Pattern matching for `instanceof` becomes permanent

Final form:

```java
if (obj instanceof String s && !s.isBlank()) {
    System.out.println(s.length());
}
```

This combines:

```text
instanceof check
+ cast
+ local variable
```

### Variable scope

The pattern variable is available only where the compiler can prove the match succeeded.

That is why this works:

```java
if (obj instanceof String s && s.length() > 3) {
    ...
}
```

but you cannot assume `s` exists outside an unrelated branch where the match may have failed.

---

## 9.3 `Stream.toList()`

Java 16 introduced:

```java
List<String> result = stream.toList();
```

For interview comparisons:

```text
stream.toList()
    -> returns an unmodifiable List

Collectors.toList()
    -> does not promise a specific mutability/type contract
```

Do not answer “Collectors.toList always returns ArrayList” as a contractual guarantee.

---

# 10. Java 17 — LTS + sealed classes become permanent

Java 17 is a major interview target because it is an LTS release and a common production baseline.

## 10.1 Sealed classes become permanent

Final syntax:

```java
sealed interface Shape permits Circle, Rectangle {
}

final class Circle implements Shape {
}

final class Rectangle implements Shape {
}
```

### The design intuition

Use sealed types when the domain model has a **known, controlled set of variants**.

This is especially useful when combined with pattern matching, because the compiler can reason about all permitted cases.

---

## 10.2 Pattern matching for switch — preview in 17

The big idea:

Old:

```java
if (value instanceof String s) {
    ...
} else if (value instanceof Integer i) {
    ...
}
```

Modern switch concept:

```java
String result = switch (value) {
    case String s -> "string: " + s;
    case Integer i -> "integer: " + i;
    default -> "other";
};
```

The feature continued through multiple previews and became permanent in Java 21.

---

## 10.3 Strong encapsulation of JDK internals

Java 17 strongly encapsulates internal JDK APIs.

Interview mental model:

```text
Do not depend on sun.* / internal JDK implementation details.
```

Prefer supported public Java APIs.

This matters for upgrades because code that relied on JDK internals can break or need migration.

---

## 10.4 Other Java 17 topics worth knowing

- Enhanced pseudo-random number generators (JEP 356)
- Always-strict floating-point semantics
- Security Manager deprecated for removal
- Pattern matching for `switch` remained preview
- Foreign Function & Memory API remained incubating

For a normal Java backend interview, these are lower priority than records/sealed classes and the language features.

---

# 11. Java 18 — platform consistency improvements

## 11.1 UTF-8 by default

Java 18 made UTF-8 the default charset for Java SE APIs that depend on the default charset, improving consistency across platforms.

### Interview intuition

Before, default-charset behavior could vary with environment.

Now the default is more predictable.

### Why it matters

File/network/text processing bugs caused by different machine encodings become less likely when code relies on the standard default.

Still, when the encoding is part of a protocol/file contract, **specify the charset explicitly** rather than depending on defaults.

---

## 11.2 Simple Web Server

Java 18 added a minimal `jwebserver` tool for serving static files.

Use case:

```text
prototype / testing / quick local serving
```

It is not a Spring/Tomcat replacement for production web applications.

---

## 11.3 JavaDoc snippets

`@snippet` improves how code examples can be included in JavaDoc.

Interview relevance: low; know it only if discussing documentation tooling.

---

# 12. Java 19 — the beginning of virtual-thread era

## 12.1 Virtual threads (preview in 19)

### The problem

Traditional platform threads are backed by operating-system threads and are comparatively expensive.

In highly concurrent server workloads, you may want thousands/millions of concurrent tasks, especially when many tasks spend time waiting on I/O.

Virtual threads are lightweight Java-managed threads designed to make very high concurrency easier to express.

### Mental model

```text
Platform thread
    ≈ expensive execution resource

Virtual thread
    ≈ lightweight task/thread representation managed by JVM
```

The key benefit is **scale of concurrency**, not “every calculation becomes faster.”

### Example

Java 21 final API:

```java
Thread.startVirtualThread(() -> {
    handleRequest();
});
```

or:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> serviceA());
    executor.submit(() -> serviceB());
}
```

### Best fit

Especially useful for workloads with lots of waiting:

```text
request
  -> DB call (wait)
  -> HTTP call (wait)
  -> DB call (wait)
  -> response
```

### Important interview correction

Do **not** say:

> “Virtual threads make CPU-bound code faster.”

A better answer:

> “Virtual threads mainly improve scalability for highly concurrent tasks, especially I/O-heavy workloads, by making blocking-style code much cheaper to run concurrently.”

### Do not pool virtual threads like platform threads

The common programming model is **one virtual thread per task**, rather than maintaining a tiny reusable pool to avoid thread-creation cost.

---

## 12.2 Record patterns (preview in 19)

Pattern matching can go inside records.

```java
record Point(int x, int y) {}

if (obj instanceof Point(int x, int y)) {
    System.out.println(x + y);
}
```

### Mental model

Normal `instanceof` pattern:

```text
check type + bind object
```

Record pattern:

```text
check record + deconstruct its components
```

This became permanent in Java 21.

---

## 12.3 Structured Concurrency (incubator in 19)

Idea:

Treat related concurrent child tasks as **one unit of work**.

Imagine a request needs:

```text
User service
Product service
Recommendation service
```

Instead of managing unrelated futures/threads everywhere, structured concurrency aims to make their:

- lifetime,
- cancellation,
- error handling,
- observability

follow a parent-child structure.

It remained non-final through Java 21, so know the concept but do not call it a permanent Java 21 API.

---

## 12.4 Other Java 19 incubator/preview areas

- Foreign Function & Memory API
- Vector API
- more pattern-switch previews

For a regular backend interview, virtual threads are by far the most important Java 19 topic.

---

# 13. Java 20 — refinement release

Java 20 mainly continued preview/incubator evolution rather than introducing a large number of permanent mainstream language features.

Important concepts continued/refined:

- Pattern Matching for `switch` — fourth preview
- Record Patterns — second preview
- Virtual Threads — second preview
- Structured Concurrency — second incubator
- Scoped Values — incubator
- Foreign Function & Memory API — preview/incubator evolution

### Interview strategy

Do not try to memorize every preview iteration number.

Remember the evolution:

```text
Pattern switch     -> preview in 17 -> final in 21
Record patterns    -> preview in 19 -> final in 21
Virtual threads    -> preview in 19 -> final in 21
```

That is much more useful.

---

# 14. Java 21 — the major modern Java target

Java 21 is an LTS release and contains some of the most interview-relevant modern Java features.

The big four to remember:

```text
1. Virtual Threads            -> FINAL
2. Pattern Matching for switch -> FINAL
3. Record Patterns              -> FINAL
4. Sequenced Collections        -> FINAL
```

Then know the preview features separately:

```text
String Templates               -> PREVIEW
Unnamed Patterns/Variables     -> PREVIEW
Unnamed Classes/Main Methods   -> PREVIEW
Scoped Values                  -> PREVIEW
Structured Concurrency         -> PREVIEW
```

---

## 14.1 Virtual Threads — final in Java 21

### Basic usage

```java
Thread vt = Thread.startVirtualThread(() -> {
    System.out.println("Hello from virtual thread");
});
```

Executor style:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> future = executor.submit(() -> callRemoteService());
    System.out.println(future.get());
}
```

### The architecture intuition

Traditional model:

```text
many requests
   ↓
many platform threads
   ↓
OS resources become expensive
```

Virtual-thread model:

```text
many requests
   ↓
many lightweight virtual threads
   ↓
JVM schedules them onto a much smaller number of carrier/platform threads
```

### Blocking is no longer automatically a design smell

This is a major conceptual shift.

With virtual threads, ordinary blocking APIs can become easier to scale because a blocked virtual thread does not necessarily mean an OS thread must remain dedicated to that task for the entire wait.

But this does **not** mean:

- databases become faster;
- network calls become faster;
- CPU work becomes cheaper;
- external systems stop being bottlenecks.

It means the Java application can represent **far more concurrent waiting tasks** efficiently.

### Interview question: Virtual thread vs asynchronous programming

A strong answer:

> “Virtual threads let developers keep straightforward sequential/blocking-style code while supporting much higher concurrency. Async APIs can still be appropriate, but virtual threads reduce the need to transform application logic into deeply nested callback/future pipelines just to scale I/O concurrency.”

---

## 14.2 Pattern matching for switch — final in Java 21

Example:

```java
static String describe(Object value) {
    return switch (value) {
        case Integer i -> "int: " + i;
        case Long l -> "long: " + l;
        case String s -> "string: " + s;
        default -> "other";
    };
}
```

### What changed conceptually?

Traditional switch says:

> “Does this value equal this constant?”

Pattern switch can say:

> “Does this value match this type/pattern?”

That makes `switch` much better for **data-oriented branching**.

### Null handling

Pattern switch can explicitly match `null`:

```java
return switch (value) {
    case null -> "null";
    case String s -> "string";
    default -> "other";
};
```

### Guarded patterns (`when`)

Java 21 supports a `when` clause for additional conditions:

```java
return switch (value) {
    case String s when s.isBlank() -> "blank";
    case String s -> "text";
    default -> "other";
};
```

### Dominance matters

More specific cases must appear before broader cases.

Bad idea conceptually:

```java
case Object o -> ...
case String s -> ...  // unreachable/dominated
```

because every String is already an Object.

### Sealed classes + switch

This is where sealed classes become especially powerful.

```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double r) implements Shape {}
record Rectangle(double w, double h) implements Shape {}

static double area(Shape shape) {
    return switch (shape) {
        case Circle c -> Math.PI * c.r() * c.r();
        case Rectangle r -> r.w() * r.h();
    };
}
```

No `default` is required because the compiler can know the complete permitted hierarchy.

### Mental model

```text
sealed hierarchy
      +
pattern switch
      ↓
compiler knows all valid cases
      ↓
safer exhaustive branching
```

---

## 14.3 Record patterns — final in Java 21

Example:

```java
record User(String name, Address address) {}
record Address(String city, String country) {}
```

Nested pattern:

```java
if (user instanceof User(String name,
        Address(String city, String country))) {
    System.out.println(name + " lives in " + city);
}
```

### Why this exists

Without record patterns, you would manually unpack:

```java
if (user instanceof User u) {
    String name = u.name();
    Address a = u.address();
    String city = a.city();
}
```

Record patterns allow **destructuring-style extraction while retaining static typing**.

### Mental model

```text
Record
  ↓
pattern
  ↓
check + deconstruct + bind components
```

---

## 14.4 Sequenced Collections — new in Java 21

Java collection APIs historically had inconsistent ways to ask for “first”, “last”, or “reverse order” depending on the concrete type.

Java 21 adds:

```text
SequencedCollection
SequencedSet
SequencedMap
```

### Important methods

For sequenced collections:

```java
getFirst()
getLast()
addFirst(...)
addLast(...)
removeFirst()
removeLast()
reversed()
```

Example:

```java
SequencedCollection<String> names = new ArrayList<>();

names.addLast("A");
names.addLast("B");

System.out.println(names.getFirst());
System.out.println(names.getLast());
System.out.println(names.reversed());
```

### Why this matters

The API now gives a common abstraction for collections where **encounter order is meaningful**.

### Mental model

Before:

```text
first/last/reverse APIs were scattered across implementations
```

Java 21:

```text
common sequenced abstraction + common operations
```

---

# 15. Java 21 preview features — know them, but label them correctly

This section is a major interview trap.

## 15.1 String Templates — PREVIEW in Java 21

The transcript discusses String Templates as a new Java 21 feature. The important correction is:

> **String Templates were a preview feature in Java 21, not a permanent Java 21 feature.**

Example preview syntax:

```java
String name = "Rishabh";
String text = STR."Hello \{name}";
```

### What problem do they try to solve?

Today you commonly write:

```java
"Hello " + name + ", age=" + age
```

or:

```java
String.format("Hello %s, age=%d", name, age)
```

String templates provide a structured mechanism for combining literal text and embedded expressions through a **template processor**.

### Very important security nuance

Do **not** answer:

> “String templates automatically prevent SQL injection.”

That is too broad.

A template processor can be designed to produce a safe domain-specific result, but SQL safety still depends on using proper SQL parameterization/prepared statements and correct APIs.

Think:

```text
String template = flexible interpolation + processor model
PreparedStatement = SQL parameterization
```

They solve different problems.

### Interview status

```text
Java 21 -> Preview
Not a final Java 21 language feature
```

---

## 15.2 Unnamed patterns and variables — PREVIEW in 21

Use `_` when you deliberately do not need a value.

Conceptually:

```java
if (obj instanceof Point(int x, _)) {
    System.out.println(x);
}
```

The idea is:

> “I matched/bound something, but I intentionally do not care about its name/value.”

This reduces meaningless variable names and signals intent clearly.

---

## 15.3 Unnamed classes and instance main methods — PREVIEW in 21

The goal is to make tiny Java programs easier to start.

Traditional:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```

The preview feature aims to allow a much more direct source form for simple programs, making Java easier for beginners and small scripts while retaining a path to normal classes later.

### Interview status

Preview in Java 21.

Do not describe it as the standard Java 21 main-method style.

---

## 15.4 Scoped Values — PREVIEW in 21

Problem with `ThreadLocal`:

- state can be mutable;
- lifecycle management can be tricky;
- large numbers of threads make per-thread state costly to reason about.

Scoped values provide a mechanism for sharing **immutable contextual data** within a dynamic scope of execution.

Conceptual mental model:

```text
ThreadLocal:
“store state associated with this thread”

ScopedValue:
“make this immutable context available to code running in this scope”
```

This is particularly interesting with virtual threads.

Status in Java 21: **preview**.

---

## 15.5 Structured Concurrency — PREVIEW in 21

The concept is more important than the exact preview API.

Suppose one request launches:

```text
service A
service B
service C
```

Structured concurrency treats those tasks as a structured unit with clearer:

- task ownership;
- cancellation;
- failure propagation;
- lifecycle management.

Mental model:

```text
Unstructured concurrency:
parent request -> random independent tasks

Structured concurrency:
parent scope -> child tasks -> join/cancel/handle together
```

Java 21 status: **preview**, not permanent.

---

# 16. Important Java 19–21 concurrency concepts together

The easiest way to remember the modern concurrency evolution is:

```text
Virtual Threads
      ↓
cheap high-concurrency tasks

Scoped Values
      ↓
immutable context propagation

Structured Concurrency
      ↓
manage related concurrent tasks as one unit
```

They attack different problems; they are complementary, not competing features.

---

# 17. Other Java 21 changes worth knowing at interview level

## 17.1 Generational ZGC

ZGC added generational support in Java 21.

Mental model:

```text
Most objects die young.

Young-generation-focused collection
        ↓
reduce work on long-lived objects
        ↓
better GC efficiency for suitable workloads
```

This is mainly a JVM/performance topic rather than everyday Java syntax.

---

## 17.2 Key Encapsulation Mechanism (KEM) API

Java 21 added a standard KEM API as part of modern cryptographic support.

Interview priority: lower than virtual threads/patterns/records.

Know the name if your role includes security/crypto.

---

# 18. Preview vs Final vs Incubator — interview must-know

## Preview feature

A Java feature available for evaluation before becoming permanent.

Typical lifecycle:

```text
preview
  ↓
re-preview / refine
  ↓
final OR withdrawn
```

Examples:

```text
Switch expressions         -> preview 12/13 -> final 14
Text blocks                 -> preview 13/14 -> final 15
Records                     -> preview 14/15 -> final 16
Pattern instanceof          -> preview 14/15 -> final 16
Sealed classes              -> preview 15/16 -> final 17
Pattern switch              -> preview 17/18/19/20 -> final 21
Record patterns              -> preview 19/20 -> final 21
Virtual threads              -> preview 19/20 -> final 21
```

## Incubator

Typically used for APIs where the design or implementation is still evolving, often outside the final standardized Java SE API surface.

Examples around Java 19–21 include:

- Structured Concurrency (incubator, then preview)
- Scoped Values (incubator, then preview)
- Foreign Function & Memory API (incubator/preview evolution)
- Vector API (incubator)

### Interview sentence

> “Preview means the language/API is exposed for evaluation before standardization; incubator APIs are experimental APIs being developed outside the standard final API surface.”

---

# 19. Java 8 → 21 release map for fast revision

| Java | Most interview-relevant change | Status / mental model |
|---|---|---|
| 8 | Lambda, functional interfaces, Stream API, Optional, Date/Time, CompletableFuture | Foundation of modern Java |
| 9 | Modules, collection factories, private interface methods, better try-with-resources, Stream/Optional additions, JShell | Platform modularization |
| 10 | `var`, `copyOf` collection APIs | Type inference, not dynamic typing |
| 11 | HTTP Client, String/Files APIs, `var` in lambda parameters | Major LTS |
| 12 | Switch expressions | Preview begins |
| 13 | Text blocks | Preview + switch refinement |
| 14 | Switch expressions final, helpful NPE, records/instanceof preview | Cleaner branching + data modeling |
| 15 | Text blocks final, sealed/records/instanceof previews, ZGC production | Data + hierarchy design |
| 16 | Records final, instanceof pattern final, `Stream.toList()` | Boilerplate reduction |
| 17 | Sealed classes final, switch pattern preview, strong encapsulation | Major LTS |
| 18 | UTF-8 default, simple web server | Platform consistency |
| 19 | Virtual threads preview, record patterns preview | Modern concurrency begins |
| 20 | More preview refinement | Maturity step |
| 21 | Virtual threads final, pattern switch final, record patterns final, sequenced collections | Major LTS + modern concurrency |

---

# 20. The feature journey you should actually remember

Instead of memorizing 14 separate releases, memorize this chain:

```text
Java 8
  ↓
Lambda + Stream + Optional
  ↓
Java 9
  ↓
Modules + better APIs
  ↓
Java 10
  ↓
var = less type repetition
  ↓
Java 11
  ↓
modern JDK APIs + HTTP Client
  ↓
Java 12–14
  ↓
switch becomes an expression
  ↓
Java 15–16
  ↓
text blocks + records + pattern instanceof
  ↓
Java 17
  ↓
sealed classes
  ↓
Java 19–21
  ↓
virtual threads + record patterns + pattern switch
  ↓
Java 21
  ↓
sequenced collections + modern concurrency APIs
```

That sequence explains *why* Java evolved instead of forcing you to memorize isolated syntax.

---

# 21. High-value interview comparisons

## 21.1 `var` vs dynamic typing

```text
var:
- compile-time inference
- static type remains fixed
- local variables only

Dynamic typing:
- runtime determines/changes the variable's type semantics
```

**Answer:** `var` is not dynamic typing.

---

## 21.2 Record vs normal class

```text
Normal class:
- full control over fields/state/lifecycle
- can be mutable
- can extend another class
- boilerplate is your responsibility

Record:
- compact data-carrier declaration
- component fields are final
- generated equals/hashCode/toString/accessors/constructor semantics
- cannot extend another class
- can implement interfaces
```

Use a record when the type is naturally a value/data carrier.

---

## 21.3 Sealed vs final vs abstract

```text
abstract -> designed for inheritance, open to subclasses
final    -> inheritance completely blocked
sealed   -> only explicitly permitted direct subclasses
```

---

## 21.4 Switch statement vs switch expression

```text
statement  -> performs control flow
expression -> produces a value
```

Modern example:

```java
String result = switch (code) {
    case 200 -> "OK";
    default -> "OTHER";
};
```

---

## 21.5 `break` vs `yield`

```text
break -> exits a statement flow

 yield -> supplies a value from a switch expression arm
```

---

## 21.6 Arrow switch vs colon switch

Arrow:

```java
case A -> doSomething();
```

has no accidental fall-through.

Colon:

```java
case A:
    doSomething();
    break;
```

retains traditional statement/block semantics.

---

## 21.7 `instanceof` pattern vs traditional casting

Old:

```java
if (obj instanceof String) {
    String s = (String) obj;
}
```

New:

```java
if (obj instanceof String s) {
    ...
}
```

Same type-safety idea; the new form removes repetitive casting and binds the variable in one step.

---

## 21.8 Record patterns vs normal record access

Normal:

```java
if (obj instanceof User u) {
    System.out.println(u.name());
}
```

Record pattern:

```java
if (obj instanceof User(String name, _)) {
    System.out.println(name);
}
```

Think:

```text
instanceof pattern -> extract object
record pattern     -> extract components
```

---

## 21.9 Platform thread vs virtual thread

```text
Platform thread:
- backed closely by OS thread model
- relatively expensive
- limited concurrency at huge scale

Virtual thread:
- lightweight JVM-managed thread
- cheap to create/use in huge numbers
- designed especially for high-concurrency workloads
```

Again: **virtual threads are about concurrency scale, not automatic CPU speedup.**

---

## 21.10 `List.of` vs `Arrays.asList` vs `new ArrayList`

```text
List.of(...)          -> unmodifiable
Arrays.asList(...)    -> fixed-size; set allowed; backed by array
new ArrayList<>(...)  -> mutable resizable list
```

---

## 21.11 `stream().toList()` vs `Collectors.toList()`

```text
stream.toList()
    -> unmodifiable result

Collectors.toList()
    -> mutability/type are not promised by the API contract
```

Do not rely on the concrete implementation of `Collectors.toList()`.

---

## 21.12 `orElse` vs `orElseGet`

```text
orElse(value)
    -> fallback expression may be evaluated eagerly

orElseGet(supplier)
    -> fallback produced lazily when needed
```

Use `orElseGet` when fallback computation is expensive or has side effects.

---

## 21.13 `map` vs `flatMap`

This is more important for Java 8 interviews than many post-Java-8 features.

```text
map:
T -> R

flatMap:
T -> Stream<R>
```

`flatMap` is used when one input can produce multiple outputs and you want one flattened stream.

Example:

```java
List<List<Integer>> nested = ...;

List<Integer> flat = nested.stream()
        .flatMap(List::stream)
        .toList();
```

---

# 22. Modern Java coding style — what an interviewer expects you to understand

## 22.1 Prefer clarity over “maximum modern syntax”

Modern Java features are not a competition.

Do not write:

```java
var x = somethingVeryComplex();
```

when the explicit type makes the code much easier to understand.

Do not use records just because they are short if the domain object has behavior/state semantics that deserve a normal class.

Do not use parallel streams without understanding the workload.

Do not use virtual threads and assume every external bottleneck disappears.

### The interview principle

> **Modern Java reduces accidental complexity; it does not remove the need for good design.**

---

# 23. Practical backend examples of where these features fit

## DTOs

Prefer a record when the DTO is a straightforward immutable data carrier:

```java
public record UserResponse(Long id, String name, String email) {}
```

Controller:

```java
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return service.getUser(id);
}
```

---

## Parsing heterogeneous domain objects

Sealed interface + records + pattern switch:

```java
sealed interface Payment permits CardPayment, UpiPayment {}

record CardPayment(String cardToken) implements Payment {}
record UpiPayment(String upiId) implements Payment {}

String route(Payment payment) {
    return switch (payment) {
        case CardPayment c -> "CARD";
        case UpiPayment u -> "UPI";
    };
}
```

This is a very strong example for modern Java interviews because three features reinforce each other:

```text
record        -> data representation
sealed        -> controlled domain hierarchy
pattern switch -> exhaustive behavior
```

---

## High-concurrency I/O

Virtual threads are especially interesting for code like:

```text
HTTP request
   ↓
DB query
   ↓
remote REST call
   ↓
DB query
   ↓
response
```

When each task spends significant time waiting, virtual threads can allow many such tasks to exist concurrently without requiring one costly platform thread per task.

---

# 24. Common traps / wrong answers to avoid

## Trap 1

> “`var` makes Java dynamically typed.”

Wrong.

Correct:

> “`var` enables compile-time local variable type inference; the inferred type is static.”

---

## Trap 2

> “A record is deeply immutable.”

Too strong.

Correct idea:

> “Records provide a concise value-oriented data-carrier model with final components; immutability is generally shallow.”

---

## Trap 3

> “String Templates are a final Java 21 feature.”

Wrong.

Correct:

> “String Templates were previewed in Java 21.”

---

## Trap 4

> “String Templates prevent SQL injection automatically.”

Too broad.

Correct:

> “Template processors can support safe domain-specific processing, but SQL injection prevention still relies on correct parameterized SQL APIs such as prepared statements.”

---

## Trap 5

> “Virtual threads make Java CPU processing faster.”

Wrong framing.

Correct:

> “Virtual threads primarily improve scalability for high-concurrency workloads, especially where tasks spend time waiting.”

---

## Trap 6

> “`Collectors.toList()` always returns an `ArrayList`.”

Not guaranteed by the API contract.

---

## Trap 7

> “`switch` expression and `switch` statement are the same.”

No.

An expression produces a value; a statement performs control flow without being used as a value-producing expression.

---

## Trap 8

> “`yield` is just another name for `break`.”

No.

`yield` provides a value from a switch expression arm.

---

# 25. 5-minute interview revision sheet

Read this section immediately before the interview.

## Java 8

```text
Lambda       -> behavior as a value
Functional   -> one abstract method
Method ref   -> concise lambda using existing method
Stream       -> data-processing pipeline
Optional     -> explicit possible absence
java.time    -> modern date/time API
CompletableFuture -> composable async stages
Default method -> evolve interfaces safely
```

## Java 9

```text
Modules
List.of / Set.of / Map.of
Private interface methods
try(resource) using effectively-final variables
JShell
Stream takeWhile/dropWhile
Optional ifPresentOrElse/or
```

## Java 10

```text
var = local type inference, NOT dynamic typing
List.copyOf / Set.copyOf / Map.copyOf
```

## Java 11

```text
HTTP Client
String.isBlank / lines / strip / repeat
Files.readString / writeString
var in lambda parameters
```

## Java 12–14

```text
switch expression
-> returns a value
-> no accidental fall-through
yield -> value from block arm
Helpful NPE
pattern instanceof preview
records preview
```

## Java 15–17

```text
Text blocks final
Records final
instanceof pattern final
Sealed classes final
Stream.toList()
Strong JDK encapsulation
```

## Java 18

```text
UTF-8 default
Simple Web Server
```

## Java 19–20

```text
Virtual threads preview -> final in 21
Record patterns preview -> final in 21
Pattern switch previews -> final in 21
Structured concurrency evolves
Scoped values evolve
```

## Java 21

```text
Virtual threads           -> FINAL
Record patterns            -> FINAL
Pattern switch             -> FINAL
Sequenced collections      -> FINAL

String templates           -> PREVIEW
Unnamed patterns/vars      -> PREVIEW
Unnamed classes/main       -> PREVIEW
Scoped values              -> PREVIEW
Structured concurrency    -> PREVIEW
```

---

# 26. Interview-ready answers you can say naturally

## “What are the most important Java 8 features?”

> “The biggest shift in Java 8 was functional-style programming. Lambda expressions and functional interfaces let us treat behavior as values, the Stream API gave us declarative collection processing, `Optional` made possible absence explicit, Java 8 introduced the modern `java.time` API, and `CompletableFuture` enabled composable asynchronous operations. Default interface methods also allowed library interfaces to evolve without breaking existing implementations.”

## “What did Java 9 bring?”

> “Java 9’s biggest architectural change was the Java Platform Module System. It introduced explicit module boundaries and dependencies. It also added collection factory methods such as `List.of`, private interface methods, more concise try-with-resources, JShell, and several Stream and Optional API improvements.”

## “What is `var`?”

> “`var` is local-variable type inference. The compiler infers the variable’s static type from the initializer at compile time. It is not dynamic typing and it doesn’t remove Java’s static type checking.”

## “Why were switch expressions introduced?”

> “Traditional switch is statement-oriented and commonly needs `break`, which creates fall-through risks. Switch expressions let switch directly produce a value and arrow cases avoid accidental fall-through. `yield` is used when a block arm needs to produce a value.”

## “Why were records introduced?”

> “Records target data-carrier classes. They reduce boilerplate by deriving the canonical constructor, accessors, equals, hashCode, and toString from the declared components. They are ideal when the identity of the object is primarily represented by its data.”

## “What are sealed classes?”

> “Sealed classes restrict which classes or interfaces are allowed to directly extend or implement a type. They sit between abstract and final in terms of openness: the hierarchy is controlled rather than completely open or completely closed.”

## “What is pattern matching?”

> “Pattern matching combines type checking with data extraction. With `instanceof`, instead of checking a type and then explicitly casting, I can match the type and bind the variable in one expression. Java 21 extends the same idea to switch and record deconstruction.”

## “What is the biggest Java 21 feature?”

> “For backend systems, virtual threads are one of the most important Java 21 features because they make very high-concurrency, especially I/O-heavy workloads, much easier to model using straightforward thread-per-task code. Java 21 also finalizes pattern matching for switch and record patterns and introduces sequenced collections.”

## “Are String Templates final in Java 21?”

> “No. They were a preview feature in Java 21. I would not describe them as a permanent Java 21 language feature.”

---

# 27. What to prioritize for a Java/Spring Backend interview

If you only have limited revision time, use this order:

### Tier 1 — must know deeply

```text
Java 8:
Lambda
Functional interfaces
Stream API
Optional
CompletableFuture
Date/Time

Java 9–11:
Modules (conceptually)
var
List.of / Set.of / Map.of
HTTP Client
String/Files improvements

Java 14–17:
switch expressions
instanceof patterns
records
sealed classes
text blocks
Stream.toList

Java 19–21:
virtual threads
pattern matching switch
record patterns
sequenced collections
preview vs final
```

### Tier 2 — know the idea and one example

```text
JShell
Process API
takeWhile/dropWhile
UTF-8 by default
Simple Web Server
Structured concurrency
Scoped values
Generational ZGC
```

### Tier 3 — recognize the name

```text
Vector API
Foreign Function & Memory API
KEM API
Unnamed classes/main preview
Unnamed patterns/variables preview
```

---

# 28. One-page conceptual map

```text
                         MODERN JAVA
                              |
        +---------------------+----------------------+
        |                                            |
     EXPRESSIVE CODE                            CONCURRENCY
        |                                            |
   Java 8 Lambda                               CompletableFuture
        |                                            |
   Functional interfaces                       Virtual Threads
        |                                            |
      Streams                                  Structured Concurrency
        |                                            |
      Optional                                  Scoped Values
        |
   -----------------------------
        |
   LESS BOILERPLATE
        |
      var (10)
        |
    Text Blocks (15)
        |
     Records (16)
        |
 Pattern Matching (16/21)
        |
   Record Patterns (21)

                         DOMAIN SAFETY
                              |
                        Sealed Classes
                              |
                    Pattern Switch (21)
                              |
                    Exhaustive modeling

                         COLLECTIONS
                              |
                     List.of / copyOf
                              |
                       Stream.toList
                              |
                  Sequenced Collections (21)
```

---

# 29. Final “why” summary — the part you should understand, not memorize

Java’s post-8 evolution can be explained as a sequence of pain points:

```text
Pain: too much anonymous-class / loop boilerplate
Solution: lambdas + streams (8)

Pain: interfaces hard to evolve
Solution: default/private interface methods (8/9)

Pain: huge classpath / weak boundaries
Solution: modules (9)

Pain: repeated obvious local types
Solution: var (10)

Pain: verbose networking/string/file code
Solution: better standard APIs, HTTP Client, String/Files methods (11)

Pain: switch is statement-oriented and fall-through-prone
Solution: switch expressions + arrow cases (12–14)

Pain: multiline strings are ugly
Solution: text blocks (13–15)

Pain: data carriers generate boilerplate
Solution: records (14–16)

Pain: check + cast is repetitive
Solution: pattern matching for instanceof (14–16)

Pain: inheritance hierarchies are too open
Solution: sealed classes (15–17)

Pain: huge numbers of concurrent waiting tasks need huge numbers of platform threads
Solution: virtual threads (19–21)

Pain: nested data extraction is repetitive
Solution: record patterns (19–21)

Pain: type-oriented branching is awkward
Solution: pattern matching for switch (17–21)

Pain: ordered collection APIs are inconsistent
Solution: sequenced collections (21)
```

### The single sentence to remember

> **Modern Java is mostly about reducing boilerplate while preserving Java’s static type safety, making data models more explicit, and making high-concurrency code easier to write and reason about.**

---

# 30. Official references

These notes were cross-checked against Oracle’s Java language-change documentation and JDK migration documentation.

- Oracle — Java Language Changes Summary (Java SE 9–21):
  https://docs.oracle.com/en/java/javase/21/language/java-language-changes-summary.html

- Oracle — Java Language Changes by Release:
  https://docs.oracle.com/en/java/javase/21/language/java-language-changes-release.html

- Oracle — Significant Changes in JDK Releases:
  https://docs.oracle.com/en/java/javase/21/migrate/significant-changes-jdk-release.html

- Oracle — Java 8 Language Enhancements:
  https://docs.oracle.com/javase/8/docs/technotes/guides/language/enhancements.html

- Oracle — Java 8 Lambda/Stream APIs:
  https://docs.oracle.com/javase/8/docs/technotes/guides/language/lambda_api_jdk8.html

- Oracle — Java 9 What’s New / Module System:
  https://docs.oracle.com/javase/9/whatsnew/

- Oracle — Sequenced Collections (Java 21):
  https://docs.oracle.com/en/java/javase/21/core/creating-sequenced-collections-sets-and-maps.html

- Oracle — Pattern Matching for switch (Java 21):
  https://docs.oracle.com/en/java/javase/21/language/pattern-matching-switch.html

- Oracle — `HttpClient` API (Java 21):
  https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html

---

# Appendix A — Video transcript coverage map

The supplied transcript is especially useful for the following concepts:

| Video section | Topic | How these notes use it |
|---|---|---|
| ~14:01 onward | Switch statement → arrow cases → switch expression | Expanded with `yield`, expression vs statement, exhaustiveness, pattern switch |
| ~29:08 onward | `var` / local variable type inference | Expanded with static typing rules and interview traps |
| ~41:04 onward | Text blocks | Expanded with version/status and practical JSON/SQL examples |
| ~45:35 onward | String templates | Corrected/expanded: Java 21 preview, template processors, SQL-injection nuance |
| ~56:52 onward | Sealed classes | Expanded with `final` / `sealed` / `non-sealed` and pattern-switch connection |
| ~66:46 onward | Record classes | Expanded with record semantics, shallow immutability, constructors, accessors, DTO usage |

The transcript’s core message is preserved: modern Java is not replacing Java’s fundamentals; it is making common code more concise, expressive, and safer while adding platform capabilities for modern workloads.
