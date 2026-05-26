Idempotency means performing the same operation multiple times should produce the same result without creating duplicate side effects.

Payment Example (Best for Interview)
Client sends a payment request with a unique Transaction ID / Idempotency Key.
Server stores that key along with the transaction result.
If the same request comes again with the same key, the server does not process payment again.
Instead, it returns the previous response.
Why It Is Important
Prevents duplicate payments/orders.
Handles retries caused by network failures or timeouts.
Improves reliability in distributed systems and microservices.
Common Implementation in Spring Boot
Generate/store an Idempotency Key (UUID).
Save request status in DB/Redis.
Before processing, check whether the key already exists.
If exists → return old response.
Else → process request and save result.
Real-World Examples
Payment gateways (UPI, Stripe, PayPal)
Ticket booking systems
Order placement APIs
Bank money transfer APIs
Important Interview Point
GET requests are naturally idempotent.
POST requests are usually non-idempotent, so we implement idempotency manually using unique keys.