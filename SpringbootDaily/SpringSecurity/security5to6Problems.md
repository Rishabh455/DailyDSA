> “One major challenge during migration from Spring Security 5 to Spring Security 6 was that many older configuration approaches became deprecated or removed.

> Earlier, we were using `WebSecurityConfigurerAdapter`, but in Spring Security 6 it is removed. So we had to migrate to the new `SecurityFilterChain` bean-based configuration approach.

Old approach:

```java
extends WebSecurityConfigurerAdapter
```

New approach:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
```

> Another issue was related to authorization configuration. Earlier we used:

```java
.antMatchers()
```

> but in Spring Security 6 it was replaced with:

```java
.requestMatchers()
```

> We also faced issues with LDAP authentication configuration because some APIs and method signatures changed. We had to properly configure `ActiveDirectoryLdapAuthenticationProvider` with the new style configuration.

> CSRF and session management configurations also required changes because Spring Security 6 is more strict by default.

> Another important change was migration from `javax.*` to `jakarta.*` packages after Spring Boot 3 upgrade, which caused compilation issues in filters, servlets, and authentication-related classes.

Example:

```java
javax.servlet.Filter
```

became:

```java
jakarta.servlet.Filter
```

> We also updated custom JWT filters extending `OncePerRequestFilter` and ensured they were correctly added using:

```java
.addFilterBefore(jwtAuthFilter,
                 UsernamePasswordAuthenticationFilter.class)
```

> Overall, the migration mainly involved updating deprecated APIs, moving to component-based security configuration, fixing LDAP integration compatibility issues, and adapting the application to the new Spring Security 6 standards.”
--------------------read this directly
Security 5 → 6 Migration:

WebSecurityConfigurerAdapter → SecurityFilterChain
antMatchers() → requestMatchers()
javax.* → jakarta.*
Updated JWT Filters
Updated LDAP Config
CSRF & Session Config Changes