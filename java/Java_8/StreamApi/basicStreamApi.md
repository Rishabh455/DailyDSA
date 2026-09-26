For a **3–5 years Java Developer interview**, these are the **20 basic but very high-probability Stream API coding questions**. They focus on the operations interviewers ask repeatedly: `filter`, `map`, `sorted`, `distinct`, `groupingBy`, `Collectors`, `Optional`, `reduce`, etc.

---

# 1. Filter Even Numbers

### Input

```java
List<Integer> list = Arrays.asList(1,2,3,4,5,6);
```

### Solution

```java
List<Integer> result = list.stream()
        .filter(n -> n % 2 == 0)
        .collect(Collectors.toList());

System.out.println(result);
```

Output

```
[2, 4, 6]
```

---

# 2. Filter Odd Numbers

```java
List<Integer> result = list.stream()
        .filter(n -> n % 2 != 0)
        .collect(Collectors.toList());
```

---

# 3. Convert Strings to Uppercase

```java
List<String> names = Arrays.asList("java","spring","boot");

List<String> result = names.stream()
        .map(String::toUpperCase)
        .collect(Collectors.toList());

System.out.println(result);
```

Output

```
[JAVA, SPRING, BOOT]
```

---

# 4. Convert Strings to Lowercase

```java
List<String> result = names.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toList());
```

---

# 5. Find Length of Every String

```java
List<Integer> result = names.stream()
        .map(String::length)
        .collect(Collectors.toList());

System.out.println(result);
```

Output

```
[4,6,4]
```

---

# 6. Remove Duplicate Numbers

```java
List<Integer> list = Arrays.asList(1,2,2,3,4,4,5);

List<Integer> result = list.stream()
        .distinct()
        .collect(Collectors.toList());

System.out.println(result);
```

Output

```
[1,2,3,4,5]
```

---

# 7. Sort Numbers Ascending

```java
list.stream()
    .sorted()
    .forEach(System.out::println);
```

---

# 8. Sort Numbers Descending

```java
list.stream()
    .sorted(Comparator.reverseOrder())
    .forEach(System.out::println);
```

---

# 9. Find Maximum Number

```java
Integer max = list.stream()
        .max(Integer::compareTo)
        .orElse(null);

System.out.println(max);
```

---

# 10. Find Minimum Number

```java
Integer min = list.stream()
        .min(Integer::compareTo)
        .orElse(null);
```

---

# 11. Find Sum of Numbers

```java
int sum = list.stream()
        .mapToInt(Integer::intValue)
        .sum();

System.out.println(sum);
```

---

# 12. Find Average

```java
double avg = list.stream()
        .mapToInt(Integer::intValue)
        .average()
        .orElse(0);

System.out.println(avg);
```

---

# 13. Count Total Elements

```java
long count = list.stream().count();

System.out.println(count);
```

---

# 14. Find First Element

```java
Integer first = list.stream()
        .findFirst()
        .orElse(null);

System.out.println(first);
```

---

# 15. Find Any Element

```java
Integer any = list.stream()
        .findAny()
        .orElse(null);

System.out.println(any);
```

---

# 16. Check if Any Number is Greater than 100

```java
boolean result = list.stream()
        .anyMatch(n -> n > 100);

System.out.println(result);
```

---

# 17. Check if All Numbers are Positive

```java
boolean result = list.stream()
        .allMatch(n -> n > 0);

System.out.println(result);
```

---

# 18. Check if No Number is Negative

```java
boolean result = list.stream()
        .noneMatch(n -> n < 0);

System.out.println(result);
```

---

# 19. Find Second Highest Number

```java
Integer secondHighest = list.stream()
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .findFirst()
        .orElse(null);

System.out.println(secondHighest);
```

---

# 20. Join Strings Using Comma

```java
List<String> names = Arrays.asList("Java","Spring","Boot");

String result = names.stream()
        .collect(Collectors.joining(","));

System.out.println(result);
```

Output

```
Java,Spring,Boot
```

---

# ⭐ Bonus (Very Frequently Asked)

## Find Top 3 Highest Numbers

```java
list.stream()
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(3)
    .forEach(System.out::println);
```

---

## Filter Strings Starting with "A"

```java
List<String> names = Arrays.asList("Apple","Java","Angular","AWS");

names.stream()
     .filter(name -> name.startsWith("A"))
     .forEach(System.out::println);
```

---

## Count Frequency of Elements

```java
Map<Integer, Long> map = list.stream()
        .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()));

System.out.println(map);
```

---

## Convert List to Set

```java
Set<Integer> set = list.stream()
        .collect(Collectors.toSet());
```

---

## Flatten Nested List

```java
List<List<Integer>> nested = Arrays.asList(
        Arrays.asList(1,2),
        Arrays.asList(3,4));

List<Integer> result = nested.stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

System.out.println(result);
```

---

# Stream API Methods You Must Know

| Method            | Purpose                    | Interview Frequency |
| ----------------- | -------------------------- | ------------------- |
| `filter()`        | Filter data                | ⭐⭐⭐⭐⭐               |
| `map()`           | Transform elements         | ⭐⭐⭐⭐⭐               |
| `distinct()`      | Remove duplicates          | ⭐⭐⭐⭐⭐               |
| `sorted()`        | Sort elements              | ⭐⭐⭐⭐⭐               |
| `limit()`         | Top N records              | ⭐⭐⭐⭐⭐               |
| `skip()`          | Skip first N elements      | ⭐⭐⭐⭐⭐               |
| `collect()`       | Collect into List/Set/Map  | ⭐⭐⭐⭐⭐               |
| `groupingBy()`    | Group or count elements    | ⭐⭐⭐⭐⭐               |
| `flatMap()`       | Flatten nested collections | ⭐⭐⭐⭐                |
| `reduce()`        | Aggregate values           | ⭐⭐⭐⭐                |
| `findFirst()`     | First matching element     | ⭐⭐⭐⭐                |
| `findAny()`       | Any matching element       | ⭐⭐⭐                 |
| `max()` / `min()` | Find extremes              | ⭐⭐⭐⭐⭐               |
| `anyMatch()`      | At least one matches       | ⭐⭐⭐⭐                |
| `allMatch()`      | All match                  | ⭐⭐⭐⭐                |
| `noneMatch()`     | None match                 | ⭐⭐⭐⭐                |
| `joining()`       | Concatenate strings        | ⭐⭐⭐⭐                |

These 20 questions form a strong foundation and are among the most common Stream API coding exercises asked in Java interviews for developers with 3–5 years of experience.
