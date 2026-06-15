# Serialization & Deserialization in Java (Interview Notes)

## What is Serialization?

> **Serialization** is the process of converting a Java object into a byte stream so that it can be stored in a file, sent over a network, or cached.

```text
Java Object
     |
Serialization
     |
Byte Stream
```

### Why do we need it?

* Store object in a file
* Send object over network
* Distributed systems
* Caching (Redis, Hazelcast)
* Messaging systems (Kafka, RabbitMQ)

---

## What is Deserialization?

> **Deserialization** is the reverse process of converting a byte stream back into a Java object.

```text
Byte Stream
     |
Deserialization
     |
Java Object
```

---

## Example

### Step 1: Serializable Class

```java
import java.io.Serializable;

class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    String name;
    int age;

    Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

---

### Step 2: Serialization

```java
ObjectOutputStream oos =
    new ObjectOutputStream(
        new FileOutputStream("emp.ser"));

Employee emp = new Employee("Rishabh", 25);

oos.writeObject(emp);
```

### Flow

```text
Employee Object
      |
writeObject()
      |
emp.ser file
```

---

### Step 3: Deserialization

```java
ObjectInputStream ois =
    new ObjectInputStream(
        new FileInputStream("emp.ser"));

Employee emp =
    (Employee) ois.readObject();
```

### Flow

```text
emp.ser
    |
readObject()
    |
Employee Object
```

---

# Serializable Interface

```java
class Employee implements Serializable
```

`Serializable` is a **Marker Interface**.

### Marker Interface

```text
No methods
No fields
Just gives metadata to JVM
```

Examples:

```text
Serializable
Cloneable
Remote
```

---

# serialVersionUID

```java
private static final long serialVersionUID = 1L;
```

### Purpose

Version control for serialized objects.

Suppose:

Version 1

```java
class Employee {
    String name;
}
```

Object serialized.

Later:

Version 2

```java
class Employee {
    String name;
    int age;
}
```

Now deserialization may fail because class structure changed.

### Exception

```text
InvalidClassException
```

To avoid unexpected version mismatch issues:

```java
private static final long serialVersionUID = 1L;
```

---

# transient Keyword

Suppose password should not be serialized.

```java
class User implements Serializable {

    String username;

    transient String password;
}
```

### Before Serialization

```text
username = admin
password = 123
```

### After Deserialization

```text
username = admin
password = null
```

`transient` fields are skipped during serialization.

---

# static Variables

```java
static int count;
```

Static fields belong to class, not object.

Therefore:

```text
Not serialized
```

---

# Most Asked Interview Question

### Can Constructor Execute During Deserialization?

```text
Serializable Class -> NO
Non-Serializable Parent -> YES
```

Example:

```java
class Parent {
    Parent() {
        System.out.println("Parent Constructor");
    }
}

class Child extends Parent
        implements Serializable {
}
```

During deserialization:

```text
Parent Constructor executes
Child Constructor does NOT execute
```

---

# Real Spring Boot Usage

### Redis Cache

```java
@Cacheable
public User getUser() {
}
```

Object may be serialized before storing in Redis.

---

### Kafka

```java
User Object
    |
JSON / Byte Stream
    |
Kafka Topic
```

Consumer deserializes it back.

---

### Session Replication

```text
User Session
     |
Serialize
     |
Store
     |
Deserialize
```

Used in clustered applications.

---

# 20-Second Interview Answer

> Serialization is the process of converting a Java object into a byte stream so that it can be stored or transferred over a network. Deserialization is the reverse process of reconstructing the object from the byte stream. A class must implement the Serializable marker interface. We use serialVersionUID for version control, transient to exclude fields from serialization, and static fields are not serialized because they belong to the class rather than the object.

### Quick Revision

```text
Serialization   = Object → Byte Stream

Deserialization = Byte Stream → Object

Serializable    = Marker Interface

serialVersionUID = Version Control

transient       = Don't Serialize

static          = Never Serialized
```

🚀 This is enough for 90% of Java/Spring Boot interview questions on serialization.
