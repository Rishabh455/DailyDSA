# Java Reflection - 3 YOE Interview Quick Notes

## What is Reflection?

> Reflection is a Java feature that allows us to inspect and manipulate classes, methods, fields, and constructors at runtime without knowing them at compile time.

---

## Simple Example

Normally:

```java
User user = new User();
user.getName();
```

Compile time pe sab pata hai.

Reflection:

```java
Class<?> clazz = Class.forName("com.example.User");
```

Runtime pe class load kar rahe hain.

---

## What Can Reflection Do?

### 1. Get Class Information

```java
Class<?> clazz = User.class;
```

---

### 2. Get Methods

```java
Method[] methods = clazz.getDeclaredMethods();
```

---

### 3. Get Fields

```java
Field[] fields = clazz.getDeclaredFields();
```

---

### 4. Create Objects Dynamically

```java
User user = clazz.getDeclaredConstructor()
                 .newInstance();
```

---

### 5. Invoke Methods Dynamically

```java
Method method = clazz.getMethod("getName");
method.invoke(user);
```

---

# Real-World Usage in Spring

### Dependency Injection

When Spring sees:

```java
@Autowired
private UserRepository repository;
```

Spring uses Reflection internally to inject the dependency.

---

### Creating Beans

```java
@Service
public class UserService {}
```

Spring discovers and creates beans using Reflection.

---

### Reading Annotations

```java
@RestController
@Service
@Repository
```

Spring uses Reflection to scan these annotations.

---

# Why Reflection is Powerful?

Because framework doesn't know your classes beforehand.

Spring dynamically finds:

```java
Controllers
Services
Repositories
Configurations
```

and manages them automatically.

---

# Drawbacks

### Slower than Normal Calls

Reflection bypasses some JVM optimizations.

---

### Breaks Encapsulation

Can access private fields/methods.

```java
field.setAccessible(true);
```

---

### Less Type Safety

Errors found at runtime instead of compile time.

---

# Common Interview Question

### Does Spring Use Reflection?

> Yes. Spring heavily uses Reflection for component scanning, dependency injection, bean creation, annotation processing, AOP, and proxy generation.

---

# 30-Second Interview Answer

> Reflection is a Java feature that allows a program to inspect and manipulate classes, methods, fields, and constructors at runtime. In Spring Boot, Reflection is used for component scanning, dependency injection, bean creation, and reading annotations like `@Service`, `@Repository`, and `@Autowired`. While Reflection provides flexibility, it is slower than direct method calls and reduces compile-time type safety.

---

# One-Line Memory Trick

> **"Reflection = Inspect and manipulate classes at runtime."**

---

# If Interviewer Asks: "Where have you seen Reflection in Spring?"

Answer:

> "Spring uses Reflection internally for component scanning, bean creation, dependency injection, annotation processing, and AOP proxy creation." 🔥

For a 3 YOE Java developer, this level of understanding is usually sufficient.

--------------------------
# Java Reflection - Interview Notes (3 YOE)

## What is Reflection?

> Reflection is a Java feature that allows inspection and manipulation of classes, methods, fields, and constructors at runtime.

---

## What Can Reflection Do?

* Get class metadata
* Access methods and fields
* Create objects dynamically
* Invoke methods dynamically
* Read annotations at runtime

---

## Where is Reflection Used in Spring?

### Dependency Injection

```java
@Autowired
private UserRepository repository;
```

Spring uses Reflection to inject dependencies.

### Component Scanning

```java
@Service
@Repository
@RestController
```

Spring scans these annotations using Reflection.

### Bean Creation

Spring creates and manages beans using Reflection.

### AOP & Proxies

Spring uses Reflection internally for proxy creation and method interception.

---

## Why is Reflection Useful?

Frameworks like Spring do not know application classes beforehand, so Reflection helps discover and manage them dynamically at runtime.

---

## Drawbacks

* Slower than direct method calls
* Less compile-time type safety
* Can break encapsulation by accessing private members

---

## Does Spring Use Reflection?

> Yes. Spring uses Reflection for component scanning, bean creation, dependency injection, annotation processing, and AOP proxy creation.

---

## Interview Answer (30 Seconds)

> Reflection is a Java feature that allows inspection and manipulation of classes, methods, fields, and constructors at runtime. In Spring Boot, it is used for component scanning, dependency injection, bean creation, annotation processing, and AOP. It provides flexibility to frameworks, although it is slightly slower than direct method calls.

---

## One-Line Memory Trick

> Reflection = Inspect and manipulate classes at runtime.

---

## Where Have You Used Reflection in Spring?

> Spring uses Reflection internally for component scanning, dependency injection, bean creation, annotation processing, and AOP proxy creation.

