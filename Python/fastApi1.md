Bilkul bhai. Tumhare **IKS Health Sr. Full Stack Engineer JD + resume + interviewer ke Enterprise Architect background** ko dekhte hue, FastAPI mein interviewer ke questions mostly **basic concepts se start hoke practical architecture/scenario** tak ja sakte hain.

Tumhare resume mein FastAPI, REST APIs, Pydantic, async programming, microservices, API validation/error handling aur downstream integrations explicitly mentioned hain.  

Neeche **30 high-probability FastAPI questions + interview-ready answers** hain.

---

# 🚀 FastAPI — 30 High-Probability Questions + Answers

## 🟢 BASIC LEVEL

### 1. What is FastAPI and why would you use it?

**Answer:**

> "FastAPI is a modern Python web framework used for building APIs. It is built on Starlette for the web layer and Pydantic for data validation. I would use it because it provides high performance, automatic request validation, automatic OpenAPI documentation, dependency injection and strong support for asynchronous programming."

Tumhare project mein FastAPI ka use REST APIs expose karne, request validation, service logic aur downstream integrations ke liye hua hai. 

---

### 2. Why is FastAPI considered fast?

FastAPI's performance comes mainly from:

* ASGI architecture
* Starlette
* Async/await support
* Pydantic validation
* Efficient request handling

Simple flow:

```text
Client
   ↓
ASGI Server
   ↓
FastAPI
   ↓
Route
   ↓
Business Logic
```

**Interview answer:**

> "FastAPI is designed on top of ASGI, which allows asynchronous request handling. This makes it particularly suitable for I/O-bound workloads such as database calls and external API calls."

---

### 3. What is ASGI?

ASGI stands for:

**Asynchronous Server Gateway Interface.**

It is the interface between an asynchronous Python web server and the Python application.

For example:

```text
Client
 ↓
Uvicorn
 ↓
ASGI
 ↓
FastAPI
```

FastAPI applications commonly run using servers such as Uvicorn.

---

### 4. What is Uvicorn?

Uvicorn is an **ASGI server** used to run FastAPI applications.

For example:

```bash
uvicorn main:app --reload
```

Here:

```text
main → Python module
app  → FastAPI application object
```

So Uvicorn receives HTTP requests and passes them to FastAPI.

---

### 5. How do you create a basic FastAPI application?

```python
from fastapi import FastAPI

app = FastAPI()

@app.get("/hello")
def hello():
    return {"message": "Hello World"}
```

Run:

```bash
uvicorn main:app --reload
```

The `/hello` endpoint can then be accessed through HTTP GET.

---

### 6. What are GET, POST, PUT, PATCH and DELETE?

These are common HTTP methods.

| Method | Purpose                   |
| ------ | ------------------------- |
| GET    | Retrieve data             |
| POST   | Create/process data       |
| PUT    | Replace/update resource   |
| PATCH  | Partially update resource |
| DELETE | Delete resource           |

Example:

```python
@app.get("/users")
async def get_users():
    ...

@app.post("/users")
async def create_user():
    ...

@app.delete("/users/{user_id}")
async def delete_user(user_id: int):
    ...
```

---

### 7. How does FastAPI handle path parameters?

Example:

```python
@app.get("/users/{user_id}")
async def get_user(user_id: int):
    return {"user_id": user_id}
```

Request:

```text
GET /users/101
```

FastAPI extracts:

```text
user_id = 101
```

and validates it as an integer.

---

### 8. What is the difference between path parameter and query parameter?

### Path parameter

Part of the URL path:

```text
/users/101
```

```python
@app.get("/users/{user_id}")
async def get_user(user_id: int):
    ...
```

### Query parameter

Comes after `?`:

```text
/users?status=active
```

```python
@app.get("/users")
async def get_users(status: str):
    ...
```

**Simple rule:**

> Path parameters usually identify a resource; query parameters usually filter or modify the request.

---

### 9. How do you accept a request body in FastAPI?

Usually with a Pydantic model.

```python
from pydantic import BaseModel

class User(BaseModel):
    name: str
    age: int

@app.post("/users")
async def create_user(user: User):
    return user
```

Request:

```json
{
    "name": "Rishabh",
    "age": 25
}
```

FastAPI automatically validates the request.

---

### 10. What is Pydantic's role in FastAPI?

Pydantic provides **data validation and serialization using Python type hints**.

For example:

```python
class Transaction(BaseModel):
    transaction_id: str
    amount: float
    customer_id: str
```

FastAPI uses this model to validate incoming data.

Your fraud project specifically uses Pydantic models to validate transaction payloads and maintain consistent API contracts. 

---

# 🟡 MEDIUM LEVEL

## 11. What is dependency injection in FastAPI?

FastAPI provides dependency injection through `Depends()`.

Example:

```python
from fastapi import Depends

def get_database():
    return database

@app.get("/users")
async def get_users(db=Depends(get_database)):
    ...
```

FastAPI automatically calls `get_database()` and injects its result.

### Why useful?

For:

* Database connections
* Authentication
* Authorization
* Common validations
* Shared services

**Architect-level answer:**

> "Dependency injection helps decouple route handlers from infrastructure or cross-cutting concerns, making the application easier to test and maintain."

---

# 12. How would you implement authentication in FastAPI?

A common approach is JWT-based authentication.

Flow:

```text
Login
 ↓
Validate credentials
 ↓
Generate JWT
 ↓
Client stores token
 ↓
Client sends Authorization header
 ↓
FastAPI dependency validates token
 ↓
Allow/Deny request
```

Example header:

```text
Authorization: Bearer <token>
```

FastAPI dependencies can be used to implement authentication checks.

---

# 13. Authentication vs Authorization?

Very common interview question.

### Authentication

**Who are you?**

Example:

```text
Username + Password
JWT
OAuth
```

### Authorization

**What are you allowed to do?**

Example:

```text
Admin → Delete user
User → View profile
```

So:

> Authentication verifies identity, while authorization determines permissions.

---

# 14. How do you return different HTTP status codes?

You can use `status_code`.

```python
from fastapi import status

@app.post(
    "/users",
    status_code=status.HTTP_201_CREATED
)
async def create_user():
    ...
```

You can also use `JSONResponse` when you need more control.

---

# 15. How do you handle errors in FastAPI?

For explicit HTTP errors:

```python
from fastapi import HTTPException

if user is None:
    raise HTTPException(
        status_code=404,
        detail="User not found"
    )
```

For larger applications, I'd prefer:

```text
Custom Exception
       ↓
Global Exception Handler
       ↓
HTTP Response
```

This keeps business logic cleaner.

Your resume explicitly mentions handling validation failures, integration failures, invalid requests and application-level errors. 

---

# 16. What is middleware in FastAPI?

Middleware runs around the request/response lifecycle.

Conceptually:

```text
Request
   ↓
Middleware
   ↓
Route
   ↓
Business Logic
   ↓
Response
   ↓
Middleware
   ↓
Client
```

Typical uses:

* Logging
* Request IDs
* CORS
* Authentication-related processing
* Timing
* Headers

---

# 17. What is CORS?

CORS = **Cross-Origin Resource Sharing**.

Suppose your React application runs on:

```text
http://frontend.com
```

and FastAPI runs on:

```text
http://backend.com
```

The browser may block cross-origin requests unless the backend allows the frontend origin.

FastAPI can configure CORS middleware.

```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://frontend.com"],
    allow_methods=["*"],
    allow_headers=["*"],
)
```

---

# 18. What is response_model in FastAPI?

It defines the expected structure of the response.

```python
class UserResponse(BaseModel):
    id: int
    name: str

@app.get(
    "/users/{id}",
    response_model=UserResponse
)
async def get_user(id: int):
    ...
```

Benefits:

* Response validation
* Serialization
* Consistent API contracts
* Automatic documentation

---

# 19. What is the difference between request model and response model?

Request model:

```text
Client → API
```

Response model:

```text
API → Client
```

Example:

```python
class UserRequest(BaseModel):
    name: str
    password: str

class UserResponse(BaseModel):
    id: int
    name: str
```

Notice that the response doesn't contain the password.

**Important security benefit:**

> Response models help prevent accidentally exposing internal or sensitive fields.

---

# 20. How does FastAPI automatically generate Swagger documentation?

FastAPI automatically generates OpenAPI documentation from:

* Routes
* Type hints
* Pydantic models
* Parameters
* Response models

Usually available at:

```text
/docs
```

and:

```text
/redoc
```

This is useful for API testing and collaboration between frontend/backend teams.

---

# 🔥 HIGH-PROBABILITY PRACTICAL QUESTIONS

## 21. What is the difference between `def` and `async def` in FastAPI?

Example:

```python
@app.get("/users")
def get_users():
    ...
```

versus:

```python
@app.get("/users")
async def get_users():
    ...
```

`async def` allows asynchronous operations using:

```python
await
```

Example:

```python
@app.get("/users")
async def get_users():
    data = await external_api_call()
    return data
```

### Important:

Don't say:

> "async is always faster."

Better answer:

> "Async is beneficial mainly for I/O-bound workloads where the application spends time waiting for external operations."

---

# 22. Suppose your API needs to call 3 external APIs. How would you improve latency?

Suppose:

```text
API 1 → 500ms
API 2 → 500ms
API 3 → 500ms
```

If called sequentially:

```text
500 + 500 + 500
= 1500ms
```

If independent and called concurrently:

```python
results = await asyncio.gather(
    call_api_1(),
    call_api_2(),
    call_api_3()
)
```

The total latency can approach the slowest operation rather than the sum, subject to network/service overhead and concurrency limits.

**Important interviewer trap:**

If the calls depend on each other, you **cannot** blindly use `gather()`.

---

# 23. Does async FastAPI make CPU-intensive operations faster?

**No.**

Suppose you have:

```text
Large ML computation
Image processing
Complex mathematical calculation
```

These are CPU-bound.

Simply writing:

```python
async def
```

doesn't make them execute faster.

For CPU-intensive workloads, you might use:

* Multiple worker processes
* Background workers
* Dedicated ML service
* Task queues
* Optimized native libraries

This distinction is particularly relevant to your fraud project because the FastAPI service communicates with the risk/ML scoring component; async is useful primarily around the I/O communication rather than magically speeding up the ML computation. 

---

# 24. How would you structure a production FastAPI project?

I'd separate responsibilities.

```text
app/
│
├── main.py
├── api/
│   └── routes/
│
├── schemas/
│
├── services/
│
├── repositories/
│
├── models/
│
├── clients/
│
├── core/
│
└── exceptions/
```

Flow:

```text
Request
 ↓
Router
 ↓
Pydantic Validation
 ↓
Service
 ↓
Repository / External Client
 ↓
Database / External API
 ↓
Response Model
 ↓
Client
```

**Interview line:**

> "I prefer keeping route handlers thin and moving business logic into service classes/functions, database operations into repositories, and external integrations into dedicated clients."

This aligns very closely with the service separation described in your resume. 

---

# 25. How would FastAPI communicate with another microservice?

Suppose:

```text
Fraud Service
      ↓
Risk Scoring Service
```

The FastAPI service can call the downstream service using an HTTP client such as `httpx`.

Conceptually:

```python
async with httpx.AsyncClient() as client:
    response = await client.post(
        risk_service_url,
        json=transaction_data
    )
```

I'd also consider:

* Timeout
* Retry strategy
* Error handling
* Circuit breaker
* Authentication
* Logging
* Correlation/request ID

---

# 26. What happens if the downstream service is unavailable?

I wouldn't allow the request to hang indefinitely.

I'd use:

### Timeout

```text
Request
 ↓
Downstream
 ↓
Timeout
 ↓
Controlled failure
```

Then potentially:

```text
Retry → if appropriate
       ↓
Circuit breaker → if repeated failures
       ↓
Fallback / REVIEW / controlled error
```

For a fraud system, the fallback behavior should be determined by **business and risk requirements**, not arbitrarily coded.

That's a good Architect-level point.

---

# 27. How would you improve FastAPI API performance?

I'd first identify the bottleneck.

Then consider:

### Application

* Async I/O
* Efficient serialization
* Avoid unnecessary processing

### Database

* Indexing
* Query optimization
* Connection pooling
* Avoid N+1 queries

### External APIs

* Async clients
* Timeouts
* Connection reuse
* Appropriate concurrency

### Architecture

* Caching
* Horizontal scaling
* Load balancing
* Background processing where appropriate

### Infrastructure

* Multiple workers
* Containerization
* Monitoring

**Strong answer:**

> "I wouldn't optimize blindly. I'd first measure latency across each layer and then optimize the actual bottleneck."

---

# 28. How do you test FastAPI APIs?

I'd use `pytest` along with FastAPI's testing utilities / HTTP client approach.

Tests could cover:

```text
Unit Tests
   ↓
Service logic

API Tests
   ↓
Endpoints

Integration Tests
   ↓
API + DB / external integrations
```

Example:

```python
def test_get_user():
    response = client.get("/users/1")

    assert response.status_code == 200
```

Your resume specifically mentions PyTest, unit testing, API testing and integration testing. 

---

# 29. How would you secure a FastAPI application?

I'd consider multiple layers:

### Authentication

JWT/OAuth2 depending on requirements.

### Authorization

Role/permission checks.

### Input validation

Pydantic.

### Transport security

HTTPS/TLS.

### Secrets

Don't hardcode secrets.

Use environment variables / secret management.

### API security

* Rate limiting
* Proper CORS configuration
* Security headers
* Request size limits where appropriate
* Dependency vulnerabilities monitoring

### Logging

Never log sensitive information such as:

```text
passwords
tokens
PII unnecessarily
```

For your healthcare/banking-type systems, this is particularly important.

---

# 30. 🔥 Your FastAPI fraud API receives 1,000 requests/sec. What would you do?

This is the **most Architect-oriented question** in this list.

I would think at multiple levels.

### 1. Application

Keep endpoints efficient and use async for I/O-bound operations.

### 2. Database

Check:

```text
Indexes
Connection pool
Query performance
Read/write patterns
```

### 3. Downstream services

Check whether ML/risk service can handle the load.

### 4. Horizontal scaling

Deploy multiple FastAPI instances:

```text
                 Load Balancer
                 /     |     \
                /      |      \
          FastAPI  FastAPI  FastAPI
             |        |        |
             └────────┼────────┘
                      ↓
                 DB / Services
```

### 5. Caching

Cache data that is safe and appropriate to cache.

### 6. Observability

Monitor:

```text
Latency
Throughput
Error rate
CPU
Memory
DB latency
Downstream latency
```

### 7. Resilience

Use:

```text
Timeouts
Retries
Circuit breakers
Rate limiting
```

### Strong final answer:

> "I would not solve 1,000 requests per second simply by adding more FastAPI workers. I would first identify the bottleneck across the API, database, downstream ML service and infrastructure. Then I would combine efficient async I/O, connection pooling, horizontal scaling, appropriate caching and resilience patterns based on the actual bottleneck."

---

# 🔥 TOP 12 — Inko Sabse Pehle Prepare Karo

Agar time limited hai, **ye 12 pakka kar lo**:

1. **What is FastAPI and why FastAPI?**
2. **FastAPI vs Flask**
3. **ASGI vs WSGI**
4. **Uvicorn**
5. **Pydantic**
6. **Dependency Injection / Depends**
7. **Middleware**
8. **Exception handling**
9. **Async vs sync**
10. **Multiple API calls using asyncio**
11. **FastAPI microservice architecture**
12. **How to optimize a slow/high-traffic FastAPI API**

### ⚠️ Aur ek bahut important question jo almost certainly aa sakta hai:

> **"You have mentioned FastAPI and Flask on your resume. Why did you use FastAPI in one scenario and Flask in another? What are the differences?"**

Iska answer ready rakhna, because **tumhare resume mein dono explicitly listed hain**. 

Aur interviewer architect hai, isliye woh definition ke baad immediately **"Why?", "When?", "What happens if?", "How would you design it?"** type follow-ups karega. Is interview ke liye FastAPI ko **sirf framework ke features ki list ki tarah nahi, balki production architecture ke perspective se** prepare karna better rahega.
