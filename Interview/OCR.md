# Azure AI Document Intelligence — Interview Q&A (Copy-Paste Ready)

**How to use this:** Read once fully (10 min), then revise the bolded one-liners (10 min). Answers are short, spoken-English, and honest — where something is a "good design" rather than confirmed project fact, I've marked it **[Design]** so you don't overclaim.

---

## A. CORE CONCEPT QUESTIONS

**Q1. What is Azure AI Document Intelligence?**
It's a cloud service from Azure that analyzes documents. It does OCR — reading text from images and PDFs — and beyond that, it extracts structured information like fields, key-value pairs, tables, and layout. So it's not just OCR; it's document analysis with structured output.

**Q2. How is it different from simple OCR?**
Simple OCR only gives you raw text. Document Intelligence gives you meaning — it understands the document type, identifies fields like name, DOB, document number, and returns them as structured data with confidence scores. That structured output is what our backend can actually validate against.

**Q3. Why did you use it in your project?**
In our customer onboarding and KYC flow, the customer uploads an identity document. We needed to extract reliable data from that document automatically instead of manual entry. Azure Document Intelligence gave us structured extraction with confidence scores, which we then validated against customer-provided data.

**Q4. What models or capabilities did you work with?**
Broadly, Document Intelligence offers OCR/Read, prebuilt models (like ID document, invoice, receipt), layout, and custom models. For identity documents, a prebuilt or custom model is typically used. **[Adapt to your project]** — if you used a specific model, name it; if unsure, say "we used the prebuilt identity/document model as part of the verification flow."

---

## B. INTEGRATION & FLOW

**Q5. Can you explain how you integrated Azure AI Document Intelligence into your Spring Boot application?**
The document upload comes into our Identity Verification flow through a REST API. The backend calls Azure Document Intelligence's Analyze Document API with the document. Azure processes it and returns a structured analysis response. Our service then maps the extracted fields into our domain model, normalizes values, runs business validations, compares against customer-provided data, and updates the verification status.

**Q6. Which layer or class calls Azure?**
Typically we'd have a dedicated client/service — something like an `AzureDocumentClient` or `DocumentProcessingService` — that sits below the Identity Verification service. The controller doesn't call Azure directly. **[Adapt]** — describe your actual structure if different.

**Q7. What API did you use?**
The Analyze Document API. It's a REST call — typically a POST with the document — and the analysis result comes back as a structured JSON response that we parse.

**Q8. Is document analysis synchronous or asynchronous?**
Azure Document Intelligence supports asynchronous analysis for larger documents. The POST returns a 202 Accepted with an operation location, and you retrieve the result once processing completes. **[If your project used the synchronous/SDK path]** — say "we used the SDK which handled the operation lifecycle for us."

**Q9. How did you handle the asynchronous response?**
**[Honest framing]** — "In our integration, we used the Azure SDK which abstracts the polling/operation-retrieval. If implemented manually, it would be: submit → get operation location → poll or retrieve result → parse." Don't claim polling if you didn't build it.

**Q10. How did you map the Azure response to Java objects?**
We parsed the structured JSON response into DTOs, then mapped the relevant fields — name, DOB, document number — into our domain model. We kept the mapping layer separate so the rest of the app doesn't depend on Azure's response shape.

---

## C. VALIDATION & OCR CHALLENGES

**Q11. How did you validate the extracted information?**
Azure gives extracted values; our application does the business validation. We normalize values (dates, names), compare with customer-entered data, and decide success, failure, or review-required.

**Q12. What happens if OCR extracts incorrect information?**
We don't treat Azure output as final truth. We compare against customer-provided data, and if they don't match we either fail verification or route to manual review. Confidence scores help here — low confidence triggers review.

**Q13. How did you handle date-format mismatch?**
OCR might return `12/05/1998` while the backend expects `1998-05-12`. We normalize the extracted value to a standard format before comparison. This was a real issue — a valid document could fail just because of formatting.

**Q14. What happens if the document is blurry or low quality?**
Extraction quality drops. If required fields are missing or confidence is low, we don't auto-approve — we mark it as REVIEW_REQUIRED and let a human verify, or ask the customer to re-upload.

**Q15. How do you handle low-confidence extraction?**
We check the confidence score on each field. Below a threshold, we either treat as missing or flag for manual review. We don't blindly trust low-confidence values.

**Q16. What if a required field is missing?**
Validation fails for that field. Depending on the flow, we either reject, ask for re-upload, or route to manual review. The workflow doesn't silently proceed.

---

## D. FAILURE HANDLING & RESILIENCE

**Q17. What happens if Azure is unavailable?**
Azure is an external dependency. We handle it with proper error handling — timeout, retries, and marking the document as PROCESSING or RETRY_PENDING instead of failing the whole onboarding. **[Design]** — circuit breaker with Resilience4j is standard here.

**Q18. How did you handle retries?**
**[Design/adapt]** — "We used retry with backoff for transient failures. For persistent failures, we marked the request for retry or manual intervention." If you didn't implement retries, say "we had error handling; retry could be added with Spring Retry or Resilience4j."

**Q19. How did you handle timeout?**
We set an HTTP client timeout so a slow Azure call doesn't block the request thread. If it times out, we mark the processing as pending and retry or escalate.

**Q20. How did you avoid duplicate processing?**
We use an idempotency key based on document ID or onboarding session ID. Before calling Azure, we check if that document is already being processed or already processed. A unique DB constraint backs this up.

**Q21. What happens if Azure succeeds but your DB update fails?**
**[Design]** — We should be able to recover. Options: retry the DB update, or re-fetch the Azure result (if the operation is still available) and save again. The key is that the Azure result and the DB write shouldn't be treated as one atomic operation — we use idempotent writes and a processing status so we can safely retry.

**Q22. How would you make the integration resilient?**
Timeout, retry with backoff, circuit breaker for repeated failures, asynchronous processing with status tracking, idempotency to avoid duplicates, and a dead-letter/recovery path for failed jobs.

**Q23. Would you use synchronous REST or asynchronous messaging?**
For onboarding, async is better because document analysis can be slow. The customer submits, we return a "processing" status, and the workflow continues when the result is ready. Sync is fine only if latency is acceptable.

---

## E. IDEMPOTENCY & CONCURRENCY (SENIOR)

**Q24. Two requests arrive for the same document — how do you prevent duplicate OCR?**
Idempotency key from document ID or session ID, check existing processing status in DB, and a unique constraint on the document-processing record. Second request sees "already processing" and returns the existing status.

**Q25. Azure processes the document but your app crashes before saving — what happens?**
On restart or retry, we check the processing status. If Azure result is retrievable, we re-fetch and persist. Idempotent writes prevent duplicate business effects. The status field tells us we're in an incomplete state.

**Q26. Azure is down for 5 minutes — how do you keep onboarding from failing completely?**
We don't fail the whole onboarding. We mark the document as PROCESSING or RETRY_PENDING, return control to the workflow, and retry with backoff. Circuit breaker prevents hammering Azure. Customer can continue with other steps if the flow allows.

**Q27. OCR returns a wrong DOB — how do you detect it?**
Compare extracted DOB with customer-entered DOB after normalization. If mismatch, and confidence is low or values clearly differ, we route to REVIEW_REQUIRED instead of auto-failing. Manual verification handles edge cases.

**Q28. KYC depends on OCR and OCR is still processing — what happens?**
The workflow stays in an intermediate state. KYC doesn't proceed until identity data is available. This is handled by workflow state management — the orchestrator waits for the identity verification step to complete (via event, callback, or status poll).

**Q29. Two app instances get the same event — will OCR run twice?**
Not if we use consumer idempotency: a unique business key (document ID), an inbox/processed-events table, or a DB unique constraint. Second instance sees it's already processed and skips.

---

## F. SECURITY

**Q30. How are Azure credentials stored?**
Not in code. They're stored in a secure config — Azure Key Vault, or environment-specific config/secret manager. In our banking environment, secrets aren't hardcoded or committed to git.

**Q31. Why shouldn't API keys be hardcoded?**
Hardcoded keys leak through source control, logs, and builds. Rotation is painful. In a banking app with PII, that's a compliance risk. Use a secret manager and rotate regularly.

**Q32. How do you protect customer documents?**
Encrypted in transit (HTTPS/TLS) and at rest. Access controlled — only the verification service and authorized roles. Documents aren't publicly accessible. We also avoid storing raw documents longer than needed.

**Q33. How do you prevent PII in logs?**
We never log raw document content, extracted PII fields, or full Azure responses. We log IDs (session ID, document ID, correlation ID) and statuses — not values.

**Q34. How do you secure communication with Azure?**
HTTPS with TLS. API key or Azure AD token-based auth, depending on setup. Endpoint and key from secure config, not code.

**Q35. How do you handle secrets across environments?**
Separate config per environment (dev, test, prod) with separate credentials. Managed through Key Vault or the platform's secret store. Prod secrets never shared with lower environments.

---

## G. SPRING BOOT IMPLEMENTATION

**Q36. Which Spring Boot layer calls Azure?**
A dedicated service/client layer — not the controller. Controller → Service (business logic) → Azure Client → Azure. Keeps the integration isolated and testable.

**Q37. Would you create a separate AzureDocumentService?**
Yes. It isolates Azure-specific code, makes it mockable in tests, and keeps the domain service clean.

**Q38. RestTemplate, WebClient, or Azure SDK?**
WebClient for reactive/non-blocking if needed. RestTemplate for simple blocking calls. Azure SDK if you want built-in handling of the async operation. **[Adapt]** — name what your project actually used.

**Q39. How would you handle HTTP errors?**
Catch specific status codes — 4xx for bad request (don't retry), 5xx and timeouts for transient errors (retry). Log with correlation ID and map to a domain error.

**Q40. How would you implement timeout?**
Configure the HTTP client's connect and read timeout. For WebClient, use timeout operators. Don't let a hung call block the thread.

**Q41. How would you implement retry?**
Spring Retry or Resilience4j `@Retry` with exponential backoff for transient failures only.

**Q42. How would you write unit tests?**
Mock the Azure client. Test the mapping, normalization, validation, and error handling logic in isolation. Use WireMock or Mockito for the HTTP layer.

**Q43. How would you handle malformed Azure responses?**
Defensive parsing — validate the response structure before mapping. If required fields are missing or types don't match, throw a domain exception, log it, and mark processing as failed/review.

---

## H. DATABASE / STATE

**Q44. Where do you store document-processing status?**
In a document-processing or verification table, linked to the onboarding session. Status values: PROCESSING, SUCCESS, FAILED, REVIEW_REQUIRED.

**Q45. How do you associate the result with the onboarding session?**
Foreign key to the onboarding session ID and/or document ID. The result record is tied to the session.

**Q46. How do you avoid duplicate processing?**
Unique constraint on document ID + idempotency key, plus a status check before calling Azure.

**Q47. What if Azure succeeds but DB update fails?**
Retry the DB write; the operation should be idempotent so re-saving is safe. Status field marks it incomplete until saved.

**Q48. How do you handle concurrent requests?**
Optimistic locking (version column) or pessimistic locking if contention is high. Idempotency key prevents duplicate work.

**Q49. How do you make status transitions atomic?**
Update status in a single transaction with a WHERE clause on the expected current status (e.g., `WHERE status = 'PROCESSING'`). If 0 rows updated, another process already changed it.

**Q50. Difference between workflow state, idempotency, and concurrency control?**
- **Workflow state:** Which stage is the onboarding in?
- **Idempotency:** Have I already processed this request?
- **Concurrency control:** Can two requests modify the same record safely?

---

## I. OBSERVABILITY / PRODUCTION

**Q51. What do you log?**
Correlation ID, session ID, document ID, status transitions, latency, and error codes. Never raw document content or PII.

**Q52. What should you NOT log?**
Document images, extracted PII values, API keys, full Azure response bodies.

**Q53. How do you trace a document request?**
Correlation ID propagated from the onboarding request through the Azure call and back. Session ID links it to the onboarding flow.

**Q54. How do you identify failed OCR requests?**
Search logs by status = FAILED or REVIEW_REQUIRED and error codes. Metrics on failure rate per time window.

**Q55. How would you monitor latency?**
Track time from document submission to result received. Alert if p95 crosses a threshold. Azure SDK/API also returns processing time.

**Q56. How do you identify increased Azure failures?**
Error-rate metric on Azure calls. Spike in 5xx/timeouts → alert. Circuit breaker state change is also a signal.

**Q57. How would you troubleshoot production issues?**
Correlation ID → find logs → check request payload (without PII), Azure response status, DB status, and downstream effects. Structured logs + Elasticsearch/Kibana make this fast. **[Don't claim Azure Monitor if you didn't use it.]**

---

## J. SENIOR SCENARIO — QUICK ANSWERS

**S1: Duplicate OCR prevention** → Idempotency key + DB unique constraint + status check.

**S2: App crashes after Azure success** → Processing status + idempotent re-fetch/re-save on recovery.

**S3: Azure down 5 min** → Timeout, retry with backoff, circuit breaker, PROCESSING/RETRY_PENDING status, async processing.

**S4: Wrong DOB extracted** → Normalize + compare + confidence check + REVIEW_REQUIRED if mismatch.

**S5: KYC waits on OCR** → Workflow stays in intermediate state; KYC only proceeds when identity data is ready.

**S6: Two instances, same event** → Consumer idempotency via unique business key or inbox table.

---

## K. THE 30-SECOND OPENING ANSWER (Memorize This)

> "In our digital onboarding and KYC platform, the customer uploads an identity document. Our Spring Boot backend, as part of the identity verification flow, calls Azure AI Document Intelligence's Analyze Document API. Azure analyzes the document and returns structured extracted information — fields like name, DOB, document number — with confidence scores. Our service maps that response into our domain model, normalizes values like dates, compares them with customer-provided data, and updates the verification status. Azure handles extraction; our application owns validation and the workflow decision."

---

**Final tips:**
- If you don't know an implementation detail, say: *"In our project we used the SDK which abstracted that. If implemented manually, it would be..."* — honest and senior.
- Always separate **Azure = extraction** from **our app = validation + decision**.
- Never say "Azure converts document to JSON." Say "Azure returns extracted information in a structured API response."

Good luck — you've got this.

//if we upload fgacek doucment
OCR itself does not determine whether a document is genuine or fake. Its primary responsibility is to extract information from the document.

In our onboarding flow, the document first goes through Azure AI Document Intelligence, where we extract the required fields and receive confidence information for the extracted data.

After that, our Identity Verification layer performs application-level validations. For example, we validate the document type, required fields, expiration information, and compare the extracted identity information with the information provided by the customer.

For actual authenticity verification, the application should rely on the appropriate identity or KYC verification mechanism rather than treating OCR success as proof that the document is genuine.

So if a document is readable but the identity or authenticity checks fail, we should not move the onboarding workflow forward. We can mark the identity verification as failed or review-required, depending on the reason, and the onboarding session remains in that state until the issue is resolved.

The important distinction is that Azure Document Intelligence helps us with document analysis and extraction, while the identity/KYC layer is responsible for the verification decision.
