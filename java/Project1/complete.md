Here's the clean, copy-pasteable Markdown content for your `README.md`:

```markdown
# 🏛️ Aaple Sarkar — Government Services Platform
## Production-Grade Microservices Architecture

> **Core Design Principle:** One microservice per domain, not one per certificate type.  
> This supports 400+ citizen services without creating 400+ deployables.

---

## 📐 Platform-Wide Architectural Rules

| Concern | Decision |
|---|---|
| Service design | Stateless, horizontally scalable |
| Transactional state | PostgreSQL (applications, workflow, payment, audit) |
| Flexible metadata | MongoDB (form schemas, templates, service definitions) |
| Caching & locking | Redis + Redisson (hot reads, rate limiting, distributed locks) |
| Workflow control | Centralized Saga Orchestrator — no pure choreography for core states |
| Async work | Custom `ExecutorService` pools (PDF gen, notifications, doc validation) |
| Idempotency | Enforced on submit, payment, and certificate generation |
| File validation | Size + extension + MIME + magic-number checks |
| Resilience | Resilience4j — circuit breaker, retry, timeout, bulkhead on all external calls |
| Observability | Correlation IDs, structured logs, metric counters everywhere |

---

## 🗺️ Service Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                          API Gateway (Edge)                         │
└────────────────────────────┬────────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                     ▼
 ┌─────────────┐    ┌──────────────────┐   ┌──────────────────────┐
 │  Auth &     │    │  Citizen Profile │   │  Service Catalog &   │
 │  Identity   │───▶│  Service         │   │  Form Schema Service │
 └─────────────┘    └──────────────────┘   └──────────────────────┘
                                                     │
                             ┌───────────────────────▼──────────────────┐
                             │          Application Intake Service        │
                             └──────────────┬────────────────────────────┘
                                            │
              ┌─────────────────────────────┼──────────────────────┐
              ▼                             ▼                       ▼
    ┌──────────────────┐         ┌─────────────────────┐  ┌──────────────────┐
    │  Document Service│         │ Workflow Orchestrator│  │  Payment Service │
    └──────────────────┘         └──────────┬──────────┘  └──────────────────┘
                                            │
                        ┌───────────────────┼──────────────────┐
                        ▼                   ▼                   ▼
              ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────┐
              │ Certificate      │ │ Notification     │ │  Audit & Tracking   │
              │ Service          │ │ Service          │ │  Service            │
              └──────────────────┘ └──────────────────┘ └─────────────────────┘
```

---

## 1️⃣ Auth & Identity Service

**Owns:** OTP verification, JWT issuance, refresh token lifecycle, RBAC role resolution.  
> ⚠️ Does NOT store citizen profile business data — only identity and login state.

### Dependencies
`spring-boot-starter-security` · `spring-boot-starter-data-jpa` · `jjwt` · `redisson-spring-boot-starter` · `resilience4j-spring-boot3` · `flyway-core` · `postgresql`

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `AuthController` | `@RestController` | Login, OTP, token refresh, logout |
| `OtpController` | `@RestController` | Generate and verify OTP |
| `AuthService` | `@Service` | Coordinates login flow, user lookup, role resolution |
| `OtpService` | `@Service` | Hashed OTP in Redis with TTL; brute-force blocking |
| `JwtTokenService` | `@Service` | Sign/validate JWTs, create access/refresh tokens |
| `RefreshTokenService` | `@Service @Transactional` | Persist refresh token records and revocation state |
| `SecurityConfig` | `@Configuration` | Filter chain, stateless policy, endpoint auth |
| `JwtAuthenticationFilter` | Filter | Resolves token and sets security context |
| `AuthAuditListener` | `@KafkaListener @Async` | Writes login events asynchronously to audit trail |

### PostgreSQL Schema
```
user_accounts · roles · user_roles · refresh_tokens · otp_challenges · login_audit
```

### Design Decisions
- UUID/ULID primary keys
- Unique constraints on `mobile_number` and `aadhaar_hash`
- Partition `login_audit` by month at scale
- Distributed lock on OTP verification to prevent race conditions
- Rate limit OTP + login at both gateway and service layer

---

## 2️⃣ Citizen Profile Service

**Owns:** Citizen master profile — name, demographics, address, contact details, identity linkage.  
> Separate from Auth so profile can evolve without touching login logic.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `CitizenProfileService` | `@Service @Transactional` | Create/update/read profile, enforce completeness rules |
| `ProfileMergeService` | `@Service` | Merge auth identity + profile data without overwriting trusted fields |
| `IdentityLinkService` | `@Service` | Bind MahaID, Aadhaar hash, mobile to citizen profile |
| `ProfileSyncConsumer` | `@KafkaListener` | Consume `USER_CREATED` events from Auth |
| `AuthClient` | `@FeignClient` | Sync fetch for trusted identity context |

### PostgreSQL Schema
```
citizen_profiles · citizen_addresses · identity_links
```

### Design Decisions
- Unique index on `mahid`, `aadhaar_hash`
- Optimistic locking on profile updates
- Separate historical addresses from current address
- Circuit breaker around identity lookup

---

## 3️⃣ Service Catalog & Form Schema Service

**Owns:** 400+ service definitions, document requirements, fee rules, eligibility rules, dynamic form schemas, workflow configs, certificate templates.  
> This is what makes the platform configurable — not hardcoded.

### Dependencies
`spring-boot-starter-data-mongodb` · `redisson-spring-boot-starter` · `spring-kafka`

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `ServiceCatalogService` | `@Service` | Resolve service definitions by dept/code/version |
| `FormSchemaService` | `@Service` | Return dynamic fields, validations, conditional rules |
| `WorkflowRuleService` | `@Service` | Return approval path per service |
| `FeeRuleService` | `@Service` | Provide fee and exemption rules |
| `CatalogRefreshConsumer` | `@KafkaListener` | Invalidate cache on config changes |
| `CatalogCacheWarmupJob` | `@Scheduled` | Preload hot services into Redis at startup |

### MongoDB Collections
```
service_definitions · form_schemas · workflow_rules · fee_rules · document_requirements · certificate_templates
```

### Design Decisions
- Compound indexes: `(department_code, service_code, active_version)`
- Schema versioned for backward compatibility
- Aggressive Redis caching — read-heavy workload
- Fallback to last known active version if DB temporarily unavailable

---

## 4️⃣ Application Intake Service

**Owns:** Draft creation, form state, submission, idempotency, application reference number generation, status history.  
> Core citizen-facing transactional service.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `DraftApplicationService` | `@Service @Transactional` | Create/edit draft, save incremental form state |
| `SubmissionService` | `@Service @Transactional` | Validate mandatory data, check payment, change state to SUBMITTED |
| `ApplicationStatusService` | `@Service` | Manage status history and state transitions |
| `IdempotencyService` | `@Service` | Prevent duplicate submissions and retry creation |
| `Application` | `@Entity @Version` | Root aggregate with optimistic locking |
| `ApplicationIdempotency` | `@Entity` | Store idempotency key and request fingerprint |
| `ServiceCatalogClient` | `@FeignClient` | Fetch schema, fee, workflow definition |
| `WorkflowOrchestratorClient` | `@FeignClient` | Start workflow after submit |

### PostgreSQL Schema
```
applications · application_status_history · application_document_refs · application_remarks · application_idempotency
```

### Design Decisions
- Unique constraint on `(citizen_id, service_code, active_cycle)` — stops duplicate active applications
- Distributed lock on `(citizen_id + service_code)` during final submit — prevents double-click
- Circuit breaker for catalog/doc lookup so draft save remains available even if dependency degrades
- Fallback: keep in `DRAFT` or `PENDING_VALIDATION` instead of hard failing

---

## 5️⃣ Document Service

**Owns:** Pre-signed upload sessions, document metadata, validation, S3 references, file readiness state.  
> File bytes live in S3. This service owns metadata and the validation lifecycle only.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `UploadSessionService` | `@Service` | Create upload transaction, generate session token, bind to application |
| `PresignedUrlService` | `@Service` | Generate direct-to-S3 upload URLs |
| `DocumentValidationService` | `@Service` | Validate size, extension, MIME, magic byte signature |
| `ScanWorkflowService` | `@Service @Async` | Queue async malware/integrity scan |
| `MagicNumberValidator` | `@Component` | Pure byte-signature validation helper |
| `S3StorageAdapter` | `@Component` | Adapter around cloud storage |
| `DocumentScanConsumer` | `@KafkaListener` | Consume post-upload validation events |

### MongoDB Collections
```
document_metadata · upload_sessions · validation_rules · scan_results
```

### Design Decisions
- `upload_sessions` has TTL — automatically cleans abandoned uploads
- Actual file upload: **browser → S3 directly** (bypasses backend bandwidth)
- Index on `(application_id, document_type)` with uniqueness
- Circuit breaker on S3 adapter

---

## 6️⃣ Workflow Orchestrator Service

**Owns:** Central saga, allowed state transitions, officer routing, SLA timers, compensation steps, final approval.  
> ❤️ Heart of the enterprise design.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `WorkflowOrchestratorService` | `@Service @Transactional` | Coordinate the saga, own state progression |
| `StateTransitionEngine` | `@Service` | Validate allowed transitions, prevent illegal jumps |
| `OfficerAssignmentService` | `@Service` | Assign by jurisdiction, workload, and officer role |
| `CompensationService` | `@Service` | Revert/compensate failed downstream steps |
| `SlaManagementService` | `@Service @Scheduled` | Create deadlines and escalation triggers |
| `WorkflowInstance` | `@Entity @Version` | Current workflow snapshot with concurrency protection |
| `CompensationLog` | `@Entity` | Rollback and remediation audit |
| `WorkflowEventConsumer` | `@KafkaListener` | Consume submission/payment/document events |

### PostgreSQL Schema
```
workflow_instances · workflow_transitions · officer_assignments · sla_deadlines · compensation_log · workflow_rules
```

### Design Decisions
- **Source of truth for all state transitions** — no service bypasses this
- Sync commands for critical control points; Kafka for non-critical side effects
- Compensation steps are explicit: revert state → release officer lock → mark pending retry → notify citizen
- Idempotent event consumers — repeated events must not re-drive the same transition
- Partition transition + SLA tables by month/department

---

## 7️⃣ Payment Service

**Owns:** Payment initiation, gateway integration, callback handling, deduplication, reconciliation, refund handling.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `PaymentCommandService` | `@Service @Transactional` | Create pending payment, validate fee, update final state |
| `GatewayAdapterService` | `@Service` | Talk to external payment gateway |
| `PaymentIdempotencyService` | `@Service` | Prevent duplicate gateway callbacks from creating duplicate records |
| `PaymentReconciliationService` | `@Service @Scheduled` | Compare internal vs gateway state, fix mismatches |
| `PaymentCallbackController` | `@RestController` | Receive gateway success/failure callbacks |
| `PaymentTransaction` | `@Entity @Version` | Payment aggregate root |

### PostgreSQL Schema
```
payment_transactions · payment_callback_logs · refund_records · payment_reconciliation_logs
```

### Design Decisions
- Unique index on `gateway_txn_id`
- Callback endpoint must be idempotent
- Fallback state remains `PAYMENT_PENDING` until reconciliation clarifies truth
- Circuit breaker, retry, and timeout around all gateway calls

---

## 8️⃣ Notification Service

**Owns:** Email, SMS, in-app notifications, template rendering, delivery status, retries, preference logic.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `NotificationConsumer` | `@KafkaListener` | Consume application/payment/certificate events |
| `NotificationDispatchService` | `@Service @Async` | Decide channel, render template, dispatch message |
| `TemplateRenderService` | `@Service` | Fill placeholders and language variants |
| `NotificationRetryService` | `@Service` | Retry failed messages |
| `NotificationExecutorConfig` | `@Configuration` | Bounded thread pool for parallel dispatch |
| `NotificationRetryJob` | `@Scheduled` | Retry failed deliveries on schedule |

### PostgreSQL Schema
```
notification_templates · notification_logs · notification_preferences · notification_retry_queue
```

### Design Decisions
- Primary input is fully async — event-driven
- If SMS fails → fallback to email + in-app
- Circuit breaker on all external provider clients
- Retry with backoff → dead-letter queue
- **Notification failures must never block application approval**

---

## 9️⃣ Certificate Service

**Owns:** Certificate number generation, PDF rendering, QR code creation, digital signature, secure storage.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `CertificateGenerationService` | `@Service @Transactional` | Orchestrate PDF generation after approval |
| `PdfRenderService` | `@Service` | Render final PDF with certificate data |
| `QrCodeService` | `@Service` | Embed QR for public verification |
| `DigitalSignatureService` | `@Service` | Sign certificate and track signing metadata |
| `CertificateNumberService` | `@Service` | Generate unique human-readable certificate numbers |
| `CertificateEventConsumer` | `@KafkaListener` | React to approved workflow events |

### PostgreSQL Schema
```
certificates · certificate_versions · signature_audits · qr_tokens
```

### Design Decisions
- Unique index on `certificate_number`
- PDF + QR generated off the request thread via executor pools
- Actual PDFs stored in S3; only metadata in DB
- If signing fails → mark `PENDING_GENERATION` + retry async

---

## 🔟 Audit & Tracking Service

**Owns:** Immutable audit logs, citizen application tracking, state snapshots, officer action traceability, SLA breach history.  
> Governance-grade traceability — pure event-driven sink.

### Key Classes

| Class | Annotation | Responsibility |
|---|---|---|
| `AuditEventConsumer` | `@KafkaListener` | Consume events from ALL services, persist them |
| `AuditLogService` | `@Service @Transactional` | Write immutable audit entries |
| `TrackingQueryService` | `@Service` | Serve citizen "track your application" queries |
| `StatusProjectionService` | `@Service` | Maintain query-friendly denormalized tracking snapshot |
| `AuditRetentionJob` | `@Scheduled` | Archival and retention policy enforcement |

### PostgreSQL Schema
```
audit_logs · tracking_snapshots · sla_breach_logs
```

### Design Decisions
- Audit log is **append-only — never update historic rows**
- Citizen reads from projection/snapshot, not from live workflow tables
- Partition audit logs by month and optionally by department
- Idempotent event handling is mandatory

---

## 🚀 Scaling to 10K Concurrent Users

The real scaling decisions are not just "use microservices" — they are:

| Technique | Where Applied |
|---|---|
| Stateless request handling | All services |
| Redis-based rate limiting | Auth, API Gateway |
| Distributed locking | Submit, payment finalization |
| Async side effects via bounded `ExecutorService` + Kafka | PDF gen, notifications, document validation |
| Direct browser-to-S3 upload | Document Service |
| Optimistic locking (`@Version`) | Workflow, Application, Payment |
| Lean denormalized read models | Audit & Tracking |
| High-cardinality indexes on status, time, citizen_id | All PostgreSQL services |
| Circuit breakers on every external dependency | All Feign/HTTP clients |
| No sync PDF gen or notifications in main request path | Certificate, Notification |

---

## 📦 Tech Stack Summary

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Language | Java 21 |
| Relational DB | PostgreSQL + Flyway |
| Document DB | MongoDB |
| Cache / Lock | Redis + Redisson |
| Messaging | Apache Kafka |
| Resilience | Resilience4j |
| Service Comm | Spring Cloud OpenFeign |
| File Storage | AWS S3 |
| PDF Generation | Apache PDFBox / OpenPDF |
| QR Codes | ZXing |
| Security | Spring Security + JWT |
| Build | Maven / Gradle |
| Observability | Spring Actuator + structured logging |
```

Just paste this directly into your `README.md`. The tables, code blocks, headers, and service map diagram are all copy-paste ready.