**Diamond Problem** occurs when a class inherits from two classes that have the same method, creating ambiguity about which method should be used.

### Why Java Doesn't Have Diamond Problem with Classes

Java does **not support multiple inheritance of classes**.

❌ Invalid:

```java
class A {
    void show() {
        System.out.println("A");
    }
}

class B extends A {
}

class C extends A {
}

class D extends B, C { // Compilation Error
}
```

Because of this restriction, Java avoids the classic diamond problem.

---

## Diamond Problem with Interfaces (Java 8+)

Java allows multiple interface inheritance, so ambiguity can occur with default methods.

### Example

```java
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    default void show() {
        System.out.println("B");
    }
}

class Test implements A, B {

    @Override
    public void show() {
        System.out.println("Resolved");
    }
}
```

Here Java forces you to override `show()` because both interfaces provide the same default method.

---

## How to Call Specific Interface Method

```java
class Test implements A, B {

    @Override
    public void show() {
        A.super.show();
        B.super.show();
    }
}
```

Output:

```text
A
B
```

---

## Interview Answer (30 seconds)

> Diamond problem occurs when a class can inherit the same method from multiple parents, causing ambiguity about which implementation to use. Java avoids this for classes by not supporting multiple inheritance. However, with Java 8 default methods in interfaces, a similar situation can occur. In that case, Java forces the implementing class to override the conflicting method and resolve the ambiguity explicitly.
