# Spring Security — Complete Interview Prep (3 YOE Level)

> Goal: Samajh + ratta dono. Har concept simple Hinglish me + 1 example + interview trap.

---

## 1. Big Picture — Request Flow

```
Client
  ↓
Tomcat
  ↓
DelegatingFilterProxy   → servlet container ko Spring Security se connect karta
  ↓
FilterChainProxy        → actual chain manage karta, URL-wise filters decide karta
  ↓
SecurityFilterChain      → filters ki list (CSRF, JWT, Auth, etc.)
  ↓
DispatcherServlet
  ↓
Controller → Service → DB
```

**One-line definition (bolo exactly isi tarah interview me):**
> "Security Filter Chain is a sequence of servlet filters used by Spring Security to intercept every incoming request for authentication, authorization, session management, CSRF protection, and other security concerns before the request reaches the controller."

---

## 2. Core Components — Kaun Kya Karta Hai

| Component | Kaam | Analogy |
|---|---|---|
| `DelegatingFilterProxy` | Tomcat ↔ Spring Security bridge | Gate |
| `FilterChainProxy` | URL ke basis pe sahi filter chain choose karta | Security supervisor |
| `SecurityFilterChain` | Filters ki ordered list | Checkpoints |
| `AuthenticationManager` | Auth request ko delegate karta, khud verify nahi karta | Receptionist |
| `AuthenticationProvider` | Actual verification karta | Employee verifier |
| `DaoAuthenticationProvider` | DB-based default provider | — |
| `UserDetailsService` | DB se user load karta (`loadUserByUsername`) | — |
| `PasswordEncoder` | Password hash compare karta (BCrypt) | — |
| `SecurityContextHolder` | Current authenticated user store karta (ThreadLocal) | ID card |
| `OncePerRequestFilter` | JWT filters isse extend karte (1 request = 1 execution) | — |

**Authentication vs Authorization:**
- Authentication = "Tu kaun hai?" → handled by `AuthenticationManager`
- Authorization = "Tu allowed hai?" → handled by `AuthorizationFilter` (e.g. `.hasRole("ADMIN")`)

---

## 3. End-to-End Login Flow (Yaad Karne Wali Diagram)

```
1. Client sends username/password
2. UsernamePasswordAuthenticationFilter → Authentication object banata (authenticated=false)
3. AuthenticationManager.authenticate(auth) call hota
4. ProviderManager (default impl of AuthenticationManager) provider choose karta
5. DaoAuthenticationProvider:
     a. UserDetailsService.loadUserByUsername() → DB se user
     b. PasswordEncoder.matches(raw, encoded) → password check
6. Success → Authentication object (authenticated=true, roles attached)
7. SecurityContextHolder.getContext().setAuthentication(auth)
8. Response (JWT generate hota agar token-based hai)
```

---

## 4. JWT Flow (Stateless)

```
Request with JWT token
   ↓
JWT Filter (extends OncePerRequestFilter)
   ↓
Token validate → username extract
   ↓
UserDetailsService se user load
   ↓
Authentication object banaya
   ↓
SecurityContextHolder.setAuthentication()
   ↓
AuthorizationFilter (role check)
   ↓
Controller
```

⚠️ **Important:** `AuthenticationManager` JWT me **sirf login time** use hota hai (token generate karte waqt). Har subsequent request pe **nahi** — wahan sirf JWT filter validate karke directly `SecurityContextHolder` set karta hai.

---

## 5. Modern Security Config (Spring Security 6 style)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                 // stateless APIs me disable
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## 6. Spring Security 5 vs Spring Security 6 — Side by Side

| Aspect | Spring Security 5 | Spring Security 6 |
|---|---|---|
| Config style | `extends WebSecurityConfigurerAdapter`, override `configure(HttpSecurity)` | `WebSecurityConfigurerAdapter` **removed (deprecated)**. Use `@Bean SecurityFilterChain` |
| HttpSecurity style | Method chaining: `.authorizeRequests().antMatchers(...)` | Lambda DSL: `.authorizeHttpRequests(auth -> auth...)` |
| URL matchers | `antMatchers()`, `mvcMatchers()` | `requestMatchers()` (unified) |
| Password encoding | `NoOpPasswordEncoder` allowed easily | Plaintext discouraged, `BCryptPasswordEncoder` enforced by default |
| AuthenticationManagerBuilder | `auth.inMemoryAuthentication()` inside `configure(AuthenticationManagerBuilder)` | Define `UserDetailsService` & `AuthenticationManager` as separate `@Bean`s |
| CSRF default | Enabled by default | Still enabled by default, but lambda config: `.csrf(csrf -> csrf.disable())` |
| Java baseline | Java 8+ | Java 17+ (Spring Boot 3.x requirement) |
| `@EnableGlobalMethodSecurity` | Used for method-level security | Deprecated → use `@EnableMethodSecurity` |
| Authorize order | Order matters but old syntax allowed mistakes more easily | Lambda DSL forces more explicit, readable rules |
| Default behaviour for unauthenticated requests | Similar | Similar, but `anyRequest().authenticated()` is now near-mandatory best practice |

**Interview line:**
> "Spring Security 6 removed `WebSecurityConfigurerAdapter` and moved fully to component-based, lambda-style configuration using `SecurityFilterChain` beans — this is part of Spring's move toward composition over inheritance, and aligns with Spring Boot 3 / Java 17 baseline."

---

## 7. Important Annotations — Quick Table

| Annotation | Use |
|---|---|
| `@EnableWebSecurity` | Enables Spring Security web config |
| `@EnableMethodSecurity` | Enables method-level security (replaces `@EnableGlobalMethodSecurity`) |
| `@PreAuthorize("hasRole('ADMIN')")` | Check before method execution |
| `@PostAuthorize("returnObject.owner == authentication.name")` | Check after method execution, based on return value |
| `@Secured("ROLE_ADMIN")` | Older role-based method security (less flexible than `@PreAuthorize`) |
| `@RolesAllowed("ADMIN")` | JSR-250 standard annotation, similar to `@Secured` |
| `@Bean` | Define filter chain, password encoder, auth manager, etc. |
| `@Configuration` | Marks security config class |
| `@Component` | Custom filters/providers as Spring beans |
| `@Service` | `UserDetailsService` implementation |
| `@AuthenticationPrincipal` | Inject current logged-in user into controller method |
| `@CrossOrigin` | CORS config at controller/method level |

---

## 8. Most Asked Interview Q&A (Simple Language)

**Q1. Filter vs Interceptor?**

| Filter | Interceptor |
|---|---|
| Servlet level | Spring MVC level |
| Before DispatcherServlet | After DispatcherServlet |
| Low-level (works on raw request) | Controller-level (works on handler) |

---

**Q2. Why does JWT filter run before `UsernamePasswordAuthenticationFilter`?**
> Taaki request authenticate ho jaye before Spring Security ke authorization checks chalein. JWT filter pehle token validate karega aur `SecurityContextHolder` set karega, fir authorization filter role check karega.

```java
http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
```

---

**Q3. Why extend `OncePerRequestFilter` for JWT?**
> Taaki same request pe filter ek hi baar chale — avoid duplicate execution (e.g. forward/include scenarios me filter dobara chal sakta hai agar normal `Filter` use kiya).

---

**Q4. AuthenticationManager khud authenticate karta hai?**
> ❌ NO. Ye sirf **delegate** karta hai `AuthenticationProvider` ko. Default implementation `ProviderManager` hai, jo multiple providers (DB, JWT, LDAP, OAuth) manage karta hai aur jo `supports()` true return kare, usi se authenticate karwata hai.

---

**Q5. AuthenticationManager vs AuthenticationProvider — difference?**

| AuthenticationManager | AuthenticationProvider |
|---|---|
| Manager / delegator | Worker / actual logic |
| Interface with 1 method: `authenticate()` | Interface with `authenticate()` + `supports()` |
| Default impl: `ProviderManager` | Default impl: `DaoAuthenticationProvider` |

---

**Q6. `supports()` method kyu chahiye?**
> Ek app me multiple `AuthenticationProvider` ho sakte (username/password, JWT, OTP). `supports()` decide karta hai ki ye provider kis type ka `Authentication` object handle kar sakta hai. `ProviderManager` har provider try karta jab tak koi `supports() == true` return na kare.

```java
@Override
public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
}
```

---

**Q7. SecurityContextHolder internally kaise kaam karta hai?**
> Internally `ThreadLocal` use karta hai — har request/thread ka apna alag `SecurityContext` hota hai, taaki concurrent users ka data mix na ho (thread-safe).

```
SecurityContextHolder → ThreadLocal → SecurityContext → Authentication → Principal
```

---

**Q8. JWT (stateless) me SecurityContextHolder kaise behave karta hai?**
> Session-based login me context session se restore hota hai. JWT me **har request pe dobara populate hota hai** kyunki server kuch store nahi karta (stateless). JWT filter token validate karke fresh `Authentication` object banata aur set karta hai.

---

**Q9. Logout pe kya hota hai?**
```java
SecurityContextHolder.clearContext();
```
> Current user ka authentication data thread se remove ho jata hai.

---

**Q10. `filterChain.doFilter(request, response)` na call kiya to?**
> Request aage chain me nahi jayegi — controller hit hi nahi hoga. Chain wahi stop ho jayegi. Custom filter likhte waqt yeh sabse common bug hai.

---

**Q11. Stateless vs Stateful session?**

| Stateful | Stateless |
|---|---|
| Server session store karta (Session ID) | Server kuch store nahi karta |
| Traditional form login | JWT-based, har request me token aata |
| `SessionCreationPolicy.IF_REQUIRED` (default) | `SessionCreationPolicy.STATELESS` |

---

**Q12. CSRF kab disable karte hain aur kyu?**
> Form-based login (browser, cookies) me CSRF zaroori hai. Stateless JWT REST APIs me cookies use nahi hote, isliye CSRF attack ka risk nahi hota → `csrf.disable()` safe hai.

---

**Q13. Password kaise compare hota hai — plain text?**
> Nahi. `PasswordEncoder` (usually `BCryptPasswordEncoder`) use hota hai. Login ke time `passwordEncoder.matches(rawPassword, encodedPasswordFromDB)` call hota — DB me hash store rehta hai, raw kabhi compare nahi hota.

---

**Q14. `UserDetailsService` vs `AuthenticationProvider` — difference?**

| AuthenticationProvider | UserDetailsService |
|---|---|
| Full authentication logic | Sirf user load karta DB se |
| Password verify karta | Password verify nahi karta |
| Roles/authorities attach karta | Sirf raw user data deta |

---

**Q15. `@PreAuthorize` vs `.hasRole()` in SecurityFilterChain — kab use karein?**
> `SecurityFilterChain` me `.hasRole()` URL-level (endpoint-level) authorization ke liye hai. `@PreAuthorize` method-level fine-grained control deta hai (e.g. service layer me, ya jab condition complex ho — `hasRole('ADMIN') and #id == authentication.principal.id`).

---

## 9. Common Interview Traps ⚠️ (3 YOE level pe specifically poochte hain)

1. **Trap:** "JWT me AuthenticationManager har request pe use hota hai?"
   **Answer:** ❌ No — sirf login/token-generation time. Baad ke requests JWT filter handle karta hai directly.

2. **Trap:** "AuthenticationManager hi DB query karta hai?"
   **Answer:** ❌ No — ye delegate karta hai `AuthenticationProvider` (e.g. `DaoAuthenticationProvider`) ko, jo `UserDetailsService` use karta hai.

3. **Trap:** "WebSecurityConfigurerAdapter abhi bhi use karna chahiye?"
   **Answer:** ❌ Spring Security 5.7+ se deprecated, Spring Security 6 me **removed**. `SecurityFilterChain` bean use karo.

4. **Trap:** "CSRF hamesha disable kar dena chahiye REST API me?"
   **Answer:** Generic statement galat hai — sirf **stateless, token-based (no cookie session)** APIs me safe hai disable karna. Cookie-based session auth me CSRF zaroori hai.

5. **Trap:** "`hasRole("ADMIN")` aur `hasAuthority("ADMIN")` same hai?"
   **Answer:** ❌ `hasRole("ADMIN")` internally `ROLE_ADMIN` check karta (prefix auto add hota). `hasAuthority("ADMIN")` exact string match karta — `ROLE_` prefix add nahi hota.

6. **Trap:** "Filter order matter nahi karta, Spring khud handle kar lega?"
   **Answer:** ❌ Order bahut critical hai. JWT filter agar `UsernamePasswordAuthenticationFilter` ke baad add ho jaye, to authentication galat order me ho sakti — security bypass ka risk.

7. **Trap:** "SecurityContextHolder thread-safe hai by default for async/multi-threaded code?"
   **Answer:** Default `MODE_THREADLOCAL` hai — naye thread (e.g. `@Async`) me context **inherit nahi hota** unless `MODE_INHERITABLETHREADLOCAL` set karo.

8. **Trap:** "401 vs 403 — same hi hai?"
   **Answer:** ❌ 401 Unauthorized = authentication fail (credentials galat/missing). 403 Forbidden = authenticated hai but authorization fail (role/permission nahi hai).

---

## 10. Final Revision Cheat-Sheet (1-Minute Recall)

```
SecurityFilterChain  = pipeline of security filters
AuthenticationManager → delegates → AuthenticationProvider → verifies
                          ↓
                  UserDetailsService (load user)
                          ↓
                  PasswordEncoder (compare password)
                          ↓
                  SecurityContextHolder (store result, ThreadLocal)

JWT flow: Token → OncePerRequestFilter → validate → SecurityContext set → AuthorizationFilter → Controller

SS5 → SS6: WebSecurityConfigurerAdapter ❌ → @Bean SecurityFilterChain ✅
           method chaining ❌ → lambda DSL ✅
           @EnableGlobalMethodSecurity ❌ → @EnableMethodSecurity ✅
```