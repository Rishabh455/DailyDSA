# Global Exception Handling in Spring Boot (Interview Notes - 3 YOE)

## Direct Interview Answer (30 Seconds)

> Global Exception Handling in Spring Boot is implemented using `@ControllerAdvice` and `@ExceptionHandler`. Instead of writing try-catch blocks in every controller, exceptions are handled centrally in one place. When an exception occurs in the controller, service, or repository layer, Spring forwards it to the appropriate `@ExceptionHandler` method inside the `@ControllerAdvice` class, which returns a standardized error response to the client.

---

# Flow

```text
Client Request
      ↓
Controller
      ↓
Service / Repository
      ↓
Exception Thrown
      ↓
@ControllerAdvice
      ↓
@ExceptionHandler
      ↓
Custom Error Response
      ↓
Client
```

---

# Example

### Custom Exception

```java
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
```

---

### Global Exception Handler

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

### Controller

```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {

    throw new UserNotFoundException("User not found");
}
```

---

### Response

```json
{
  "message": "User not found"
}
```

Status:

```http
404 NOT FOUND
```

---

# Why Use Global Exception Handling?

### Without Global Exception Handling

```java
try {
   // business logic
} catch(Exception e) {
   // handle exception
}
```

Repeated in every controller ❌

---

### With Global Exception Handling

```java
@ControllerAdvice
```

One centralized place for handling all exceptions ✅

---

# Advantages

### 1. Centralized Error Handling

All exception handling logic is in one place.

---

### 2. Cleaner Controllers

No repetitive try-catch blocks.

---

### 3. Consistent API Responses

Every API returns the same error format.

```json
{
   "timestamp":"2026-06-05T10:30:00",
   "status":404,
   "message":"User not found",
   "path":"/users/1"
}
```

---

### 4. Easier Maintenance

Update error handling logic in one class.

---

### 5. Better API Design

Clients receive predictable and meaningful error responses.

---

# Frequently Asked Interview Questions

### What does @ControllerAdvice do?

> `@ControllerAdvice` is used to handle exceptions globally across all controllers.

---

### What does @ExceptionHandler do?

> `@ExceptionHandler` handles specific exception types and defines the response returned to the client.

---

### Difference Between @ControllerAdvice and @ExceptionHandler?

> `@ControllerAdvice` provides global exception handling, while `@ExceptionHandler` handles a specific exception method inside that advice class.

---

# 10-Second Interview Answer

> `@ControllerAdvice` provides centralized exception handling for the entire application, and `@ExceptionHandler` handles specific exceptions. This avoids duplicate try-catch blocks, keeps controllers clean, and provides consistent error responses to API clients.

---

# Memory Trick

```text
Request
   ↓
Controller
   ↓
Exception
   ↓
@ControllerAdvice
   ↓
@ExceptionHandler
   ↓
Custom Response
```

### One-Line Formula

> **Exception Occurs → ControllerAdvice Catches → ExceptionHandler Handles → Standard Response Returned** 🚀
