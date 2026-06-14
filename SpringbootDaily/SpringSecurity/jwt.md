# JWT Authentication Flow in Spring Security (Easy + Interview Ready)
JWT consists of three parts: Header, Payload, and Signature. Header contains algorithm details, payload contains claims/user data, and signature is used to verify integrity and authenticity of the token.
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