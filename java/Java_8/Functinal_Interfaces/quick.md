# Java 8 Functional Interfaces + Optional Cheat Sheet

---

# 1. Predicate<T>

## Purpose

Used for condition checking.

```text
Input  -> One Value
Output -> boolean
```

## Abstract Method

```java
boolean test(T t);
```

## Common Methods

```java
test()
and()
or()
negate()
```

## Example

```java
Predicate<Integer> isEven =
        n -> n % 2 == 0;

System.out.println(
        isEven.test(4)
);
```

Output:

```text
true
```

## Stream API Usage

```java
filter()
```

Example:

```java
list.stream()
    .filter(n -> n > 10)
    .forEach(System.out::println);
```

## Interview Definition

Predicate is a Functional Interface that accepts one input and returns a boolean value.

---

# 2. Function<T,R>

## Purpose

Used for transformation.

```text
Input  -> Output
```

## Abstract Method

```java
R apply(T t);
```

## Common Methods

```java
apply()
andThen()
compose()
identity()
```

## Example

```java
Function<String,Integer> length =
        str -> str.length();

System.out.println(
        length.apply("Java")
);
```

Output:

```text
4
```

## Stream API Usage

```java
map()
```

Example:

```java
names.stream()
     .map(String::toUpperCase)
     .forEach(System.out::println);
```

## Interview Definition

Function is a Functional Interface that accepts one input and returns a result.

---

# 3. Consumer<T>

## Purpose

Used when we want to perform an action.

```text
Input  -> No Output
```

## Abstract Method

```java
void accept(T t);
```

## Common Methods

```java
accept()
andThen()
```

## Example

```java
Consumer<String> print =
        System.out::println;

print.accept("Java");
```

Output:

```text
Java
```

## Stream API Usage

```java
forEach()
```

Example:

```java
names.stream()
     .forEach(System.out::println);
```

## Interview Definition

Consumer is a Functional Interface that accepts one input and returns nothing.

---

# 4. Supplier<T>

## Purpose

Used when we need to provide data.

```text
No Input -> Output
```

## Abstract Method

```java
T get();
```

## Example

```java
Supplier<String> supplier =
        () -> "Java";

System.out.println(
        supplier.get()
);
```

Output:

```text
Java
```

## Common Usage

```java
Stream.generate()
Optional.orElseGet()
```

Example:

```java
Stream.generate(() -> "Java")
      .limit(3)
      .forEach(System.out::println);
```

## Interview Definition

Supplier is a Functional Interface that takes no input and returns a value.

---

# 5. Optional<T>

## Purpose

Used to avoid NullPointerException.

Think:

```text
Safe Box
```

May contain:

```text
Value
OR
Nothing
```

## Creation Methods

```java
Optional.of()

Optional.ofNullable()

Optional.empty()
```

---

## Important Methods

```java
get()

isPresent()

ifPresent()

orElse()

orElseGet()

orElseThrow()
```

---

## Example

```java
Optional<String> name =
        Optional.ofNullable(null);

System.out.println(
        name.orElse("Default")
);
```

Output:

```text
Default
```

---

## Spring Boot Usage

```java
Optional<User> user =
        userRepository.findById(id);
```

```java
User u =
        userRepository.findById(id)
                      .orElseThrow(
                          () -> new RuntimeException(
                              "User Not Found"
                          )
                      );
```

---

# Optional + Functional Interfaces

| Optional Method | Functional Interface |
|----------------|---------------------|
| filter() | Predicate |
| map() | Function |
| ifPresent() | Consumer |
| orElseGet() | Supplier |

---

# Most Asked Stream API Mapping

| Stream Method | Functional Interface |
|--------------|----------------------|
| filter() | Predicate |
| map() | Function |
| forEach() | Consumer |
| generate() | Supplier |

---

# Comparison Table

| Interface | Input | Output | Method |
|------------|--------|---------|---------|
| Predicate<T> | One | boolean | test() |
| Function<T,R> | One | Result | apply() |
| Consumer<T> | One | Nothing | accept() |
| Supplier<T> | None | Result | get() |

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

Optional  = Safe Box For Null Handling
```

---

# Interview One-Liners

Predicate:
Input -> Boolean

Function:
Input -> Output

Consumer:
Input -> Nothing

Supplier:
Nothing -> Output

Optional:
Safe way to handle null values and avoid NullPointerException.

---------------------------------------------------------------------------
filter()   -> Predicate -> test()

map()      -> Function  -> apply()

forEach()  -> Consumer  -> accept()

generate() -> Supplier  -> get()

Optional:
of()
ofNullable()
orElse()
orElseThrow()
isPresent()

Purpose:
Avoid NullPointerException