Absolutely. Based on **your actual onboarding/KYC project**, your introduction, and the fact that your stronger area is Java/Spring Boot, I would prepare Angular around **realistic 2.5–3 year interview questions**, not deep frontend architecture.

The safest positioning is:

> **“I have hands-on Angular experience mainly in API integration, onboarding screens, forms, validation, routing, error handling and frontend-backend integration. My stronger area is backend development.”**

Below is the **20-question Angular set** with the most likely follow-ups. I’ve explained the concept inside the answers so you can understand them rather than memorize them.

````md
# ANGULAR 19 — BANKING ONBOARDING / KYC PROJECT
# 20 HIGH-PROBABILITY INTERVIEW QUESTIONS

## Project Context

Frontend:
Angular 19

Backend:
Java + Spring Boot + REST APIs

Project:
Enterprise digital banking customer onboarding and KYC platform for a Saudi banking client.

Angular is mainly responsible for:
- Customer onboarding screens
- Forms and validations
- Calling Spring Boot REST APIs
- Displaying API responses
- Handling loading/error states
- Navigation between onboarding steps
- Document upload UI
- Showing verification/KYC status

My stronger area is backend, so I should answer Angular questions confidently but should not claim deep frontend architecture expertise.


====================================================
Q1. HOW IS ANGULAR USED IN YOUR CURRENT PROJECT?
====================================================

### Answer

In our banking onboarding application, Angular is used as the frontend layer.

It handles the customer-facing onboarding screens, collects customer information, performs basic frontend validation, calls the Spring Boot REST APIs, and displays the backend responses.

For example:

Angular UI
   ↓
Angular Service
   ↓
HttpClient
   ↓
Spring Boot REST API
   ↓
Business Logic
   ↓
Database / External Services

My main Angular work has been around integrating REST APIs, handling forms and validations, managing onboarding screens, handling API responses and errors, and integrating the frontend flow with the backend workflow.

### Concept

Angular is mainly responsible for presentation and frontend interaction.

The important separation is:

Frontend:
→ UI + user interaction + client-side validation

Backend:
→ business rules + security + database + final validation

### Follow-up

Q: Why shouldn't business validation be done only in Angular?

A:

Frontend validation improves user experience, but it cannot be trusted as the final validation layer because the client can be bypassed.

The backend must perform the actual business validation.


====================================================
Q2. HOW DOES ANGULAR COMMUNICATE WITH YOUR SPRING BOOT BACKEND?
====================================================

### Answer

Angular communicates with the Spring Boot backend using HTTP-based REST APIs.

Typically, the component calls an Angular service, the service uses HttpClient, and the request is sent to the Spring Boot endpoint.

Example:

Component
   ↓
OnboardingService
   ↓
HttpClient
   ↓
POST /api/onboarding/identity-verification

The backend processes the request and returns a response, usually in JSON format.

### Follow-up

Q: Why do you put the API call inside a service?

A:

To separate UI logic from API communication.

The component handles the screen and user interaction, while the service handles backend communication.

This also makes the API logic reusable and easier to test.


====================================================
Q3. WHAT IS A COMPONENT IN ANGULAR?
====================================================

### Answer

A component is the main UI building block in Angular.

It normally contains:

- TypeScript class for behavior
- HTML template for the UI
- CSS/styles
- Component metadata

For example:

```ts
@Component({
  selector: 'app-identity-verification',
  templateUrl: './identity-verification.component.html'
})
export class IdentityVerificationComponent {
}
````

In our onboarding application, different screens or sections can be represented by different components, such as mobile verification, identity verification, document upload, and KYC status.

Angular's documentation describes a component as a TypeScript class with a template and selector, together with component metadata. ([Angular][1])

### Follow-up

Q: Why split the onboarding page into multiple components?

A:

To keep the UI modular and maintainable.

For example:

Onboarding
├── Mobile Verification
├── Identity Verification
├── Document Upload
└── KYC Status

Each component has a focused responsibility.

====================================================
Q4. WHAT IS AN ANGULAR SERVICE AND WHY DID YOU USE IT?
======================================================

### Answer

An Angular service is a reusable class used to keep shared or non-UI logic outside components.

In our project, the most common use was API communication.

Example:

```ts
@Injectable({
  providedIn: 'root'
})
export class OnboardingService {

  verifyIdentity(request: any) {
    return this.http.post('/api/onboarding/verify', request);
  }
}
```

Then the component calls the service instead of directly handling HTTP communication.

This follows separation of concerns.

### Follow-up

Q: What is dependency injection?

A:

Dependency injection means Angular creates and provides required dependencies instead of the class manually creating them.

For example, the component or service can receive `HttpClient` through injection.

This reduces tight coupling and makes testing easier.

====================================================
Q5. HOW DO YOU CALL AN API IN ANGULAR?
======================================

### Answer

We use Angular's `HttpClient`.

For example:

```ts
verifyIdentity(request: any) {
  return this.http.post(
    '/api/onboarding/identity-verification',
    request
  );
}
```

The component subscribes to the returned Observable and handles success or error.

Angular's HttpClient is designed for HTTP communication and integrates with Angular's dependency injection system. ([Angular][2])

### Follow-up

Q: Why doesn't the service directly return the final response?

A:

Because HTTP calls are asynchronous.

The service returns the Observable, and the consumer decides how it should react to success, error, loading state, or transformation.

====================================================
Q6. WHAT IS AN OBSERVABLE IN ANGULAR?
=====================================

### Answer

An Observable represents an asynchronous stream of values.

Angular's HttpClient returns Observables for HTTP operations.

For example:

```ts
this.service.verifyIdentity(request)
  .subscribe({
    next: response => {
      // success
    },
    error: error => {
      // error
    }
  });
```

The main idea is that the HTTP response arrives asynchronously, and the subscription allows us to react when it arrives.

### Concept

For our project:

Customer clicks Submit
↓
HTTP request
↓
Backend processing
↓
Response arrives later
↓
Observable notifies the component

### Follow-up

Q: Is an Observable the same as a Promise?

A:

Both handle asynchronous operations, but Observables provide stream-oriented capabilities and integrate with RxJS operators.

For normal Angular HttpClient work, I mainly use Observables.

====================================================
Q7. WHAT IS THE DIFFERENCE BETWEEN PROMISE AND OBSERVABLE?
==========================================================

### Answer

A Promise generally represents one future result.

An Observable represents a stream of values over time and provides operators for transforming and combining those values.

For example:

```text
Promise
→ One future result

Observable
→ Stream of values
→ RxJS operators
→ Can unsubscribe
```

Angular HttpClient naturally works with Observables.

### Follow-up

Q: Which one did you mainly use in your project?

A:

For Angular HTTP API integration, I mainly worked with Observables because HttpClient returns them.

====================================================
Q8. HOW DID YOU HANDLE FORMS IN THE ONBOARDING APPLICATION?
===========================================================

### Answer

For structured onboarding forms, we can use Angular Reactive Forms.

For example:

```ts
this.form = this.fb.group({
  firstName: ['', Validators.required],
  dob: ['', Validators.required],
  documentNumber: ['', Validators.required]
});
```

Reactive Forms allow us to define controls and validation rules in TypeScript.

The flow becomes:

User enters data
↓
Angular validation
↓
If valid
↓
Call backend API
↓
Backend performs final validation

### Follow-up

Q: Why not rely only on HTML validation?

A:

Angular Reactive Forms give us more control over validation state, dynamic forms, error messages, and complex form logic.

====================================================
Q9. HOW DO YOU HANDLE API ERRORS IN ANGULAR?
============================================

### Answer

For individual API calls, we can handle errors through the Observable's error callback.

Example:

```ts
this.service.verifyIdentity(request)
  .subscribe({
    next: response => {
      // success
    },
    error: error => {
      // display appropriate message
    }
  });
```

For common HTTP concerns, an interceptor can handle errors centrally.

For example:

400 → validation/business error
401 → authentication issue
403 → authorization issue
500 → generic server error

We should avoid exposing internal backend details directly to the customer.

### Follow-up

Q: Why use an interceptor for some errors?

A:

If the same handling is required across many API calls, handling it centrally avoids duplicating the same code in every component.

====================================================
Q10. WHAT IS AN HTTP INTERCEPTOR?
=================================

### Answer

An HTTP interceptor acts like middleware around Angular HTTP requests and responses.

It allows us to implement common behaviour such as:

* Authentication headers
* Logging
* Common error handling
* Retry logic
* Loading indicators

Conceptually:

Angular Service
↓
HTTP Interceptor
↓
Backend

Angular supports both functional and DI-based interceptors; current Angular guidance recommends functional interceptors for more predictable behaviour. ([Angular][3])

### Follow-up

Q: Give an example from a banking application.

A:

If all API requests need a common authentication token or correlation information, an interceptor can add the appropriate header before sending the request.

====================================================
Q11. HOW WOULD YOU HANDLE A SLOW BACKEND API IN ANGULAR?
========================================================

### Answer

I would not make Angular wait silently.

I would normally:

1. Show a loading indicator.
2. Disable repeated submission where appropriate.
3. Call the backend asynchronously.
4. Handle the response when it arrives.
5. Show an appropriate message if it takes too long or fails.

Example:

```text
Submit
  ↓
isLoading = true
  ↓
API request
  ↓
Response
  ↓
isLoading = false
```

For long-running backend operations such as KYC, the frontend can receive a processing/in-progress status and show that status to the user rather than keeping the user blocked.

### Follow-up

Q: What if the user clicks Submit twice?

A:

At the frontend, I can disable the button while the request is in progress.

But this is not enough by itself.

The backend should still use idempotency because frontend protection can be bypassed.

====================================================
Q12. HOW DO YOU PREVENT DOUBLE SUBMISSION FROM ANGULAR?
=======================================================

### Answer

At the UI level, we can maintain an `isSubmitting` or loading state.

Example:

```ts
if (this.isSubmitting) {
  return;
}

this.isSubmitting = true;

this.service.verifyIdentity(request)
  .subscribe({
    next: response => {
      this.isSubmitting = false;
    },
    error: error => {
      this.isSubmitting = false;
    }
  });
```

The button can also be disabled while the request is running.

But in a banking application, frontend prevention is only a UX-level protection.

The backend must still use idempotency to prevent duplicate business processing.

### Follow-up

Q: Why isn't disabling the button enough?

A:

Because requests can be duplicated through retries, network behaviour, browser actions, or direct API calls.

Only the backend can reliably enforce business-level idempotency.

====================================================
Q13. HOW DID YOU HANDLE DOCUMENT UPLOAD IN ANGULAR?
===================================================

### Answer

For document upload, Angular collects the selected file from the user and sends it to the backend using a multipart request, usually through `FormData`.

Conceptually:

```ts
const formData = new FormData();
formData.append('document', file);

this.http.post('/api/document/upload', formData);
```

The backend receives the file, performs the required processing, and triggers the document verification flow.

### Follow-up

Q: Why use FormData?

A:

Because `FormData` is designed for sending multipart form data, including binary files.

### Follow-up

Q: Does Angular itself perform OCR?

A:

No.

Angular only handles the upload/UI flow.

The backend communicates with the document-processing service such as Azure AI Document Intelligence.

====================================================
Q14. HOW DO YOU SHOW THE OCR / KYC PROCESSING STATUS TO THE USER?
=================================================================

### Answer

Because document processing or KYC can be asynchronous, the frontend should not assume that the operation is completed immediately.

The backend can return a state such as:

```text
PROCESSING
COMPLETED
FAILED
REVIEW_REQUIRED
```

Angular displays the appropriate UI based on that status.

For example:

```text
Upload successful
      ↓
Processing...
      ↓
Verification completed
```

This keeps the frontend aligned with the actual backend workflow.

### Follow-up

Q: What if the backend says PROCESSING?

A:

The UI should show that processing is still in progress rather than treating it as failure.

Depending on the backend design, Angular may later query the status endpoint or receive the updated state through the application's supported mechanism.

====================================================
Q15. WHAT IS ANGULAR ROUTING AND HOW WOULD YOU USE IT IN ONBOARDING?
====================================================================

### Answer

Angular routing maps URLs or navigation states to components.

For example:

```text
/onboarding
/mobile-verification
/identity-verification
/document-upload
/kyc-status
```

Each route can display the appropriate component.

This allows the onboarding UI to behave like a multi-step application without reloading the entire page.

### Follow-up

Q: Why do you need routing in onboarding?

A:

Because the onboarding process contains multiple screens and steps.

Routing lets us navigate between these screens while keeping the application structured.

====================================================
Q16. WHAT IS A ROUTE GUARD? GIVE A PROJECT EXAMPLE.
===================================================

### Answer

A route guard controls whether navigation to a route is allowed.

In an onboarding application, suppose the user has not completed mobile verification.

We may prevent them from directly navigating to a later stage such as identity verification.

Conceptually:

```text
Mobile Verification
      ↓
Completed?
   /      \
 Yes       No
 ↓          ↓
Identity   Stay/redirect
Verification
```

A guard can make that decision before activating the route.

### Follow-up

Q: Is a route guard enough for security?

A:

No.

A route guard only controls frontend navigation.

The backend must still enforce authorization and business rules because the frontend can be bypassed.

====================================================
Q17. HOW DO YOU PASS DATA BETWEEN ANGULAR COMPONENTS?
=====================================================

### Answer

For parent-child communication, Angular commonly provides `@Input` for passing data from parent to child and `@Output` with an event emitter for sending events from child to parent.

Conceptually:

```text
Parent
  ↓ @Input
Child

Child
  ↓ @Output
Parent
```

For unrelated components, a shared service or another state-sharing mechanism can be used.

### Follow-up

Q: In your onboarding application, where could this be useful?

A:

For example, a parent onboarding component could maintain the current customer/session state and pass relevant information to child components such as identity verification or document upload.

====================================================
Q18. WHAT IS CHANGE DETECTION? DO YOU KNOW ONPUSH?
==================================================

### Answer

Change detection is how Angular detects changes in application state and updates the UI when necessary.

Angular has different change detection strategies.

`Default` checks more broadly.

`OnPush` reduces unnecessary checking by making Angular check the component under more specific conditions.

For example, a component using:

```ts
changeDetection: ChangeDetectionStrategy.OnPush
```

can be useful when we want more predictable and efficient UI updates.

Angular documents `OnPush` as a `CheckOnce` strategy compared with `Default`'s `CheckAlways`. ([Angular][4])

### Follow-up

Q: Did you do deep performance optimization with OnPush?

A:

My main Angular work was around API integration, forms, routing, and onboarding screens. I understand the purpose of OnPush, but my stronger expertise is backend development.

====================================================
Q19. WHAT ARE STANDALONE COMPONENTS? HAVE YOU USED THEM IN ANGULAR 19?
======================================================================

### Answer

Standalone components allow a component to manage its own template dependencies through the component's `imports` instead of requiring it to be declared in an NgModule.

In Angular 19, standalone components are the default style for new components. ([Angular][1])

Conceptually:

```ts
@Component({
  selector: 'app-identity',
  imports: [
    CommonModule
  ],
  templateUrl: './identity.component.html'
})
export class IdentityComponent {
}
```

This makes component dependencies more explicit and reduces the need for NgModule declarations.

### Follow-up

Q: Are NgModules completely invalid in Angular 19?

A:

No.

Existing applications can still use NgModules, and applications can migrate incrementally toward standalone components. Angular provides tooling for such migrations. ([Angular][5])

### Safe project answer

If your project actually used standalone components:

> "Our Angular 19 frontend follows the newer standalone approach."

If you're not sure about the exact project configuration, don't claim it.

====================================================
Q20. HOW WOULD YOU DESIGN A SIMPLE ANGULAR ONBOARDING FLOW?
===========================================================

### Answer

I would keep the frontend modular and separate UI concerns from API communication.

A simple structure could be:

```text
Onboarding
│
├── Mobile Verification Component
│
├── Identity Verification Component
│
├── Document Upload Component
│
├── KYC Status Component
│
├── Onboarding Service
│
└── Auth / HTTP Interceptor
```

The flow would be:

```text
Customer
   ↓
Angular onboarding screen
   ↓
Angular service
   ↓
REST API
   ↓
Spring Boot
   ↓
Business processing
   ↓
Response
   ↓
Angular updates UI
```

For long-running operations, the frontend should represent the backend state rather than assuming that a request finishing means the entire onboarding process is complete.

### Follow-up

Q: How would you keep the frontend maintainable?

A:

I would keep:

* Components focused on UI
* Services focused on API communication
* Models/interfaces for request and response structures
* Shared/common functionality in reusable services or components
* Validation close to the form layer
* Backend business validation on the server

The main principle is separation of concerns.

```

## The 20 questions you should actually prioritise

For **your Infosys interview**, I would learn these in this order:

| Priority | Question |
|---|---|
| ⭐⭐⭐⭐⭐ | Angular + Spring Boot integration |
| ⭐⭐⭐⭐⭐ | HttpClient / REST API calls |
| ⭐⭐⭐⭐⭐ | Angular Services |
| ⭐⭐⭐⭐⭐ | Reactive Forms |
| ⭐⭐⭐⭐⭐ | API error handling |
| ⭐⭐⭐⭐⭐ | Interceptors |
| ⭐⭐⭐⭐⭐ | Duplicate submission |
| ⭐⭐⭐⭐ | Observables |
| ⭐⭐⭐⭐ | Document upload |
| ⭐⭐⭐⭐ | Routing / Guards |
| ⭐⭐⭐⭐ | Async KYC/document status |
| ⭐⭐⭐⭐ | Component communication |
| ⭐⭐⭐⭐ | Change Detection / OnPush |
| ⭐⭐⭐ | Standalone Components |
| ⭐⭐⭐ | Promise vs Observable |

### One very important interview positioning for you

Because your **backend is your stronger area**, don't try to sound like a frontend architect.

When an interviewer asks something deep like:

> “How would you optimize a complex Angular rendering bottleneck?”

A perfectly reasonable answer is:

> **“My stronger area is backend development, but from my Angular experience I would first check component rendering, unnecessary API calls, subscriptions and change detection. For deeper frontend optimization, I would investigate the specific component and profiling data before deciding on the optimization.”**

That is far better than inventing expertise you don't have.

And your Angular introduction has a very strong safe positioning:

> **“I have hands-on experience integrating Angular with Spring Boot REST APIs, working with onboarding forms, validation, routing, document upload and handling backend responses. My stronger expertise is on the Java/Spring Boot backend side.”**

That statement makes your **Angular answers believable without inviting unnecessarily difficult frontend questions**.
```

[1]: https://angular.dev/guide/components?utm_source=chatgpt.com "Anatomy of components • Angular"
[2]: https://angular.dev/guide/http/setup?utm_source=chatgpt.com "Setting up HttpClient • Angular"
[3]: https://angular.dev/guide/http/interceptors?utm_source=chatgpt.com "Intercepting requests and responses • Angular"
[4]: https://v19.angular.dev/api/core/Component?utm_source=chatgpt.com "Component • Angular"
[5]: https://angular.dev/reference/migrations/standalone?utm_source=chatgpt.com "Standalone • Angular"
