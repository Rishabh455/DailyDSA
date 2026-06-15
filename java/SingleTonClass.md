यहाँ एक ही code से **Singleton के 4 evolution stages** समझ सकते हो।

---

# 1. Basic Singleton Implementation

### Code

```java
class Calc {

    private static Calc obj = new Calc();

    private Calc() {
        System.out.println("Inside Singleton");
    }

    public static Calc getInstance() {
        return obj;
    }
}
```

### Idea

```text
Class Load
    |
Object Create
    |
Application Start
```

Object JVM startup पर ही बन जाता है।

### Problem

```text
Object chahiye ya nahi
Fir bhi create hoga
```

Memory waste हो सकती है।

---

# 2. Lazy Initialization

### Code

```java
class Calc {

    private static Calc obj;

    private Calc() {}

    public static Calc getInstance() {

        if(obj == null) {
            obj = new Calc();
        }

        return obj;
    }
}
```

### Idea

```text
Application Start
      |
Object NOT Created
      |
First getInstance()
      |
Create Object
```

Object तभी बनेगा जब पहली बार जरूरत होगी।

### Problem

Multi-threading में fail.

---

### Scenario

```text
Thread-1
Thread-2
```

दोनों:

```java
if(obj == null)
```

देखते हैं।

```text
Thread-1 -> TRUE
Thread-2 -> TRUE
```

दोनों object बना सकते हैं।

Singleton टूट गया ❌

---

# 3. Thread Safety using synchronized

### Code

```java
class Calc {

    private static Calc obj;

    private Calc() {}

    public static synchronized Calc getInstance() {

        if(obj == null) {
            obj = new Calc();
        }

        return obj;
    }
}
```

### Idea

```text
Thread-1 --> Lock
Thread-2 --> Wait
Thread-3 --> Wait
```

एक समय पर केवल एक thread अंदर जाएगा।

### Benefit

```text
Only One Object
```

Singleton safe ✅

### Problem

Object बन जाने के बाद भी:

```java
Calc.getInstance();
```

हर call पर lock लगेगा।

Performance issue ❌

---

# 4. Double Checked Locking (DCL)

### Code

```java
class Calc {

    private static volatile Calc obj;

    private Calc() {}

    public static Calc getInstance() {

        if(obj == null) {

            synchronized (Calc.class) {

                if(obj == null) {
                    obj = new Calc();
                }
            }
        }

        return obj;
    }
}
```

---

## First Check

```java
if(obj == null)
```

Purpose:

```text
Object already bana hai?
```

Yes →

```text
Direct return
```

No lock required.

Performance ✅

---

## Second Check

```java
synchronized(Calc.class) {

    if(obj == null)
```

Purpose:

```text
Multiple threads ek sath aaye?
```

Thread-1 object create karega.

Thread-2 lock milne ke baad check karega:

```java
if(obj == null)
```

False

New object create nahi hoga.

Safety ✅

---

## Why volatile?

```java
private static volatile Calc obj;
```

Without volatile:

```text
Memory Allocate
Reference Assign
Constructor Execute
```

Reordering ho sakti hai.

Dusra thread half-created object dekh sakta hai.

`volatile` prevent karta hai.

---

# Final Evolution Chart

```text
1. Basic Singleton
   |
   |-- Eager Loading
   |-- Thread Safe
   |-- Memory Waste

2. Lazy Initialization
   |
   |-- Lazy Loading
   |-- Not Thread Safe

3. Synchronized Method
   |
   |-- Lazy Loading
   |-- Thread Safe
   |-- Slow Performance

4. Double Checked Locking
   |
   |-- Lazy Loading
   |-- Thread Safe
   |-- Better Performance
   |-- Uses volatile
```

# Interview Answer (30 Seconds)

> Singleton ensures only one object of a class exists. Initially we can create the object eagerly, but that wastes memory. Lazy Initialization creates the object only when needed but is not thread-safe. Using a synchronized method makes it thread-safe but impacts performance because every call acquires a lock. Double Checked Locking solves this by checking null before and after synchronization, reducing lock overhead while maintaining thread safety. The instance variable must be declared volatile to prevent instruction reordering. 🚀
