Bahut achha sawal hai! Chaliye main **ek simple ASCII diagram** ke through aapko poori architecture samjhata hoon. 

Is diagram mein main **6 microservices** dikha raha hoon, unke **Databases**, **External APIs**, **Communication style (Sync/Async)**, aur har service ka **Rollback strategy**.

---

### 🗺️ System Architecture Diagram (Text-based)

```
                             [ Angular UI (Frontend) ]
                                      |
                                      | (HTTPS / REST API)
                                      v
                             [ API Gateway (Kong) ]
                                      |
                                      | (Route to Onboarding)
                                      v
              +----------------------------------------------------------------------------------+
              |           [1. ONBOARDING ORCHESTRATOR SERVICE] (Spring Boot)                    |
              |           (Heart of the system - manages State Machine & 6 steps)              |
              +------------------------------------+---------------------------------------------+
                       | (Feign Client - Sync)     | (Kafka - Async)    | (Feign - Sync)
                       v                           v                    v
          +-----------------------+    +-----------------------+    +-----------------------+
          | [2. IDENTITY VERIFI-  |    | [4. NOTIFICATION      |    | [5. IAM / CREDENTIAL  |
          |     CATION SERVICE]   |    |     SERVICE]          |    |     SERVICE]          |
          +-----------------------+    +-----------------------+    +-----------------------+
          | DB: PostgreSQL       |    | DB: MongoDB/PSQL     |    | DB: Keycloak DB      |
          | (id_db)              |    | (notification_logs)  |    | (user_store)         |
          +-----------------------+    +-----------------------+    +-----------------------+
                     |                         |                            |
                     | (Feign - Sync)           | (Feign - Sync)            | (Kafka - Async)
                     v                         v                            v
          +-----------------------+    +-----------------------+    +-----------------------+
          | [3. ADDRESS VALIDAT- |    | [External API:        |    | [6. AML BACKGROUND   |
          |     ION SERVICE]     |    |  Twilio/SMS Gateway]  |    |     SERVICE]          |
          +-----------------------+    +-----------------------+    +-----------------------+
          | DB: Redis/PSQL       |                              | DB: PostgreSQL        |
          | (address_cache)      |                              | (aml_db)              |
          +-----------------------+                              +-----------------------+
                     |                                                 |
                     | (Feign - Sync)                                 | (Kafka - Async)
                     v                                                 v
          +-----------------------+                         +-----------------------+
          | [External API:       |                         | [External API:        |
          |  Saudi Post /        |                         |  World-Check /        |
          |  National Address]   |                         |  Sanctions List API]  |
          +-----------------------+                         +-----------------------+
```

---

### 📋 Ab har service ka detail breakdown (Purpose, DB, Reads/Updates, External Calls, Rollback)

| Microservice | Purpose (Kya karti hai?) | External APIs (Kisko call karti hai?) | Communication (Kaise baat karti hai?) | Database (Kis DB se connect hai?) | Data Read/Update (Kya data read/update karti hai?) | Rollback Strategy (Agar fail ho to?) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Orchestrator Service** | User ke 6 steps ko control karti hai. Step counter maintain karti hai aur decide karti hai ki ab next kaunsa step aayega. | **Kisi ko nahi** (ye sirf baaki services ko call karta hai). | **Synchronous (REST/Feign)** with Service 2, 3, 5.<br>**Asynchronous (Kafka)** with Service 4 & 6. | PostgreSQL (`onboarding_db`) | **Read**: Current step, status.<br>**Update**: `current_step`, `status` (IN_PROGRESS to PENDING_KYC). | Agar kisi step mein fail ho, to ye **local DB transaction rollback** karta hai (current_step wapas purani value par aa jati hai). |
| **2. Identity Verification** | User ka National ID/Iqama scan karke OCR se data read karta hai, duplicate check karta hai, aur sensitive data ko encrypt karke store karta hai. | **OCR API** (image se text nikalne ke liye) <br>**(Optional) KYC Vendor API** (fraud check). | **Synchronous (REST/Feign)** with Orchestrator. | PostgreSQL (`identity_db`) | **Read**: Check duplicate `national_id_hash` (90 days).<br>**Update**: `encrypted_national_id`, `full_name`, `image_paths`. | Agar OCR fail ho ya duplicate ID mil jaye, to **DB rollback** (INSERT nahi hoga). External API call fail hui to bhi rollback. |
| **3. Address Validation** | User ke Saudi National Address ko Saudi Post ke database se verify karta hai. | **Saudi Post / National Address API** (building number aur postal code validate karta hai). | **Synchronous (REST/Feign)** with Orchestrator. | PostgreSQL (`address_db`) **+ Redis** (caching for quick lookups). | **Read**: City, district cache se fetch karta hai.<br>**Update**: `building_number`, `postal_code`, `validated_at`. | Agar Saudi Post API down hai ya timeout ho jata hai, to **DB rollback**. User Step 3 par hi rukta hai, aage nahi badhta. |
| **4. Notification Service** | OTP (SMS) bhejna, aur onboarding complete hone par welcome email bhejna. | **Twilio / SMS Gateway API**<br>**SendGrid / Email API**. | **Asynchronous (Kafka)** – Orchestrator Kafka par message daalta hai, ye service consume karti hai (taake UI slow na ho). | MongoDB or PostgreSQL (`notification_logs`) | **Read**: OTP hash verify karne ke liye (Redis mein).<br>**Update**: Logs store karta hai (SMS sent, email sent). | **Koi rollback nahi** hota (kyunki ye sirf notification hai). Agar SMS fail ho to retry hota hai, par onboarding process rukta nahi hai. |
| **5. IAM / Credential Service** | User ka username/password create karta hai aur login credentials generate karta hai. | **Keycloak / Internal IAM API** (user creation endpoint). | **Synchronous (REST/Feign)** with Orchestrator for creation.<br>**Asynchronous (Kafka)** for disabling user. | Keycloak's internal PostgreSQL (`user_store`) | **Read**: Check if username already exists.<br>**Update**: Creates new user entry (username, password hash). | **Sab se important rollback!** Agar Orchestrator ne IAM mein user create kar diya, lekin local DB update fail ho gaya, to ye **Compensating Transaction** trigger karta hai – Orchestrator IAM ko `DELETE /user` API call karta hai taake user delete ho jaye. |
| **6. AML Background Service** | Background mein AML (Anti-Money Laundering) check karta hai. Sanctions lists aur PEP (Politically Exposed Persons) check karta hai. | **World-Check / Refinitiv API**<br>**Government Sanctions List API**. | **Asynchronous (Kafka)** – Orchestrator Step 6 complete hone par Kafka message publish karta hai, ye consume karti hai. | PostgreSQL (`aml_db`) | **Read**: Fetches user data from Orchestrator DB (via Kafka payload).<br>**Update**: `aml_result` (PASS/FAIL/REVIEW). | Agar AML check FAIL ho jati hai (user blacklist mein hai), to ye service **Kafka** ke through Orchestrator ko wapas message bhejti hai. Orchestrator fir IAM Service ko **`DISABLE`** API call karta hai taake user login na kar sake. |

---

### 🔄 Internal Communication (Microservices aapas mein kaise baat karte hain?)

1. **Synchronous (REST/Feign Client)** – **For Steps 1 to 5 (User is waiting)**:
   - Orchestrator → Identity Service (ID verify karne ke liye).
   - Orchestrator → Address Service (Address validate karne ke liye).
   - Orchestrator → IAM Service (User create karne ke liye).
   - *Yeh real-time hota hai. Agar 3 seconds mein response nahi aata, to timeout ho jata hai aur rollback trigger ho jata hai.*

2. **Asynchronous (Kafka / RabbitMQ)** – **For heavy tasks & background jobs (User doesn't need to wait)**:
   - Orchestrator → Notification Service (OTP/SMS bhejne ke liye – taake UI hang na ho).
   - Orchestrator → AML Service (Final KYC check ke liye – isme 5-10 minutes lag sakte hain).
   - *Isme Orchestrator bus Kafka par message daal kar "Fire and Forget" kar deta hai. AML service result aane par wapas Kafka se reply bhejti hai.*

---

### 🧠 Summary: Kaunsa Data Kahan Update ho raha hai?

| Event | Service | Database Table | Action (Read/Update) |
| :--- | :--- | :--- | :--- |
| User enters Mobile | Orchestrator | `onboarding_session` | `UPDATE current_step = 2` |
| User verifies OTP | Orchestrator | `onboarding_mobile` | `UPDATE is_verified = TRUE` |
| User uploads ID | Identity Service | `identity_db.id_details` | `INSERT` encrypted ID + Hash |
| User enters Address | Address Service | `address_db.address` | `INSERT` city/postal_code |
| User clicks Submit | Orchestrator | `onboarding_session` | `UPDATE status = 'PENDING_KYC'` |
| IAM creates user | IAM Service | Keycloak `user_store` | `INSERT` username/password |
| Background AML runs | AML Service | `aml_db.aml_records` | `INSERT` KYC result (PASS/FAIL) |
| AML fails | Orchestrator (listener) | IAM Service | Calls IAM `DISABLE` API to block user |

---

Yeh design completely **Database-per-Service** pattern follow karta hai. Har microservice ki apni database hai, aur koi bhi doosri service directly doosre ki DB ko query nahi karti (sirf APIs ke through baat karti hai). Isliye agar Identity service down ho jaye, toh bhi Orchestrator ka DB safe hai.

Kya aap chahenge ke main is diagram ko **real Spring Boot annotations** (`@FeignClient`, `@KafkaListener`, `@Transactional`) ke saath code level par bhi explain karun?