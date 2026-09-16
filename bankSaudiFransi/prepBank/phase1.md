Bilkul. **PHASE 1 — Architecture** start karte hain.

Phase 0 mein humne decide kiya tha:

> **Business step ≠ State ≠ Microservice**

Ab Phase 1 mein question hai:

> **"Customer ki request system ke andar travel kaise karti hai, aur kaunsa component kis responsibility ko handle karta hai?"**

Aur ek important rule: tumhare project documents mein architecture ke **2 conflicting views** hain. Main unko silently merge nahi karunga.

---

# PHASE 1 — SYSTEM ARCHITECTURE

## 1.1 Sabse pehle overall picture

Tumhare detailed onboarding/API/DB material se jo main flow samajh aata hai, usko conceptual level par:

```text
                         CUSTOMER
                            │
                            ▼
                    Angular Frontend
                            │
                            ▼
                       API Gateway
                            │
                            ▼
                  Onboarding Service
                            │
                  ┌─────────┴─────────┐
                  │                   │
                  ▼                   ▼
             PostgreSQL          External Systems
                  │                   │
                  │          ┌────────┼─────────┐
                  │          ▼        ▼         ▼
                  │        OCR     Address     IAM
                  │                 /Saudi
                  │                  Post
                  │
                  ▼
             Kafka Event
                  │
                  ▼
              KYC / AML
                  │
            ┌─────┴─────┐
            ▼           ▼
          ACTIVE      REJECTED
```

**Ye abhi high-level mental model hai.**

---

# 1.2 Har component ka role

Ab ek-ek box ko samjho.

## A. Angular Frontend

Customer directly backend DB ko access nahi karta.

Customer browser/app se onboarding UI use karta hai.

```text
Customer
   ↓
Angular
```

Angular ka responsibility:

* onboarding forms
* stepper UI
* customer input
* API calls
* state/UI handling
* route guards/interceptors as documented

### Important:

Angular **business authority nahi hai.**

Suppose Angular UI customer ko sirf Address screen dikha rahi hai.

Customer manually API call karke Credentials endpoint hit kar de, tab bhi backend ko request validate karni padegi.

Isliye:

> **Frontend controls user experience; backend controls business rules.**

🔥 Interview mein ye line useful hai.

---

# 1.3 API Gateway

Tumhare documents mein **Kong** gateway ka mention hai, although one source conflicts with it.

Conceptually gateway:

```text
Internet
   ↓
Gateway
   ↓
Backend
```

Gateway ka typical role ho sakta hai:

* external entry point
* routing
* authentication/security boundary
* rate limiting
* request policies

Lekin tumhare source mein har possible gateway responsibility conclusively documented nahi hai.

Therefore interview mein:

> "Kong is documented as the API gateway in the architecture."

Don't claim:

> "Kong definitely handled authentication, load balancing, TLS termination, etc."

unless you know that.

---

# 1.4 Onboarding Service — ⭐ VERY IMPORTANT

Ab architecture ka central component.

Tumhare detailed onboarding documentation mein **Onboarding Service** central application hai.

Iske andar documented hai:

```text
Onboarding Service
       │
       ├── REST APIs
       ├── State Machine
       ├── Business Logic
       ├── Transactions
       ├── PostgreSQL interaction
       ├── External API calls
       └── Kafka interaction
```

Isliye onboarding service ko tum:

> **"The central workflow/application responsible for coordinating the onboarding journey."**

ke form mein samajh sakte ho.

---

# 1.5 Orchestrator kya hai?

Yahan ek important architecture concept hai.

Documentation ke ek detailed view mein:

> **Orchestrator koi separately deployed service nahi hai; orchestration logic onboarding-service ke andar Spring State Machine ke through embedded hai.**

Conceptually:

```text
                  Onboarding Service
                         │
                ┌────────┴────────┐
                │                 │
                ▼                 ▼
          REST Controllers    Orchestrator
                                  │
                            State Machine
                                  │
                       ┌──────────┼─────────┐
                       ▼          ▼         ▼
                     ID       Address      IAM
```

So agar interviewer pooche:

> **"Did you have a separate orchestration microservice?"**

Tum blindly **yes** mat bolna.

Safer:

> **"In the detailed onboarding design, orchestration was embedded within the onboarding service using Spring State Machine rather than being a separately deployed orchestrator service."**

---

# 1.6 Orchestrator actually karta kya hai?

Suppose customer `S100` address verification complete kar raha hai.

Request:

```text
POST /step/address
```

Conceptually:

```text
Customer
   ↓
Angular
   ↓
Gateway
   ↓
Onboarding Service
   ↓
Orchestrator / State Machine
   ↓
Check current state
   ↓
Is address transition valid?
   ↓
Call address validation
   ↓
Save result
   ↓
Move to next state
```

So orchestration ka basic responsibility:

> **"Which step should happen, under what condition, and what state should the onboarding move to?"**

---

# 1.7 PostgreSQL ka role

Onboarding service ka documented local relational DB:

> **PostgreSQL**

Main onboarding data:

```text
onboarding_session
onboarding_mobile
onboarding_id_details
onboarding_address
onboarding_additional
audit_log
```

High-level relationship:

```text
                  onboarding_session
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
       mobile            ID          address
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                    additional

                         │
                         ▼
                     audit_log
```

**Database phase mein hum is diagram ko microscope se dekhenge.**

Abhi sirf responsibility samjho:

> PostgreSQL stores the persistent state and onboarding information managed by the onboarding application.

---

# 1.8 External systems

Onboarding application isolated world mein nahi hai.

Documentation mein several external systems/components hain:

### OCR

```text
Onboarding
    ↓
OCR
    ↓
Extract/validate identity information
```

### Address validation

```text
Onboarding
    ↓
Address validation
    ↓
Saudi Post / related validation
```

### IAM / Keycloak

```text
Onboarding
    ↓
IAM / Keycloak
    ↓
User / credential creation
```

### Kafka

```text
Onboarding
    ↓
Kafka
    ↓
KYC / AML processing
```

Ye distinction important hai:

> **Local DB transaction aur external system transaction ek hi transaction nahi hain.**

Isko hum Phase 7 onward mein deeply dekhenge.

---

# 1.9 Synchronous communication

Documentation mein Feign-based synchronous communication described hai.

Example:

```text
Onboarding
     │
     │ HTTP / Feign
     ▼
Identity / External API
     │
     │ response
     ▼
Onboarding
```

Meaning:

> Onboarding request response ka wait karti hai.

For example OCR:

```text
Upload ID
    ↓
Call OCR
    ↓
Wait
    ↓
OCR response
    ↓
Continue workflow
```

Why synchronous?

Because next step ko response ki zarurat ho sakti hai.

---

# 1.10 Asynchronous communication

KYC/AML processing ke liye Kafka documented hai.

```text
Onboarding
     │
     │ publish event
     ▼
   Kafka
     │
     ▼
 KYC / AML
```

Onboarding ko necessarily KYC processing complete hone tak same HTTP request open nahi rakhna padta.

Instead:

```text
Onboarding
    ↓
PENDING_KYC
    ↓
Event
    ↓
Kafka
    ↓
KYC/AML
    ↓
Result
    ↓
ACTIVE / REJECTED
```

Ye **eventual consistency** ki taraf le jaata hai.

---

# 1.11 Ab sabse important architecture principle

Tumhare system mein **sab kuch synchronous nahi hai.**

Think:

```text
             ONBOARDING
                  │
       ┌──────────┴──────────┐
       │                     │
       ▼                     ▼
   Synchronous           Asynchronous
       │                     │
       ▼                     ▼
 Feign / REST              Kafka
       │                     │
       ▼                     ▼
 Need response          Process later
 immediately
```

Ye decision random nahi hota.

Basic reasoning:

### Synchronous

Use when:

> **Current step cannot proceed without the response.**

### Asynchronous

Use when:

> **Processing can happen independently after the current transaction/workflow point.**

---

# 1.12 Ab architecture conflict

Ab ye **bahut important** hai.

Tumhare documents mein ek higher-level service map hai:

```text
Onboarding
Identity
Address
Notification
IAM
AML
```

and it describes these as separate services.

But detailed onboarding/API/DB documents centralize much of the flow around:

```text
Onboarding Service
```

and its embedded orchestration.

Therefore:

### ❌ Don't memorize:

> "We definitely had six microservices."

### ❌ Don't memorize:

> "Identity was definitely inside onboarding service."

### Instead:

> **"The documentation has two architecture views: a detailed onboarding-centric view and a higher-level service decomposition. The exact deployed topology isn't conclusively established by the available source material."**

This is now part of your **interview safety layer**.

---

# 1.13 Why architecture matters for DB?

This is where everything starts connecting.

Suppose `S100` is being onboarded.

Request comes:

```text
Angular
  ↓
Gateway
  ↓
Onboarding Service
  ↓
PostgreSQL
```

Then suddenly:

```text
Onboarding Service
      ↓
External IAM
```

Now one business operation involves **two systems**:

```text
PostgreSQL
     +
IAM
```

And then:

```text
PostgreSQL
     +
Kafka
     +
KYC/AML
```

And THAT is why later we need to learn:

```text
Transaction
    ↓
Rollback
    ↓
Saga
    ↓
Compensation
    ↓
Kafka consistency
    ↓
Reconciliation
```

So architecture is not something separate from DB.

**Architecture explains why the DB problems become distributed-system problems.**

---

# 1.14 `S100` ka complete architectural journey

Let's follow one customer.

```text
                    Customer S100
                         │
                         ▼
                    Angular UI
                         │
                         ▼
                       Kong
                         │
                         ▼
                 Onboarding Service
                         │
             ┌───────────┴────────────┐
             │                        │
             ▼                        ▼
        State Machine             PostgreSQL
             │                        │
             │                 onboarding_session
             │                        │
             ▼                        ▼
      Business Steps              Child tables
             │
       ┌─────┼──────┐
       ▼     ▼      ▼
     OTP    OCR   Address
       │     │      │
       └─────┼──────┘
             │
             ▼
       Credential Creation
             │
             ▼
        IAM / Keycloak
             │
             ▼
        PENDING_KYC
             │
             ▼
           Kafka
             │
             ▼
         KYC / AML
          /       \
       PASS       FAIL
        │           │
        ▼           ▼
      ACTIVE     REJECTED
```

**Ab tumhara project ek interconnected system ki tarah dikhna chahiye, alag-alag technologies ki list ki tarah nahi.**

---

# 🎯 Phase 1 ka interview answer

Agar interviewer bole:

> **"Explain the architecture of your onboarding application."**

Tum roughly:

> "It's a digital customer onboarding platform where the customer interacts through an Angular frontend. Requests go through the API gateway to the onboarding backend. The onboarding service is responsible for coordinating the workflow, with a state-machine-based orchestration model documented for the onboarding flow. PostgreSQL stores the persistent onboarding session and related data.
>
> During the journey, the onboarding service interacts with external capabilities such as identity/OCR, address validation and IAM. Some interactions are synchronous, where the next step requires an immediate response, while the KYC/AML part is documented as asynchronous using Kafka. The onboarding eventually moves from a pending state to an active or rejected outcome based on the compliance result."

**Is answer mein unnecessary service-count claim nahi hai.**

---

# 🧠 Phase 1 ke 7 things yaad rakho

```text
1. Angular
   → Customer-facing UI

2. Gateway
   → Entry/routing layer

3. Onboarding Service
   → Central onboarding workflow/application

4. State Machine
   → Workflow transitions

5. PostgreSQL
   → Persistent onboarding data/state

6. Feign/REST
   → Synchronous communication where response is needed

7. Kafka
   → Asynchronous KYC/AML processing
```

---

## 🎤 Ab tumhara Phase-1 test

Imagine interviewer suddenly asks:

> **"Why didn't you make the entire onboarding process asynchronous using Kafka? Why do you need synchronous REST/Feign calls at all?"**

Iska answer **apni understanding se** do.

Hint:

```text
Mobile → ?
OCR → ?
Address validation → ?
KYC/AML → ?
```

Har jagah socho:

> **"Kya current step ko next step par jaane ke liye immediately response chahiye?"**

Tum answer do. Phir main tumhe architect-level counter-question karunga.
