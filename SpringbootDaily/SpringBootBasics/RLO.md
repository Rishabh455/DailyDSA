# Spring Boot Startup Flow - Deep Dive (3 YOE)

## Complete Flow

```text
Main Method
   ↓
SpringApplication.run()
   ↓
Create Environment
   ↓
Create ApplicationContext
   ↓
Component Scan
   ↓
Auto Configuration
   ↓
Bean Creation
   ↓
Dependency Injection
   ↓
Proxy Creation (AOP)
   ↓
Embedded Tomcat
   ↓
Application Ready
```

---

# 1. Main Method

```java
public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
}
```

### What Happens?

This is the application's entry point.

Nothing Spring-specific has happened yet.

Java simply starts the JVM and executes the `main()` method.

The moment Spring sees:

```java
SpringApplication.run(...)
```

control is handed over to Spring Boot.

---

# 2. SpringApplication.run()

### What Happens?

This is the bootstrap method of Spring Boot.

Internally it:

```text
Prepare Application
 ↓
Prepare Environment
 ↓
Create ApplicationContext
 ↓
Refresh Context
 ↓
Start Web Server
```

Think of it as the master coordinator that starts the entire Spring Boot lifecycle.

---

# 3. Create Environment

### What is Environment?

Environment contains application configuration.

Examples:

```properties
server.port=8081
spring.datasource.url=...
spring.profiles.active=dev
```

Spring loads configuration from:

```text
application.properties
application.yml
Environment Variables
Command Line Arguments
System Properties
```

All values are merged into one Environment object.

---

### Why Needed?

Because later Spring must know:

```text
Which profile is active?
Which database to connect?
Which port to start?
```

before creating beans.

---

# 4. Create ApplicationContext

### What is ApplicationContext?

Spring's IoC Container.

Responsible for:

```text
Creating Beans
Managing Beans
Injecting Dependencies
Managing Lifecycle
Creating Proxies
```

Example:

```java
UserController
UserService
UserRepository
```

All are managed inside ApplicationContext.

---

### Interview Definition

> ApplicationContext is Spring's IoC container responsible for creating, managing, and wiring beans.

---

# 5. Component Scan

Spring starts scanning packages.

Example:

```java
@SpringBootApplication
```

Suppose it finds:

```java
@RestController
public class UserController {}

@Service
public class UserService {}

@Repository
public class UserRepository {}
```

---

### Important

At this stage Spring does NOT create objects.

It only creates:

```text
Bean Definitions
```

Think of Bean Definition as metadata.

Example:

```text
Bean Name: userService
Class: UserService
Scope: Singleton
```

Spring is building a list of beans that need to be created.

---

# 6. Auto Configuration

This is Spring Boot's biggest feature.

Triggered by:

```java
@EnableAutoConfiguration
```

---

### Internal Flow

```text
@EnableAutoConfiguration
        ↓
AutoConfiguration.imports
        ↓
Load Auto Config Classes
        ↓
Check Conditions
        ↓
Create Infrastructure Beans
```

---

### Example

Dependency:

```xml
spring-boot-starter-web
```

Spring automatically configures:

```text
DispatcherServlet
Tomcat
Jackson
Spring MVC
```

---

Dependency:

```xml
spring-boot-starter-data-jpa
```

Spring automatically configures:

```text
DataSource
EntityManager
Hibernate
TransactionManager
```

---

### Important Interview Point

Spring Boot does NOT blindly create beans.

It checks conditions:

```java
@ConditionalOnClass
@ConditionalOnBean
@ConditionalOnProperty
@ConditionalOnMissingBean
```

Only if conditions match will configuration activate.

---

# 7. Bean Creation

Now Spring starts creating actual objects.

Bean Definition:

```text
UserService
```

becomes:

```java
new UserService()
```

---

### How?

Using Reflection.

Example:

```java
Class<?> clazz = UserService.class;

Object bean =
clazz.getDeclaredConstructor()
     .newInstance();
```

Spring uses Reflection heavily.

---

### Output

```text
UserController Object
UserService Object
UserRepository Object
```

now exist in memory.

---

# 8. Dependency Injection

Spring now resolves dependencies.

Example:

```java
@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```

---

Spring asks:

```text
Does UserRepository bean exist?
```

YES.

Inject it.

---

Result:

```text
UserController
      ↓
UserService
      ↓
UserRepository
```

Complete object graph is built.

---

# 9. Proxy Creation (AOP)

Now Spring checks whether any bean needs interception.

Example:

```java
@Transactional
@Async
@Cacheable
@Aspect
```

---

Suppose:

```java
@Service
public class UserService {

    @Transactional
    public void saveUser() {
    }
}
```

Spring creates:

```text
Original Bean
      ↓
Proxy Bean
```

---

### Why?

Because Spring needs to intercept method calls.

Without proxy:

```java
saveUser();
```

Spring cannot:

```text
Start Transaction
Commit Transaction
Rollback Transaction
```

---

### Runtime Flow

```text
Client
   ↓
Proxy Bean
   ↓
Start Transaction
   ↓
Actual Method
   ↓
Commit/Rollback
```

---

### Proxy Usage

```text
@Transactional
@Async
@Cacheable
Spring Security
AOP Logging
```

All work through proxies.

---

# 10. Embedded Tomcat Startup

Spring now starts Tomcat.

Internally:

```text
Create Web Server
 ↓
Register DispatcherServlet
 ↓
Register Filters
 ↓
Open Port 8080
```

---

Example Log

```text
Tomcat started on port(s): 8080
```

Now server is listening.

---

# 11. Application Ready

Everything is complete.

```text
Environment Ready
Context Ready
Beans Ready
Dependencies Injected
Proxies Created
Tomcat Running
```

Application can now serve requests.

---

# First Request Flow

```text
Browser
   ↓
GET /users/1
   ↓
Tomcat
   ↓
DispatcherServlet
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

---

# Ultimate Mental Model

```text
SpringApplication.run()
         ↓
Environment
         ↓
ApplicationContext
         ↓
Component Scan
         ↓
Auto Configuration
         ↓
Bean Definitions
         ↓
Bean Creation
         ↓
Dependency Injection
         ↓
Proxy Creation
         ↓
Tomcat Start
         ↓
Application Ready
```

If you fully understand these 11 steps, you'll understand most of what happens internally when a Spring Boot application starts.
