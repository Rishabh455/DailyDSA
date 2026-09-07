Python mein **Tuple** ek collection/data structure hai jo multiple values ko **ek single variable** mein store karta hai.

### 1. Tuple kaise banate hain?

```python
my_tuple = (10, 20, 30, 40)
```

Isme 4 values hain.

```python
print(my_tuple)
```

Output:

```text
(10, 20, 30, 40)
```

### 2. Tuple aur List mein main difference

Sabse important point:

> **Tuple immutable hota hai**, yani create hone ke baad uski values change nahi kar sakte.

```python
numbers = (10, 20, 30)

numbers[0] = 100
```

❌ Error aayega:

```text
TypeError: 'tuple' object does not support item assignment
```

Lekin List mutable hoti hai:

```python
numbers = [10, 20, 30]

numbers[0] = 100

print(numbers)
```

Output:

```text
[100, 20, 30]
```

### 3. Tuple indexing

Tuple mein bhi indexing **0 se start** hoti hai:

```python
numbers = (10, 20, 30, 40)

print(numbers[0])  # 10
print(numbers[1])  # 20
print(numbers[3])  # 40
```

### 4. Different types ki values bhi store kar sakte ho

```python
person = ("Rishabh", 25, "Python Developer")
```

Tuple mein:

* String
* Integer
* Float
* Boolean
* Objects
* Even another tuple/list

sab store ho sakte hain.

### 5. Single-element tuple ⚠️

Ye common interview question hai.

```python
x = (10)
```

Ye **tuple nahi**, integer hai.

```python
x = (10,)
```

Ye tuple hai.

Comma `,` important hai.

### 6. Tuple kab use karna chahiye?

Jab tumhare data ko **change nahi karna hai**, tab tuple useful hai.

Example:

```python
coordinates = (19.0760, 72.8777)
```

Ya:

```python
days = ("Monday", "Tuesday", "Wednesday")
```

### Interview answer

> **A tuple is an ordered and immutable collection in Python. It can store multiple values of different data types. Tuples are created using parentheses `()` and their elements cannot be modified after creation.**

**Short trick:**
`List = []` → change kar sakte ho ✅
`Tuple = ()` → change nahi kar sakte ❌
