# Spring Bean Scopes - Interview Notes (3 Years Java Developer)

## What is Bean Scope?

Bean Scope defines:

1. How many instances Spring creates
2. How long those instances live

---

# E-Commerce Website Story

```text
User Login
    ↓
Browse Products
    ↓
Add To Cart
    ↓
Checkout
    ↓
Payment
    ↓
Invoice Generation
```

Use this story to explain all scopes.

---

# 1. Singleton Scope (Default)

## Definition

Only one instance per Spring IoC Container.

```java
@Service
public class ProductService {}

@Repository
public class ProductRepository {}

@Service
public class OrderService {}

@Service
public class PaymentService {}
```

## E-Commerce Example

```text
10,000 users browsing products

           ProductController
                  |
                  V
          ProductService
             (1 Object)
                  |
                  V
         ProductRepository
             (1 Object)
```

All users share the same service and repository objects.

## Why Singleton?

* Less memory usage
* Better performance
* Services are usually stateless
* Default Spring behavior

## Interview Trap

### Is Singleton Bean Thread Safe?

NO.

```text
Singleton = One Object

Thread Safe = Safe for Multiple Threads
```

Both are different concepts.

### Bad Example

```java
@Service
public class CounterService {

    private int count;

    public int increment() {
        return ++count;
    }
}
```

Problem:

```text
Multiple users share same variable
Race Condition
```

### Correct Practice

Singleton beans should be Stateless.

```java
@Service
public class UserService {

    public User getUser(Long id) {
        return repository.findById(id);
    }
}
```

No shared mutable state.

---

# 2. Request Scope

## Definition

One Bean per HTTP Request.

```java
@RequestScope
@Component
public class RequestContext {
}
```

## E-Commerce Example

```text
Request 1

GET /products

RequestContext A
requestId = abc123
```

```text
Request 2

GET /products

RequestContext B
requestId = xyz999
```

Every request gets its own object.

## Real Use Cases

* Request ID
* Correlation ID
* JWT Claims
* Audit Data
* Request Metadata

## Interview Trap

### Can Request Scope Bean be injected into Singleton Bean?

YES.

Spring injects a Proxy Object.

```java
@Autowired
private RequestContext context;
```

Actually:

```text
Singleton Bean
      |
      V
   Proxy
      |
      V
Current Request Bean
```

---

# 3. Session Scope

## Definition

One Bean per User Session.

```java
@SessionScope
@Component
public class UserSession {
}
```

## E-Commerce Example

User A:

```text
Session A

cart = [Laptop]
user = Rishabh
```

User B:

```text
Session B

cart = [Phone]
user = Rahul
```

Both users have different session beans.

## Real Use Cases

* Shopping Cart
* Logged-in User
* Language Preference
* Theme Preference

## Request vs Session

```text
Request Scope
-------------
Lives for one request

Session Scope
-------------
Lives until logout/session timeout
```

---

# 4. Prototype Scope

## Definition

New object every time Spring is asked for the bean.

```java
@Component
@Scope("prototype")
public class InvoiceGenerator {
}
```

## E-Commerce Example

```text
Order 1

InvoiceGenerator A
```

```text
Order 2

InvoiceGenerator B
```

```text
Order 3

InvoiceGenerator C
```

New object every time.

## Real Use Cases

* Invoice Generator
* PDF Generator
* Excel Export
* Report Generator
* File Processor

---

# MOST IMPORTANT INTERVIEW TRAP

## Prototype Bean inside Singleton Bean

```java
@Service
public class OrderService {

    @Autowired
    private InvoiceGenerator generator;
}
```

Question:

How many InvoiceGenerator objects?

Most candidates answer:

```text
New object every call
```

Wrong.

Correct:

```text
Only One Object
```

Because dependency injection happens only once during startup.

---

## Correct Solution

### ObjectFactory

```java
@Autowired
private ObjectFactory<InvoiceGenerator> factory;

public void generate() {
    InvoiceGenerator generator =
        factory.getObject();
}
```

### Provider

```java
@Autowired
private Provider<InvoiceGenerator> provider;

public void generate() {
    InvoiceGenerator generator =
        provider.get();
}
```

Now a new object is created every time.

---

# Prototype Lifecycle Trap

Question:

Does Spring manage Prototype Bean destruction?

Answer:

```text
NO

Spring creates it.
Developer destroys it.
```

Singleton:

```text
Create + Destroy
```

Prototype:

```text
Create Only
```

---

# Scope Comparison

| Scope     | Objects Created          |
| --------- | ------------------------ |
| Singleton | One per Spring Container |
| Prototype | New bean every getBean() |
| Request   | One per HTTP Request     |
| Session   | One per User Session     |

---

# Real-World Usage

```text
Singleton  -> 90-95%

Services
Repositories
Controllers
Configurations
Utility Classes
```

```text
Request Scope -> Rare

Request Metadata
Request Tracking
JWT Information
```

```text
Session Scope -> Less Common

Shopping Cart
Logged-in User
Preferences
```

```text
Prototype Scope -> Rare

PDF Generator
Invoice Generator
Report Generator
File Processing
```

---

# 30-Second Interview Answer

"In a typical e-commerce application, ProductService, OrderService, PaymentService, repositories, and controllers are Singleton because they are stateless and shared across all users. Request Scope is used for request-specific data like request IDs and JWT information. Session Scope is used for shopping carts and logged-in user data because it persists across multiple requests from the same user. Prototype Scope is used when a completely new object is required every time, such as invoice generation or report generation. One common interview trap is injecting a Prototype bean into a Singleton bean; in that case only one prototype instance is injected unless ObjectFactory or Provider is used."
