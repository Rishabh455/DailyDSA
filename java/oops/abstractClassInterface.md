Why do we need Abstract Classes when we already have Interfaces?

Think of a company employee system.

We have:

- Developer
- Tester
- Manager

All employees have some common properties:

- employeeId
- name
- attendance system

But their work is different.

-----------------------------------
Using Abstract Class
-----------------------------------

abstract class Employee {

    protected String name;

    public Employee(String name) {
        this.name = name;
    }

    public void markAttendance() {
        System.out.println(name + " marked attendance");
    }

    public abstract void work();
}

class Developer extends Employee {

    public Developer(String name) {
        super(name);
    }

    @Override
    public void work() {
        System.out.println("Writing code");
    }
}

class Tester extends Employee {

    public Tester(String name) {
        super(name);
    }

    @Override
    public void work() {
        System.out.println("Testing application");
    }
}

Why Abstract Class?

Because all employees share:

- name field
- constructor
- attendance logic

We write it once and reuse it.

-----------------------------------
Using Interface
-----------------------------------

interface Payable {
    void calculateSalary();
}

class Developer extends Employee implements Payable {

    public Developer(String name) {
        super(name);
    }

    @Override
    public void work() {
        System.out.println("Writing code");
    }

    @Override
    public void calculateSalary() {
        System.out.println("Calculating salary");
    }
}

Why Interface?

Because "Payable" is not an "Employee".

It is just a capability/contract.

Anyone can be payable:

- Employee
- Consultant
- Vendor
- Freelancer

Interface defines WHAT should be done.

Abstract class defines WHAT + SOME COMMON IMPLEMENTATION.

-----------------------------------
Story-Based Explanation
-----------------------------------

Imagine a restaurant.

There are:

- Waiter
- Chef
- Manager

All are Employees.

Common things:

- Employee ID
- Name
- Attendance

So create an Abstract Class:

Employee

because all employees share common data and behavior.

Now suppose some people can receive tips.

- Waiter can receive tips.
- Delivery Boy can receive tips.

Receiving tips is a capability, not a parent-child relationship.

So create an Interface:

interface TipReceivable

Any class can implement it.

-----------------------------------
When to use Abstract Class?
-----------------------------------

Use Abstract Class when:

✓ Classes are closely related.
✓ They share common fields.
✓ They share common methods.
✓ Code reuse is required.
✓ Constructors are needed.

Example:

Employee
Vehicle
BankAccount

-----------------------------------
When to use Interface?
-----------------------------------

Use Interface when:

✓ You want to define a contract.
✓ Unrelated classes can implement it.
✓ Multiple inheritance is required.
✓ Only capability/behavior matters.

Example:

Runnable
Comparable
Serializable
Payable

-----------------------------------
Key Difference
-----------------------------------

Abstract Class = "IS-A" relationship

Developer IS AN Employee
Tester IS AN Employee

Interface = "CAN-DO" relationship

Developer CAN calculate salary
Printer CAN print
Bird CAN fly

-----------------------------------
Interview Answer
-----------------------------------

"We use an Abstract Class when multiple related classes share common state and behavior. It provides code reuse through fields, constructors, and concrete methods.

We use an Interface when we want to define a contract or capability that can be implemented by unrelated classes. An abstract class represents an 'IS-A' relationship, while an interface represents a 'CAN-DO' relationship."