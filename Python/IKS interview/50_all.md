IKS Health — Sr Full Stack Engineer Interview Prep
Interviewer: Manish Purohit, AVP Enterprise Architecture (18+ yrs, ex-Capgemini Principal Architect, strong microservices/cloud/architecture background)
Candidate: Rishabh Kumar Chourasia — 3+ yrs Python Full Stack (FastAPI, Flask, React, SQL) at TCS, Saudi National Bank account
> Note: Since your interviewer is an **Enterprise Architect**, expect him to probe *why* you made design choices (not just *what* you built) — separation of concerns, scalability, failure handling, trade-offs. The answers below lean into that.
---
A. Introduction & Behavioral (Q1–8)
1. Walk me through your resume / Tell me about yourself.
Start with: 3+ yrs Python full-stack dev at TCS on the Saudi National Bank account → two projects: (1) Digital Onboarding/KYC platform — microservices, FastAPI/React, document ingestion + Azure Document Intelligence OCR + KYC decisioning; (2) Real-time Fraud Detection/Credit Risk engine — FastAPI service layer feeding an ML scoring component for APPROVE/REVIEW/BLOCK decisions. Mention GH-300/600 Copilot certifications and hands-on GenAI/RAG exposure. Close with why you're excited about IKS Health's Tech Engineering / Care Enablement Platform.
2. Why are you looking to leave TCS / why IKS Health?
Focus positively: you want to work closer to a product-engineering, healthcare-technology platform rather than a services/staff-augmentation model, and IKS Health's Care Enablement Platform (FastAPI/React/GCP stack) is a direct match to your skill set and lets you own end-to-end product features rather than enhancement tickets.
3. Describe a challenging bug you debugged end-to-end.
Use a KYC example: an intermittent mismatch between OCR-extracted document fields and onboarding data caused false REVIEW_REQUIRED outcomes. Walk through: reproduced via API logs → traced through Document Service → found a validation edge case in Pydantic model coercion → root-caused to a data-type mismatch from the extraction service → fixed + added regression test in PyTest. Emphasize cross-layer debugging (frontend → API → service → DB → integration).
4. Tell me about a time you disagreed with a technical decision.
Give a real/plausible example: disagreed on synchronous vs async call pattern for downstream identity-verification integration; advocated for async I/O with FastAPI to avoid blocking under load; presented latency data; team adopted it for the fraud engine's real-time evaluation path.
5. How do you handle production issues / on-call pressure?
Reference the 60+ high-priority UAT/production issues you resolved: triage by business impact → isolate via logs/API traces/SQL → patch with rollback plan → root-cause analysis → post-mortem to prevent recurrence.
6. How do you stay current with technology?
Mention active use of GitHub Copilot, Claude, Cursor, Windsurf for 100+ dev tasks, GH-300/600 certifications, and self-driven exploration of RAG/embeddings/prompt engineering across 3+ use cases.
7. Describe your experience working in Agile/Scrum.
Sprint planning, daily stand-ups, backlog grooming for 30+ enhancements, sprint demos to stakeholders, retrospectives — tie to "supported 20+ production releases."
8. Where do you see yourself in 3–5 years?
Growing from feature delivery into owning system design/architecture decisions — natural rapport-builder with an Enterprise Architect interviewer; shows ambition aligned with growth path.
---
B. Python & FastAPI (Q9–18)
9. Why FastAPI over Flask/Django for new services?
Native async support (ASGI), automatic OpenAPI/Swagger docs, Pydantic-based validation out of the box, better performance for I/O-bound microservices, type-hint-driven developer experience — all directly relevant to your real-time fraud engine's low-latency requirement.
10. Explain Pydantic and why it matters in your APIs.
Pydantic enforces typed request/response models, does automatic validation/coercion, and gives you a single source of truth for API contracts. In your KYC/fraud projects you used it to validate transaction payloads and onboarding requests, catching malformed data before it reached business logic.
11. Sync vs async in Python/FastAPI — when do you use `async def`?
Use async for I/O-bound operations (DB calls, HTTP calls to downstream services like identity-verification or ML scoring) so the event loop isn't blocked; use regular sync functions for CPU-bound work (FastAPI runs them in a threadpool automatically). Tie to your fraud engine's need for low-latency I/O-bound downstream calls.
12. How do you structure a FastAPI project for maintainability?
Layered architecture: routers (API layer) → schemas (Pydantic models) → services (business logic) → repositories/DAL (data access) → external integrations isolated behind interfaces (e.g., identity-verification abstraction you mentioned). This is exactly the separation you described keeping "API routing, validation, business rules, external integrations, and data-access" apart — say this explicitly, it will resonate with an architect.
13. How do you handle exceptions/errors in FastAPI?
Custom exception classes + `@app.exception_handler`, consistent error response schema (code, message, details), distinguishing validation errors (422), business rule violations (4xx), and integration failures (502/504) — as you did for validation/integration/application-level errors.
14. What is dependency injection in FastAPI and where have you used it?
`Depends()` for shared resources — DB sessions, auth/user context, config — promotes testability (can override dependencies in PyTest) and keeps route handlers thin.
15. How would you implement authentication/authorization in a FastAPI microservice?
OAuth2/JWT bearer tokens validated via a dependency, role/claims-based authorization checks in the service layer, and short-lived tokens with refresh flow; for service-to-service calls, mutual TLS or API keys/service accounts (GCP IAM in the IKS context).
16. How do you handle background/long-running tasks in FastAPI?
`BackgroundTasks` for lightweight fire-and-forget work; for heavier async processing (e.g., document OCR/classification pipelines) use a message queue (e.g., Pub/Sub on GCP, or Celery/RQ) so the API responds immediately and processing happens asynchronously — directly maps to your document-processing workflow.
17. How do you version your APIs?
URL versioning (`/v1/...`) or header-based versioning; maintain backward compatibility via additive Pydantic schema changes; deprecate old versions with a sunset plan — important in a multi-service onboarding/KYC platform.
18. What's the GIL and does it matter for your FastAPI services?
The GIL prevents true parallel execution of Python bytecode in threads; it matters less for I/O-bound FastAPI services (async event loop handles concurrency), but for CPU-bound work (ML scoring, heavy transforms) you'd offload to worker processes or a separate service rather than block the API process.
---
C. React / Frontend (Q19–24)
19. How do you manage state in a React application?
useState/useReducer for local state, Context API for cross-cutting concerns (auth, theme), and lifting state up for shared onboarding-workflow state; for larger apps, mention awareness of Redux/Zustand even if you used simpler patterns.
20. How did you connect React components to your FastAPI backend?
Fetch/axios calls to REST endpoints, handling loading/error/success states, mapping API response shapes to UI state, and centralizing API calls in a service layer — tie to "connecting UI workflows with backend REST APIs and handling API responses and application states."
21. How do you handle React performance issues (re-renders)?
`React.memo`, `useMemo`/`useCallback` to avoid unnecessary recalculation/re-render, key props for lists, code-splitting/lazy loading for large onboarding forms.
22. TypeScript vs JavaScript — why use TypeScript in your frontend?
Compile-time type safety catches integration bugs early (especially matching Pydantic response shapes to frontend types), better IDE autocomplete/refactoring safety, self-documenting interfaces for API contracts.
23. How do you handle form validation in a multi-step onboarding UI?
Client-side validation (schema-based, e.g., mirroring backend Pydantic rules) for fast feedback + always re-validate server-side; manage multi-step form state and persist progress (e.g., onboarding application status) so users can resume.
24. How would you make a React app responsive and accessible?
CSS3/responsive layout techniques (flexbox/grid, media queries), semantic HTML, keyboard navigation and ARIA roles for forms — especially relevant for a healthcare/clinician-facing product.
---
D. Databases / SQL (Q25–30)
25. How do you decide between SQL and NoSQL for a given service?
Structured, relational data with strong consistency needs (customer/application records, transaction data) → PostgreSQL/MySQL; flexible/semi-structured or high-write-throughput data (document metadata, logs) → NoSQL/MongoDB. In your onboarding platform, document metadata was SQL while actual files sat in object storage — a good real example to cite.
26. How did you optimize slow SQL queries (you mention ~20% improvement)?
Explain the process: identified slow queries via execution plans, added/optimized indexes on frequently filtered/joined columns, avoided N+1 query patterns from the API layer, used pagination for large result sets, and reduced unnecessary joins/denormalized where read-heavy.
27. Explain indexing — when can an index hurt performance?
Indexes speed up reads (WHERE/JOIN/ORDER BY) but slow down writes (INSERT/UPDATE/DELETE) since indexes must be maintained; over-indexing also bloats storage — balance based on read/write ratio of the table.
28. How do you design a schema for an audit-heavy domain like KYC/banking?
Separate transactional tables from an append-only audit/history table (or use temporal/versioned columns) to preserve compliance trails without bloating hot tables; status fields with enumerated states (APPROVED/REJECTED/REVIEW_REQUIRED) and timestamps for every transition.
29. What's a stored procedure and when would you use one vs application-layer logic?
Precompiled SQL routines executed in the DB — useful for tight, set-based operations or enforcing invariants close to the data; but you generally prefer business logic in the FastAPI service layer for testability/versioning, using stored procedures sparingly for performance-critical, DB-native operations.
30. How do you handle database transactions across multiple service calls (distributed transactions)?
Prefer the Saga pattern (choreography or orchestration) over 2PC in microservices — each service commits its local transaction and publishes an event/triggers the next step; compensating actions roll back on failure. Mention this explicitly — it's an architect-favorite question.
---
E. Microservices, System Design & Architecture (Q31–40)
31. How would you design the KYC onboarding system if you owned the architecture end-to-end?
Structure as: API Gateway → Onboarding Service (workflow/state) → Document Service (ingestion/storage) → Document Processing (OCR/classification, async via queue) → Identity Verification (isolated integration) → KYC/Decisioning Service (aggregates all signals) → AML/Risk Service (independent capability). Emphasize loose coupling, single responsibility per service, async communication for long-running steps, and isolating external integrations behind adapters — this mirrors what you actually worked on; say so.
32. How do services communicate in your microservices architecture — REST, messaging, or both?
REST/JSON for synchronous request/response (e.g., API Gateway → Onboarding Service), and async messaging (queue/event bus) for long-running or fan-out work like document processing — decouples throughput of OCR from user-facing latency.
33. How do you isolate external/third-party integrations (e.g., Azure Document Intelligence, identity providers)?
Adapter/anti-corruption layer pattern — a thin interface in your service that wraps the external API, so core business logic depends only on your own interface, not the vendor's contract. If you switch OCR vendors, only the adapter changes.
34. How would you ensure resilience if a downstream service (e.g., identity verification) is slow or down?
Timeouts, retries with exponential backoff, circuit breaker pattern to fail fast and avoid cascading failure, and a fallback/queued-retry path (e.g., mark application as PENDING_VERIFICATION rather than blocking the whole request).
35. How do you handle data consistency between the Document Service and the KYC Decisioning Service?
Eventual consistency via events (document processed → event → KYC service consumes and updates its view) rather than tight synchronous coupling; each service owns its own data store (database-per-service).
36. What would you consider for scaling the real-time fraud detection service under high transaction volume?
Horizontal scaling of stateless FastAPI instances behind a load balancer, async I/O to maximize throughput per instance, caching frequently-needed reference data (e.g., merchant risk profiles), and decoupling the ML scoring component so it can scale independently of the API layer.
37. How do you approach API contract design between frontend and multiple backend services (API Gateway pattern)?
Define contracts with OpenAPI/Swagger (FastAPI generates this automatically), use an API Gateway/BFF to aggregate calls for the frontend so the UI doesn't need to know about internal service topology, and version contracts to avoid breaking consumers.
38. Monolith vs Microservices — how do you decide?
Microservices help when teams/domains need independent deployability, scaling, and technology choices (as in a KYC platform with distinct Document/Identity/Risk domains); but they add operational complexity (distributed tracing, network latency, data consistency) — for a small product or early-stage system, a modular monolith can be the pragmatic starting point. Showing you know both sides will land well with an architect.
39. How do you approach securing microservices (auth, secrets, network)?
OAuth2/JWT for service-to-service and user auth, secrets managed via a vault/secret manager (not env files), network-level isolation (private VPC, service mesh mTLS where applicable), and least-privilege IAM roles per service (GCP service accounts).
40. What design patterns have you actually applied in your code (not just heard of)?
Adapter (external integration isolation), Strategy (different verification/decisioning rules), Repository (data-access separation), Dependency Injection (FastAPI `Depends`), and Circuit Breaker (resilience) — pick 2–3 you can talk through concretely from your projects.
---
F. Cloud, DevOps & Testing (Q41–46)
41. What's your experience with cloud platforms — how would you map it to GCP (used at IKS)?
You've worked with Microsoft Azure (Azure AI Document Intelligence, deployments); core cloud concepts transfer directly — compute (VMs/containers), managed databases, object storage, IAM, and managed AI services. Be honest: strong cloud fundamentals, ramping up on GCP-specific tooling (Cloud Run, GKE, Pub/Sub, BigQuery) quickly given the FastAPI/Docker overlap.
42. Explain your CI/CD pipeline experience.
Git/GitHub for source control and branching strategy (feature branches, PR review), Jenkins pipelines for build/test/deploy automation, running PyTest suites as a pipeline gate, and supporting 20+ production releases through this process.
43. What's your experience with Docker/containerization?
Packaging FastAPI services with a Dockerfile, defining base image/dependencies, environment-based config, and how this enables consistent deployment across environments — be ready to sketch a simple Dockerfile if asked live.
44. How do you approach testing a FastAPI service (unit vs integration)?
Unit tests with PyTest mocking external dependencies (DB, downstream APIs) to test business logic in isolation; integration tests using FastAPI's `TestClient` to hit real routes against a test DB; API/contract tests (Postman/newman) for cross-team validation of the onboarding/fraud APIs.
45. How do you test code that depends on an external AI/ML scoring service?
Mock/stub the ML component's response in unit tests to test your service logic (validation, routing to APPROVE/REVIEW/BLOCK) independent of model behavior; use contract tests to verify the request/response schema stays in sync with the real scoring service.
46. How do you ensure code quality across a team (code reviews, standards)?
PR-based code review culture, consistent use of design patterns/SOLID principles, linting/type-checking (mypy/ESLint) in CI, and using AI tools (Copilot/Claude) to assist with reviews and catch issues early — ties to your resume's AI-assisted development angle.
---
G. Generative AI / LLM (Q47–48)
47. You mention RAG and prompt engineering — explain RAG in your own words and where you've applied it.
RAG (Retrieval-Augmented Generation) retrieves relevant context (via embeddings/vector search) from a knowledge base and injects it into the LLM prompt, so the model answers grounded in real, current data instead of relying purely on its training knowledge — reduces hallucination. Mention your 3+ explored use cases and be ready to describe one concretely (even a proof-of-concept) rather than only in the abstract.
48. How have you used GitHub Copilot/Claude/Cursor beyond autocomplete in real engineering work?
Code generation for boilerplate (Pydantic models, test scaffolding), refactoring suggestions, debugging assistance by pasting stack traces/logs for root-cause hints, generating unit tests, and documentation — quantify with "100+ development tasks" from your resume, and be ready with one specific example of it saving meaningful time.
---
H. Closing / Questions for Them (Q49–50)
49. Do you have any questions for me?
Always say yes. Good questions for an Enterprise Architect interviewer:
"How is the Care Enablement Platform's microservices architecture structured today, and where is the team investing next — GCP-native services, AI/ML integration, or platform consolidation?"
"What does the path look like from a Senior Engineer role toward more architecture/design ownership at IKS?"
"What's the biggest technical challenge the team is currently solving on the FastAPI/React stack?"
50. What are your salary expectations / notice period?
Have a clear, confident number range researched beforehand (based on your experience level and market rate for Sr Full Stack in Mumbai) and state your actual notice period at TCS plainly. Keep it short — this isn't the place to over-explain.
---
Quick Reminders for Tomorrow
Since the interviewer is an Architect, always answer technical questions with the why (trade-off, alternative considered) not just the what.
Have 2–3 concrete stories ready (KYC debugging, fraud-engine design, a production incident) you can stretch or compress depending on time.
Be honest about GCP vs Azure — show transferable fundamentals rather than overclaiming direct GCP experience.
Bring one thoughtful question back to him about IKS's architecture — architects love being asked about architecture.