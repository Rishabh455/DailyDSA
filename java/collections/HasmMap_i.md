# HashMap — Scenario-Based Java Interview Preparation

## Core Mental Model — Understand This First

Before going into scenarios, keep this one model in your head:

```text
                    HashMap
                       |
                  hashCode(key)
                       |
                       v
                 Find the bucket
                       |
                       v
             Check entries in bucket
                       |
                    equals()
                       |
              +--------+--------+
              |                 |
            true              false
              |                 |
         Same logical key     Collision
              |                 |
         Update value       Store another node
```

The easiest way to remember the responsibilities:

> **`hashCode()` tells HashMap WHERE to look.**
> **`equals()` tells HashMap WHETHER it is the same key.**

A hash collision is not an error. Different keys are allowed to have the same hash code.

---

# Question 1 — Two Different Keys Have the Same Hash Code

### Interview Question

Suppose:

```java
Map<Employee, String> map = new HashMap<>();

Employee e1 = new Employee(101, "Rishabh");
Employee e2 = new Employee(102, "Amit");

map.put(e1, "Developer");
map.put(e2, "Manager");
```

Assume:

```java
e1.hashCode() == e2.hashCode()
```

What happens internally?

Will the second `put()` overwrite the first value, or will both entries be stored?

---

### Conceptual Answer

Both entries can be stored.

The important thing to understand is that **same hash code does not mean same key**.

When we execute:

```java
map.put(e1, "Developer");
```

HashMap:

```text
e1
 ↓
hashCode()
 ↓
hash
 ↓
bucket index
 ↓
store entry
```

Now when we execute:

```java
map.put(e2, "Manager");
```

Suppose `e2` produces the same hash.

HashMap therefore reaches the **same bucket**.

That is called a **collision**.

Conceptually:

```text
Bucket 5

[e1 → "Developer"]
```

Then HashMap examines the existing entry to determine whether `e2` is actually the same key.

It uses equality.

If:

```java
e1.equals(e2) == false
```

then they are considered different keys.

Therefore:

```text
Bucket 5

[e1 → "Developer"] → [e2 → "Manager"]
```

Both entries remain in the HashMap.

### Critical point

HashMap does **not** generate another hash and move `e2` to another bucket just because the bucket already contains something.

The collision is handled **inside the same bucket**.

---

# Question 2 — Why Does HashMap Need `equals()` If It Already Has `hashCode()`?

### Interview Question

If two keys have the same hash code, how does HashMap distinguish between them?

Why does HashMap need `equals()`?

---

### Conceptual Answer

Because **hash codes are not guaranteed to be unique**.

Imagine:

```text
e1.hashCode() = 500
e2.hashCode() = 500
e3.hashCode() = 500
```

All three keys can land in the same bucket.

If HashMap assumed:

```text
same hash → same key
```

then inserting `e2` could incorrectly replace `e1`.

Therefore HashMap uses two levels of identification.

### Level 1 — `hashCode()`

It helps determine:

> "Which bucket should I search?"

### Level 2 — `equals()`

Once HashMap reaches that bucket, it asks:

> "Is this actually the same logical key?"

Therefore:

```text
hashCode()
    ↓
Find bucket
    ↓
equals()
    ↓
Same key or different key?
```

If `equals()` is false:

```text
Collision → keep both entries
```

If `equals()` is true:

```text
Same key → update existing value
```

---

# Question 3 — What If Hash Codes Are Same and `equals()` Is False?

### Interview Question

Suppose:

```java
e1.hashCode() == e2.hashCode();
e1.equals(e2) == false;
```

What happens?

---

### Conceptual Answer

They are **different keys with the same hash code**.

Therefore:

```text
Same hash
   ↓
Same bucket
   ↓
equals() = false
   ↓
Different keys
   ↓
Both entries stored
```

Conceptually:

```text
Bucket 5

[e1 → Developer] → [e2 → Manager]
```

This is a normal **hash collision**.

The important mental model is:

> **A hash identifies a location approximately; it does not uniquely identify an object.**

That is why collisions are allowed.

---

# Question 4 — Mutable Object Used as a HashMap Key

### Interview Question

Consider:

```java
class Employee {
    int id;
    String name;

    // hashCode() is based on id
}
```

Then:

```java
Employee e = new Employee(101, "Rishabh");

Map<Employee, String> map = new HashMap<>();

map.put(e, "Developer");

e.setId(102);

System.out.println(map.get(e));
```

Why can `map.get(e)` return `null` even though we are using the exact same object reference?

---

### Conceptual Answer

This is one of the most important HashMap concepts.

Initially:

```text
e.id = 101
```

Suppose:

```text
hashCode(101) = H1
```

HashMap calculates a bucket from `H1`.

Conceptually:

```text
e
 ↓
id = 101
 ↓
hash = H1
 ↓
Bucket 5
 ↓
[e → Developer]
```

Now we mutate the same object:

```java
e.setId(102);
```

The object reference is still the same.

But its state has changed:

```text
e
 ↓
id = 102
```

If `hashCode()` depends on `id`, then:

```text
hashCode(102) = H2
```

Now the object has a **different hash**.

But HashMap does not monitor the object and relocate its existing node.

So the physical situation can become:

```text
Bucket 5
   ↓
[e(id=102) → Developer]
```

while a new lookup does:

```text
get(e)
 ↓
current hash
 ↓
H2
 ↓
Bucket 7
```

HashMap searches bucket 7.

It does not find the entry that is physically sitting in bucket 5.

Therefore:

```java
map.get(e)
```

can return:

```text
null
```

### The critical insight

The problem is **not** that HashMap still contains an "old Employee object."

It contains the **same object**, but that object is now in the wrong logical bucket relative to its current hash code.

### Mental model

> **A HashMap key must remain stable while it is being used as a key.**

If a field participating in `equals()`/`hashCode()` changes, lookup behavior can break.

---

# Question 5 — What Happens If We Mutate the Key and Then Call `put()` Again?

### Interview Question

Consider:

```java
Employee e = new Employee(101, "Rishabh");

map.put(e, "Developer");

e.setId(102);

map.put(e, "Manager");
```

Will HashMap update the old entry, create a second entry, or repair the old entry?

---

### Conceptual Answer

It can create a **second entry**.

Initially:

```text
e.id = 101
hash = H1
bucket = 5

Bucket 5
   ↓
[e → Developer]
```

Then:

```java
e.setId(102);
```

Now:

```text
e.id = 102
hash = H2
bucket = 7
```

The old node remains in bucket 5.

Now:

```java
map.put(e, "Manager");
```

HashMap calculates the **current hash**.

It searches bucket 7.

If it doesn't find an equal key there, it inserts another node.

Conceptually:

```text
Bucket 5
   ↓
[e → Developer]


Bucket 7
   ↓
[e → Manager]
```

Both entries can refer to the **same Employee object**.

This is why mutable HashMap keys are dangerous.

### Important correction to your mental model

Do not think:

```text
Employee(101) = old object
Employee(102) = new object
```

Here it is the **same object**:

```text
same reference
     ↓
state changed
     ↓
hash changed
     ↓
new bucket used for lookup/insertion
```

---

# Question 6 — HashMap Has Thousands of Collisions

### Interview Question

Suppose a badly implemented `hashCode()` causes thousands of keys to land in the same bucket.

Won't HashMap become slow?

How does modern Java HashMap deal with excessive collisions?

---

### Conceptual Answer

Yes.

If many entries are stored in one bucket, searching through a linked structure can become expensive.

Conceptually:

```text
Bucket 5

A → B → C → D → E → F → G → H → ...
```

Searching may require traversing many nodes.

Worst-case linked-list lookup is approximately:

```text
O(n)
```

Java 8+ HashMap introduced **treeification**.

When a bucket becomes sufficiently crowded, HashMap can convert that bucket's node structure into a **Red-Black Tree**.

Conceptually:

```text
Before:

Bucket 5
A → B → C → D → E → F → G → H


After:

Bucket 5

        D
      /   \
     B     F
    / \   / \
   A   C E   G
```

Tree-based lookup can provide approximately:

```text
O(log n)
```

instead of:

```text
O(n)
```

for that heavily-collided bucket.

### Very important

The **entire HashMap does NOT become a Red-Black Tree**.

Only the heavily-collided bucket's node structure can be treeified.

You could therefore have:

```text
Bucket 0 → linked structure
Bucket 1 → empty
Bucket 2 → one node
Bucket 3 → linked structure
Bucket 4 → empty
Bucket 5 → Red-Black Tree
Bucket 6 → linked structure
```

---

# Question 7 — Does 8 Nodes Automatically Mean Treeification?

### Interview Question

Suppose:

```text
HashMap capacity = 16

Bucket 5:
A → B → C → D → E → F → G → H
```

There are 8 nodes in the bucket.

Will HashMap immediately convert the bucket into a Red-Black Tree?

---

### Conceptual Answer

Not necessarily.

The commonly discussed Java 8+ thresholds are:

```text
TREEIFY_THRESHOLD = 8
MIN_TREEIFY_CAPACITY = 64
UNTREEIFY_THRESHOLD = 6
```

When the bucket becomes sufficiently large, HashMap considers treeification.

But it also considers the overall table capacity.

If:

```text
capacity < 64
```

HashMap generally prefers **resizing the table** rather than treeifying.

Why?

Because the problem may be that the table itself is too small.

Increasing the number of buckets can distribute entries more evenly and reduce collisions.

Therefore:

```text
Many collisions
      ↓
Treeification threshold reached
      ↓
Check table capacity
      ↓
Capacity < 64
      ↓
Resize
```

Whereas:

```text
Many collisions
      ↓
Threshold reached
      ↓
Capacity >= 64
      ↓
Treeify bucket
```

### Important distinction

**Resizing** changes the table capacity.

**Treeification** changes the structure of a heavily-collided bucket.

They are two different mechanisms.

---

# Question 8 — What Happens When a Treeified Bucket Shrinks?

### Interview Question

Suppose:

```text
HashMap capacity = 64

Bucket 5 → Red-Black Tree
```

After removing several entries, only a few nodes remain.

What happens?

---

### Conceptual Answer

A tree is useful when there are many nodes.

If the number of entries in the treeified bucket falls sufficiently, HashMap can **untreeify** it and convert it back into a linked structure.

The commonly discussed threshold is:

```text
UNTREEIFY_THRESHOLD = 6
```

Conceptually:

```text
Red-Black Tree
      ↓
Entries removed
      ↓
Number of nodes decreases
      ↓
Bucket becomes small
      ↓
Untreeify
      ↓
Linked structure
```

For example:

```text
8 nodes → tree
7 nodes → tree
6 or fewer → can revert to linked structure
```

### Important

Removing entries does **not mean HashMap automatically shrinks its overall table capacity**.

Do not confuse:

```text
Untreeification
```

with:

```text
Resizing/shrinking the HashMap
```

They are different concepts.

---

# Question 9 — `equals()` Is Overridden but `hashCode()` Is Not

### Interview Question

Consider:

```java
class Employee {

    int id;
    String name;

    @Override
    public boolean equals(Object obj) {
        Employee e = (Employee) obj;
        return this.id == e.id;
    }

    // hashCode() is NOT overridden
}
```

Then:

```java
Employee e1 = new Employee(101, "Rishabh");
Employee e2 = new Employee(101, "Rishabh");

Map<Employee, String> map = new HashMap<>();

map.put(e1, "Developer");

System.out.println(map.get(e2));
```

`e1.equals(e2)` returns true.

Why can `map.get(e2)` still return `null`?

---

### Conceptual Answer

Because overriding `equals()` without correctly overriding `hashCode()` violates the Java contract.

We have:

```java
e1.equals(e2) == true
```

But because `hashCode()` wasn't overridden, the two different objects can have different hash codes:

```text
e1.hashCode() → H1
e2.hashCode() → H2

H1 != H2
```

Now look at what happens.

### During `put(e1)`

```text
e1
 ↓
hashCode() → H1
 ↓
Bucket 5
 ↓
[e1 → Developer]
```

### During `get(e2)`

```text
e2
 ↓
hashCode() → H2
 ↓
Bucket 12
```

HashMap looks in bucket 12.

But the entry is in bucket 5.

Therefore it may return:

```text
null
```

The important thing is:

> **HashMap does not search the entire map looking for a key for which `equals()` returns true.**

It first uses the hash to identify the relevant bucket.

Only then does it compare entries inside that bucket.

Therefore, if equal objects produce different hashes, the lookup can fail before `equals()` can help.

---

# Question 10 — The `equals()` / `hashCode()` Contract

### Interview Question

What is the contract between `equals()` and `hashCode()` that HashMap depends upon?

---

### Conceptual Answer

The most important rule is:

```java
if (a.equals(b)) {
    a.hashCode() == b.hashCode();
}
```

In words:

> **If two objects are equal according to `equals()`, they MUST have the same hash code.**

However, the reverse is not required.

This is perfectly valid:

```text
a.hashCode() == b.hashCode()
a.equals(b) == false
```

That is simply a **hash collision**.

So:

```text
Equal objects
      ↓
Must have same hash

Same hash
      ↓
May or may not be equal
```

This distinction is fundamental to understanding HashMap.

---

# Question 11 — Is HashMap Thread-Safe?

### Interview Question

Suppose two threads concurrently modify the same HashMap:

```text
Thread 1:
map.put("A", 100);

Thread 2:
map.put("B", 200);
```

Or both threads update the same key:

```text
Thread 1:
map.put("A", 100);

Thread 2:
map.put("A", 400);
```

Is HashMap thread-safe?

What problems can occur?

---

### Conceptual Answer

No.

`HashMap` is **not thread-safe**.

It does not provide synchronization for concurrent structural modifications.

With concurrent access, we can encounter issues such as:

* lost or overwritten updates
* inconsistent visibility between threads
* inconsistent map state
* problems during concurrent structural modifications
* unsafe behavior when one thread modifies the map while another is iterating

For example:

```text
Thread 1                 Thread 2

put("A", 100)            put("A", 400)
       \                   /
        \                 /
         concurrent access
                 ↓
       final result depends
       on execution/interleaving
```

Therefore, we should not assume ordinary `HashMap` is safe for concurrent modification.

---

# Question 12 — What Should We Use for Concurrent Access?

### Interview Question

If multiple threads need to read and update the same map, what would you use?

---

### Conceptual Answer

A common choice is:

```java
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>();
```

`ConcurrentHashMap` is specifically designed for concurrent access.

It provides thread-safe map operations while allowing substantially more concurrency than simply synchronizing the entire map.

So the mental model is:

```text
Single-threaded / externally synchronized
        ↓
HashMap

Concurrent shared access
        ↓
ConcurrentHashMap
```

---

# FINAL HASHMAP MENTAL MODEL

If an interviewer gives you any unfamiliar HashMap scenario, mentally walk through these steps:

```text
1. What is the key?

       ↓

2. What does its hashCode() return?

       ↓

3. Which bucket does that hash map to?

       ↓

4. Is that bucket empty?

       ↓

5. If not, is the existing key equal?

       ↓

6. equals() == true?
       → Same logical key
       → Update value

   equals() == false?
       → Collision
       → Another node in same bucket

       ↓

7. Are collisions becoming excessive?

       ↓

8. Consider resizing / treeification

       ↓

9. Is the key mutable?

       ↓

10. Is multiple-thread access involved?
```

---

# The 7 Rules You Should Actually Understand

### Rule 1

> **Same hash code does NOT mean same key.**

```text
same hash + equals false
        ↓
collision
        ↓
both entries can exist
```

### Rule 2

> **Equal objects MUST have the same hash code.**

```text
equals true
      ↓
hash codes must be equal
```

### Rule 3

> **`hashCode()` helps locate the bucket; `equals()` identifies the logical key within that bucket.**

### Rule 4

> **HashMap does not create a new bucket just because a collision occurs.**

Multiple entries can exist inside the same bucket.

### Rule 5

> **Don't mutate fields used by `equals()`/`hashCode()` while the object is being used as a HashMap key.**

Otherwise the object can become effectively unreachable from the map.

### Rule 6

> **Treeification applies to a particular heavily-collided bucket, not the entire HashMap.**

### Rule 7

> **HashMap is not thread-safe.**

For concurrent shared access, consider `ConcurrentHashMap`.

---

# One Final Interview-Level Example

If an interviewer gives you:

```java
Employee e1 = new Employee(101, "Rishabh");
Employee e2 = new Employee(101, "Rishabh");

map.put(e1, "Developer");

map.get(e2);
```

Don't immediately answer.

Run the mental algorithm:

```text
Are e1 and e2 equal?
        ↓
What does equals() say?

        +

Do e1 and e2 have the same hashCode?
        ↓
What does hashCode() say?

        ↓

Same hash + equals true
        → retrieve existing value

Same hash + equals false
        → different key / collision

Different hash
        → different lookup location
        → existing entry may not be found
```

That way, even if the interviewer changes the scenario, you don't need to memorize the answer.

You simply **execute the HashMap algorithm mentally**.
