

## 🔥 Pure Answer (40 sec)

> **HashMap stores data in buckets. Whenever we do `put(key, value)`, it calculates the key's `hashCode()`, finds the correct bucket, and stores the entry there. If two keys go to the same bucket, it's called a collision. Before Java 8, collisions were handled using a Linked List. In Java 8+, if there are many entries in a bucket, it converts the Linked List into a Red-Black Tree for faster searching. When the map becomes 75% full, HashMap doubles its size and rehashes all entries.**

### 🎯 Bas is formula ko yaad rakh:

```text
PUT
 ↓
HASH
 ↓
BUCKET
 ↓
COLLISION
 ↓
TREE
 ↓
RESIZE
```

**Ye 6 keywords yaad hain = poora HashMap answer yaad hai.**
