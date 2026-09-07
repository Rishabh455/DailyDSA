Bilkul bhai. Since tum **FastAPI interview preparation** kar rahe ho, main is set ko basic CRUD se hata kar **real production scenarios + troubleshooting + architecture + performance** oriented rakh raha hoon.

## 🔥 30 Medium–Hard Scenario-Based FastAPI Interview Questions + Answers

### 1. Your FastAPI API suddenly becomes slow when 100 users hit it simultaneously. How would you debug it?

**Answer:**

> "First, I would identify whether the bottleneck is CPU, database, external API calls, or application code. I would check latency metrics, CPU/memory utilization, DB connection pool usage and logs. If the API is I/O bound, I would make sure I'm using async endpoints and async DB/client libraries. I would also check whether blocking code is running inside the event loop. Finally, I would use load testing and profiling to identify the exact bottleneck before optimizing."

---

### 2. You have an endpoint that calls 5 external APIs sequentially. How would you improve performance?

**Answer:**

Instead of:

```python
result1 = await call_api1()
result2 = await call_api2()
result3 = await call_api3()
```

I would use concurrent execution:

```python
results = await asyncio.gather(
    call_api1(),
    call_api2(),
    call_api3(),
    call_api4(),
    call_api5()
)
```

> "If those calls are independent and I/O-bound, `asyncio.gather()` allows them to execute concurrently, reducing total waiting time."

---

### 3. What happens if you use `requests.get()` inside an `async def` FastAPI endpoint?

**Answer:**

`requests` is synchronous/blocking.

```python
@app.get("/users")
async def users():
    response = requests.get(url)
```

This can **block the event loop** while waiting for the network.

> "For asynchronous endpoints I would prefer an async HTTP client such as `httpx.AsyncClient`. If I have unavoidable synchronous code, I can move it to a threadpool rather than blocking the event loop."

---

### 4. Your endpoint performs heavy CPU processing. Should you use `async def`?

**Answer:**

Not automatically.

> "Async is primarily beneficial for I/O-bound operations. CPU-heavy work can still block the event loop. For CPU-intensive processing, I would consider multiprocessing, a task queue such as Celery, or another background worker architecture."

---

### 5. You need to process a 500 MB file through FastAPI. Would you load it completely into memory?

**Answer:**

No.

> "I would stream the file or process it in chunks. Loading a 500 MB file into memory for every request can cause high memory consumption and potentially crash the application under concurrent load."

FastAPI provides mechanisms such as `UploadFile`, which is preferable for larger uploads.

---

### 6. Your API needs authentication for every endpoint except `/login` and `/health`. How would you design it?

**Answer:**

I would use FastAPI's dependency injection.

For example:

```python
async def get_current_user(...):
    ...
```

Then:

```python
@app.get("/users")
async def users(user = Depends(get_current_user)):
    ...
```

> "For larger applications, I would organize authentication dependencies at router level so that protected endpoints don't need to repeat the dependency individually."

---

### 7. An attacker sends a huge JSON payload to your API. How would you protect the endpoint?

**Answer:**

I would use multiple layers:

* Request size limits at reverse proxy/API gateway
* Pydantic validation
* Authentication/authorization
* Rate limiting
* Appropriate server configuration

> "Validation alone isn't sufficient because the request body may already consume resources before validation completes."

---

### 8. Your API returns database passwords accidentally in the response. How would you prevent this?

**Answer:**

Use response schemas that explicitly define what can be returned.

```python
class UserResponse(BaseModel):
    id: int
    name: str
    email: str
```

Don't directly expose the database model.

> "I prefer separate request, database and response models so sensitive fields cannot accidentally leak."

---

### 9. You receive this request:

```json
{
  "age": "twenty"
}
```

but your API expects:

```python
age: int
```

What happens?

**Answer:**

FastAPI/Pydantic validates the request and returns a **422 validation error** because `"twenty"` cannot be converted to an integer.

> "This is one of the major benefits of FastAPI's type-driven validation."

---

### 10. Your database query takes 10 seconds. Would making the endpoint async solve it?

**Answer:**

**No.**

> "Async doesn't make a slow database query inherently faster. It mainly prevents the application from blocking while waiting for I/O. I would investigate query optimization, indexes, execution plans, pagination, connection pooling and potentially caching."

This is a **very common interview trap**.

---

### 11. You have 1000 concurrent requests but your DB allows only 20 connections. What problem can occur?

**Answer:**

Requests can wait for an available database connection, causing increased latency and potentially timeouts.

> "I would configure an appropriate connection pool, avoid unnecessarily long transactions, optimize queries, and potentially introduce caching or queueing depending on the workload."

---

### 12. How would you implement pagination for 10 million database records?

**Answer:**

I wouldn't return everything.

Basic pagination:

```text
GET /users?page=1&limit=20
```

For very large datasets, I would consider **cursor/keyset pagination** instead of large offsets.

> "Offset pagination can become expensive for deep pages because the database may scan or skip many records."

---

### 13. Two users update the same bank account simultaneously. How would you prevent inconsistent data?

**Answer:**

I would use **database transactions and appropriate concurrency control**, potentially optimistic or pessimistic locking depending on the use case.

> "For financial operations, I wouldn't rely only on application-level checks because concurrent requests can bypass those checks."

---

### 14. Your API receives the same payment request twice because the client retries. How do you prevent duplicate payment processing?

**Answer:**

Use an **idempotency key**.

Example:

```http
Idempotency-Key: abc123
```

The server stores the result associated with that key.

> "If the same request arrives again with the same key, I return the previous result instead of processing the payment again."

---

### 15. You have an API that takes 30 seconds to generate a report. Would you keep the client waiting?

**Answer:**

Usually no.

I would use asynchronous job processing:

```text
POST /reports
      ↓
Create Job
      ↓
Return job_id
      ↓
Background Worker
      ↓
Generate Report
      ↓
GET /reports/{job_id}
```

> "For long-running work, I prefer a task queue/background worker architecture rather than keeping an HTTP request open."

---

### 16. What's the difference between FastAPI `BackgroundTasks` and Celery?

**Answer:**

`BackgroundTasks` is suitable for relatively lightweight tasks executed within the application process after returning the response.

Celery is better for:

* Heavy processing
* Distributed workers
* Retries
* Scheduled jobs
* Large workloads
* Fault tolerance

> "I wouldn't use FastAPI BackgroundTasks as a replacement for a distributed task queue."

---

### 17. Your FastAPI application works perfectly locally but crashes in production under load. What would you investigate?

**Answer:**

I would check:

1. Worker configuration
2. CPU/memory
3. Database connections
4. Connection pool
5. Blocking code
6. External API latency
7. Logs
8. Timeouts
9. Container/resource limits
10. Load balancer configuration

> "I would reproduce the issue through load testing rather than guessing."

---

### 18. What is the purpose of multiple Uvicorn/Gunicorn workers?

**Answer:**

Multiple workers create multiple processes.

This helps utilize multiple CPU cores and increases throughput for workloads where a single process is insufficient.

But:

> "Each worker has its own memory space and event loop, so increasing workers also increases resource consumption."

---

### 19. Does `asyncio` mean your FastAPI code is running on multiple CPU cores?

**Answer:**

No.

This is a **trick question**.

> "`asyncio` provides concurrency within an event loop, primarily useful for I/O-bound workloads. It does not by itself provide CPU parallelism across multiple cores."

For CPU parallelism, multiprocessing or distributed workers may be appropriate.

---

### 20. Your FastAPI endpoint uses `async def`, but performance is still terrible. Why?

**Answer:**

Because simply declaring:

```python
async def
```

doesn't make everything asynchronous.

For example:

```python
async def endpoint():
    data = requests.get(...)
    result = cpu_heavy_function()
```

Both can cause problems.

> "I would check for synchronous/blocking libraries, CPU-heavy operations, slow database queries, and inefficient application logic."

---

### 21. You need to call 100 APIs concurrently. Would you simply use `asyncio.gather()` for all 100?

**Answer:**

Not necessarily.

100 concurrent calls may overload:

* External API
* Network
* Your connection pool
* Your application

I would use **bounded concurrency**, for example a semaphore:

```python
semaphore = asyncio.Semaphore(10)

async def call_api(url):
    async with semaphore:
        return await client.get(url)
```

> "This allows concurrency while controlling resource usage."

---

### 22. An external API sometimes takes 60 seconds. How would you prevent your FastAPI request from hanging?

**Answer:**

I would configure timeouts.

For example with `httpx`:

```python
timeout = httpx.Timeout(10.0)
```

I would also consider:

* Retry policy
* Exponential backoff
* Circuit breaker
* Fallback response
* Logging/monitoring

---

### 23. Should you retry every failed external API request?

**Answer:**

No.

I would distinguish between transient and permanent failures.

Potentially retry:

```text
Timeout
503
temporary network failure
```

But generally don't blindly retry:

```text
400
401
403
validation errors
```

> "Retries should also have limits and backoff to avoid creating a retry storm."

---

### 24. How would you implement global exception handling in FastAPI?

**Answer:**

Using exception handlers.

Conceptually:

```python
@app.exception_handler(MyException)
async def handler(request, exc):
    return JSONResponse(
        status_code=400,
        content={"detail": str(exc)}
    )
```

> "This lets us centralize error formatting, logging and monitoring instead of duplicating error handling across endpoints."

---

### 25. Your API is returning different error formats from different endpoints. How would you fix it?

**Answer:**

I would establish a standardized error response.

For example:

```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found"
  }
}
```

Then use common exception handlers and custom exceptions.

---

### 26. You need API versioning because `/users` has a breaking change. What would you do?

**Answer:**

I would introduce explicit versions, for example:

```text
/api/v1/users
/api/v2/users
```

and maintain backward compatibility during migration.

> "I wouldn't silently change the contract of v1 because existing clients may break."

---

### 27. Your FastAPI application has 50 endpoints in one Python file. How would you restructure it?

**Answer:**

I would separate concerns:

```text
app/
├── main.py
├── routers/
│   ├── users.py
│   ├── auth.py
│   └── payments.py
├── schemas/
├── models/
├── services/
├── repositories/
├── dependencies/
└── config/
```

> "Routers handle HTTP concerns, services handle business logic, repositories handle data access, and schemas define API contracts."

---

### 28. Where should business logic go — router or service layer?

**Answer:**

Preferably the **service layer**.

Bad:

```python
@app.post("/order")
async def create_order():
    # 100 lines business logic
```

Better:

```python
@app.post("/order")
async def create_order(service = Depends(...)):
    return await service.create_order(...)
```

> "Thin controllers/routers make code easier to test, maintain and reuse."

---

### 29. Your FastAPI service needs Redis caching. What would you cache?

**Answer:**

Good candidates:

* Frequently accessed data
* Expensive database queries
* Configuration/reference data
* Results that don't change frequently

I would avoid caching highly dynamic or sensitive data blindly.

A typical flow:

```text
Request
   ↓
Check Redis
   ↓
Cache Hit → Return
   ↓
Cache Miss
   ↓
Database
   ↓
Store in Redis
   ↓
Return
```

---

### 30. 🔥 Production scenario: Your FastAPI API has 500 requests/sec. Suddenly database latency increases, requests start timing out, and CPU is only 30%. What do you suspect?

**Strong answer:**

> "Since CPU isn't saturated, I wouldn't immediately add more application workers. I'd investigate the database and I/O path first."

I would check:

```text
DB connection pool
       ↓
Slow queries
       ↓
Missing indexes
       ↓
Lock contention
       ↓
Too many concurrent queries
       ↓
Database CPU/IO
       ↓
Connection exhaustion
```

Then I'd check whether requests are waiting for DB connections.

> "If the DB is the bottleneck, horizontally scaling FastAPI instances could actually make things worse because more application instances may create even more database connections."

**This is the kind of answer that shows production-level understanding.**

---

# 🧠 5 Concepts Interviewer Is Likely to Probe Deeply

Tumhare FastAPI interview mein especially ye chains prepare kar lena:

### 1. Async

```text
async/await
   ↓
Event Loop
   ↓
I/O concurrency
   ↓
Blocking code
   ↓
ThreadPool
   ↓
CPU-bound work
   ↓
Multiprocessing
```

### 2. Database

```text
FastAPI
   ↓
Dependency
   ↓
DB Session
   ↓
Connection Pool
   ↓
Transaction
   ↓
Query
   ↓
Indexes
```

### 3. Scalability

```text
Client
 ↓
Load Balancer
 ↓
Multiple FastAPI Workers
 ↓
Service
 ↓
Redis
 ↓
Database
```

### 4. Reliability

```text
Timeout
 ↓
Retry
 ↓
Exponential Backoff
 ↓
Circuit Breaker
 ↓
Fallback
```

### 5. Production API

```text
Authentication
Authorization
Validation
Rate Limiting
Logging
Monitoring
Exception Handling
Caching
Testing
```

**Sabse important:** interviewer agar tumhe scenario de, sirf FastAPI feature ka naam mat bolo. Answer ko **"problem → root cause → solution → trade-off"** format mein do. Ye tumhe normal FastAPI developer se significantly stronger candidate dikhayega.
