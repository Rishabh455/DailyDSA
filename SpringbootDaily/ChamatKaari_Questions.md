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
### Q: Spring problems which was solved in springboot?

So, in Spring, we faced a few challenges. First, when we created beans, we had to manually register them using XML. Second, we had to explicitly search for beans we needed. Lastly, we had to configure everything manually—the dispatcher servlet, view resolvers, object mapper, transaction manager, even the Tomcat server. This infrastructure setup was quite heavy compared to business logic.

Now, Spring Boot solved these issues. With @Configuration, we just annotate and beans are registered automatically—no XML needed. With @ComponentScan, beans like services or repositories are automatically detected, again without XML. Finally, @EnableAutoConfiguration does the magic—based on dependencies like Spring Boot Web, it automatically configures things like the dispatcher servlet. If something is missing, it will set it up. In short, I no longer need to handle infrastructure manually; Spring Boot does it for me.