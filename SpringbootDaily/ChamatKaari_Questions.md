### Q: Does Global Exception Handling (`@ControllerAdvice`) use Spring Proxy/AOP internally?

**Answer:**

**No.** `@ControllerAdvice` is **not proxy-based**.

* `@Transactional`, `@Async`, `@Cacheable`, `@Retryable` use **Spring AOP Proxies** because they need to **intercept method calls** before/after execution.
* `@ControllerAdvice` works through Spring MVC's **DispatcherServlet → HandlerExceptionResolver → @ExceptionHandler** mechanism.
* When a controller throws an exception, `DispatcherServlet` catches it and delegates it to `HandlerExceptionResolver`, which finds the appropriate `@ExceptionHandler` method.

```text
@Transactional
Client → Proxy → Target Method

@ControllerAdvice
Controller → Exception → DispatcherServlet
           → HandlerExceptionResolver
           → @ExceptionHandler
```

**Interview One-Liner:**

> "`@Transactional` uses proxies because it must intercept method execution, whereas `@ControllerAdvice` does not use proxies; it is handled by Spring MVC's `HandlerExceptionResolver` after an exception is thrown."
