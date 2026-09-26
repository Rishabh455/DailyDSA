# Java 17 Features — Interview Notes

> **Java 17 = LTS release**
>
> Key interview topics:
>
> * Sealed Classes
> * Records
> * Pattern Matching for `instanceof`
> * Pattern Matching for `switch` *(Preview in Java 17)*
> * Enhanced Pseudo-Random Number Generators

---

## 1. Sealed Classes

### What is a Sealed Class?

A **sealed class/interface** restricts which classes are allowed to extend/implement it.

```java
public sealed class Payment
        permits UPI, Cash, CreditCard {
}
```

Only `UPI`, `Cash`, and `CreditCard` can directly extend `Payment`.

```java
public final class Cash extends Payment {
}

public non-sealed class UPI extends Payment {
}

public sealed class CreditCard extends Payment
        permits Amex {
}
```

### Why use Sealed Classes?

Normal inheritance:

```text
Payment
 ├── UPI
 ├── Cash
 ├── CreditCard
 └── Crypto   ❌
```

With sealed classes, we can explicitly control the hierarchy:

```text
Payment (sealed)
 ├── Cash (final)       → inheritance stops
 ├── UPI (non-sealed)   → inheritance open
 └── CreditCard (sealed)
       └── Amex (final)
```

### `final` vs `sealed` vs `non-sealed`

| Modifier     | Meaning                                     |
| ------------ | ------------------------------------------- |
| `final`      | Stops inheritance completely                |
| `sealed`     | Allows only explicitly permitted subclasses |
| `non-sealed` | Opens inheritance again                     |

### Important Rules

**Rule 1:** Every permitted subclass must be one of:

```text
final
sealed
non-sealed
```

Example:

```java
public sealed class Payment
        permits Cash, UPI {
}

public final class Cash extends Payment {
}

public non-sealed class UPI extends Payment {
}
```

---

**Rule 2:** A permitted subclass must actually extend/implement the sealed type.

```java
public sealed class Payment
        permits Cash {
}

// Must extend Payment
public final class Cash extends Payment {
}
```

---

**Rule 3:** A sealed hierarchy has to respect Java's package/module restrictions.

For a normal unnamed module/package setup, the sealed class and its permitted direct subclasses must be in the same package.

> **Interview phrase:**
> "A sealed class defines a closed inheritance hierarchy by explicitly specifying its permitted direct subclasses."

### Common Interview Question

**Q: Why not use `final` instead of `sealed`?**

`final` prevents **all** inheritance.

`sealed` allows **controlled inheritance**.

```text
final   → No child classes
sealed  → Only permitted child classes
non-sealed → Any child class
```

---

# 2. Records

### What is a Record?

A **record** is a special class designed primarily to represent immutable data with less boilerplate.

```java
public record Person(String name, int age) {
}
```

Instead of manually writing:

```text
fields
constructor
getters/accessors
equals()
hashCode()
toString()
```

Java generates the essential members automatically.

Records were finalized before Java 17, but are highly relevant when working with Java 17 LTS.

---

## Record Accessors

For:

```java
public record Person(String name, int age) {
}
```

Use:

```java
Person p = new Person("John", 25);

p.name();
p.age();
```

**Important:** Record accessors are:

```java
p.name()
p.age()
```

not:

```java
p.getName()
p.getAge()
```

---

## Automatically Generated Members

A record automatically provides:

```text
Canonical constructor
Accessor methods
equals()
hashCode()
toString()
```

Example:

```java
Person p1 = new Person("John", 25);
Person p2 = new Person("John", 25);

System.out.println(p1.equals(p2));  // true
```

Records use component values for equality.

---

## Are Records Immutable?

### Important interview trap:

**Records provide shallow immutability, not deep immutability.**

Record components are:

```text
private
final
```

So this is not allowed:

```java
p.age = 30;   // ❌
```

But if a component refers to a mutable object:

```java
public record Person(String name, List<String> hobbies) {
}
```

The list itself can still be modified:

```java
person.hobbies().add("Reading");
```

Therefore:

```text
primitive/String/immutable object
        ↓
effectively immutable

mutable object
        ↓
can still be modified
```

### Interview Answer

> "Records are shallowly immutable. Their components are final, but if a component references a mutable object such as a List, the object's internal state can still change."

---

# 3. Record Inheritance Rules

A record is implicitly `final`.

Therefore:

```java
class Child extends Person {
}
```

❌ Not allowed.

A record also **cannot extend another class**.

```java
record Person(...) extends SomeClass {
}
```

❌ Not allowed.

But a record **can implement interfaces**:

```java
interface Printable {
}

record Person(String name, int age)
        implements Printable {
}
```

✅ Allowed.

### Remember

```text
Record
 ├── cannot be extended
 ├── cannot extend another class
 └── can implement interfaces
```

---

# 4. Record Constructors

Records automatically have a **canonical constructor**.

```java
record Person(String name, int age) {
}
```

Equivalent conceptually to:

```java
Person(String name, int age) {
    this.name = name;
    this.age = age;
}
```

---

## Compact Constructor

Useful for validation:

```java
record Person(String name, int age) {

    public Person {
        if (age < 0) {
            throw new IllegalArgumentException("Invalid age");
        }
    }
}
```

Notice:

```java
public Person {
```

No parameter list and no explicit field assignment.

Java automatically performs the component initialization.

### Interview Use Case

> "Compact constructors are commonly used in records to validate or normalize input before the automatically generated field assignments."

---

# 5. Methods in Records

Records can contain methods.

```java
record Person(String name, int age) {

    public boolean isAdult() {
        return age >= 18;
    }
}
```

Usage:

```java
Person p = new Person("John", 25);

System.out.println(p.isAdult());
```

So:

> **Record ≠ only data. A record can also contain behavior.**

---

# 6. Static Members in Records

Records can have:

```java
static fields
static methods
```

Example:

```java
record Person(String name, int age) {

    static int count = 0;

    public static void printCount() {
        System.out.println(count);
    }
}
```

But records cannot declare additional non-static instance fields.

```java
record Person(String name, int age) {

    int x;   // ❌
}
```

### Remember

```text
Record
 ├── static field       ✅
 ├── static method      ✅
 ├── instance method    ✅
 └── extra instance field ❌
```

---

# 7. Pattern Matching for `instanceof`

### Before Java 17

Traditional code:

```java
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.toUpperCase());
}
```

Three steps:

```text
1. Type check
2. Cast
3. Variable declaration
```

### Java 17

```java
if (obj instanceof String s) {
    System.out.println(s.toUpperCase());
}
```

Pattern matching combines:

```text
Type check + Cast + Variable declaration
```

into one expression.

---

## Pattern Variable with Conditions

```java
if (obj instanceof Integer i && i > 10) {
    System.out.println(i * 2);
}
```

This works because `i` is definitely available when the second condition is evaluated.

### `&&` vs `||`

Works:

```java
obj instanceof Integer i && i > 10
```

Problematic:

```java
obj instanceof Integer i || i > 10
```

Why?

With `||`, the first condition could be false, meaning `i` was never created.

### Interview Rule

> Pattern variables are available only where the compiler can guarantee that the pattern matched.

---

# 8. Pattern Matching in `switch`

Pattern matching for `switch` allows type-based cases such as:

```java
switch (obj) {
    case String s -> System.out.println(s);
    case Integer i -> System.out.println(i);
    default -> System.out.println("Other");
}
```

It can also use conditions/guards in the pattern-matching switch syntax discussed in the video.

### Important Java 17 Interview Trap

**Pattern matching for `switch` was a preview feature in Java 17.**

Therefore:

```text
Java 17 → Preview
Java 21 → Final feature
```

Do **not** simply say:

> "Pattern matching switch is a final Java 17 feature."

Better answer:

> "Pattern matching for switch was introduced as a preview feature in Java 17 and became a final feature in Java 21."

---

## `null` in Pattern-Matching Switch

The video also demonstrates:

```java
case null -> ...
```

This allows `null` to be handled explicitly in the switch.

---

# 9. Enhanced Pseudo-Random Number Generators

Java 17 introduced the `RandomGenerator` API.

Before:

```java
Random random = new Random();

int value = random.nextInt(100);
```

Java 17 provides an abstraction:

```java
RandomGenerator generator =
        RandomGenerator.getDefault();

int value = generator.nextInt(100);
```

### Main Benefits

```text
RandomGenerator
      ↓
Common abstraction
      ↓
Multiple random algorithms
      ↓
Easy algorithm selection
```

---

## Select a Specific Algorithm

```java
RandomGenerator generator =
        RandomGenerator.of("L32X64MixRandom");
```

This makes switching between algorithms easier.

---

## SplittableGenerator

Java 17 also provides splittable generators that are useful when generating random values in parallel/concurrent scenarios.

Key idea:

```text
One generator
      ↓
Split into independent generators
      ↓
Parallel/multithreaded work
```

---

# Java 17 — Interview Quick Revision

## Must Know

### 1. Sealed Class

```java
sealed class Payment
    permits UPI, Cash, CreditCard {
}
```

Remember:

```text
final      → stop inheritance
sealed     → controlled inheritance
non-sealed → open inheritance
```

---

### 2. Record

```java
record Person(String name, int age) {
}
```

Remember:

```text
Less boilerplate
Private final components
Canonical constructor
Accessors
equals()
hashCode()
toString()
Cannot extend class
Can implement interface
Can contain methods
Can contain static members
No extra instance fields
```

**Important:** Record immutability is **shallow**, not deep.

---

### 3. `instanceof` Pattern Matching

Old:

```java
if (obj instanceof String) {
    String s = (String) obj;
}
```

Java 17:

```java
if (obj instanceof String s) {
}
```

Remember:

```text
Type check + Cast + Variable declaration
```

---

### 4. Switch Pattern Matching

```java
switch (obj) {
    case String s -> ...
    case Integer i -> ...
}
```

**Java 17 → Preview**

**Java 21 → Final**

---

### 5. RandomGenerator

```java
RandomGenerator generator =
        RandomGenerator.getDefault();
```

Benefits:

```text
Abstraction
Multiple algorithms
Easy algorithm selection
Splittable generators
```

---

# ⭐ Top 15 Interview Questions

1. **What are the major features introduced/relevant in Java 17?**
2. **What is a sealed class?**
3. **Why use sealed classes instead of `final`?**
4. **What are the rules for permitted subclasses of a sealed class?**
5. **Difference between `final`, `sealed`, and `non-sealed`?**
6. **What is a Java record?**
7. **What methods are automatically generated for a record?**
8. **Are records truly immutable?**
9. **Can a record extend another class?**
10. **Can a record implement an interface?**
11. **Can records have constructors and methods?**
12. **What is pattern matching for `instanceof`?**
13. **Why does `&&` work with pattern variables but `||` cause a scope problem?**
14. **Was pattern matching for `switch` final in Java 17?**
15. **What is `RandomGenerator` and why was it introduced?**

---

# 🧠 30-Second Java 17 Revision

```text
JAVA 17
│
├── Sealed Classes
│   ├── sealed
│   ├── permits
│   ├── final
│   ├── sealed
│   └── non-sealed
│
├── Records
│   ├── Less boilerplate
│   ├── private final components
│   ├── constructor
│   ├── accessors
│   ├── equals/hashCode/toString
│   ├── shallow immutability
│   ├── cannot extend class
│   ├── can implement interface
│   └── methods + static members
│
├── Pattern Matching
│   ├── instanceof
│   │   └── type check + cast + variable
│   │
│   └── switch
│       └── Preview in Java 17
│
└── RandomGenerator
    ├── common abstraction
    ├── multiple algorithms
    └── SplittableGenerator
```

## Highest-Priority Interview Traps

> **Sealed ≠ final**
> Sealed allows controlled inheritance; final prevents inheritance.

> **Record ≠ deeply immutable**
> Mutable components can still be modified.

> **Record ≠ normal class**
> It cannot extend another class and cannot be extended.

> **Record can implement interfaces.**

> **Pattern matching for `switch` ≠ final Java 17 feature**
> It was preview in Java 17.

> **`instanceof` pattern matching is different from switch pattern matching.**
