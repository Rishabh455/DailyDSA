"Why didn't you make the entire onboarding process asynchronous?"

Tum bolo:

"Not every part of the onboarding workflow can be asynchronous because some steps require the result of the previous operation before the workflow can proceed. For example, during OTP verification or identity verification, the application needs the verification result before allowing the customer to proceed to the next step, so a synchronous request-response interaction is appropriate.

On the other hand, after the onboarding information is submitted, KYC/AML processing can happen independently of the customer's immediate request. The customer can remain in a pending state while the compliance processing happens, so that part is suitable for asynchronous event-driven processing using Kafka."

"Suppose KYC processing takes 3 hours. If I use synchronous REST, what's the problem? Why exactly is asynchronous Kafka better here?"
Suppose KYC ko 3 hours lagte hain.

Agar synchronous HTTP:

100 customers
     ↓
100 HTTP requests
     ↓
KYC processing
     ↓
3 hours

Depending on implementation, ye consume kar sakta hai:

server threads
HTTP connections
connection-pool resources
memory
request timeouts
infrastructure capacity

Agar enough concurrent requests aa gaye, application resource exhaustion ka shikar ho sakti hai.

So architect-level reasoning:

Long-running work
       ↓
Synchronous HTTP
       ↓
Request remains open
       ↓
Resources remain occupied
       ↓
Poor scalability
       ↓
Timeout/resource exhaustion risk

Whereas asynchronous:

Customer submits onboarding
          ↓
DB → PENDING_KYC
          ↓
Publish event
          ↓
Kafka
          ↓
KYC worker
          ↓
Process for however long required
          ↓
Store result
          ↓
ACTIVE / REJECTED

Customer ke original HTTP request ko 3 hours open rakhne ki zarurat nahi.

🔥 But there's an even deeper point

Architect tumse ye bhi pooch sakta hai:

"If asynchronous processing is better for long-running KYC, why not make OCR asynchronous too?"

Aur iska answer sirf:

"OCR fast hai"

nahi hona chahiye.

Correct question hai:

Does the onboarding workflow need the OCR result before it can move forward?

If yes:

Upload ID
   ↓
OCR
   ↓
Need verification result
   ↓
Continue onboarding

then synchronous request-response can make sense.

If OCR itself can be queued and customer can continue later:

Upload
  ↓
202 Accepted
  ↓
Kafka
  ↓
OCR
  ↓
Result

then asynchronous architecture could also be appropriate.

So communication style depends on the business dependency, not simply on whether an operation takes "a few seconds" or "a few hours."

🎯 Your final mental model

Remember this:

Synchronous: "I need your result before I can continue."

Asynchronous: "I've submitted the work; you can process it independently and tell me the result later."

And then:

Long-running synchronous work is dangerous at scale because it can keep resources occupied for a long time.

"If KYC processing could take several hours, keeping the HTTP request synchronous would be inefficient. The request could remain open for a long time and consume server-side resources, and under high concurrency this could lead to timeouts or resource exhaustion. Since KYC/AML can be processed independently after the onboarding reaches a pending state, we use an asynchronous event-driven approach. The onboarding publishes an event, KYC/AML processes it independently, and the final status is persisted and can be retrieved later."

3"If Identity Service owns identity data, why are you storing identity details in the onboarding database?"

Tumhara safe answer:

"The presence of identity-related data in the onboarding database doesn't necessarily mean that onboarding owns the identity domain. It can contain onboarding-specific information or a reference needed to execute and track the onboarding workflow. However, for our specific implementation, I would distinguish what is authoritative identity data from what is stored for onboarding purposes rather than assuming they are the same."

4."Does the onboarding service own IAM, identity and address?"

Your answer:

"No. By ownership of onboarding, I mean the onboarding service coordinates the overall customer onboarding workflow and drives it toward the final onboarding outcome. It doesn't own the IAM, identity or address capabilities. It invokes or coordinates with those external services or systems and uses their results to determine whether the customer can proceed to the next stage."