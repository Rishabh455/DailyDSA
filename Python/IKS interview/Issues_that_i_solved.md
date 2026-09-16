Bilkul. Interview mein **long story nahi bolni**. Har bug ko **Problem → Root Cause → Fix → Result** mein 30–60 sec mein explain karo.

Neeche tumhari shared production-bug stories ki **direct speaking scripts** hain.

---

## 1. False `REVIEW_REQUIRED` — DOB / Document Processing Bug

One production issue I worked on was where valid onboarding applications were unnecessarily going into REVIEW_REQUIRED.

I traced the affected requests using correlation IDs from Document Processing to KYC and found that for a particular document type, DOB was being extracted in a different format than what the downstream KYC validation expected.

Our validation at the extraction boundary was too permissive, so the incorrect representation passed through and the KYC cross-check failed.

I fixed it by enforcing stricter validation, normalizing supported date formats, and adding regression tests for the affected document types.

After the fix, the false REVIEW_REQUIRED cases dropped to near zero.

The key lesson was to fix the data-contract issue at the service boundary rather than patching the downstream KYC logic.

**Memory:**
**False Review → Trace → DOB Format → Validation → Fix → Near Zero**

---

# 2. Onboarding Applications Stuck in `PENDING` — Timeout Config Bug

One production incident was where a batch of onboarding applications started getting stuck in PENDING instead of progressing to APPROVED or REJECTED.

I checked recent deployments and correlated the incident timing with a configuration change. We found that a downstream service timeout had been shortened incorrectly.

Logs confirmed that KYC requests were timing out because the downstream response was taking longer than the new timeout.

There was also an error-handling gap: after the timeout, the application wasn't moved to a retry or review state, so it remained silently stuck in PENDING.

We immediately rolled back the timeout configuration and re-triggered processing for the affected applications.

For the permanent fix, we added explicit timeout handling so failures result in retry or REVIEW_REQUIRED instead of silently remaining PENDING.

This restored processing without requiring customers to resubmit their applications.

**Memory:**
**PENDING → Deployment → Timeout → Error Handling Gap → Rollback → Reprocess → Retry/Review**

---

# 3. Fraud Engine — Identity Verification Timeout

We had an intermittent timeout issue in the Fraud Engine while calling the identity-verification service during peak transaction volume.

I found that the outbound call didn't have an explicit bounded timeout or retry policy, so when the downstream service became slow, our requests could remain blocked waiting for a response.

I added a bounded timeout to prevent indefinite waiting, limited retries with exponential backoff for transient failures, and a circuit breaker for sustained downstream degradation.

When the circuit opens, we fail fast and route the transaction to REVIEW instead of continuing to call the unhealthy service.

So the overall improvement was: timeout prevents hanging, retry handles transient failures, and circuit breaker protects us from sustained downstream failures.

**Memory:**
**Peak Load → Hanging Call → Timeout → Retry/Backoff → Circuit Breaker → REVIEW**

---

# 4. Query Timeout / Database Performance Bug — Index + N+1

We had an onboarding-status API that started timing out under high traffic.

I checked the database execution plan and found that the query was doing a full table scan because the filtering pattern wasn't properly indexed.

I added a composite index aligned with the query's status filter and created-at ordering.

While investigating the API flow, I also found an N+1 query pattern where we were fetching related data separately inside a loop.

For example, 100 applications could result in roughly 101 database queries.

I changed that to batch-fetch the related data using the application IDs and then mapped the results in memory.

So we improved both the database access path and the number of database round trips, contributing to the query-time improvement mentioned on my resume.

**Memory:**
**Timeout → Execution Plan → Full Scan → Index → N+1 → Batch Query → Performance**

---

# 🔥 If interviewer asks: "Tell me about a production bug you solved"

Don't randomly tell the longest one. Use this **generic opening** and then choose the relevant story:

One production issue I worked on involved a customer-facing workflow where the system wasn't progressing as expected.

I first reproduced and traced the issue using logs, correlation IDs, and the service flow rather than immediately changing code.

Once I identified the root cause, I applied an immediate safe fix to restore the service and then implemented a permanent fix to address the underlying failure mode.

I also added regression or monitoring coverage so that the same issue wouldn't silently recur.

That approach — fast triage, safe recovery, root-cause fix, and prevention — is something I've followed across multiple high-priority production issues.

---

## 🧠 सबसे important: 4 stories ko ek table mein yaad karo

| Bug                       | Root Cause                         | Fix                                             |
| ------------------------- | ---------------------------------- | ----------------------------------------------- |
| **False REVIEW_REQUIRED** | DOB format + permissive validation | Strict validation + normalization               |
| **PENDING stuck**         | Wrong timeout + error-handling gap | Rollback + reprocess + retry/review             |
| **Fraud timeout**         | No bounded timeout/retry           | Timeout + exponential backoff + circuit breaker |
| **DB timeout**            | Full scan + N+1 queries            | Composite index + batch query                   |

### Interview mein depth badhani ho to bas ye sequence follow karo:

**What happened?**
↓
**How did you investigate?**
↓
**What was the root cause?**
↓
**What exactly did you change?**
↓
**What happened after the fix?**

**Important:** exact numbers like `[X] minutes`, exact timeout values, percentage improvement, number of applications, etc. **tabhi bolo jab tumhare actual project data mein woh numbers known hon**. Interviewer follow-up mein "How did you measure that?" pooch sakta hai.
