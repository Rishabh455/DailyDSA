# Spring Boot Internals Roadmap (3 YOE Java Developer)

---

## Phase 1: Core Spring Foundation

### 1. What Problem Does Spring Solve?
- Tight Coupling vs Loose Coupling
- Boilerplate code problem (JDBC, EJB era)
- Dependency Management headache
- Why IoC was introduced
- Spring as a lightweight alternative to EJB

### 2. IoC (Inversion of Control)
- What is IoC?
- Why do we need it?
- Traditional Object Creation vs Spring Managed Objects
- IoC Container concept
- IoC is a Principle, DI is its Implementation (important distinction)

### 3. BeanFactory vs ApplicationContext
- What is a Container?
- BeanFactory — lazy, basic DI
- ApplicationContext — eager, feature-rich (events, i18n, AOP)
- Which one does Spring Boot use? → AnnotationConfigServletWebServerApplicationContext
- Types of ApplicationContext:
  - ClassPathXmlApplicationContext
  - AnnotationConfigApplicationContext
  - WebApplicationContext

### 4. Spring Bean
- What is a Bean?
- Java Bean vs Spring Bean (don't confuse)
- How Spring registers a Bean (BeanDefinition object)
- BeanDefinitionRegistry
- Bean Lifecycle (detailed in Phase 4)

### 5. Dependency Injection
- Constructor Injection
- Setter Injection
- Field Injection
- Why Constructor Injection is Preferred:
  - Immutability
  - Testability (no Spring context needed)
  - Fails fast (NullPointerException at startup, not runtime)
- Circular Dependency problem and how Constructor Injection exposes it early

### 6. Autowiring
- @Autowired
- @Qualifier
- @Primary
- @Inject vs @Autowired (JSR-330)
- How Spring resolves dependencies internally:
  - First by Type
  - If multiple → by Name
  - If still ambiguous → @Qualifier
- What happens when no bean found → NoSuchBeanDefinitionException
- What happens when multiple beans found → NoUniqueBeanDefinitionException

---

## Phase 2: Spring Boot Startup Internals

### 7. What Happens When Spring Boot Starts?

```text
main() called
      ↓
SpringApplication.run()
      ↓
SpringApplication instance created
      ↓
Determine Application Type (SERVLET / REACTIVE / NONE)
      ↓
Load SpringFactories (spring.factories / AutoConfiguration.imports)
      ↓
Create & Prepare Environment (load properties, profiles)
      ↓
Print Banner
      ↓
Create ApplicationContext (based on app type)
      ↓
prepareContext() — register BeanDefinitions
      ↓
refreshContext() — THE MOST IMPORTANT STEP
      |
      ├── Component Scan → discover beans
      ├── Auto Configuration → conditional bean creation
      ├── Bean Instantiation
      ├── Dependency Injection
      ├── BeanPostProcessors run
      ├── @PostConstruct called
      └── Embedded Tomcat started
      ↓
ApplicationStartedEvent fired
      ↓
CommandLineRunner / ApplicationRunner executed
      ↓
ApplicationReadyEvent fired
```

> **Key Insight:** `refreshContext()` is where 90% of the magic happens. Everything from bean creation to Tomcat start lives inside it.

### 8. @SpringBootApplication Internals

```java
@SpringBootConfiguration    // = @Configuration, marks this as config class
@EnableAutoConfiguration    // triggers auto-config via imports file
@ComponentScan              // scans current package + sub-packages
```

- `@SpringBootConfiguration` vs `@Configuration` — difference?
- Why base package matters for `@ComponentScan`
- Excluding auto-config: `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)`

### 9. Component Scanning
- How Spring finds beans via classpath scanning
- `@Component`, `@Service`, `@Repository`, `@Controller` — all are `@Component` specializations
- `@Repository` adds exception translation (PersistenceExceptionTranslationPostProcessor)
- `@Service` — semantic only, no extra behavior
- Custom stereotype annotations
- `includeFilters` / `excludeFilters` in `@ComponentScan`

---

## Phase 3: Auto Configuration Deep Dive

### 10. How @EnableAutoConfiguration Works

```text
@EnableAutoConfiguration
        ↓
Reads META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
(Spring Boot 2.7+; earlier it was spring.factories)
        ↓
Loads ~150+ AutoConfiguration classes
        ↓
Each class has @Conditional checks
        ↓
Only matching beans get created
```

- How to see what's being auto-configured: `--debug` flag or `ConditionEvaluationReport`
- How to write your own AutoConfiguration class

### 11. Conditional Annotations (Heart of Auto Config)
- `@ConditionalOnClass` — bean created only if class is on classpath
- `@ConditionalOnMissingBean` — only if no existing bean of that type
- `@ConditionalOnProperty` — based on property value
- `@ConditionalOnBean` — only if another specific bean exists
- `@ConditionalOnWebApplication` — only in web context
- `@ConditionalOnExpression` — SpEL-based condition
- Execution order matters — `@AutoConfigureBefore` / `@AutoConfigureAfter`

### 12. Spring Boot Starters
- What a starter actually is (just a pom aggregator + auto-config)
- `spring-boot-starter-web` → brings Tomcat + Spring MVC + Jackson
- `spring-boot-starter-data-jpa` → brings Hibernate + DataSource auto-config
- How adding a dependency on classpath triggers `@ConditionalOnClass` → creates beans automatically
- Custom starter creation (asked in senior interviews)

---

## Phase 4: Bean Lifecycle & Internals

### 13. Bean Lifecycle — Complete Flow

```text
1. BeanDefinition loaded (via scan or @Bean)
        ↓
2. BeanFactoryPostProcessor runs
   (can modify bean definitions before creation)
   Example: PropertySourcesPlaceholderConfigurer
        ↓
3. Bean Instantiation (via constructor / factory method)
        ↓
4. Populate Properties (DI happens here)
        ↓
5. BeanPostProcessor — postProcessBeforeInitialization()
   (e.g., @Autowired processing by AutowiredAnnotationBeanPostProcessor)
        ↓
6. @PostConstruct / afterPropertiesSet() / custom init-method
        ↓
7. BeanPostProcessor — postProcessAfterInitialization()
   (proxy creation happens HERE — AOP, @Transactional)
        ↓
8. Bean is READY — stored in singleton cache
        ↓
9. @PreDestroy / destroy() / custom destroy-method (on shutdown)
```

> **Key Insight:** Proxy creation happens in step 7 (postProcessAfterInitialization), not during instantiation. This is why self-invocation on `@Transactional` methods doesn't work.

### 14. BeanPostProcessor vs BeanFactoryPostProcessor
- `BeanFactoryPostProcessor` — operates on **bean definitions** (before creation)
- `BeanPostProcessor` — operates on **bean instances** (after creation)
- Most important built-in BPPs:
  - `AutowiredAnnotationBeanPostProcessor` → handles `@Autowired`
  - `CommonAnnotationBeanPostProcessor` → handles `@PostConstruct`, `@PreDestroy`
  - `AbstractAutoProxyCreator` → creates AOP proxies

### 15. Bean Scopes
- **Singleton** — one instance per ApplicationContext (default)
- **Prototype** — new instance every time requested
- **Request** — one per HTTP request (web only)
- **Session** — one per HTTP session (web only)
- **Application** — one per ServletContext
- **WebSocket** — one per WebSocket session
- Gotcha: injecting prototype bean into singleton — use `@Lookup` or `ObjectProvider`

---

## Phase 5: Reflection & Proxies

### 16. Java Reflection (Foundation)
- `Class<?>`, `Method`, `Field`, `Constructor`, `Annotation`
- `getDeclaredFields()` vs `getFields()`
- `setAccessible(true)` — how Spring bypasses private modifiers
- Performance cost of reflection — why Spring caches metadata

### 17. Where Spring Uses Reflection
- Component Scanning — reads class metadata via ASM (not full reflection, for performance)
- `@Autowired` processing — finds fields/constructors to inject
- Bean creation — invokes constructors
- Annotation processing — reads `@Transactional`, `@Cacheable` etc.
- `@Value` injection — sets field values via reflection

### 18. Proxy Objects — Most Important Concept

```text
Original Bean created
      ↓
BeanPostProcessor checks for AOP advice / @Transactional etc.
      ↓
If advice exists → Proxy Bean created wrapping Original Bean
      ↓
Spring container returns Proxy (not original) to callers
      ↓
Method call on Proxy → advice runs → delegates to Original
```

**Two proxy mechanisms:**
- **JDK Dynamic Proxy** — works via interface, creates `$Proxy` class
- **CGLIB Proxy** — subclasses the target class, used when no interface
- Spring Boot default: CGLIB for `@Configuration`, JDK for others
- Spring Boot 2.x+ default for `@Transactional`: CGLIB

**Where proxies are used:**
- `@Transactional` — wraps method in transaction
- `@Cacheable` / `@CacheEvict` — wraps with cache logic
- `@Async` — submits method to thread pool
- `@Secured` / `@PreAuthorize` — checks security before call
- AOP `@Around` advice

**Classic Proxy Gotcha:**
```java
// BROKEN — calling transactional method from within same class
// 'this' refers to original bean, not proxy → transaction ignored
public void methodA() {
    this.methodB(); // @Transactional on methodB does NOTHING here
}

@Transactional
public void methodB() { ... }
```

---

## Phase 6: Spring AOP

### 19. AOP Core Concepts
- **Aspect** — the class containing cross-cutting logic
- **Advice** — what to do (Before, After, Around, AfterReturning, AfterThrowing)
- **Join Point** — where advice can be applied (method execution in Spring)
- **Pointcut** — expression that selects join points
- **Weaving** — linking aspects to target objects (Spring does runtime weaving via proxies)

### 20. AOP Proxy Types
- Spring AOP = runtime proxy-based (not compile-time like AspectJ)
- AspectJ = compile-time or load-time weaving (more powerful, no proxy limitation)
- Spring can integrate with AspectJ via `@EnableAspectJAutoProxy`

### 21. Common AOP-powered annotations
- `@Transactional` — transaction management
- `@Cacheable` / `@CacheEvict` / `@CachePut` — caching
- `@Async` — async execution
- `@Retry` (Spring Retry) — retry on failure
- `@Scheduled` — scheduled execution
- `@PreAuthorize` / `@Secured` — method security

---

## Phase 7: Spring MVC Internals

### 22. Request Lifecycle — Complete Flow

```text
HTTP Request
      ↓
Servlet Container (Tomcat)
      ↓
DispatcherServlet (Front Controller)
      ↓
HandlerMapping → finds which Controller method handles this URL
      ↓
HandlerAdapter → invokes the handler (resolves params, bindings)
      ↓
Controller method executes
      ↓
MessageConverter (for @ResponseBody — object → JSON via Jackson)
      ↓
Response written back
      ↓
HandlerInterceptor.afterCompletion()
```

### 23. DispatcherServlet Internals
- It's a Servlet registered in embedded Tomcat at startup
- `HandlerMapping` implementations:
  - `RequestMappingHandlerMapping` — processes `@RequestMapping`
  - `RouterFunctionMapping` — for functional endpoints
- `HandlerAdapter` implementations:
  - `RequestMappingHandlerAdapter` — main one for `@Controller`
- `ViewResolver` — for traditional MVC (not used in REST APIs)
- `HttpMessageConverter` — serialize/deserialize request/response body
  - `MappingJackson2HttpMessageConverter` — Java ↔ JSON

### 24. @RestController vs @Controller
- `@RestController` = `@Controller` + `@ResponseBody` on every method
- `@ResponseBody` tells Spring to use `HttpMessageConverter` instead of ViewResolver

### 25. Filter vs Interceptor vs AOP
```text
Filter (Servlet level)
  → runs before DispatcherServlet
  → used for: auth headers, CORS, logging, rate limiting

Interceptor (Spring MVC level)
  → runs inside DispatcherServlet, before Controller
  → has access to Handler info
  → used for: auth checks, timing

AOP (Bean level)
  → runs inside the bean method
  → used for: transactions, caching, logging
```

---

## Phase 8: Exception Handling

### 26. Exception Handling Mechanisms (in order of priority)
1. `@ExceptionHandler` inside `@Controller` — local to that controller
2. `@ControllerAdvice` + `@ExceptionHandler` — global
3. `ResponseEntityExceptionHandler` — base class for Spring MVC exceptions
4. `HandlerExceptionResolver` — low-level

Internal flow:
```text
Exception thrown in Controller/Service
      ↓
DispatcherServlet catches it
      ↓
Checks for @ExceptionHandler in same controller
      ↓
If not found → checks registered @ControllerAdvice classes
      ↓
Matching handler executes → builds ResponseEntity
      ↓
HttpMessageConverter writes response
```

### 27. Problem Details (RFC 7807) — Spring Boot 3.x
- `ProblemDetail` class for structured error responses
- `ErrorResponse` interface
- Auto-enabled via `spring.mvc.problemdetails.enabled=true`

---

## Phase 9: Transaction Management

### 28. @Transactional Internals — Most Asked

```text
Call reaches Proxy (not original bean)
      ↓
TransactionInterceptor.invoke()
      ↓
PlatformTransactionManager.getTransaction() → begins transaction
      ↓
Original bean method executes
      ↓
Success → commit()
Exception → rollback() (only for unchecked by default)
      ↓
Connection returned to pool
```

### 29. Propagation Behaviors (must know all 7)
- `REQUIRED` — join existing or create new (default)
- `REQUIRES_NEW` — always new, suspends existing
- `SUPPORTS` — join if exists, else non-transactional
- `NOT_SUPPORTED` — always non-transactional, suspends existing
- `MANDATORY` — must have existing, else exception
- `NEVER` — must NOT have existing, else exception
- `NESTED` — nested within existing (savepoint-based)

### 30. Isolation Levels
- `READ_UNCOMMITTED` — dirty reads possible
- `READ_COMMITTED` — prevents dirty reads (PostgreSQL default)
- `REPEATABLE_READ` — prevents non-repeatable reads
- `SERIALIZABLE` — full isolation, slowest

### 31. Common @Transactional Gotchas
- Self-invocation → proxy bypassed → transaction ignored
- `private` methods → proxy can't intercept → ignored
- Checked exceptions don't rollback by default → use `rollbackFor = Exception.class`
- `readOnly = true` → performance optimization hint to DB + Hibernate

---

## Phase 10: Spring Security Internals

### 32. Security Filter Chain

```text
HTTP Request
      ↓
DelegatingFilterProxy (bridges Servlet world to Spring)
      ↓
FilterChainProxy
      ↓
SecurityFilterChain (ordered list of filters)
      |
      ├── SecurityContextPersistenceFilter
      ├── UsernamePasswordAuthenticationFilter
      ├── BasicAuthenticationFilter
      ├── BearerTokenAuthenticationFilter (JWT)
      ├── ExceptionTranslationFilter
      └── FilterSecurityInterceptor (authorization)
      ↓
Controller
```

### 33. Authentication Flow
```text
Request with credentials
      ↓
AuthenticationFilter extracts credentials
      ↓
Creates UsernamePasswordAuthenticationToken (unauthenticated)
      ↓
AuthenticationManager.authenticate()
      ↓
delegates to AuthenticationProvider
      ↓
AuthenticationProvider calls UserDetailsService.loadUserByUsername()
      ↓
Compares password via PasswordEncoder
      ↓
Returns authenticated token with GrantedAuthorities
      ↓
Token stored in SecurityContextHolder
```

### 34. JWT Flow (your domain — IAM)
```text
Login → validate credentials → generate JWT
      ↓
Each request: JwtAuthenticationFilter
      ↓
Extract token → validate signature + expiry
      ↓
Load UserDetails → build Authentication
      ↓
Set in SecurityContextHolder
      ↓
FilterSecurityInterceptor checks authorities
```

### 35. Authorization
- `@PreAuthorize("hasRole('ADMIN')")` — uses AOP proxy
- `@Secured({"ROLE_USER"})` — older, less flexible
- Method Security enabled via `@EnableMethodSecurity`

---

## Phase 11: Advanced Topics

### 36. Spring Boot Actuator
- Auto-configured when `spring-boot-starter-actuator` on classpath
- Key endpoints: `/health`, `/metrics`, `/beans`, `/env`, `/conditions`, `/mappings`
- `/conditions` → shows why auto-config was applied or skipped (debugging tool)
- `/beans` → shows all registered beans
- Secure in production: expose only `/health` publicly

### 37. Spring Events
```text
ApplicationEventPublisher.publishEvent(event)
      ↓
ApplicationEventMulticaster
      ↓
All matching @EventListener methods called
      (sync by default, async with @Async)
```
- Built-in events: `ContextRefreshedEvent`, `ApplicationStartedEvent`, `ApplicationReadyEvent`
- Custom events: extend `ApplicationEvent` or use any POJO (Spring 4.2+)
- Use case: decoupling modules (e.g., send email after user registration without direct call)

### 38. @Configuration & @Bean Deep Dive
- `@Configuration` classes are CGLIB-proxied
- Inter-bean method calls within `@Configuration` return same singleton instance
- `@Bean` inside `@Component` (lite mode) — NOT proxied → inter-bean calls create new instances
- `proxyBeanMethods = false` → disables CGLIB proxy, faster startup

### 39. Profiles & Environment
- `@Profile("dev")` — conditional bean registration
- `spring.profiles.active` property
- `@Value("${property}")` — property injection
- `@ConfigurationProperties` — type-safe binding (preferred over @Value for groups)
- Property resolution order (important):
  1. Command line args
  2. System properties
  3. Environment variables
  4. application-{profile}.yml
  5. application.yml

### 40. Spring Boot 3.x / Jakarta EE Changes
- `javax.*` → `jakarta.*` package rename
- Native image support via GraalVM
- Observability via Micrometer
- `ProblemDetail` for error responses
- Virtual threads support (Project Loom integration)

---

## Gaps in Your Original Roadmap

| Missing Topic | Why It Matters |
|---|---|
| `BeanFactoryPostProcessor` vs `BeanPostProcessor` | Asked directly in interviews — lifecycle confusion source |
| Proxy gotcha (self-invocation) | #1 cause of `@Transactional` bugs in production |
| Filter vs Interceptor vs AOP comparison | Very common interview question |
| JDK Proxy vs CGLIB difference | Directly tied to how Spring Boot defaults work |
| All 7 `@Transactional` propagation levels | Only 2-3 were implied in your original |
| JWT + Spring Security end-to-end flow | Core to your IAM background — should be your strongest topic |
| `@ConfigurationProperties` | Preferred over `@Value` in real projects, often asked |
| `ApplicationContext` types | `refreshContext()` and what it does internally |
| `@Configuration` lite mode vs full mode | Subtle but asked in senior-level interviews |
| Spring Boot 3.x changes | Any company running Boot 3 will ask about Jakarta migration |

---

## Corrected Learning Sequence