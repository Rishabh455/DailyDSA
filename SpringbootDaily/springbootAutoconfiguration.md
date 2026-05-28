@SpringBootApplication is a shortcut annotation in Spring Boot.

Internally it combines 3 annotations:

1. @Configuration
   → marks class as configuration class

2. @EnableAutoConfiguration
   → automatically configures Spring Boot based on dependencies

3. @ComponentScan
   → scans components, services, repositories, controllers automatically

Simple Flow:

@SpringBootApplication
↓
-

| @Configuration              |
| @EnableAutoConfiguration    |
| @ComponentScan              |
-------------------------------

```
      ↓
```

Spring Boot starts application
automatically configures beans
and scans components

MOST IMPORTANT INTERVIEW ANSWER:

@SpringBootApplication is a combination of @Configuration, @EnableAutoConfiguration, and @ComponentScan.

It marks the main Spring Boot class, enables automatic configuration, and scans Spring components automatically during application startup.
