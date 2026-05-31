What happens internally when you create an object using new?

When we create an object using new, Java does several steps behind the scenes:

Memory is allocated

Java first creates space in memory (heap) for the new object.

Default values are set

All variables of the object are given default values, like 0 for numbers, false for boolean, and null for objects.

Constructor is called

Then the constructor runs. It sets the values we pass and runs any setup code.

Reference is returned

Finally, the address (reference) of that object is returned and stored in our variable.

In simple words:

new means, make space -> set defaults -> run constructor -> give us the object.

--------------------------------------
Yes, **we can create an object without using the `new` keyword** in Java.

Interview mein commonly ye 4 methods bataye jaate hain:

## 1. Using `clone()`

Creates a copy of an existing object.

```java
class Employee implements Cloneable {
    int id = 101;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        Employee e1 = new Employee();
        Employee e2 = (Employee) e1.clone();

        System.out.println(e2.id);
    }
}
```

---

## 2. Using Reflection (`Class.forName()`)

```java
class Employee {
    Employee() {
        System.out.println("Object Created");
    }
}

public class Main {
    public static void main(String[] args) throws Exception {

        Employee emp =
            (Employee) Class.forName("Employee")
                            .getDeclaredConstructor()
                            .newInstance();
    }
}
```

Although `newInstance()` internally creates the object, we don't explicitly use `new`.

---

## 3. Using Deserialization

Object is recreated from a byte stream.

```java
ObjectInputStream in =
    new ObjectInputStream(new FileInputStream("obj.ser"));

Employee emp = (Employee) in.readObject();
```

During deserialization, constructor is not called.

---

## 4. Using `Unsafe` Class (Advanced)

```java
Unsafe unsafe = getUnsafe();

Employee emp =
    (Employee) unsafe.allocateInstance(Employee.class);
```

This creates an object **without calling the constructor**.

---

# Interview Answer (Short Version)

**Yes. Objects can be created without using the `new` keyword by:**

1. `clone()`
2. Reflection (`Class.forName()`, `newInstance()`)
3. Deserialization (`readObject()`)
4. `Unsafe.allocateInstance()`

The most commonly asked methods in interviews are **clone(), Reflection, and Deserialization**.
-------------------------------------
