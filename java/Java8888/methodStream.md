# Method Reference - Java 8 Interview Notes

---

# What is Method Reference?

Method Reference is a shorter and cleaner way of writing a lambda expression.

Instead of:

```java
x -> someMethod(x)
```

we can write:

```java
ClassName::methodName
```

Think:

```text
Lambda Shortcut
```

---

# Why Method Reference?

Without Method Reference:

```java
names.forEach(
        name -> System.out.println(name)
);
```

With Method Reference:

```java
names.forEach(
        System.out::println
);
```

Both are same.

Method Reference makes code cleaner and more readable.

---

# Syntax

```java
ClassName::methodName
```

or

```java
objectReference::methodName
```

---

# Example 1

Lambda

```java
Consumer<String> print =
        str -> System.out.println(str);
```

Method Reference

```java
Consumer<String> print =
        System.out::println;
```

---

# How Compiler Thinks

```java
str -> System.out.println(str)
```

becomes

```java
System.out::println
```

---

# Types of Method References

Java provides 4 types.

---

# 1. Static Method Reference

Reference to a static method.

Lambda:

```java
Function<Integer,String> converter =
        n -> String.valueOf(n);
```

Method Reference:

```java
Function<Integer,String> converter =
        String::valueOf;
```

Usage:

```java
System.out.println(
        converter.apply(100)
);
```

Output:

```text
100
```

---

# 2. Instance Method Reference of Particular Object

Reference to a method of a specific object.

Lambda:

```java
String str = "Java";

Supplier<String> supplier =
        () -> str.toLowerCase();
```

Method Reference:

```java
String str = "Java";

Supplier<String> supplier =
        str::toLowerCase;
```

Output:

```text
java
```

---

# 3. Instance Method Reference of Arbitrary Object

Most common in Stream API.

Lambda:

```java
names.stream()
     .map(name ->
          name.toUpperCase());
```

Method Reference:

```java
names.stream()
     .map(String::toUpperCase);
```

Output:

```text
JAVA
SPRING
KAFKA
```

---

# 4. Constructor Reference

Reference to constructor.

Lambda:

```java
Supplier<Employee> supplier =
        () -> new Employee();
```

Method Reference:

```java
Supplier<Employee> supplier =
        Employee::new;
```

Usage:

```java
Employee emp =
        supplier.get();
```

---

# Method Reference with Functional Interfaces

---

## Predicate

Lambda:

```java
Predicate<String> p =
        str -> str.isEmpty();
```

Method Reference:

```java
Predicate<String> p =
        String::isEmpty;
```

---

## Function

Lambda:

```java
Function<String,Integer> f =
        str -> str.length();
```

Method Reference:

```java
Function<String,Integer> f =
        String::length;
```

---

## Consumer

Lambda:

```java
Consumer<String> c =
        str -> System.out.println(str);
```

Method Reference:

```java
Consumer<String> c =
        System.out::println;
```

---

## Supplier

Lambda:

```java
Supplier<Date> s =
        () -> new Date();
```

Method Reference:

```java
Supplier<Date> s =
        Date::new;
```

---

# Method Reference in Stream API

---

## forEach()

Lambda:

```java
names.stream()
     .forEach(
          name ->
              System.out.println(name)
     );
```

Method Reference:

```java
names.stream()
     .forEach(
          System.out::println
     );
```

---

## map()

Lambda:

```java
names.stream()
     .map(
         name ->
             name.toUpperCase()
     );
```

Method Reference:

```java
names.stream()
     .map(
         String::toUpperCase
     );
```

---

## Constructor Reference

Lambda:

```java
names.stream()
     .map(
         name ->
             new Employee(name)
     );
```

Method Reference:

```java
names.stream()
     .map(Employee::new);
```

---

# Internal Working

Lambda:

```java
str -> str.length()
```

Compiler converts to:

```java
String::length
```

Method Reference is only syntax sugar.

No new functionality.

Just cleaner code.

---

# Frequently Asked Interview Questions

## Q1. What is Method Reference?

Method Reference is a shorthand notation for lambda expressions.

---

## Q2. What symbol is used?

```java
::
```

(Double Colon Operator)

---

## Q3. Why use Method Reference?

```text
Cleaner Code
Better Readability
Less Boilerplate
```

---

## Q4. How many types of Method References exist?

```text
1. Static Method Reference

2. Instance Method Reference
   of Particular Object

3. Instance Method Reference
   of Arbitrary Object

4. Constructor Reference
```

---

## Q5. Difference between Lambda and Method Reference?

Lambda:

```java
name -> name.toUpperCase()
```

Method Reference:

```java
String::toUpperCase
```

Method Reference is just a shorter version.

---

## Q6. Can every Lambda be converted into Method Reference?

No.

Only when lambda simply calls an existing method.

Example:

Can convert:

```java
str -> str.length()
```

to

```java
String::length
```

Cannot convert:

```java
str -> str.length() + 5
```

because additional logic exists.

---

# Most Asked Stream API Examples

## Print Elements

```java
list.stream()
    .forEach(System.out::println);
```

---

## Convert To Uppercase

```java
list.stream()
    .map(String::toUpperCase)
    .forEach(System.out::println);
```

---

## Get Length

```java
list.stream()
    .map(String::length)
    .forEach(System.out::println);
```

---

# Quick Revision

```text
Method Reference
=
Shortcut of Lambda

Operator:
::

Types:

1. Static Method
   ClassName::staticMethod

2. Particular Object
   object::method

3. Arbitrary Object
   ClassName::method

4. Constructor
   ClassName::new

Stream API:

forEach()
    -> System.out::println

map()
    -> String::toUpperCase

map()
    -> String::length
```

---

# Memory Trick

```text
Lambda
=
"Do this work"

Method Reference
=
"That method already does the work"

Use ::
```


=========================================
# Method Reference Cheat Sheet

| Functional Interface | Lambda Expression | Method Reference |
|---------------------|------------------|------------------|
| Predicate<String> | str -> str.isEmpty() | String::isEmpty |
| Predicate<String> | str -> str.isBlank() | String::isBlank |
| Predicate<String> | str -> str.startsWith("A") | Cannot Use Directly |
| Function<String,Integer> | str -> str.length() | String::length |
| Function<String,String> | str -> str.toUpperCase() | String::toUpperCase |
| Function<String,String> | str -> str.toLowerCase() | String::toLowerCase |
| Function<String,String> | str -> str.trim() | String::trim |
| Function<Integer,String> | n -> String.valueOf(n) | String::valueOf |
| Function<Integer,Integer> | n -> Math.abs(n) | Math::abs |
| Function<Double,Double> | n -> Math.sqrt(n) | Math::sqrt |
| Function<List<?>,Integer> | list -> list.size() | List::size |
| Consumer<String> | str -> System.out.println(str) | System.out::println |
| Consumer<String> | str -> System.err.println(str) | System.err::println |
| Consumer<Employee> | emp -> emp.printDetails() | Employee::printDetails |
| Supplier<Employee> | () -> new Employee() | Employee::new |
| Supplier<ArrayList<String>> | () -> new ArrayList<>() | ArrayList::new |
| Supplier<HashMap<Integer,String>> | () -> new HashMap<>() | HashMap::new |
| Supplier<Date> | () -> new Date() | Date::new |
| Supplier<Random> | () -> new Random() | Random::new |
| Supplier<StringBuilder> | () -> new StringBuilder() | StringBuilder::new |
| Runnable | () -> System.out.println("Running") | Main::runTask |
| Comparator<String> | (a,b) -> a.compareTo(b) | String::compareTo |

---

# Stream API Examples

| Lambda | Method Reference |
|----------|----------------|
| .forEach(name -> System.out.println(name)) | .forEach(System.out::println) |
| .map(name -> name.toUpperCase()) | .map(String::toUpperCase) |
| .map(name -> name.toLowerCase()) | .map(String::toLowerCase) |
| .map(name -> name.length()) | .map(String::length) |
| .filter(name -> name.isEmpty()) | .filter(String::isEmpty) |
| .sorted((a,b) -> a.compareTo(b)) | .sorted(String::compareTo) |
| .map(name -> new Employee(name)) | .map(Employee::new) |

---

# Most Important Interview Examples

```java
System.out::println

String::length

String::toUpperCase

String::toLowerCase

String::trim

String::isEmpty

Math::abs

Math::sqrt

String::valueOf

Employee::new

ArrayList::new

HashMap::new

String::compareTo
```

---

# Quick Memory Trick

```text
Predicate -> String::isEmpty

Function  -> String::length

Consumer  -> System.out::println

Supplier  -> Employee::new
```