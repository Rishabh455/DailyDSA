# Supplier<T> - Java Interview Notes

## What is Supplier?

Supplier is a Functional Interface introduced in Java 8.

It does not accept any input and returns a value.

Definition:

```java
@FunctionalInterface
public interface Supplier<T> {

    T get();
}
```

Think:

```text
No Input ---> Supply Value ---> Output
```

---

# Why is it called Supplier?

Because it supplies or provides data whenever requested.

Examples:

```text
Generate OTP
Generate Random Number
Get Current Date
Create Object
Fetch Configuration
```

Unlike Consumer:

```text
Consumer
Input -> No Output
```

Supplier:

```text
No Input -> Output
```

---

# Basic Example

```java
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {

        Supplier<String> supplier =
                () -> "Hello Java";

        System.out.println(
                supplier.get()
        );
    }
}
```

Output:

```text
Hello Java
```

---

# Abstract Method

```java
T get();
```

Interview Question:

### Which method is present in Supplier?

```java
get()
```

---

# Example 1

Return a Name

```java
Supplier<String> name =
        () -> "Rishabh";

System.out.println(
        name.get()
);
```

Output:

```text
Rishabh
```

---

# Example 2

Generate Random Number

```java
import java.util.Random;

Supplier<Integer> random =
        () -> new Random().nextInt(100);

System.out.println(
        random.get()
);
```

Output:

```text
57
```

(Random value each time)

---

# Example 3

Current Date

```java
Supplier<LocalDate> today =
        () -> LocalDate.now();

System.out.println(
        today.get()
);
```

Output:

```text
2026-05-31
```

---

# Example 4

Create Employee Object

```java
Supplier<Employee> employeeSupplier =
        () -> new Employee(
                101,
                "Rishabh"
        );

Employee emp =
        employeeSupplier.get();
```

Input:

```text
Nothing
```

Output:

```text
Employee Object
```

---

# Internal Working

```java
Supplier<String> s =
        () -> "Java";
```

When:

```java
s.get();
```

Internally:

```text
No Input
   |
Supplier.get()
   |
Returns "Java"
```

---

# Supplier in Stream API

Most common usage:

```java
Stream.generate()
```

---

## Example 1

Generate Constant Values

```java
Stream.generate(() -> "Java")
      .limit(3)
      .forEach(System.out::println);
```

Output:

```text
Java
Java
Java
```

---

## Example 2

Generate Random Numbers

```java
Stream.generate(
        () -> new Random().nextInt(100)
)
.limit(5)
.forEach(System.out::println);
```

Output:

```text
12
45
78
21
99
```

(Random every run)

---

# Optional + Supplier

Very Important Interview Topic

Used in:

```java
orElseGet()
```

---

## Example

```java
Optional<String> name =
        Optional.empty();

String result =
        name.orElseGet(
                () -> "Default Name"
        );

System.out.println(result);
```

Output:

```text
Default Name
```

Supplier is executed only when value is absent.

---

# Supplier vs Function

| Supplier | Function |
|-----------|-----------|
| No Input | Input Required |
| Returns Output | Returns Output |
| get() | apply() |

Supplier:

```java
Supplier<String> s =
        () -> "Java";
```

Function:

```java
Function<String,Integer> f =
        str -> str.length();
```

---

# Supplier vs Consumer

| Supplier | Consumer |
|-----------|-----------|
| No Input | Input Required |
| Returns Output | No Output |
| get() | accept() |

Supplier:

```java
Supplier<String> s =
        () -> "Java";
```

Consumer:

```java
Consumer<String> c =
        str -> System.out.println(str);
```

---

# Supplier vs Predicate

| Supplier | Predicate |
|-----------|-----------|
| No Input | Input Required |
| Returns Value | Returns Boolean |
| get() | test() |

Supplier:

```java
Supplier<String> s =
        () -> "Java";
```

Predicate:

```java
Predicate<Integer> p =
        n -> n % 2 == 0;
```

---

# Real Project Examples

## Generate OTP

```java
Supplier<Integer> otpSupplier =
        () -> 100000 +
              new Random().nextInt(900000);
```

---

## Generate UUID

```java
Supplier<String> uuidSupplier =
        () -> UUID.randomUUID().toString();
```

---

## Lazy Object Creation

```java
Supplier<Employee> empSupplier =
        () -> new Employee();
```

Object created only when:

```java
empSupplier.get();
```

is called.

---

## Default Configuration

```java
String value =
        optionalValue.orElseGet(
                () -> "DEFAULT"
        );
```

---

# Frequently Asked Interview Questions

## Q1. What is Supplier?

Supplier is a Functional Interface that takes no input and returns a value.

---

## Q2. What is the abstract method of Supplier?

```java
T get();
```

---

## Q3. Which package contains Supplier?

```java
java.util.function
```

---

## Q4. Does Supplier take any input?

No.

```text
Input = None
Output = One Value
```

---

## Q5. Which Stream API method commonly uses Supplier?

```java
Stream.generate()
```

---

## Q6. Difference between Supplier and Function?

Supplier:

```text
No Input -> Output
```

Function:

```text
Input -> Output
```

---

## Q7. Difference between Supplier and Consumer?

Supplier returns data.

Consumer consumes data.

---

## Q8. Real-world use cases of Supplier?

```text
OTP Generation
UUID Generation
Current Date
Lazy Initialization
Default Values
Object Creation
```

---

## Q9. Where is Supplier used with Optional?

```java
orElseGet()
```

Example:

```java
optional.orElseGet(
        () -> "Default"
);
```

---

# Most Asked Interview Mapping

```text
filter()      -> Predicate
map()         -> Function
forEach()     -> Consumer
generate()    -> Supplier
orElseGet()   -> Supplier
```

---

# Quick Revision

```text
Supplier<T>

Input  -> None
Output -> One Value

Method:
get()

Common Stream Usage:
Stream.generate()

Optional Usage:
orElseGet()

Examples:
OTP Generation
UUID Generation
Current Date
Object Creation
Lazy Initialization
```

---

# Memory Trick

```text
Predicate = Ask Question
            (Input -> Boolean)

Function  = Transform Data
            (Input -> Output)

Consumer  = Use Data
            (Input -> Nothing)

Supplier  = Give Data
            (Nothing -> Output)
```