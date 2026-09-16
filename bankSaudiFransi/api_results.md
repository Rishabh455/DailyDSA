Absolutely! Since we are in a **microservices architecture**, each API endpoint in the `onboarding-service` has a **single, well-defined responsibility**. 

The service owns its own PostgreSQL database schema (the tables I designed earlier). It does **not** directly query the Core Banking database, the CRM, or the IAM (Identity) database. Instead, it communicates with those via REST/Feign clients or asynchronous events (Kafka). 

Below is a **complete breakdown** of what each API does, the **business logic** it executes, and the **exact PostgreSQL queries** (or Hibernate/JPA-generated SQL) it runs.

---

### 1. `POST /api/onboarding/initiate`
**Purpose**: Starts a new onboarding journey.

- **Logic**: Generates a UUID, creates a new session row, and sets the starting step.
- **DB Queries**:
  ```sql
  -- 1. Insert the master session record
  INSERT INTO onboarding_session (id, id_type, current_step, status, ip_address, user_agent, created_at, updated_at) 
  VALUES (?, ?, 1, 'IN_PROGRESS', ?::inet, ?, NOW(), NOW());
  ```
  *(No SELECT query needed because we generate the UUID on the application side).*

---

### 2. `POST /api/onboarding/step/mobile`
**Purpose**: Submits the mobile number, sends OTP, and verifies the OTP. This is usually broken into two sub-APIs (Send OTP & Verify OTP).

**A) Send OTP**

- **Logic**: Check if this mobile number is already registered as an active customer elsewhere (to prevent duplicate accounts). Hash the number for lookup.
- **DB Queries**:
  ```sql
  -- 1. Check for duplicates (Active users or pending applications from the last 90 days)
  SELECT s.id, s.status 
  FROM onboarding_session s
  JOIN onboarding_mobile m ON m.session_id = s.id
  WHERE m.mobile_hash = ? 
    AND (s.status = 'ACTIVE' OR s.status = 'PENDING_KYC')
    AND s.created_at > NOW() - INTERVAL '90 days';
  
  -- 2. Get the current session to ensure we are on the right step (Optimistic Lock check)
  SELECT current_step, status FROM onboarding_session WHERE id = ? FOR UPDATE;
  
  -- 3. Insert/Update the mobile record (UPSERT pattern)
  INSERT INTO onboarding_mobile (session_id, encrypted_mobile, mobile_hash, country_code, is_verified, retry_count)
  VALUES (?, ?, ?, ?, FALSE, 0)
  ON CONFLICT (session_id) 
  DO UPDATE SET encrypted_mobile = EXCLUDED.encrypted_mobile, mobile_hash = EXCLUDED.mobile_hash;
  ```

**B) Verify OTP**

- **Logic**: Validates the OTP from Redis (not the DB), then marks the mobile as verified and moves the stepper forward.
- **DB Queries**:
  ```sql
  -- 1. Update the mobile status to verified
  UPDATE onboarding_mobile 
  SET is_verified = TRUE, verified_at = NOW(), retry_count = retry_count + 1 
  WHERE session_id = ?;
  
  -- 2. Move the step forward (only if current_step is 1)
  UPDATE onboarding_session 
  SET current_step = 2, updated_at = NOW() 
  WHERE id = ? AND current_step = 1;
  ```

---

### 3. `POST /api/onboarding/step/id-verification`
**Purpose**: Accepts ID number + images, runs OCR/validation checks, and stores the encrypted data.

- **Logic**: Encrypts the National ID, calculates a SHA-256 hash, checks the partial unique index to prevent duplicate submissions within 90 days, and stores image S3 paths.
- **DB Queries** (This runs inside a **single ACID transaction** with `@Transactional`):
  ```sql
  -- 1. (Duplicate Check) Ensure this exact ID hasn't been used recently
  SELECT 1 FROM onboarding_id_details 
  WHERE national_id_hash = ? 
    AND created_at > NOW() - INTERVAL '90 days';
  -- If this returns a row, we throw an exception and rollback the entire transaction.
  
  -- 2. (Optimistic Lock) Ensure we are still on Step 2
  SELECT current_step FROM onboarding_session WHERE id = ? FOR UPDATE;
  
  -- 3. Insert the ID details
  INSERT INTO onboarding_id_details (
      session_id, encrypted_national_id, national_id_hash, encrypted_full_name, 
      full_name_hash, date_of_birth, id_expiry_date, nationality, 
      front_image_path, back_image_path, ocr_score, is_ocr_verified
  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE); -- OCR verified assumes pass
  
  -- 4. Update the main session to Step 3
  UPDATE onboarding_session 
  SET current_step = 3, updated_at = NOW() 
  WHERE id = ? AND current_step = 2;
  ```

---

### 4. `POST /api/onboarding/step/address`
**Purpose**: Validates the National Address via an external government API and stores the details.

- **Logic**: Calls the Saudi Post API via Feign Client. If it returns a valid building number, we mark it as validated.
- **DB Queries**:
  ```sql
  -- 1. Upsert the address (if the user goes back and edits it)
  INSERT INTO onboarding_address (
      session_id, building_number, street_name, district, city, postal_code, 
      additional_marker, is_national_address, validated_at
  ) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, NOW())
  ON CONFLICT (session_id) 
  DO UPDATE SET 
      building_number = EXCLUDED.building_number,
      city = EXCLUDED.city,
      postal_code = EXCLUDED.postal_code,
      validated_at = NOW();
  
  -- 2. Move to Step 4
  UPDATE onboarding_session 
  SET current_step = 4, updated_at = NOW() 
  WHERE id = ? AND current_step = 3;
  ```

---

### 5. `POST /api/onboarding/step/additional`
**Purpose**: Saves employment status, income, and source of funds (required for AML/KYC compliance).

- **Logic**: Simple insert/update. This step is optional in terms of routing (we can skip if the user logs out), but mandatory to complete.
- **DB Queries**:
  ```sql
  -- 1. Insert/Update additional info
  INSERT INTO onboarding_additional (
      session_id, employment_status, employer_name, annual_income, 
      source_of_funds, has_previous_bsf_account
  ) VALUES (?, ?, ?, ?, ?, ?)
  ON CONFLICT (session_id) 
  DO UPDATE SET 
      employment_status = EXCLUDED.employment_status,
      annual_income = EXCLUDED.annual_income;
  
  -- 2. Move to Step 5
  UPDATE onboarding_session 
  SET current_step = 5, updated_at = NOW() 
  WHERE id = ? AND current_step = 4;
  ```

---

### 6. `POST /api/onboarding/step/credentials` (Final Step)
**Purpose**: Creates the user's login credentials in the internal IAM (Keycloak) and triggers the background KYC check.

- **Logic**: 
  1. Calls IAM service to create the user. If successful, it does **not** delete the session.
  2. Changes the session status to `PENDING_KYC`.
  3. Publishes a Kafka message `USER_ONBOARDED_PENDING_KYC` for the background anti-money laundering (AML) checker.
- **DB Queries**:
  ```sql
  -- 1. Fetch the entire consolidated data for KYC submission (Join all tables)
  SELECT 
      s.id, s.id_type, m.encrypted_mobile, id.encrypted_national_id, 
      id.encrypted_full_name, id.date_of_birth, a.city, a.postal_code, 
      ad.employment_status, ad.annual_income
  FROM onboarding_session s
  LEFT JOIN onboarding_mobile m ON m.session_id = s.id
  LEFT JOIN onboarding_id_details id ON id.session_id = s.id
  LEFT JOIN onboarding_address a ON a.session_id = s.id
  LEFT JOIN onboarding_additional ad ON ad.session_id = s.id
  WHERE s.id = ?;
  -- (This data is sent to Kafka for the AML team to process).
  
  -- 2. Update session status to Pending KYC (lock row to prevent race conditions)
  UPDATE onboarding_session 
  SET status = 'PENDING_KYC', current_step = 6, updated_at = NOW() 
  WHERE id = ? AND status = 'IN_PROGRESS';
  ```

---

### 7. `GET /api/onboarding/status/{sessionId}`
**Purpose**: Polled by Angular to restore the UI if the user refreshes the page or navigates away.

- **Logic**: Fetches the current step and an aggregated summary.
- **DB Queries**:
  ```sql
  -- Query 1: Get the session step and status
  SELECT current_step, status, updated_at FROM onboarding_session WHERE id = ?;
  
  -- Query 2: (Conditional) If step >= 2, fetch masked mobile and ID for UI display (show only last 4 digits)
  SELECT 
      RIGHT(encrypted_mobile, 4) as masked_mobile,
      is_verified
  FROM onboarding_mobile WHERE session_id = ?;
  ```
  *(Note: We never decrypt PII for Angular. We only send masked/truncated data. The raw decryption happens only on the backend during KYC submission).*

---

### 8. `GET /api/onboarding/kyc-status/{sessionId}`
**Purpose**: Long-polling endpoint used by Angular to check if the background AML check is finished.

- **Logic**: Polls every 5 seconds. The external AML system consumes the Kafka message, runs the check, and updates the DB.
- **DB Queries**:
  ```sql
  -- Check the status of the session
  SELECT status, kyc_result FROM onboarding_session WHERE id = ?;
  
  -- If the status is 'ACTIVE' or 'REJECTED', return the result.
  -- If status is still 'PENDING_KYC', return HTTP 202 (Accepted).
  ```

---

### 🔍 9. The Audit Log (Implicitly executed on every API call)

Since Audit logging is a **cross-cutting concern**, it runs asynchronously on **every API request** so it doesn't slow down the user experience.

```sql
-- Executed asynchronously via @Async or a background thread pool
INSERT INTO audit_log (session_id, event_type, payload, ip_address, user_agent, created_at) 
VALUES (
    ?, 
    'API_CALL', 
    jsonb_build_object('endpoint', ?, 'request_body', ?, 'response_code', ?), 
    ?::inet, 
    ?, 
    NOW()
);
```

---

### ✅ Summary of Query Patterns

| API Endpoint | Type of Operation | Critical Queries (Count) |
| :--- | :--- | :--- |
| `/initiate` | Write-heavy | 1 `INSERT` |
| `/step/mobile` | Write + Read | 2 `SELECT`s (duplicate check + lock), 1 `INSERT`, 1 `UPDATE` |
| `/step/id-verification` | Write + Read | 1 `SELECT` (duplicate hash), 1 `SELECT` (lock), 1 `INSERT`, 1 `UPDATE` |
| `/step/address` | Write | 1 `INSERT...ON CONFLICT`, 1 `UPDATE` |
| `/step/credentials` | Heavy Read + Write | 1 complex `JOIN SELECT` (5 tables), 1 `UPDATE` |
| `/status/{id}` | Read-heavy | 1 `SELECT` (indexed PK) |
| `Audit (auto)` | Write-heavy | 1 `INSERT` per API call (partitioned by date) |

**Key Takeaway**: Because this is a microservice, **every API strictly owns its transactional boundary**. We use `SELECT ... FOR UPDATE` to lock the `onboarding_session` row during updates to prevent double-submission, and we rely heavily on **Partial Unique Indexes** (like the hash index on National ID) to enforce business rules directly at the database level, ensuring zero duplicate applications.