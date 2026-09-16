This is an excellent system design question. Since we have already established that this is a microservices-based ecosystem, I will design the **Onboarding Module** as a standalone, dedicated microservice (let's call it the `onboarding-service`) using **Java Spring Boot** for the backend, **PostgreSQL** as the primary database, and **Angular** for the frontend. 

Here is my **careful, step-by-step technical design** for this module:

---

### 🏗️ 1. High-Level Architecture Overview

- **Pattern**: **Orchestrator-based Saga** pattern. We will not let the frontend manage the complex 6-step flow. Instead, an Orchestrator Service on the backend will manage the state, decide which step is next, and handle failures gracefully.
- **Communication**: RESTful APIs (JSON) between Angular and Spring Boot. 
- **Asynchronous Processing**: Final KYC verification will be asynchronous to keep the UI responsive.

---

### 🖥️ 2. Backend Design (Java Spring Boot)

**A. Project Structure (Hexagonal/Onion Architecture)**:

- `controller` – REST endpoints.
- `service` – Business logic & Orchestrator.
- `repository` – JPA/Hibernate for PostgreSQL.
- `dto` – Data Transfer Objects (to communicate with Angular).
- `entity` – JPA entities for DB tables.
- `config` – Security, State Machine, and Bean configurations.
- `client` – Feign Clients to call external APIs (e.g., Saudi National Address API, government KYC, OTP SMS gateway).

**B. Core Component: The Orchestrator & State Machine**

Instead of using basic `if-else` logic, I will implement **Spring State Machine** to manage the 6 steps. 

- **States**: `INITIATED`, `MOBILE_VERIFIED`, `ID_VERIFIED`, `ADDRESS_VERIFIED`, `INFO_SUBMITTED`, `CREDENTIALS_CREATED`, `KYC_PENDING`, `ACTIVE`, or `REJECTED`.
- **Events**: `VERIFY_MOBILE`, `VERIFY_ID`, `VERIFY_ADDRESS`, `SUBMIT_INFO`, `CREATE_CREDENTIALS`.
- *Why?* It ensures that a user cannot skip from Step 1 directly to Step 5. The state machine strictly guards the transitions.

**C. REST API Endpoints (Designed for Angular)**

I will design a **"Session-based"** API because the user hasn't created credentials yet for the first 5 steps.

| Method | Endpoint | Purpose |
| :--- | :--- | :--- |
| `POST` | `/api/onboarding/initiate` | Creates a new onboarding session (UUID). Returns `sessionId`. |
| `GET` | `/api/onboarding/status/{sessionId}` | Fetches the current step and previously saved data to restore UI on refresh. |
| `POST` | `/api/onboarding/step/mobile` | Validates mobile, generates OTP, stores hashed OTP in Redis. |
| `POST` | `/api/onboarding/step/id-verification` | Accepts MultipartFile (images) + ID number. Calls OCR service, validates data, encrypts PII. |
| `POST` | `/api/onboarding/step/address` | Validates the National Address via external government API. |
| `POST` | `/api/onboarding/step/additional` | Saves employment/income details. |
| `POST` | `/api/onboarding/step/credentials` | Creates username/password (sends to internal IAM/Keycloak). Triggers async final KYC. |
| `GET` | `/api/onboarding/kyc-status/{sessionId}` | Polling endpoint for frontend to check if final background check passed. |

**D. Service Layer Logic (Java)**

- **Idempotency**: Every endpoint checks the `sessionId` against the database. If Step 2 is already `COMPLETED`, and the client accidentally calls Step 2 again, the service returns a `200 OK` with the existing data instead of throwing an error.
- **Transactional Integrity**: I will use `@Transactional` on Step 3 (ID verification). If the OCR API fails, the entire database update for that step is rolled back, so no partial data is saved.

---

### 🗄️ 3. Database Design (PostgreSQL)

I will use a **normalized schema** with a primary `onboarding_session` table and child tables for each step. 

**Critical Security**: All Personally Identifiable Information (PII) like `id_number`, `full_name`, and `mobile_number` will be encrypted at the application level using **AES-256** (with the key stored in a secure vault like HashiCorp Vault, *not* in the source code) before persisting to PostgreSQL.

**Tables**:

1.  **`onboarding_session`** (Main table)
    - `id` (UUID, Primary Key) -> This is the `sessionId`.
    - `current_step` (INT, 1 to 6).
    - `status` (VARCHAR: e.g., 'IN_PROGRESS', 'PENDING_KYC', 'ACTIVE', 'FAILED').
    - `id_type` (VARCHAR: NATIONAL/IQAMA/VISITOR).
    - `created_at`, `updated_at` (TIMESTAMP).
    - *Indexed on*: `id` (obviously), `status`.

2.  **`onboarding_id_details`** (Step 2)
    - `session_id` (FK to `onboarding_session.id`).
    - `encrypted_id_number` (TEXT).
    - `encrypted_full_name` (TEXT).
    - `date_of_birth` (DATE).
    - `front_image_url` (TEXT -> S3 bucket path).
    - `back_image_url` (TEXT).

3.  **`onboarding_mobile`** (Step 1)
    - `session_id` (FK).
    - `encrypted_mobile_number` (TEXT).
    - `is_verified` (BOOLEAN).
    - (OTP is stored in Redis with a 5-minute TTL, *not* in PostgreSQL).

4.  **`onboarding_address`** (Step 3)
    - `session_id` (FK).
    - `building_number`, `street`, `district`, `city`, `postal_code`.

5.  **`onboarding_additional`** (Step 4)
    - `session_id` (FK).
    - `employment_status`, `annual_income`, `source_of_funds`.

6.  **`audit_log`** (Separate table for compliance)
    - Logs every API call, IP address, User-Agent, and timestamp. This is mandatory for banking compliance (SAMA).

---

### 💻 4. Frontend Design (Angular 16+)

**A. Architecture & State Management**

- I will use **Standalone Components** (to avoid NgModules bloat).
- **State Management**: I will use **NgRx Component Store** or a simple **RxJS BehaviorSubject** service (`OnboardingStateService`) to cache the session data locally. This prevents hitting the backend for every small UI toggle.

**B. UI/UX Implementation**

- **Parent Component**: `OnboardingContainerComponent` - hosts the Angular Material Stepper.
- **Child Components** (Lazy-loaded inside the stepper):
  - `StepMobileComponent` (with input mask for Saudi numbers + OTP countdown timer).
  - `StepIdVerificationComponent` (uses Angular File Uploader with drag-and-drop, image preview, and automatic compression to under 5MB before uploading).
  - `StepAddressComponent` (Auto-suggest dropdown integrated with the Saudi National Address API).
  - `StepAdditionalComponent` (Reactive forms with dropdowns and validators).
  - `StepCredentialsComponent` (Password strength meter).
- **Route Guard**: I will implement a custom `OnboardingGuard` that fetches the session status from the backend. If the user manually types `/onboarding/step-4` in the URL, but the backend says they are only on Step 2, the Guard redirects them back to Step 2.

**C. API Integration (Services)**

- Create `OnboardingApiService` in Angular to handle HTTP interceptors.
- **Interceptors**: Add a `sessionId` header to every request automatically after Step 1.
- **Error Handling**: Global error handler that catches HTTP 4xx/5xx errors and maps them to user-friendly Arabic/English toast notifications based on the error code sent by Spring Boot.

---

### 🔄 5. The End-to-End Communication Flow (How they connect)

Let's walk through the user journey:

1. **Initiation**: User clicks "Continue" on the landing page. Angular calls `POST /initiate`. Spring Boot creates a UUID and saves `status='IN_PROGRESS'` in PostgreSQL. Backend returns `{ sessionId: "abc-123" }`. Angular saves this in `localStorage`.
2. **Step 1 (Mobile)**: User enters +966 55XXXX. Angular POSTs to `/step/mobile`. Backend validates regex, generates OTP, stores `hashed_otp` in **Redis** (not DB), and sends SMS. User enters OTP. Backend verifies the hash, updates `current_step=2` in PostgreSQL, and returns success.
3. **Step 2 (ID)**: Angular reads `current_step=2` from the state and shows the ID upload form. User uploads images. Angular sends a `FormData` object containing images + ID number. Spring Boot receives it, saves images to **AWS S3** (generating URLs), sends the ID number to a third-party OCR/Validation API (via Feign Client). If valid, Spring Boot **encrypts** the ID number using AES-256 and saves it to the `onboarding_id_details` table. Updates `current_step=3`.
4. **Step 3, 4, 5**: Similarly, the orchestration moves forward. At each step, Angular polls `GET /status` or uses the server's response to automatically slide the Material Stepper to the next index.
5. **Step 6 (Credentials)**: User picks a password. Spring Boot calls the internal IAM (Keycloak) service via REST to create the user. If successful, Spring Boot updates the main session `status` to `'PENDING_KYC'`. It then places a message on **RabbitMQ/Kafka** to trigger the heavy background KYC compliance check (checking criminal records, sanctions list, etc.).
6. **Finalization**: The Angular app shows a "We are verifying your data" spinner. Meanwhile, a separate background scheduler in Spring Boot processes the Kafka message, runs the checks, and updates the session `status` to `'ACTIVE'` or `'REJECTED'`. The user receives an email/SMS notification, or Angular's polling catches the status change and redirects them to the login page.

---

### 🔒 6. Security & Non-Functional Considerations

- **Rate Limiting**: Implement Resilience4j or Bucket4j to limit API calls per IP (e.g., max 5 OTP requests per hour) to prevent brute force.
- **SQL Injection**: Strictly use JPA `@Param` and Hibernate prepared statements. No raw string concatenation.
- **CORS**: Properly configure `@CrossOrigin` in Spring Boot to only allow your specific Angular domain (e.g., `bank.frontend.com`).
- **Session Timeout**: If a user leaves the onboarding in the middle, the PostgreSQL `updated_at` field is checked by a daily cron job that deletes/archives sessions older than 7 days.
- **Horizontal Scaling**: Since it's a microservice, I will containerize it with Docker and deploy it on Kubernetes (EKS/AKS). PostgreSQL will handle concurrent connections with HikariCP (max pool size set to ~50 per pod).

---

### 🧠 Summary

This design is **not monolithic** because even within the `onboarding-service`, the State Machine, the Feign Clients, and the Async Listeners are heavily decoupled. By using **Spring Boot State Machine**, **PostgreSQL encryption**, and **Angular Material Stepper**, we achieve:

- **High Security** (encrypted PII, OTP in Redis, Route Guards).
- **Excellent UX** (refresh-safe, step persistence, auto-advancing stepper).
- **Scalability** (stateless Spring Boot APIs + Database connection pooling).

Would you like me to dive deeper into any specific part, such as the exact Spring State Machine configuration, the PostgreSQL encryption logic, or the Angular AsyncValidator for checking duplicate IDs?