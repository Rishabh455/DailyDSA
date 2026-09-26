````md
# Predicate<T> - Java Interview Notes

## What is Predicate?

Predicate is a Functional Interface introduced in Java 8.

It accepts one input and returns a boolean value.

Used for:
- Filtering
- Validation
- Condition Checking
- Searching

Definition:

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}
```

Think:

```text
Input -> Condition Check -> true/false
```

---

## Basic Example

```java
import java.util.function.Predicate;

public class Main {

    public static void main(String[] args) {

        Predicate<Integer> isEven =
                n -> n % 2 == 0;

        System.out.println(isEven.test(4));
        System.out.println(isEven.test(5));
    }
}
```

Output:

```text
true
false
```

---

## Important Methods

### 1. test()

Evaluates the condition.

```java
Predicate<Integer> isEven =
        n -> n % 2 == 0;

System.out.println(isEven.test(10));
```

Output:

```text
true
```

---

### 2. and()

Both conditions must be true.

```java
Predicate<Integer> greaterThan10 =
        n -> n > 10;

Predicate<Integer> even =
        n -> n % 2 == 0;

Predicate<Integer> result =
        greaterThan10.and(even);

System.out.println(result.test(12));
```

Output:

```text
true
```

---

### 3. or()

At least one condition must be true.

```java
Predicate<Integer> greaterThan10 =
        n -> n > 10;

Predicate<Integer> even =
        n -> n % 2 == 0;

Predicate<Integer> result =
        greaterThan10.or(even);

System.out.println(result.test(8));
```

Output:

```text
true
```

---

### 4. negate()

Reverses the result.

```java
Predicate<Integer> even =
        n -> n % 2 == 0;

Predicate<Integer> odd =
        even.negate();

System.out.println(odd.test(5));
```

Output:

```text
true
```

---

# Predicate with Stream API

Predicate is most commonly used with filter().

---

## Example 1: Even Numbers

```java
import java.util.*;
import java.util.stream.*;

public class Main {

    public static void main(String[] args) {

        List<Integer> numbers =
                Arrays.asList(1,2,3,4,5,6);

        numbers.stream()
               .filter(n -> n % 2 == 0)
               .forEach(System.out::println);
    }
}
```

Output:

```text
2
4
6
```

---

## Example 2: Using Predicate Variable

```java
import java.util.*;
import java.util.function.Predicate;

public class Main {

    public static void main(String[] args) {

        List<Integer> numbers =
                Arrays.asList(1,2,3,4,5,6);

        Predicate<Integer> isEven =
                n -> n % 2 == 0;

        numbers.stream()
               .filter(isEven)
               .forEach(System.out::println);
    }
}
```

---

## Example 3: Salary Greater Than 50000

```java
employees.stream()
         .filter(emp ->
                 emp.getSalary() > 50000)
         .forEach(System.out::println);
```

---

## Example 4: Names Starting With A

```java
names.stream()
     .filter(name ->
             name.startsWith("A"))
     .forEach(System.out::println);
```

---

# Internal Working of filter()

```java
stream.filter(predicate)
```

Internally:

```text
Element
   |
Predicate.test()
   |
true  -> Keep
false -> Remove
```

Example:

```text
1 -> false -> removed
2 -> true  -> kept
3 -> false -> removed
4 -> true  -> kept
```

Result:

```text
2,4
```

---

# BiPredicate

Used when two inputs are required.

```java
import java.util.function.BiPredicate;

public class Main {

    public static void main(String[] args) {

        BiPredicate<Integer,Integer> greater =
                (a,b) -> a > b;

        System.out.println(
                greater.test(10,5)
        );
    }
}
```

Output:

```text
true
```

---

# Predicate vs Function

| Predicate | Function |
|------------|------------|
| Returns boolean | Returns any type |
| Used for filtering | Used for transformation |
| Method = test() | Method = apply() |

Predicate:

```java
Predicate<Integer> p =
        n -> n > 10;
```

Returns:

```text
true / false
```

Function:

```java
Function<Integer,Integer> f =
        n -> n * 2;
```

Returns:

```text
20
```

---

# Interview Questions

## Q1. What is Predicate?

Predicate is a Functional Interface that accepts one argument and returns a boolean value.

---

## Q2. What is the abstract method of Predicate?

```java
boolean test(T t);
```

---

## Q3. Why is Predicate called a Functional Interface?

Because it contains only one abstract method:

```java
boolean test(T t);
```

---

## Q4. Which package contains Predicate?

```java
java.util.function
```

---

## Q5. Which Stream API method commonly uses Predicate?

```java
filter()
```

Example:

```java
list.stream()
    .filter(n -> n > 10)
    .forEach(System.out::println);
```

---

## Q6. What are the important methods of Predicate?

```java
test()
and()
or()
negate()
```

---

## Q7. Difference between Predicate and BiPredicate?

Predicate:

```java
Predicate<T>
```

One input.

BiPredicate:

```java
BiPredicate<T,U>
```

Two inputs.

---

## Q8. Difference between Predicate and Function?

Predicate:

```java
Input -> Boolean
```

Function:

```java
Input -> Output
```

---

## Q9. Can Predicate be chained?

Yes.

Using:

```java
and()
or()
negate()
```

Example:

```java
Predicate<Integer> p1 =
        n -> n > 10;

Predicate<Integer> p2 =
        n -> n % 2 == 0;

Predicate<Integer> result =
        p1.and(p2);
```

---

## Q10. Real Project Use Case of Predicate?

Filtering active users:

```java
users.stream()
     .filter(user ->
             user.isActive())
     .forEach(this::sendEmail);
```

Only active users are processed.

---

# Quick Revision

```text
Predicate
---------
Input  -> One Argument
Output -> Boolean

Method:
test()

Default Methods:
and()
or()
negate()

Common Stream Usage:
filter()

Package:
java.util.function

BiPredicate:
Two Inputs

Use Cases:
Filtering
Validation
Condition Checking
Searching
```
````
