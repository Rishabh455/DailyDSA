Deep-Dive STAR Answers — 5 Core Behavioral Questions
(In-depth versions with concrete bug stories. Swap in your real numbers/incident names where marked [customize].)
---
1. "Walk me through your resume / Tell me about yourself." — In-depth version
Keep it to ~90 seconds, structured in 4 beats: Who you are → What you built → How you built it → Why here.
> "I'm a Python full-stack developer with 3+ years at TCS, working on the Saudi National Bank account. My work sits across two connected systems.
>
> The first is a **Digital Customer Onboarding & KYC platform** — a microservices system where a customer applies, uploads identity documents, we run OCR and field extraction using Azure AI Document Intelligence, cross-verify that against onboarding data, run identity verification and AML/risk checks, and the system arrives at APPROVED, REJECTED, or REVIEW_REQUIRED. I worked across the stack here — FastAPI/Flask services, Pydantic-based API contracts, React/TypeScript frontend, and SQL for application and document metadata — and shipped 30+ business enhancements on it.
>
> The second, which I'm currently on, is a **real-time fraud detection and credit risk scoring engine** — a FastAPI service layer that takes transaction data, prepares features, calls into an ML scoring component, and returns an APPROVE/REVIEW/BLOCK decision, all under tight latency constraints since it's real-time.
>
> Along the way I've leaned heavily into AI-assisted engineering — I'm Microsoft-certified in GitHub Copilot (GH-300 and GH-600), and I've used Copilot, Claude, and Cursor across 100+ dev tasks, plus hands-on work with RAG and prompt engineering.
>
> What draws me to IKS Health specifically is that the Care Enablement Platform is almost exactly this stack — FastAPI, React, cloud-native, microservices — but applied to healthcare, which feels like a more mission-driven place to point these skills."
Why this works: it's chronological, quantified, ends on a forward-looking note tied to their platform — gives the interviewer an easy, relevant follow-up to ask.
---
2. "Why are you looking to leave TCS / Why IKS Health?" — In-depth version
Avoid anything that sounds like complaining about TCS. Frame it as pull toward IKS, not push away from TCS.
> "TCS has been a strong foundation — I got real ownership on a complex banking platform, touched the full stack, and got to work across onboarding, document processing, and now fraud detection. I'm not leaving because of a bad experience; I'm looking to move because of the **nature of the work** I want next.
>
> At TCS, I'm delivering into an existing client platform in a services model — the architecture and product direction are largely set, and my job is enhancement and stabilization within that. What I want next is to be closer to **product engineering** — owning features end-to-end on a platform that's actively evolving, where I can influence design decisions, not just implement within a fixed structure.
>
> IKS Health's Tech Engineering org — building the Care Enablement Platform on FastAPI, React, and GCP — is exactly that kind of environment, and it's healthcare, which adds a mission layer I find genuinely motivating: the platform directly reduces administrative burden so clinicians can focus on patients. That combination — product ownership + domain that matters — is why I'm here specifically for this role, not just 'a' new role."
If pushed harder ("but specifically, what's missing at TCS?"):
> "Mainly scope of ownership — in a services engagement, architecture decisions are often made upstream of my role. I want to be in the room for those decisions, which is more natural on a product team like IKS's."
---
3. "Describe a challenging bug you debugged end-to-end." — Full STAR + 3 story options
Have one primary story memorized in full depth, and 2 backups in case he asks "any other example?" or drills into a different layer (DB vs frontend vs integration).
PRIMARY STORY — OCR field-mismatch causing false REVIEW_REQUIRED
Situation:
> "On the onboarding platform, QA flagged that a growing number of applications — roughly [X]% **[customize]** — were landing in REVIEW_REQUIRED even when the submitted documents were clearly valid. This was a business-impacting bug because REVIEW_REQUIRED meant manual ops review, adding delay and cost per application."
Task:
> "I was asked to root-cause it since it touched the Document Processing → KYC Decisioning boundary, which I'd worked on directly."
Action:
> "I started by pulling API logs and correlation IDs for a sample of affected applications and traced the request through each service: Document Service → Document Processing (Azure AI Document Intelligence OCR/extraction) → KYC/Verification Service. 
>
> I noticed the extracted date-of-birth field was coming back in a different format for a subset of document types — but our Pydantic model was coercing it silently instead of failing loudly, so a wrong-but-parseable date was slipping through and then failing the *downstream* cross-check against onboarding data, which is what triggered REVIEW_REQUIRED.
>
> So the actual bug wasn't in the KYC logic — it was an upstream data-contract issue masked by permissive validation. I fixed it in two parts: (1) tightened the Pydantic model to explicitly validate and normalize date formats at the point of extraction rather than downstream, and (2) added an explicit extraction-confidence threshold check so low-confidence fields get flagged for manual review *before* they ever reach the automated cross-check, instead of silently failing later.
>
> I also added regression tests in PyTest covering the specific document-type/date-format combinations that caused this, so it can't regress silently again."
Result:
> "False REVIEW_REQUIRED cases for that document type dropped to near zero, and more importantly, we caught a class of 'silent validation' bugs and used it to justify a small team-wide push to make all extraction-boundary validation fail loudly rather than coerce silently."
Why this story is strong: it shows cross-service tracing, a root cause one layer removed from the symptom, a fix at the right layer (not just patching the symptom), and a systemic improvement — exactly what an architect wants to hear.
BACKUP STORY 2 — SQL/performance bug
> "A specific onboarding-status query started timing out under load during a high-traffic period. I checked the execution plan, found we were doing a full table scan because a filter column wasn't indexed, added a composite index on (status, created_at), and also found the API layer was calling this query once per row in a loop (N+1 pattern) — batched it into a single query. That combination is part of what got us to the ~20% query-time improvement I mention on my resume."
BACKUP STORY 3 — Integration/timeout bug (fraud engine)
> "On the fraud engine, we saw intermittent timeouts on the identity-verification integration during peak transaction volume. Traced it to no explicit timeout/retry config on the outbound call, meaning our service just hung waiting. Added a bounded timeout with exponential-backoff retry, and a circuit breaker so if the downstream service degrades, we fail fast and route to REVIEW instead of blocking the whole request."
---
4. "Tell me about a time you disagreed with a technical decision." — In-depth version
Pick a disagreement that shows technical judgment + collaborative resolution, not stubbornness.
Situation:
> "While building the identity-verification integration for the fraud engine, there was a proposal to call the downstream identity-verification and risk-scoring services **synchronously, in sequence**, inside the main request path — simplest to implement and reason about."
Task/Your position:
> "I pushed back because this was a real-time, latency-sensitive service, and chaining two synchronous downstream calls in-line meant our p99 latency would be the sum of both dependencies' worst-case latency — plus, if either service degraded, it would directly degrade our core transaction flow."
Action:
> "I proposed making those calls asynchronous — using FastAPI's async I/O to fire the necessary downstream calls concurrently rather than sequentially, with proper timeouts on each — and backed it with rough latency-budget math: sequential calls could add [X]ms in the worst case **[customize with a plausible number, e.g. 400–600ms]**, which would blow our SLA; concurrent async calls with timeouts kept us within budget even under degraded downstream performance.
>
> I didn't just object — I built a small proof-of-concept comparing sequential vs concurrent async calls with simulated latency, and walked the team through the numbers in a design review."
Result:
> "The team adopted the async/concurrent approach. It became the pattern we used for other downstream integrations in that service too, and it directly fed into the low-latency design goal I mention on my resume for the real-time evaluation path."
Key phrase to use if he probes "how did you handle pushback":
> "I try to disagree with data rather than opinion — a quick POC or back-of-envelope number usually moves the conversation faster than arguing preference."
---
5. "How do you handle production issues / on-call pressure?" — In-depth version + Recent Incident
General approach (say this first):
> "My process has four steps: **triage by business impact**, **isolate fast using logs/traces rather than guessing**, **patch with a rollback path**, then **root-cause and prevent recurrence** — I don't consider an incident closed until there's a fix *and* a reason it won't repeat."
Then give ONE recent, specific incident in full STAR — this answers "recent production issue" directly.
RECENT INCIDENT STORY — [customize with your real recent one if you have it; template below]
Situation:
> "A few weeks ago **[customize timeframe]**, we had a production incident where a batch of onboarding applications got stuck in a PENDING state instead of progressing to APPROVED/REJECTED — support started getting escalations from ops because customers were stuck mid-onboarding."
Task:
> "I was pulled in as one of the engineers who knew the Onboarding/KYC service boundary to find root cause fast, since this was customer-facing and actively growing in scope."
Action:
> "First step was checking whether it was isolated or systemic — pulled recent deployment history and saw a config change had gone out a few hours earlier affecting a downstream service timeout value. Correlated timestamps of stuck applications against the deploy time — strong match.
>
> Confirmed via logs that the KYC/Verification Service was timing out waiting on a downstream call that used to complete well within the old timeout window but was now exceeding the newly (and incorrectly) shortened one. Applications that timed out were left in PENDING instead of being retried or failed explicitly — a gap in our error handling.
>
> Immediate fix: reverted the timeout config to restore service, and ran a script to re-trigger processing for the stuck applications in PENDING so customers didn't have to resubmit anything.
> Follow-up fix: added explicit handling so a downstream timeout results in a clear REVIEW_REQUIRED or retry state instead of silently leaving the application in PENDING — so even if this class of issue recurs, it fails visibly instead of silently stalling."
Result:
> "Service was restored within [X] minutes **[customize]** of confirming root cause, all stuck applications were reprocessed with no customer resubmission needed, and the PENDING-state gap is now closed by design, not just patched for this one cause."
Closing line (ties back to your resume claim):
> "This is the same pattern behind the 60+ high-priority production issues I've resolved — fast triage, fix with rollback safety, then close the systemic gap so the same failure mode doesn't recur."
---
How to use this before the interview
Read each story out loud 2–3 times — don't memorize word-for-word, memorize the beats (Situation → Task → Action → Result).
Swap every [customize] with something real if you can recall an actual incident/number — even an approximate one is more credible than a made-up-sounding one.
If you genuinely don't have a matching real incident for #5, it's safer to generalize slightly ("a recent incident along these lines...") than to overstate specifics you can't defend if asked a follow-up.