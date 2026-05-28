# Resilience4j Complete Interview Guide (Spring Boot)

# 1. What is Resilience4j?

Resilience4j is a fault-tolerance library used in Spring Boot microservices to make systems resilient against failures.

It helps prevent:

* Cascading failures
* Retry storms
* System overload
* Slow downstream services
* Complete application crashes

---

# 2. Real Problem Scenario

Suppose:

```text
Order Service → Payment Service
```

Now imagine Payment Service becomes:

* slow
* partially down
* overloaded
* intermittently failing

Without Resilience4j:

```text
Order Service
    ↓
Payment Service (slow/down)
    ↓
Threads get blocked
    ↓
More retries happen
    ↓
Entire system slows down
    ↓
Cascading failure
```

Resilience4j protects the application from this.

---

# 3. Main Modules in Resilience4j

| Feature         | Purpose                        |
| --------------- | ------------------------------ |
| Circuit Breaker | Stops calls to failing service |
| Retry           | Retries temporary failures     |
| Rate Limiter    | Limits requests                |
| Time Limiter    | Adds timeout                   |
| Bulkhead        | Isolates resources             |

---

# 4. Circuit Breaker (MOST IMPORTANT)

# Problem

If a service continuously fails:

```text
Service A → Service B
```

then repeatedly calling Service B wastes:

* threads
* CPU
* memory
* network resources

---

# Circuit Breaker Solution

After too many failures:

```text
Circuit Breaker opens
↓
Further requests blocked temporarily
↓
Fallback response returned
```

---

# Circuit Breaker Flow Diagram

```text
Client Request
      ↓
Order Service
      ↓
Circuit Breaker
      ↓
Payment Service
```

If Payment Service fails repeatedly:

```text
Client Request
      ↓
Order Service
      ↓
Circuit Breaker (OPEN)
      ↓
Fallback Response
```

---

# Circuit Breaker States

## 1. CLOSED

Normal state.

```text
Requests allowed
```

---

## 2. OPEN

Too many failures.

```text
Requests blocked
Fallback returned
```

---

## 3. HALF-OPEN

After wait duration:

```text
Allow few test requests
```

If success:

```text
HALF-OPEN → CLOSED
```

If failure:

```text
HALF-OPEN → OPEN
```

---

# 5. Retry

# Problem

Temporary failures happen.

Examples:

* temporary network issue
* DB timeout
* service restarting

---

# Retry Flow

```text
Request Failed
     ↓
Retry 1
     ↓
Retry 2
     ↓
Retry 3
     ↓
Still failed?
     ↓
Fallback/Error
```

---

# Retry Annotation

```java
@Retry(name = "paymentService", fallbackMethod = "fallbackMethod")
```

---

# 6. Rate Limiter

# Purpose

Controls number of requests.

Example:

```text
Only 10 requests/sec allowed
```

---

# Flow

```text
Incoming Requests
        ↓
Rate Limiter
        ↓
Limit exceeded?
   ↙           ↘
 Yes            No
 ↓               ↓
429          Allow Request
```

---

# Annotation

```java
@RateLimiter(name = "paymentService")
```

---

# 7. Time Limiter

# Problem

Service response taking too long.

---

# Flow

```text
Request Sent
     ↓
Waiting...
     ↓
Timeout reached?
   ↙         ↘
 Yes          No
 ↓             ↓
Timeout      Success
Exception
```

---

# Annotation

```java
@TimeLimiter(name = "paymentService")
```

Usually works with:

```java
CompletableFuture
```

---

# 8. Bulkhead

# Problem

One slow service consuming all threads.

Without Bulkhead:

```text
Slow Service
     ↓
All threads blocked
     ↓
Entire application impacted
```

---

# Bulkhead Solution

Separate thread pools/resources.

```text
Service A → Thread Pool A
Service B → Thread Pool B
```

If Service A fails:

```text
Service B still works
```

---

# Annotation

```java
@Bulkhead(name = "paymentService")
```

---

# 9. Fallback Method (VERY IMPORTANT)

Fallback executes when:

* retries exhausted
* circuit open
* timeout happens

---

# Example

```java
public String fallbackMethod(Exception ex) {
    return "Service temporarily unavailable";
}
```

---

# 10. Spring Boot Dependency

# Maven

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

---

# 11. Basic Spring Boot Example

```java
@RestController
public class PaymentController {

    @GetMapping("/payment")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
    public String payment() {

        // call external service
        return restTemplate.getForObject("http://payment-service/pay", String.class);
    }

    public String fallback(Exception ex) {
        return "Fallback response";
    }
}
```

---

# 12. application.yml Configuration

# Circuit Breaker Config

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        failureRateThreshold: 50
        minimumNumberOfCalls: 5
        slidingWindowSize: 10
        waitDurationInOpenState: 10s
```

---

# Meaning of Important Configurations

| Config                  | Meaning                          |
| ----------------------- | -------------------------------- |
| failureRateThreshold    | % failure before opening circuit |
| minimumNumberOfCalls    | minimum calls before calculation |
| slidingWindowSize       | number of requests tracked       |
| waitDurationInOpenState | how long OPEN state remains      |

---

# Retry Configuration

```yaml
resilience4j:
  retry:
    instances:
      paymentService:
        maxAttempts: 3
        waitDuration: 2s
```

---

# Rate Limiter Configuration

```yaml
resilience4j:
  ratelimiter:
    instances:
      paymentService:
        limitForPeriod: 5
        limitRefreshPeriod: 1s
        timeoutDuration: 0
```

---

# Time Limiter Configuration

```yaml
resilience4j:
  timelimiter:
    instances:
      paymentService:
        timeoutDuration: 2s
```

---

# Bulkhead Configuration

```yaml
resilience4j:
  bulkhead:
    instances:
      paymentService:
        maxConcurrentCalls: 5
```

---

# 13. Real Production Flow

```text
Client Request
      ↓
API Gateway
      ↓
Order Service
      ↓
Resilience4j Features
   ↓    ↓    ↓
Retry CircuitBreaker TimeLimiter
      ↓
Payment Service
```

---

# 14. Retry vs Circuit Breaker

| Retry                       | Circuit Breaker            |
| --------------------------- | -------------------------- |
| retries failed calls        | stops failed calls         |
| used for temporary failures | used for repeated failures |
| increases requests          | reduces requests           |

---

# 15. Bucket4j vs Resilience4j

| Bucket4j                      | Resilience4j                     |
| ----------------------------- | -------------------------------- |
| specialized for rate limiting | complete fault-tolerance library |
| token bucket only             | multiple features                |
| focused library               | broader resilience solution      |

---

# 16. Common Interview Questions

# Q1. Why use Circuit Breaker?

Answer:

To prevent cascading failures and avoid repeatedly calling failing downstream services.

---

# Q2. Why Retry and Circuit Breaker together?

Answer:

Retry handles temporary failures, while Circuit Breaker stops repeated failures from overloading the system.

---

# Q3. Why Bulkhead?

Answer:

To isolate resources so one failing service does not impact the entire application.

---

# Q4. Where is Resilience4j mostly used?

Answer:

In Spring Boot microservices with Feign Client, RestTemplate, or WebClient.

---

# 17. Final Interview Revision (MOST IMPORTANT)

Resilience4j is a fault-tolerance library used in Spring Boot microservices.

Main modules are:

* Circuit Breaker
* Retry
* Rate Limiter
* Time Limiter
* Bulkhead

Circuit Breaker is the most important feature. It prevents cascading failures by stopping calls to failing services temporarily.

Retry handles temporary failures.
Time Limiter handles slow responses.
Bulkhead isolates resources.
Rate Limiter controls request flow.

It is commonly integrated with Feign Client, RestTemplate, or WebClient in microservices architecture.
