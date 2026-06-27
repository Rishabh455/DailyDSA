# JWT Authentication Flow in Spring Security (Easy + Interview Ready)
# JWT (JSON Web Token) - Interview Cheat Sheet

## JWT Structure

A JWT consists of **3 parts**:

```text
Base64(Header).Base64(Payload).Signature
```

---

## JWT Components

| Part          | Stores                                | Example                                         | Purpose                                        | Interview Point                        |
| ------------- | ------------------------------------- | ----------------------------------------------- | ---------------------------------------------- | -------------------------------------- |
| **Header**    | Token metadata                        | `alg: HS256`, `typ: JWT`                        | Specifies the signing algorithm and token type | Does **not** contain user information  |
| **Payload**   | User data (Claims)                    | `sub: rishabh`, `role: ADMIN`, `exp: 123456789` | Carries user identity and claims               | **Not encrypted**, only Base64 encoded |
| **Signature** | Hash of Header + Payload + Secret Key | Generated Hash                                  | Verifies token integrity and authenticity      | Prevents token tampering               |

---

## Signature Generation

```text
Base64(Header)
      +
Base64(Payload)
      +
Secret Key
      ↓
HMACSHA256(...)
      ↓
Signature
```

---

## Important Interview Points

* JWT has **3 parts:** Header, Payload, Signature.
* **Header** contains metadata like signing algorithm (`HS256`) and token type (`JWT`).
* **Payload** contains claims such as user ID, username, role, and expiry time.
* **Payload is NOT encrypted**; it is only Base64 encoded, so anyone can decode it.
* **Never store sensitive information** (passwords, credit card numbers, etc.) in the payload.
* **Signature** is generated using the Header, Payload, and Secret Key.
* During validation, the server regenerates the signature using its Secret Key.
* If the generated signature matches the received signature, the token is valid; otherwise, it has been tampered with.

---

## 30-Second Interview Answer

> "JWT consists of three parts: Header, Payload, and Signature. The Header contains metadata like the signing algorithm and token type. The Payload contains user claims such as user ID, role, and expiry time. The Signature is generated using the Base64-encoded Header, Base64-encoded Payload, and a Secret Key. It is used to verify the token's integrity and authenticity. One important point is that the Payload is **not encrypted**, only Base64 encoded, so sensitive information should never be stored in it."


> “In modern Spring Boot applications, we usually use JWT-based authentication to make the application stateless and scalable.

> First, the user logs in using username and password. The login request goes through Spring Security’s `UsernamePasswordAuthenticationFilter`.

> The credentials are authenticated using `AuthenticationManager`.

```java
authenticationManager.authenticate(auth);
```

> If authentication is successful, we generate a JWT token using a secret key.

> The token usually contains:
>
> * username
> * roles/authorities
> * issued time
> * expiration time

Example:

```java
String token = jwtService.generateToken(username);
```

> The JWT token is then sent back to the frontend.

> For every future request, the frontend sends the token in the Authorization header:

```text
Authorization: Bearer eyJhbGciOiJIUzI1Ni...
```

> In Spring Security, we create a custom filter usually extending:

```java
OncePerRequestFilter
```

> This filter runs before `UsernamePasswordAuthenticationFilter`.

```java
.addFilterBefore(jwtAuthFilter,
                 UsernamePasswordAuthenticationFilter.class)
```

> The JWT filter extracts the token, validates its signature and expiration, and then fetches the username from the token.

> If the token is valid, Spring Security creates an authenticated object:

```java
UsernamePasswordAuthenticationToken
```

> and stores it inside:

```java
SecurityContextHolder.getContext()
                     .setAuthentication(authentication);
```

> After this, the user is considered authenticated, and authorization rules like:

```java
.hasRole("ADMIN")
```

> or annotations like:

```java
@PreAuthorize
@Secured
```

> are applied before accessing APIs.

> Since JWT is stateless, the server does not store session data, which improves scalability and performance.”

---

# SUPER EASY FLOW TO MEMORIZE

```text
1. User logs in
2. AuthenticationManager validates credentials
3. JWT token generated
4. Token sent to frontend
5. Frontend sends token in every request
6. JwtAuthFilter intercepts request
7. Token validated
8. SecurityContextHolder updated
9. User gets access to secured APIs
```

---

# IMPORTANT CLASSES (Must Say in Interview)

## 1. UsernamePasswordAuthenticationFilter

Handles login request.

```java
UsernamePasswordAuthenticationFilter
```

---

## 2. AuthenticationManager

Authenticates credentials.

```java
authenticationManager.authenticate(auth)
```

---

## 3. OncePerRequestFilter

Base class for custom JWT filter.

```java
public class JwtAuthFilter
       extends OncePerRequestFilter
```

---

## 4. SecurityContextHolder

Stores authenticated user.

```java
SecurityContextHolder.getContext()
```

---

## 5. UsernamePasswordAuthenticationToken

Represents authenticated user.

```java
new UsernamePasswordAuthenticationToken(
    userDetails,
    null,
    userDetails.getAuthorities()
)
```

---

# IMPORTANT ANNOTATIONS

## Security Configuration

```java
@EnableWebSecurity
```

---

## Method Level Security

```java
@EnableMethodSecurity
```

---

## Authorization

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

## Secure Specific Roles

```java
@Secured("ROLE_ADMIN")
```

---

# JWT FILTER CODE (Very Important)

```java
@Component
public class JwtAuthFilter
        extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
            request.getHeader("Authorization");

        if(authHeader != null &&
           authHeader.startsWith("Bearer ")) {

            String token =
                authHeader.substring(7);

            String username =
                jwtService.extractUsername(token);

            if(username != null &&
               SecurityContextHolder
                   .getContext()
                   .getAuthentication() == null) {

                UserDetails userDetails =
                    userDetailsService
                        .loadUserByUsername(username);

                if(jwtService.isTokenValid(
                        token,
                        userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

--------------------------------------------
1. Get Authorization Header

2. Check:
   Header exists?
   Starts with "Bearer "?

3. Extract JWT Token

4. Extract Username from Token

5. Check:
   Username exists?
   User not already authenticated?

6. Load User from Database

7. Validate Token

8. If Token Valid:
      Create Authentication Object
      Set Authentication in SecurityContext

9. Continue Filter Chain
----------------------------------------------

---

# Security Config (Most Asked)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**")
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
```
-------------------------------------------------------
@Configuration
→ Marks security configuration class

@EnableWebSecurity
→ Enables Spring Security

@EnableMethodSecurity
→ Enables @PreAuthorize, @PostAuthorize

SecurityFilterChain()
→ Main security configuration method

csrf().disable()
→ Disable CSRF for JWT APIs

sessionCreationPolicy(STATELESS)
→ No server-side session

requestMatchers("/auth/**").permitAll()
→ Login/Register APIs are public

anyRequest().authenticated()
→ All other APIs are secured

addFilterBefore(jwtFilter,...)
→ Validate JWT before authentication filter

http.build()
→ Create Security Filter Chain

-------------------------------------------------------

###
SecurityConfig:
@Configuration
@EnableWebSecurity
@EnableMethodSecurity

SecurityFilterChain:
Disable CSRF
→ Stateless
→ Permit Login APIs
→ Secure Remaining APIs
→ Add JWT Filter
→ Build Chain

JwtAuthFilter:
Get Token
→ Get Username
→ Load User
→ Validate Token
→ Set Authentication
→ Continue Request

###

---

# IMPORTANT INTERVIEW POINTS

## Why JWT?

```text
Stateless authentication
Better scalability
No server-side session storage
Good for microservices and REST APIs
```

---

## What is inside JWT?

```text
Header
Payload
Signature
```

---

## What does Payload contain?

```text
Username
Roles
Expiration time
```

---

# Difference Between Session & JWT

## Session Based

```text
Server stores session
Session ID sent in cookie
Stateful
```

---

## JWT Based

```text
No session storage
Token sent in header
Stateless
Better scalability
```

---

# IMPORTANT LINE FOR INTERVIEW

Memorize this:

> “In JWT authentication, Spring Security does not authenticate the user on every request using username and password. Instead, it validates the JWT token using a custom filter and reconstructs the Authentication object from the token itself.”

---

# MOST IMPRESSIVE POINT

> “We usually place the JWT filter before `UsernamePasswordAuthenticationFilter` in the Spring Security filter chain so that token validation happens before reaching secured APIs.”

---

# SUPER SHORT 20-SECOND VERSION

> “In JWT-based authentication, after successful login Spring Security generates a signed JWT token and sends it to the client. The client sends this token in every request using the Authorization header. A custom filter extending `OncePerRequestFilter` validates the token, extracts user details, and stores authentication inside `SecurityContextHolder`. Since the server does not maintain sessions, the system becomes stateless and scalable.”
हाँ, बिल्कुल ऐसा ही होता है। पहली बार जब यूज़र लॉगिन करता है, तब UsernamePasswordAuthenticationFilter क्रेडेंशियल्स चेक करता है। लेकिन एक बार जब JWT जारी हो जाता है, तब हर आने वाली request में JWTAuthFilter काम करता है। यह Authorization header से Bearer token निकालता है, उसकी वैधता चेक करता है, और उसी के आधार पर Authentication और Authorization देता है। इस तरह, Active Directory या UsernamePassword फ़िल्टर को बार-बार नहीं हिट करना पड़ता।