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