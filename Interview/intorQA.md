//explain the porject
Currently, I’m working on a digital customer onboarding and KYC workflow for a Saudi banking client.

The main purpose of the application is to digitally onboard a new customer. The workflow starts with the onboarding service, followed by mobile number verification and identity verification.

During identity verification, the customer uploads the required identity documents. These documents are processed using OCR, and in our application we have integrated Azure AI Document Intelligence for document processing and information extraction. The extracted information is then validated as part of the identity verification process.

After the required verification is completed, the customer goes through the KYC checks. Once the required checks are successfully completed, the customer identity is provisioned through our Identity and Access Management service.

My main contribution in this project has been developing and integrating REST APIs using Java and Spring Boot, implementing business validations, integrating the APIs with the Angular frontend, and working on the integration with Azure AI Document Intelligence.

question 2
// how thge serviuce commmunicate with each other
At a high level, our onboarding service acts as the central orchestrator for the onboarding workflow.

Its responsibility is mainly to manage the workflow state and decide which step should be executed next, while the actual business logic is handled by the respective services.

For example, the onboarding service initiates the mobile verification by calling the Mobile Verification service through a synchronous REST API. Once that step is successfully completed, the workflow moves to identity verification.

In the identity verification stage, the customer uploads the required documents. The document-processing flow integrates with Azure AI Document Intelligence for OCR and information extraction.

After identity verification, the request moves to the KYC stage. Since KYC can take several minutes, we handle this as an asynchronous process. The customer receives an acknowledgement that the onboarding process is in progress, rather than waiting for the complete KYC operation.

Once KYC is successfully completed, the workflow moves to the IAM service, which handles customer identity provisioning and authentication-related information through Active Directory.

So overall, the onboarding service controls the workflow, while each individual service is responsible for its own business functionality.



//q2 follow up 
"Why did you use an orchestrator? Why didn't you simply let these services communicate directly with each other?"
We used an orchestrator mainly to separate workflow management from business logic.

The onboarding workflow involves multiple steps such as mobile verification, identity verification, KYC, and IAM. The onboarding service acts as the orchestrator and is responsible for managing the workflow state, deciding which step should execute next, and coordinating with the respective services.

The actual business logic remains within the individual services. For example, mobile verification logic stays within the Mobile Verification service, and KYC-related logic stays within the KYC service.

This separation makes the workflow easier to understand and maintain. If every service was responsible for both its own business logic and the overall workflow transitions, the services could become tightly coupled and the workflow logic would become difficult to manage.
 q2 fullow up
So, in our case, the orchestrator gives us a clear separation between **workflow coordination and business logic**.

We maintain the workflow state against a unique onboarding session ID.

For each onboarding session, we maintain information such as the current workflow state, the completed steps, and the status of the overall onboarding process. So whenever a request comes to the onboarding service, we can identify the session and determine which step needs to be executed next.

For duplicate requests, we handle them separately using idempotency and concurrency controls. The request is associated with an idempotency key or a unique business identifier, so if the same request is received again, we can identify that it has already been processed and avoid executing the same business operation again.

For concurrent requests for the same onboarding session, we also ensure that the session state is updated safely so that two requests don't move the same workflow forward simultaneously.

q2 
If two requests for the same onboarding session arrive at the same time, we need to make sure that both requests don't update the same workflow state incorrectly.

One approach is optimistic locking. We maintain a version field for the onboarding session. When a request reads the current state, it also reads the current version.

For example, suppose the current version is 5. Both Request A and Request B read version 5. Request A successfully updates the state, and the version becomes 6.

When Request B tries to update using version 5, the update condition no longer matches, so the database update affects zero rows. This indicates that another request has already modified the record.

We can then handle this as a concurrency conflict by re-reading the latest state and deciding whether the request should be retried or rejected.

So optimistic locking helps us prevent lost updates when multiple requests try to modify the same onboarding session concurrently.



# INTRODUCTION — LAST-MINUTE INTERVIEW PREPARATION

## Final Self-Introduction

Good morning. My name is Rishabh Chaurasia, and I’m a Java Full Stack Developer with over three years of experience in developing enterprise applications.

Currently, I’m working with TCS on an enterprise banking application for a Saudi banking client. I’m mainly involved in the customer onboarding and KYC workflow, along with the Identity and Access Management service.

In this project, I have worked on different parts of the onboarding process, such as mobile verification, customer identity verification, and document verification. For document verification, we have integrated Azure AI Document Intelligence for OCR and document processing. I have also worked on the Identity and Access Management service, where Microsoft Active Directory is used for managing customer identity and authentication-related information.

From a technical perspective, my main experience is in developing REST APIs using Java and Spring Boot, implementing business logic and validations, and integrating these APIs with the Angular frontend. I have also worked with databases and have experience in debugging, troubleshooting, and improving application performance.

I also use AI-assisted development tools to improve my productivity and efficiency in day-to-day development tasks.

Overall, my core experience is in Java, Spring Boot, REST APIs, Angular, and database development, along with experience working on enterprise banking applications.


# INTRODUCTION FOLLOW-UP QUESTIONS

## Q1. Can you explain your current project?

The project is an enterprise digital banking application focused on customer onboarding and KYC.

A customer goes through multiple verification steps such as mobile verification, identity and document verification, KYC checks, and finally identity provisioning through the IAM service.

My main contribution is in backend development using Java and Spring Boot, REST API development, business validations, Angular integration, database work, and integrations such as Azure Document Intelligence.


## Q2. Can you explain the customer onboarding workflow?

At a high level:

Customer
↓
Onboarding
↓
Mobile Verification
↓
Identity Verification
↓
Document Processing / OCR
↓
Identity Validation
↓
KYC
↓
IAM / Customer Provisioning
↓
Activation

The onboarding service manages the workflow, while individual services handle their respective business logic.


## Q3. What exactly was your role in the project?

My main responsibilities were:

- Developing REST APIs using Java and Spring Boot
- Implementing business logic and validations
- Integrating backend APIs with Angular
- Working with databases
- Debugging and troubleshooting issues
- Working on the document verification flow and Azure Document Intelligence integration
- Supporting onboarding and IAM-related functionality


## Q4. What technologies are you working with?

My primary technologies are:

Java, Spring Boot, REST APIs, Angular, SQL/database technologies, and Git.

I have also worked with Azure AI Document Intelligence, Microsoft Active Directory, Kafka, and AI-assisted development tools as part of the project.


## Q5. How did you integrate Angular with your backend?

The Angular frontend consumes the REST APIs exposed by the Spring Boot backend.

The backend handles business logic, validation, and database operations, while Angular is responsible for the UI and user interaction.

The communication happens mainly through HTTP-based REST APIs using JSON request and response payloads.


## Q6. What kind of REST APIs did you develop?

I worked on APIs related to the onboarding and verification workflow.

They were responsible for operations such as initiating verification, submitting customer information, processing document-related requests, updating verification status, and moving the onboarding workflow to the next stage.

I also handled validations and appropriate error responses.


## Q7. What is your experience with Spring Boot?

I have used Spring Boot primarily for developing REST APIs and backend services.

I have worked with controllers, services, repositories, dependency injection, validation, exception handling, database integration, and service-to-service integrations.


## Q8. What exactly did you do with Azure AI Document Intelligence?

We used Azure AI Document Intelligence for document analysis and OCR as part of the identity verification flow.

The customer uploads an identity document, our backend sends it for document analysis, Azure extracts relevant information, and our application processes and validates that information before allowing the workflow to proceed.


## Q9. Does Azure OCR determine whether a document is genuine or fake?

No.

Azure Document Intelligence primarily performs document analysis and information extraction.

The confidence score indicates how confident the model is about an extraction; it is not an authenticity score.

The actual identity/document verification decision comes from application-level validation and the relevant identity/KYC verification process.


## Q10. What does the Azure confidence score mean?

It indicates the model's confidence in the extracted field or prediction.

For example, a high confidence score means the model is relatively confident that it correctly extracted a value.

It does not mean that the document is genuine.


## Q11. What happens if OCR confidence is low?

If a critical extracted field is below our configured application threshold, we don't automatically accept the extraction.

Depending on the business rule, we can ask the customer to re-upload the document or move the case to REVIEW_REQUIRED for further verification.


## Q12. Why do you need a normalization layer after OCR?

Because external services can represent data differently from our internal application.

We normalize the external response into a canonical format before sending it to our business validation layer.

This keeps external-format handling separate from business rules.


## Q13. What is the role of the IAM service?

The IAM service is responsible for identity and authentication-related operations.

In our application, Microsoft Active Directory is used for managing customer identity and authentication-related information.


## Q14. What is Microsoft Active Directory doing in your project?

Active Directory is used as an identity store for customer identity and authentication-related information.

The IAM service interacts with it for identity-related operations rather than our onboarding business logic directly managing authentication data.


## Q15. What is the difference between authentication and authorization?

Authentication answers:

"Who are you?"

Authorization answers:

"What are you allowed to access or perform?"


## Q16. Why are you calling yourself a Full Stack Developer if most of your experience is backend?

My core strength is Java and Spring Boot backend development, but I have also worked with Angular for integrating REST APIs and supporting frontend functionality.

So I have experience across both backend and frontend, with stronger expertise on the backend side.


## Q17. What kind of database work have you done?

I have worked with relational databases for storing customer, onboarding, verification, and workflow-related information.

My work includes writing queries, integrating the database through the application layer, handling transactional operations, troubleshooting query issues, and performance optimization.


## Q18. What kind of performance improvements have you worked on?

I have worked on identifying slow database operations and unnecessary database calls.

Depending on the issue, I have used query optimization, appropriate indexing, and reducing unnecessary database access to improve application performance.


## Q19. Why do you use AI-assisted development tools?

I use AI-assisted development tools to improve productivity during day-to-day development tasks, such as understanding code, generating boilerplate, exploring solutions, and debugging.

I still review and validate the generated output before using it in the application.


## Q20. Which AI tool do you use?

I use GitHub Copilot for AI-assisted development.

I have also completed GitHub Copilot certifications.


# PROJECT ARCHITECTURE FOLLOW-UPS

## Q21. You mentioned that onboarding is an orchestrator. What does that mean?

The onboarding service acts as the central workflow orchestrator.

It manages the onboarding state and decides which step should execute next.

The actual business logic remains inside the respective services.

Key line:

"The orchestrator owns the workflow; individual services own their business logic."


## Q22. Why did you use an orchestrator?

We wanted to separate workflow coordination from business logic.

The onboarding workflow contains multiple steps, so keeping workflow transitions in one place makes the flow easier to understand and maintain while each service remains responsible for its own business logic.


## Q23. How do the services communicate?

For short-running operations, we use synchronous REST communication.

For long-running or asynchronous processing such as document processing or KYC-related flows, asynchronous communication can be used so that the customer request does not remain blocked.


## Q24. Why use asynchronous processing?

Because some operations can take several seconds or minutes.

Instead of keeping the customer request open, we acknowledge the request and process the operation asynchronously while maintaining the workflow state.


## Q25. Where does Kafka fit into the project?

Kafka is used for asynchronous communication where decoupling is useful.

For example, a document-processing event can be published to Kafka, and a downstream consumer can process the document and interact with Azure Document Intelligence asynchronously.


# CONTRIBUTION / OWNERSHIP QUESTIONS

## Q26. What did YOU personally implement?

My main contribution was in Java/Spring Boot backend development.

I worked on REST APIs, business logic, validation, database interaction, Angular integration, troubleshooting, and integrations involved in the onboarding workflow.


## Q27. Did you design the entire architecture?

I contributed to the implementation and understanding of the onboarding architecture and worked on specific services and workflow components.

I would not claim ownership of the entire enterprise architecture.


## Q28. Which module are you most comfortable explaining in detail?

I am most comfortable explaining the customer onboarding and identity verification flow because that is where I worked extensively with REST APIs, validations, document processing, and integrations.


# VERY IMPORTANT "SAFE ANSWERS"

## Q29. Did you work on everything you mentioned?

I have worked on specific modules and integrations within the larger platform.

My strongest hands-on experience is Java, Spring Boot, REST APIs, Angular integration, databases, onboarding workflow, and the integrations directly related to those modules.


## Q30. Are you an Azure expert?

I have hands-on experience integrating Azure AI Document Intelligence as part of our document verification workflow.

I would describe my Azure experience as project-specific rather than claiming expertise across the entire Azure platform.


## Q31. Are you an expert in Kafka?

I have worked with Kafka as part of the asynchronous workflow in the project.

My strongest expertise remains Java and Spring Boot, while Kafka is an important part of the distributed workflow I have worked with.


## Q32. Are you an expert in AI/LLMs?

My core expertise is Java and Spring Boot development.

I use AI-assisted development tools such as GitHub Copilot to improve productivity, but I would consider AI a supporting skill rather than my primary development specialization.


# 30-SECOND PROJECT SUMMARY

If the interviewer says:

"Give me a quick overview."

Answer:

I work on a digital banking customer onboarding and KYC platform for a Saudi banking client. The workflow includes mobile verification, identity and document verification, KYC, and IAM-based customer provisioning.

My primary contribution is Java and Spring Boot backend development, REST APIs, business validations, Angular integration, database work, and integrations such as Azure AI Document Intelligence.


# 3 MOST IMPORTANT LINES TO REMEMBER

1. "My core strength is Java and Spring Boot backend development, with hands-on experience in Angular integration."

2. "The onboarding service manages workflow coordination, while individual services own their business logic."

3. "Azure Document Intelligence is used for document analysis and extraction; successful OCR does not by itself mean that the document is authentic."