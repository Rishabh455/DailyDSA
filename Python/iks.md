# IKS Health — Senior Full Stack Engineer: Interview Prep
**For:** Rishabh Kumar Chourasia · **Interviewer:** Manish Purohit, AVP Enterprise Architecture, IKS Health

## Contents
1. Reading the interviewer
2. The JD, decoded (skills map + honest gaps)
3. How to talk about "the project" — get this right first
4. 50 scenario-based questions & model answers

---

## 1. Reading the interviewer

Manish's career, in order: Architect/PM at TCS (GE Aviation & Transportation, NBC Universal) → Tech Architect at Saraswat Infotech (a core-banking product) → Architect/Sr. PM at Polaris Financial Technology → Sr. Technical Architect at Ness Technologies → Sr. Solution Architect at Citiustech → Principal Architect at Capgemini (8.5 years) → AVP Enterprise Architecture at IKS Health (current). 18+ years, almost entirely on the architecture/design-review side, not day-to-day feature coding.

A few things stand out and should shape your prep:

- **He built access control for a living.** At GE's Predix platform he personally contributed the User & Org Management (UOM) service, including the Access Control System (ACS) for authorization. IAM and authz aren't a side interest for him — it's close to his signature work.
- **He's cross-domain by design.** Core banking (Saraswat), fintech (Polaris), aviation (GE), media (NBC/SONAR), and now healthcare (IKS). Expect him to test whether you understand *patterns* that generalize across industries, not just one vertical's vocabulary.
- **Every role on his résumé repeats the same verbs:** requirement analysis → define architecture → validate technologies → *conduct architecture review* → *conduct design review* → POC. He's spent two decades professionally picking apart other people's designs. Expect "why," not just "what," and expect him to keep pulling on a thread once he finds one.
- **He's less likely to run a whiteboard-algorithm gauntlet** and more likely to ask you to defend a decision, describe a failure mode, or explain how a pattern holds up at 10x scale.

**What this predicts:** fewer syntax-trivia questions, more "walk me through your reasoning," more follow-ups on anything that sounds rehearsed, and a decent chance he asks at least one question that forces you to generalize your experience into the healthcare/compliance context IKS actually operates in.

---

## 2. The JD, decoded

Underneath the bullets, IKS is hiring someone who can (a) genuinely ship features across the full stack, (b) reason about API/DB/security design rather than just implement someone else's spec, and (c) sit in front of a client and explain what was built and why.

| JD asks for | Where you're genuinely strong | Honest gap — and how to bridge it |
|---|---|---|
| Python + FastAPI backend | Spring Boot depth transfers conceptually (DI, validation, middleware); a real FastAPI + GPT-4 build from your hackathon | Limited *production* FastAPI hours — say so plainly, point to the hackathon as evidence you pick frameworks up fast |
| React.js frontend | Real production Angular experience | Different framework, not a different skill — name the gap directly rather than implying React depth you haven't logged |
| REST APIs, auth/authz, microservices | Genuinely strong: RBAC, OTP-based MFA, LDAP/AD integration, Kafka microservices with an API Gateway | Minimal — this is your best material, lead with it |
| SQL + NoSQL databases | Real Postgres depth; MySQL and the ~20% optimization figure per this resume | No evidenced NoSQL/MongoDB — acknowledge it if asked instead of dodging |
| GCP / cloud-native | — | If your Azure experience is real, name Azure and bridge the concepts (managed Postgres, secrets, container runtimes map across clouds); don't claim GCP hours you don't have |
| Docker, CI/CD | Real — Jenkins pipelines | Solid, low risk |
| Design patterns, scalable architecture | Real — circuit breakers (Resilience4j), the Saga pattern, microservices decomposition | Solid — good material for architecture questions |
| Client-facing delivery, ownership | Production/UAT support experience; the 60+ figure is from this resume | Solid — just don't over-index on the exact number if pressed on specifics |

---

## 3. How to talk about "the project" — get this right first

Worth being direct about this before anything else: the detailed Digital Onboarding & KYC module (OCR pipeline, AML/sanctions screening, government ID verification, SAMA compliance specifics) reads like a strong reference architecture — and the material behind it says as much: it traces back to a course, not confirmed line-by-line delivery. That's genuinely fine as *design knowledge*. It becomes a liability the moment it's presented as personal, hands-on delivery to an interviewer who has spent 18 years running design reviews for a living.

The fix isn't to throw the material away — it's to use it for the right kind of question. Architecture interviews routinely include "how would you design X," and being fluent in this pipeline is a real asset there. What you want to avoid is answering "tell me what you built" with a system you researched rather than shipped.

**Suggested opening frame**, if he asks you to walk him through your background (adapt into your own words):

> "My production background is Java and Spring Boot — I built and optimized IAM systems at TCS: a password management system with LDAP/Active Directory integration and OTP-based multi-factor authentication, and a new-user onboarding application, both at enterprise scale. One sync job I optimized went from around 30 minutes to about 5, across roughly 630,000 users. Alongside that, I've been building hands-on Python — I won an internal hackathon building a FastAPI backend that used GPT-4 for a banking-compliance assistant. I'm looking to bring that backend and security depth into a role like this one, on a Python-and-React stack."

That's true, specific, and gives Manish real hooks — which, given his background, works in your favor rather than against you. The 50 questions below are built on that split: your real projects answer every "what did you personally build" question, and the KYC/onboarding architecture only ever answers "how would you design/extend this."

---

## 4. 50 scenario-based questions & model answers

### A. Architecture & Ownership (Q1–Q6)

**Q1. "Walk me through the most technically challenging problem you've solved in your current role — symptom to fix."**
Lead with the AD sync story: a nightly job re-crawling roughly 630,000 Active Directory records was taking about 30 minutes and delaying downstream provisioning. The root cause was a full re-crawl every run instead of detecting what had actually changed. The fix combined `whenChanged`-based incremental LDAP queries so only changed objects were pulled, LDAP paging so large result sets didn't blow out memory or time out, a 20-thread `ExecutorService` pool to parallelize processing, and batched Hibernate writes instead of row-by-row saves. Runtime dropped to about 5 minutes. Specific, quantified, and yours — this is your best all-purpose answer to "tell me about a hard problem."

**Q2. "Which parts of your recent work did you personally own end-to-end, versus integrate with or extend?"**
This is the calibration question, and it rewards a precise answer: "I owned the password management system's Spring Security/LDAP integration and MFA flow, and the new-user onboarding application, end-to-end on the backend — auth flows, LDAP/AD integration, request validation, exception handling, and the sync performance work. On the Python side, my most complete hands-on build is a compliance-assistant tool from an internal hackathon — FastAPI backend, GPT-4 integration, prompt design, built end-to-end. Where I've studied broader architectures, like full KYC/OCR/AML onboarding pipelines, that's design-level understanding from research rather than something I've shipped, and I'd rather say that plainly than imply otherwise."

**Q3. "Give an example of a feature you took from requirement to production, across the whole stack."**
The password-reset/MFA flow works well here: the requirement (reduce helpdesk tickets from manual password resets) drove a Spring Security/LDAP design with OTP verification, an API layer with validation and exception handling, an Angular frontend wired to it, tests, and a Jenkins-driven deploy, with you supporting it in production afterward. Add, honestly: "My frontend stack has been Angular; this role is React-first, and I'd want to be upfront that I'd be ramping on React specifics, even though the component-based, reactive-UI thinking transfers directly."

**Q4. "Why should we trust a primarily Java/Spring engineer to be productive quickly in a FastAPI/React shop?"**
Rehearse this one specifically — it's likely to come up in some form. The core argument: FastAPI and Spring Boot solve the same problems with different syntax — Pydantic models do what Bean Validation does, FastAPI's `Depends` does what Spring's DI container does, async handlers solve what Spring WebFlux solves. The harder skills — API design, authorization architecture, database modeling, testing discipline, debugging production issues — are language-agnostic and already proven. Point to the hackathon FastAPI project as evidence of picking up the framework fast, and commit to a concrete next step (a small personal project, focused docs) rather than claiming the gap is already closed.

**Q5. "How would you extend your onboarding-application experience into a customer-facing, KYC-style onboarding flow like the one common in banking?"**
Answer at the design level, using the real onboarding app as scaffolding: the shape is the same — intake → validate submitted identity data against an independent, authoritative source (in the enterprise case, that source was AD/LDAP; in banking, an approved identity-verification provider) → risk-based branching, auto-approving low-friction cases and routing exceptions to manual review → an audit trail on every state change. Frame it explicitly as "here's how I'd approach it," not "here's what I built."

**Q6. "If I called your manager right now, what would they say your single biggest contribution was this year?"**
This should map to something specific and checkable — the AD sync optimization (30 minutes to 5, ~630K users) is the standout, quantified answer. This is exactly the kind of question a design-review-minded interviewer likes to ask, because vague answers don't survive a follow-up and specific ones don't need to.

### B. FastAPI & Backend (Q7–Q14)

**Q7. "In your hackathon project, how did the FastAPI backend talk to GPT-4, and what would you change to productionize it?"**
This one should be entirely your own memory — describe the actual request flow (FastAPI validates via Pydantic, calls the model — likely through an async HTTP client so the event loop isn't blocked — and returns a structured response) and the real details of what you built. For "productionize," reasonable additions include retries and timeouts around the model call, caching for repeated prompts, moving slow generation off the request path into a background job if latency is high, rate limiting given the cost of LLM calls, and logging/cost tracking per request.

**Q8. "How does FastAPI achieve what `@Autowired` gives you in Spring?"**
`Depends()` is the analog — a callable FastAPI resolves and injects into a path function, most commonly used for database sessions, current-user/auth checks, and shared validation logic — the same role Spring's DI container plays when it wires a repository or security context into a controller.

**Q9. "You need the same request-validation rigor `@Valid` gave you in Spring. How do you get that in FastAPI?"**
Pydantic models on the request body give typed, declarative validation — types, constraints, custom validators — enforced automatically before your handler runs, with violations turned into structured 422 responses. Same contract-first discipline, expressed as Python classes instead of annotated Java DTOs.

**Q10. "Under load, a FastAPI endpoint calling a slow external API starts stacking up latency across *all* requests, not just the slow ones. What's happening?"**
Almost certainly a blocking (synchronous) call sitting inside an `async def` handler, tying up the event loop for every request on that worker while it waits. Fix by using an async HTTP client for the external call, or explicitly offloading blocking work to a thread pool, and add a timeout so one slow dependency can't stall the whole worker.

**Q11. "Product wants 10MB+ document uploads supported without workers running out of memory under load. What do you do?"**
Use `UploadFile` rather than reading the whole file into memory, enforce size limits at the gateway and app layer, stream the file straight to object storage, and — the bigger architectural move — never do the heavy processing (OCR, parsing) synchronously inside the request. Accept the upload, persist it, queue a background job, and return a fast acknowledgement instead.

**Q12. "How would you version an API so older mobile clients don't break when a response shape changes?"**
Version at the route or header level (`/v1`, `/v2`), keep changes additive where possible, and deprecate old fields on a clear timeline rather than mutating a shared response shape in place — the same backward-compatibility discipline as maintaining a shared Spring REST contract.

**Q13. "You're asked to replace client polling with real-time status pushes. What would you evaluate?"**
WebSockets for bidirectional real-time updates, or Server-Sent Events for simpler one-way push — FastAPI supports both natively. The trade-off is added connection-management complexity against reduced server load and better UX than aggressive polling; worth weighing against actual traffic before adding the complexity.

**Q14. "How do you decide whether new logic belongs in Flask, FastAPI, or whether it's worth a second backend language at all, given the team already runs Spring Boot elsewhere?"**
FastAPI/Flask earn their place where async I/O, auto-generated OpenAPI docs, or the Python ML/AI ecosystem specifically matter. Introducing a second language and runtime adds real operational cost — deployment, monitoring, on-call familiarity — so the decision should follow a concrete capability gap, not novelty. This kind of trade-off framing tends to land well with an architect specifically.

### C. React & Frontend (Q15–Q20)

**Q15. "Your Angular experience is real, but this role is React-first. What transfers, and what doesn't?"**
Transfers: component-based thinking, weighing one-way versus two-way data flow, state-management principles, routing concepts, consuming REST APIs from the frontend, TypeScript itself. Doesn't transfer directly: Angular's opinionated, batteries-included structure (built-in DI, RxJS, modules) versus React's more manual composition via hooks and context. Naming that adjustment up front reads as self-aware, not weak.

**Q16. "A user uploads a document, the tab crashes, they reopen the app later. How should the UI recover the correct state?"**
Don't treat local component state as the source of truth. On mount, fetch current status from the server and resume rendering/polling from there, so the UI always reflects server state instead of assuming a continuous session.

**Q17. "How do you keep an upload-and-process UI from feeling broken during a 10–30 second background job?"**
Explicit UI states — idle, uploading, processing, success, error — instead of one generic spinner: a progress indicator during upload, then a distinct "processing" state with polling or push updates until a terminal state, so the user always knows what's happening.

**Q18. "How do you stop a user from firing duplicate requests by double-clicking submit?"**
Disable the control on click and guard against a second in-flight request client-side — but don't stop there. Make the backend endpoint idempotent (dedupe by a request or file hash) as the real safety net, since the client-side guard alone isn't reliable.

**Q19. "A reviewer dashboard needs to list hundreds of pending records without feeling sluggish. What do you do?"**
Paginate or virtualize the list so only visible rows render, push filtering and sorting to the server instead of the client, and avoid fetching more data than the current view actually needs.

**Q20. "How do you avoid duplicating auth-header handling, base URLs, and error handling across every component calling your API?"**
Centralize it: one configured HTTP client with interceptors for auth tokens and error handling, wrapped in custom hooks per resource, so components consume a clean hook instead of raw fetch/axios calls scattered everywhere.

### D. Database Design (Q21–Q26)

**Q21. "A status-lookup query that used to be instant is now slow on a table with tens of millions of rows. How do you approach it?"**
Start with `EXPLAIN ANALYZE` to see the actual plan, check for a missing or non-selective index on the filtered columns, consider a partial index for the small "active" subset that's actually queried most often, and evaluate whether old or closed records should be archived out of the hot table entirely.

**Q22. "The JD calls out both relational and document databases. When would you actually introduce MongoDB alongside Postgres, rather than just using Postgres for everything?"**
When the data is genuinely schema-variable across records — very different field shapes per document type, say — and you're not doing much relational joining on it. Otherwise, Postgres's JSONB gives a lot of that same flexibility with less operational overhead than running two databases. Worth acknowledging directly: no evidenced NoSQL experience on your side, rather than overreaching.

**Q23. "How would you model application status so it's structurally impossible to jump straight from CREATED to APPROVED?"**
An enum status column plus an explicit state-machine layer in application code that only permits defined transitions, a check constraint or trigger as a second line of defense, and every transition written to an audit table for traceability.

**Q24. "How do you decide between normalized columns and a single JSON column for extracted or variable data?"**
Normalize the fields you'll query, filter, or index on frequently and that stay stable across records; keep JSON/JSONB for the long tail of variable, rarely-queried fields. A hybrid is usually more realistic than an all-or-nothing choice.

**Q25. "Your résumé cites roughly a 20% query-performance improvement. Walk me through how you actually found and fixed one of those slow queries."**
Identify slow queries via logs or a slow-query threshold, run `EXPLAIN`/`EXPLAIN ANALYZE`, look for missing indexes, unnecessary joins, or N+1 patterns coming from Hibernate, fix at the query or index level, and re-measure. Answer this with your actual example — the structure above is the shape to hang it on, not a substitute for the real story.

**Q26. "A write-heavy audit-log table is slowing down the main transaction path. How do you decouple it?"**
Don't make the audit write block the primary transaction if it doesn't have to — write it asynchronously through a queue or outbox pattern, or to a separate table optimized for high write throughput and time-based partitioning, keeping only genuinely compliance-critical events tightly coupled to the same transaction.

### E. Security (Q27–Q34)

This is your strongest category — lean into the real depth.

**Q27. "Walk me through how OTP-based MFA actually worked in the password management system you built."**
Describe the real flow: a user-initiated sensitive action (password reset) triggers an OTP delivered out of band, with a short validity window and single use, verified server-side before the change is allowed to proceed against LDAP/AD, with attempt limits and lockout handling to resist brute-force attempts.

**Q28. "How did you structure role-based access so operations staff, admins, and end users each see only what they should?"**
RBAC enforced server-side on every request — never a hidden frontend button as the actual security boundary — with roles resolved from the authenticated identity (LDAP/AD groups, in your real system). The deeper point Manish will likely push on: authorization checked at the resource level too, so a user can't reach another user's record just by knowing its ID, not only "is this role allowed to call this endpoint."

**Q29. "A pen-tester renames a script to `resume.pdf` and uploads it. What stops that from becoming a problem?"**
Never trust the extension or declared content type. Validate the actual file signature/magic bytes server-side, enforce size limits, store uploads under randomized keys in storage that never executes content, and run a malware scan before the file is trusted anywhere downstream.

**Q30. "Given your access-control background, how would you design authorization so a new role later doesn't mean hunting down `if role == X` checks across the codebase?"**
This one directly mirrors Manish's own ACS/UOM work at GE — worth having a sharp answer. Centralize authorization as a permissions/policy layer mapping roles to permissions, instead of hardcoding role checks inline. A new role then becomes a configuration change — assign it an existing set of permissions — rather than a code change scattered across services.

**Q31. "Sensitive identifiers live in your database. What protects them beyond disk-level encryption?"**
Column-level or application-level encryption before the value ever reaches the database, strict least-privilege DB access, and — often the actual failure point in real incidents — making sure these fields are scrubbed or masked before they ever reach logs or non-production environments.

**Q32. "An external identity-verification dependency goes down for an hour. What's the wrong move, and what's the right one?"**
Wrong: silently treating unverifiable users as verified just to keep the pipeline moving. Right: fail safe into a pending/hold state, retry with backoff or a circuit breaker so the outage doesn't cascade into your own service, and alert so a human is aware before anything auto-resolves.

**Q33. "How do you prevent yourself from being the reason PII ends up in a log aggregator?"**
Structured logging with an explicit allow-list of fields — event types and identifiers, not full payloads — a PII-scrubbing step in the logging pipeline as a backstop, and treating "does this log line contain something sensitive" as a normal part of code review, not an afterthought.

**Q34. "Manish spent years building access control systems for GE's Predix platform. If he asks you to critique the RBAC model you just described, what's the honest weak point you'd volunteer before he finds it?"**
Worth rehearsing specifically: pick a real, modest limitation — for example, "our role granularity was coarser than ideal; a couple of roles had more access than they strictly needed because we hadn't gotten to fine-grained permission splitting yet." Volunteering a real, specific weakness lands far better with an architecture reviewer than insisting the design had none.

### F. Performance, Async & Scalability (Q35–Q40)

**Q35. "You mentioned a sync job that went from 30 minutes to 5. What was the first thing you tried, and did it work?"**
Give the real arc, not just the ending — if the first instinct was "just add more threads," it's worth being honest that this alone wouldn't fix a full-table-rescan problem. The actual unlock was switching to `whenChanged` incremental detection so unchanged records weren't reprocessed at all; parallelism only helped once the underlying workload itself was reduced. Swap in what genuinely happened.

**Q36. "Why a 20-thread pool specifically — how would you have tuned that number?"**
Thread count for I/O-bound work like LDAP calls and DB writes is usually tuned empirically against the real bottleneck — LDAP server capacity, DB connection pool size, available cores — rather than picked arbitrarily. Start conservative, measure throughput and error/timeout rates as you increase it, and stop before overwhelming a downstream dependency like the LDAP server or exhausting the connection pool.

**Q37. "How does LDAP paging actually prevent failures on large result sets, mechanically?"**
Without paging, a query against a very large directory can hit server-side size limits or time out trying to materialize the full result at once. Paging fetches results in bounded chunks via a cookie or continuation token, keeping memory and per-request time bounded regardless of the total result size.

**Q38. "If this same batch pattern needed to run every 5 minutes instead of nightly, what would you reconsider?"**
Logic tolerant of nightly slack may need to become truly event-driven instead — reacting to AD change notifications rather than polling on a timer — and it's worth revisiting whether the thread pool and DB batch sizes still make sense at a much tighter cadence, plus adding monitoring so an overrun run can't overlap with the next one.

**Q39. "How would you apply the Saga pattern you explored to a multi-step process like onboarding, where a later step can fail after earlier steps already succeeded?"**
Describe the real trade-off explored: choreography, where each service reacts to the previous service's event — fully decoupled but harder to trace — versus orchestration, where a central coordinator drives each step and issues compensating actions on failure — easier to reason about and debug at the cost of a central dependency. For something like onboarding, an orchestrator that can roll back a provisional account if a later verification step fails is usually easier to operate and audit.

**Q40. "How did the API Gateway plus Resilience4j circuit breaker in your Kafka project actually protect the system, concretely?"**
The circuit breaker trips once a downstream service crosses a failure or latency threshold, so callers fail fast or fall back instead of piling up requests against a service that's already struggling — protecting the caller's own resources and giving the failing dependency room to recover instead of absorbing a growing backlog.

### G. Testing, CI/CD & Cloud (Q41–Q46)

**Q41. "How do you unit-test logic that depends on an external service — LDAP, an OTP provider, GPT-4 — without hitting the real thing every run?"**
Depend on an interface or abstraction rather than calling the external client directly, and substitute a mock or fake in tests — Mockito-style in Java, `unittest.mock`/`pytest-mock` in Python. Same principle in either ecosystem: isolate the thing you don't control.

**Q42. "A deploy silently breaks one specific flow in production. What in your CI/CD pipeline should have caught it?"**
Test coverage for edge cases and each supported variant, not just the happy path; a staging environment with genuinely representative data; and a smoke test or canary/gradual rollout before full production traffic hits the new build. The earlier a bad build is caught, the cheaper the incident.

**Q43. "You've run Jenkins pipelines before. What changes moving to a cloud-native CI/CD setup on GCP?"**
Worth being honest that hands-on GCP is the newer part, while showing the concepts transfer directly: build/test/deploy stages stay the same; what changes is where artifacts live (Artifact Registry) and how deploys target the runtime (Cloud Run/GKE), plus secrets coming from a managed secret store instead of Jenkins credentials. Committing to closing the GCP-specifics gap directly reads better than overstating current depth.

**Q44. "How would you containerize a multi-part system — API, background worker, frontend — for local dev versus production?"**
`docker-compose` locally, running the API, worker, database, and frontend together with realistic wiring; Kubernetes or a managed equivalent (GKE/Cloud Run) in production for scaling each piece independently; config and secrets injected via environment variables or a secret manager, never baked into the image.

**Q45. "What non-test things do you look for reviewing a teammate's PR, even if it's green across the board?"**
Whether auth and authorization checks are actually present, not just that the happy path works; error-handling and logging quality; backward compatibility for anything already in production; and whether the change fits the system's existing patterns rather than quietly introducing a new one.

**Q46. "You've resolved 60+ production and UAT issues. Walk me through your actual process for one, from alert to root cause."**
Push for a real, specific one: what surfaced it (an error-rate spike, a specific user report), how it was isolated (logs, correlation IDs, reproducing it in a lower environment), what the root cause turned out to be, and what changed afterward — ideally something that prevents the same class of bug next time, not just a one-off patch.

### H. Domain Adaptation, AI/LLM & Behavioral (Q47–Q50)

**Q47. "IKS Health serves clinicians, not banks. How does IAM/onboarding experience from a banking-adjacent enterprise app translate to a healthcare provider platform?"**
The shape repeats across domains: verify an identity or credential against an authoritative source (AD/LDAP in your world; a medical license registry or credentialing body in healthcare), enforce risk- or role-based access to sensitive records, and keep an audit trail because compliance will eventually ask for one. Same architecture, different regulator and different authoritative source — a good moment to show the pattern generalizes rather than being tied to one vertical's vocabulary.

**Q48. "Tell me about the AI hackathon project — what did the FastAPI + GPT-4 compliance assistant actually do, and what would you do differently with more time?"**
This should be entirely real and specific — describe the actual problem it solved, the actual architecture even if simple, what worked, and one honest "with more runway I'd have added X" (evaluation or guardrails on the model's outputs, caching, cost controls). A small real project described precisely outperforms a large invented one described vaguely, every time, in front of an architect.

**Q49. "How do you decide when it's appropriate to lean on GitHub Copilot or Claude for a task versus writing it yourself?"**
Good for boilerplate, test scaffolding, first-draft refactors, and exploring unfamiliar APIs quickly; not a substitute for understanding what the generated code does. Describe actually reviewing and rewriting AI-suggested code, especially around security-sensitive logic, rather than accepting suggestions blindly — that answer matters more to an architecture-minded interviewer than the tool name itself.

**Q50. "A stakeholder wants to skip a validation step to hit a deadline, in a system handling sensitive data. What do you do?"**
Name the specific risk in concrete terms rather than "it's not best practice," propose a scoped-down but still-safe alternative if one exists, document the trade-off and decision, and escalate rather than silently comply if the risk is real. This is exactly the ownership-and-communication combination the JD asks for.

---

## Before you walk in

- Bring numbers, not adjectives: "~630K users," "30 minutes to 5," "~20% faster" beat "significantly improved" every time.
- If Manish asks point-blank whether you personally built the KYC/OCR/AML pipeline, the only good answer is the true one — redirect to what you actually own and what you'd design.
- Architects like being asked good questions back. Given his background, something like "how did access control approaches differ across the GE, banking, and healthcare clients you've worked with" is a genuine, well-aimed question — not just flattery.