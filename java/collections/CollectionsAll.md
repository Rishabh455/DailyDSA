# Java 03 — Collections & Generics

Arrays are fixed-size and clunky. The **Collections Framework** gives you flexible, powerful containers: lists, sets, maps, and more. This is used constantly in real code.

---

## 1. Generics first (needed to understand collections)

**Generics** let you tell a container *what type* it holds, using angle brackets `<...>`.

```java
List<String> names = new ArrayList<>();   // a List that holds Strings
names.add("Aditya");
// names.add(42);   ← ERROR caught at compile time — not a String!
String first = names.get(0);   // no casting needed; Java knows it's a String
```

Without generics you'd get untyped junk and runtime crashes. With generics, mistakes are caught early and you never cast. Read `List<String>` as "List of String."

## 2. The Collections family tree

```
Collection (interface)
├── List   → ordered, allows duplicates       (ArrayList, LinkedList)
├── Set    → no duplicates                     (HashSet, LinkedHashSet, TreeSet)
└── Queue  → process in order (FIFO)           (LinkedList, ArrayDeque)

Map (separate)  → key → value pairs            (HashMap, LinkedHashMap, TreeMap)
```

> Note: `Map` is not technically a `Collection`, but it's part of the framework.

## 3. List — an ordered, resizable sequence

The most-used collection. Keeps order, allows duplicates, grows automatically.

```java
import java.util.*;

List<String> fruits = new ArrayList<>();
fruits.add("apple");
fruits.add("banana");
fruits.add("apple");            // duplicates allowed

System.out.println(fruits.get(0));      // apple  (index-based access)
System.out.println(fruits.size());      // 3
System.out.println(fruits.contains("banana"));  // true
fruits.remove("banana");
fruits.set(0, "cherry");        // replace item at index 0

for (String f : fruits) {       // loop over it
    System.out.println(f);
}
```

**ArrayList vs LinkedList:**
- **ArrayList** — fast to read by index (`get(5)`). Your default choice 95% of the time.
- **LinkedList** — fast to add/remove at the ends. Slower to jump to a random index.

## 4. Set — unique items only

A `Set` automatically rejects duplicates. Great for "is this already here?" checks.

```java
Set<String> emails = new HashSet<>();
emails.add("a@x.com");
emails.add("a@x.com");   // ignored — already present
System.out.println(emails.size());   // 1
```

- **HashSet** — fastest, but no order.
- **LinkedHashSet** — keeps insertion order.
- **TreeSet** — keeps items sorted automatically.

## 5. Map — key → value lookup

A `Map` stores pairs. You look up a **value** by its **key**. Think dictionary: word → definition.

```java
Map<String, Integer> ages = new HashMap<>();
ages.put("Aditya", 25);
ages.put("Sam", 30);

System.out.println(ages.get("Aditya"));        // 25
System.out.println(ages.containsKey("Sam"));   // true
System.out.println(ages.getOrDefault("Bob", 0)); // 0 (Bob not present)

// Loop over a map
for (Map.Entry<String, Integer> entry : ages.entrySet()) {
    System.out.println(entry.getKey() + " → " + entry.getValue());
}

// Keys start at 0 count example: counting words
Map<String, Integer> wordCount = new HashMap<>();
String[] words = {"a", "b", "a", "a", "b"};
for (String w : words) {
    wordCount.put(w, wordCount.getOrDefault(w, 0) + 1);
}
System.out.println(wordCount);   // {a=3, b=2}
```

- **HashMap** — fast, no order. Default choice.
- **LinkedHashMap** — keeps insertion order.
- **TreeMap** — keeps keys sorted.

> **Keys are unique.** `put` with an existing key overwrites the old value.

## 6. Queue and Deque — order-based processing

```java
Queue<String> queue = new LinkedList<>();
queue.add("first");
queue.add("second");
System.out.println(queue.poll());   // "first" — FIFO (First In, First Out)

Deque<String> stack = new ArrayDeque<>();
stack.push("a");
stack.push("b");
System.out.println(stack.pop());    // "b" — LIFO (Last In, First Out)
```

Use these for task scheduling, processing pipelines, undo stacks, etc.

## 7. Choosing the right collection

| I need... | Use |
|-----------|-----|
| An ordered list, duplicates OK | `ArrayList` |
| Unique items | `HashSet` |
| Key → value lookup | `HashMap` |
| Sorted keys/items | `TreeMap` / `TreeSet` |
| Keep insertion order | `LinkedHashMap` / `LinkedHashSet` |
| First-in-first-out processing | `Queue` (LinkedList) |
| Stack (last-in-first-out) | `Deque` (ArrayDeque) |

## 8. Useful Collections utilities

```java
List<Integer> nums = new ArrayList<>(List.of(3, 1, 2));
Collections.sort(nums);              // [1, 2, 3]
Collections.reverse(nums);           // [3, 2, 1]
System.out.println(Collections.max(nums));   // 3

// Quick immutable (unchangeable) collections
List<String> fixed = List.of("a", "b", "c");     // can't add/remove
Map<String, Integer> m = Map.of("x", 1, "y", 2);
```

> `List.of(...)` creates a **read-only** list. Trying to `add` to it throws an error. Use `new ArrayList<>(...)` if you need to modify it.

## 9. Iterators and safe removal

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));

// DON'T remove inside a for-each loop — it crashes (ConcurrentModificationException)
// DO use removeIf:
nums.removeIf(n -> n % 2 == 0);   // removes evens → [1, 3]
```

## 10. Generic methods and classes (writing your own)

You can make your *own* generic code with a type placeholder (commonly `T`):

```java
// A generic method — works for any type
static <T> T firstOf(List<T> list) {
    return list.get(0);
}

// A generic class — a box holding any type
class Box<T> {
    private T item;
    void set(T item) { this.item = item; }
    T get() { return item; }
}

Box<String> b = new Box<>();
b.set("hello");
String s = b.get();
```

`T` is just a made-up name (T = Type). You could use any letter. Common ones: `T` (type), `E` (element), `K`/`V` (key/value).

---

## Practice

1. Store 5 city names in an `ArrayList`, print them, then sort alphabetically.
2. Read 10 numbers and use a `Set` to print only the unique ones.
3. Build a `Map<String, Integer>` of product → price. Print each product and its price.
4. Count how many times each character appears in a word using a `HashMap`.
5. Write a generic method `<T> void printAll(List<T> list)` that prints every element.

**Next:** [04-exceptions-and-io.md](04-exceptions-and-io.md) — handling errors and reading/writing files.