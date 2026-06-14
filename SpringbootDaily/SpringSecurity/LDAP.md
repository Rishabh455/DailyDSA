“In our application we use Spring Security integrated with Microsoft Active Directory for centralized authentication. When the user enters username and password on the login page, the request first reaches Spring Security’s `UsernamePasswordAuthenticationFilter`. This filter extracts the credentials and creates a `UsernamePasswordAuthenticationToken`.

Then the filter calls:

```java
authenticationManager.authenticate(auth);
```

The `AuthenticationManager` itself does not validate credentials directly. It delegates the request to `ActiveDirectoryLdapAuthenticationProvider`, which is configured with the company domain and LDAP URL.

Example configuration:

```java
ActiveDirectoryLdapAuthenticationProvider provider =
    new ActiveDirectoryLdapAuthenticationProvider(
        "tcs.com",
        "ldap://adserver.tcs.com:389"
    );
```

The provider internally performs an LDAP bind operation against the Active Directory server using credentials like:

```text
rishabh.kumar@tcs.com
```

and the entered password.

At this stage, the Active Directory server itself validates the password hash, checks whether the account is locked, disabled, or expired, and also fetches user groups or roles.

If authentication succeeds, Spring Security creates an authenticated `Authentication` object containing user details and authorities like `ROLE_ADMIN` or `ROLE_USER`.

Then Spring stores it inside:

```java
SecurityContextHolder
    .getContext()
    .setAuthentication(authentication);
```

After this, the user is considered authenticated for the current request/session, and authorization rules such as:

```java
.hasRole("ADMIN")
```

are applied before allowing access to secured APIs or pages.

In modern enterprise systems, after successful AD authentication, we usually generate a JWT token so that future requests are authenticated using JWT instead of hitting Active Directory on every request.”



1. User enters username/password
2. UsernamePasswordAuthenticationFilter intercepts request
3. Authentication token created
4. AuthenticationManager.authenticate()
5. ActiveDirectoryLdapAuthenticationProvider validates via LDAP
6. AD server checks password + groups
7. SecurityContextHolder stores authenticated user
8. JWT generated for future requests



“In our application, we use Spring Security integrated with Microsoft Active Directory for centralized authentication.

When the user enters username and password, the request first goes to Spring Security’s `UsernamePasswordAuthenticationFilter`. This filter extracts the credentials and creates a `UsernamePasswordAuthenticationToken`.

Then it calls:

```java
authenticationManager.authenticate(authentication)
```

The `AuthenticationManager` delegates the authentication request to `ActiveDirectoryLdapAuthenticationProvider`, which performs LDAP bind authentication against the Active Directory server.

The AD server validates the username and password, checks account status like locked or expired, and also fetches user roles and groups.

If authentication is successful, Spring Security creates an authenticated `Authentication` object and stores it inside:

```java
SecurityContextHolder.getContext()
```

After that, authorization rules like:

```java
.hasRole("ADMIN")
```

or annotations such as:

```java
@PreAuthorize
@Secured
```

are applied to secure APIs and resources.

In modern applications, after successful AD authentication, we generate a JWT token. For future requests, Spring Security validates the JWT using a custom `JwtAuthFilter` instead of hitting Active Directory again, making the application stateless and scalable.”
