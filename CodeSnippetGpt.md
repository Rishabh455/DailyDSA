Ye wahi snippets hain jo 3-5 YOE Java/Spring Boot interviews me whiteboard ya in-person discussion me frequently kaam aate hain.

# 1. Functional Interface

```java
@FunctionalInterface
interface Calculator {
    int add(int a, int b);
}
```

Lambda:

```java
Calculator c = (a,b) -> a+b;
System.out.println(c.add(10,20));
```

**Interview:** Functional Interface = Exactly 1 abstract method.

---

# 2. Predicate

Input → boolean

```java
Predicate<Integer> isEven = n -> n%2==0;

System.out.println(isEven.test(4));
```

---

# 3. Function

Input → Output

```java
Function<String,Integer> length =
        s -> s.length();

System.out.println(length.apply("Java"));
```

---

# 4. Consumer

Input → No Return

```java
Consumer<String> print =
        s -> System.out.println(s);

print.accept("Hello");
```

---

# 5. Supplier

No Input → Output

```java
Supplier<String> name =
        () -> "Rishabh";

System.out.println(name.get());
```

---

# 6. Method Reference

```java
List<String> names = List.of("A","B","C");

names.forEach(System.out::println);
```

---

# 7. Stream Filter

```java
List<Integer> nums = List.of(1,2,3,4);

nums.stream()
    .filter(n -> n%2==0)
    .forEach(System.out::println);
```

---

# 8. Stream Map

```java
List<String> names =
        List.of("java","spring");

names.stream()
     .map(String::toUpperCase)
     .forEach(System.out::println);
```

---

# 9. Optional

```java
Optional<String> name =
        Optional.ofNullable(null);

System.out.println(
        name.orElse("Default")
);
```

---

# 10. Singleton (Thread Safe)

```java
public class Singleton {

    private Singleton(){}

    private static final Singleton obj =
            new Singleton();

    public static Singleton getInstance() {
        return obj;
    }
}
```

---

# 11. Lazy Singleton

```java
public static synchronized Singleton getInstance() {

    if(obj==null)
        obj = new Singleton();

    return obj;
}
```

---

# 12. Double Checked Locking

```java
if(obj==null){
    synchronized(Singleton.class){
        if(obj==null){
            obj = new Singleton();
        }
    }
}
```

**Interview:** `volatile` required.

---

# 13. ExecutorService

```java
ExecutorService ex =
        Executors.newFixedThreadPool(3);

ex.submit(() ->
        System.out.println(
                Thread.currentThread().getName()
        ));

ex.shutdown();
```

---

# 14. Callable

Returns value.

```java
Callable<Integer> task =
        () -> 10+20;

Future<Integer> result =
        executor.submit(task);

System.out.println(result.get());
```

---

# 15. CompletableFuture

```java
CompletableFuture
        .supplyAsync(() -> "Java")
        .thenApply(String::toUpperCase)
        .thenAccept(System.out::println);
```

---

# 16. Combine Futures

```java
CompletableFuture<Integer> a =
        CompletableFuture.completedFuture(10);

CompletableFuture<Integer> b =
        CompletableFuture.completedFuture(20);

a.thenCombine(b,Integer::sum)
 .thenAccept(System.out::println);
```

---

# 17. Exceptionally

```java
CompletableFuture
        .supplyAsync(() -> 10/0)
        .exceptionally(ex -> 0)
        .thenAccept(System.out::println);
```

---

# 18. Spring Bean

```java
@Component
public class UserService {
}
```

---

# 19. Constructor Injection

```java
@Service
public class UserService {

    private final Repo repo;

    public UserService(Repo repo){
        this.repo=repo;
    }
}
```

**Interview:** Preferred over field injection.

---

# 20. @Value

```java
@Value("${app.name}")
private String appName;
```

---

# 21. REST API

```java
@GetMapping("/users/{id}")
public User getUser(
        @PathVariable Long id){

    return service.get(id);
}
```

---

# 22. JPA Query Method

```java
User findByEmail(String email);
```

Spring generates query automatically.

---

# 23. JPQL

```java
@Query("""
select u
from User u
where u.email=:email
""")
```

---

# 24. Pagination

```java
Pageable pageable =
        PageRequest.of(0,10);

userRepo.findAll(pageable);
```

---

# 25. Transaction

```java
@Transactional
public void transfer(){
}
```

Rollback on exception.

---

# 26. Global Exception Handling

```java
@RestControllerAdvice
public class Handler {

    @ExceptionHandler(Exception.class)
    public String handle(){
        return "Error";
    }
}
```

---

# 27. Custom Exception

```java
throw new ResourceNotFoundException(
        "User Not Found"
);
```

---

# 28. Security Endpoint Protection

```java
http.authorizeHttpRequests(auth ->
        auth
                .requestMatchers("/public/**")
                .permitAll()
                .anyRequest()
                .authenticated()
);
```

---

# 29. Password Encoder

```java
@Bean
PasswordEncoder encoder(){
    return new BCryptPasswordEncoder();
}
```

---

# 30. UserDetailsService

```java
@Override
public UserDetails
loadUserByUsername(String username){
}
```

---

# 31. Security Context

Current user:

```java
Authentication auth =
        SecurityContextHolder
                .getContext()
                .getAuthentication();
```

---

# 32. AOP Before Advice

```java
@Before(
"execution(* com.app.service.*.*(..))"
)
```

Runs before method.

---

# 33. AOP Around

```java
@Around(
"execution(* com.app.service.*.*(..))"
)
```

Most asked.

```java
long start = System.currentTimeMillis();

Object result = joinPoint.proceed();

long end = System.currentTimeMillis();
```

Used for logging.

---

# 34. Caching

Enable:

```java
@EnableCaching
```

---

# 35. @Cacheable

```java
@Cacheable("users")
public User getUser(Long id){
}
```

First call DB.

Second call Cache.

---

# 36. Cache Evict

```java
@CacheEvict(
value="users",
key="#id"
)
```

Cache remove.

---

# 37. Rate Limiting (Bucket4j)

```java
Bucket bucket =
        Bucket.builder()
        .addLimit(
                Bandwidth.simple(
                        5,
                        Duration.ofMinutes(1)
                )
        )
        .build();
```

5 requests/min.

---

# 38. Circuit Breaker

```java
@CircuitBreaker(
name="paymentService",
fallbackMethod="fallback"
)
```

---

Fallback:

```java
public String fallback(
        Exception ex){

    return "Service Down";
}
```

---

# 39. Retry

```java
@Retry(
name="payment",
fallbackMethod="fallback"
)
```

---

# 40. Bulkhead

```java
@Bulkhead(
name="payment"
)
```

Limits concurrent calls.

---

# 41. Resilience4j TimeLimiter

```java
@TimeLimiter(name="payment")
```

Stops long-running calls.

---

# 42. Synchronization

```java
public synchronized void print(){
}
```

One thread at a time.

---

# 43. ReentrantLock

```java
lock.lock();

try{

}
finally{
    lock.unlock();
}
```

---

# 44. Volatile

```java
private volatile boolean running;
```

Visibility guarantee.

---

# 45. ConcurrentHashMap

```java
ConcurrentHashMap<
        String,
        Integer> map =
        new ConcurrentHashMap<>();
```

Thread-safe HashMap.

---

# 46. Immutable Class

```java
final class Employee {

    private final String name;

    public Employee(String name){
        this.name=name;
    }
}
```

---

# 47. Record (Java 16+)

```java
record User(
        Long id,
        String name
){}
```

Auto getter/constructor.

---

# 48. Strategy Pattern + Lambda

```java
interface Payment{
    void pay();
}
```

```java
Payment p =
        () -> System.out.println("UPI");
```

---

# 49. Factory Pattern

```java
return switch(type){

    case "CAR" -> new Car();

    default -> throw new RuntimeException();
};
```

---

# 50. Observer Pattern

```java
publisher.subscribe(user);

publisher.notifyAllUsers();
```

---

### Sabse Important Interview Snippets (Top 10)

1. `CompletableFuture`
2. `ExecutorService`
3. `@Transactional`
4. `@Cacheable`
5. `@CircuitBreaker`
6. `@RestControllerAdvice`
7. `SecurityFilterChain`
8. `UserDetailsService`
9. `JpaRepository + Pageable`
10. `Predicate / Function / Consumer / Supplier`

Agar ye 50 snippets aur unke 1-line explanations yaad hain, to 80% Java + Spring Boot in-person interviews ke rapid-fire round comfortably handle ho jaate hain.
