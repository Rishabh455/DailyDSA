# Python Decorators — Interview-Friendly Explanation

A **decorator** in Python is a function that **adds or modifies the behavior of another function without changing its original code**.

Think of it like a **wrapper around a function**.

### Simple Example

```python
def decorator(func):

    def wrapper():
        print("Before function")
        
        func()
        
        print("After function")

    return wrapper


@decorator
def greet():
    print("Hello")


greet()
```

### Output

```text
Before function
Hello
After function
```

---

## 1. How does a decorator actually work?

This:

```python
@decorator
def greet():
    print("Hello")
```

is basically the same as:

```python
def greet():
    print("Hello")

greet = decorator(greet)
```

So Python passes the `greet` function to the `decorator()` function.

The decorator returns `wrapper`, and now `greet` refers to `wrapper`.

---

# 2. Why do we need decorators?

Suppose you have 100 functions and you want to add **logging** to every function.

Without decorators, you might have to write:

```python
def add(a, b):
    print("Function started")
    result = a + b
    print("Function ended")
    return result
```

And repeat this logging code for every function.

With a decorator:

```python
def logger(func):

    def wrapper(*args, **kwargs):
        print("Function started")

        result = func(*args, **kwargs)

        print("Function ended")

        return result

    return wrapper
```

Now you can simply do:

```python
@logger
def add(a, b):
    return a + b


@logger
def multiply(a, b):
    return a * b
```

The logging logic is reusable.

---

# 3. Decorator with arguments

This is **very important for interviews**.

Consider:

```python
def logger(func):

    def wrapper(*args, **kwargs):
        print("Function started")

        result = func(*args, **kwargs)

        print("Function ended")

        return result

    return wrapper


@logger
def add(a, b):
    return a + b


print(add(10, 20))
```

Output:

```text
Function started
Function ended
30
```

### Why `*args` and `**kwargs`?

Because we don't know what arguments the original function will accept.

For example:

```python
@logger
def add(a, b):
    pass
```

could be:

```python
@logger
def greet(name):
    pass
```

or:

```python
@logger
def employee(name, age, city):
    pass
```

So:

```python
def wrapper(*args, **kwargs):
```

makes the decorator flexible.

---

# 4. Real-world example: Authentication

Decorators are commonly used for authentication.

```python
def login_required(func):

    def wrapper(user):
        if user == "admin":
            return func(user)

        return "Access Denied"

    return wrapper


@login_required
def dashboard(user):
    return "Welcome to Dashboard"


print(dashboard("admin"))
print(dashboard("john"))
```

Output:

```text
Welcome to Dashboard
Access Denied
```

The original `dashboard()` function doesn't contain authentication logic.

The decorator handles it.

---

# 5. Decorator for measuring execution time

This is another good interview example.

```python
import time


def timer(func):

    def wrapper(*args, **kwargs):

        start = time.time()

        result = func(*args, **kwargs)

        end = time.time()

        print("Execution time:", end - start)

        return result

    return wrapper


@timer
def calculate():
    time.sleep(2)
    return "Done"


print(calculate())
```

The decorator measures how long the function takes to execute.

---

# 6. Why do we use `functools.wraps`?

This is a **very common 2–4 year Python interview question**.

Consider:

```python
def logger(func):

    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)

    return wrapper
```

Now:

```python
@logger
def add(a, b):
    """Add two numbers."""
    return a + b
```

If you do:

```python
print(add.__name__)
```

you may get:

```text
wrapper
```

because `add` has been replaced by the wrapper function.

To preserve the original function's metadata, use:

```python
from functools import wraps


def logger(func):

    @wraps(func)
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)

    return wrapper
```

Now:

```python
print(add.__name__)
```

gives:

```text
add
```

### Interview answer:

> `functools.wraps` is used inside decorators to preserve the metadata of the original function, such as its name, docstring, and other attributes.

---

# 7. Decorator with multiple functions

```python
def logger(func):

    @wraps(func)
    def wrapper(*args, **kwargs):
        print(f"Calling {func.__name__}")
        result = func(*args, **kwargs)
        print(f"Finished {func.__name__}")
        return result

    return wrapper
```

Then:

```python
@logger
def add(a, b):
    return a + b


@logger
def multiply(a, b):
    return a * b
```

Both functions automatically get logging behavior.

---

# 8. Multiple decorators

You can apply multiple decorators to one function.

```python
@decorator1
@decorator2
def greet():
    print("Hello")
```

Python applies them from **bottom to top**.

It is equivalent to:

```python
greet = decorator1(decorator2(greet))
```

So `decorator2` is applied first, then `decorator1`.

---

# 9. Decorator that accepts its own arguments

Sometimes the decorator itself needs parameters.

Example:

```python
def repeat(times):

    def decorator(func):

        def wrapper(*args, **kwargs):

            for _ in range(times):
                func(*args, **kwargs)

        return wrapper

    return decorator
```

Usage:

```python
@repeat(3)
def greet():
    print("Hello")


greet()
```

Output:

```text
Hello
Hello
Hello
```

Notice there are **three levels**:

```text
repeat()
   ↓
decorator()
   ↓
wrapper()
   ↓
original function
```

---

# 10. Decorators are based on important Python concepts

To understand decorators properly, you should know:

### First-class functions

Functions can be:

```python
# assigned to a variable
x = my_function

# passed as arguments
some_function(my_function)

# returned from another function
return my_function
```

### Nested functions

A function can be defined inside another function:

```python
def outer():

    def inner():
        print("Hello")

    inner()
```

### Closures

The inner function can remember variables from the enclosing function.

Decorators make heavy use of these concepts.

---

# 11. Common built-in decorators

Python itself provides several decorators.

### `@staticmethod`

```python
class Employee:

    @staticmethod
    def company_info():
        print("Company information")
```

### `@classmethod`

```python
class Employee:

    @classmethod
    def create_employee(cls):
        return cls()
```

### `@property`

```python
class Employee:

    @property
    def name(self):
        return self._name
```

---

# 12. Frameworks use decorators heavily

You'll see decorators frequently in **Flask** and **FastAPI**.

For example, Flask:

```python
@app.route("/users")
def get_users():
    return "Users"
```

Here:

```python
@app.route("/users")
```

is a decorator.

FastAPI also uses decorators:

```python
@app.get("/users")
def get_users():
    return {"users": []}
```

The decorator tells the framework which URL and HTTP method should be associated with the function.

---

# 13. Most important interview questions

### Q1. What is a decorator?

**Answer:**

> A decorator is a function that modifies or extends the behavior of another function without changing its source code.

### Q2. What does `@decorator` mean?

**Answer:**

```python
@decorator
def test():
    pass
```

is equivalent to:

```python
test = decorator(test)
```

### Q3. Why are `*args` and `**kwargs` commonly used in decorators?

**Answer:**

> They allow the wrapper to accept any number of positional and keyword arguments, making the decorator reusable with functions having different signatures.

### Q4. What is `functools.wraps`?

**Answer:**

> `functools.wraps` preserves the metadata of the original function when it is wrapped by a decorator.

### Q5. Can we have multiple decorators?

**Answer:**

Yes.

```python
@decorator1
@decorator2
def test():
    pass
```

They are applied from bottom to top.

### Q6. Where are decorators used in real projects?

**Answer:**

Common use cases include:

* Authentication
* Authorization
* Logging
* Execution-time measurement
* Caching
* Validation
* Rate limiting
* Transaction management
* API routing

---

## ⭐ Interview Definition to Remember

> **A decorator in Python is a higher-order function that takes another function as input and returns a modified or wrapped function, allowing us to add functionality without modifying the original function's source code.**

If you're preparing for a **Python Full Stack interview with ~3 years' experience**, decorators are definitely a topic worth knowing beyond just the basic definition—especially **`@wraps`, `*args/**kwargs`, closures, multiple decorators, and Flask/FastAPI usage**.
