बहुत बढ़िया। Interview में अगर तुम यह story बता दो तो interviewer को लगेगा कि तुमने production में काम किया है।

# Scenario: GET /users/101

मान लो database में User 101 मौजूद नहीं है।

---

# Case 1: WITHOUT Global Exception Handling

## Controller

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {

        try {
            return userService.getUser(id);

        } catch (UserNotFoundException ex) {

            return null; // or custom response
        }
    }
}
```

---

## Service

```java
@Service
public class UserService {

    public User getUser(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return user;
    }
}
```

---

## Flow

```text
Client
  ↓
GET /users/101
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
User Not Found
  ↓
Exception Thrown
  ↓
Back to Controller
  ↓
Catch Block
  ↓
Return Response
```

---

## Problem

अब सोचो:

```java
UserController
ProductController
OrderController
PaymentController
```

सभी में:

```java
try {
}
catch(Exception e){
}
```

लिखना पड़ेगा।

100 endpoints हैं तो 100 जगह error handling।

❌ Code Duplication

❌ Hard to Maintain

❌ Different Error Responses

---

# Case 2: WITH Global Exception Handling

## Controller

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {

        return userService.getUser(id);
    }
}
```

देखो...

कोई try-catch नहीं।

Controller बिल्कुल clean है।

---

## Service

```java
@Service
public class UserService {

    public User getUser(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }
}
```

Exception throw कर दिया।

बस।

---

## Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(
            UserNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
}
```

---

## Actual Runtime Flow

```text
Client
  ↓
GET /users/101
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
User Not Found
  ↓
UserNotFoundException
  ↓
Spring DispatcherServlet
  ↓
@ControllerAdvice
  ↓
@ExceptionHandler
  ↓
Custom Response
  ↓
Client
```

---

## What Spring Does Internally

जब exception ऊपर तक पहुंच जाती है:

```java
UserNotFoundException
```

Spring DispatcherServlet सोचता है:

> "क्या कोई @ExceptionHandler है जो इस exception को handle कर सकता है?"

फिर वो:

```java
GlobalExceptionHandler
```

को खोजता है।

और execute करता है:

```java
handleUserNotFound()
```

---

## Response Returned

```json
{
   "message":"User not found"
}
```

```http
404 NOT FOUND
```

---

# Production Example

मान लो application में:

```text
50 Controllers
300 Endpoints
```

Without Global Handling:

```text
300 try-catch blocks
```

With Global Handling:

```text
1 GlobalExceptionHandler
```

बस।

---

# Interview Answer

> Earlier we used try-catch blocks inside every controller method. This caused code duplication and inconsistent error responses. With `@RestControllerAdvice`, exceptions thrown from the controller, service, or repository layer are intercepted centrally. The corresponding `@ExceptionHandler` method generates a standardized response, resulting in cleaner controllers, better maintainability, and consistent API error handling.

यही answer 3 YOE Spring Boot developer के लिए perfect है, क्योंकि इसमें **flow + internal working + practical benefit** तीनों cover हो जाते हैं। 🚀
