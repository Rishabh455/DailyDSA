# Optional - Easy Interview Notes

## Optional Kya Hai?
Optional is a container object introduced in Java 8 to safely handle null values and avoid NullPointerException.
Optional ek box hai.

Ye box:

* Value rakh sakta hai
* Ya khali ho sakta hai

Example:

```java
Optional<String> name =
        Optional.of("Java");
```

Box:

```text
[ Java ]
```

Example:

```java
Optional<String> name =
        Optional.empty();
```

Box:

```text
[ Empty ]
```

---

## Optional Ki Zarurat Kyu Padi?

Pehle:

```java
String name = null;

System.out.println(name.length());
```

Output:

```text
NullPointerException
```

Application crash.

Java 8 ne Optional diya taaki null ko safely handle kar sake.

---

## Optional Banane Ke 3 Tarike

### 1. of()

Jab value pakka present ho.

```java
Optional<String> name =
        Optional.of("Java");
```

⚠️

```java
Optional.of(null);
```

Exception dega.

---

### 2. ofNullable()

Most Common.

```java
Optional<String> name =
        Optional.ofNullable("Java");
```

Ya

```java
Optional<String> name =
        Optional.ofNullable(null);
```

Dono safe hain.

---

### 3. empty()

Khali box.

```java
Optional<String> name =
        Optional.empty();
```

---

## Important Methods

### isPresent()

Check karta hai value hai ya nahi.

```java
Optional<String> name =
        Optional.of("Java");

System.out.println(
        name.isPresent()
);
```

Output:

```text
true
```

---

### get()

Value nikalta hai.

```java
Optional<String> name =
        Optional.of("Java");

System.out.println(
        name.get()
);
```

Output:

```text
Java
```

⚠️

```java
Optional.empty().get();
```

Exception dega.

Interview mein bolna:

```text
Direct get() avoid karna chahiye.
```

---

### orElse()

Value nahi hai to default de do.

```java
Optional<String> name =
        Optional.empty();

String result =
        name.orElse("Default");

System.out.println(result);
```

Output:

```text
Default
```

---

### orElseThrow()

Value nahi hai to exception phek do.

```java
String name =
        optional.orElseThrow(
                () -> new RuntimeException(
                        "User Not Found"
                )
        );
```

---

### ifPresent()

Value hai to code chalao.

```java
Optional<String> name =
        Optional.of("Java");

name.ifPresent(
        System.out::println
);
```

Output:

```text
Java
```

---

# Spring Boot Interview

Most Common Example

```java
Optional<User> user =
        userRepository.findById(id);
```

Safe Handling:

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

# Most Asked Interview Questions

Q. Why Optional?

Answer:

```text
To avoid NullPointerException.
```

---

Q. Difference between of() and ofNullable()?

Answer:

```text
of() -> null allowed nahi

ofNullable() -> null allowed
```

---

Q. Which method gives default value?

Answer:

```java
orElse()
```

---

Q. Which method throws exception?

Answer:

```java
orElseThrow()
```

---

Q. Which method checks value exists?

Answer:

```java
isPresent()
```

---

# One Line Revision

Optional = Safe Box For Null Handling

```
```
Optional.of()         -> Value pakka hai

Optional.ofNullable() -> Value ho bhi sakti hai, nahi bhi

isPresent()           -> Value hai?

get()                 -> Value nikaalo

orElse()              -> Default value do

orElseThrow()         -> Exception phek do

ifPresent()           -> Value hai to kaam karo


------------