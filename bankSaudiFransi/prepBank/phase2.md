Haan. **Ye new information important hai**, aur isse hum roadmap ko adjust karenge.

Lekin ek discipline maintain karenge: **"sab services individual microservices hain" ko ab working architecture assumption/fact maan kar आगे बढ़ेंगे**, क्योंकि tumhare paas project-level evidence hai. Saath hi, har service ke *exact architectural reason* ko hum evidence ke bina fabricate nahi karenge. Jahan reason documentation mein explicit nahi hai, main tumhe **architecturally defensible reasoning** sikhaunga aur clearly bolunga ki ye rationale hai, historical fact nahi.

Aur ek correction: **"har business step = microservice"** ko universal rule mat banana. Tumhare project mein agar documented/deployed decomposition aisa hai, toh hum **is project mein** step-to-service mapping ko use karenge.

---

# PHASE 2 — MICROSERVICE ARCHITECTURE & SERVICE OWNERSHIP

Ab tumhare project ko is model se samjho:

```text
                         CUSTOMER
                            │
                            ▼
                       Angular UI
                            │
                            ▼
                      API Gateway
                            │
                            ▼
                  Onboarding / Orchestrator
                            │
          ┌─────────────────┼──────────────────┐
          │                 │                  │
          ▼                 ▼                  ▼
    OTP/Mobile          Identity            Address
      Service           Service             Service
          │                 │                  │
          ▼                 ▼                  ▼
        Redis              OCR            Saudi Post /
                                          validation
          │                 │                  │
          └─────────────────┼──────────────────┘
                            │
                            ▼
                  Additional Information
                            │
                            ▼
                       IAM Service
                            │
                            ▼
                     KYC / AML Service
                            │
                            ▼
                     ACTIVE / REJECTED
```

**Important:** exact naming/topology ko tumhare source evidence ke terminology ke saath hi use karenge. Abhi diagram ka purpose ownership samajhna hai.

---

# 2.1 Sabse pehle: Microservice architecture ka fundamental idea

Tumhare project mein services ko separate karne ka objective ye nahi hai:

> "Har step ko alag kar do."

Actual idea:

> **Har service ek well-defined responsibility/capability own karti hai aur independently deploy/scale/fail/manage ki ja sakti hai.**

For example:

```text
OTP Service
    ↓
OTP-related capability

Identity Service
    ↓
Identity verification capability

Address Service
    ↓
Address verification capability

AML Service
    ↓
AML/compliance capability
```

Ye **separation of responsibility** hai.

---

# 2.2 OTP Service — why separate?

Tumne jo reasoning di, usme achha point hai:

> OTP frequently generate/verify ho sakta hai aur load fluctuate kar sakta hai.

Exactly.

Imagine:

```text
Normal traffic
     ↓
OTP requests = 1,000/min
```

Suddenly campaign:

```text
Peak traffic
     ↓
OTP requests = 10,000/min
```

Agar OTP capability onboarding application ke andar tightly coupled hoti:

```text
Onboarding
   ├── OTP
   ├── Identity
   ├── Address
   ├── ...
```

toh scaling karte waqt tum potentially **poore onboarding application ko scale** karoge.

Separate service:

```text
Onboarding
     │
     ▼
 OTP Service
   ┌─┼─┐
   ▼ ▼ ▼
 OTP1 OTP2 OTP3
```

Ab OTP capability ko independently scale kar sakte ho.

### But scalability alone isn't enough.

OTP service separate hone ke other possible reasons:

* high request frequency
* independent scaling
* security isolation
* rate limiting
* retry handling
* temporary data/cache management
* independent deployment

Tumhare project ke **documented historical reason** mein se kaunsa specifically decision driver tha, woh hum claim nahi karenge unless evidence hai.

---

# 2.3 Identity Verification Service

Tumhara reasoning:

> Identity verification multiple workflows mein reuse ho sakti hai.

### Architecturally: very strong reason.

For example:

```text
                 Identity Service
                 /      |       \
                /       |        \
               ↓        ↓         ↓
         Onboarding    Loan    Credit Card
```

Agar same identity verification capability multiple workflows ko chahiye, toh duplication avoid karne ke liye centralized service useful hai.

Additionally:

### Heavy processing

Identity verification mein:

```text
Document
   ↓
OCR
   ↓
Extraction
   ↓
Validation
   ↓
Verification
```

OCR/document processing CPU/network intensive ho sakta hai.

So independent scaling/failure isolation bhi reason ho sakta hai.

### Interview wording

Don't say:

> "We definitely created Identity Service because loan and credit-card workflows were using it."

Unless you've verified that actual fact.

Say:

> **"One architectural reason for separating identity verification is that it can be a reusable capability across multiple banking workflows and may have different scaling and processing characteristics from the rest of onboarding."**

---

# 2.4 Address Verification Service — ⭐ Important

Tumne specifically poocha tha:

> **Address ko separate microservice kyun rakha?**

Yahan third-party dependency important hai.

Suppose:

```text
Address Service
      │
      ▼
Saudi Post / external provider
```

Imagine external provider has:

* different API
* authentication
* rate limits
* timeout behavior
* response format
* retry requirements
* provider-specific errors

Agar ye logic directly onboarding service mein bhar diya:

```text
Onboarding Service
      │
      ├── onboarding logic
      ├── address logic
      ├── Saudi Post integration
      ├── retry logic
      ├── provider mapping
      └── provider credentials
```

Onboarding service ka coupling badhega.

Separate:

```text
Onboarding
     │
     ▼
Address Service
     │
     ▼
Saudi Post
```

Now **provider-specific complexity is isolated**.

This is a very good architectural rationale.

### Interview answer:

> **"Address verification was separated because it encapsulates a distinct business capability and external-provider integration. The address service can isolate provider-specific API handling, failures, retries and integration changes from the core onboarding workflow, and it can also be independently scaled or evolved if required."**

Again: **this is architectural reasoning**, not necessarily the documented historical ADR.

---

# 2.5 AML Service — Why separate?

AML is fundamentally a **compliance capability**.

It has its own:

* rules
* screening
* data processing
* external screening sources
* compliance decisions

Conceptually:

```text
Onboarding
     │
     │ Event
     ▼
   Kafka
     │
     ▼
 AML Service
     │
     ├── screening
     ├── rules
     └── decision
```

And importantly, AML doesn't necessarily need to block the original HTTP request.

That's why:

```text
Onboarding
    ↓
PENDING_KYC
    ↓
Kafka
    ↓
AML
    ↓
PASS / FAIL
```

is a natural architecture.

### Why separate?

Strong architectural reasons:

1. **Different business responsibility**
2. **Compliance domain isolation**
3. **Independent evolution of AML rules**
4. **Potentially different workload**
5. **Independent failure handling**
6. **Asynchronous processing is natural**
7. **Security/compliance boundary**

---

# 2.6 KYC Service — similar but don't blindly merge with AML

KYC and AML are related but aren't automatically identical.

Conceptually:

```text
KYC
↓
"Is this customer properly verified?"

AML
↓
"Does this customer present a compliance / financial-crime risk?"
```

In real enterprise systems they may be:

```text
KYC Service
AML Service
```

or:

```text
Compliance Service
   ├── KYC
   └── AML
```

Your project evidence needs to determine the exact implementation.

So **don't invent a separate KYC microservice just because the business has KYC.**

---

# 2.7 IAM Service

IAM is a particularly good candidate for a separate service because identity/access management is a **cross-cutting platform capability**.

Onboarding shouldn't own:

```text
User authentication
Credential management
Identity lifecycle
Access control
```

Instead:

```text
Onboarding
    │
    ▼
IAM / Keycloak
    │
    ▼
User credentials
```

This gives separation between:

```text
Business onboarding
        vs
Authentication / Identity Management
```

Also, IAM may be used by many applications—not just onboarding.

Again, if you say this in interview:

> "IAM is reusable across applications."

that's architectural reasoning unless you have direct project evidence.

---

# 2.8 Notification Service

This one is simpler.

Onboarding shouldn't necessarily contain:

```text
Email code
SMS code
Template management
Provider integration
Retry
Notification delivery
```

Instead:

```text
Onboarding
     ↓
Notification Service
     ↓
Email/SMS Provider
```

This isolates notification concerns from onboarding business logic.

And notification is a good candidate for asynchronous processing because:

> Sending an email generally shouldn't force the core business transaction to wait for the email provider.

---

# 2.9 Now understand the biggest benefit: Failure Isolation

Suppose Saudi Post is down.

Without separation:

```text
Onboarding
    ↓
Saudi Post DOWN
    ↓
Onboarding functionality heavily affected
```

With Address Service:

```text
Onboarding
    ↓
Address Service
    ↓
Saudi Post DOWN
```

Address Service can handle:

```text
timeout
retry
fallback
provider error
```

without mixing all provider-specific logic into onboarding.

**But:** separate service does not magically prevent failure from affecting the business workflow. It **isolates and localizes the failure**.

That's a subtle but important distinction.

---

# 2.10 Microservices introduce cost too

This is VERY important for architect interviews.

If you say:

> "Microservices are better because they are scalable."

Architect may ask:

> **"Then why not make 50 microservices?"**

Your answer:

Because microservices introduce distributed-system complexity:

```text
More services
   ↓
More network calls
   ↓
More latency
   ↓
More failure points
   ↓
More monitoring
   ↓
More deployments
   ↓
Distributed transactions
   ↓
Data consistency problems
```

And now you understand why our later topics are:

```text
REST/Feign
Kafka
Transactions
Saga
Compensation
Reconciliation
```

They are **consequences of distributed architecture.**

---

# 2.11 The most important concept: Service Boundary

Don't define a microservice as:

> "One API = one service."

Instead:

> **A service boundary should represent a meaningful capability/responsibility and define who owns its business logic and data.**

Example:

```text
                 ONBOARDING
                     │
        ┌────────────┼─────────────┐
        ▼            ▼             ▼
      Identity     Address        IAM
      Service      Service       Service
        │            │             │
     Identity DB   Address DB    IAM data
```

If that's the architecture you're working with, **database ownership becomes extremely important.**

---

# 2.12 And here's the question we were approaching earlier

Suppose:

```text
Identity Service
       ↓
identity_db
```

Then why did your onboarding documentation also show:

```text
onboarding_id_details
```

inside onboarding DB?

This is **not automatically wrong**.

Possible architectures include:

### Option A — authoritative identity data

```text
Identity DB
   ↓
Source of truth
```

Onboarding stores only:

```text
identity_id
verification_result
workflow metadata
```

### Option B — onboarding-specific snapshot

```text
Identity Service
      ↓
verification result
      ↓
Onboarding DB
      ↓
snapshot/reference needed for workflow
```

### Option C — duplicated mutable identity data

This is dangerous:

```text
Identity DB ←→ Onboarding DB
```

Both independently modifying the same business entity creates consistency problems.

**Which one your project actually used needs evidence.**

And this is why I don't want you memorizing:

> "onboarding_id_details is just metadata."

We **haven't proven that yet.**

---

# 🎯 Phase 2 ka core mental model

Tum ab ye samjho:

```text
                  CUSTOMER
                     ↓
              Onboarding Workflow
                     ↓
          ┌──────────┼──────────┐
          ↓          ↓          ↓
      Identity     Address      IAM
       Service      Service    Service
          │          │          │
          ↓          ↓          ↓
        OCR       Provider   Keycloak
```

And separately:

```text
                    Kafka
                     ↓
                 KYC / AML
                     ↓
               PASS / FAIL
```

Each service potentially has:

```text
Business responsibility
        +
Data ownership
        +
Independent deployment
        +
Independent scaling
        +
Failure boundary
```

---

# 🔥 Ab tumhara interview test

Ab main tumhe **architect-level question** deta hoon.

Suppose interviewer says:

> **"You have separate Identity, Address, OTP, IAM and AML microservices. Why didn't you simply put all of them inside one Onboarding Service? What concrete problems would you have if everything were in one service?"**

Is baar tum **microservice ke benefits ki list mat ratna**.

Try to explain with **your actual onboarding project**:

```text
OTP
Identity/OCR
Address/Saudi Post
IAM
AML/KYC
```

Think:

**coupling + scaling + failure isolation + independent ownership + deployment + async processing**

Apne words mein answer do.
