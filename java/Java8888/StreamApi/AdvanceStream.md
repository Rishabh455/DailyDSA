These are exactly the kinds of questions that come up in experienced Java interviews. Below are concise, interview-ready Java 8+ Stream API solutions.

---

# 1. Find the First Non-Repeated Character

### Input

```text
programming
```

### Solution

```java
String str = "programming";

Character result = str.chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(
                Function.identity(),
                LinkedHashMap::new,
                Collectors.counting()))
        .entrySet()
        .stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);

System.out.println(result);
```

**Output**

```
p
```

---

# 2. Print Duplicate Characters

```java
String str = "programming";

str.chars()
   .mapToObj(c -> (char) c)
   .collect(Collectors.groupingBy(
           Function.identity(),
           Collectors.counting()))
   .entrySet()
   .stream()
   .filter(e -> e.getValue() > 1)
   .map(Map.Entry::getKey)
   .forEach(System.out::println);
```

**Output**

```
r
g
m
```

---

# 3. Count Frequency of Every Word

```java
String sentence = "java is easy java is powerful";

Map<String, Long> map = Arrays.stream(sentence.split("\\s+"))
        .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()));

System.out.println(map);
```

**Output**

```
{java=2, is=2, easy=1, powerful=1}
```

---

# 4. Find Second Highest Number

```java
List<Integer> list = Arrays.asList(10, 40, 30, 60, 90, 80);

Integer secondHighest = list.stream()
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .findFirst()
        .orElse(null);

System.out.println(secondHighest);
```

**Output**

```
80
```

---

# 5. Find Longest String

```java
List<String> list = Arrays.asList(
        "Java",
        "SpringBoot",
        "Microservices",
        "REST");

String longest = list.stream()
        .max(Comparator.comparing(String::length))
        .orElse("");

System.out.println(longest);
```

**Output**

```
Microservices
```

---

# 6. Highest Paid Employee in Each Department

```java
class Employee {

    int id;
    String name;
    String dept;
    double salary;

    Employee(int id, String name, String dept, double salary) {
        this.id=id;
        this.name=name;
        this.dept=dept;
        this.salary=salary;
    }

    public String getDept() {
        return dept;
    }

    public double getSalary() {
        return salary;
    }

    public String toString() {
        return name + " " + salary;
    }
}

List<Employee> employees = Arrays.asList(
        new Employee(1,"John","IT",80000),
        new Employee(2,"Bob","IT",90000),
        new Employee(3,"David","HR",70000),
        new Employee(4,"Alex","HR",95000)
);

Map<String, Employee> result =
employees.stream()
.collect(Collectors.groupingBy(
        Employee::getDept,
        Collectors.collectingAndThen(
                Collectors.maxBy(
                        Comparator.comparing(Employee::getSalary)),
                Optional::get)));

System.out.println(result);
```

---

# 7. Flatten Nested List

```java
List<List<Integer>> list = Arrays.asList(
        Arrays.asList(1,2),
        Arrays.asList(3,4),
        Arrays.asList(5,6));

List<Integer> result = list.stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

System.out.println(result);
```

**Output**

```
[1,2,3,4,5,6]
```

---

# 8. Find Duplicate Elements in Integer Array

```java
List<Integer> list = Arrays.asList(1,2,3,2,4,5,1);

Set<Integer> set = new HashSet<>();

list.stream()
        .filter(n -> !set.add(n))
        .forEach(System.out::println);
```

**Output**

```
2
1
```

---

# 9. Remove Duplicate Words (Maintain Order)

```java
String sentence = "java java spring java boot spring";

String result = Arrays.stream(sentence.split("\\s+"))
        .distinct()
        .collect(Collectors.joining(" "));

System.out.println(result);
```

**Output**

```
java spring boot
```

---

# 10. Group Strings by Length

```java
List<String> list = Arrays.asList(
        "Java",
        "REST",
        "API",
        "SpringBoot");

Map<Integer,List<String>> map =
list.stream()
.collect(Collectors.groupingBy(String::length));

System.out.println(map);
```

**Output**

```
{
3=[API],
4=[Java, REST],
10=[SpringBoot]
}
```

---

# 11. Find Top 3 Highest Numbers

```java
List<Integer> list =
Arrays.asList(10,90,20,60,50,70,90);

List<Integer> result =
list.stream()
        .distinct()
        .sorted(Comparator.reverseOrder())
        .limit(3)
        .collect(Collectors.toList());

System.out.println(result);
```

**Output**

```
[90,70,60]
```

---

# 12. Check Whether Two Strings are Anagrams

```java
String s1 = "listen";
String s2 = "silent";

boolean result =
s1.length()==s2.length() &&
s1.chars().sorted().boxed().collect(Collectors.toList())
.equals(
s2.chars().sorted().boxed().collect(Collectors.toList()));

System.out.println(result);
```

**Output**

```
true
```

---

# ⭐ Follow-up Questions Interviewers Often Ask

### Q1. Find First Repeated Character

```java
String str = "programming";

Set<Character> set = new HashSet<>();

Character result = str.chars()
        .mapToObj(c -> (char)c)
        .filter(ch -> !set.add(ch))
        .findFirst()
        .orElse(null);

System.out.println(result);
```

Output

```
r
```

---

### Q2. Count Character Frequency

```java
String str="banana";

Map<Character,Long> map =
str.chars()
.mapToObj(c->(char)c)
.collect(Collectors.groupingBy(
Function.identity(),
Collectors.counting()));

System.out.println(map);
```

Output

```
{b=1, a=3, n=2}
```

---

### Q3. Remove Duplicate Characters

```java
String str="programming";

String result =
str.chars()
.mapToObj(c->String.valueOf((char)c))
.distinct()
.collect(Collectors.joining());

System.out.println(result);
```

Output

```
progamin
```

---

### Q4. Reverse Every Word

```java
String sentence="Java Stream API";

String result=
Arrays.stream(sentence.split(" "))
.map(word->new StringBuilder(word).reverse())
.map(StringBuilder::toString)
.collect(Collectors.joining(" "));

System.out.println(result);
```

Output

```
avaJ maertS IPA
```

---

### Q5. Find Palindrome Strings

```java
List<String> list=
Arrays.asList("madam","java","level","spring");

list.stream()
.filter(s->s.equals(new StringBuilder(s).reverse().toString()))
.forEach(System.out::println);
```

Output

```
madam
level
```

---

## Most Important Stream API Methods to Master

These methods cover the vast majority of interview questions:

| Method             | Typical Use                     |
| ------------------ | ------------------------------- |
| `filter()`         | Filtering values                |
| `map()`            | Transforming elements           |
| `flatMap()`        | Flattening nested collections   |
| `distinct()`       | Removing duplicates             |
| `sorted()`         | Sorting                         |
| `limit()`          | Top N elements                  |
| `skip()`           | Nth highest/lowest              |
| `groupingBy()`     | Frequency counts, grouping      |
| `partitioningBy()` | Split into true/false groups    |
| `collect()`        | Gather results                  |
| `reduce()`         | Sum, product, custom reductions |
| `max()` / `min()`  | Extremes                        |
| `findFirst()`      | First matching element          |
| `findAny()`        | Any matching element            |
| `joining()`        | Build strings                   |
| `counting()`       | Frequency calculations          |

If you can solve these 12 problems comfortably and understand the methods above, you'll be well prepared for the Stream API coding questions commonly asked in 3–5 year Java backend interviews.
