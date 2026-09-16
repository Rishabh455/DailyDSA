Here is the **in-depth database design** for this onboarding module. I will explain the **PostgreSQL schema**, **table relationships**, **foreign key linkages**, **constraints**, **indexing strategies**, and **data flow** step-by-step.

Since this is a banking application, the database must be **highly normalized** (mostly 3NF) to prevent data anomalies, but we will use **strategic denormalization** for audit logs and performance.

---

### 🧩 1. Entity Relationship Diagram (ERD) Overview

The central entity is the **`onboarding_session`** table. Every other table links to it via a `session_id` as a **Foreign Key (FK)**. 

Here is how the tables relate:

- **`onboarding_session` (Parent)** → **`onboarding_mobile` (Child)**: **1-to-1** (One session has exactly one mobile record).
- **`onboarding_session` (Parent)** → **`onboarding_id_details` (Child)**: **1-to-1** (One session has exactly one ID record).
- **`onboarding_session` (Parent)** → **`onboarding_address` (Child)**: **1-to-1**.
- **`onboarding_session` (Parent)** → **`onboarding_additional` (Child)**: **1-to-0..1** (Optional, based on user profile).
- **`onboarding_session` (Parent)** → **`audit_log` (Child)**: **1-to-Many** (One session generates hundreds of audit logs).

---

### 📊 2. Detailed Table Schemas with DDL (PostgreSQL)

#### A. Core Table: `onboarding_session`
This holds the master state of the user's journey.

```sql
CREATE TABLE onboarding_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_reference_id VARCHAR(50) UNIQUE, -- Optional: For tracking via CRM
    id_type VARCHAR(20) NOT NULL CHECK (id_type IN ('NATIONAL', 'IQAMA', 'VISITOR')),
    current_step SMALLINT NOT NULL DEFAULT 1 CHECK (current_step BETWEEN 1 AND 6),
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS' 
        CHECK (status IN ('IN_PROGRESS', 'PENDING_KYC', 'ACTIVE', 'REJECTED', 'EXPIRED')),
    kyc_result VARCHAR(20) DEFAULT NULL CHECK (kyc_result IN ('PASS', 'FAIL', 'MANUAL_REVIEW')),
    ip_address INET, -- Stores IP as network address type
    user_agent TEXT, 
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_session_status ON onboarding_session (status);
CREATE INDEX idx_session_updated_at ON onboarding_session (updated_at);
CREATE INDEX idx_session_current_step ON onboarding_session (current_step);
```

#### B. Step 1 Table: `onboarding_mobile`
**Relation**: `session_id` references `onboarding_session(id)` with `ON DELETE CASCADE`.

```sql
CREATE TABLE onboarding_mobile (
    session_id UUID PRIMARY KEY, -- Primary key is also the FK (1-to-1)
    encrypted_mobile TEXT NOT NULL, -- Stored as AES-256 ciphertext
    mobile_hash CHAR(64) NOT NULL UNIQUE, -- SHA-256 hash for fast lookups (check duplicates)
    country_code VARCHAR(5) DEFAULT '+966',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    retry_count SMALLINT DEFAULT 0,
    CONSTRAINT fk_mobile_session FOREIGN KEY (session_id) 
        REFERENCES onboarding_session(id) ON DELETE CASCADE
);

-- Index for duplicate checks before starting onboarding
CREATE INDEX idx_mobile_hash ON onboarding_mobile (mobile_hash);
```

#### C. Step 2 Table: `onboarding_id_details`
**Relation**: 1-to-1 with `onboarding_session`.

```sql
CREATE TABLE onboarding_id_details (
    session_id UUID PRIMARY KEY,
    encrypted_national_id TEXT NOT NULL,
    national_id_hash CHAR(64) NOT NULL, -- For quick duplicate validation
    encrypted_full_name TEXT NOT NULL,
    full_name_hash CHAR(64), -- For searching
    date_of_birth DATE NOT NULL,
    id_expiry_date DATE NOT NULL,
    nationality VARCHAR(100),
    front_image_path TEXT NOT NULL, -- S3/MinIO path
    back_image_path TEXT NOT NULL, -- S3/MinIO path
    ocr_score DECIMAL(5,2) CHECK (ocr_score BETWEEN 0 AND 100), -- Confidence score from OCR
    is_ocr_verified BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_id_session FOREIGN KEY (session_id) 
        REFERENCES onboarding_session(id) ON DELETE CASCADE
);

-- CRITICAL: Partial unique index to prevent duplicate applications within 90 days
CREATE UNIQUE INDEX idx_unique_national_id_recent 
    ON onboarding_id_details (national_id_hash) 
    WHERE created_at > NOW() - INTERVAL '90 days';
```
**Why the partial index?** A user who already has an active bank account shouldn't apply again for 90 days. This index prevents race conditions where someone submits twice simultaneously.

#### D. Step 3 Table: `onboarding_address`
**Relation**: 1-to-1 with `onboarding_session`.

```sql
CREATE TABLE onboarding_address (
    session_id UUID PRIMARY KEY,
    building_number VARCHAR(50),
    street_name VARCHAR(200),
    district VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10),
    additional_marker VARCHAR(100), -- Nearby landmark
    is_national_address BOOLEAN DEFAULT TRUE, -- If validated via Saudi Post API
    validated_at TIMESTAMPTZ,
    CONSTRAINT fk_address_session FOREIGN KEY (session_id) 
        REFERENCES onboarding_session(id) ON DELETE CASCADE
);

CREATE INDEX idx_address_city ON onboarding_address (city);
```

#### E. Step 4 Table: `onboarding_additional`
**Relation**: 1-to-0..1 (optional child). If the user exits before this step, the row is never created.

```sql
CREATE TABLE onboarding_additional (
    session_id UUID PRIMARY KEY,
    employment_status VARCHAR(50) NOT NULL CHECK (employment_status IN ('EMPLOYED', 'SELF_EMPLOYED', 'STUDENT', 'RETIRED', 'UNEMPLOYED')),
    employer_name VARCHAR(200),
    annual_income NUMERIC(15,2) CHECK (annual_income >= 0),
    source_of_funds TEXT, -- e.g., 'SALARY', 'BUSINESS', 'INVESTMENT'
    has_previous_bsf_account BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_additional_session FOREIGN KEY (session_id) 
        REFERENCES onboarding_session(id) ON DELETE CASCADE
);
```

#### F. Audit Log Table (Partitioned)
**Relation**: Many-to-1 (Many logs per session). Notice we use `ON DELETE SET NULL` so if we purge the session, the audit logs remain for regulatory compliance (SAMA requires 5-year retention).

```sql
-- Create partitioned table by month
CREATE TABLE audit_log (
    id BIGSERIAL,
    session_id UUID, -- FK but with SET NULL to preserve audit
    event_type VARCHAR(50) NOT NULL, -- e.g., 'STEP1_OTP_SENT', 'STEP2_OCR_FAILED'
    payload JSONB NOT NULL, -- Stores entire request/response metadata in JSON
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_audit_session FOREIGN KEY (session_id) 
        REFERENCES onboarding_session(id) ON DELETE SET NULL
) PARTITION BY RANGE (created_at);

-- Create monthly partitions (Example for Jan 2026)
CREATE TABLE audit_log_2026_01 PARTITION OF audit_log
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
-- Index on partition only
CREATE INDEX idx_audit_session_id ON audit_log (session_id);
CREATE INDEX idx_audit_created ON audit_log (created_at DESC);
```

---

### 🔗 3. How They Are Related (The Data Flow Journey)

Let me walk you through **how data is inserted, updated, and related** across these tables during runtime:

| Step | User Action | Database Transaction (ACID) |
| :--- | :--- | :--- |
| **Initiation** | Clicks "Continue" | **`INSERT INTO onboarding_session`** (id=UUID, status='IN_PROGRESS', step=1). Returns UUID to Angular. |
| **Step 1** | Submits mobile number + OTP | **`INSERT INTO onboarding_mobile`** (session_id=UUID, encrypted_mobile, mobile_hash, is_verified=true). <br> **`UPDATE onboarding_session`** SET current_step = 2 WHERE id = UUID. |
| **Step 2** | Uploads ID images | **`INSERT INTO onboarding_id_details`** (session_id=UUID, encrypted_national_id, national_id_hash, etc.). <br> *Before insert, a query runs:* `SELECT 1 FROM onboarding_id_details WHERE national_id_hash = ? AND created_at > NOW() - INTERVAL '90 days'` → if exists, rollback the transaction! <br> **`UPDATE onboarding_session`** SET current_step = 3. |
| **Step 3** | Submits Address | **`INSERT INTO onboarding_address`** (session_id=UUID, city, postal_code, etc.). <br> **`UPDATE onboarding_session`** SET current_step = 4. |
| **Step 4** | Submits Employment Info | **`INSERT INTO onboarding_additional`** (session_id=UUID, employment_status, income, etc.). <br> **`UPDATE onboarding_session`** SET current_step = 5. |
| **Step 5** | Creates Credentials & Triggers KYC | **`UPDATE onboarding_session`** SET status = 'PENDING_KYC', current_step = 6. <br> A background thread picks this session, performs heavy KYC checks, and finally does a **`UPDATE onboarding_session`** SET status = 'ACTIVE' or 'REJECTED'. |
| **Every step** | Any API call | **`INSERT INTO audit_log`** (session_id=UUID, event_type='API_CALL', payload=JSONB containing request body, ip_address, user_agent). Note: This insert does *not* block the main transaction; it's often done asynchronously using a `@Async` annotation or a separate thread to keep the UI fast. |

---

### 🔐 4. Advanced PostgreSQL Specifics & Constraints

#### A. Encryption Strategy (App-Level vs DB-Level)
- **Storage**: All PII fields (`encrypted_mobile`, `encrypted_national_id`, `encrypted_full_name`) are stored as `TEXT` containing a combined format: `IV:CIPHER_TEXT` (e.g., `a1b2c3:U2FsdGVkX1/...`).
- **Lookup Fields**: Since you cannot `WHERE` on encrypted data, we store `national_id_hash` and `mobile_hash` as `CHAR(64)` (SHA-256). When a user submits an ID, the Spring Boot service hashes the raw ID and queries `SELECT * FROM onboarding_id_details WHERE national_id_hash = ?` to check for duplicates.

#### B. Transaction Isolation & Locking
- **Pessimistic Locking**: When Step 2 runs and checks for duplicate IDs, we use `SELECT FOR UPDATE SKIP LOCKED` on the session row to prevent two simultaneous API requests from processing the same `session_id`. 
- **Optimistic Locking**: We add a `version` column (`BIGINT DEFAULT 1`) to `onboarding_session`. When updating `current_step`, we do `UPDATE onboarding_session SET current_step = 2, version = version + 1 WHERE id = ? AND version = 1`. If the update affects 0 rows, it means another request already updated it, and we throw a conflict exception.

#### C. Partial Indexes for Data Integrity
We already added a partial unique index on `national_id_hash`. But let's add one more for OTP brute-force protection:

```sql
-- Prevent more than 5 failed OTP attempts per session in the last 1 hour
CREATE INDEX idx_mobile_retry ON onboarding_mobile (session_id, retry_count) 
    WHERE retry_count >= 5 AND updated_at > NOW() - INTERVAL '1 hour';
```

#### D. Archival & Cleanup Strategy (Performance)
To keep the `onboarding_session` table fast (under 1 million rows), we run a daily batch job:

```sql
-- 1. Move old IN_PROGRESS sessions (> 7 days) to a partitioned archive table.
WITH archived_sessions AS (
    DELETE FROM onboarding_session 
    WHERE status = 'IN_PROGRESS' AND updated_at < NOW() - INTERVAL '7 days'
    RETURNING *
)
INSERT INTO onboarding_session_archive SELECT * FROM archived_sessions;

-- 2. Delete old audit logs (keep only last 6 months in the main partition).
DELETE FROM audit_log WHERE created_at < NOW() - INTERVAL '6 months';
```
*(Note: Because we used `ON DELETE CASCADE` on the main 1-to-1 tables, deleting from `onboarding_session` automatically deletes the linked `mobile`, `id_details`, `address`, and `additional` rows, preventing orphan records).*

---

### 📈 5. Query Patterns & Index Justification

| Query Purpose | SQL Snippet | Index Used |
| :--- | :--- | :--- |
| **Resume onboarding** (User refreshes the page) | `SELECT * FROM onboarding_session WHERE id = ?` | Primary Key (B-Tree) |
| **Check duplicate ID** | `SELECT 1 FROM onboarding_id_details WHERE national_id_hash = ? AND created_at > NOW() - INTERVAL '90 days'` | `idx_unique_national_id_recent` (Partial Unique) |
| **Cron Job: Find stale sessions** | `SELECT id FROM onboarding_session WHERE status = 'IN_PROGRESS' AND updated_at < NOW() - INTERVAL '7 days' LIMIT 1000` | `idx_session_status` + `idx_session_updated_at` (Composite) |
| **Admin Panel: Search by Mobile** | `SELECT s.* FROM onboarding_session s JOIN onboarding_mobile m ON s.id = m.session_id WHERE m.mobile_hash = ?` | `idx_mobile_hash` (Fast hash lookup) |
| **Audit Forensics** | `SELECT * FROM audit_log WHERE session_id = ? AND created_at BETWEEN ? AND ? ORDER BY created_at ASC` | `idx_audit_session_id` + Partition pruning by `created_at` |

---

### ✅ Summary of Relationships

- **`onboarding_session`** is the **Master Root**. 
- **`onboarding_mobile`**, **`onboarding_id_details`**, **`onboarding_address`**, and **`onboarding_additional`** are **Slave/Child** tables. They have a **Foreign Key** with `ON DELETE CASCADE` so if the session is archived, the garbage data is automatically cleaned by PostgreSQL.
- **`audit_log`** is a **Slave/Child** with `ON DELETE SET NULL` because compliance requires keeping the log even if the session is deleted.
- **Foreign Keys enforce strict Referential Integrity**: You cannot insert a record into `onboarding_mobile` with a `session_id` that doesn't already exist in `onboarding_session`. This prevents orphaned data during system failures.

This design ensures **ACID compliance**, **high performance** (via strategic indexes and partitions), and **strict regulatory compliance** (via audit trails and encryption). Let me know if you want me to explain the Flyway/Liquibase migration scripts for this schema!