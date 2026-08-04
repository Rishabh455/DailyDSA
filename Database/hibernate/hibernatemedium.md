# Hibernate Interview Questions (Medium Level)

> **Target Experience:** 3–4 Years Java Backend Developer
>
> These are the **highest probability Hibernate interview questions** based on my resume (Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, MySQL, Enterprise Banking Application).

---

# Q1. Explain the Hibernate Architecture.

## Answer

Hibernate follows a layered architecture that acts as a bridge between Java applications and relational databases.

```
Java Application
       │
       ▼
Spring Data JPA
       │
       ▼
JPA (Specification)
       │
       ▼
Hibernate
       │
       ▼
JDBC
       │
       ▼
Database
```

### Core Components

- Configuration
- SessionFactory
- Session
- Transaction
- Query
- Entity
- Database

### Interview Tip

In Spring Boot, we usually don't create `SessionFactory` or `Session` manually because Spring Boot configures them automatically.

---

# Q2. Explain the lifecycle of an Entity in Hibernate.

## Answer

An Entity goes through four states during its lifecycle.

### 1. Transient

Object exists only in Java memory.

Not associated with Hibernate.

```java
Employee emp = new Employee();
emp.setName("Rishabh");
```

No SQL is generated.

---

### 2. Persistent

Entity is attached to Hibernate Session.

```java
entityManager.persist(emp);
```

Hibernate tracks all changes.

---

### 3. Detached

Session is closed.

Entity still exists but is no longer managed.

```java
entityManager.close();
```

Any modifications are NOT saved automatically.

---

### 4. Removed

Entity is marked for deletion.

```java
entityManager.remove(emp);
```

Hibernate deletes it during transaction commit.

---

### Interview Diagram

```
new Employee()

↓

Transient

↓

persist()

↓

Persistent

↓

close()

↓

Detached

↓

remove()

↓

Removed
```

---

# Q3. What is Persistence Context?

## Answer

Persistence Context is a memory area where Hibernate stores all managed entities.

Whenever we fetch an entity, Hibernate first checks the Persistence Context.

If found, it returns the object from memory.

Otherwise, it queries the database.

### Benefits

- Avoids unnecessary database calls
- Enables Dirty Checking
- Improves performance
- Maintains object identity

---

# Q4. What is Dirty Checking?

## Answer

Dirty Checking is Hibernate's automatic mechanism for detecting changes made to managed entities.

If an entity is modified inside a transaction, Hibernate automatically generates an UPDATE query during commit.

Example

```java
Employee emp = entityManager.find(Employee.class,1);

emp.setSalary(80000);
```

No save() is called.

During commit:

```sql
UPDATE employee
SET salary = 80000
WHERE id = 1;
```

is automatically executed.

---

### Interview Follow-up

**Why is Dirty Checking useful?**

Because developers don't need to manually call update() every time.

Hibernate automatically synchronizes modified objects with the database.

---

# Q5. What is First Level Cache?

## Answer

First Level Cache is the cache associated with a Hibernate Session.

It is enabled by default.

Whenever an entity is fetched for the first time,

Hibernate stores it inside the Session.

Subsequent requests for the same entity are served from memory instead of hitting the database.

Example

```java
Employee e1 = entityManager.find(Employee.class,1);

Employee e2 = entityManager.find(Employee.class,1);
```

Only one SQL query is executed.

---

### Advantages

- Improves performance
- Reduces database load
- Automatic
- Session scoped

---

# Q6. What is the difference between First Level Cache and Second Level Cache?

## Answer

| First Level Cache | Second Level Cache |
|-------------------|--------------------|
| Session Scoped | SessionFactory Scoped |
| Enabled by Default | Disabled by Default |
| One Session | Shared Across Sessions |
| No Configuration Needed | Requires Configuration |
| Faster | Useful for Frequently Read Data |

Examples of Second Level Cache Providers

- EhCache
- Hazelcast
- Infinispan

---

# Q7. What is the difference between persist() and merge()?

## Answer

### persist()

Used for new entities.

Makes the entity persistent.

```java
entityManager.persist(emp);
```

---

### merge()

Used for detached entities.

Copies detached object data into a managed entity.

```java
Employee managed = entityManager.merge(emp);
```

---

### Comparison

| persist() | merge() |
|------------|----------|
| New Entity | Detached Entity |
| No Return Value | Returns Managed Entity |
| Insert | Update / Insert |
| Managed Object | Creates Managed Copy |

---

# Q8. Difference between save() and persist()?

## Answer

`save()` is a Hibernate-specific method.

`persist()` is defined by JPA.

### save()

Returns generated ID.

```java
Long id = session.save(emp);
```

### persist()

Returns void.

```java
entityManager.persist(emp);
```

### Which should we use?

In Spring Boot applications,

Prefer `persist()` because it follows the JPA standard.

---

# Q9. Difference between get() and load()?

## Answer

### get()

Immediately fetches data from the database.

Returns null if data is not found.

```java
Employee emp = session.get(Employee.class,1);
```

---

### load()

Returns a proxy object.

Database is queried only when required.

Throws exception if object doesn't exist.

```java
Employee emp = session.load(Employee.class,1);
```

---

### Comparison

| get() | load() |
|--------|---------|
| Immediate Fetch | Lazy Fetch |
| Returns null | Exception if Missing |
| No Proxy | Proxy Object |

---

# Q10. What is Lazy Loading?

## Answer

Lazy Loading means related entities are loaded only when they are actually needed.

Example

```java
Employee

↓

Department
```

Department won't be fetched until

```java
employee.getDepartment();
```

is called.

### Benefits

- Faster Queries
- Less Memory Usage
- Better Performance

---

# Q11. What is Eager Loading?

## Answer

Eager Loading loads all related entities immediately.

Example

Employee and Department are loaded together.

Useful when related data is always required.

---

# Q12. Difference between Lazy and Eager Loading?

| Lazy | Eager |
|------|--------|
| Loads on Demand | Loads Immediately |
| Better Performance | Can Load Unnecessary Data |
| Default for Collections | Default for ManyToOne |

---

# Q13. What is the N+1 Query Problem?

## Answer

Suppose we fetch 100 Employees.

Each Employee has one Department.

Hibernate executes

```
1 Query

+

100 Queries
```

Total

```
101 Queries
```

instead of one optimized query.

This is called the N+1 Query Problem.

---

# Q14. How do you solve the N+1 Query Problem?

## Answer

Use

- JOIN FETCH
- EntityGraph
- Batch Fetching
- DTO Projection

Example

```java
@Query("""
SELECT e
FROM Employee e
JOIN FETCH e.department
""")
```

Now only one query is executed.

---

# Q15. What is Cascade?

## Answer

Cascade automatically propagates operations from parent to child entities.

Example

Employee

↓

Address

Saving Employee automatically saves Address.

```java
@OneToOne(cascade = CascadeType.ALL)
```

---

# Q16. What are different Cascade Types?

## Answer

- PERSIST
- MERGE
- REMOVE
- REFRESH
- DETACH
- ALL

---

# Q17. What is FetchType?

## Answer

FetchType decides when related entities should be loaded.

Available Types

- LAZY
- EAGER

Example

```java
@OneToMany(fetch = FetchType.LAZY)
```

---

# Q18. Explain JPQL.

## Answer

JPQL stands for Java Persistence Query Language.

Instead of table names,

JPQL uses Entity names.

SQL

```sql
SELECT * FROM employee;
```

JPQL

```java
SELECT e FROM Employee e
```

Hibernate converts JPQL into SQL.

---

# Q19. JPQL vs Native SQL

| JPQL | Native SQL |
|------|------------|
| Entity Based | Table Based |
| Database Independent | Database Specific |
| Portable | Vendor Specific |
| Automatically Converted | Executes Directly |

---

# Q20. How does Spring Data JPA use Hibernate internally?

## Answer

Suppose we write

```java
employeeRepository.save(employee);
```

Internally,

```
JpaRepository

↓

EntityManager

↓

Hibernate Session

↓

Hibernate Generates SQL

↓

JDBC

↓

Database
```

The developer never writes SQL manually.

Hibernate handles

- SQL generation
- Dirty Checking
- Transactions
- Caching
- Object Mapping

---

# Q21. What happens internally when repository.save() is called?

## Answer

Suppose

```java
employeeRepository.save(emp);
```

Internally

```
Repository

↓

EntityManager.persist()

↓

Hibernate Session

↓

Persistence Context

↓

SQL Generation

↓

JDBC

↓

Database
```

If the entity already exists,

Hibernate generates an UPDATE.

Otherwise,

it generates an INSERT.

---

# Q22. Which Hibernate features have you used in your project?

## Sample Interview Answer

In my banking application, I primarily worked with Spring Data JPA backed by Hibernate. Hibernate was responsible for:

- Mapping Java entities to PostgreSQL/MySQL tables using JPA annotations.
- Automatically generating SQL for CRUD operations.
- Managing entity lifecycle and the Persistence Context.
- Using Dirty Checking to synchronize entity changes without explicit update calls.
- Leveraging the First-Level Cache to reduce redundant database queries within a transaction.
- Executing JPQL and repository methods for data retrieval.
- Managing transactions through Spring's `@Transactional` support.

Although I didn't directly use the `Session` API, Hibernate handled it internally through `EntityManager` and Spring Data JPA.

---

# Quick Revision

### Entity States

```
Transient

↓

Persistent

↓

Detached

↓

Removed
```

---

### Caches

- First Level Cache → Session
- Second Level Cache → SessionFactory

---

### Loading

- Lazy → On Demand
- Eager → Immediate

---

### Entity Methods

- persist()
- merge()
- remove()

---

### Query Types

- JPQL
- Native SQL

---

### High Probability Topics

- Entity Lifecycle ⭐⭐⭐⭐⭐
- Dirty Checking ⭐⭐⭐⭐⭐
- Persistence Context ⭐⭐⭐⭐⭐
- First Level Cache ⭐⭐⭐⭐⭐
- persist() vs merge() ⭐⭐⭐⭐⭐
- save() vs persist() ⭐⭐⭐⭐☆
- get() vs load() ⭐⭐⭐⭐☆
- Lazy vs Eager ⭐⭐⭐⭐⭐
- N+1 Query Problem ⭐⭐⭐⭐⭐
- Cascade Types ⭐⭐⭐⭐☆
- JPQL vs SQL ⭐⭐⭐⭐☆
- repository.save() Internal Flow ⭐⭐⭐⭐⭐