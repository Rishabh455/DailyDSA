# PHASE 0 — Project Overview 🏦

Bilkul basic se start karte hain. **Abhi DB, Kafka, Saga, locking kuch deep mein nahi jayenge.** Pehle tumhare dimaag mein poore system ka ek clear picture hona chahiye.

Main jo bata raha hoon woh tumhare project documents ke basis par hai. Jahan documents mein conflict/ambiguity hai, main explicitly bataunga.

---

## 0.1 Sabse pehle — ye project karta kya hai?

Tumhara project ek **digital customer onboarding system** hai.

Simple language mein:

> Jab koi customer bank ke saath digitally onboard hona chahta hai, system uski information collect karta hai, mobile verify karta hai, identity verify/OCR karta hai, address verify karta hai, additional information leta hai, credentials create karta hai, aur finally KYC/AML processing ke baad customer ko **ACTIVE** ya **REJECTED** state mein le jaata hai.

Overall journey:

```text
Customer
   │
   ▼
Angular Frontend
   │
   ▼
API Gateway
   │
   ▼
Onboarding Backend
   │
   ├── Mobile Verification
   │
   ├── ID Verification / OCR
   │
   ├── Address Verification
   │
   ├── Additional Information
   │
   ├── Credential Creation
   │
   └── KYC / AML
            │
            ▼
       ACTIVE / REJECTED
```

**Ye diagram abhi tumhara mental starting point hai.**

---

# 0.2 Customer ko exactly kya experience hota hai?

Imagine main customer hoon.

Main onboarding start karta hoon.

### Step 1 — Initiate

Customer onboarding start karta hai.

System ek onboarding session create karta hai.

Conceptually:

```text
Customer
   ↓
"I want to open/onboard"
   ↓
Create onboarding session
   ↓
sessionId
```

Database mein `onboarding_session` parent record banta hai.

---

### Step 2 — Mobile Verification

Customer mobile number deta hai.

```text
Mobile Number
      ↓
OTP
      ↓
Customer enters OTP
      ↓
Verify
```

Redis OTP-related temporary data ke liye documented hai.

Successful verification ke baad onboarding next state par move karta hai.

---

### Step 3 — ID Verification

Customer identity document deta hai.

For example:

```text
National ID / Identity Document
             ↓
          Upload
             ↓
             S3
             ↓
            OCR
             ↓
     Identity information
```

OCR se information extract/verify hoti hai.

Yahan duplicate identity checking bhi important business concern hai.

---

### Step 4 — Address Verification

Customer address deta hai.

Address validation ke liye documented external/service interaction hai, including Saudi Post-related validation.

Conceptually:

```text
Customer Address
       ↓
Address Service / Validation
       ↓
Valid?
   /       \
 Yes       No
 ↓          ↓
Next      Reject/Error
```

---

### Step 5 — Additional Information

Customer additional information submit karta hai.

Ismein financial/additional onboarding information documented hai.

```text
Additional Information
          ↓
Database
```

---

### Step 6 — Credentials

Onboarding ke later stage mein credentials/user creation hoti hai.

Documentation mein IAM/Keycloak involved hai.

Conceptually:

```text
Onboarding
    ↓
Create credentials/user
    ↓
IAM / Keycloak
```

Yahan se system distributed consistency problems face kar sakta hai, because ab sirf apna DB nahi, external system bhi involved hai.

**Isko hum Phase 7–11 mein deeply karenge.**

---

### Step 7 — KYC / AML

Credentials ke baad customer KYC/AML processing ke liye jaata hai.

Documentation mein Kafka-based asynchronous processing documented hai.

Conceptually:

```text
Onboarding
     │
     ▼
Kafka Event
     │
     ▼
KYC / AML processing
     │
     ├──────────┐
     ▼          ▼
   PASS       FAIL
     │          │
     ▼          ▼
  ACTIVE     REJECTED
```

---

# 0.3 Sabse important concept — ONBOARDING SESSION

Abhi sirf ek cheez yaad rakho:

> **`onboarding_session` poore onboarding journey ka central record hai.**

Suppose customer ka session:

```text
session_id = S100
```

To system us session ke around information maintain karta hai.

Conceptually:

```text
                  S100
                   │
          onboarding_session
                   │
       ┌───────────┼───────────┐
       │           │           │
       ▼           ▼           ▼
    Mobile        ID         Address
       │           │           │
       └───────────┼───────────┘
                   │
                   ▼
              Additional
                   │
                   ▼
               Audit Log
```

Later hum DB phase mein iska exact schema dekhenge.

---

# 0.4 System ko kaise pata hai customer kis stage par hai?

Yahan aata hai:

```text
current_step
```

Aur:

```text
status
```

Ye dono **same cheez nahi hain.**

### `current_step`

Customer onboarding journey mein **kahan tak pahucha hai**.

Example conceptually:

```text
current_step = 1
→ Mobile

current_step = 2/3
→ ID stage

current_step = 4
→ Address

...
```

Tumhare documents mein exact step numbering mein kuch inconsistency hai, isliye **abhi numbers memorize mat karo**.

Concept important hai:

> `current_step` = workflow position.

---

### `status`

Overall onboarding ki condition.

Examples documented:

```text
IN_PROGRESS
PENDING_KYC
ACTIVE
REJECTED
EXPIRED
```

So:

```text
current_step → workflow mein kahan hoon?

status       → overall onboarding ki condition kya hai?
```

**Architect ye difference pooch sakta hai.**

---

# 0.5 State Machine kyu hai?

Tumhare documentation mein **Spring State Machine** documented hai.

Iska basic purpose:

> Customer ko arbitrary step par jump karne se rokna aur valid state transitions control karna.

Without state machine:

```text
Mobile
  ↓
Address
  ↓
Credentials
```

Customer theoretically ID verification skip karne ki request kar sakta hai.

State machine conceptually bolti hai:

```text
INITIATED
    │
    ▼
MOBILE_VERIFIED
    │
    ▼
ID_VERIFIED
    │
    ▼
ADDRESS_VERIFIED
    │
    ▼
INFO_SUBMITTED
    │
    ▼
CREDENTIALS_CREATED
    │
    ▼
KYC_PENDING
    │
    ├──── PASS ────► ACTIVE
    │
    └──── FAIL ────► REJECTED
```

So interview mein:

> **Why State Machine?**

Tumhara basic answer:

> "Because onboarding is a state-driven workflow. Each step has valid transitions and we need to prevent invalid transitions or skipping steps. The state machine gives us centralized control over the onboarding lifecycle."

Ye answer **abhi enough hai**.

Baad mein interviewer pooche:

> Why not just use `current_step` in DB?

Tab hum detailed answer prepare karenge.

---

# 0.6 Architecture ka basic picture

Tumhare documentation mein architecture ke regarding **conflict hai**.

Ye bahut important hai.

One set of documents describes:

```text
Angular
   ↓
Kong
   ↓
Onboarding Service
   ↓
Embedded Orchestrator / State Machine
   ↓
External Services
```

Another document describes multiple discrete services:

```text
Onboarding
Identity
Address
Notification
IAM
AML
```

with database-per-service.

### Isliye abhi interview mein kya nahi karna?

Ye mat bolna:

> "Our production architecture definitely had six microservices."

because current source evidence conclusively establish nahi karta.

Instead safe statement:

> "The documentation contains two architecture views. The detailed onboarding/API/DB flow is centered around the onboarding service with an embedded orchestration/state-machine flow, while a higher-level service map describes separate Identity, Address, Notification, IAM and AML services. I would distinguish those views rather than claim deployment topology without source-code or deployment evidence."

**Ye architect-level honesty hai.**

---

# 0.7 Synchronous vs Asynchronous — abhi sirf concept

Tumhare system mein dono patterns hain.

### Synchronous

Example:

```text
Onboarding
    │
    │ Feign / REST
    ▼
External Service
    │
    │ response
    ▼
Onboarding
```

Matlab:

> "Mujhe response abhi chahiye."

---

### Asynchronous

Example:

```text
Onboarding
    │
    ▼
Kafka
    │
    ▼
Consumer
    │
    ▼
KYC / AML processing
```

Matlab:

> "Main event publish kar raha hoon; processing baad mein ho sakti hai."

---

# 0.8 Ek real example — S100

Ab ek imaginary customer ko `S100` maan lo.

Journey:

```text
S100
 │
 ├── Session Created
 │
 ├── Mobile Verified
 │
 ├── ID Verified
 │
 ├── Address Verified
 │
 ├── Additional Info Submitted
 │
 ├── Credentials Created
 │
 ├── KYC Pending
 │
 └── ACTIVE
```

Agar AML/KYC fail ho:

```text
S100
 │
 ├── ...
 │
 ├── KYC Pending
 │
 └── REJECTED
```

Ab ye `S100` tumhare liye **mental anchor** banega.

Baad mein hum isi customer ko database ke andar follow karenge:

```text
S100
 ↓
INSERT
 ↓
UPDATE
 ↓
SELECT
 ↓
LOCK
 ↓
TRANSACTION
 ↓
Feign
 ↓
Kafka
 ↓
COMMIT / ROLLBACK
 ↓
COMPENSATION
 ↓
ACTIVE / REJECTED
```

**Ye exactly woh journey hai jo tum interview mein explain kar paoge.**

---

# 0.9 Abhi tumhe sirf ye 8 cheezein yaad honi chahiye

Aaj ke Phase 0 ke baad tumhare mind mein ye picture honi chahiye:

```text
1. Project = Digital Customer Onboarding

2. Customer onboarding multiple steps mein hoti hai

3. onboarding_session = central onboarding record

4. current_step = workflow position

5. status = overall lifecycle status

6. State Machine = valid state transitions control

7. Some interactions are synchronous, some asynchronous

8. Final outcome = ACTIVE / REJECTED
```

**Bas. Isse zyada Phase 0 mein memorize nahi karna.**

---

# 🎤 Ab Interview Mode

Ab main interviewer hoon.

### Question 1 — Easy

**"Rishabh, can you explain the banking onboarding project you worked on, from the customer's request to final onboarding completion?"**

Tum **apne words mein answer do**.

Don't look at the document.

2–3 minute ka answer dene ki zarurat nahi abhi. **Jo naturally yaad hai, woh bolo.**

Main tumhare answer ko line-by-line evaluate karunga:

* kya correct hai
* kya missing hai
* kya overclaim hai
* architect kahan counter-question karega
* aur phir tumhara **interview-ready version** banayenge.
