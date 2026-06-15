## Aggregation vs Composition (Interview Notes)

### Aggregation (Weak HAS-A Relationship)

**Definition:**

> Parent object uses the child object, but the child can exist independently.

### Real Example

```java
@Service
class UserService {

    @Autowired
    private EmailService emailService;
}
```

```text
UserService ----> EmailService
```

* UserService uses EmailService
* EmailService can exist without UserService
* Lifecycle is independent

### Other Examples

```text
Controller -> Service
Service -> Repository
OrderService -> PaymentService
UserService -> EmailService
```

### Key Point

```text
Aggregation = USES Relationship
Parent does not own child.
Child can exist independently.
```

---

## Composition (Strong HAS-A Relationship)

**Definition:**

> Parent object owns the child object, and the child cannot exist independently.

### Real Example

```java
class Order {

    private List<OrderItem> items;
}
```

```text
Order ----> OrderItem
```

* Order owns OrderItems
* If Order is deleted, OrderItems are deleted
* Lifecycle is dependent

### Other Examples

```text
Order -> OrderItems
Invoice -> InvoiceLines
Blog -> Comments
Car -> Engine
House -> Room
```

### JPA Example

```java
@OneToMany(
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<OrderItem> items;
```

### Key Point

```text
Composition = OWNS Relationship
Parent owns child.
Child cannot exist independently.
```

---

## Quick Comparison

| Feature                     | Aggregation | Composition  |
| --------------------------- | ----------- | ------------ |
| Relationship                | Weak HAS-A  | Strong HAS-A |
| Ownership                   | No          | Yes          |
| Lifecycle                   | Independent | Dependent    |
| Child exists without Parent | Yes         | No           |
| Keyword                     | USES        | OWNS         |

---

## 15-Second Interview Answer

> Aggregation is a weak HAS-A relationship where the parent uses the child object, but the child can exist independently. For example, UserService uses EmailService. Composition is a strong HAS-A relationship where the parent owns the child object, and the child's lifecycle depends on the parent. For example, Order owns OrderItems; deleting the Order also removes its OrderItems.

### Memory Trick

```text
Aggregation = USES

UserService -> EmailService
OrderService -> PaymentService
```

```text
Composition = OWNS

Order -> OrderItems
Invoice -> InvoiceLines
```

**Spring Beans ⇒ Aggregation**
**JPA Parent-Child Entities ⇒ Composition** 🚀
