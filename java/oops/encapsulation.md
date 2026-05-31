An ideal encapsulated class:

- Keeps fields `private`.
- Provides controlled access through methods.
- Validates data before modifying it.
- Exposes only the operations that make sense.
- Does not allow the object to enter an invalid state.

Example:

class BankAccount {

    private String accountNumber;
    private double balance;

    public BankAccount(String accountNumber, double initialBalance) {

        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                "Initial balance cannot be negative");
        }

        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Deposit amount must be positive");
        }

        balance += amount;
    }

    public void withdraw(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Withdrawal amount must be positive");
        }

        if (amount > balance) {
            throw new IllegalArgumentException(
                "Insufficient balance");
        }

        balance -= amount;
    }
}

Why is this ideal encapsulation?

- Fields are private.
- No public setter for balance.
- Balance can only be changed through deposit() and withdraw().
- Validation prevents invalid states.
- The object controls its own data.

Interview Answer:

"An ideal encapsulated class hides its data using private fields and provides controlled methods to access or modify that data. It validates inputs and ensures that the object's state always remains valid."
---------------------------------------------------------------
We use getters and setters to protect and control access to an object's data.

If fields are public, any code can directly modify them, which can lead to invalid or inconsistent object states.

Example without encapsulation:

class Employee {
    public int age;
}

Employee emp = new Employee();
emp.age = -10;   // Invalid value

Here, anyone can set an incorrect age.

Example with getters and setters:

class Employee {
    private int age;

    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        this.age = age;
    }

    public int getAge() {
        return age;
    }
}

Employee emp = new Employee();
emp.setAge(-10);   // Validation prevents invalid data

Benefits of Getters and Setters:

1. Encapsulation
   - Hides internal data from direct access.

2. Validation
   - Prevents invalid values from being assigned.

3. Flexibility
   - Internal implementation can change without affecting client code.

4. Read-only or Write-only Access
   - We can provide only getters or only setters when needed.

5. Logging, Security, and Additional Logic
   - Extra processing can be added when data is accessed or modified.

Interview Answer:

"We use getters and setters instead of public fields to achieve encapsulation. They allow us to validate data, control access, maintain object integrity, and change the internal implementation without affecting external code."