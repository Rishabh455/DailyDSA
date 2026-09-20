Here is a clean, well-structured notes format ready for you to copy and paste into your documentation or notes app (like Notion, Obsidian, or Markdown files):

---
@Service
public class OnboardingService {

    private final VerificationStateService stateService;
    private final IdentityService identityService;

    @Transactional // <-- Yaha transaction shuru karna hoga
    public void completeOnboarding(Long id) {

        stateService.markInProgress(id);

        IdentityResponse response =
                identityService.verify(id);

        stateService.updateResult(id, response);
    }
}
this will be having issues.
# Spring Boot: Handling Transactions with External API Calls

## 1. The Issue: `@Transactional` + External API Anti-Pattern

If you annotate an orchestration service method (like `OnboardingService.completeOnboarding`) with `@Transactional` while it calls an external API, it creates a major architectural flaw:

* **Database Connection Exhaustion:** The database transaction remains open while waiting for the external API response (which can take seconds). This blocks connection pool resources.
* **Transaction Locks:** Long-running transactions hold locks on database rows/tables, drastically reducing application concurrency and performance.
* **Timeout Risks:** If the external API takes too long, it can trigger transaction timeouts.

Alternatively, if you **don't** use `@Transactional` on the main service, independent inner transactional methods commit immediately. If the external API fails later, the initial database changes (`markInProgress`) **cannot be rolled back** because their transaction is already closed and committed.

---

## 2. The Solution: Short-Lived Transactions & External Calls Outside TX

The best practice is to keep database transactions **short-lived**. Execute external API calls **outside** of any database transaction so that DB connections are never held hostage during network waits.

### Recommended Implementation Pattern

```java
@Service
public class OnboardingService {

    private final VerificationStateService stateService;
    private final IdentityService identityService;

    public void completeOnboarding(Long id) {
        
        // Step 1: Short transaction - Marks status as "IN_PROGRESS" and commits immediately
        stateService.markInProgress(id);

        IdentityResponse response;
        try {
            // Step 2: External API Call happens OUTSIDE any database transaction
            // (No DB locks or connections are held during this wait)
            response = identityService.verify(id);
            
            // Step 3: Short transaction - Updates the final result and commits
            stateService.updateResult(id, response);
            
        } catch (Exception e) {
            // Step 4: Handle failure in a separate transaction if needed
            stateService.markAsFailed(id, e.getMessage());
            throw e; 
        }
    }
}

```

### Key Benefits

* **Zero Connection Blocking:** Database connections are only acquired briefly during actual write operations.
* **Audit Trail Preserved:** Even if the external API fails, the initial "IN_PROGRESS" or "FAILED" status is safely recorded in the database.
* **High Concurrency:** Keeps the system scalable and prevents connection pool starvation.
Ek 3-year experienced Java/Spring Developer ke taur par aapko sabhi HTTP status codes yaad rakhne ki zaroorat nahi hai. Aapko sirf **Top 10 essential status codes** pata hone chahiye jo interview aur daily development mein sabse zyada use hote hain.

Yeh raha sabse concise cheat-sheet aur yaad rakhne ka asaan trick, jise aap apni copy mein note kar sakte hain:

---

## ⚡ 10-Minute Copy-Paste Notes: Essential HTTP Status Codes

### **1. 2xx (Success) - Sab Kuch Sahi Hai**

* **200 OK:** Request successful. (GET ya PUT ka standard response).
* **201 Created:** Naya resource successfully ban gaya. (POST request ke baad use hota hai).
* **204 No Content:** Request successful thi, par return karne ke liye koi data nahi hai. (DELETE request ke baad best practice).

### **2. 4xx (Client Error) - User/Client ki Galti Hai**

* **400 Bad Request:** Request ki syntax galat hai, ya validation fail ho gayi.
* **401 Unauthorized:** User logged-in nahi hai (Authentication missing/invalid).
* **403 Forbidden:** User logged-in hai, par uske paas us resource ko access karne ka **permission nahi hai** (Authorization failed).
* **404 Not Found:** Jo URL ya Resource aap dhoond rahe hain, wo server par exist nahi karta.
* **409 Conflict:** Resource already exist karta hai ya state clash ho rahi hai (e.g., Duplicate email registration).

### **3. 5xx (Server Error) - Server ki Galti Hai**

* **500 Internal Server Error:** Jo abhi humne discuss kiya—Unhandled exception ya NullPointerException.
* **503 Service Unavailable:** Server overloaded hai ya maintenance mode par hai.

---

## 🧠 Yaad Rakhne ka Magic Trick (First Digit Rule)

Pehla digit (First Number) hi sab kuch bata deta hai:

* **1xx = Informational** (Ruko, process chal raha hai - rarely used)
* **2xx = SUCCESS** (Sab **2**k (Thik) hai!)
* **3xx = REDIRECT** (Rasta badal gaya hai, **3**osra rasta lo)
* **4xx = CLIENT FAULT** (Aapki (**4**apki) galti hai)
* **5xx = SERVER FAULT** (Server ki (**5**arver) galti hai)

### 🔑 Interviews ke liye 3 Golden Interview Pairs:

1. **401 vs 403:** 401 = "Who are you?" (Not logged in), 403 = "I know you, but you can't go here" (No permission).
2. **200 vs 201:** 200 = Updated/Fetched, 201 = Newly Created.
3. **400 vs 500:** 400 = Client ne request galat bheji, 500 = Request theek thi par server phat gaya (Exception aa gayi).

. What happens to the remaining requests when the pool is full?They do not fail immediately; instead, they block and wait in a queue for an available connection.This wait is limited by connection-timeout (default is 30 seconds).If a connection frees up in time, the request proceeds. If the timeout is reached, HikariCP throws a SQLTransientConnectionException, resulting in a 500 Internal Server Error for the client.2. Should you simply increase the pool size (e.g., from 10 to 100) to fix slowness?No, absolutely not. This is a classic anti-pattern.Increasing connections beyond the database's hardware capacity causes heavy context switching, CPU thrashing, and database locking, making performance significantly worse. More connections $\neq$ Better performance.3. How do you calculate the correct pool size?Hikari’s Formula: $\text{Pool Size} = (\text{Core Count} \times 2) + \text{Spindle Count}$ (where spindle count is 1 for SSDs).Instance Sharing (Most Important): If your database max capacity is 20, and you run 2 instances of your service, you must split the pool: $\frac{20}{2} = 10$ max pool size per instance. Always account for other microservices sharing the same database.One-Liner for the Interview:"Increasing the connection pool doesn't improve throughput; it just shifts the bottleneck to the database. We size connection pools based on DB hardware limits, core counts, and total concurrent microservices."