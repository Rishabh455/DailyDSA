Bilkul. **Python mein Async Programming** 3-year Python Full Stack interview ke liye kaafi important topic hai, especially **FastAPI** ke context mein.

# Async Programming in Python

## 1. Async Programming kya hoti hai?

**Async programming** ka matlab hai ki program kisi **I/O operation** (API call, database query, file/network operation) ke complete hone ka wait karte hue doosra kaam kar sakta hai.

Simple example:

```text
Synchronous:

Task A → wait → complete
                 ↓
Task B → wait → complete
                 ↓
Task C → wait → complete
```

Async:

```text
Task A → waiting ─────────→ complete
Task B → waiting → complete
Task C → complete
```

Isse **I/O-bound applications** mein performance improve ho sakti hai.

---

# 2. `async` kya hai?

`async` keyword function ko **coroutine function** banata hai.

```python
async def greet():
    print("Hello")
```

Is function ko normally call karne par function immediately execute nahi hota:

```python
result = greet()

print(result)
```

Ye ek **coroutine object** return karta hai.

---

# 3. `await` kya hai?

`await` ka use kisi awaitable operation ka result asynchronously wait karne ke liye hota hai.

```python
import asyncio

async def greet():
    await asyncio.sleep(2)
    print("Hello")

asyncio.run(greet())
```

Yahan:

```python
await asyncio.sleep(2)
```

ka matlab roughly:

> "Is operation ke complete hone ka wait karo, lekin event loop ko doosre async tasks run karne do."

---

# 4. `asyncio` kya hai?

`asyncio` Python ki standard library hai jo **asynchronous programming** ke liye event loop, coroutines, tasks etc. provide karti hai.

```python
import asyncio
```

Commonly used APIs:

```python
asyncio.run()
asyncio.sleep()
asyncio.create_task()
asyncio.gather()
```

---

# 5. Coroutine kya hoti hai?

`async def` se define ki gayi function **coroutine function** hoti hai.

```python
async def fetch_data():
    await asyncio.sleep(1)
    return "Data"
```

Jab call karte hain:

```python
result = fetch_data()
```

`result` ek **coroutine object** hota hai.

Coroutine ko execute karne ke liye:

```python
asyncio.run(fetch_data())
```

---

# 6. `asyncio.run()` kya karta hai?

`asyncio.run()` ek coroutine ko execute karne ke liye event loop create/run karta hai.

```python
import asyncio

async def main():
    print("Hello")

asyncio.run(main())
```

Interview mein bol sakte ho:

> `asyncio.run()` is commonly used as the entry point for running an asynchronous coroutine.

---

# 7. Event Loop kya hota hai?

**Event loop** async programming ka core mechanism hai.

Ye continuously check karta hai:

* Kaunsa task ready hai?
* Kaunsa task I/O ke liye wait kar raha hai?
* Kisko resume karna hai?

Conceptually:

```text
             Event Loop
                 |
       ---------------------
       |         |         |
     Task A    Task B    Task C
       |         |
    waiting   running
       |
   I/O complete
       |
    resume
```

Interview answer:

> An event loop manages and schedules asynchronous tasks and resumes them when their awaited operations are ready.

---

# 8. Synchronous vs Asynchronous

### Synchronous

```python
import time

def task(name):
    time.sleep(2)
    print(name)

task("A")
task("B")
```

Approximate time:

```text
4 seconds
```

### Asynchronous

```python
import asyncio

async def task(name):
    await asyncio.sleep(2)
    print(name)

async def main():
    await asyncio.gather(
        task("A"),
        task("B")
    )

asyncio.run(main())
```

Approximate time:

```text
2 seconds
```

Because both tasks can wait concurrently.

---

# 9. `asyncio.gather()` kya hai?

`asyncio.gather()` multiple awaitables ko concurrently run karne ke liye commonly use hota hai.

```python
import asyncio

async def task1():
    await asyncio.sleep(2)
    return "Task 1"

async def task2():
    await asyncio.sleep(2)
    return "Task 2"

async def main():
    results = await asyncio.gather(
        task1(),
        task2()
    )

    print(results)

asyncio.run(main())
```

Output:

```text
['Task 1', 'Task 2']
```

Sequential execution mein approximately 4 seconds lag sakte the, async concurrent waiting mein around 2 seconds.

---

# 10. `asyncio.create_task()` kya hai?

`create_task()` coroutine ko event loop ke through **Task** ke roop mein schedule karta hai.

```python
async def main():
    task1 = asyncio.create_task(task_a())
    task2 = asyncio.create_task(task_b())

    await task1
    await task2
```

Useful when you want to start tasks and await them later.

---

# 11. `await` ke bina async function call karenge to kya hoga?

Example:

```python
async def greet():
    print("Hello")

greet()
```

`greet()` coroutine object return karega, lekin coroutine execute nahi hogi.

Usually:

```python
await greet()
```

ya:

```python
asyncio.run(greet())
```

use karna padega.

---

# 12. Kya async programming multithreading hai?

**No.**

Async programming aur multithreading different concepts hain.

### Async

Generally:

```text
One event loop
      ↓
Multiple cooperative tasks
```

### Threads

```text
Thread 1
Thread 2
Thread 3
```

Async I/O-bound workloads ke liye particularly useful hai.

---

# 13. Async programming CPU-bound tasks ke liye useful hai?

Generally **not by itself**.

Example CPU-heavy task:

```python
def calculate():
    for i in range(100000000):
        ...
```

Aise CPU-bound work mein async event loop ko bhi block kar sakta hai.

CPU-bound workloads ke liye commonly:

* multiprocessing
* process pools
* optimized native libraries

consider kiye jaate hain.

---

# 14. I/O-bound vs CPU-bound

### I/O-bound

Program mostly waiting karta hai:

* API request
* Database query
* Network request
* File I/O

**Async is useful.**

### CPU-bound

Program CPU ko continuously use karta hai:

* Image processing
* Large calculations
* Machine-learning computation
* Heavy data processing

**Async alone generally isn't the solution.**

---

# 15. Blocking code kya hota hai?

Example:

```python
import time

async def task():
    time.sleep(5)
    print("Done")
```

Problem:

```python
time.sleep(5)
```

blocking hai.

Ye event loop ko block kar sakta hai.

Async code mein preferably:

```python
await asyncio.sleep(5)
```

use karo.

---

# 16. FastAPI mein Async ka use kyu hota hai?

FastAPI asynchronous endpoints support karta hai:

```python
from fastapi import FastAPI

app = FastAPI()

@app.get("/users")
async def get_users():
    users = await fetch_users()
    return users
```

Agar `fetch_users()` asynchronous I/O operation hai, server waiting time ke during other requests handle kar sakta hai.

---

# 17. `def` vs `async def` in FastAPI

```python
@app.get("/users")
def users():
    return get_users()
```

vs

```python
@app.get("/users")
async def users():
    return await get_users()
```

`async def` tab useful hai jab endpoint ke andar asynchronous operations hain.

**Important interview point:**

> `async def` automatically makes every operation asynchronous nahi bana deta.

For example:

```python
async def endpoint():
    time.sleep(10)
```

still blocking hai.

---

# 18. Async HTTP request ka example

Async HTTP client jaise `httpx` ke saath:

```python
import httpx

async def fetch_data():
    async with httpx.AsyncClient() as client:
        response = await client.get(
            "https://example.com"
        )

        return response.json()
```

Yahan:

```python
await client.get(...)
```

I/O complete hone ka asynchronously wait karta hai.

---

# 19. Async Database kya hota hai?

Async database driver async operations provide kar sakta hai:

```python
result = await db.execute(query)
```

Iska benefit ye hai ki database response ka wait karte waqt event loop other tasks handle kar sakta hai.

---

# 20. `async with` kya hai?

Async context manager ke liye:

```python
async with AsyncClient() as client:
    response = await client.get(url)
```

Ye asynchronous resource management ke liye use hota hai.

---

# 21. `async for` kya hai?

Asynchronous iterator ko iterate karne ke liye:

```python
async for item in async_generator():
    print(item)
```

Normal:

```python
for item in generator:
    ...
```

Async:

```python
async for item in async_generator():
    ...
```

---

# 22. Coroutine vs Task

Ye interview mein frequently poocha ja sakta hai.

### Coroutine

```python
async def fetch():
    ...
```

Calling:

```python
fetch()
```

coroutine object deta hai.

### Task

```python
task = asyncio.create_task(fetch())
```

Task coroutine ko event loop mein schedule karta hai.

**Interview answer:**

> A coroutine represents an asynchronous computation, while a Task schedules a coroutine to run on the event loop.

---

# 23. `asyncio.sleep()` vs `time.sleep()`

### `time.sleep()`

```python
time.sleep(2)
```

Blocking.

### `asyncio.sleep()`

```python
await asyncio.sleep(2)
```

Non-blocking from the event loop's perspective.

**Remember:**

```text
time.sleep()      → blocking
asyncio.sleep()   → cooperative async waiting
```

---

# 24. Common Interview Question: Does async always make code faster?

**Answer: No.**

Async mainly helps with **I/O-bound concurrent workloads**.

For a simple CPU calculation:

```python
x = 10 + 20
```

async use karne ka koi meaningful benefit nahi hai.

Interview mein bolo:

> Async programming improves throughput and responsiveness for suitable I/O-bound workloads; it does not automatically make every program faster.

---

# 25. Async Programming ka Real-World Example

Suppose API ko 3 external APIs call karni hain:

```text
API 1 → 2 sec
API 2 → 2 sec
API 3 → 2 sec
```

Sequential:

```text
API 1 → 2 sec
API 2 → 2 sec
API 3 → 2 sec

Total ≈ 6 sec
```

Concurrent async:

```text
API 1 ───────── 2 sec
API 2 ───────── 2 sec
API 3 ───────── 2 sec

Total ≈ 2 sec
```

Example:

```python
async def main():
    result = await asyncio.gather(
        call_api_1(),
        call_api_2(),
        call_api_3()
    )

    return result
```

---

# 🔥 Interview ke liye Most Important Questions

Agar tum **Python + FastAPI developer** interview de rahe ho, in questions ko strongly prepare karo:

1. What is asynchronous programming?
2. What is `async`?
3. What is `await`?
4. What is a coroutine?
5. What is an event loop?
6. What is `asyncio`?
7. What does `asyncio.run()` do?
8. What is `asyncio.gather()`?
9. What is `asyncio.create_task()`?
10. Coroutine vs Task?
11. Async vs synchronous programming?
12. Async vs multithreading?
13. Async vs multiprocessing?
14. I/O-bound vs CPU-bound?
15. Why is `time.sleep()` problematic in async code?
16. `asyncio.sleep()` vs `time.sleep()`?
17. What is blocking code?
18. Can async code execute CPU-bound tasks efficiently?
19. How does async work in FastAPI?
20. When should you use `async def` in FastAPI?
21. Can synchronous code be called from async function?
22. What happens if you don't use `await`?
23. What are `async with` and `async for`?
24. How do you make multiple API calls concurrently?
25. How does an async HTTP client work?

### ⭐ Ek line mein pura concept

> **Python async programming allows multiple I/O-bound operations to make progress concurrently using coroutines, `async`/`await`, and an event loop, without blocking while waiting for I/O.**
