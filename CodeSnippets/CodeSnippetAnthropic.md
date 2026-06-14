# 3-5 YOE Java/Spring Boot Interview — HIGH PRIORITY Coding Snippets

Ye list **filtered** hai — pehle wali 50 me se sirf wo cherna gaya hai jo 3-5 YOE level pe **white-board pe likhne ko bola jaata hai** ya rapid-fire me directly poocha jaata hai. Har snippet ke saath:
- 🔴 = **MUST** — daily practice karo, likhna aana chahiye bina dekhe
- 🟡 = **HIGH** — concept clear hona chahiye, code likh sako to bonus
- ⚪ = SKIP for daily practice — sirf 1-line definition yaad rakho (niche reasoning diya hai)

---

## SECTION 1 — Functional Programming (3-5 YOE ka bread & butter)

### 1. Predicate / Function / Consumer / Supplier 🔴
```java
Predicate<Integer> isEven = n -> n % 2 == 0;
Function<String, Integer> len = s -> s.length();
Consumer<String> printer = s -> System.out.println(s);
Supplier<String> name = () -> "Rishabh";

System.out.println(isEven.test(4));
System.out.println(len.apply("Java"));
printer.accept("Hello");
System.out.println(name.get());
```
**Why MUST:** Interviewer kehta hai "ek custom Predicate likho jo string ki length 5 se zyada check kare" — ye combo har 2nd interview me poocha jaata hai.

---

### 2. Stream — filter + map + collect 🔴
```java
List<String> names = List.of("rishabh", "java", "spring", "boot");

List<String> result = names.stream()
        .filter(n -> n.length() > 4)
        .map(String::toUpperCase)
        .collect(Collectors.toList());

System.out.println(result); // [RISHABH, SPRING]
```
**Why MUST:** "Given a list, filter even/odd ya length-based, then map and collect" — almost guaranteed coding question.

---

### 3. Stream — groupingBy + counting 🔴
```java
List<String> words = List.of("apple","banana","avocado","blueberry","cherry");

Map<Character, List<String>> grouped = words.stream()
        .collect(Collectors.groupingBy(w -> w.charAt(0)));

System.out.println(grouped);
// {a=[apple, avocado], b=[banana, blueberry], c=[cherry]}
```
**Why MUST:** "Group employees by department", "count words by first letter" — classic 3-5 YOE stream question, easily forgotten under pressure.

---

### 4. Optional — proper usage 🔴
```java
Optional<String> name = Optional.ofNullable(getName());

String result = name
        .map(String::toUpperCase)
        .orElse("DEFAULT");

System.out.println(result);
```
**Why MUST:** Interviewer pucheta hai "NullPointerException kaise avoid karoge" → Optional ka chaining usage dikhana padta hai, sirf `.get()` likhna red flag hai.

---

## SECTION 2 — Concurrency (3-5 YOE pe deeply poocha jaata hai)

### 5. Double-Checked Locking Singleton 🔴
```java
public class Singleton {
    private static volatile Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```
**Why MUST:** "Thread-safe Singleton likho" — top-3 most asked design coding question for 3-5 YOE. `volatile` ka reason bolna bhi zaroori hai (instruction reordering).

---

### 6. ExecutorService + Future 🔴
```java
ExecutorService executor = Executors.newFixedThreadPool(3);

Future<Integer> future = executor.submit(() -> {
    Thread.sleep(1000);
    return 10 + 20;
});

System.out.println(future.get()); // blocks until result ready
executor.shutdown();
```
**Why MUST:** "Submit a task and get result" / "thread pool kaise banate ho" — almost always poocha jaata hai if resume me multithreading/Kafka mentioned hai (jo tumhare resume me hai).

---

### 7. CompletableFuture — chaining + combine 🔴
```java
CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 10);
CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 20);

a.thenCombine(b, Integer::sum)
 .thenAccept(sum -> System.out.println("Total = " + sum));
```
**Why MUST:** Async microservices background ke liye ye favorite topic hai — "two independent API calls ko parallel chalao aur combine karo" type scenario question.

---

### 8. ConcurrentHashMap vs synchronized 🟡
```java
Map<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1);
map.computeIfPresent("a", (k, v) -> v + 1);
```
**Why HIGH:** Code kam likhwaya jaata hai, but "HashMap vs ConcurrentHashMap vs synchronizedMap" difference verbally explain karna padta hai — practice karo bolke.

---

## SECTION 3 — Spring Core (DI, REST, Exceptions)

### 9. Constructor Injection 🔴
```java
@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```
**Why MUST:** "Field injection vs constructor injection — kyun constructor better hai" ka follow-up turant aata hai (immutability, testability, circular dependency detection).

---

### 10. REST Controller — full CRUD endpoint 🔴
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid UserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }
}
```
**Why MUST:** "Likho ek REST endpoint jo user create kare with validation" — ye full snippet likhne ko milta hai, especially `ResponseEntity` aur `@Valid` ka use dikhana important hai.

---

### 11. Global Exception Handling 🔴
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse("Internal error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
**Why MUST:** "Production-grade error handling kaise karte ho" — almost guaranteed for IAM/enterprise background, since clean error response standard expected hai.

---

## SECTION 4 — Database / Spring Data JPA (sabse zyada chances)

### 12. JPA Entity with Relationships 🔴
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
```
**Why MUST:** "@OneToMany vs @ManyToOne, FetchType LAZY vs EAGER, cascade types" — entity relationship likhwana + N+1 problem discussion ek standard combo hai.

---

### 13. JPQL + Native Query 🔴
```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailJPQL(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE created_at > :date", nativeQuery = true)
    List<User> findRecentUsers(@Param("date") LocalDate date);

    // Derived query — Spring auto-generates
    List<User> findByStatusAndDepartment(String status, String department);
}
```
**Why MUST:** Tumhare AD sync experience (630K records) ke context me ye topic deeply poochenge — "complex filter wala query kaise likhoge", JPQL vs native difference bhi.

---

### 14. Pagination + Sorting 🔴
```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

Page<User> usersPage = userRepository.findAll(pageable);

usersPage.getContent().forEach(System.out::println);
System.out.println("Total pages: " + usersPage.getTotalPages());
```
**Why MUST:** "630K users ka data kaise fetch karoge UI me" — direct connection tumhare resume ke saath, pagination implementation poochna almost certain hai.

---

### 15. @Transactional — rollback behavior 🔴
```java
@Service
public class TransferService {

    @Transactional(rollbackFor = InsufficientFundsException.class)
    public void transferAmount(Long fromId, Long toId, double amount) {
        Account from = accountRepo.findById(fromId).orElseThrow();
        Account to = accountRepo.findById(toId).orElseThrow();

        if (from.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        accountRepo.save(from);
        accountRepo.save(to);
    }
}
```
**Why MUST:** "Agar dusra save fail ho jaye to pehla rollback hoga ya nahi" — propagation, isolation levels, aur checked vs unchecked exception rollback ka follow-up guaranteed.

---

### 16. N+1 Problem Fix — JOIN FETCH 🔴
```java
// Problem: N+1 queries fire ho jaate hain lazy loading se
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
User findUserWithOrders(@Param("id") Long id);
```
**Why MUST:** "Lazy loading se N+1 problem aata hai, isko kaise fix karoge" — performance-related question, tumhare AD-sync optimization story se directly connect hota hai. **Bolna mat bhoolo:** AD sync optimization (630K → 3K records via incremental query) yahi concept hai real life me.

---

### 17. Batch Insert/Update (Hibernate) 🟡
```java
@Modifying
@Query("UPDATE User u SET u.status = :status WHERE u.id IN :ids")
int bulkUpdateStatus(@Param("status") String status, @Param("ids") List<Long> ids);
```
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_updates=true
```
**Why HIGH:** Tumhara batch-update story (Hibernate batch) resume me hai — code likhna shायद na bole, lekin config + `@Modifying` ka use samajhna chahiye for follow-up.

---

### 18. SQL — Joins + GroupBy (raw SQL also asked) 🔴
```sql
SELECT d.dept_name, COUNT(e.id) AS emp_count, AVG(e.salary) AS avg_salary
FROM employee e
INNER JOIN department d ON e.dept_id = d.id
WHERE e.status = 'ACTIVE'
GROUP BY d.dept_name
HAVING COUNT(e.id) > 5
ORDER BY avg_salary DESC;
```
**Why MUST:** Java interview ke saath bhi 1-2 raw SQL questions aate hain — INNER vs LEFT JOIN, GROUP BY + HAVING (WHERE vs HAVING difference) bolna chahiye.

---

## SECTION 5 — AOP / Caching / Resilience (microservices-heavy roles ke liye)

### 19. AOP @Around — Logging/Timing 🔴
```java
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.app.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        System.out.println(joinPoint.getSignature() + " executed in " + (end - start) + "ms");
        return result;
    }
}
```
**Why MUST:** "Method execution time kaise log karoge bina har jagah code change kiye" — AOP ka most common practical use-case, likhna padta hai.

---

### 20. @Cacheable + @CacheEvict 🔴
```java
@Cacheable(value = "users", key = "#id")
public User getUserById(Long id) {
    return userRepo.findById(id).orElseThrow();
}

@CacheEvict(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepo.save(user);
}
```
**Why MUST:** "Caching kaise implement karoge aur stale data avoid kaise karoge" — direct follow-up cache invalidation strategy pe jaata hai.

---

### 21. Circuit Breaker + Fallback (Resilience4j) 🔴
```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
public String processPayment(String orderId) {
    return restTemplate.getForObject("http://payment-service/pay/" + orderId, String.class);
}

public String fallbackPayment(String orderId, Throwable ex) {
    return "Payment service unavailable, please try later";
}
```
**Why MUST:** Tumhare Spring Cloud Gateway + Resilience4j experience ke saath ye direct match karta hai — fallback method signature (Throwable param) likhna important detail hai jo log bhool jaate hain.

---

## ⚪ SKIP — Sirf 1-line yaad rakho, code likhna NAHI poocha jaata (3-5 YOE level)

| Topic | 1-line jo bolna kaafi hai |
|---|---|
| **Observer Pattern** | "Publisher-subscriber model — Kafka isi pattern ka real-world example hai" |
| **Factory Pattern (switch-based)** | "Object creation logic ko centralize karta hai, client ko implementation se decouple karta hai" |
| **Strategy Pattern + Lambda** | "Runtime pe algorithm switch karne ke liye — lambda se interface implement karna common ho gaya hai" |
| **Bulkhead / TimeLimiter (Resilience4j)** | "Bulkhead = concurrent calls limit karta hai; TimeLimiter = slow calls ko timeout karta hai — CircuitBreaker ke siblings hain" |
| **Method References (`::`)** | "Lambda ka shorthand jab existing method ko call karna ho" |
| **Record (Java 16+)** | "Immutable DTO ke liye boilerplate-free class — getters, equals, hashCode auto-generate hote hain" |
| **Rate Limiting (Bucket4j)** | "Token bucket algorithm — API abuse rokne ke liye, rarely coded live" |
| **ReentrantLock** | "synchronized ka flexible alternative — tryLock(), fairness option deta hai" |

---

## Daily Practice Plan (suggestion)

- **Day 1-2:** Section 1 (Streams + Predicate/Function family) — likhna fluent hona chahiye
- **Day 3:** Section 2 (Singleton + ExecutorService + CompletableFuture)
- **Day 4:** Section 3 (DI + REST Controller + Exception Handling)
- **Day 5-6:** Section 4 (JPA + N+1 + Transactional + SQL) — **ye section sabse zyada weight rakhta hai tumhare IAM background ke saath**
- **Day 7:** Section 5 (AOP + Caching + Circuit Breaker) + revise SKIP table verbally

Goal: har snippet **5 minute me bina dekhe** likh pao, aur uske saath 1-2 follow-up questions ka answer bhi ready ho.
