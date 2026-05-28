SPRING CLOUD GATEWAY — INTERVIEW REVISION

1. What is Spring Cloud Gateway?

Spring Cloud Gateway is the single entry point in microservices architecture.

Client → Gateway → Microservices

It centralizes:

* routing
* security
* rate limiting
* logging
* monitoring

---

2. Routing in Gateway

Gateway routes requests to correct microservice.

Example:

/orders/** → ORDER-SERVICE
/payment/** → PAYMENT-SERVICE

Configuration:

spring:
cloud:
gateway:
routes:
- id: order-service
uri: lb://ORDER-SERVICE
predicates:
- Path=/orders/**

lb:// means:

* load balancing
* service discovery integration (Eureka)

---

3. Security in Gateway

Gateway usually handles:

* JWT validation
* token expiry check
* authentication
* centralized security

Flow:

Client Request
↓
JWT Token
↓
Gateway validates token
↓
If valid → forward request
Else → 401 Unauthorized

Technologies:

* Spring Security
* JWT
* OAuth2 Resource Server

---

4. Security in Microservices

Gateway handles authentication.

Microservices still handle:

* role-based authorization
* business security
* method-level security

Annotations used:

@EnableMethodSecurity
@PreAuthorize

Examples:

@PreAuthorize("hasRole('ADMIN')")

---

5. Rate Limiting

Usually implemented at Gateway.

Uses:

* Redis
* Token Bucket

Spring Cloud Gateway uses:

* RedisRateLimiter

---

6. Monitoring & Tracing

Gateway centralizes:

* logging
* metrics
* distributed tracing

Common tools:

* Prometheus
* Grafana
* Zipkin
* ELK Stack

---

7. MOST IMPORTANT INTERVIEW ANSWER

Spring Cloud Gateway acts as the single entry point in microservices architecture.

It is mainly used for routing, security, rate limiting, filtering, monitoring, and load balancing.

Gateway centrally handles JWT authentication and token validation using Spring Security, while microservices usually contain lightweight authorization logic using annotations like @PreAuthorize.

This approach centralizes cross-cutting concerns and keeps microservices simpler, secure, and easier to maintain.
