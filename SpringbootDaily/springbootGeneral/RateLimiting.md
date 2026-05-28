psedu code for rate limiting

START

Create MAX_REQUESTS = 5
Create TIME_WINDOW = 1 minute

Create Map<IP, RequestInfo> concurrent hashmap

For every incoming request:

    Get client IP

    If IP not present in map:
        Create new RequestInfo
        count = 0
        startTime = current time

    Get RequestInfo of that IP

    Get current time

    If current time - startTime > 1 minute:
        Reset count = 0
        Reset startTime = current time

    Increment count

    If count > MAX_REQUESTS:
        Return HTTP 429 (Too Many Requests)
        Stop request

    Else:
        Allow request to proceed

END

-----------------------------------------------------------------

oroginal code

@Component
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW = 60 * 1000; // 1 minute

    private final Map<String, RequestInfo> requestMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = httpRequest.getRemoteAddr();

        RequestInfo info = requestMap.getOrDefault(clientIp,
                new RequestInfo(0, System.currentTimeMillis()));

        long currentTime = System.currentTimeMillis();

        // Reset after 1 minute
        if (currentTime - info.startTime > TIME_WINDOW) {
            info.count = 0;
            info.startTime = currentTime;
        }

        info.count++;

        requestMap.put(clientIp, info);

        if (info.count > MAX_REQUESTS) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Too many requests");
            return;
        }

        chain.doFilter(request, response);
    }

    static class RequestInfo {
        int count;
        long startTime;

        RequestInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}
----
SpringBootRateLimitingApplication.java:


package com.gfg.springbootratelimiting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootRateLimitingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRateLimitingApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitingFilter());
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
------
got thorugh the link : https://www.geeksforgeeks.org/advance-java/implementing-rate-limiting-in-a-spring-boot-application/
-----------------------------------------------
Bucket4j internally uses the Token Bucket algorithm, which is efficient for handling burst traffic.

The flow would be:

Intercept incoming requests using Filter or Interceptor
Identify client using IP, user ID, or API key
Maintain a token bucket per client
Consume one token for each request
If tokens are available → allow request
If bucket is empty → return HTTP 429 Too Many Requests
-------------------------------------------

Rate limiting is used to control how many requests a client can make in a specific time window to prevent server overload, API abuse, and brute-force attacks.

Basic flow:
Client Request → Identify IP/User → Check Request Count → If limit exceeded return HTTP 429 else allow request.

Common algorithms:

* Fixed Window → simple counter per minute
* Sliding Window → rolling time window
* Token Bucket → tokens refill continuously, best for burst traffic
* Leaky Bucket → processes requests at constant rate

In monolithic applications:

* we can use ConcurrentHashMap with Filter/Interceptor
* for production-grade monoliths, Bucket4j is preferred because it is thread-safe and production-ready

In microservices:

* in-memory maps do not work properly because multiple instances have separate counters
* so we use Redis or API Gateway-based rate limiting

Redis flow:
Request → check counter in Redis → increment counter → if limit exceeded return 429 else allow request.

API Gateway flow:
Client → API Gateway → Microservices

Gateway centrally handles:

* rate limiting
* authentication
* logging
* routing

Common tools:

* Redis
* Bucket4j
* Spring Cloud Gateway

Token Bucket is most commonly used in production because it handles burst traffic efficiently.




