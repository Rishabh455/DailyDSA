For interviews (especially your Java/Spring Boot experience level), focus on **3 things for each SOLID principle**:

1. Definition (1 line)
2. Bad Example
3. Real-world Spring Boot Example

---

# S - Single Responsibility Principle (SRP)

### Definition

A class should have only **one reason to change**.

### Bad Example

```java
class UserService {

    public void saveUser() {
    }

    public void sendEmail() {
    }

    public void generateReport() {
    }
}
```

Problems:

```text
User logic changes
Email logic changes
Report logic changes

=> 3 reasons to change
```

### Good Example

```java
class UserService {
    public void saveUser() {}
}

class EmailService {
    public void sendEmail() {}
}

class ReportService {
    public void generateReport() {}
}
```

### Spring Boot Example

```java
@Service
class UserService {}

@Service
class NotificationService {}

@Repository
class UserRepository {}
```

Each layer has a single responsibility.

### Interview One-Liner

> A class should have only one responsibility and therefore only one reason to change.

---

# O - Open Closed Principle (OCP)

### Definition

Software entities should be:

```text
Open for Extension
Closed for Modification
```

### Bad Example

```java
class PaymentService {

    public void pay(String type) {

        if(type.equals("UPI")) {
        }
        else if(type.equals("CARD")) {
        }
    }
}
```

Every new payment method requires modifying existing code.

---

### Good Example

```java
interface PaymentStrategy {
    void pay();
}

class UpiPayment implements PaymentStrategy {
    public void pay() {}
}

class CardPayment implements PaymentStrategy {
    public void pay() {}
}
```

Usage:

```java
paymentStrategy.pay();
```

Add new payment?

```java
class NetBankingPayment implements PaymentStrategy
```

No modification required.

### Spring Boot Example

Strategy Pattern + Dependency Injection

```java
@Autowired
private PaymentStrategy paymentStrategy;
```

### Interview One-Liner

> We should be able to add new functionality without modifying existing tested code.

---

# L - Liskov Substitution Principle (LSP)

### Definition

Child class should be replaceable by parent class without breaking behavior.

---

### Bad Example

```java
class Bird {
    void fly(){}
}

class Penguin extends Bird {

    void fly() {
        throw new RuntimeException();
    }
}
```

Problem:

```java
Bird bird = new Penguin();
bird.fly();
```

Boom 💥

LSP violated.

---

### Good Example

```java
interface Bird {}

interface FlyingBird {
    void fly();
}

class Sparrow implements Bird, FlyingBird {}

class Penguin implements Bird {}
```

### Interview One-Liner

> A subclass should be substitutable for its parent without changing the correctness of the program.

---

# I - Interface Segregation Principle (ISP)

### Definition

Clients should not be forced to implement methods they don't use.

---

### Bad Example

```java
interface Worker {

    void work();

    void eat();

    void sleep();
}
```

Robot:

```java
class Robot implements Worker {

    public void work(){}

    public void eat(){
        throw new UnsupportedOperationException();
    }

    public void sleep(){
        throw new UnsupportedOperationException();
    }
}
```

Bad design.

---

### Good Example

```java
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

interface Sleepable {
    void sleep();
}
```

Robot:

```java
class Robot implements Workable {}
```

Human:

```java
class Human implements Workable,
                       Eatable,
                       Sleepable {}
```

### Spring Boot Example

Instead of:

```java
interface UserService {
   save();
   delete();
   uploadFile();
   sendEmail();
}
```

Create smaller focused interfaces.

### Interview One-Liner

> Prefer many small interfaces over one large interface.

---

# D - Dependency Inversion Principle (DIP)

### Definition

High-level modules should not depend on low-level modules.

Both should depend on abstractions.

---

### Bad Example

```java
class UserService {

    private MySQLDatabase db =
            new MySQLDatabase();
}
```

Problem:

Switch to MongoDB?

Need code changes.

---

### Good Example

```java
interface Database {
    void save();
}
```

Implementations:

```java
class MySQLDatabase
        implements Database {}

class MongoDatabase
        implements Database {}
```

Service:

```java
class UserService {

    private Database database;

    UserService(Database database) {
        this.database = database;
    }
}
```

Now:

```java
new UserService(
    new MongoDatabase()
);
```

works without modification.

---

# Spring Boot's Biggest SOLID Principle

Dependency Injection is the best example of DIP.

```java
@Service
class UserService {

    private final UserRepository repo;

    public UserService(
        UserRepository repo) {

        this.repo = repo;
    }
}
```

UserService depends on abstraction managed by Spring, not concrete object creation.

---

# Restaurant Story (Easy to Remember)

### SRP

Waiter takes orders.

Chef cooks.

Cashier handles payments.

Everyone has one responsibility.

---

### OCP

New payment method added:

```text
UPI
Card
Wallet
```

Add new counter.

Don't change existing counters.

---

### LSP

Any waiter can replace another waiter and still serve customers correctly.

---

### ISP

Chef shouldn't be forced to do cashier work.

Waiter shouldn't be forced to cook.

---

### DIP

Restaurant manager talks to "Staff" abstraction.

Not directly to:

```text
Waiter A
Waiter B
Waiter C
```

Anyone implementing Staff can work.

---

# Interview Favorite Question

**Which SOLID principle is most visible in Spring Boot?**

Answer:

```text
Dependency Inversion Principle (DIP)

Implemented through Dependency Injection (DI).

Classes depend on abstractions (interfaces)
instead of concrete implementations, and Spring
injects the dependencies at runtime.
```

Memorize these examples; they cover about 90% of SOLID-related Java/Spring Boot interview questions.
