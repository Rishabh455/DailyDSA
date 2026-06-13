### Interview Answer

Pagination is used to fetch data in **small chunks (pages)** instead of loading all records at once, improving performance and reducing memory usage.

---

### 1. Repository Layer

Spring Data JPA provides `PagingAndSortingRepository` functionality through `JpaRepository`.

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

---

### 2. Service Layer

```java
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
}
```

---

### 3. Controller Layer

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Page<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return userService.getUsers(page, size);
    }
}
```

---

### API Call

```http
GET /users?page=0&size=5
```

---

### Response

```json
{
  "content": [
    {
      "id": 1,
      "name": "John"
    }
  ],
  "totalElements": 100,
  "totalPages": 20,
  "size": 5,
  "number": 0
}
```

---

### Pagination with Sorting

```java
Pageable pageable =
        PageRequest.of(page, size, Sort.by("name").ascending());
```

or

```java
Pageable pageable =
        PageRequest.of(page, size,
                Sort.by("salary").descending());
```

---

### Custom Query Pagination

```java
Page<User> findByDepartment(
        String department,
        Pageable pageable);
```

Usage:

```java
userRepository.findByDepartment(
        "IT",
        PageRequest.of(0, 10));
```

---

### Internal SQL Generated

For MySQL:

```sql
SELECT *
FROM users
LIMIT 10 OFFSET 0;
```

For:

```java
PageRequest.of(2, 10)
```

Spring generates:

```sql
LIMIT 10 OFFSET 20;
```

---

### 30-Second Interview Answer

> In Spring Boot, pagination is implemented using Spring Data JPA's `Pageable` and `Page` interfaces. We create a `PageRequest` with page number and size, pass it to repository methods like `findAll(Pageable)`, and Spring automatically generates SQL using `LIMIT` and `OFFSET`. Pagination can also be combined with sorting using the `Sort` class.
