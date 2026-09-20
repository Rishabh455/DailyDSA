Absolutely. Below is the **complete Story 4 — SQL Query Optimization** in a copy-paste Markdown format. I’ve kept the answers **concept-focused and interview-ready**, so you understand the reasoning instead of memorizing lines.

````md
# STORY 4 — SQL QUERY OPTIMIZATION / SLOW REVIEW-REQUIRED QUERY

## 1. Core Interview Story

### Question
**Tell me about a SQL performance issue you faced in your project and how you resolved it.**

### Answer

One SQL performance issue we faced was in the Operations/KYC dashboard of our onboarding application.

The Operations team uses this dashboard to see customers whose onboarding is still in `REVIEW_REQUIRED` or `PENDING` state and therefore needs manual intervention.

There was an API responsible for fetching these onboarding records from the database, and we noticed that the response time of this API was higher than expected.

We first checked the application flow to make sure the delay was not coming from unnecessary application-side processing. Then we analyzed the database query and used `EXPLAIN ANALYZE` to understand how PostgreSQL was executing it.

The query was filtering records based on `status` and then retrieving the latest records using `created_at DESC`.

We already had an index on `status`, but it was not efficiently supporting the complete filter-plus-ordering access pattern.

So we introduced a composite index on:

```sql
(status, created_at DESC)
````

This gave PostgreSQL an access path that better matched the query pattern and reduced unnecessary database work.

We then compared the execution plan and actual execution time before and after the change and observed an improvement in the query/API performance.

---

# 2. Why Did We Need This Query?

### Question

**Why does the application need to fetch REVIEW_REQUIRED/PENDING onboarding records?**

### Answer

Some onboarding cases cannot automatically proceed because of issues such as identity/document validation problems or other verification conditions.

Those cases move to states such as:

```text
REVIEW_REQUIRED
PENDING
```

The Operations/KYC/Support team needs to see these cases and manually review or take the required action.

So the system provides APIs for the operations dashboard to fetch these cases.

Conceptually:

```text
Customer
   ↓
Onboarding
   ↓
Identity / Document Verification
   ↓
Validation
   ↓
REVIEW_REQUIRED
   ↓
Operations / KYC Team
   ↓
Manual Review
```

---

# 3. Why Can This Query Become Slow?

### Question

**Why would this query become slow? We only have around 500–600 onboardings per day.**

### Answer

The issue is not simply the number of new customers created per day.

The more important factors are:

* Historical onboarding data keeps growing.
* The review API is queried repeatedly by operations users.
* The query filters by `status`.
* The query orders records by `created_at`.
* There may be joins with related verification data.
* The database may have to examine much more data than the small result set returned by the API.

For example:

```sql
WHERE status = 'REVIEW_REQUIRED'
ORDER BY created_at DESC
LIMIT 50
```

Even though the API only returns 50 records, the database may have to do much more work internally to find and order those records.

Important concept:

> **A small result set does not necessarily mean a small amount of database work.**

---

# 4. What Was the Query Pattern?

A simplified version of the query was:

```sql
SELECT
    session_id,
    customer_id,
    status,
    created_at
FROM onboarding
WHERE status = 'REVIEW_REQUIRED'
ORDER BY created_at DESC
LIMIT 50;
```

The requirement is:

> Get the latest 50 onboarding cases whose status is `REVIEW_REQUIRED`.

---

# 5. How Did You Debug the Slow API?

### Question

**How did you identify that the database query was the bottleneck?**

### Answer

We followed the request flow from the API layer to the database.

The investigation was roughly:

```text
Slow API
   ↓
Check application/service logic
   ↓
Identify SQL query
   ↓
Run EXPLAIN ANALYZE
   ↓
Inspect execution plan
   ↓
Identify expensive scan/sort work
   ↓
Optimize index/query
   ↓
Compare before vs after
```

The important point is that we didn't immediately add an index.

We first analyzed how PostgreSQL was actually executing the query.

---

# 6. What Is EXPLAIN?

### Question

**What is EXPLAIN?**

### Answer

`EXPLAIN` shows the execution plan that PostgreSQL's optimizer chooses or considers for the query.

It helps us understand things such as:

* Sequential Scan
* Index Scan
* Sort
* Join operations
* Estimated rows
* Estimated cost

It answers:

> **"How does PostgreSQL plan to execute this query?"**

---

# 7. What Is EXPLAIN ANALYZE?

### Question

**What is EXPLAIN ANALYZE?**

### Answer

`EXPLAIN ANALYZE` actually executes the query and gives us real runtime information in addition to the execution plan.

So:

```text
EXPLAIN
→ What plan does PostgreSQL intend to use?

EXPLAIN ANALYZE
→ Execute the query and show what actually happened.
```

Important information includes:

* Actual rows
* Actual execution time
* Number of loops
* Scan type
* Sort operations
* Planning time
* Execution time

---

# 8. What Did You Look For in EXPLAIN ANALYZE?

### Question

**What exactly did you look for in the execution plan?**

### Answer

I mainly looked at:

1. Whether PostgreSQL was doing a Sequential Scan or an Index Scan.
2. Whether there was an expensive Sort operation.
3. How many rows were being examined versus returned.
4. Whether the estimated rows were very different from actual rows.
5. The actual execution time.
6. Whether PostgreSQL was using parallel execution.

The goal was not only to check the final response time, but to understand where the database was doing unnecessary work.

---

# 9. What Is a Sequential Scan?

### Question

**What is a Sequential Scan?**

### Answer

A Sequential Scan means PostgreSQL scans the table sequentially and checks rows against the query condition.

Conceptually:

```text
Table
 ↓
Read rows
 ↓
Check condition
 ↓
Keep matching rows
```

A Sequential Scan is not automatically bad.

For a small table or a query that needs a large percentage of the rows, it can actually be the cheapest plan.

---

# 10. What Is an Index Scan?

### Question

**What is an Index Scan?**

### Answer

An Index Scan uses an index to locate relevant rows instead of broadly scanning the table.

Conceptually:

```text
Query
 ↓
Index
 ↓
Find matching rows
 ↓
Read required table rows
```

It is generally useful when the query is selective and only a relatively small portion of the table is required.

---

# 11. When Would PostgreSQL Still Use Sequential Scan Even If an Index Exists?

### Question

**Why wouldn't PostgreSQL always use the index if the index exists?**

### Answer

Because PostgreSQL chooses the plan based on estimated cost.

If:

* the table is small, or
* the query needs a large percentage of the rows, or
* the condition has poor selectivity,

then a Sequential Scan may be cheaper than using the index.

For example:

```text
1,000,000 rows
700,000 match the condition
```

Fetching most of the table through an index may cost more than scanning the table sequentially.

Important:

> **The existence of an index does not guarantee that PostgreSQL will use it.**

---

# 12. What Is Selectivity?

### Question

**What is selectivity?**

### Answer

Selectivity describes how effectively a condition reduces the number of rows that need to be considered.

Example:

```text
1,000,000 total rows
50,000 match REVIEW_REQUIRED
```

Only 5% of rows match, so the condition is relatively selective.

But:

```text
1,000,000 total rows
700,000 match REVIEW_REQUIRED
```

The condition has poor selectivity.

Mental model:

```text
Few rows match
→ High selectivity
→ Index can be useful

Many rows match
→ Low selectivity
→ Sequential Scan may be cheaper
```

---

# 13. What Was the Original Problem With the Status Index?

### Question

**We already had an index on status. Why wasn't that enough?**

### Answer

The existing index on `status` could help locate `REVIEW_REQUIRED` records.

But our query had another requirement:

```sql
ORDER BY created_at DESC
```

So the database could still need additional work to produce the matching rows in the required order.

Conceptually:

```text
Index(status)
   ↓
Find REVIEW_REQUIRED
   ↓
Additional work
   ↓
Sort by created_at DESC
   ↓
LIMIT 50
```

So the status-only index did not fully match the query's access pattern.

---

# 14. Why Did We Add a Composite Index?

### Question

**Why did you create a composite index on `(status, created_at)`?**

### Answer

Because the common query pattern was:

```sql
WHERE status = 'REVIEW_REQUIRED'
ORDER BY created_at DESC
LIMIT 50
```

A composite index:

```sql
CREATE INDEX idx_onboarding_status_created_at
ON onboarding(status, created_at DESC);
```

matches both parts of the access pattern:

```text
Filter by status
       ↓
Order by created_at
       ↓
Take latest records
```

So the index can potentially reduce the amount of filtering and sorting work.

---

# 15. Why Status First and Created_at Second?

### Question

**Why did you put `status` first and `created_at` second?**

### Answer

Because our query first applies:

```sql
WHERE status = 'REVIEW_REQUIRED'
```

and then needs:

```sql
ORDER BY created_at DESC
```

So:

```sql
(status, created_at DESC)
```

matches the filter-plus-ordering pattern.

Conceptually:

```text
Composite Index
       ↓
REVIEW_REQUIRED subset
       ↓
Already ordered by created_at
       ↓
Take latest 50
```

The result of the SQL query would be the same regardless of index order. The difference is how efficiently the database can access the data.

---

# 16. Why Not `(created_at, status)`?

### Question

**What happens if you create the index as `(created_at, status)`?**

### Answer

Then the index is primarily ordered by `created_at`.

Conceptually:

```text
2026-09-20
   ├── COMPLETED
   ├── PENDING
   ├── REVIEW_REQUIRED
   └── FAILED

2026-09-19
   ├── COMPLETED
   ├── REVIEW_REQUIRED
   └── ...
```

Our query does not restrict `created_at`, so PostgreSQL may have to inspect more index entries and filter by status to find enough `REVIEW_REQUIRED` records.

Therefore, for our particular query pattern:

```text
(status, created_at)
```

is a better fit.

Important:

> **This does not mean `status` should always come first in every composite index.**

Index order depends on the actual query patterns and workload.

---

# 17. Why Not Use Two Separate Indexes?

### Question

**Why not create one index on `status` and another on `created_at`?**

### Answer

PostgreSQL can sometimes combine multiple indexes depending on the execution plan.

However, our query has a very specific access pattern:

```text
Filter
status = REVIEW_REQUIRED

+
Order
created_at DESC
```

A composite index:

```text
(status, created_at)
```

directly represents that access pattern.

Therefore, for this workload, the composite index was more appropriate than relying on separate indexes.

Important:

> **Separate indexes are not always bad, but the composite index better matches this particular query.**

---

# 18. What Is the Downside of Adding Indexes?

### Question

**Why not create indexes on every column?**

### Answer

Indexes improve read performance, but they have costs.

Every insert/update/delete may also need to maintain the relevant indexes.

Indexes also consume storage.

So:

```text
More indexes
→ Faster reads in some cases
→ More storage
→ More write/maintenance overhead
```

Therefore:

> **Indexes should be created based on actual query patterns and measured performance needs, not added blindly.**

---

# 19. How Did You Verify the Index Improved Performance?

### Question

**How did you confirm that the composite index actually improved the query?**

### Answer

We compared the `EXPLAIN ANALYZE` results before and after the index.

We compared:

```text
Scan type
Sort operation
Rows examined
Actual execution time
Loops
Execution plan
```

The important point is:

> **We didn't only compare the final response time. We compared the execution plans to understand whether the database was doing less work.**

If actual numbers are available from testing:

```text
Before: ~40 ms
After: ~0.12 ms
```

say explicitly that these were measured in the test environment.

Do not present test numbers as production numbers unless they were actually measured in production.

---

# 20. What Is Estimated Cost?

### Question

**What is the difference between estimated cost and actual execution time?**

### Answer

Estimated cost is an internal value used by PostgreSQL's optimizer to compare execution plans.

It is NOT milliseconds.

For example:

```text
Plan A → cost 25,000
Plan B → cost 8,000
```

PostgreSQL may prefer Plan B because its estimated cost is lower.

Actual execution time is the real time taken when the query executes.

So:

```text
Estimated Cost
→ Optimizer estimate
→ Used for plan comparison
→ Not milliseconds

Actual Execution Time
→ Real runtime
→ Measured during EXPLAIN ANALYZE
```

Key line:

> **Cost helps PostgreSQL choose a plan; execution time tells us what actually happened.**

---

# 21. What Is Actual Execution Time?

### Question

**What does actual execution time mean?**

### Answer

It is the actual time PostgreSQL spent executing the query or a particular execution-plan node.

`EXPLAIN ANALYZE` provides real runtime information.

For the complete query, PostgreSQL also reports:

```text
Planning Time
Execution Time
```

Planning time is the time spent creating the plan.

Execution time is the time spent actually executing the query.

---

# 22. Why Isn't Estimated Cost Measured in Milliseconds?

### Question

**Why doesn't PostgreSQL simply give cost in milliseconds?**

### Answer

Because cost is primarily used as a relative measure for comparing execution plans.

Actual runtime depends on:

* hardware
* cache state
* disk I/O
* data distribution
* system load
* statistics

So PostgreSQL uses cost units rather than saying:

```text
cost = 500 ms
```

---

# 23. What Is a Bitmap Index Scan?

### Question

**What is a Bitmap Index Scan?**

### Answer

A Bitmap Index Scan is another way PostgreSQL can use an index when many rows may match.

Conceptually:

```text
Bitmap Index Scan
       ↓
Identify relevant rows/pages
       ↓
Bitmap Heap Scan
       ↓
Fetch relevant table pages
```

It can be useful when an ordinary Index Scan would require many individual random lookups, but a full Sequential Scan would also be wasteful.

Simple comparison:

```text
Sequential Scan
→ Broadly scan the table

Index Scan
→ Follow index to matching rows

Bitmap Scan
→ Use index to identify many relevant pages,
  then fetch those pages efficiently
```

Do not say that Bitmap Scan is always better.

The optimizer decides based on estimated cost.

---

# 24. Why Did We Not Just Force an Index?

### Question

**Why not force PostgreSQL to use our composite index?**

### Answer

Because the goal is not to force an index.

The goal is to give PostgreSQL a good access path and allow the optimizer to choose the cheapest suitable plan.

If the data distribution changes and a Sequential Scan becomes cheaper, PostgreSQL should be allowed to choose it.

---

# 25. What If REVIEW_REQUIRED Has Poor Selectivity?

### Question

**Suppose REVIEW_REQUIRED becomes a very common status. Would you still use `(status, created_at)`?**

### Answer

I would not automatically assume the index is beneficial.

If:

```text
1,000,000 rows
700,000 are REVIEW_REQUIRED
```

then the predicate has poor selectivity.

PostgreSQL may decide a Sequential Scan is cheaper.

However, because our query also has:

```sql
ORDER BY created_at DESC
LIMIT 50
```

the composite index could still be useful depending on the data distribution and plan.

So the correct approach is:

> **Check the execution plan and benchmark the actual workload rather than assuming the index is always better.**

---

# 26. What Is the Difference Between Index and Composite Index?

### Question

**What is a composite index?**

### Answer

A normal index can be created on one column:

```sql
CREATE INDEX idx_status
ON onboarding(status);
```

A composite index is created across multiple columns:

```sql
CREATE INDEX idx_status_created_at
ON onboarding(status, created_at DESC);
```

It is useful when queries commonly use those columns together in a specific access pattern.

---

# 27. What Happens to Writes After Adding the Index?

### Question

**How can adding an index affect INSERT/UPDATE operations?**

### Answer

When rows are inserted or updated, the database also has to maintain the relevant index structures.

Therefore indexes can add some write overhead.

To verify the trade-off, we should monitor:

```text
Read latency
Insert latency
Update latency
Write throughput
Database resource usage
```

The goal is:

```text
Read improvement
+
Acceptable write overhead
```

---

# 28. How Did You Verify Writes Were Not Negatively Affected?

### Question

**How did you confirm the new index didn't hurt INSERT/UPDATE performance?**

### Answer

We compare both sides of the workload.

For reads:

```text
EXPLAIN ANALYZE
API response time
Query execution time
```

For writes:

```text
INSERT latency
UPDATE latency
Write throughput
Database resource usage
```

Then we check whether the read improvement is significant while the additional write overhead remains acceptable.

Do not say:

> "Indexes never affect writes."

Say:

> **"Indexes add some write and storage overhead."**

---

# 29. Offset Pagination Problem

### Question

**What if the Operations API needs deep pagination?**

### Answer

Offset pagination can become less efficient for large offsets.

Example:

```sql
SELECT ...
FROM onboarding
WHERE status = 'REVIEW_REQUIRED'
ORDER BY created_at DESC
LIMIT 50 OFFSET 100000;
```

The database may still need to walk through a large number of earlier records and discard them before returning the next 50.

So:

```text
OFFSET 0
→ Small amount of skipping

OFFSET 100000
→ Much more data may need to be skipped
```

---

# 30. Why Use Keyset/Cursor Pagination?

### Question

**How would you improve deep pagination?**

### Answer

Use keyset/cursor pagination.

Instead of saying:

> Give me page number 2001.

we say:

> Give me the next 50 records after the last record from the previous page.

For example, use:

```text
created_at
+
id
```

as the cursor.

Conceptually:

```sql
SELECT ...
FROM onboarding
WHERE status = 'REVIEW_REQUIRED'
  AND (
       created_at < :lastCreatedAt
       OR (
           created_at = :lastCreatedAt
           AND id < :lastId
       )
  )
ORDER BY created_at DESC, id DESC
LIMIT 50;
```

This allows the database to continue from a known position.

---

# 31. Why Do We Need ID Along With created_at?

### Question

**Why can't we use only created_at as the cursor?**

### Answer

Because multiple onboarding records can have exactly the same timestamp.

For example:

```text
S1001 → 10:30:00
S1002 → 10:30:00
S1003 → 10:30:00
```

If we use only `created_at`, the cursor can become ambiguous.

So we use:

```text
created_at + id
```

where `id` acts as a tie-breaker.

This gives us a deterministic ordering.

---

# 32. Keyset vs Offset Pagination

```text
OFFSET PAGINATION
-----------------
Page number
    ↓
Skip earlier rows
    ↓
Return next 50

Simple
But deep pages can become expensive


KEYSET PAGINATION
-----------------
Last seen created_at + id
    ↓
Continue from that position
    ↓
Return next 50

Better for large/deep datasets
```

---

# 33. Where Does the Composite Index Help With Pagination?

For the keyset query, the index can be aligned with the filtering and ordering pattern:

```sql
CREATE INDEX idx_review_pagination
ON onboarding(status, created_at DESC, id DESC);
```

Conceptually:

```text
status = REVIEW_REQUIRED
        ↓
created_at DESC
        ↓
id DESC
        ↓
continue from cursor
        ↓
fetch next 50
```

Again, the exact index should be validated using the actual query and execution plan.

---

# 34. Could We Use Caching Instead?

### Question

**You mentioned caching was already present. Why didn't you simply add more caching?**

### Answer

Caching and query optimization solve different problems.

Caching can reduce repeated reads, but it does not fix an inefficient underlying query when the data is dynamic or frequently changing.

In our case, the Operations dashboard required relatively current onboarding states, so we first optimized the database access pattern itself.

Caching can still be useful where appropriate, but it should not automatically be the first solution to every slow query.

---

# 35. Did You Optimize the Query or the Database?

### Question

**What exactly did you optimize?**

### Answer

The SQL access pattern itself was acceptable from a functional perspective.

The main optimization was at the database access-path level by introducing a composite index that better matched the query's filtering and ordering pattern.

So the core change was:

```text
Existing
index(status)

↓
Improved
index(status, created_at DESC)
```

---

# 36. What Was the Actual Root Cause?

### One-line answer

> **The review-query workload repeatedly filtered by `status` and ordered by `created_at`, but the existing indexing strategy did not efficiently support that combined access pattern as the dataset grew.**

---

# 37. What Was the Fix?

### One-line answer

> **We introduced a composite index on `(status, created_at DESC)` after validating the query with `EXPLAIN ANALYZE`.**

---

# 38. What Was the Result?

### One-line answer

> **The database had a more appropriate access path for the review query, reducing unnecessary scanning/sorting work and improving API response time.**

If using actual test numbers:

```text
Before → ~40 ms
After  → ~0.12 ms
```

Say:

> **“These numbers were observed in my test environment, so actual production performance can vary.”**

---

# 39. 60-Second Final Story

### Interviewer:

**“Tell me about the SQL optimization issue you solved.”**

### Answer

> One issue we faced was in the Operations/KYC dashboard of our onboarding application. The dashboard is used to identify customers whose onboarding is in `REVIEW_REQUIRED` or `PENDING` state and needs manual intervention.
>
> The API responsible for fetching the review cases started taking more time as the underlying onboarding data grew.
>
> I traced the API flow and identified the database query as the bottleneck. We then used `EXPLAIN ANALYZE` to understand the execution plan. The query was filtering on `status` and ordering the results by `created_at DESC`. We already had an index on `status`, but it wasn't efficiently supporting the complete access pattern.
>
> We introduced a composite index on `(status, created_at DESC)`. This allowed PostgreSQL to use an access path better aligned with both the filter and ordering requirements.
>
> We compared the execution plan and actual execution time before and after the change and observed an improvement in the query/API performance.
>
> The key learning was that query optimization is not just about adding an index; we first need to understand the workload, execution plan, data distribution, and then choose an index that matches the actual query pattern.

---

# 40. Story 4 — Core Mental Model

Remember this instead of memorizing the full story:

```text
BUSINESS PROBLEM
Operations needs latest REVIEW_REQUIRED cases
             ↓
API became slow
             ↓
IDENTIFY SQL
             ↓
EXPLAIN ANALYZE
             ↓
Understand execution plan
             ↓
Existing status index insufficient
             ↓
Query pattern:
WHERE status
ORDER BY created_at DESC
LIMIT 50
             ↓
Composite index
(status, created_at DESC)
             ↓
Compare before/after
             ↓
Improved read performance
```

---

# 41. SQL Concepts You Must Know From This Story

```text
EXPLAIN
→ Planned execution plan

EXPLAIN ANALYZE
→ Actual execution + runtime statistics

Sequential Scan
→ Scan table broadly

Index Scan
→ Use index to locate relevant rows

Bitmap Scan
→ Use index to identify relevant pages and fetch them efficiently

Estimated Cost
→ Optimizer's internal estimate
→ NOT milliseconds

Actual Execution Time
→ Real runtime

Selectivity
→ How much a condition narrows the dataset

Composite Index
→ Index across multiple columns

(status, created_at)
→ Matches filter + ordering pattern

Keyset Pagination
→ Continue using last seen key instead of large OFFSET

Index Trade-off
→ Faster reads, additional write/storage overhead
```

---

# 42. Important Interview Corrections

## Don't say:

> “Composite key.”

### Say:

> **“Composite index.”**

---

## Don't say:

> “Cost is the time taken by the operation.”

### Say:

> **“Cost is an optimizer estimate used to compare execution plans.”**

---

## Don't say:

> “Sequential scan is bad.”

### Say:

> **“Whether a sequential scan is good or bad depends on the table size, selectivity, and workload.”**

---

## Don't say:

> “If an index exists, PostgreSQL will use it.”

### Say:

> **“PostgreSQL chooses the plan based on estimated cost.”**

---

## Don't say:

> “Created_at is another filter.”

### Say:

> **“The query filters by status and orders by created_at.”**

---

## Don't say:

> “created_at first will scan all one million rows.”

### Say:

> **“With created_at as the leading column and no created_at restriction, the index may require PostgreSQL to examine more entries and filter by status.”**

---

## Don't say:

> “Status should always be first.”

### Say:

> **“For this particular query pattern, status is an appropriate leading column.”**

---

## Don't say:

> “Offset pagination won't work.”

### Say:

> **“Offset pagination works, but large offsets can become increasingly expensive.”**

---

# 43. Final One-Line Answer for Last-Minute Revision

> **“We had a slow Operations/KYC API for fetching REVIEW_REQUIRED onboarding cases. I used EXPLAIN ANALYZE to identify the database access pattern, found that the existing status index didn't efficiently support the status filter plus created_at ordering, and introduced a composite `(status, created_at DESC)` index to improve the read path.”**

```
```
