# Checked vs Unchecked Exception - Restaurant Story

Imagine you own a restaurant.

One day, the manager assigns a waiter a task:

> "Go to the kitchen and bring the customer's food."

---

## Checked Exception

Before leaving, the waiter notices:

> "The kitchen might be closed."

Since this is a known possibility, the manager says:

> "Before you go, you must have a backup plan."

Either:

- Check whether the kitchen is open, OR
- Be ready to tell the customer that the kitchen is closed.

The manager will not allow the waiter to leave without handling this situation.

This is a **Checked Exception**.

The compiler also says:

> "You must handle this exception before running the program."

Examples:
- IOException
- SQLException
- FileNotFoundException

These are situations that are expected and recoverable.

---

## Unchecked Exception

Now imagine the waiter is carrying food and suddenly slips on a banana peel.

Nobody expected this.

The manager did not ask him to prepare for banana peels because it is a programming mistake or unexpected problem.

This is an **Unchecked Exception**.

The compiler does not force you to handle it.

Examples:
- NullPointerException
- ArithmeticException
- ArrayIndexOutOfBoundsException

These usually happen because of bugs in the code.

---

## Easy Memory Trick

### Checked Exception

Manager says:

> "I know this problem can happen, so you must be prepared."

Examples:
- File not found
- Database connection issue
- Network issue

---

### Unchecked Exception

Manager says:

> "This should not have happened. Someone made a mistake."

Examples:
- Null pointer
- Divide by zero
- Invalid array index

---

## Interview One-Liner

Checked Exceptions are known and recoverable exceptions that the compiler forces us to handle.

Unchecked Exceptions are programming errors or unexpected runtime problems that the compiler does not force us to handle.