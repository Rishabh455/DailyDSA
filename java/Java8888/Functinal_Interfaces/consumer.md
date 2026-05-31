# Consumer<T> - Java Interview Notes

## What is Consumer?

Consumer is a Functional Interface introduced in Java 8.

It accepts one input and returns nothing.

Definition:

```java
@FunctionalInterface
public interface Consumer<T> {

    void accept(T t);
}
```

Think:

```text
Input ---> Consume ---> No Return Value
```

---

# Why is it called Consumer?

Because it consumes data and performs an operation on it.

Examples:

```text
Print data
Save data
Send email
Write log
Update database
```

Unlike Function:

```text
Function
Input -> Output
```

Consumer:

```text
Input -> No Output
```

---

# Basic Example

```java
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {

        Consumer<String> print =
                str -> System.out.println(str);

        print.accept("Hello Java");
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
void accept(T t);
```

Interview Question:

### Which method is present in Consumer?

```java
accept()
```

---

# Example 1

Print Number

```java
Consumer<Integer> print =
        n -> System.out.println(n);

print.accept(100);
```

Output:

```text
100
```

---

# Example 2

Print Square

```java
Consumer<Integer> square =
        n -> System.out.println(n * n);

square.accept(5);
```

Output:

```text
25
```

---

# Example 3

Print Employee Name

```java
Consumer<Employee> printName =
        emp -> System.out.println(
                emp.getName()
        );
```

Input:

```text
Employee Object
```

Output:

```text
Employee Name Printed
```

---

# Most Important Method

## accept()

Executes the consumer.

```java
Consumer<String> print =
        str -> System.out.println(str);

print.accept("Java");
```

Output:

```text
Java
```

---

# Consumer in Stream API

Most common usage:

```java
forEach()
```

---

## Example 1

Print Elements

```java
List<String> names =
        Arrays.asList(
                "Ram",
                "Shyam",
                "Mohan"
        );

names.stream()
     .forEach(System.out::println);
```

Output:

```text
Ram
Shyam
Mohan
```

Internally:

```java
Consumer<String> print =
        System.out::println;
```

---

## Example 2

Using Consumer Variable

```java
Consumer<String> print =
        System.out::println;

names.stream()
     .forEach(print);
```

---

## Example 3

Print Employee Names

```java
employees.stream()
         .forEach(emp ->
                 System.out.println(
                         emp.getName()
                 ));
```

Consumer:

```java
emp -> System.out.println(emp.getName())
```

---

# Internal Working of forEach()

```java
stream.forEach(consumer)
```

Internally:

```text
Ram
 |
Consumer.accept()
 |
Printed

Shyam
 |
Consumer.accept()
 |
Printed

Mohan
 |
Consumer.accept()
 |
Printed
```

---

# Consumer Chaining

Very Important Interview Topic

Method:

```java
andThen()
```

---

## Example

```java
Consumer<String> print =
        str -> System.out.println(
                "Hello " + str
        );

Consumer<String> upper =
        str -> System.out.println(
                str.toUpperCase()
        );

Consumer<String> result =
        print.andThen(upper);

result.accept("java");
```

Output:

```text
Hello java
JAVA
```

Execution:

```text
java
 |
print()
 |
Hello java
 |
upper()
 |
JAVA
```

---

# BiConsumer

Used when two inputs are required.

```java
BiConsumer<String,Integer> employee =
        (name,salary) ->
                System.out.println(
                        name + " " + salary
                );

employee.accept(
        "Rishabh",
        50000
);
```

Output:

```text
Rishabh 50000
```

---

# Consumer vs Function

| Consumer | Function |
|-----------|-----------|
| Input -> No Output | Input -> Output |
| accept() | apply() |
| forEach() | map() |

Consumer:

```java
Consumer<String> print =
        System.out::println;
```

Function:

```java
Function<String,Integer> length =
        str -> str.length();
```

---

# Consumer vs Predicate

| Consumer | Predicate |
|-----------|-----------|
| Returns Nothing | Returns Boolean |
| Performs Action | Checks Condition |
| accept() | test() |

Consumer:

```java
Consumer<String> print =
        System.out::println;
```

Predicate:

```java
Predicate<Integer> even =
        n -> n % 2 == 0;
```

---

# Consumer vs Supplier

| Consumer | Supplier |
|-----------|-----------|
| Input Required | No Input |
| No Output | Output Returned |

Consumer:

```java
Consumer<String> print =
        System.out::println;
```

Supplier:

```java
Supplier<String> s =
        () -> "Hello";
```

---

# Real Project Examples

## Logging

```java
Consumer<String> logger =
        msg -> log.info(msg);
```

---

## Sending Email

```java
Consumer<User> sendEmail =
        user -> emailService.send(user);
```

---

## Saving Employee

```java
Consumer<Employee> saveEmployee =
        emp -> repository.save(emp);
```

---

## Kafka Message Processing

```java
Consumer<String> processMessage =
        msg -> System.out.println(msg);
```

Message received and processed.

---

# Frequently Asked Interview Questions

## Q1. What is Consumer?

Consumer is a Functional Interface that accepts one input and returns nothing.

---

## Q2. What is the abstract method of Consumer?

```java
void accept(T t);
```

---

## Q3. Which package contains Consumer?

```java
java.util.function
```

---

## Q4. Which Stream API method commonly uses Consumer?

```java
forEach()
```

---

## Q5. Difference between Consumer and Function?

Consumer:

```text
Input -> No Output
```

Function:

```text
Input -> Output
```

---

## Q6. Difference between Consumer and Predicate?

Consumer performs an action.

Predicate evaluates a condition and returns boolean.

---

## Q7. What is BiConsumer?

A Functional Interface that accepts two inputs and returns nothing.

```java
BiConsumer<T,U>
```

---

## Q8. What is andThen() in Consumer?

Used to chain multiple Consumers.

```java
consumer1.andThen(consumer2);
```

---

## Q9. Real-world use cases of Consumer?

```text
Logging
Printing
Saving Data
Sending Emails
Kafka Message Processing
Database Updates
```

---

# Consumer + Stream API Interview Example

```java
List<String> names =
        Arrays.asList(
                "Java",
                "Spring",
                "Kafka"
        );

Consumer<String> print =
        System.out::println;

names.stream()
     .forEach(print);
```

Output:

```text
Java
Spring
Kafka
```

---

# Quick Revision

```text
Consumer<T>

Input  -> One Argument
Output -> Nothing

Method:
accept()

Default Method:
andThen()

Stream API:
forEach()

BiConsumer:
Two Inputs

Use Cases:
Printing
Logging
Saving
Email Sending
Kafka Processing
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
----------------------
filter()  -> Predicate
map()     -> Function
forEach() -> Consumer