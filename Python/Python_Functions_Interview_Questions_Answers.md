# Python Functions — Interview Questions & Answers

## 1. What is a function in Python?

**Answer:**

A function is a reusable block of code designed to perform a specific task. It helps avoid code duplication and makes code more modular, readable, and maintainable.

```python
def add(a, b):
    return a + b
```

---

## 2. How do you define a function in Python?

**Answer:**

We use the `def` keyword followed by the function name, parameters, and function body.

```python
def greet(name):
    print(f"Hello {name}")
```

---

## 3. How do you call a function?

**Answer:**

We call a function using its name followed by parentheses and passing required arguments.

```python
def greet(name):
    print(f"Hello {name}")

greet("Rishabh")
```

---

## 4. What are parameters and arguments?

**Answer:**

A parameter is a variable defined in the function definition. An argument is the actual value passed during the function call.

```python
def add(a, b):       # a and b are parameters
    return a + b

add(10, 20)          # 10 and 20 are arguments
```

---

## 5. What is a return statement?

**Answer:**

`return` sends a value back to the caller and terminates the function's execution.

```python
def add(a, b):
    return a + b

result = add(10, 20)
print(result)
```

Output:

```text
30
```

---

## 6. What happens if a function has no return statement?

**Answer:**

Python automatically returns `None`.

```python
def greet():
    print("Hello")

result = greet()
print(result)
```

Output:

```text
Hello
None
```

---

## 7. What is the difference between `return` and `print()`?

**Answer:**

`print()` displays a value on the console, while `return` sends a value back to the caller so it can be stored or used in another operation.

---

## 8. Can a function return multiple values?

**Answer:**

Yes. Python can return multiple values, which are packed into a tuple.

```python
def calculate(a, b):
    return a + b, a - b, a * b

result = calculate(10, 5)
print(result)
```

Output:

```text
(15, 5, 50)
```

They can also be unpacked:

```python
addition, subtraction, multiplication = calculate(10, 5)
```

---

## 9. What are positional arguments?

**Answer:**

Positional arguments are matched to parameters based on their position.

```python
def employee(name, age):
    print(name, age)

employee("John", 25)
```

---

## 10. What are keyword arguments?

**Answer:**

Keyword arguments are passed using parameter names.

```python
def employee(name, age):
    print(name, age)

employee(age=25, name="John")
```

---

## 11. What are default arguments?

**Answer:**

A default argument is a parameter with a predefined value. If the caller does not provide a value, the default is used.

```python
def greet(name="Guest"):
    print(f"Hello {name}")

greet()
greet("John")
```

---

## 12. What is the rule for default arguments?

**Answer:**

A parameter with a default value cannot be followed by a non-default parameter.

Incorrect:

```python
def test(a=10, b):
    pass
```

Correct:

```python
def test(a, b=10):
    pass
```

---

## 13. What are `*args`?

**Answer:**

`*args` allows a function to accept a variable number of positional arguments. Inside the function, `args` is a tuple.

```python
def add(*args):
    return sum(args)

print(add(10, 20))
print(add(10, 20, 30))
```

---

## 14. What are `**kwargs`?

**Answer:**

`**kwargs` allows a function to accept a variable number of keyword arguments. Inside the function, `kwargs` is a dictionary.

```python
def employee(**kwargs):
    print(kwargs)

employee(name="John", age=25, city="Delhi")
```

---

## 15. Difference between `*args` and `**kwargs`

**Answer:**

`*args` collects variable positional arguments into a tuple, while `**kwargs` collects variable keyword arguments into a dictionary.

```python
def test(*args, **kwargs):
    print(args)
    print(kwargs)

test(10, 20, name="John", age=25)
```

Output:

```text
(10, 20)
{'name': 'John', 'age': 25}
```

---

## 16. Can `*args` and `**kwargs` be used together?

**Answer:**

Yes.

```python
def test(*args, **kwargs):
    print(args)
    print(kwargs)

test(10, 20, name="John", age=25)
```

---

## 17. What are keyword-only arguments?

**Answer:**

Keyword-only parameters must be supplied using their parameter names. They are defined after `*`.

```python
def create_user(name, *, age, city):
    print(name, age, city)

create_user("John", age=25, city="Delhi")
```

---

## 18. What are positional-only parameters?

**Answer:**

Parameters before `/` can only be passed positionally.

```python
def add(a, b, /):
    return a + b

add(10, 20)
```

This is invalid:

```python
add(a=10, b=20)
```

---

## 19. What is a lambda function?

**Answer:**

A lambda is a small anonymous function created using the `lambda` keyword. It contains a single expression.

```python
square = lambda x: x * x

print(square(5))
```

Output:

```text
25
```

---

## 20. What is the difference between `def` and `lambda`?

**Answer:**

`def` is used for regular named functions and supports complex multi-line logic. `lambda` is designed for small anonymous functions containing a single expression.

---

## 21. Can a lambda have multiple parameters?

**Answer:**

Yes.

```python
add = lambda a, b: a + b

print(add(10, 20))
```

---

## 22. What is a nested function?

**Answer:**

A nested function is a function defined inside another function.

```python
def outer():

    def inner():
        print("Inside inner")

    inner()

outer()
```

---

## 23. What is recursion?

**Answer:**

Recursion is a technique where a function calls itself. A recursive function must have a base condition.

```python
def factorial(n):
    if n == 0:
        return 1

    return n * factorial(n - 1)

print(factorial(5))
```

Output:

```text
120
```

---

## 24. What is the LEGB rule?

**Answer:**

LEGB defines the order Python follows when searching for a variable:

- L = Local
- E = Enclosing
- G = Global
- B = Built-in

---

## 25. What is a local variable?

**Answer:**

A local variable is defined inside a function and is normally accessible only within that function.

```python
def test():
    x = 10
    print(x)
```

---

## 26. What is a global variable?

**Answer:**

A global variable is defined outside a function and can generally be accessed from functions in the same module.

```python
x = 100

def test():
    print(x)
```

---

## 27. What is the `global` keyword?

**Answer:**

The `global` keyword allows a function to assign to a variable defined in the global scope.

```python
count = 0

def increment():
    global count
    count += 1

increment()

print(count)
```

Output:

```text
1
```

---

## 28. What is the `nonlocal` keyword?

**Answer:**

`nonlocal` allows a nested function to modify a variable from its nearest enclosing function scope.

```python
def outer():
    count = 0

    def inner():
        nonlocal count
        count += 1

    inner()
    print(count)

outer()
```

Output:

```text
1
```

---

## 29. What are first-class functions?

**Answer:**

Python treats functions as first-class objects. This means functions can be assigned to variables, passed as arguments, returned from functions, and stored in data structures.

```python
def greet():
    return "Hello"

message = greet

print(message())
```

---

## 30. Can a function be passed as an argument?

**Answer:**

Yes.

```python
def square(x):
    return x * x

def process(func, value):
    return func(value)

print(process(square, 5))
```

Output:

```text
25
```

---

## 31. What is a higher-order function?

**Answer:**

A higher-order function is a function that takes another function as an argument, returns a function, or both.

```python
def apply_operation(func, value):
    return func(value)

def square(x):
    return x * x

print(apply_operation(square, 5))
```

---

## 32. What is a closure?

**Answer:**

A closure occurs when an inner function remembers and accesses variables from its enclosing function even after the enclosing function has finished.

```python
def multiplier(x):

    def multiply(y):
        return x * y

    return multiply

double = multiplier(2)

print(double(5))
```

Output:

```text
10
```

---

## 33. What is a decorator?

**Answer:**

A decorator is a function that modifies or extends the behavior of another function without changing its source code.

```python
def logger(func):

    def wrapper():
        print("Function started")
        func()
        print("Function ended")

    return wrapper


@logger
def greet():
    print("Hello")

greet()
```

---

## 34. Why are decorators used?

**Answer:**

Decorators are commonly used for:

- Logging
- Authentication
- Authorization
- Timing
- Caching
- Validation
- Transaction handling

They are heavily used in frameworks such as Flask and FastAPI.

---

## 35. What does `@decorator` mean?

**Answer:**

This:

```python
@logger
def greet():
    pass
```

is essentially equivalent to:

```python
def greet():
    pass

greet = logger(greet)
```

---

## 36. Why is `functools.wraps` used?

**Answer:**

`functools.wraps` preserves metadata such as the original function's name and documentation when a function is wrapped by a decorator.

```python
from functools import wraps

def logger(func):

    @wraps(func)
    def wrapper(*args, **kwargs):
        print("Calling function")
        return func(*args, **kwargs)

    return wrapper
```

---

## 37. What is a generator function?

**Answer:**

A generator function uses `yield` to produce values lazily, one at a time.

```python
def numbers():
    yield 1
    yield 2
    yield 3

for number in numbers():
    print(number)
```

Generators are memory-efficient because they do not need to create the entire result collection at once.

---

## 38. Difference between `return` and `yield`

**Answer:**

`return` terminates a function and sends back a result. `yield` pauses a generator function and produces a value while preserving its execution state.

```python
def normal():
    return [1, 2, 3]

def generator():
    yield 1
    yield 2
    yield 3
```

---

## 39. What is a generator expression?

**Answer:**

A generator expression is a compact way to create a generator.

```python
numbers = (x * x for x in range(5))

for number in numbers:
    print(number)
```

---

## 40. What is a docstring?

**Answer:**

A docstring is a string used to document a function, class, or module.

```python
def add(a, b):
    """Return the sum of two numbers."""
    return a + b
```

It can be accessed using:

```python
print(add.__doc__)
```

---

## 41. What are type hints in functions?

**Answer:**

Type hints specify the expected types of parameters and return values.

```python
def add(a: int, b: int) -> int:
    return a + b
```

Type hints improve readability, IDE support, static analysis, and maintainability. Python generally does not enforce them at runtime.

---

## 42. Are type hints mandatory?

**Answer:**

No. Type hints are optional because Python is dynamically typed.

```python
def add(a, b):
    return a + b
```

---

## 43. What is a callback function?

**Answer:**

A callback is a function passed to another function so it can be called later or at a particular point during execution.

```python
def greet(name):
    print(f"Hello {name}")

def process(callback):
    callback("John")

process(greet)
```

---

## 44. What is `map()`?

**Answer:**

`map()` applies a function to every item in an iterable and returns a map iterator.

```python
numbers = [1, 2, 3, 4]

result = map(lambda x: x * 2, numbers)

print(list(result))
```

Output:

```text
[2, 4, 6, 8]
```

---

## 45. What is `filter()`?

**Answer:**

`filter()` returns an iterator containing elements for which the function returns a truthy value.

```python
numbers = [1, 2, 3, 4, 5]

result = filter(lambda x: x % 2 == 0, numbers)

print(list(result))
```

Output:

```text
[2, 4]
```

---

## 46. What is `reduce()`?

**Answer:**

`reduce()` repeatedly applies a function to elements of an iterable and reduces them to a single value. It is available in `functools`.

```python
from functools import reduce

numbers = [1, 2, 3, 4]

result = reduce(lambda a, b: a + b, numbers)

print(result)
```

Output:

```text
10
```

---

## 47. Difference between `map()`, `filter()`, and `reduce()`

**Answer:**

- `map()` transforms each element.
- `filter()` selects elements based on a condition.
- `reduce()` combines elements into a single result.

---

## 48. What is a pure function?

**Answer:**

A pure function produces the same output for the same inputs and has no observable side effects.

```python
def add(a, b):
    return a + b
```

---

## 49. What is a side effect?

**Answer:**

A side effect is an observable change outside a function's local computation.

Examples include:

- Modifying a global variable
- Modifying a mutable object
- Writing to a file
- Printing to the console
- Making a network request
- Updating a database

---

## 50. What is function composition?

**Answer:**

Function composition means combining functions so that the output of one function becomes the input of another.

```python
def double(x):
    return x * 2

def add_one(x):
    return x + 1

result = add_one(double(5))

print(result)
```

Output:

```text
11
```

---

## 51. What is `pass` inside a function?

**Answer:**

`pass` is a placeholder statement that does nothing.

```python
def future_function():
    pass
```

---

## 52. Can a function accept a list as an argument?

**Answer:**

Yes.

```python
def calculate_total(numbers):
    return sum(numbers)

print(calculate_total([10, 20, 30]))
```

---

## 53. Can a function accept a dictionary as an argument?

**Answer:**

Yes.

```python
def show_user(user):
    print(user["name"])

show_user({"name": "John", "age": 25})
```

---

## 54. Does Python use pass-by-value or pass-by-reference?

**Answer:**

Python uses **call by object sharing**, also described as call by object reference. Function parameters receive references to objects. Mutating a mutable object can be visible to the caller, but rebinding the local parameter does not change the caller's variable.

---

## 55. What happens when a mutable object is passed to a function?

**Answer:**

If the function mutates the object, the change can be visible outside the function.

```python
def update(numbers):
    numbers.append(100)

values = [1, 2, 3]

update(values)

print(values)
```

Output:

```text
[1, 2, 3, 100]
```

---

## 56. What happens when an immutable object is passed to a function?

**Answer:**

Immutable objects such as integers and strings cannot be modified in place. Reassigning the parameter creates a new local binding.

```python
def update(x):
    x = x + 10

value = 5
update(value)

print(value)
```

Output:

```text
5
```

---

## 57. What is the mutable default argument problem?

**Answer:**

Using a mutable object such as a list or dictionary as a default argument can cause unexpected behavior because the default object is created once and reused across calls.

Avoid:

```python
def add_item(item, items=[]):
    items.append(item)
    return items
```

Prefer:

```python
def add_item(item, items=None):
    if items is None:
        items = []

    items.append(item)
    return items
```

---

## 58. When are default argument values evaluated?

**Answer:**

Default argument expressions are evaluated when the function definition is executed, not each time the function is called.

---

## 59. What is function overloading in Python?

**Answer:**

Python does not support traditional compile-time function overloading like Java or C++. Defining the same function name multiple times replaces the previous definition.

```python
def add(a, b):
    return a + b

def add(a, b, c):
    return a + b + c
```

The second `add()` replaces the first one.

Similar behavior can be achieved using default arguments, `*args`, or tools such as `functools.singledispatch`.

---

## 60. How can function overloading be simulated?

**Answer:**

We can use default arguments or `*args`.

```python
def add(*numbers):
    return sum(numbers)

print(add(10, 20))
print(add(10, 20, 30))
```

---

## 61. What is `functools.singledispatch`?

**Answer:**

`singledispatch` provides single-dispatch generic functions where the implementation is selected based on the type of the first argument.

```python
from functools import singledispatch

@singledispatch
def show(value):
    print("Default")

@show.register
def _(value: int):
    print("Integer")

@show.register
def _(value: str):
    print("String")

show(10)
show("Python")
```

---

## 62. What are built-in functions?

**Answer:**

Built-in functions are functions provided by Python and available without defining them ourselves.

Examples:

```python
len()
print()
sum()
max()
min()
type()
id()
```

---

## 63. What are user-defined functions?

**Answer:**

User-defined functions are functions created by the programmer using `def` or `lambda`.

```python
def greet():
    print("Hello")
```

---

## 64. What is an anonymous function?

**Answer:**

An anonymous function is a function without a conventional name. In Python, lambda expressions are commonly used for anonymous functions.

```python
square = lambda x: x * x
```

---

## 65. Can a function be stored in a list?

**Answer:**

Yes, because functions are first-class objects.

```python
def add(a, b):
    return a + b

def multiply(a, b):
    return a * b

operations = [add, multiply]

print(operations[0](2, 3))
print(operations[1](2, 3))
```

---

## 66. What is memoization?

**Answer:**

Memoization is an optimization technique that stores previous function results so repeated calculations can be avoided.

```python
from functools import lru_cache

@lru_cache
def fibonacci(n):
    if n <= 1:
        return n

    return fibonacci(n - 1) + fibonacci(n - 2)
```

---

## 67. What is `functools.partial`?

**Answer:**

`partial` creates a new callable with some arguments of an existing function pre-filled.

```python
from functools import partial

def power(base, exponent):
    return base ** exponent

square = partial(power, exponent=2)

print(square(5))
```

Output:

```text
25
```

---

## 68. What does `*` do when calling a function?

**Answer:**

When calling a function, `*` unpacks an iterable into positional arguments.

```python
def add(a, b, c):
    return a + b + c

numbers = [10, 20, 30]

print(add(*numbers))
```

Output:

```text
60
```

---

## 69. What does `**` do when calling a function?

**Answer:**

When calling a function, `**` unpacks a dictionary into keyword arguments.

```python
def employee(name, age):
    print(name, age)

data = {
    "name": "John",
    "age": 25
}

employee(**data)
```

---

## 70. Difference between `*args` and `*` during a function call

**Answer:**

In a function definition:

```python
def test(*args):
    pass
```

`*args` collects positional arguments.

In a function call:

```python
test(*numbers)
```

`*` unpacks an iterable into positional arguments.

---

## 71. Difference between `**kwargs` and `**` during a function call

**Answer:**

In a function definition:

```python
def test(**kwargs):
    pass
```

`**kwargs` collects keyword arguments into a dictionary.

In a function call:

```python
test(**data)
```

`**` unpacks a dictionary into keyword arguments.

---

# Coding Interview Questions

## 72. Write a function to add two numbers.

```python
def add(a, b):
    return a + b

print(add(10, 20))
```

---

## 73. Write a function to check whether a number is even or odd.

```python
def check_even_odd(number):
    if number % 2 == 0:
        return "Even"
    return "Odd"

print(check_even_odd(10))
```

---

## 74. Write a function to find the maximum of two numbers.

```python
def maximum(a, b):
    return a if a > b else b

print(maximum(10, 20))
```

---

## 75. Write a function to find the maximum of three numbers.

```python
def maximum(a, b, c):
    return max(a, b, c)

print(maximum(10, 20, 15))
```

---

## 76. Write a function to calculate factorial.

```python
def factorial(n):
    result = 1

    for i in range(1, n + 1):
        result *= i

    return result

print(factorial(5))
```

---

## 77. Write a recursive function for factorial.

```python
def factorial(n):
    if n == 0:
        return 1

    return n * factorial(n - 1)

print(factorial(5))
```

---

## 78. Write a function to check whether a number is prime.

```python
def is_prime(n):
    if n < 2:
        return False

    for i in range(2, int(n ** 0.5) + 1):
        if n % i == 0:
            return False

    return True

print(is_prime(17))
```

---

## 79. Write a function to reverse a string.

```python
def reverse_string(text):
    return text[::-1]

print(reverse_string("Python"))
```

---

## 80. Write a function to check whether a string is a palindrome.

```python
def is_palindrome(text):
    return text == text[::-1]

print(is_palindrome("madam"))
```

---

## 81. Write a function to count vowels in a string.

```python
def count_vowels(text):
    vowels = "aeiou"
    count = 0

    for char in text.lower():
        if char in vowels:
            count += 1

    return count

print(count_vowels("Python Programming"))
```

---

## 82. Write a function to find the sum of elements in a list.

```python
def list_sum(numbers):
    return sum(numbers)

print(list_sum([1, 2, 3, 4, 5]))
```

---

## 83. Write a function to find the largest element without using `max()`.

```python
def find_largest(numbers):
    largest = numbers[0]

    for number in numbers:
        if number > largest:
            largest = number

    return largest

print(find_largest([10, 5, 30, 20]))
```

---

## 84. Write a function to remove duplicates from a list.

```python
def remove_duplicates(numbers):
    return list(dict.fromkeys(numbers))

print(remove_duplicates([1, 2, 2, 3, 3, 4]))
```

---

## 85. Write a function to find the second-largest number.

```python
def second_largest(numbers):
    unique_numbers = list(set(numbers))

    if len(unique_numbers) < 2:
        return None

    unique_numbers.sort()
    return unique_numbers[-2]

print(second_largest([10, 20, 5, 20, 30]))
```

---

## 86. Write a function to count the frequency of elements.

```python
def frequency(numbers):
    result = {}

    for number in numbers:
        result[number] = result.get(number, 0) + 1

    return result

print(frequency([1, 2, 2, 3, 3, 3]))
```

---

## 87. Write a function to find duplicate elements.

```python
def find_duplicates(numbers):
    seen = set()
    duplicates = set()

    for number in numbers:
        if number in seen:
            duplicates.add(number)
        else:
            seen.add(number)

    return list(duplicates)

print(find_duplicates([1, 2, 2, 3, 3, 4]))
```

---

## 88. Write a function to generate Fibonacci numbers.

```python
def fibonacci(n):
    a, b = 0, 1
    result = []

    for _ in range(n):
        result.append(a)
        a, b = b, a + b

    return result

print(fibonacci(7))
```

---

## 89. Write a recursive Fibonacci function.

```python
def fibonacci(n):
    if n <= 1:
        return n

    return fibonacci(n - 1) + fibonacci(n - 2)

print(fibonacci(6))
```

**Interview note:** This simple recursive version is exponential and inefficient for large `n`. An iterative approach or memoization is preferable.

---

## 90. Write a function to calculate the sum of digits.

```python
def sum_of_digits(number):
    total = 0

    while number > 0:
        total += number % 10
        number //= 10

    return total

print(sum_of_digits(12345))
```

---

## 91. Write a function to check whether a number is an Armstrong number.

```python
def is_armstrong(number):
    digits = str(number)
    power = len(digits)

    total = sum(int(digit) ** power for digit in digits)

    return total == number

print(is_armstrong(153))
```

---

# Quick Revision

## Function Syntax

```python
def function_name(parameters):
    # code
    return value
```

## Positional Arguments

```python
def add(a, b):
    return a + b

add(10, 20)
```

## Keyword Arguments

```python
add(a=10, b=20)
```

## Default Arguments

```python
def greet(name="Guest"):
    print(name)
```

## `*args`

```python
def test(*args):
    print(args)
```

`args` → tuple

## `**kwargs`

```python
def test(**kwargs):
    print(kwargs)
```

`kwargs` → dictionary

## Lambda

```python
square = lambda x: x * x
```

## Recursion

```python
def factorial(n):
    if n == 0:
        return 1

    return n * factorial(n - 1)
```

## Decorator

```python
def decorator(func):
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)

    return wrapper
```

## Generator

```python
def numbers():
    yield 1
    yield 2
```

## Type Hint

```python
def add(a: int, b: int) -> int:
    return a + b
```

## LEGB

```text
L = Local
E = Enclosing
G = Global
B = Built-in
```

# Most Important Topics for Interviews

1. Function definition and calling
2. Parameters vs arguments
3. `return` vs `print`
4. Positional arguments
5. Keyword arguments
6. Default arguments
7. `*args`
8. `**kwargs`
9. Keyword-only arguments
10. Positional-only arguments
11. Lambda functions
12. Scope and LEGB
13. `global`
14. `nonlocal`
15. Recursion
16. First-class functions
17. Higher-order functions
18. Closures
19. Decorators
20. Generators
21. `yield` vs `return`
22. Mutable default arguments
23. Function overloading
24. `map()` / `filter()` / `reduce()`
25. Type hints
26. Function annotations
27. Pure functions
28. Side effects
29. Memoization
30. Function argument unpacking

# Interview One-Liner

> A Python function is a reusable block of code that performs a specific task. It can accept input through parameters, perform operations, and optionally return a result to the caller.
