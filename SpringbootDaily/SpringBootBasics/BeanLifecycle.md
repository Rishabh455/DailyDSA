6. How does Spring Bean lifecycle work?

Answer:

When Spring starts, it scans the classpath, identifies beans, and creates objects
inside the ApplicationContext. That's the beginning of the bean lifecycle.

Step by step what happens:

First, Spring instantiates the bean using the constructor.

Second, it performs dependency injection. If the bean has @Autowired fields or
constructor parameters, Spring resolves and injects those dependencies.

Third, Spring calls aware interfaces if implemented, like BeanNameAware or
ApplicationContextAware. This allows the bean to know about its container.

Fourth, BeanPostProcessor runs before initialization. This is important because
Spring AOP and proxies are created here.

Then initialization happens:

. If the bean has @PostConstruct, that method is called.
. If it implements InitializingBean, afterPropertiesSet() is called.
. If initMethod is defined in configuration, that method runs.

Now the bean is ready to serve requests.

When the application shuts down:

. @PreDestroy is called.
. destroy() from DisposableBean is called.
. custom destroy methods are executed.

In a real-world scenario, imagine a database connection pool bean. During
initialization, it establishes connections. During destroy, it closes them cleanly. That's
lifecycle management in action.


----------------------------------------------------------------------------------------------------
For a **3-year Spring Boot developer**, ye Bean Lifecycle ke sabse important interview questions hain:

---

# 1. What is Bean Lifecycle?

### Answer

> Bean lifecycle is the process through which a Spring bean goes from creation to destruction. It includes bean instantiation, dependency injection, initialization, usage, and destruction.

---

# 2. What is the order of Bean Lifecycle?

### Answer

```text id="gqu2s5"
Constructor
↓
Dependency Injection
↓
Aware Interfaces
↓
BeanPostProcessor (Before Init)
↓
@PostConstruct
↓
afterPropertiesSet()
↓
initMethod()
↓
Bean Ready
↓
@PreDestroy
↓
destroy()
↓
destroyMethod()
```

---

# 3. Difference between @PostConstruct and Constructor?

### Answer

```text id="m2pqnr"
Constructor:
Bean object is created.

@PostConstruct:
Called after dependency injection is completed.
```

Example:

```text id="4v7vpk"
Constructor → Dependencies may not be available

@PostConstruct → Dependencies available
```

---

# 4. Difference between @PostConstruct and afterPropertiesSet()?

### Answer

```text id="smek8r"
@PostConstruct
→ JSR standard annotation

afterPropertiesSet()
→ InitializingBean interface method
```

Both run during initialization.

---

# 5. Difference between @PreDestroy and destroy()?

### Answer

```text id="qu8m53"
@PreDestroy
→ Annotation based

destroy()
→ DisposableBean interface method
```

Both execute during bean destruction.

---

# 6. What is BeanPostProcessor?

### Answer

> BeanPostProcessor allows custom processing of beans before and after initialization. Spring AOP proxies are commonly created using BeanPostProcessor.

---

# 7. Why is BeanPostProcessor important?

### Answer

> Features like @Transactional, @Async, and Spring AOP work using BeanPostProcessor because Spring creates proxy objects around beans during this phase.

---

# 8. Can @Autowired be used inside Constructor?

### Answer

```text id="xjlwmm"
Yes.
```

Constructor injection is actually preferred.

---

# 9. Which initialization method do you prefer?

### Answer

> In modern Spring Boot applications, @PostConstruct is commonly preferred because it is simple and annotation-based.

---

# 10. Real Project Usage Question

### Interviewer:

> Have you used bean lifecycle methods in your project?

### Answer

> Yes. We used @PostConstruct to load configuration or initialize resources during application startup and @PreDestroy to release resources gracefully during application shutdown.

---

# Most Important One-Liner

```text id="u6h4nd"
Constructor creates the bean,
@PostConstruct initializes the bean,
@PreDestroy cleans up the bean.
```

Agar interviewer Bean Lifecycle puchta hai, 80% chance inhi 10 questions ke around discussion hoti hai.



