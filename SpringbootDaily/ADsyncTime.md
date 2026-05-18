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