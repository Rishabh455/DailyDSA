# Why Should Field Injection Be Avoided? (Interview Notes - 3 YOE)

## Direct Interview Answer

> Field injection is generally avoided because it hides dependencies, makes unit testing difficult, does not support immutability, and can allow objects to be created in an invalid state. Constructor injection makes dependencies explicit, easier to test, and allows fields to be declared as final.

---

## Problems with Field Injection

### 1. Hidden Dependencies

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;
}
```

Looking at the class, it is not immediately clear what dependencies are required.

---

### 2. Difficult Unit Testing

```java
UserService service = new UserService();
```

The repository is null unless Spring injects it.

Testing often requires:

* Spring Context
* Reflection utilities

With constructor injection:

```java
UserService service =
    new UserService(mockRepository);
```

Testing becomes simple.

---

### 3. No Immutability

```java
@Autowired
private UserRepository repository;
```

The dependency can be modified accidentally.

Constructor injection allows:

```java
private final UserRepository repository;
```

which makes the dependency immutable.

---

### 4. Invalid Object State

Field injection allows:

```java
UserService service = new UserService();
```

The object is created even though required dependencies are missing.

Constructor injection prevents this by forcing all required dependencies during object creation.

---

## Why Constructor Injection is Preferred

* Dependencies are explicit
* Easier unit testing
* Supports final fields
* Better maintainability
* Recommended by the Spring Team

---

## Interview Answer (15 Seconds)

> We avoid field injection because it hides dependencies, makes unit testing harder, and does not support immutable dependencies. Constructor injection makes dependencies explicit, allows final fields, and makes testing easier, which is why it is the recommended approach in Spring Boot.

---

## Memory Trick

Field Injection:

* Hidden Dependencies
* Hard to Test
* Mutable

Constructor Injection:

* Explicit Dependencies
* Easy to Test
* Immutable
