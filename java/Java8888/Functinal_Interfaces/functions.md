# Function<T,R> - Java Interview Notes

## What is Function?

Function is a Functional Interface introduced in Java 8.

It accepts one input and returns one output.

Definition:

```java
@FunctionalInterface
public interface Function<T, R> {

    R apply(T t);
}
```

Where:

```text
T = Input Type
R = Return Type
```

Think:

```text
Input ---> Transformation ---> Output
```

Unlike Predicate:

```text
Predicate
Input -> boolean
```

Function:

```text
Function
Input -> Output
```

---

# Basic Example

```java
import java.util.function.Function;

public class Main {

    public static void main(String[] args) {

        Function<String,Integer> lengthFunction =
                str -> str.length();

        System.out.println(
                lengthFunction.apply("Java")
        );
    }
}
```

Output:

```text
4
```

Explanation:

```text
Input  = "Java"
Output = 4
```

---

# Real Meaning of Function

Function is used when we need to convert one thing into another.

Examples:

```text
String -> Integer
Employee -> Salary
Name -> Uppercase
Number -> Square
User -> Email
```

---

# Example 1

String to Length

```java
Function<String,Integer> getLength =
        str -> str.length();

System.out.println(
        getLength.apply("SpringBoot")
);
```

Output:

```text
10
```

---

# Example 2

Number to Square

```java
Function<Integer,Integer> square =
        n -> n * n;

System.out.println(
        square.apply(5)
);
```

Output:

```text
25
```

---

# Example 3

Convert String to Uppercase

```java
Function<String,String> upperCase =
        str -> str.toUpperCase();

System.out.println(
        upperCase.apply("java")
);
```

Output:

```text
JAVA
```

---

# Example 4

Employee to Salary

```java
Function<Employee,Double> getSalary =
        emp -> emp.getSalary();
```

Input:

```text
Employee Object
```

Output:

```text
Salary
```

---

# Most Important Method

## apply()

Used to execute the function.

```java
Function<Integer,Integer> square =
        n -> n * n;

square.apply(4);
```

Output:

```text
16
```

Interview Question:

### Which method is present in Function?

```java
R apply(T t)
```

---

# Function in Stream API

Most common usage:

```java
map()
```

---

## Example 1

Convert names to uppercase

```java
List<String> names =
        Arrays.asList(
                "ram",
                "shyam",
                "mohan"
        );

names.stream()
     .map(name ->
             name.toUpperCase())
     .forEach(System.out::println);
```

Output:

```text
RAM
SHYAM
MOHAN
```

Here:

```java
name -> name.toUpperCase()
```

is a Function.

---

## Example 2

Find lengths of strings

```java
List<String> names =
        Arrays.asList(
                "Java",
                "Spring",
                "Kafka"
        );

names.stream()
     .map(str ->
             str.length())
     .forEach(System.out::println);
```

Output:

```text
4
6
5
```

---

# Internal Working of map()

```java
stream.map(function)
```

Internally:

```text
Java
  |
Function
  |
4

Spring
  |
Function
  |
6

Kafka
  |
Function
  |
5
```

Result:

```text
[4,6,5]
```

---

# Function Composition

Very Important Interview Topic

Function provides:

```java
andThen()
compose()
```

---

## andThen()

First execute current function.

Then execute next function.

```java
Function<Integer,Integer> multiplyBy2 =
        n -> n * 2;

Function<Integer,Integer> add10 =
        n -> n + 10;

Function<Integer,Integer> result =
        multiplyBy2.andThen(add10);

System.out.println(
        result.apply(5)
);
```

Execution:

```text
5
|
*2
|
10
|
+10
|
20
```

Output:

```text
20
```

---

## compose()

Reverse order.

```java
Function<Integer,Integer> multiplyBy2 =
        n -> n * 2;

Function<Integer,Integer> add10 =
        n -> n + 10;

Function<Integer,Integer> result =
        multiplyBy2.compose(add10);

System.out.println(
        result.apply(5)
);
```

Execution:

```text
5
|
+10
|
15
|
*2
|
30
```

Output:

```text
30
```

---

# identity()

Returns same object.

```java
Function<String,String> identity =
        Function.identity();

System.out.println(
        identity.apply("Java")
);
```

Output:

```text
Java
```

---

# Function vs Predicate

| Function | Predicate |
|-----------|-----------|
| Input -> Output | Input -> Boolean |
| apply() | test() |
| map() | filter() |
| Transformation | Condition Checking |

Function:

```java
Function<Integer,Integer> square =
        n -> n * n;
```

Output:

```text
25
```

Predicate:

```java
Predicate<Integer> even =
        n -> n % 2 == 0;
```

Output:

```text
true
```

---

# Function vs Consumer

Function:

```java
Input -> Output
```

Consumer:

```java
Input -> No Output
```

Function:

```java
Function<String,Integer> length =
        str -> str.length();
```

Consumer:

```java
Consumer<String> print =
        str -> System.out.println(str);
```

---

# Function vs Supplier

Function:

```java
Input Required
Output Returned
```

Supplier:

```java
No Input
Output Returned
```

Function:

```java
Function<Integer,Integer> square =
        n -> n * n;
```

Supplier:

```java
Supplier<String> supplier =
        () -> "Hello";
```

---

# Real Project Examples

## Convert User Object to Email

```java
Function<User,String> getEmail =
        user -> user.getEmail();
```

---

## Convert Employee to EmployeeDTO

```java
Function<Employee,EmployeeDTO> converter =
        emp -> new EmployeeDTO(
                emp.getId(),
                emp.getName()
        );
```

---

## Stream Mapping

```java
employees.stream()
         .map(Employee::getName)
         .forEach(System.out::println);
```

Input:

```text
Employee
```

Output:

```text
Name
```

---

# Frequently Asked Interview Questions

## Q1. What is Function?

Function is a Functional Interface that accepts one argument and returns a result.

---

## Q2. What is the abstract method of Function?

```java
R apply(T t);
```

---

## Q3. Which package contains Function?

```java
java.util.function
```

---

## Q4. Which Stream API method commonly uses Function?

```java
map()
```

---

## Q5. Difference between Function and Predicate?

Function:

```text
Input -> Output
```

Predicate:

```text
Input -> Boolean
```

---

## Q6. Difference between Function and Consumer?

Function returns a value.

Consumer does not return anything.

---

## Q7. Difference between Function and Supplier?

Function requires input.

Supplier does not require input.

---

## Q8. What are the important methods of Function?

```java
apply()
andThen()
compose()
identity()
```

---

## Q9. What is andThen()?

Executes current function first and next function afterwards.

---

## Q10. What is compose()?

Executes supplied function first and current function afterwards.

---

# Quick Revision

```text
Function<T,R>

T = Input Type
R = Return Type

Method:
apply()

Default Methods:
andThen()
compose()

Static Method:
identity()

Common Stream Usage:
map()

Examples:
String -> Length
String -> Uppercase
Employee -> Salary
Employee -> DTO

Function:
Input -> Output

Predicate:
Input -> Boolean

Consumer:
Input -> No Output

Supplier:
No Input -> Output
``` 