## Embedded Tomcat in Spring Boot (Interview Answer)

### What is Embedded Tomcat?

> By default, Spring Boot comes with an embedded Tomcat server. It is packaged inside the application JAR, so we don't need to install or configure an external web server separately. We can start the application using `java -jar app.jar`.

---

### Can we replace Embedded Tomcat?

> Yes. Spring Boot's embedded server is pluggable. We can replace Tomcat with Jetty or Undertow by excluding the Tomcat dependency and adding the desired server dependency.

**Example:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

---

### How to deploy Spring Boot on JBoss / External Server?

> To deploy on an external server like JBoss, WebLogic, or external Tomcat, we create a WAR file instead of a JAR file.

#### Step 1: Change Packaging

```xml
<packaging>war</packaging>
```

#### Step 2: Extend SpringBootServletInitializer

```java
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### Step 3: Mark Tomcat as Provided

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

---

## Quick Revision (30 Seconds)

**Default:**

* Packaging = JAR
* Embedded Tomcat
* Run using `java -jar`

**Replace Server:**

* Exclude Tomcat
* Add Jetty or Undertow

**External Server (JBoss/WebLogic/Tomcat):**

* Packaging = WAR
* Extend `SpringBootServletInitializer`
* Tomcat scope = `provided`
* Deploy WAR on server

---

## One-Liner Interview Answer

> "Spring Boot uses Embedded Tomcat by default. If needed, we can replace it with Jetty or Undertow by changing dependencies. For deployment on external servers like JBoss, we package the application as a WAR file, extend `SpringBootServletInitializer`, and mark Tomcat as provided." 🚀
