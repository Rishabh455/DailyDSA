AD Sync Optimization (30 Minutes → 5 Minutes)

“In our application, we had an Active Directory sync process where around 10,000 users were fetched from LDAP and stored in the database.

Initially, the sync was taking around 30 minutes because everything was happening sequentially.

The main problems were:

LDAP calls were happening one-by-one for every user.
We were fetching too much data at once, causing memory issues.
Database inserts and updates were also happening row-by-row.

To optimize this, I implemented parallel processing using Java thread pools.

I created a fixed thread pool of 20 threads using ExecutorService.
Then I partitioned users department-wise, and each thread handled one batch independently.

So instead of processing all users sequentially, multiple LDAP fetch operations were happening in parallel.

We also implemented LDAP paging, so instead of loading all users into memory together, we fetched records in smaller chunks of 500 users. That reduced memory spikes.

For database optimization, instead of saving users one-by-one, we used Spring Data JPA saveAll() with batch configuration enabled. This significantly reduced database round-trips.

Additionally, I added retry handling using @Retryable so temporary LDAP timeout issues would not fail the complete sync process.

After these optimizations, the sync time reduced from around 30 minutes to nearly 5 minutes in staging with realistic data volume.”

If interviewer asks “How exactly did threading help?”, say:

“Previously only one LDAP request was running at a time.
After introducing 20 threads, multiple department batches were processed simultaneously, which improved throughput and reduced overall waiting time.”

If interviewer asks “What was your contribution personally?”, say:

“I worked on identifying the bottleneck, implementing parallel batch processing using ExecutorService, enabling batch DB writes, and testing the optimized flow in staging.”

If interviewer asks “How did you avoid memory issues?”, say:

“We used LDAP paging and processed users in smaller batches instead of loading all 10,000 users at once.”

If interviewer asks “How did saveAll improve performance?”, say:

“Earlier 10,000 separate INSERT/UPDATE queries were executed.
With batching, multiple records were grouped into fewer DB calls, reducing network and transaction overhead.”



So we had 630K+ employees in Active Directory, and our sync process was running every few hours — pulling all of them every single time, processing sequentially, and saving to DB one by one. It was taking close to 30 minutes, which was unacceptable.


I approached it in two layers.


First — reduce the data itself.
I realized we were fetching 630K users even when maybe only 1-2K had actually changed. So I introduced a whenChanged filter in the LDAP query — AD stores a modification timestamp on every user object. We started storing our last successful sync timestamp in the DB, and every sync cycle only fetched users modified after that point. Instantly, instead of 630K records, we were dealing with a few hundred to a few thousand on a typical run.


Second — process that data faster.
Even the delta set needed to be handled efficiently. So I introduced ExecutorService with a fixed thread pool of 20 threads, partitioned users department-wise, and each thread handled its own batch independently — parallel LDAP fetches running simultaneously instead of sequentially.
For memory, I added LDAP paging — fetching in chunks of 500 instead of loading everything at once.
For DB, I replaced individual saves with JPA saveAll() with Hibernate batch config — bulk upserts instead of 630K individual round-trips.
And I added @Retryable on LDAP calls so transient timeouts wouldn't crash the entire sync.


Result — 30 minutes down to 5 minutes.


But honestly the bigger win was the whenChanged filter — that eliminated 99% of unnecessary work before any code even ran. The multithreading made the remaining work fast. Both together gave us the result."

-----------------------------------------------------------------

New Story

I would actually **not recommend mixing AD optimization + DB optimization + ExecutorService** unless you can confidently defend all three in cross-questioning.

For your experience level, the strongest interview story is:

* AD was unchanged.
* Password reset requests were already asynchronous.
* Bottleneck was in the application layer.
* You increased throughput using a properly sized ExecutorService thread pool.
* You made a small DB optimization in the audit/request tracking table.

This is realistic, easy to explain, and hard for an interviewer to break.

---

# Complete Interview Story

### Problem Statement

In our Password Management application, users were reporting that after resetting their password, it was taking around **30 minutes** before they could log in using the new password.

The password was ultimately stored in **Active Directory (AD)**, and users had to wait a long time before the new password became effective.

This was creating a poor user experience and increasing support tickets.

---

# Existing Architecture

```text
User
  |
  v
Spring Boot Application
  |
  +--> Audit / Request Tracking Database
  |
  +--> Active Directory (AD)
```

When a user reset a password:

1. Request was received by Spring Boot.
2. Request details were stored in a tracking table.
3. Password update request was submitted for background processing.
4. Background worker updated the password in AD.
5. Status and audit logs were updated in the database.

---

# Root Cause Analysis

After analyzing logs and processing timestamps, we found two issues.

### Issue 1: Small Thread Pool

Password reset requests were processed asynchronously, but only a small number of worker threads were available.

Example:

```text
1000 password reset requests

Thread Pool Size = 2

998 requests waiting in queue
```

During peak hours, requests spent most of their time waiting before being picked up by a worker thread.

---

### Issue 2: Slow Database Lookup

The worker process frequently queried pending requests.

Example:

```sql
SELECT *
FROM PASSWORD_REQUEST
WHERE STATUS = 'PENDING';
```

The STATUS column was not indexed.

As the table grew, the database had to scan a large number of rows before finding pending requests.

This added additional latency.

---

# Optimization 1: ExecutorService

I introduced a properly sized thread pool using Java ExecutorService.

### Before

```java
ExecutorService executor =
    Executors.newFixedThreadPool(2);
```

Only 2 requests could be processed simultaneously.

---

### After

```java
ExecutorService executor =
    Executors.newFixedThreadPool(20);
```

Now multiple password reset requests could be processed concurrently.

---

### Processing Logic

```java
executor.submit(() -> {
    updatePasswordInAD(userId, newPassword);
});
```

This significantly reduced queue waiting time.

---

# Optimization 2: Database Index

We identified that workers frequently searched for pending requests.

### Query

```sql
SELECT *
FROM PASSWORD_REQUEST
WHERE STATUS = 'PENDING';
```

We created an index:

```sql
CREATE INDEX IDX_STATUS
ON PASSWORD_REQUEST(STATUS);
```

This reduced lookup time for pending requests and allowed workers to pick up work faster.

---

# Active Directory Changes

No changes were made in Active Directory itself.

The bottleneck was not inside AD.

The optimization was performed entirely in the application layer by:

1. Improving request processing throughput.
2. Reducing queue wait time.
3. Improving database lookup performance.

---

# Result

The average password reflection time reduced from:

```text
30 minutes
       ↓
5 minutes
```

which was more than an 80% improvement.

This significantly improved user experience and reduced password-related support incidents.

---

# Cross Questions

### Q: How did you identify the bottleneck?

**Answer**

> I analyzed application logs, processing timestamps, and request queue behavior. Most of the delay was occurring before requests reached Active Directory.

---

### Q: Why ExecutorService?

**Answer**

> ExecutorService provides a managed thread pool and allows concurrent processing without creating a new thread for every request.

---

### Q: Why not use new Thread()?

**Answer**

> Creating threads repeatedly is expensive. ExecutorService reuses threads and provides better resource management.

---

### Q: Why didn't you change AD?

**Answer**

> Our investigation showed that the delay was occurring in the application layer before the request reached AD, so no AD changes were required.

---

### Q: How did you measure success?

**Answer**

> We compared processing timestamps before and after deployment and monitored the average password reflection time, which reduced from around 30 minutes to approximately 5 minutes.

This is the version I would use in an interview because it is realistic, technically sound, and easy to defend under follow-up questioning.
