Bilkul. Python interviews mein **Wrapper Class** se directly kam, lekin **composition, delegation, proxy pattern, decorators aur `__getattr__`** ke through questions aa sakte hain. Neeche **copy-paste-ready interview Q&A** hain.

# Python Wrapper Class – Interview Questions & Answers

## 1. What is a Wrapper Class in Python?

**Answer:**

A wrapper class is a class that **contains another object** and provides additional, modified, or controlled behavior around that object.

It usually uses **composition**.

```python
class Car:
    def start(self):
        print("Car started")


class CarWrapper:
    def __init__(self, car):
        self.car = car

    def start(self):
        print("Performing checks...")
        self.car.start()
```

Here, `CarWrapper` wraps the `Car` object.

---

## 2. Why do we use Wrapper Classes?

**Answer:**

Wrapper classes are used when we want to:

* Add extra behavior
* Modify existing behavior
* Control access to an object
* Hide implementation details
* Add logging, validation, caching, etc.
* Reuse an existing class without modifying its source code

Example:

```python
class Database:
    def save(self, data):
        print("Saving data")


class DatabaseWrapper:
    def __init__(self, db):
        self.db = db

    def save(self, data):
        print("Logging:", data)
        self.db.save(data)
```

---

## 3. What is the difference between Wrapper Class and Inheritance?

**Answer:**

Inheritance represents an **"is-a" relationship**, while a wrapper generally represents a **"has-a" relationship**.

### Inheritance

```python
class Car:
    def start(self):
        print("Starting")


class SportsCar(Car):
    pass
```

`SportsCar` **is a** `Car`.

### Wrapper / Composition

```python
class CarWrapper:
    def __init__(self, car):
        self.car = car
```

`CarWrapper` **has a** `Car`.

**Interview line:**

> Wrapper classes generally use composition, while inheritance uses an is-a relationship.

---

## 4. What is Composition in Python?

**Answer:**

Composition means creating a class that **contains an object of another class**.

```python
class Engine:
    def start(self):
        print("Engine started")


class Car:
    def __init__(self):
        self.engine = Engine()

    def start(self):
        self.engine.start()
```

Here:

```python
self.engine = Engine()
```

means `Car` contains an `Engine` object.

This is called **composition**.

---

## 5. What is Delegation?

**Answer:**

Delegation means a wrapper object **passes a method call to the object it contains**.

```python
class Car:
    def start(self):
        print("Car started")


class CarWrapper:
    def __init__(self, car):
        self.car = car

    def start(self):
        print("Before starting")
        self.car.start()
        print("After starting")
```

This line:

```python
self.car.start()
```

is delegation.

---

## 6. What is the difference between Wrapper and Decorator?

**Answer:**

A **wrapper class** wraps an object, whereas a **decorator** commonly wraps a function or class to add behavior.

### Wrapper Class

```python
class Wrapper:
    def __init__(self, obj):
        self.obj = obj
```

### Function Decorator

```python
def decorator(func):
    def wrapper():
        print("Before")
        func()
        print("After")

    return wrapper
```

**Interview answer:**

> A wrapper class typically uses composition to wrap an object, while a decorator uses callable wrapping to dynamically add behavior to a function or class.

---

## 7. Can a Wrapper Class modify the behavior of an existing object?

**Answer:**

Yes.

```python
class Payment:
    def pay(self):
        print("Payment successful")


class PaymentWrapper:
    def __init__(self, payment):
        self.payment = payment

    def pay(self):
        print("Validating payment")
        self.payment.pay()
        print("Payment logged")
```

The original `Payment` class is not modified.

---

## 8. Can a Wrapper Class add validation?

**Answer:**

Yes.

```python
class User:
    def save(self, age):
        print("User saved")


class UserWrapper:
    def __init__(self, user):
        self.user = user

    def save(self, age):
        if age < 18:
            raise ValueError("User must be 18+")

        self.user.save(age)
```

The wrapper adds validation before calling the original object.

---

## 9. How can a Wrapper Class add logging?

**Answer:**

```python
class Service:
    def process(self):
        print("Processing...")


class ServiceWrapper:
    def __init__(self, service):
        self.service = service

    def process(self):
        print("LOG: Process started")

        self.service.process()

        print("LOG: Process completed")
```

This is useful when we don't want to modify the original `Service` class.

---

## 10. What is a Proxy Pattern?

**Answer:**

A Proxy is an object that **controls access to another object**.

A wrapper class can be used to implement the Proxy pattern.

Example:

```python
class RealService:
    def process(self):
        print("Processing...")


class ServiceProxy:
    def __init__(self, service):
        self.service = service

    def process(self):
        print("Checking permission")
        self.service.process()
```

The proxy controls access to `RealService`.

---

## 11. What is `__getattr__()` and how is it useful in Wrapper Classes?

**Answer:**

`__getattr__()` is called when an attribute is **not found normally** on an object.

It can be used to delegate unknown attributes to the wrapped object.

```python
class Car:
    def start(self):
        print("Started")

    def stop(self):
        print("Stopped")


class CarWrapper:
    def __init__(self, car):
        self.car = car

    def __getattr__(self, name):
        return getattr(self.car, name)
```

Now:

```python
car = Car()
wrapper = CarWrapper(car)

wrapper.start()
wrapper.stop()
```

The wrapper forwards these calls to `Car`.

---

## 12. What does `getattr()` do?

**Answer:**

`getattr()` allows us to access an object's attribute dynamically.

```python
class Person:
    name = "Rishabh"


p = Person()

print(getattr(p, "name"))
```

Output:

```text
Rishabh
```

It is especially useful for delegation in wrapper classes.

---

## 13. What is the difference between `__getattr__()` and `__getattribute__()`?

**Answer:**

`__getattr__()` is called **only when normal attribute lookup fails**.

`__getattribute__()` is called for **every attribute access**.

Example:

```python
class Test:
    def __getattr__(self, name):
        print("Attribute not found")

    def __getattribute__(self, name):
        print("Accessing:", name)
        return object.__getattribute__(self, name)
```

**Interview tip:**

> `__getattribute__()` intercepts all attribute access, while `__getattr__()` handles attributes that could not be found normally.

---

## 14. Can a Wrapper Class hide the original object's implementation?

**Answer:**

Yes.

The caller interacts with the wrapper instead of directly interacting with the underlying object.

```python
class Database:
    def connect(self):
        print("Connecting to database")


class DatabaseWrapper:
    def __init__(self, db):
        self.db = db

    def connect(self):
        print("Secure connection")
        self.db.connect()
```

The caller only needs to know about `DatabaseWrapper`.

---

## 15. What is the advantage of Wrapper Class over modifying the original class?

**Answer:**

A wrapper follows the principle:

> **Open for extension, closed for modification.**

Instead of changing existing code, we add another layer around it.

Benefits:

* Less risk of breaking existing code
* Better separation of concerns
* Easier testing
* Reusability
* Existing class remains unchanged

---

# Advanced Interview Questions

## 16. How would you create a generic Wrapper Class?

**Answer:**

We can use `__getattr__()` for generic delegation.

```python
class Wrapper:
    def __init__(self, obj):
        self._obj = obj

    def __getattr__(self, name):
        return getattr(self._obj, name)
```

Example:

```python
class Person:
    def speak(self):
        print("Hello")

    def walk(self):
        print("Walking")


person = Person()
wrapper = Wrapper(person)

wrapper.speak()
wrapper.walk()
```

The wrapper automatically delegates unknown attributes to `Person`.

---

## 17. What happens if the wrapped object doesn't have the requested attribute?

**Answer:**

`getattr()` raises an `AttributeError`.

```python
class Wrapper:
    def __init__(self, obj):
        self.obj = obj

    def __getattr__(self, name):
        return getattr(self.obj, name)
```

If:

```python
wrapper.xyz()
```

and `xyz` doesn't exist in the wrapped object, Python raises:

```text
AttributeError
```

---

## 18. Is a Wrapper Class the same as a Decorator?

**Answer:**

No.

They are related in the sense that both can add behavior without modifying the original implementation, but they work differently.

**Wrapper Class:**

```text
Object → Wrapper → Modified/Controlled behavior
```

**Decorator:**

```text
Function → Decorator → Wrapped Function
```

A decorator can also be implemented using a class that defines `__call__()`, but that's a **class-based decorator**, not the same thing as a wrapper class for objects.

---

## 19. Can a class itself be used as a decorator?

**Answer:**

Yes. A class can act as a decorator if it implements `__call__()`.

```python
class Logger:
    def __init__(self, func):
        self.func = func

    def __call__(self, *args, **kwargs):
        print("Function called")
        return self.func(*args, **kwargs)


@Logger
def greet():
    print("Hello")


greet()
```

Output:

```text
Function called
Hello
```

---

## 20. What is `__call__()`?

**Answer:**

`__call__()` allows an object to be called like a function.

```python
class Test:
    def __call__(self):
        print("Object called")


obj = Test()

obj()
```

Output:

```text
Object called
```

This is commonly used in **class-based decorators**.

---

## 21. Give a real-world example of a Wrapper Class.

**Answer:**

A common example is adding logging around an API client.

```python
class APIClient:
    def get(self, url):
        print("Calling API:", url)


class LoggingAPIClient:
    def __init__(self, client):
        self.client = client

    def get(self, url):
        print("LOG: API request started")
        self.client.get(url)
        print("LOG: API request completed")
```

This allows us to add logging without changing `APIClient`.

---

# ⭐ Most Important Interview Questions

Agar interviewer **Wrapper Class** par questions poochta hai, ye 7 pakka prepare karo:

1. **What is a wrapper class?**
2. **Why do we use wrapper classes?**
3. **Wrapper vs inheritance?**
4. **Wrapper vs composition?**
5. **What is delegation?**
6. **How does `__getattr__()` help in wrappers?**
7. **Wrapper class vs decorator?**

### One-line answer to remember

> **A wrapper class uses composition to contain another object and adds, modifies, or controls its behavior through delegation without changing the original class.**
