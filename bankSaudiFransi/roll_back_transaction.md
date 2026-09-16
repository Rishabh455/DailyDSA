This is a brilliant and crucial question. In a distributed microservices environment, handling flow control and failures is the biggest challenge. 

Let me break this down into two clear parts:

### Part 1: The Orchestrator Microservice – Does it exist?

**Yes, the Orchestrator exists, but it is NOT a separate, standalone microservice deployed independently.** 

Instead, the Orchestrator is embedded **inside the `onboarding-service` itself** as a core business logic component (using **Spring State Machine**). 

**Why not a separate service?**
Because Steps 1 through 6 are **synchronous, user-driven** actions (the user clicks "Next" after filling each page). If we put the Orchestrator in a separate service, every single API call would require an extra network hop (Onboarding Service → Orchestrator Service → Database), adding 50-100ms of latency per step. 

**What does this embedded Orchestrator do?**

- It maintains the **current state** (e.g., `MOBILE_VERIFIED`, `ID_VALIDATED`) in memory and in the PostgreSQL `onboarding_session.current_step` column.
- It **validates transitions**: It ensures a user cannot directly call `/step/credentials` unless the orchestrator confirms `current_step` is exactly `5`.
- It **triggers external calls** (e.g., calling the IAM service to create credentials, calling the KYC engine via Kafka).
- It **handles compensation** (rollbacks) when something fails.

---

### Part 2: Rollback Strategy in Databases (The Saga Pattern)

In a monolithic app, we just use `@Transactional` and roll back everything automatically. **In microservices, we CANNOT use distributed 2-phase-commit (2PC)** because it locks database resources and kills performance. 

Instead, we use the **Orchestrator-based Saga Pattern** with **Compensating Transactions**. 

This means: *If Step 5 fails, we don't reverse time. We execute a new "compensating" API call to undo what Step 5 did.*

Here is exactly how rollback works for every possible failure scenario in our design:

---

#### Scenario A: Failure inside a single step (e.g., OCR fails during Step 2)
- **What happened**: The user uploaded an ID, but the OCR API returned an error (invalid image).
- **What the Orchestrator does**: It catches the exception immediately. It **does not** update `current_step`.
- **Database Rollback**: Since we used `@Transactional` on the Service method, **PostgreSQL automatically rolls back** the `INSERT` into `onboarding_id_details` and the `UPDATE` on `onboarding_session`. Nothing is saved. 
- **User sees**: An error toast saying "Please upload a clearer image." The session stays safely on Step 2.

---

#### Scenario B: Failure during Step 6 (IAM/Credential creation fails)
This is a classic distributed transaction problem. 
- **What happened**: The user submitted Step 5. The Orchestrator:
  1. Called the external **IAM (Keycloak) service** via REST to create the username/password. 
  2. *Successfully created the user in IAM.*
  3. Tried to update the local PostgreSQL session to `status='PENDING_KYC'`. 
  4. *Database connection timed out, and the update failed.*
- **Problem**: The user exists in IAM, but our DB says the session is still `IN_PROGRESS`. The system is now **inconsistent**.
- **Rollback Strategy (Compensating Transaction)**:
  The Orchestrator catches the DB exception and immediately triggers a **Compensating Action**:
  - It calls the IAM service's `DELETE /api/users/{username}` API to **delete the credentials** that were just created.
  - It rolls back the local DB transaction (since it failed anyway, the DB is unchanged).
  - It returns a generic error to the Angular UI: "System busy, please retry."
- **Result**: The IAM user is deleted, the DB is clean. The user retries fresh. **Eventual Consistency** is restored.

---

#### Scenario C: Failure during the Final Background KYC (AML check fails)
- **What happened**: Step 6 succeeded. The DB was updated to `status='PENDING_KYC'`, and the IAM user was created. The Orchestrator published a Kafka message to the AML engine. The AML engine checks the user and returns a `REJECTED` status due to a sanctions list match.
- **Problem**: The user has valid login credentials (can log in) but is not approved to hold an account.
- **Rollback Strategy (Compensating Transaction)**:
  The Orchestrator has a **Kafka Listener** that consumes the AML result:
  1. It updates the local PostgreSQL session `status = 'REJECTED'`.
  2. It calls the **IAM service** `PUT /api/users/{username}/disable` (or `DELETE`) to **lock/delete the user's login access** immediately.
  3. It pushes a notification to the user's email: "Your application was declined."
- **Result**: The user cannot log in, even though the credentials exist. The system is consistent.

---

#### Scenario D: The user abandons the session (No completion)
- **What happened**: User completes Step 2, saves data, and never comes back for 7 days.
- **Database Rollback / Cleanup**:
  We don't instantly roll this back. Instead, a **Scheduled Cron Job** runs daily:
  ```sql
  DELETE FROM onboarding_session 
  WHERE status = 'IN_PROGRESS' AND updated_at < NOW() - INTERVAL '7 days';
  ```
  *(Because we used `ON DELETE CASCADE` on `onboarding_mobile`, `onboarding_id_details`, `onboarding_address`, and `onboarding_additional`, PostgreSQL automatically deletes all child records linked to these stale sessions).*

---

#### Scenario E: Network timeout while calling External API (e.g., Address API)
- **What happened**: The Orchestrator called the Saudi Post API, but it took 10 seconds and timed out.
- **Database Rollback**: The Orchestrator treats this as a failure. It **does not** save the address to `onboarding_address` and **does not** advance the step. The user stays on Step 3.
- **Why?** Because we never commit the DB transaction until the *entire* step logic (including the external call) is successful. The `@Transactional` annotation ensures the local DB changes are only flushed/committed at the very end of the method. 

---

### 📊 Summary Table: Rollback Actions

| Failure Point | Local DB Action | External Service Compensation |
| :--- | :--- | :--- |
| **Step 1/2/3/4 (OCR/API timeout)** | Automatic rollback via `@Transactional`. Session stays on the previous step. | None. We don't call external services until we are sure. |
| **Step 5 (IAM creation fails)** | Automatic rollback (session stays at Step 5). | Orchestrator calls `DELETE /user` on IAM to undo the partial creation. |
| **Step 5 (DB update fails after IAM success)** | DB rollback occurs automatically. | Orchestrator explicitly calls `DELETE /user` on IAM to compensate. |
| **Background KYC (AML rejects)** | DB updated to `status='REJECTED'`. | Orchestrator calls IAM `DISABLE /user` to revoke access. |
| **User abandons** | Cron job deletes the main row (Cascade deletes all child rows). | None. No credentials were created yet. |

---

### 🧠 The Golden Rule We Follow

**"If you cannot commit the local database transaction, do not trust the external call."**

We use the **"Database-First"** approach:

1. Do all local DB saves.
2. *Flush* the transaction (but keep the connection open).
3. Call the external API (IAM/Address). 
4. If the external API succeeds → `COMMIT` the DB transaction.
5. If the external API fails → `ROLLBACK` the DB transaction, and *do not* retry the external call unless it is idempotent (which our `DELETE` compensation is).

This ensures that our PostgreSQL database is always the **Single Source of Truth**, and we never end up with ghost entries. 

Would you like me to show you the exact Spring Boot `@Transactional` propagation levels and the `@Retryable` logic we use to handle transient network failures during these compensation calls?