Haan bhai 😄 — **questions ke saath proper interview-ready answers** chahiye. Tumhare resume + IKS JD + Architect interviewer ke perspective se 30 questions ko **easy → medium** rakhta hoon, lekin answers aise dunga ki tum interview mein directly explain kar sako.

Tumhare resume mein Python/FastAPI, Pydantic, async programming, microservices, testing aur production debugging specifically mentioned hain, isliye in topics ko higher priority de raha hoon. 

---

# 🐍 30 Python Interview Questions + Answers

## 🟢 1. What are the main features of Python?

**Answer:**

Python is a high-level, interpreted, dynamically typed and general-purpose programming language.

Its major features are:

* Easy and readable syntax
* Dynamically typed
* Object-oriented
* Supports functional programming
* Large standard library
* Huge ecosystem of third-party libraries
* Automatic memory management
* Supports synchronous and asynchronous programming

**Interview mein bol sakte ho:**

> "I primarily use Python because of its readability, strong ecosystem and the availability of frameworks like FastAPI and libraries for APIs, databases, testing and AI/ML. In my projects, Python is mainly used for backend services and API development."

---

# 2. What is the difference between List, Tuple, Set and Dictionary?

| Type       | Ordered | Mutable | Duplicates  |
| ---------- | ------- | ------- | ----------- |
| List       | Yes     | Yes     | Yes         |
| Tuple      | Yes     | No      | Yes         |
| Set        | No*     | Yes     | No          |
| Dictionary | Yes**   | Yes     | Keys unique |

Example:

```python
my_list = [1, 2, 2, 3]

my_tuple = (1, 2, 3)

my_set = {1, 2, 3}

my_dict = {
    "name": "Rishabh",
    "age": 25
}
```

**When to use?**

* List → collection that changes
* Tuple → fixed collection
* Set → unique values
* Dictionary → key-value data

---

# 3. What is mutable vs immutable?

**Mutable** means object can be changed after creation.

Examples:

```python
list
dict
set
```

**Immutable** means object cannot be changed after creation.

Examples:

```python
int
float
str
tuple
bool
```

Example:

```python
x = "hello"
x = x + " world"
```

Here Python creates a new string rather than modifying the existing string.

**Important interview point:**

> "Mutability is a property of the object, not simply of the variable."

---

# 4. Difference between `==` and `is`?

`==` checks **value equality**.

`is` checks **object identity**.

```python
a = [1, 2]
b = [1, 2]

print(a == b)   # True
print(a is b)   # False
```

Because values are same but objects are different.

Usually:

```python
if value is None:
```

is preferred instead of:

```python
if value == None:
```

---

# 5. What are common Python data types?

Common built-in types are:

```text
int
float
str
bool
list
tuple
set
dict
NoneType
```

For backend development, I frequently use:

```python
dict
list
str
int
bool
```

and structured models through Pydantic.

---

# 6. Shallow copy vs Deep copy?

**Shallow copy** creates a new outer object but nested objects can still be shared.

**Deep copy** recursively copies nested objects.

```python
import copy

a = [[1, 2], [3, 4]]

b = copy.copy(a)
c = copy.deepcopy(a)
```

With shallow copy:

```text
a → outer object
b → new outer object
      ↓
   same nested objects
```

With deep copy:

```text
a → completely independent structure
c → completely independent structure
```

**Use deep copy when you need complete independence of nested mutable objects.**

---

# 7. What is List Comprehension?

List comprehension provides a concise way to create a list.

Normal:

```python
numbers = [1, 2, 3, 4]

squares = []

for n in numbers:
    squares.append(n * n)
```

Comprehension:

```python
squares = [n * n for n in numbers]
```

With condition:

```python
even = [n for n in numbers if n % 2 == 0]
```

**Interview point:**

> "I use comprehensions when the transformation is simple and readable. For complex logic, I prefer a normal loop for maintainability."

---

# 8. Difference between append(), extend() and insert()?

### `append()`

Adds one object.

```python
a = [1, 2]
a.append(3)

# [1, 2, 3]
```

### `extend()`

Adds multiple elements.

```python
a.extend([3, 4])

# [1, 2, 3, 4]
```

### `insert()`

Adds at a specific position.

```python
a.insert(1, 100)
```

Result:

```text
[1, 100, 2, ...]
```

---

# 9. Difference between remove(), pop() and del?

### remove()

Removes by value.

```python
a.remove(10)
```

### pop()

Removes by index and returns the value.

```python
x = a.pop(2)
```

### del

Deletes an item/slice/reference.

```python
del a[2]
```

---

# 10. What are *args and **kwargs?

`*args` allows multiple positional arguments.

```python
def add(*args):
    return sum(args)

add(1, 2, 3)
```

`**kwargs` allows multiple keyword arguments.

```python
def user(**kwargs):
    print(kwargs)

user(name="Rishabh", age=25)
```

Inside the function:

```text
args   → tuple
kwargs → dictionary
```

---

# 🟡 11. What is a decorator?

A decorator is a function that modifies or extends another function's behavior without changing its actual code.

Example:

```python
def logger(func):

    def wrapper():
        print("Before function")
        func()
        print("After function")

    return wrapper
```

Usage:

```python
@logger
def process():
    print("Processing")
```

In backend applications, decorators can be useful for things like:

* Logging
* Authentication
* Authorization
* Timing
* Cross-cutting concerns

**Architect interviewer follow-up:**
"Where have you seen decorators in FastAPI?"

You should know that FastAPI itself uses decorator-based route definitions:

```python
@app.get("/users")
def get_users():
    ...
```

---

# 12. What is a Generator?

A generator produces values **lazily**, instead of storing all results in memory.

Example:

```python
def numbers():
    for i in range(5):
        yield i
```

Then:

```python
for n in numbers():
    print(n)
```

`yield` makes the function a generator.

### Why useful?

Suppose we process a very large file.

Instead of:

```python
data = file.readlines()
```

which loads everything into memory, we can process records incrementally.

**Interview answer:**

> "Generators are useful when dealing with large datasets or streams because they produce values one at a time and are memory efficient."

---

# 13. Iterable vs Iterator?

An **iterable** is an object that can be iterated over.

Examples:

```python
list
tuple
string
dict
```

An **iterator** is an object that actually produces the next value using:

```python
next()
```

Example:

```python
numbers = [1, 2, 3]

iterator = iter(numbers)

print(next(iterator))
print(next(iterator))
```

Output:

```text
1
2
```

---

# 14. What is Lambda?

Lambda is an anonymous function.

Example:

```python
square = lambda x: x * x

print(square(5))
```

Output:

```text
25
```

Commonly used with:

```python
sorted()
map()
filter()
```

Example:

```python
users = [
    {"name": "A", "age": 30},
    {"name": "B", "age": 20}
]

users.sort(key=lambda x: x["age"])
```

---

# 15. Explain map(), filter() and reduce()

### map()

Transforms every element.

```python
numbers = [1, 2, 3]

result = map(lambda x: x * 2, numbers)
```

### filter()

Filters elements based on condition.

```python
result = filter(lambda x: x > 2, numbers)
```

### reduce()

Combines values into one result.

```python
from functools import reduce

result = reduce(lambda x, y: x + y, numbers)
```

Result:

```text
6
```

---

# 16. Explain exception handling.

Python provides:

```python
try
except
else
finally
```

Example:

```python
try:
    result = 10 / 0

except ZeroDivisionError:
    print("Cannot divide by zero")

finally:
    print("Cleanup")
```

### `try`

Risky code.

### `except`

Handles exception.

### `else`

Runs if no exception occurs.

### `finally`

Runs regardless of success/failure.

---

# 17. Raise exception vs return error response?

These are different responsibilities.

Inside business logic:

```python
raise InvalidTransactionError()
```

Then API layer can convert that exception into an appropriate HTTP response.

For example:

```text
Business Layer
      ↓
raise exception
      ↓
Exception Handler
      ↓
HTTP 400/404/500
      ↓
Client
```

This helps keep business logic separate from HTTP concerns.

This separation is particularly relevant to the layered backend architecture described in your resume. 

---

# 18. How do you create a custom exception?

Example:

```python
class InvalidTransactionError(Exception):
    pass
```

Then:

```python
if amount <= 0:
    raise InvalidTransactionError("Invalid amount")
```

Why use custom exceptions?

Because they allow us to represent **domain-specific failures** clearly.

For example:

```text
InvalidTransactionError
DocumentProcessingError
KYCValidationError
ExternalServiceError
```

This is especially useful in your projects where different business/service failures need different handling.

---

# 19. Module vs Package?

A **module** is generally a Python file.

```text
payment.py
```

A **package** is a collection of related modules organized into a directory.

Example:

```text
payment/
    __init__.py
    service.py
    models.py
    repository.py
```

In larger backend applications, packages help organize responsibilities.

---

# 20. What is `if __name__ == "__main__"`?

It determines whether a Python file is being run directly or imported.

```python
if __name__ == "__main__":
    main()
```

If executed directly:

```bash
python app.py
```

then the block executes.

If imported:

```python
import app
```

the block doesn't execute.

Useful for separating reusable code from executable code.

---

# 🟠 21. Synchronous vs Asynchronous programming?

### Synchronous

One task waits for the previous task to complete.

```text
Task A → wait → Task B → wait → Task C
```

### Asynchronous

While one I/O operation is waiting, another task can execute.

```text
Task A → waiting
           ↓
        Task B
           ↓
        Task C
           ↓
        Task A completes
```

Async is especially useful for **I/O-bound operations** such as:

* API calls
* Database calls
* Network requests
* File/network operations

Your fraud service specifically uses FastAPI's asynchronous capabilities primarily around I/O-bound communication with downstream services/APIs/databases. 

---

# 22. What is asyncio?

`asyncio` is Python's framework for writing asynchronous code using an event loop.

Example:

```python
import asyncio

async def process():
    await some_operation()
```

Important keywords:

```python
async
await
```

`await` allows the event loop to handle other tasks while an asynchronous operation is waiting.

**Simple interview explanation:**

> "Asyncio enables cooperative concurrency using an event loop. It is particularly useful for I/O-bound workloads where the program spends significant time waiting."

---

# 23. If Python has GIL, why does async improve performance?

This is a **very likely follow-up**.

The GIL limits execution of Python bytecode by multiple threads within a single CPython process.

But async programming is not primarily about bypassing the GIL.

For I/O-bound applications:

```text
API call
   ↓
waiting...
   ↓
event loop executes another task
```

So the CPU doesn't sit idle waiting for network responses.

Therefore:

> **Async improves concurrency for I/O-bound workloads, even though the GIL exists.**

For CPU-heavy work, async alone generally doesn't provide the same benefit.

---

# 24. Concurrency vs Parallelism?

### Concurrency

Multiple tasks make progress during overlapping time periods.

```text
Task A ───────
       Task B ───────
```

### Parallelism

Multiple tasks literally execute at the same time, typically on different CPU cores.

```text
CPU 1 → Task A
CPU 2 → Task B
```

Simple interview line:

> "Concurrency is about managing multiple tasks, whereas parallelism is about executing multiple tasks simultaneously."

---

# 25. Threading vs Multiprocessing vs Asyncio?

This is **high priority**.

| Approach        | Best for                      |
| --------------- | ----------------------------- |
| Asyncio         | I/O-bound                     |
| Threading       | I/O-bound/blocking operations |
| Multiprocessing | CPU-bound                     |
| Async + workers | Mixed architectures           |

Example:

### Asyncio

Multiple API calls.

```text
API1 ─┐
API2 ─┼→ event loop
API3 ─┘
```

### Multiprocessing

CPU-heavy computation.

```text
Process 1 → CPU Core 1
Process 2 → CPU Core 2
```

For your fraud service, asynchronous programming makes sense primarily when communicating with downstream services rather than for CPU-heavy ML computation. 

---

# 26. What is Pydantic and why use it with FastAPI?

Pydantic is used for **data validation and serialization using Python type hints**.

Example:

```python
from pydantic import BaseModel

class Transaction(BaseModel):
    amount: float
    customer_id: str
    merchant_id: str
```

FastAPI can use this model to validate incoming JSON.

Input:

```json
{
    "amount": "abc"
}
```

will fail validation because:

```text
amount → float
```

is expected.

Your resume specifically mentions Pydantic request/response models for both onboarding and fraud APIs.  

---

# 27. How would you validate a transaction request?

I would define a Pydantic model.

```python
from pydantic import BaseModel, Field

class TransactionRequest(BaseModel):
    transaction_id: str
    customer_id: str
    amount: float = Field(gt=0)
    merchant_id: str
```

Then:

```python
@app.post("/transactions")
async def evaluate_transaction(
    transaction: TransactionRequest
):
    ...
```

FastAPI automatically validates the request against the model.

This gives us:

* Type validation
* Required fields
* Constraints
* Structured API contracts
* Consistent error responses

---

# 28. How would you structure a FastAPI application?

This is **very important for an Architect interviewer.**

I would separate responsibilities.

For example:

```text
app/
│
├── api/
│   └── routes/
│
├── schemas/
│   └── transaction.py
│
├── services/
│   └── fraud_service.py
│
├── repositories/
│   └── transaction_repository.py
│
├── clients/
│   └── ml_client.py
│
├── models/
│
├── core/
│   └── config.py
│
└── main.py
```

Flow:

```text
Request
   ↓
API Route
   ↓
Pydantic Validation
   ↓
Service Layer
   ↓
Repository / External Client
   ↓
Database / ML Service
   ↓
Response
```

The key idea is:

> **Don't put business logic, database queries and external API calls directly inside route handlers.**

Your resume itself describes separation between API/service layer, feature preparation, ML scoring and risk/business-rule engine. 

---

# 29. How do you handle exceptions in production FastAPI?

I would avoid putting large `try/except` blocks everywhere.

Instead, I would use:

### 1. Custom domain exceptions

```python
class FraudServiceError(Exception):
    pass
```

### 2. Global exception handlers

Convert exceptions into appropriate HTTP responses.

### 3. Logging

Log:

```text
request ID
service
error
stack trace
timestamp
downstream service
```

### 4. Correct HTTP status codes

For example:

```text
400 → Bad Request
401 → Unauthorized
403 → Forbidden
404 → Not Found
422 → Validation Error
500 → Internal Server Error
503 → Downstream service unavailable
```

### 5. Don't expose internal details

Instead of:

```json
{
    "error": "database password xyz..."
}
```

return something controlled:

```json
{
    "error": "Internal server error",
    "request_id": "abc123"
}
```

This is especially relevant to your production-support experience, where you've handled API, validation, database, business-logic and integration issues. 

---

# 30. Your Fraud API is slow. How would you debug it?

🔥 **This is probably the most important scenario question from this list.**

I would follow a systematic approach rather than immediately changing code.

### Step 1 — Identify where latency occurs

```text
Client
 ↓
FastAPI
 ↓
Validation
 ↓
Feature preparation
 ↓
Database
 ↓
ML service
 ↓
Business rules
 ↓
Response
```

Measure each stage.

### Step 2 — Check application logs

Look for:

* Request latency
* Exceptions
* Timeouts
* High-frequency endpoints

### Step 3 — Check downstream APIs

Maybe ML service is taking:

```text
50 ms normally
2 seconds currently
```

Then optimizing Python code won't solve the actual bottleneck.

### Step 4 — Check database

Look for:

* Slow queries
* Missing indexes
* Excessive queries
* N+1 patterns
* Large result sets

### Step 5 — Check async usage

If we're making independent I/O calls sequentially:

```python
result1 = await call_service_1()
result2 = await call_service_2()
result3 = await call_service_3()
```

and they're independent, we may be able to execute them concurrently using appropriate asyncio patterns.

### Step 6 — Profile CPU-heavy code

If the bottleneck is CPU-bound, async won't magically solve it.

Consider:

```text
worker processes
caching
optimization
moving heavy computation to dedicated service
```

### Strong interview answer:

> "I would first measure and identify the bottleneck instead of assuming Python itself is the problem. I would trace the request across FastAPI, validation, database calls, downstream services and business logic. Once I identify whether the bottleneck is I/O, database, external service or CPU, I would optimize that specific layer."

**Ye answer Architect ko kaafi strong lagega**, because you're showing **root-cause thinking instead of blindly saying 'use async'**.

---

# 🎯 Last-Minute Preparation Priority

Agar interview **kal hai**, to ye sequence follow karo:

### 🔴 Must Prepare

**Q21 → Q30**

Especially:

```text
Async/Await
     ↓
asyncio
     ↓
GIL
     ↓
Concurrency vs Parallelism
     ↓
Threading vs Multiprocessing vs Async
     ↓
Pydantic
     ↓
FastAPI architecture
     ↓
Exception handling
     ↓
Performance debugging
```

### 🟡 Then prepare

```text
Decorators
Generators
Iterators
Mutable/Immutable
Shallow/Deep Copy
*args/**kwargs
```

### 🟢 Then fundamentals

```text
List/Tuple/Set/Dict
append/extend/insert
remove/pop/del
lambda
map/filter/reduce
modules/packages
```

**Ek aur important point:** interviewer Architect hai aur uska background architecture, microservices, SOA, distributed systems, cloud aur architecture reviews ka hai.  

Isliye Python ka answer **"definition → practical example → tumhare project mein use"** format mein dena. Sirf textbook definition mat bolna.
