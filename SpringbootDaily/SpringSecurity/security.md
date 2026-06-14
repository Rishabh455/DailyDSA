spring security
# Spring Security Filter Chain — Beginner to Advanced

Spring Security ka heart hi hai **Security Filter Chain**.
Agar ye samajh gaya, to authentication, authorization, JWT, CSRF, session — sab clear ho jayega.

---

# 1. Sabse Pehle — Filter kya hota hai?

Java web application me request directly controller tak nahi jaati.

Flow hota hai:

```text
Client Request
      ↓
Filters
      ↓
DispatcherServlet
      ↓
Controller
      ↓
Service
      ↓
Response
```

Filter ka kaam:

* request ko intercept karna
* check karna
* modify karna
* block karna
* logging
* authentication
* authorization

---

# 2. Security Filter Chain kya hota hai?

Spring Security multiple filters ka ek chain banata hai.

```text
Request
   ↓
Security Filter 1
   ↓
Security Filter 2
   ↓
Security Filter 3
   ↓
...
   ↓
Controller
```

Har filter ka specific kaam hota hai.

Example:

* username/password check
* JWT validation
* CSRF token validation
* session validation
* authorization

---

# 3. Real Life Analogy 🚪

Imagine airport security.

```text
Entry Gate
   ↓
Ticket Check
   ↓
ID Verification
   ↓
Bag Scan
   ↓
Final Approval
   ↓
Flight
```

Exactly same Spring Security me hota hai.

Request har security checkpoint se pass hoti hai.

---

# 4. Internally Kaise Kaam Karta Hai

Main component:

## `DelegatingFilterProxy`

Ye servlet container aur Spring Security ko connect karta hai.

Server request ko yaha bhejta hai.

```text
Tomcat
   ↓
DelegatingFilterProxy
   ↓
FilterChainProxy
   ↓
SecurityFilterChain
```

---

# 5. Main Hero → `FilterChainProxy`

Ye actual security chain manage karta hai.

Ye decide karta hai:

* kaunsi URL pe kaunsi filters chalengi

Example:

```java
/api/**  → JWT filters
/admin/** → admin filters
/public/** → no filters
```

---

# 6. SecurityFilterChain

Ye ek list hoti hai filters ki.

Example:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
```

Isme hum define karte:

* authentication
* authorization
* csrf
* sessions
* jwt
* login

---

# 7. Important Built-in Filters

## (1) UsernamePasswordAuthenticationFilter

Login handle karta hai.

```text
username + password read karta hai
```

Example:

```json
{
  "username":"rishabh",
  "password":"1234"
}
```

Ye authenticate karta hai.

---

## (2) BasicAuthenticationFilter

HTTP Basic Auth handle karta hai.

```text
Authorization: Basic base64(username:password)
```

---

## (3) OncePerRequestFilter

JWT filters mostly isse extend karte hain.

Why?

Taaki ek request me filter sirf ek baar chale.

Example:

```java
public class JwtFilter extends OncePerRequestFilter
```

---

## (4) SecurityContextHolderFilter

Authenticated user ko context me store karta hai.

---

## (5) AnonymousAuthenticationFilter

Agar login nahi hai:

* anonymous user assign karta hai

---

## (6) ExceptionTranslationFilter

Security exceptions handle karta hai.

Example:

* 401 Unauthorized
* 403 Forbidden

---

## (7) AuthorizationFilter

Check karta:

* user allowed hai ya nahi

Example:

```java
.hasRole("ADMIN")
```

---

# 8. Request Flow Step By Step

Suppose request:

```http
GET /api/users
Authorization: Bearer JWT_TOKEN
```

Flow:

```text
Request
 ↓
DelegatingFilterProxy
 ↓
FilterChainProxy
 ↓
JWT Filter
   → token validate
   → user extract
 ↓
SecurityContext set
 ↓
AuthorizationFilter
   → role check
 ↓
Controller
 ↓
Response
```

---

# 9. JWT Filter Ka Actual Role

JWT architecture me session nahi hota.

Har request me token aata hai.

JWT filter:

1. header read karta
2. token validate karta
3. username extract karta
4. user details load karta
5. authentication object banata
6. SecurityContext me set karta

---

# 10. SecurityContext kya hota hai?

Current authenticated user ka data.

```java
SecurityContextHolder.getContext()
```

Isme:

* username
* roles
* authorities
* authentication status

store hota hai.

---

# 11. Authentication vs Authorization

## Authentication

"Tu kaun hai?"

Example:

```text
username/password
JWT token
```

---

## Authorization

"Tu allowed hai ya nahi?"

Example:

```java
hasRole("ADMIN")
```

---

# 12. Custom JWT Filter Example

```java
present in jwt fdlow
```

---

# 13. `filterChain.doFilter()` Important Hai ⚠️

Agar ye nahi call kiya:

```java
filterChain.doFilter(request,response);
```

to request aage nahi jayegi.

Controller hit hi nahi hoga.

Chain wahi stop ho jayegi.

---

# 14. Filter Order Bahut Important Hai

JWT filter galat position pe hua to security fail ho sakti.

Example:

```java
http.addFilterBefore(jwtFilter,
        UsernamePasswordAuthenticationFilter.class);
```

Means:
JWT filter login filter se pehle chalega.

---

# 15. Spring Security 6 Architecture

Pehle:

```java
WebSecurityConfigurerAdapter
```

Now deprecated ❌

Ab:

```java
@Bean
SecurityFilterChain
```

Use hota hai.

---

# 16. Complete Modern Config

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )

        .sessionManagement(session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )

        .addFilterBefore(jwtFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

# 17. Stateless vs Stateful

## Stateful

Server session store karta.

```text
Session ID
```

Traditional login.

---

## Stateless

Server kuch store nahi karta.

JWT token har request me aata.

Microservices me mostly यही use hota.

---

# 18. CSRF Filter

Form based login me important.

Check karta:

* request genuine hai ya nahi

JWT APIs me mostly disable karte.

```java
csrf.disable()
```

---

# 19. Interview Deep Questions

## Q1. Why OncePerRequestFilter?

Taaki same request pe multiple execution na ho.

---

## Q2. Why SecurityContextHolder?

Authenticated user ko globally access karne ke liye.

---

## Q3. Why JWT filter before UsernamePasswordAuthenticationFilter?

Taaki request authenticate ho jaye before Spring security authorization checks.

---

## Q4. Difference between Filter and Interceptor?

| Filter                   | Interceptor             |
| ------------------------ | ----------------------- |
| Servlet level            | Spring MVC level        |
| Before DispatcherServlet | After DispatcherServlet |
| Low level                | Controller level        |

---

# 20. Actual Full Picture

```text
Client
 ↓
Tomcat
 ↓
DelegatingFilterProxy
 ↓
FilterChainProxy
 ↓
Security Filters
   ↓
JWT Filter
   ↓
Authentication
   ↓
Authorization
 ↓
DispatcherServlet
 ↓
Controller
 ↓
Service
 ↓
DB
 ↓
Response
```

---

# 21. Most Important Interview Line ⭐

> "Security Filter Chain is a sequence of servlet filters used by Spring Security to intercept every incoming request for authentication, authorization, session management, CSRF protection, and other security concerns before the request reaches the controller."

---

# 22. Ekdum Short Revision

## Security Filter Chain

= Multiple security filters ka pipeline.

## Main kaam

* Authentication
* Authorization
* JWT validation
* Session handling
* CSRF protection

## Main classes

* DelegatingFilterProxy
* FilterChainProxy
* SecurityFilterChain
* OncePerRequestFilter

## JWT flow

```text
Request → JWT Filter → Validate Token →
SecurityContext → Authorization → Controller
```
# AuthenticationManager in Spring Security — Complete Understanding

AuthenticationManager Spring Security ka core authentication engine hai.

Agar simple language me bole:

> "AuthenticationManager ka kaam user ko verify karna hota hai."

Matlab:

* username/password sahi hai?
* JWT valid hai?
* user exist karta hai?
* credentials correct hai?

---

# 1. Big Picture First

Jab user login karta hai:

```text id="znkw1u"
Client
  ↓
UsernamePasswordAuthenticationFilter
  ↓
AuthenticationManager
  ↓
AuthenticationProvider
  ↓
UserDetailsService
  ↓
Database
```

---

# 2. AuthenticationManager Kya Hai?

Ye ek interface hai.

```java id="c3yc4m"
public interface AuthenticationManager {

    Authentication authenticate(
        Authentication authentication
    ) throws AuthenticationException;
}
```

Bas ek hi method:

```java id="1o9j0r"
authenticate()
```

---

# 3. Authentication Object Kya Hai?

Ye user credentials carry karta hai.

Before login success:

```java id="3nt08q"
UsernamePasswordAuthenticationToken
```

Contains:

* username
* password

After login success:

* username
* authorities/roles
* authenticated=true

---

# 4. Real Flow Step by Step

Suppose login request:

```json id="zjlwm5"
{
  "username":"rishabh",
  "password":"1234"
}
```

---

## Step 1 → Filter Request Receive Karta

Usually:

```java id="8lnl4q"
UsernamePasswordAuthenticationFilter
```

Ye:

* username extract karta
* password extract karta

---

## Step 2 → Authentication Object Banata

```java id="9lnh1y"
Authentication auth =
    new UsernamePasswordAuthenticationToken(
        username,
        password
    );
```

Abhi:

```text id="6x4pl3"
authenticated = false
```

---

## Step 3 → AuthenticationManager ko bhejta

```java id="xgq6mk"
authenticationManager.authenticate(auth);
```

---

# 5. AuthenticationManager Khud Verify Nahi Karta ⚠️

Bahut important.

AuthenticationManager directly DB check nahi karta.

Ye kaam delegate karta hai:

```text id="k3qy0t"
AuthenticationProvider
```

---

# 6. Internal Architecture

```text id="t3z0ql"
AuthenticationManager
      ↓
AuthenticationProvider
      ↓
UserDetailsService
      ↓
Database
```

---

# 7. Actual Implementation → ProviderManager

Mostly internally Spring use karta:

```java id="n4j4fw"
ProviderManager
```

Ye AuthenticationManager ka implementation hai.

---

# 8. ProviderManager Ka Kaam

Ye multiple providers ko manage karta.

Example:

* JWT auth
* DB auth
* LDAP auth
* OAuth2 auth

```text id="8h9gdl"
ProviderManager
   ↓
Provider 1
Provider 2
Provider 3
```

---

# 9. AuthenticationProvider Kya Hai?

Actual authentication yahi karta hai.

Interface:

```java id="9h4x6e"
public interface AuthenticationProvider {

    Authentication authenticate(
        Authentication authentication);

    boolean supports(Class<?> authentication);
}
```

---

# 10. DaoAuthenticationProvider

Most common provider.

Ye:

* DB se user load karta
* password compare karta

---

# 11. DaoAuthenticationProvider Flow

```text id="0lmz54"
UsernamePasswordAuthenticationFilter
        ↓
AuthenticationManager
        ↓
DaoAuthenticationProvider
        ↓
UserDetailsService
        ↓
Database
```

---

# 12. UserDetailsService Ka Role

Ye database se user fetch karta.

```java id="rjjw4e"
UserDetails loadUserByUsername(String username)
```

Example:

```java id="ut2zw2"
@Service
public class MyUserDetailsService
       implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(
            String username) {

        return userRepository
                .findByUsername(username);
    }
}
```

---

# 13. PasswordEncoder Ka Role

Password plain text me compare nahi hote.

```java id="hphhmy"
BCryptPasswordEncoder
```

Use hota hai.

---

# 14. Complete Login Flow Internally

```text id="14l49n"
1. User sends username/password
2. Filter creates Authentication object
3. AuthenticationManager.authenticate()
4. ProviderManager starts
5. DaoAuthenticationProvider called
6. UserDetailsService loads user
7. PasswordEncoder checks password
8. Success Authentication object created
9. SecurityContext updated
10. User authenticated
```

---

# 15. Success Ke Baad Kya Hota Hai?

Before success:

```text id="3lwlji"
authenticated = false
```

After success:

```text id="sd8q6z"
authenticated = true
```

Authorities bhi attach hoti:

```java id="zjlwm7"
ROLE_ADMIN
ROLE_USER
```

---

# 16. SecurityContext Me Store

After authentication:

```java id="g0m6pw"
SecurityContextHolder
    .getContext()
    .setAuthentication(auth);
```

Ab current user globally available.

---

# 17. Custom AuthenticationManager Example

Modern Spring Security:

```java id="e57zsu"
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config)
        throws Exception {

    return config.getAuthenticationManager();
}
```

---

# 18. Complete Security Config

```java id="v5oj31"
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails user =
            User.builder()
                .username("rishabh")
                .password(passwordEncoder()
                .encode("1234"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config)
        throws Exception {

        return config.getAuthenticationManager();
    }
}
```

---

# 19. JWT Authentication Me AuthenticationManager

Bahut important interview concept.

## Login Time

AuthenticationManager use hota hai.

```text id="1rq19c"
username/password validate
```

Then:

```text id="08e7lz"
JWT token generate
```

---

## Later Requests

JWT filter token validate karta.

AuthenticationManager usually use nahi hota every request me.

---

# 20. Interview Trick Question ⚠️

## "JWT me AuthenticationManager har request pe use hota hai?"

Mostly:
❌ NO

Sirf login time pe use hota.

Baaki requests:

* JWT filter
* token validation
* SecurityContext set

---

# 21. AuthenticationManager vs AuthenticationProvider

| AuthenticationManager           | AuthenticationProvider     |
| ------------------------------- | -------------------------- |
| Delegates auth                  | Actual auth logic          |
| Manager                         | Worker                     |
| Multiple providers manage karta | Credentials validate karta |

---

# 22. Authentication vs Authorization

## Authentication

```text id="2mmbz7"
Who are you?
```

Handled by:

```text id="xttmo5"
AuthenticationManager
```

---

## Authorization

```text id="37wfe9"
Are you allowed?
```

Handled by:

```text id="c7ef6j"
AuthorizationFilter
```

---

# 23. Common Interview Questions

## Q1. Why AuthenticationManager needed?

Authentication process centralize karne ke liye.

---

## Q2. Default implementation?

```java id="s5hzhq"
ProviderManager
```

---

## Q3. Actual authentication kaun karta?

```java id="1b7c4g"
AuthenticationProvider
```

---

## Q4. User DB se kaun load karta?

```java id="5ukvjl"
UserDetailsService
```

---

## Q5. Password compare kaun karta?

```java id="rwv84g"
PasswordEncoder
```

---

# 24. Complete Internal Diagram

```text id="2v7y05"
Client Login Request
       ↓
UsernamePasswordAuthenticationFilter
       ↓
AuthenticationManager
       ↓
ProviderManager
       ↓
DaoAuthenticationProvider
       ↓
UserDetailsService
       ↓
Database
       ↓
PasswordEncoder
       ↓
Authentication Success
       ↓
SecurityContextHolder
```

---

# 25. Most Important Interview Explanation ⭐

> "AuthenticationManager is the central authentication API in Spring Security. It receives an Authentication object from security filters and delegates the authentication process to one or more AuthenticationProviders. After successful authentication, it returns a fully authenticated Authentication object containing user details and authorities."

---

# 26. Ekdum Short Revision

## AuthenticationManager

= authentication ka central engine

## Main implementation

```java id="0jjdzd"
ProviderManager
```

## Actual verification

```java id="s4c54n"
AuthenticationProvider
```

## User loading

```java id="qgdmtm"
UserDetailsService
```

## Password matching

```java id="jlwm13"
PasswordEncoder
```

## Success ke baad

```java id="u8r4pz"
SecurityContextHolder
```

me user store hota hai.

# AuthenticationProvider in Spring Security — Complete Understanding

Agar simple language me samjhe:

> `AuthenticationProvider` actual authentication karta hai.

Matlab:

* username/password verify karna
* JWT validate karna
* LDAP check karna
* OAuth verify karna

Ye sab AuthenticationProvider ka kaam hai.

---

# 1. Sabse Pehle Big Picture

```text id="8v3v1g"
Client Login Request
        ↓
UsernamePasswordAuthenticationFilter
        ↓
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
Database / JWT / LDAP
```

---

# 2. Important Concept ⚠️

Bahut log confuse hote hain:

| Component              | Kaam                            |
| ---------------------- | ------------------------------- |
| AuthenticationManager  | Manager hai                     |
| AuthenticationProvider | Actual authentication karta hai |

---

# 3. Real Life Analogy 🏢

Imagine company office.

```text id="9xph0k"
Receptionist → AuthenticationManager
Employee Verifier → AuthenticationProvider
```

Receptionist bas request forward karta.

Verification actual employee verifier karta.

---

# 4. AuthenticationProvider Kya Hai?

Ye ek interface hai.

```java id="2d5j9i"
public interface AuthenticationProvider {

    Authentication authenticate(
            Authentication authentication)
            throws AuthenticationException;

    boolean supports(Class<?> authentication);
}
```

---

# 5. Do Important Methods

## (1) authenticate()

Actual verification yaha hota.

---

## (2) supports()

Ye check karta:

```text id="6fjlwm"
ye provider kis type authentication ko support karta hai
```

Example:

* username/password
* JWT
* OTP
* OAuth2

---

# 6. Internal Flow

```text id="f9y6fc"
Filter
 ↓
AuthenticationManager
 ↓
AuthenticationProvider
 ↓
DB/JWT validation
 ↓
Authenticated object return
```

---

# 7. Most Common Provider

# `DaoAuthenticationProvider`

Ye sabse common provider hai.

Kaam:

* DB se user load karna
* password verify karna

---

# 8. DaoAuthenticationProvider Internal Flow

```text id="0e4mgm"
1. Username receive
2. UserDetailsService call
3. DB se user load
4. PasswordEncoder compare
5. Success Authentication object return
```

---

# 9. Actual Login Flow Step by Step

Suppose:

```json id="mtfujv"
{
  "username":"rishabh",
  "password":"1234"
}
```

---

## Step 1 → Filter Authentication Object Banata

```java id="c4rxy7"
Authentication auth =
    new UsernamePasswordAuthenticationToken(
        username,
        password
    );
```

---

## Step 2 → AuthenticationManager ko deta

```java id="sgg0ku"
authenticationManager.authenticate(auth);
```

---

## Step 3 → AuthenticationProvider Call Hota

```text id="v62dbd"
DaoAuthenticationProvider
```

---

## Step 4 → UserDetailsService Call

```java id="jvlbvk"
loadUserByUsername(username)
```

DB hit hota.

---

## Step 5 → Password Compare

```java id="3o57fk"
passwordEncoder.matches(raw, encoded)
```

---

## Step 6 → Success Authentication Return

```java id="jrt8g4"
authenticated = true
```

Roles bhi attach hoti.

---

# 10. Important Classes

| Class                             | Role      |
| --------------------------------- | --------- |
| AuthenticationProvider            | Interface |
| DaoAuthenticationProvider         | DB auth   |
| JwtAuthenticationProvider         | JWT auth  |
| LdapAuthenticationProvider        | LDAP      |
| OAuth2LoginAuthenticationProvider | OAuth     |

---

# 11. DaoAuthenticationProvider Internals

Internally ye use karta:

```text id="anx2pd"
UserDetailsService
PasswordEncoder
```

---

# 12. UserDetailsService Role

User load karta DB se.

```java id="fjwqpj"
UserDetails loadUserByUsername(String username)
```

---

# 13. PasswordEncoder Role

Password hashing compare karta.

Example:

```java id="uhq3rj"
BCryptPasswordEncoder
```

---

# 14. Complete Internal Diagram

```text id="2q6bqf"
Request
 ↓
UsernamePasswordAuthenticationFilter
 ↓
AuthenticationManager
 ↓
DaoAuthenticationProvider
     ↓
UserDetailsService
     ↓
Database
     ↓
PasswordEncoder
 ↓
Authentication Success
 ↓
SecurityContextHolder
```

---

# 15. supports() Method Deep Understanding

Bahut important interview question.

Example:

```java id="g25r5z"
@Override
public boolean supports(Class<?> authentication) {

    return UsernamePasswordAuthenticationToken
            .class.isAssignableFrom(authentication);
}
```

Meaning:

```text id="3znxg4"
Ye provider username/password auth support karta hai
```

---

# 16. Why supports() Needed?

Because ek application me multiple providers ho sakte.

Example:

```text id="4n5n1y"
Provider 1 → Username/password
Provider 2 → JWT
Provider 3 → OTP
```

Spring decide karta:
kaunsa provider use karna hai.

---

# 17. Multiple AuthenticationProviders

Bahut important architecture concept.

```text id="bg7zqe"
ProviderManager
   ↓
Provider 1
Provider 2
Provider 3
```

ProviderManager:

* ek ek provider try karta
* jo support karega wahi authenticate karega

---

# 18. Custom AuthenticationProvider

Suppose:

* custom OTP login
* employee ID login
* biometric login

Then custom provider banate.

---

# 19. Custom Provider Example

```java id="9m5vuh"
@Component
public class CustomAuthenticationProvider
       implements AuthenticationProvider {

    @Override
    public Authentication authenticate(
            Authentication authentication) {

        String username =
            authentication.getName();

        String password =
            authentication.getCredentials()
                    .toString();

        if(username.equals("rishabh") &&
           password.equals("1234")) {

            return new UsernamePasswordAuthenticationToken(
                    username,
                    password,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        throw new BadCredentialsException(
                "Invalid credentials");
    }

    @Override
    public boolean supports(
            Class<?> authentication) {

        return UsernamePasswordAuthenticationToken
                .class.isAssignableFrom(authentication);
    }
}
```

---

# 20. Important Concept ⚠️

Before authentication:

```text id="w5qx1y"
authenticated = false
```

After authentication:

```text id="8l1cgr"
authenticated = true
```

---

# 21. SecurityContextHolder

After successful auth:

```java id="oz8a5j"
SecurityContextHolder
    .getContext()
    .setAuthentication(auth);
```

Current user globally accessible ho jata.

---

# 22. JWT AuthenticationProvider

JWT systems me kabhi custom provider use hota.

Flow:

```text id="f8pb4l"
JWT Filter
   ↓
JWT Provider
   ↓
Token Validate
   ↓
Authentication object
```

---

# 23. Exception Handling

Agar credentials wrong:

```java id="2kh7cr"
throw new BadCredentialsException(...)
```

Spring automatically:

```http id="uq5mdw"
401 Unauthorized
```

return kar deta.

---

# 24. AuthenticationProvider vs UserDetailsService

| AuthenticationProvider    | UserDetailsService         |
| ------------------------- | -------------------------- |
| Full authentication logic | Sirf user load karta       |
| Password verify karta     | Password verify nahi karta |
| Roles attach karta        | DB se data laata           |

---

# 25. AuthenticationProvider vs AuthenticationManager

| AuthenticationManager           | AuthenticationProvider   |
| ------------------------------- | ------------------------ |
| Delegates auth                  | Actual auth              |
| Manager                         | Worker                   |
| Multiple providers manage karta | Credentials verify karta |

---

# 26. Interview Deep Questions

## Q1. Why AuthenticationProvider needed?

Authentication logic modular banane ke liye.

---

## Q2. Default provider?

```java id="0cw1s5"
DaoAuthenticationProvider
```

---

## Q3. Actual password compare kaun karta?

```java id="gy3p7s"
PasswordEncoder
```

---

## Q4. User DB se kaun laata?

```java id="32b8yu"
UserDetailsService
```

---

## Q5. supports() ka purpose?

Decide karta:

```text id="4l4rvl"
provider kis authentication type ko handle karega
```

---

# 27. Most Important Interview Explanation ⭐

> "AuthenticationProvider is the core component responsible for performing actual authentication in Spring Security. It validates user credentials, loads user details, and returns an authenticated Authentication object. AuthenticationManager delegates authentication requests to one or more AuthenticationProviders."

---

# 28. Full End-to-End Flow

```text id="6yl89d"
Client Login
    ↓
UsernamePasswordAuthenticationFilter
    ↓
AuthenticationManager
    ↓
ProviderManager
    ↓
DaoAuthenticationProvider
    ↓
UserDetailsService
    ↓
Database
    ↓
PasswordEncoder
    ↓
Authenticated Authentication Object
    ↓
SecurityContextHolder
```

---

# 29. Ekdum Short Revision

## AuthenticationProvider

= actual authentication engine

## Main methods

```java id="zj0a2k"
authenticate()
supports()
```

## Default implementation

```java id="4rqzpd"
DaoAuthenticationProvider
```

## Uses

* UserDetailsService
* PasswordEncoder

## Returns

```text id="it3tgd"
authenticated=true
```

Authentication object.
# SecurityContextHolder — Complete Understanding (Beginner → Advanced)

Agar Spring Security ka sabse important runtime concept samajhna hai, to wo hai:

# `SecurityContextHolder`

Simple language me:

> "Ye current logged-in user ki information ko store karta hai."

Matlab:

* current user kaun hai
* uska role kya hai
* authenticated hai ya nahi
* authorities kya hain

sab yahi rakhta hai.

---

# 1. Real Life Analogy 🪪

Imagine office building.

Gate pe entry ke baad:

* tumhe ek ID card milta hai

Ab building me kahi bhi jao:

* tumhari identity available hai

Exactly same:

```text id="6djlwm"
SecurityContextHolder
```

current authenticated user ki identity store karta hai.

---

# 2. Big Picture

```text id="g8u9h0"
Client Login
    ↓
AuthenticationManager
    ↓
AuthenticationProvider
    ↓
Authentication Success
    ↓
SecurityContextHolder
```

---

# 3. Main Purpose

Current request/user ka authentication data globally accessible banana.

---

# 4. Actual Structure

```text id="5l8m4x"
SecurityContextHolder
        ↓
SecurityContext
        ↓
Authentication
        ↓
Principal(User)
```

---

# 5. Hierarchy Deep Understanding

## Level 1 → SecurityContextHolder

Top level container.

---

## Level 2 → SecurityContext

Authentication object hold karta.

---

## Level 3 → Authentication

Current user ki details.

---

## Level 4 → Principal

Actual logged-in user.

---

# 6. Internal Diagram

```text id="5z1p6e"
SecurityContextHolder
    ↓
SecurityContext
    ↓
Authentication
    ↓
Principal(UserDetails)
```

---

# 7. Authentication Object Me Kya Hota Hai?

```text id="t9bjlwm"
- username
- password
- roles
- authorities
- authenticated=true/false
```

---

# 8. Actual Login Flow

Suppose login successful hua.

Spring internally:

```java id="b6xq6r"
Authentication auth =
    new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        authorities
    );
```

Then:

```java id="5jv9c2"
SecurityContextHolder
    .getContext()
    .setAuthentication(auth);
```

---

# 9. Ye Bahut Important Line Hai ⭐

```java id="jlwm1a"
SecurityContextHolder
    .getContext()
    .setAuthentication(auth);
```

Meaning:

```text id="jlwm1b"
Current user authenticated hai
```

---

# 10. Current Logged-in User Kaise Nikale?

```java id="jlwm1c"
Authentication auth =
    SecurityContextHolder
        .getContext()
        .getAuthentication();
```

---

# 11. Username Kaise Nikale?

```java id="jlwm1d"
String username = auth.getName();
```

---

# 12. Roles Kaise Nikale?

```java id="jlwm1e"
Collection<? extends GrantedAuthority>
    authorities = auth.getAuthorities();
```

---

# 13. Full Example

```java id="jlwm1f"
Authentication auth =
    SecurityContextHolder
        .getContext()
        .getAuthentication();

System.out.println(auth.getName());

System.out.println(auth.getAuthorities());

System.out.println(auth.isAuthenticated());
```

---

# 14. Internally Request Flow

```text id="jlwm1g"
Request
 ↓
JWT Filter/Login Filter
 ↓
Authentication success
 ↓
SecurityContextHolder updated
 ↓
Controller
 ↓
Service
 ↓
Current user accessible everywhere
```

---

# 15. Why SecurityContextHolder Needed?

Without this:

* current user ko har method me pass karna padta

Example:

❌ Bad:

```java id="jlwm1h"
getOrders(User user)
```

Instead:

✅ Better:

```java id="jlwm1i"
SecurityContextHolder.getContext()
```

---

# 16. JWT Flow Me Important Role

JWT authentication me:

* server session store nahi karta

Har request pe:

* JWT filter token validate karta
* SecurityContextHolder populate karta

---

# 17. JWT Flow Complete

```text id="jlwm1j"
Request with JWT
       ↓
JWT Filter
       ↓
Token Validate
       ↓
Create Authentication Object
       ↓
SecurityContextHolder.setAuthentication()
       ↓
Controller Access
```

---

# 18. Very Important ⚠️

JWT me:

```text id="jlwm1k"
SecurityContextHolder
```

har request pe dubara populate hota.

Because:

```text id="jlwm1l"
stateless architecture
```

---

# 19. Session-Based Login Me

Session systems me:

* authentication session me save hota
* next request pe restore hota

---

# 20. SecurityContextPersistenceFilter

Ye filter:

* context load karta
* context save karta

---

# 21. ThreadLocal Concept ⭐

Bahut important advanced topic.

Internally:

```java id="jlwm1m"
SecurityContextHolder
```

uses:

# `ThreadLocal`

---

# 22. ThreadLocal Kya Karta?

Har request/thread ka separate security context maintain karta.

Example:

```text id="jlwm1n"
User A request
   ↓
Thread 1
   ↓
Own SecurityContext

User B request
   ↓
Thread 2
   ↓
Own SecurityContext
```

---

# 23. Why ThreadLocal Needed?

Taaki:

* users ka data mix na ho
* thread-safe authentication ho

---

# 24. SecurityContextHolder Modes

3 modes hote:

| Mode                        | Meaning               |
| --------------------------- | --------------------- |
| MODE_THREADLOCAL            | Default               |
| MODE_INHERITABLETHREADLOCAL | Child threads inherit |
| MODE_GLOBAL                 | Global context        |

---

# 25. Default Mode

```java id="jlwm1o"
MODE_THREADLOCAL
```

---

# 26. Anonymous User

Agar login nahi:

Spring anonymous authentication set karta.

```text id="jlwm1p"
anonymousUser
```

---

# 27. Logout Me Kya Hota?

Logout pe:

```java id="jlwm1q"
SecurityContextHolder.clearContext();
```

---

# 28. Ye Bahut Important Hai ⭐

```java id="jlwm1r"
clearContext()
```

Matlab:

```text id="jlwm1s"
current user remove
```

---

# 29. SecurityContextHolder vs Session

| SecurityContextHolder  | Session          |
| ---------------------- | ---------------- |
| Current thread/request | Server storage   |
| Runtime auth data      | Persistent data  |
| ThreadLocal based      | Session ID based |

---

# 30. Accessing User in Controller

```java id="jlwm1t"
@GetMapping("/user")
public String user() {

    Authentication auth =
        SecurityContextHolder
            .getContext()
            .getAuthentication();

    return auth.getName();
}
```

---

# 31. Better Way

Spring automatically inject kar sakta:

```java id="jlwm1u"
@GetMapping("/user")
public String user(Authentication auth) {

    return auth.getName();
}
```

---

# 32. SecurityContextHolder Internals

```text id="jlwm1v"
SecurityContextHolder
        ↓
ThreadLocal
        ↓
SecurityContext
        ↓
Authentication
```

---

# 33. Common Interview Questions

## Q1. Purpose of SecurityContextHolder?

Current authenticated user store karna.

---

## Q2. Internally kispe based hai?

# `ThreadLocal`

---

## Q3. JWT me iska role?

Har request pe authenticated user store karta.

---

## Q4. Logout pe kya hota?

```java id="jlwm1w"
clearContext()
```

---

## Q5. Authentication kaha store hota?

```text id="jlwm1x"
SecurityContext
```

---

# 34. Very Important Interview Explanation ⭐

> "SecurityContextHolder is the central holder in Spring Security that stores the SecurityContext of the currently authenticated user. It uses ThreadLocal internally to maintain security information per request thread, allowing the application to access authentication details globally during request processing."

---

# 35. Complete Internal Architecture

```text id="jlwm1y"
Request
 ↓
Security Filter
 ↓
Authentication Success
 ↓
Authentication Object
 ↓
SecurityContext
 ↓
SecurityContextHolder
 ↓
ThreadLocal
 ↓
Controller/Service Access
```

---

# 36. Ekdum Short Revision

## SecurityContextHolder

= current logged-in user store karta

## Stores

* username
* roles
* authorities
* authentication status

## Internally uses

# `ThreadLocal`

## Main methods

```java id="jlwm1z"
getContext()
setAuthentication()
getAuthentication()
clearContext()
```

## JWT me

Har request pe populate hota.

