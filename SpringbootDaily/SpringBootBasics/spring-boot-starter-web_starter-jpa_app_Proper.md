Interview ke liye itna hi yaad rakho. Ye pura flow 90% questions cover kar dega.

```text
spring-boot-starter-web
        |
        ├── spring-web
        │      ├── RestController
        │      ├── RequestMapping
        │      └── HTTP APIs
        |
        ├── spring-webmvc
        │      ├── DispatcherServlet
        │      ├── ViewResolver
        │      ├── HandlerMapping
        │      └── MVC Architecture
        |
        ├── jackson-databind
        │      └── ObjectMapper
        │
        ├── spring-boot-starter-tomcat
        │      └── Embedded Tomcat
        │
        └── validation
               └── @Valid
```

```text
spring-boot-starter-data-jpa
        |
        ├── spring-data-jpa
        │      └── JpaRepository
        │
        ├── spring-tx
        │      └── @Transactional
        │          PlatformTransactionManager
        │
        ├── spring-orm
        │      └── EntityManager
        │
        └── Hibernate
               └── JPA Implementation
```

```text
application.properties
        |
        ├── datasource.url
        ├── datasource.username
        └── datasource.password
                    |
                    ▼
              DataSource Bean
```

### 20-Second Interview Answer

> **spring-boot-starter-web** gives me Spring MVC, DispatcherServlet, ViewResolver, Jackson ObjectMapper, Validation support, and Embedded Tomcat.
>
> **spring-boot-starter-data-jpa** gives me JPA repositories, Hibernate, EntityManager, TransactionManager, and `@Transactional` support.
>
> Spring Boot detects these dependencies on the classpath and auto-configures the required beans automatically.

### One-Line Revision

```text
Web Starter  -> MVC + DispatcherServlet + ObjectMapper + Tomcat
JPA Starter  -> Hibernate + EntityManager + TransactionManager
Spring Boot  -> Auto Configuration
Spring Framework -> Actual Classes
```

Bas ye 4 lines rat lo. Interviewer agar pooche **"DispatcherServlet kahan se aata hai?"**, **"ObjectMapper kaun banata hai?"**, **"Transactional kaise kaam karta hai?"**, sabka answer isi flow se nikal jayega. 🚀
