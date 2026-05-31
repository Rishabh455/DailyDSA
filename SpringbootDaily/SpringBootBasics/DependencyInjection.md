# 1️⃣ What is Dependency Injection (DI)?

Dependency Injection is a design pattern where **the dependencies of a class are provided by the Spring container instead of the class creating them itself**.

For example, in our **Password Management System**, the `PasswordService` depends on `OtpRepository` and `LdapService`.

Instead of creating them manually like:

```java
new OtpRepository();
```

Spring injects them automatically through **constructor injection**:

```java
@Service
public class PasswordService {

   private final OtpRepository otpRepo;

   public PasswordService(OtpRepository otpRepo){
       this.otpRepo = otpRepo;
   }
}
```

Spring creates both beans and injects them automatically.

This gives us:

* **loose coupling**
* **easy unit testing**
* **better maintainability**

For example, if we replace the OTP storage from **database to Redis**, the service layer does not change.

In our project we mostly used **constructor injection**, which is the recommended approach in Spring Boot.

---

# 2️⃣ What is Inversion of Control (IoC)?

Inversion of Control means that **the responsibility of creating and managing objects is transferred from the application code to the Spring container**.

Normally in Java we create objects using `new`.

But in Spring Boot, the **IoC container creates and manages beans automatically**.

For example, in our password reset system:

* Spring creates beans like `PasswordService`, `OtpService`, `LdapService`
* Injects them where required.

So instead of the application controlling object creation, **Spring manages the lifecycle of all beans inside the ApplicationContext**.

Dependency Injection is actually **the mechanism through which IoC is implemented in Spring**.

---

# 3️⃣ What is the Spring Bean Lifecycle?

A **Spring Bean** is any object managed by the **Spring IoC container**.

In our Password Management System, beans include:

* `PasswordService`
* `OtpService`
* `OtpRepository`
* `LdapService`

The lifecycle works like this:

### 1️⃣ Bean Creation

Spring creates the object when the application starts.

### 2️⃣ Dependency Injection

Spring injects dependencies.

Example:

```
PasswordService → OtpRepository injected
PasswordService → LdapService injected
```

### 3️⃣ Initialization

If we define `@PostConstruct`, Spring executes it after dependencies are injected.

Example:

```java
@PostConstruct
public void init(){
   log.info("Password service initialized");
}
```

### 4️⃣ Bean Ready

Now the bean is available for use in controllers and services.

### 5️⃣ Destruction

When the application shuts down, `@PreDestroy` methods run.

So the flow is:

```
Bean Creation
↓
Dependency Injection
↓
@PostConstruct
↓
Bean Ready
↓
@PreDestroy
```

---

# 4️⃣ Difference between @Component, @Service, @Repository

All three annotations register classes as **Spring Beans**, but they represent different layers of the application.

### @Component

Generic Spring bean.

Used for utility classes.

Example in our project:

```
OtpGenerator
TokenUtils
```

---

### @Service

Represents **business logic layer**.

Example in our password management project:

```
PasswordService
OtpService
AuthenticationService
```

These classes contain the main business logic like **OTP validation and password reset flow**.

---

### @Repository

Represents **data access layer**.

Example:

```
OtpRepository
UserRepository
```

These interact with the database using **Spring Data JPA**.

Spring also automatically **translates database exceptions into DataAccessException**.

---

# 5️⃣ What is @Autowired?

`@Autowired` is used by Spring to **automatically inject dependencies from the ApplicationContext**.

Example:

```java
@Autowired
private OtpRepository otpRepository;
```

Spring looks for a bean of type `OtpRepository` and injects it.

In our password reset system, this allowed services like `PasswordService` to automatically access the **OTP repository and LDAP integration service**.

However, in modern Spring Boot we prefer **constructor injection instead of field injection**, because it improves testability and immutability.

---

# 6️⃣ What is @Qualifier?

`@Qualifier` is used when **multiple beans of the same type exist**.

For example, in our project we could have different **OTP providers** like:

* EmailOtpService
* SmsOtpService

Both implement `OtpService`.

Spring would not know which one to inject.

So we specify:

```java
@Autowired
@Qualifier("emailOtpService")
private OtpService otpService;
```

This tells Spring exactly **which implementation should be injected**.

---

# 7️⃣ What is @Configuration?

`@Configuration` is used to define **custom bean configuration**.

Example from our project:

```java
@Configuration
public class SecurityConfig {

   @Bean
   public PasswordEncoder passwordEncoder(){
       return new BCryptPasswordEncoder();
   }
}
```

Spring registers the returned object as a bean.

In our password reset system we used `@Configuration` classes mainly for:

* **Spring Security configuration**
* **LDAP integration configuration**
* **Password encoder configuration**

---

# 8️⃣ What is Spring Boot?

Spring Boot is a framework built on top of Spring that **simplifies application development by reducing configuration and providing production-ready features**.

It provides:

* **auto configuration**
* **embedded servers**
* **starter dependencies**
* **production monitoring**

In our Password Management System we used Spring Boot to quickly build **REST APIs for password reset, OTP verification, and LDAP authentication** without manually configuring the server.

---

# 9️⃣ Difference between Spring vs Spring Boot

Traditional Spring requires **a lot of manual configuration**, such as setting up the dispatcher servlet, XML configuration, and dependency management.

Spring Boot simplifies this by providing:

* **embedded Tomcat server**
* **auto configuration**
* **starter dependencies**

For example, in our project we simply added:

```
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-data-jpa
```

And Spring Boot automatically configured the web server, REST APIs, and JPA integration.

---

# 🔟 What is Auto Configuration?

Auto configuration means **Spring Boot automatically configures beans based on the dependencies present in the classpath**.

For example, in our password management system we added:

```
spring-boot-starter-data-jpa
```

Spring Boot automatically configured:

* DataSource
* EntityManager
* Hibernate
* transaction management

Similarly, when we added **Spring Security**, Spring Boot automatically configured the security filter chain.

This significantly reduces manual configuration.

---

# 1️⃣1️⃣ What is Spring Boot Starter?

Spring Boot Starters are **preconfigured dependency bundles that simplify dependency management**.

For example in our project we used:

```
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-data-jpa
```

Instead of adding dozens of dependencies individually, a **starter automatically brings all required libraries together**.

This makes dependency management much easier.

---

# 1️⃣2️⃣ What is application.properties?

`application.properties` is the **central configuration file of a Spring Boot application**.

In our password management system we used it for configuration like:

Database connection:

```
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

LDAP configuration:

```
ldap.url
ldap.base-dn
```

Server configuration:

```
server.port=8080
```

Spring Boot automatically reads this file and configures the application during startup.

---

# 1️⃣3️⃣ What is Spring Boot Actuator?

Spring Boot Actuator provides **production monitoring and management endpoints**.

It helps monitor:

* application health
* metrics
* memory usage
* thread information

For example:

```
/actuator/health
```

returns whether the application is **UP or DOWN**.

In production environments, actuator endpoints are often integrated with monitoring tools like **Prometheus and Grafana** to monitor microservices health.

---

# ⭐ Final Interview Trick (Very Powerful)

After any answer you can say:

> “In our password management system we used this feature extensively for implementing secure OTP validation and LDAP password reset workflows.”

This **instantly convinces the interviewer you actually worked on the system**.

1️⃣ What happens internally when Spring Boot application starts?
When a Spring Boot application starts, several steps happen internally.
SpringApplication.run() is executed from the main class.

Spring Boot creates the ApplicationContext (IoC container).

Component scanning runs to detect beans like @Service, @Repository, etc.

Auto-configuration configures beans based on dependencies in the classpath.

Spring creates and initializes all singleton beans.

The embedded Tomcat server starts.

The application becomes ready to receive HTTP requests.

In our Password Management System, this process creates beans such as:
PasswordService

OtpService

OtpRepository

LdapService

SecurityConfig

and starts the REST API server.

2️⃣ What happens if two beans depend on each other?
This is called a circular dependency.
Example:
ServiceA → depends on ServiceB
ServiceB → depends on ServiceA
Spring cannot create these beans because it does not know which one to create first.
This causes:
BeanCurrentlyInCreationException
Solutions include:
Refactoring the design

Using setter injection

Introducing an intermediate service.

In our project we avoided circular dependencies by separating service responsibilities clearly between:
PasswordService

OtpService

LdapService


3️⃣ What is the difference between BeanFactory and ApplicationContext?
Both are IoC containers in Spring.
BeanFactory
ApplicationContext
Basic container
Advanced container
Lazy bean loading
Eager bean loading
No AOP or events
Supports AOP, events, internationalization
Spring Boot uses ApplicationContext by default.
For example, when our password reset API starts, Spring Boot loads all beans inside the ApplicationContext.

4️⃣ What happens if a bean is not found during dependency injection?
Spring throws:
NoSuchBeanDefinitionException
This happens if:
the class is not annotated with @Component / @Service

package scanning does not include that class

bean is not defined in configuration.

In our project this once happened when a custom LDAP service was outside the scanned package, so Spring could not find the bean.

5️⃣ What is the difference between Singleton and Prototype bean scope?
Spring beans can have different scopes.
Singleton (default)
Only one instance exists in the entire application.
Example:
PasswordService
OtpService
These should be singleton because they are stateless services.

Prototype
A new instance is created every time the bean is requested.
Example use case:
Objects with state or temporary data.
But in most Spring Boot applications we mainly use singleton scope.

6️⃣ What happens if an exception occurs inside a @Transactional method?
If a RuntimeException occurs, Spring automatically rolls back the transaction.
Example in our project:
During OTP verification:
verifyOtp → update password → delete OTP record
If LDAP password update fails, the transaction rolls back and OTP record remains unchanged.
This prevents inconsistent data.

7️⃣ What is the difference between @ComponentScan and @EnableAutoConfiguration?
@ComponentScan
Scans packages for Spring beans.
Example:
@Service
@Repository
@Controller

@EnableAutoConfiguration
Automatically configures beans based on dependencies.
Example:
If spring-boot-starter-web is present, Spring Boot configures:
Tomcat

DispatcherServlet

Jackson JSON converters.


@SpringBootApplication actually includes both.

8️⃣ What is the difference between @ControllerAdvice and @ExceptionHandler?
@ExceptionHandler
Handles exceptions inside a specific controller.
@ControllerAdvice
Handles exceptions globally across all controllers.
In our Password Management System we used global exception handling to handle errors like:
Invalid OTP

Expired OTP

LDAP authentication failures

and return proper API responses.

9️⃣ What is the difference between synchronous and asynchronous processing in Spring?
Synchronous
Request waits until the process finishes.
Example:
Password reset API
User waits until password is updated.

Asynchronous
Processing runs in background.
Spring provides:
@Async
Example from our project:
After password reset we published an audit event asynchronously so the user does not wait for logging to complete.

🔟 How does Spring Boot manage transactions internally?
Spring Boot uses AOP proxies to manage transactions.
When a method is annotated with:
@Transactional
Spring creates a proxy object around that bean.
Flow:
Client → Proxy → Transaction start → Method execution → Commit/Rollback
If an exception occurs, the proxy rolls back the transaction automatically.
In our OTP verification logic, this ensures that password update and OTP deletion happen atomically.
