SESSION MANAGEMENT — INTERVIEW REVISION NOTES

1. What is Session Management?

Session management is used to maintain user state across multiple HTTP requests.

Example:

* User logs in
* Server creates session
* Session ID stored in browser cookie
* Browser sends session ID in every request
* Server identifies the user

---

2. Basic Flow

Client Login
↓
Server Creates Session
↓
Session ID Generated
↓
JSESSIONID stored in browser cookie
↓
Browser sends JSESSIONID in every request
↓
Server validates session

---

3. Default Session Handling in Spring Boot

Spring Boot uses:

* HttpSession

Example:

HttpSession session;

---

4. Store Data in Session

session.setAttribute("username", "Rishabh");

---

5. Read Session Data

String user = (String) session.getAttribute("username");

---

6. Logout / Invalidate Session

session.invalidate();

---

7. Session Timeout

application.yml

server:
servlet:
session:
timeout: 30m

---

8. Default Session Cookie

JSESSIONID

---

9. Types of Session Management

* In-memory session
* Database session
* Redis session
* JWT token-based authentication

---

10. Problem in Microservices

In-memory sessions fail because:

* requests may hit different servers
* sessions are not shared

Example:

Request 1 → Server A
Request 2 → Server B

Server B does not know session created in Server A.

---

11. Solution in Microservices

Use:

* Redis shared sessions
  OR
* JWT stateless authentication

---

12. Spring Session + Redis

Dependency:

spring-session-data-redis

Redis stores sessions centrally so all microservice instances can access them.

---

13. JWT vs Session

SESSION:

* Stateful
* Server stores session
* Good for monoliths

JWT:

* Stateless
* Client stores token
* Good for microservices

---

14. Security Points (VERY IMPORTANT)

Remember these keywords:

* HTTPS
* Secure cookies
* HttpOnly cookies
* Session timeout
* Session fixation protection

---

15. MOST IMPORTANT INTERVIEW ANSWER

Session management in Spring Boot is used to maintain user state across multiple HTTP requests.

Spring Boot uses HttpSession by default and stores a session ID called JSESSIONID in browser cookies.

The browser sends this session ID with every request, and the server uses it to identify the user session.

In monolithic applications, in-memory sessions are commonly used, while in microservices architectures we usually prefer Redis-based shared sessions or JWT-based stateless authentication.
