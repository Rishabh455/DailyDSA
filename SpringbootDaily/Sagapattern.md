# Saga Design Pattern — Interview Notes (3 Years Java Developer)
Compensation vs Rollback

Very important line:

“In distributed systems, true rollback across multiple databases is not possible. Saga uses compensating transactions to undo previously completed operations.”

## What Problem Does Saga Solve?

> “Saga pattern solves the distributed transaction problem in microservices.”

Example:

* Order Service
* Payment Service
* Delivery Service

If payment succeeds but delivery fails, we cannot do a normal DB rollback because every microservice has its own database.

So Saga uses:

* Local transactions
* Compensating transactions (undo actions)

instead of one global rollback. 

---

# Simple Definition

> “Saga is a sequence of local transactions where each microservice completes its own transaction and publishes an event to trigger the next service. If any step fails, compensating transactions are triggered to maintain consistency.” 

---

# Easy Flow to Explain

```text
1. Order created
2. Payment processed
3. Delivery assigned
4. Order completed
```

Failure Case:

```text
1. Order created
2. Payment success
3. Delivery failed
4. Compensation triggered
5. Payment refunded
6. Order cancelled
```

---

# Important Interview Keyword

## Compensating Transaction

> “In Saga, we do not perform actual rollback. Instead, we use compensating transactions to reverse previous successful operations.”

Example:

* Refund payment
* Cancel order

---

# Two Types of Saga

## 1. Choreography Saga

### Flow

* Services communicate using events/message broker.
* No central controller.

Example:

* Kafka
* RabbitMQ

### Important Point

> “Each microservice listens to events and triggers the next event.”

### Advantages

* Simple
* Loosely coupled
* No central dependency

### Disadvantages

* Hard to track flow
* Difficult debugging
* Cyclic dependency risk

---

# 2. Orchestration Saga

### Flow

* One central orchestrator controls all services.

Example:

* Orchestrator service
* Camunda
* Temporal

### Important Point

> “Orchestrator manages the complete workflow and triggers each microservice step-by-step.”

### Advantages

* Better control
* Easy monitoring
* Easier debugging

### Disadvantages

* Single point of failure
* Extra complexity

---

# Most Important Difference

| Choreography    | Orchestration      |
| --------------- | ------------------ |
| Event-driven    | Central controller |
| No orchestrator | Has orchestrator   |
| Hard to debug   | Easier tracking    |
| Loosely coupled | More controlled    |

---

# Real Interview Answer (Best)

> “Saga pattern is used in microservices to handle distributed transactions. Since each microservice has its own database, we cannot use a single ACID transaction across services. Saga solves this using local transactions and compensating transactions. If one service fails, previously completed operations are reverted using compensation logic like refunding payment or cancelling order. Saga can be implemented using Choreography with Kafka/RabbitMQ or using a centralized Orchestrator.” 

---

# Important Technologies to Mention

```text
Kafka
RabbitMQ
Spring Boot
Microservices
Event-driven architecture
REST APIs
Distributed Transactions
```

---

# One-Liner Difference Between Rollback & Compensation

> “In distributed systems, actual rollback is not possible across multiple databases, so Saga uses compensating transactions instead of database rollback.”

---

# Most Important Interview Keywords

```text
Distributed Transaction
Local Transaction
Compensating Transaction
Event-Driven
Kafka
RabbitMQ
Orchestrator
Choreography
Eventually Consistent
```
