## Polymorphism Interview Notes (Based on Your Code)

### Compile Time Polymorphism (Method Overloading)

```java
class Add {

    public void add(int a, int b) {
        System.out.println(a+b);
    }

    public void add(int a, int b, int c) {
        System.out.println(a+b+c);
    }
}
```

Usage:

```java
Add a1 = new Add();

a1.add(1,2);
a1.add(1,2,3);
```

### What happens internally?

```text
Compiler sees:

add(1,2)
      ↓
add(int,int)

add(1,2,3)
      ↓
add(int,int,int)
```

Compiler already knows which method to call.

✅ Decision taken at Compile Time

### Key Points

```text
Method Overloading
Same Method Name
Different Parameters
Compile Time Binding
Faster
Inheritance Not Required
```

---

### Runtime Polymorphism (Method Overriding)

```java
interface Payment {
    void pay();
}

class CreditCardPayment implements Payment {
    public void pay() {
        System.out.println("Credit Card Payment");
    }
}

class NetBankingPayment implements Payment {
    public void pay() {
        System.out.println("Net Banking Payment");
    }
}
```

Usage:

```java
Payment p1 = new CreditCardPayment();
Payment p2 = new NetBankingPayment();

p1.pay();
p2.pay();
```

### What happens internally?

Compile Time:

```text
Reference Type = Payment

Compiler checks:

Does Payment have pay() ?

YES ✅
```

Runtime:

```text
p1 → CreditCardPayment Object
     ↓
CreditCardPayment.pay()

p2 → NetBankingPayment Object
     ↓
NetBankingPayment.pay()
```

JVM checks actual object and calls the corresponding method.

✅ Decision taken at Runtime

### Key Points

```text
Method Overriding
Parent Reference -> Child Object
Dynamic Binding
Inheritance Required
JVM Decides At Runtime
```

---

## Quick Comparison

| Compile Time          | Runtime            |
| --------------------- | ------------------ |
| Overloading           | Overriding         |
| Compiler decides      | JVM decides        |
| Static Binding        | Dynamic Binding    |
| Different Parameters  | Same Signature     |
| Faster                | Slightly Slower    |
| No Inheritance Needed | Inheritance Needed |

---

## 15-Second Interview Answer

> Compile Time Polymorphism is achieved using Method Overloading where the compiler decides which method to call based on the method signature. Runtime Polymorphism is achieved using Method Overriding where the compiler checks the reference type, but the JVM decides which method to execute based on the actual object at runtime.

### Memory Trick

```text
OverLoading
=
Compile Time
=
Compiler Decides

OverRiding
=
Runtime
=
JVM Decides
```

### Your Code Memory Line

```text
a1.add(1,2)
a1.add(1,2,3)
        ↓
Compile Time Polymorphism

Payment p = new CreditCardPayment();
p.pay();
        ↓
Runtime Polymorphism
```

🔥 If interviewer asks "Explain polymorphism with an example", these two snippets are enough for a strong Java interview answer.
