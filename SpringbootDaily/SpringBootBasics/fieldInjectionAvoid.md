यह question सिर्फ Spring का नहीं है, यह **Software Design + Clean Code + Testability** का question है। Interviewer अक्सर देखना चाहता है कि तुम्हें सिर्फ syntax पता है या design reasoning भी समझते हो।

---

# 1. Field Injection (Bad Practice)

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;
}
```

यह code काम करेगा।

लेकिन problem क्या है?

---

## Problem 1: Hidden Dependency

अगर मैं सिर्फ class देखूं:

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;
}
```

मुझे तुरंत नहीं पता चलेगा कि

```text
UserService
   ↓
depends on
   ↓
UserRepository
```

क्योंकि dependency field के अंदर छुपी हुई है।

---

## Constructor Injection

```java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

अब class पढ़ते ही दिख रहा है:

```text
UserService needs UserRepository
```

Dependency explicit है।

---

# Real World Analogy

मान लो तुम Car खरीद रहे हो।

### Field Injection

```text
Car
?
?
?
```

तुम्हें नहीं पता engine चाहिए या नहीं।

---

### Constructor Injection

```text
Car(Engine engine)
```

Car clearly बता रही है:

> "मुझे Engine चाहिए तभी मैं बन सकती हूँ"

---

# Problem 2: Difficult Unit Testing

Suppose:

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User findUser(Long id) {
        return repository.findById(id).orElse(null);
    }
}
```

अब unit test लिखना है।

---

## Constructor Injection

```java
UserRepository mockRepo = mock(UserRepository.class);

UserService service =
        new UserService(mockRepo);
```

बस।

No Spring.

No Container.

Fast Test.

---

## Field Injection

```java
UserService service = new UserService();
```

अब repository null है।

---

तुम्हें reflection use करना पड़ेगा:

```java
ReflectionTestUtils.setField(
    service,
    "repository",
    mockRepo
);
```

या Spring context उठाना पड़ेगा।

---

Interviewer वाली लाइन:

> Constructor injection makes unit testing easy because dependencies can be supplied directly without starting the Spring container.

---

# Problem 3: Mutable Dependency

Field Injection

```java
@Autowired
private UserRepository repository;
```

कोई future developer लिख सकता है:

```java
repository = new FakeRepository();
```

Dependency बदल गई।

---

Constructor Injection

```java
private final UserRepository repository;
```

अब:

```java
repository = new FakeRepository();
```

Compile Error ❌

---

Dependency immutable हो गई।

---

Production systems में immutability बहुत important है।

---

# Problem 4: Object Can Be Created in Invalid State

Field Injection

```java
UserService service =
        new UserService();
```

यह object बन जाएगा।

लेकिन:

```java
repository == null
```

---

फिर:

```java
service.findUser(1L);
```

Boom 💥

```text
NullPointerException
```

---

Constructor Injection

```java
new UserService();
```

Compile Error ❌

---

Compiler बोल देगा:

```text
UserRepository required
```

यानि object invalid state में बन ही नहीं सकता।

---

# Why Spring Team Recommends Constructor Injection

Spring docs भी यही recommend करती हैं क्योंकि:

### Explicit Dependencies

```java
public UserService(UserRepository repository)
```

Dependency clearly visible.

---

### Easier Unit Testing

```java
new UserService(mockRepo)
```

Simple testing.

---

### Immutable Objects

```java
private final UserRepository repository;
```

No accidental changes.

---

### Better Design

Class अपने required dependencies declare करती है।

---

### No Reflection Needed

Field Injection internally reflection use करती है।

Constructor Injection direct object creation है।

---

# Interview Answer (Best)

> Field injection works, but it hides dependencies, makes unit testing difficult, and allows mutable dependencies. Constructor injection makes dependencies explicit, supports immutability using final fields, and allows easy unit testing without loading the Spring container. That's why constructor injection is the recommended approach in production applications and by the Spring team.

---

# One-Liner to Remember

```text
Field Injection
= Hidden + Mutable + Hard to Test

Constructor Injection
= Explicit + Immutable + Easy to Test
```

अगर interviewer पूछे:

**"In your project, which injection style do you use and why?"**

तो confidently बोलना:

> "We prefer constructor injection because dependencies are explicit, fields can be marked final, and unit testing becomes much easier without requiring the Spring container." 🔥

यह answer 3 YOE Java/Spring Boot interviews में बहुत strong माना जाता है।
