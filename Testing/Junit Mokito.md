# JUnit + Mockito Interview Questions (3 Years Java/Spring Boot Experience)

## 1. What is JUnit?

JUnit is a Java testing framework used for writing and executing unit test cases.
It helps verify individual components or methods independently.

Main benefits:

* Automated testing
* Regression testing
* Improves code quality
* Supports CI/CD pipelines

---

# JUnit 5 Important Questions

---

## 2. Difference between JUnit 4 and JUnit 5?

| JUnit 4                  | JUnit 5                        |
| ------------------------ | ------------------------------ |
| Uses `@Before`           | Uses `@BeforeEach`             |
| Uses `@After`            | Uses `@AfterEach`              |
| Limited extensibility    | Modular and extensible         |
| Old architecture         | Jupiter engine                 |
| Less flexible assertions | Better assertions and features |

---

## 3. Important JUnit Annotations

| Annotation           | Purpose                            |
| -------------------- | ---------------------------------- |
| `@Test`              | Marks test method                  |
| `@BeforeEach`        | Runs before every test             |
| `@AfterEach`         | Runs after every test              |
| `@BeforeAll`         | Runs once before all tests         |
| `@AfterAll`          | Runs once after all tests          |
| `@DisplayName`       | Custom test name                   |
| `@ParameterizedTest` | Run same test with multiple inputs |

---

## 4. Difference between Unit Testing and Integration Testing?

### Unit Testing

* Tests individual class/method
* Uses mocks
* Fast
* No DB/server required

Example:
Testing service layer using mocked repository.

### Integration Testing

* Tests interaction between components
* Uses actual Spring context/database
* Slower

Example:
Testing controller + service + repository together.

---

# Mockito Important Questions

---

## 5. What is Mockito?

Mockito is a mocking framework used with JUnit to create dummy/mock objects for dependencies.

Used when:

* External dependency exists
* DB/API calls should be avoided
* Isolated testing is needed

---

## 6. What is Mocking?

Mocking means creating a fake object for dependencies.

Example:
Instead of calling actual database repository,
we mock repository response.

---

## 7. Difference between Mock and Spy?

| Mock                    | Spy                        |
| ----------------------- | -------------------------- |
| Fully fake object       | Partial real object        |
| Real methods not called | Real methods can be called |
| Used for isolation      | Used for partial mocking   |

---

## 8. Common Mockito Annotations

| Annotation     | Purpose                  |
| -------------- | ------------------------ |
| `@Mock`        | Creates mock object      |
| `@InjectMocks` | Injects mocks into class |
| `@Spy`         | Partial mocking          |
| `@Captor`      | Captures arguments       |

---

## 9. What is `when().thenReturn()`?

Used to define mock behavior.

```java
when(userRepo.findById(1L)).thenReturn(user);
```

Meaning:
When method is called, return dummy response.

---

## 10. What is `verify()` in Mockito?

Used to verify whether a method was called.

```java
verify(userRepo, times(1)).save(user);
```

---

## 11. Difference between `@MockBean` and `@Mock`?

| `@Mock`                     | `@MockBean`                           |
| --------------------------- | ------------------------------------- |
| Mockito annotation          | Spring Boot annotation                |
| Used in unit tests          | Used in Spring Boot integration tests |
| Doesn't load Spring context | Replaces bean in Spring context       |

---

## 12. What is Stubbing?

Defining expected behavior of mocked method.

Example:

```java
when(service.getUser()).thenReturn(user);
```

---

## 13. What happens if we don't mock dependencies?

Actual DB/API/service calls may happen:

* Slow tests
* Unstable tests
* External dependency failures

---

# Spring Boot Testing Questions

---

## 14. Difference between `@WebMvcTest` and `@SpringBootTest`?

| `@WebMvcTest`         | `@SpringBootTest`        |
| --------------------- | ------------------------ |
| Tests controller only | Loads full application   |
| Faster                | Slower                   |
| Uses mocked services  | Full integration testing |

---

## 15. What is MockMvc?

Used to test REST APIs without starting actual server.

Example:

* Test status code
* Request/response
* JSON validation

---

## 16. How do you test Exception Handling?

Using:

* `assertThrows()`
* MockMvc for REST exceptions

Example:

```java
assertThrows(UserNotFoundException.class, () -> {
    service.getUser(1L);
});
```

---

# Advanced Mockito Questions

---

## 17. Difference between `thenReturn()` and `thenAnswer()`?

### thenReturn()

Returns fixed value.

### thenAnswer()

Dynamic/custom logic.

Example:

```java
when(repo.save(any()))
.thenAnswer(invocation -> invocation.getArgument(0));
```

---

## 18. What is ArgumentCaptor?

Used to capture method arguments passed to mocks.

```java
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

verify(repo).save(captor.capture());

assertEquals("Rishabh", captor.getValue().getName());
```

---

## 19. What is `doReturn()` vs `when()`?

`doReturn()` mainly used with spies to avoid calling real methods.

---

## 20. What are common issues faced during unit testing?

* NullPointerException due to missing mocks
* Wrong stubbing
* Over-mocking
* Tight coupling
* Static methods difficult to mock

---

# Project-Based Interview Questions (VERY IMPORTANT)

Since your project involved:

* Password management application
* User onboarding
* Spring Boot
* Security
* CI/CD
* Katalon automation
* Production issue handling

These questions are highly relevant.

---

# 21. How did you use JUnit and Mockito in your project?

### Answer

"In our password management and onboarding application, I used JUnit 5 and Mockito for unit testing service layer logic. We mocked repository and external service dependencies using Mockito so that actual database calls were avoided. I mainly tested validation logic, password reset flow, onboarding APIs, and exception handling scenarios. These tests were integrated into our CI/CD pipeline and executed during build stages before deployment."

---

# 22. What exactly did you test?

### Answer

"I tested:

* User onboarding validation
* Password policy validation
* OTP verification flow
* Repository interaction
* Exception scenarios
* API response validation
* Service layer business logic"

---

# 23. Did you write controller tests?

### Answer

"Yes, for some APIs we used MockMvc to test REST endpoints. We validated:

* HTTP status codes
* Request/response payloads
* Exception responses
* Authentication-related scenarios"

---

# 24. How did testing help in CI/CD?

### Answer

"Our JUnit and Mockito test cases were executed automatically in CI/CD pipelines during build stages. This helped catch regression issues early before deployment to SIT/UAT environments."

---

# 25. Have you faced flaky tests?

### Answer

"Yes, mainly due to dependency-related issues and improper mock setup. We stabilized tests by isolating external dependencies properly and avoiding unnecessary shared test data."

---

# 26. How do you test service layer?

Example:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Test
    void testGetUser() {

        User user = new User(1L, "Rishabh");

        when(repo.findById(1L))
                .thenReturn(Optional.of(user));

        User result = service.getUser(1L);

        assertEquals("Rishabh", result.getName());

        verify(repo).findById(1L);
    }
}
```

---

# 27. How do you test exceptions?

```java
@Test
void shouldThrowException() {

    when(repo.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
        service.getUser(1L);
    });
}
```

---

# 28. Why is Mockito important in microservices?

### Answer

"Mockito helps isolate service dependencies during unit testing. In microservices architecture, services often depend on databases, external APIs, Kafka, or other services. Mocking prevents real network/database calls and keeps tests fast and reliable."

---

# 29. Difference between `any()` and `eq()`?

| Method  | Purpose           |
| ------- | ----------------- |
| `any()` | Accept any value  |
| `eq()`  | Match exact value |

Example:

```java
when(repo.findByName(any())).thenReturn(user);

when(repo.findByName(eq("Rishabh"))).thenReturn(user);
```

---

# 30. Best Practices for Unit Testing

* One behavior per test
* Clear naming convention
* Avoid unnecessary mocks
* Keep tests independent
* Use meaningful assertions
* Cover positive + negative scenarios

---

# MOST IMPORTANT PRACTICAL INTERVIEW QUESTION

## 31. Explain one real testing scenario from your project.

### Strong Answer

"In our onboarding application, we had a password validation module where users had to follow strict password policies. I wrote unit tests using JUnit and Mockito to validate scenarios like invalid passwords, expired OTPs, successful onboarding, and repository failures. Repository calls were mocked using Mockito, and these tests helped catch multiple regression issues during deployments."

---

# Frequently Asked Rapid Fire Questions

| Question               | Short Answer                  |
| ---------------------- | ----------------------------- |
| What is unit testing?  | Testing individual components |
| Why Mockito?           | To mock dependencies          |
| What is stubbing?      | Defining mock behavior        |
| What is verify?        | Checks method invocation      |
| What is spy?           | Partial mock                  |
| What is MockMvc?       | REST API testing utility      |
| What is assertion?     | Validation in test            |
| Why CI/CD uses tests?  | Prevent regression issues     |
| What is code coverage? | Percentage of tested code     |

---

# IMPORTANT TOPICS TO PREPARE

Must know:

* JUnit lifecycle
* Mockito annotations
* MockMvc
* Exception testing
* Service layer testing
* Repository mocking
* CI/CD integration
* Assertions
* `verify()`
* `when().thenReturn()`
* `assertThrows()`
* Spring Boot testing annotations

These are the most commonly asked topics for 3-year Java/Spring Boot interviews.
----------------------------------------------------------------------------------------------------

# 1. JUnit Lifecycle

JUnit lifecycle defines what runs before and after test execution.

## Important Annotations

| Annotation    | Purpose                    |
| ------------- | -------------------------- |
| `@BeforeEach` | Runs before every test     |
| `@AfterEach`  | Runs after every test      |
| `@BeforeAll`  | Runs once before all tests |
| `@AfterAll`   | Runs once after all tests  |
| `@Test`       | Marks test method          |

---

## Interview Answer

"JUnit lifecycle annotations help initialize and clean test data before and after test execution. `@BeforeEach` is commonly used for setup, while `@AfterEach` is used for cleanup."

---

## Example

```java id="zhgcv8"
class UserServiceTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("Runs once");
    }

    @BeforeEach
    void setup() {
        System.out.println("Runs before every test");
    }

    @Test
    void test1() {
    }

    @AfterEach
    void cleanup() {
        System.out.println("Runs after every test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Runs once after all tests");
    }
}
```

---

# 2. Mockito Annotations

## `@Mock`

Creates fake dependency object.

```java id="ovwvyc"
@Mock
UserRepository repo;
```

---

## `@InjectMocks`

Injects mocked dependencies into actual class.

```java id="9e3b8x"
@InjectMocks
UserService service;
```

---

## `@Spy`

Partial mocking.

Real methods can execute.

```java id="qzh2ov"
@Spy
List<String> list = new ArrayList<>();
```

---

## `@Captor`

Captures method arguments.

```java id="wb7hsv"
@Captor
ArgumentCaptor<User> captor;
```

---

## Interview Answer

"Mockito annotations reduce boilerplate code and help create mock dependencies easily for isolated unit testing."

---

# 3. MockMvc

MockMvc is used to test REST APIs without starting actual server.

---

## Why Used?

* Test controllers
* Validate JSON response
* Check status codes
* Test exception responses

---

## Example

```java id="k9gh0q"
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService service;

    @Test
    void testGetUser() throws Exception {

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }
}
```

---

## Interview Answer

"MockMvc helps test Spring Boot REST controllers without deploying application on server."

---

# 4. Exception Testing

Used to verify expected exceptions.

---

## `assertThrows()`

```java id="n5rfkl"
@Test
void shouldThrowException() {

    assertThrows(RuntimeException.class, () -> {
        throw new RuntimeException();
    });
}
```

---

## Real Project Example

```java id="s3e57l"
assertThrows(UserNotFoundException.class, () -> {
    service.getUser(1L);
});
```

---

## Interview Answer

"We use `assertThrows()` to validate negative scenarios and exception handling logic."

---

# 5. Service Layer Testing

Mainly tests business logic.

Dependencies are mocked.

---

## Example

```java id="c9p2m5"
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Test
    void testUser() {

        User user = new User(1L, "Rishabh");

        when(repo.findById(1L))
                .thenReturn(Optional.of(user));

        User result = service.getUser(1L);

        assertEquals("Rishabh", result.getName());
    }
}
```

---

## Interview Answer

"In service layer testing, repository and external dependencies are mocked to test only business logic."

---

# 6. Repository Mocking

Instead of actual DB calls, repository responses are mocked.

---

## Example

```java id="9ik3ku"
when(repo.save(any(User.class)))
        .thenReturn(user);
```

---

## Why Important?

* Faster tests
* No DB dependency
* Isolated testing

---

## Interview Answer

"We mock repositories to avoid actual database interaction and keep unit tests fast and independent."

---

# 7. CI/CD Integration

JUnit tests run automatically in pipelines.

---

## Flow

Developer Code → Build → Unit Tests → Sonar → Deploy

---

## Interview Answer

"In our CI/CD pipeline, JUnit and Mockito test cases executed during build stages. This helped catch regression issues before SIT/UAT deployment."

---

# 8. Assertions

Assertions validate expected output.

---

## Common Assertions

| Assertion         | Purpose            |
| ----------------- | ------------------ |
| `assertEquals()`  | Compare values     |
| `assertNotNull()` | Check not null     |
| `assertTrue()`    | Check condition    |
| `assertThrows()`  | Validate exception |

---

## Example

```java id="sylhkn"
assertEquals("Rishabh", user.getName());

assertNotNull(user);

assertTrue(user.isActive());
```

---

## Interview Answer

"Assertions are used to validate actual vs expected results during testing."

---

# 9. `verify()`

Checks whether method was called.

---

## Example

```java id="z4g8ol"
verify(repo).save(user);
```

---

## With Times

```java id="08rdn8"
verify(repo, times(1)).save(user);
```

---

## Why Used?

* Verify interaction
* Ensure flow correctness

---

## Interview Answer

"`verify()` validates whether mocked methods were invoked correctly."

---

# 10. `when().thenReturn()`

Defines mock behavior.

---

## Example

```java id="qu9b8t"
when(repo.findById(1L))
        .thenReturn(Optional.of(user));
```

---

## Meaning

"When this method is called, return this value."

---

## Interview Answer

"`when().thenReturn()` is used for stubbing mocked methods with predefined responses."

---

# 11. `assertThrows()`

Tests exception scenarios.

---

## Example

```java id="3t6hlw"
assertThrows(IllegalArgumentException.class, () -> {
    service.validate(null);
});
```

---

## Why Important?

Validates negative scenarios.

---

## Interview Answer

"`assertThrows()` ensures application throws expected exception for invalid scenarios."

---

# 12. Spring Boot Testing Annotations

---

## `@SpringBootTest`

Loads complete application context.

### Used For

* Integration testing

```java id="xqjlwm"
@SpringBootTest
class AppTest {
}
```

---

## `@WebMvcTest`

Loads only controller layer.

```java id="3u4d8o"
@WebMvcTest(UserController.class)
```

---

## `@MockBean`

Creates mock inside Spring context.

```java id="n52vbm"
@MockBean
UserService service;
```

---

## `@ExtendWith(MockitoExtension.class)`

Enables Mockito in JUnit 5.

```java id="8ks2jr"
@ExtendWith(MockitoExtension.class)
```

---

## Interview Answer

"`@SpringBootTest` is used for integration testing, while `@WebMvcTest` is mainly for controller testing. `@MockBean` replaces Spring beans with mocks during testing."

---

# VERY IMPORTANT FINAL INTERVIEW FLOW ANSWER

If interviewer asks:

## “How did you use JUnit and Mockito in your project?”

### Answer

"In our password management and onboarding application, we used JUnit 5 and Mockito for unit testing service and controller layers. Repository dependencies were mocked using Mockito to avoid actual database calls. We tested validation logic, onboarding flow, password reset scenarios, and exception handling. MockMvc was used for REST API testing, and these test cases were integrated into CI/CD pipelines to prevent regression issues before deployment."
