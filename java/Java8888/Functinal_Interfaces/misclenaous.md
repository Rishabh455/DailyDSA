A **Functional Interface** in Java has **exactly one abstract method** because its main purpose is to represent **one behavior (one action)** that can be implemented using a **lambda expression** or **method reference**.

### Example

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}
```

Using lambda:

```java
Calculator add = (a, b) -> a + b;

System.out.println(add.calculate(10, 20)); // 30
```

The compiler knows that the lambda `(a, b) -> a + b` belongs to the only abstract method `calculate()`.

---

### What if there were two abstract methods?

```java
interface Test {
    void method1();
    void method2();
}
```

Now consider:

```java
Test t = () -> System.out.println("Hello");
```

The compiler gets confused:

* Is this lambda for `method1()`?
* Or for `method2()`?

There is no way to determine which method the lambda should implement.

That's why a functional interface must have **only one abstract method**.

---

### Can it have other methods?

Yes.

A functional interface can contain:

#### 1. Default methods

```java
@FunctionalInterface
interface MyInterface {
    void show();

    default void print() {
        System.out.println("Default Method");
    }
}
```

#### 2. Static methods

```java
@FunctionalInterface
interface MyInterface {
    void show();

    static void test() {
        System.out.println("Static Method");
    }
}
```

#### 3. Methods from `Object`

```java
@FunctionalInterface
interface MyInterface {
    void show();

    String toString(); // Doesn't count
}
```

Methods inherited from `Object` (`toString()`, `equals()`, `hashCode()`) do **not** count toward the abstract method count.

---

### Real-world Examples

Java provides many built-in functional interfaces:

| Functional Interface | Abstract Method           |
| -------------------- | ------------------------- |
| `Runnable`           | `void run()`              |
| `Callable<T>`        | `T call()`                |
| `Comparator<T>`      | `int compare(T o1, T o2)` |
| `Predicate<T>`       | `boolean test(T t)`       |
| `Consumer<T>`        | `void accept(T t)`        |
| `Supplier<T>`        | `T get()`                 |
| `Function<T,R>`      | `R apply(T t)`            |

Example:

```java
Runnable r = () -> System.out.println("Running");
```

Here the lambda implements the single abstract method `run()`.

### Interview Answer (Short)

> A functional interface has only one abstract method so that a lambda expression can map unambiguously to a single behavior. If multiple abstract methods were allowed, the compiler would not know which method the lambda is intended to implement. Default, static, and Object class methods are allowed because they do not create ambiguity for lambda expressions.



Before Java 8, interfaces could contain only:

* `public static final` variables
* `public abstract` methods

Example:

```java
interface Animal {
    void sound();
}
```

The problem was that once an interface was published, **adding a new method would break all existing implementations**.

---

# 1. Default Methods (Java 8)

## Problem

Suppose thousands of classes implement an interface.

```java
interface Vehicle {
    void start();
}
```

Implementations:

```java
class Car implements Vehicle {
    public void start() {
        System.out.println("Car Started");
    }
}
```

Now you want to add a new method:

```java
interface Vehicle {
    void start();
    void stop();
}
```

Suddenly every implementation breaks because `stop()` is not implemented.

---

## Solution: Default Method

Provide a default implementation.

```java
interface Vehicle {

    void start();

    default void stop() {
        System.out.println("Vehicle Stopped");
    }
}
```

Implementation:

```java
class Car implements Vehicle {

    public void start() {
        System.out.println("Car Started");
    }
}
```

Usage:

```java
Car c = new Car();

c.start();
c.stop();
```

Output:

```text
Car Started
Vehicle Stopped
```

### Why introduced?

To support **interface evolution** without breaking existing code.

This was heavily used in the Java Collections Framework.

Example:

```java
Collection.removeIf()
Iterable.forEach()
List.sort()
```

These methods were added in Java 8 as default methods.

---

# 2. Static Methods (Java 8)

## Problem

Sometimes utility methods logically belong to an interface.

Before Java 8 we used utility classes:

```java
class MathUtil {
    static int square(int x) {
        return x * x;
    }
}
```

---

## Solution

Put utility methods directly inside the interface.

```java
interface Calculator {

    static int square(int x) {
        return x * x;
    }
}
```

Usage:

```java
System.out.println(Calculator.square(5));
```

Output:

```text
25
```

---

### Why introduced?

To keep helper methods close to the interface they belong to.

Example from JDK:

```java
Comparator.comparing()
List.of()
Map.of()
Predicate.isEqual()
```

---

## Important

Static methods are NOT inherited.

```java
interface Test {
    static void show() {
        System.out.println("Hello");
    }
}

class Demo implements Test {
}
```

Wrong:

```java
Demo.show(); // Compilation Error
```

Correct:

```java
Test.show();
```

---

# 3. Private Methods (Java 9)

Java 8 introduced default methods.

But default methods often had duplicate code.

Example:

```java
interface Logger {

    default void info(String msg) {
        System.out.println("[INFO] " + msg);
    }

    default void error(String msg) {
        System.out.println("[ERROR] " + msg);
    }
}
```

Imagine complex formatting logic repeated in many default methods.

---

## Solution: Private Methods

```java
interface Logger {

    default void info(String msg) {
        print("INFO", msg);
    }

    default void error(String msg) {
        print("ERROR", msg);
    }

    private void print(String level, String msg) {
        System.out.println("[" + level + "] " + msg);
    }
}
```

Usage:

```java
class AppLogger implements Logger {
}

public class Main {
    public static void main(String[] args) {

        AppLogger log = new AppLogger();

        log.info("Application Started");
        log.error("Something went wrong");
    }
}
```

Output:

```text
[INFO] Application Started
[ERROR] Something went wrong
```

---

## Why introduced?

To avoid duplicate code inside:

* default methods
* static methods

and improve encapsulation.

---

# Interview Answer (Short)

### Default Methods (Java 8)

* Introduced to support backward compatibility.
* Allow adding new methods to interfaces without breaking existing implementations.
* Can have a method body.

```java
default void show() {
    System.out.println("Default");
}
```

---

### Static Methods (Java 8)

* Allow utility/helper methods inside interfaces.
* Called using interface name.
* Not inherited by implementing classes.

```java
static void display() {
    System.out.println("Static");
}
```

---

### Private Methods (Java 9)

* Allow code reuse among default and static methods.
* Improve encapsulation.
* Cannot be accessed outside the interface.

```java
private void helper() {
    System.out.println("Helper");
}
```

---

### Real Reason in One Line

* **Default methods** → Interface evolution without breaking old code.
* **Static methods** → Keep interface-related utility methods inside the interface.
* **Private methods** → Remove duplication and hide internal implementation logic inside interfaces.
Bahut accha question hai. Ye exactly wahi confusion hai jo Java 8 seekhte waqt sabko hoti hai.

### Java 7 tak

Interface ka purpose tha **100% abstraction**.

Isliye interface me sirf abstract methods allowed the:

```java
interface Vehicle {
    void start(); // compiler automatically abstract maan leta hai
}
```

Compiler internally ise aise dekhta tha:

```java
interface Vehicle {
    public abstract void start();
}
```

Agar tum body dete:

```java
interface Vehicle {
    void stop() {
        System.out.println("Stop");
    }
}
```

To Java 7 me compilation error aata.

---

### Java 8 me kya badla?

Java designers ko problem hui ki existing interfaces me naye methods add nahi kar pa rahe the.

For example:

```java
interface List {
    void add();
    void remove();
}
```

Lakho classes `List` implement kar rahi hain.

Agar Oracle naya method add kare:

```java
interface List {
    void add();
    void remove();
    void sort();
}
```

To puri duniya ka code toot jayega.

Is problem ko solve karne ke liye Java 8 me **default methods** introduce kiye gaye.

Ab compiler ko explicitly batana padta hai:

```java
interface Vehicle {

    void start();

    default void stop() {
        System.out.println("Vehicle Stopped");
    }
}
```

Yaha `default` keyword compiler ko bol raha hai:

> "Haan, mujhe pata hai ye interface hai, lekin is method ki implementation intentionally di gayi hai."

---

### Kya ye concrete method hai?

**Haan.**

Ye ek concrete method hi hai.

```java
default void stop() {
    System.out.println("Vehicle Stopped");
}
```

Isme body hai, implementation hai, executable code hai.

Bas difference ye hai ki ye interface ke andar hai aur `default` keyword se mark kiya gaya hai.

---

### Implementing class ko implement karna padega?

Nahi.

```java
class Car implements Vehicle {

    @Override
    public void start() {
        System.out.println("Car Started");
    }
}
```

Ye perfectly valid hai.

```java
Car c = new Car();

c.start();
c.stop();
```

Output:

```text
Car Started
Vehicle Stopped
```

`stop()` method Car me nahi hai, fir bhi chal raha hai kyunki implementation interface se mil rahi hai.

---

### Override kar sakte hain?

Bilkul.

```java
class Car implements Vehicle {

    @Override
    public void start() {
        System.out.println("Car Started");
    }

    @Override
    public void stop() {
        System.out.println("Car Stopped");
    }
}
```

Ab:

```java
new Car().stop();
```

Output:

```text
Car Stopped
```

Class ki implementation interface wali implementation ko override kar degi.

---

### Interview me kya bolna hai?

> Java 7 tak interface me sirf abstract methods allowed the. Java 8 me backward compatibility aur interface evolution ke liye default methods introduce kiye gaye. Default methods interface ke andar concrete methods hote hain jinki implementation already provided hoti hai, isliye implementing classes ko unhe implement karna mandatory nahi hota.

Yahi reason hai ki tum interface ke andar concrete method dekh rahe ho — **ye normal concrete method nahi, `default` concrete method hai jo Java 8 ke baad allow hua hai.**
