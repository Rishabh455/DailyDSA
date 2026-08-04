# Hibernate Interview Questions (Basic Level)

These are the most likely basic Hibernate interview questions based on your resume (Spring Boot, Hibernate, JPA, Spring Data JPA).

---

# Q1. What is Hibernate?

## Answer

Hibernate is an **Object Relational Mapping (ORM)** framework for Java that simplifies database operations by mapping Java objects to database tables.

Instead of writing SQL queries manually using JDBC, Hibernate allows us to perform CRUD operations using Java objects.

Hibernate internally generates SQL queries and communicates with the database.

### Example

Instead of writing:

```sql
INSERT INTO employee(id, name)
VALUES (1, 'Rishabh');
```

We simply write:

```java
Employee emp = new Employee();
emp.setId(1);
emp.setName("Rishabh");

entityManager.persist(emp);
```

Hibernate automatically generates the SQL query.

### Follow-up: Why do companies use Hibernate?

Because it:

- Reduces boilerplate JDBC code
- Automatically maps Java objects to tables
- Generates SQL automatically
- Provides caching
- Manages transactions
- Supports lazy loading
- Is database independent
- Integrates seamlessly with Spring Boot

---

# Q2. What is ORM?

## Answer

ORM stands for **Object Relational Mapping**.

It is a technique used to map Java classes to database tables.

Example:

Java Class

```java
Employee
```

↓

Database Table

```text
EMPLOYEE
```

Every Java object becomes one row in the database.

Java class:

```java
class Employee {
    Long id;
    String name;
    double salary;
}
```

Database Table:

| id | name | salary |
|----|------|---------|
| 1 | Rishabh | 50000 |

Hibernate performs this mapping automatically.

---

# Q3. Why Hibernate when JDBC already exists?

## Answer

JDBC is a low-level API.

Hibernate is a high-level ORM framework.

With JDBC, developers need to:

- Write SQL manually
- Manage database connections
- Handle ResultSet
- Convert rows into Java objects manually
- Close resources explicitly

Hibernate performs all these tasks automatically.

### JDBC vs Hibernate

| JDBC | Hibernate |
|--------|-----------|
| Manual SQL | Automatic SQL Generation |
| Manual Object Mapping | Automatic Mapping |
| More Boilerplate Code | Less Code |
| Database Specific | Database Independent |
| No Built-in Caching | Supports Caching |

---

# Q4. What is JPA?

## Answer

JPA stands for **Java Persistence API**.

It is **not a framework**.

It is a **Java specification** that defines standard rules for persisting Java objects into relational databases.

Hibernate is one of the implementations of JPA.

### Easy Explanation

Think of it like this:

```
Interface
     ↓
Implementation
```

Similarly,

```
JPA
     ↓
Hibernate
```

JPA defines the contract.

Hibernate implements that contract.

---

# Q5. Difference between Hibernate and JPA?

## Answer

JPA is a specification.

Hibernate is an ORM framework that implements the JPA specification.

Hibernate also provides additional features beyond JPA.

### Comparison

| JPA | Hibernate |
|------|-----------|
| Specification | Implementation |
| Defines Rules | Implements Rules |
| Standard API | ORM Framework |
| Provider Independent | Hibernate Specific |

### Follow-up

**Can we use JPA without Hibernate?**

Yes.

Other JPA implementations include:

- EclipseLink
- OpenJPA

---

# Q6. What is an Entity?

## Answer

An Entity is a Java class that represents a table in the database.

Each object of the entity represents one row.

It is marked using the `@Entity` annotation.

Example:

```java
@Entity
public class Employee {

    @Id
    private Long id;

    private String name;
}
```

Database:

| id | name |
|----|------|
| 1 | Rishabh |

---

# Q7. Why do we use @Entity?

## Answer

`@Entity` tells Hibernate that this Java class should be mapped to a database table.

Without `@Entity`, Hibernate ignores the class.

---

# Q8. What is @Table?

## Answer

`@Table` specifies the database table name.

Example:

```java
@Entity
@Table(name = "EMPLOYEE")
public class Employee {

}
```

If `@Table` is not specified, Hibernate uses the class name as the table name by default.

---

# Q9. What is @Id?

## Answer

`@Id` identifies the primary key of an entity.

Every entity must have one unique identifier.

Example:

```java
@Id
private Long id;
```

---

# Q10. What is @GeneratedValue?

## Answer

`@GeneratedValue` tells Hibernate how the primary key should be generated automatically.

Example:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Saving an entity:

```java
Employee employee = new Employee();
employee.setName("Rishabh");

repository.save(employee);
```

Hibernate automatically generates the ID.

---

# Q11. What are the different GenerationType strategies?

## Answer

### 1. IDENTITY

- Uses database auto-increment.
- Commonly used with MySQL.

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

---

### 2. SEQUENCE

- Uses a database sequence.
- Commonly used with Oracle and PostgreSQL.

```java
@GeneratedValue(strategy = GenerationType.SEQUENCE)
```

---

### 3. TABLE

- Uses a separate table to maintain ID values.
- Less commonly used.

```java
@GeneratedValue(strategy = GenerationType.TABLE)
```

---

### 4. AUTO

- Hibernate automatically selects the most appropriate strategy based on the database.

```java
@GeneratedValue(strategy = GenerationType.AUTO)
```

---

# Q12. What is Hibernate Session?

## Answer

A **Session** is the primary interface between a Java application and Hibernate.

It is responsible for communicating with the database.

Using a Session, we can:

- Save entities
- Update entities
- Delete entities
- Fetch entities

In Spring Boot applications, developers usually work with repositories or `EntityManager`, while Hibernate manages the Session internally.

---

# Q13. What is the difference between Session and EntityManager?

## Answer

`EntityManager` is the standard interface defined by JPA.

`Session` is Hibernate's native interface.

Hibernate internally implements `EntityManager` using `Session`.

In Spring Boot applications, developers generally use `EntityManager` because it keeps the code independent of the ORM implementation.

### Comparison

| Session | EntityManager |
|-----------|---------------|
| Hibernate Specific | JPA Standard |
| Native Hibernate API | Standard Persistence API |
| More Hibernate Features | Portable Across JPA Providers |

---

# High Probability Interview Question

## If Spring Data JPA is already there, where does Hibernate come into the picture?

### Answer

Spring Data JPA is a higher-level abstraction that provides ready-made repository interfaces such as `JpaRepository`.

JPA defines the standard APIs for persistence.

Hibernate is the implementation of JPA that actually performs the work.

Hibernate is responsible for:

- Generating SQL queries
- Managing entities
- Maintaining the persistence context
- Dirty checking
- Caching
- Database communication

### Complete Flow

```text
Application Code
        │
        ▼
Spring Data JPA (JpaRepository)
        │
        ▼
JPA (Specification)
        │
        ▼
Hibernate (Implementation)
        │
        ▼
Database
```

---

# Revision Summary

## Hibernate

- ORM Framework
- Generates SQL automatically
- Maps Java Objects to Database Tables

## JPA

- Java Persistence API
- Specification
- Defines Persistence Rules

## Hibernate

- Implements JPA
- Actual ORM Provider

## Important Annotations

- `@Entity`
- `@Table`
- `@Id`
- `@GeneratedValue`

## Generation Types

- IDENTITY
- SEQUENCE
- TABLE
- AUTO

## Session

- Hibernate Interface
- Performs CRUD Operations

## EntityManager

- JPA Interface
- Uses Hibernate Session internally

## Interview Tip

Always remember the relationship:

```text
Spring Boot
      ↓
Spring Data JPA
      ↓
JPA
      ↓
Hibernate
      ↓
Database
```