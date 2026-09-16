Question 1 — Easy

"Rishabh, can you explain the banking onboarding project you worked on, from the customer's request to final onboarding completion?"
"I worked on a digital customer onboarding platform for a banking application. The objective was to allow customers to complete the onboarding journey digitally without manual intervention.

The journey starts with creating an onboarding session and verifying the customer's mobile number through OTP. Once mobile verification is completed, the customer submits their national ID, which goes through document processing and OCR-based verification.

The customer then provides their address information, which is validated through the address-validation flow. After that, additional onboarding information is submitted.

Once the required onboarding information is completed, the system moves toward credential creation through the IAM system. The onboarding then enters the KYC/AML processing stage, where the customer's information is evaluated and compliance checks are performed.

Based on the final compliance result, the onboarding can eventually reach an ACTIVE or REJECTED state."

2.Okay, you said there are several stages. How does your application know which stage the customer is currently in?"

"We used Spring State Machine to control the valid transitions in the onboarding workflow, while the onboarding session persisted its current step in PostgreSQL. So the state machine handled the workflow logic, and the database gave us the persisted state for the session."

3."Why did you need a State Machine? Couldn't you simply store current_step in the database and use an if-else or switch statement to decide what the next step should be?"
"We could have used only a current_step field with if-else logic, but the onboarding workflow has multiple states and defined transitions between them. We used Spring State Machine to explicitly model those states and control valid transitions based on events. The database current_step gives us the persisted position of the onboarding session, while the state machine manages the workflow transition logic. This also makes the workflow easier to extend and avoids spreading transition logic across multiple APIs."

4."Suppose the customer is currently in ID_VERIFIED state. What prevents the customer from directly calling the credentials API and skipping address verification?"
The customer can technically send the API request, but the backend validates the current onboarding state and the allowed transition. If credentials creation is not a valid transition from the current state, the request is rejected, so the customer cannot legitimately bypass the required onboarding steps."
current_step
    ↓
Where is the customer?

State Machine
    ↓
What transitions are allowed?

Backend validation
    ↓
Can this API request cause that transition?
5.Okay. Suppose the customer is in ID_VERIFIED state. Two requests arrive at exactly the same time: one request tries to complete address verification and another request tries to create credentials. What could go wrong?"
"The State Machine would reject an invalid credentials transition if the session is still in ID_VERIFIED. However, because the question involves two simultaneous requests, State Machine validation alone isn't enough to reason about the race condition. We also need concurrency control at the database level so that two requests cannot incorrectly modify the same onboarding session simultaneously. In our documented flow, SELECT FOR UPDATE is used for pessimistic row locking."
6.business step ,stage, misrocservice>?
"A business step represents something the customer needs to complete, such as mobile verification or identity verification. A state represents the current condition of the onboarding workflow, such as MOBILE_VERIFIED or ID_VERIFIED. A microservice is an independently deployable software component responsible for a defined capability. The important distinction is that a business step doesn't necessarily map one-to-one to a microservice."
"Why might mobile and identity verification belong to the same service instead of two microservices?"

Say:

"A business workflow doesn't necessarily map one-to-one to microservices. We can keep multiple tightly coupled onboarding capabilities within the same service if they share the same lifecycle, ownership and transaction boundaries, and don't require independent scaling or deployment. We would consider extracting identity verification if there were strong reasons such as independent scaling, reuse across multiple workflows, separate ownership, independent deployment or failure isolation. For the exact reason why our implementation chose the current boundary, I would rely on the project's architecture decision rather than speculate."