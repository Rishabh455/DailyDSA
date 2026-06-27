## Employee Table (Use this for all 30 SQL Interview Questions)

```sql
CREATE TABLE Employee (
    emp_id INT PRIMARY KEY,
    emp_name VARCHAR(50),
    department VARCHAR(50),
    salary INT,
    manager_id INT,
    hire_date DATE,
    city VARCHAR(50)
);
```

### Dummy Data

```sql
INSERT INTO Employee VALUES
(101,'Amit','IT',80000,NULL,'2020-01-15','Mumbai'),
(102,'Rahul','HR',50000,101,'2021-03-12','Delhi'),
(103,'Neha','IT',90000,101,'2019-07-01','Pune'),
(104,'Priya','Finance',70000,105,'2022-05-10','Mumbai'),
(105,'Karan','Finance',120000,NULL,'2018-09-20','Delhi'),
(106,'Ankit','IT',80000,101,'2023-02-15','Bangalore'),
(107,'Sneha','HR',55000,102,'2020-11-05','Delhi'),
(108,'Rohit','Sales',65000,109,'2022-08-18','Mumbai'),
(109,'Megha','Sales',95000,NULL,'2017-12-30','Pune'),
(110,'Vikas','IT',75000,103,'2021-06-11','Hyderabad');
```

---

# Top 30 Most Frequently Asked SQL Interview Queries

## 1. Display all employees

```sql
SELECT * FROM Employee;
```

---

## 2. Display employee names only

```sql
SELECT emp_name FROM Employee;
```

---

## 3. Find employees working in IT department

```sql
SELECT * FROM Employee
WHERE department='IT';
```

---

## 4. Find employees earning more than 80000

```sql
SELECT * FROM Employee
WHERE salary>80000;
```

---

## 5. Find employees whose name starts with 'A'

```sql
SELECT *
FROM Employee
WHERE emp_name LIKE 'A%';
```

---

## 6. Find employees whose name ends with 'a'

```sql
SELECT *
FROM Employee
WHERE emp_name LIKE '%a';
```

---

## 7. Find employees whose salary is between 60000 and 90000

```sql
SELECT *
FROM Employee
WHERE salary BETWEEN 60000 AND 90000;
```

---

## 8. Sort employees by salary descending

```sql
SELECT *
FROM Employee
ORDER BY salary DESC;
```

---

## 9. Count total employees

```sql
SELECT COUNT(*)
FROM Employee;
```

---

## 10. Find maximum salary

```sql
SELECT MAX(salary)
FROM Employee;
```

---

## 11. Find minimum salary

```sql
SELECT MIN(salary)
FROM Employee;
```

---

## 12. Find average salary

```sql
SELECT AVG(salary)
FROM Employee;
```

---

## 13. Find total salary paid

```sql
SELECT SUM(salary)
FROM Employee;
```

---

## 14. Count employees department wise

```sql
SELECT department,
COUNT(*)
FROM Employee
GROUP BY department;
```

---

## 15. Departments having more than 2 employees

```sql
SELECT department,
COUNT(*)
FROM Employee
GROUP BY department
HAVING COUNT(*)>2;
```

---

## 16. Find duplicate salaries

```sql
SELECT salary,
COUNT(*)
FROM Employee
GROUP BY salary
HAVING COUNT(*)>1;
```

---

## 17. Find employees earning maximum salary

```sql
SELECT *
FROM Employee
WHERE salary=
(
SELECT MAX(salary)
FROM Employee
);
```

---

## 18. Find second highest salary

```sql
SELECT MAX(salary)
FROM Employee
WHERE salary<
(
SELECT MAX(salary)
FROM Employee
);
```

---

## 19. Find Nth highest salary (N=3)

```sql
SELECT DISTINCT salary
FROM Employee
ORDER BY salary DESC
LIMIT 1 OFFSET 2;
```

---

## 20. Find employees earning above average salary

```sql
SELECT *
FROM Employee
WHERE salary>
(
SELECT AVG(salary)
FROM Employee
);
```

---

## 21. Find employees from Mumbai or Delhi

```sql
SELECT *
FROM Employee
WHERE city IN ('Mumbai','Delhi');
```

---

## 22. Find employees hired after 2021

```sql
SELECT *
FROM Employee
WHERE hire_date>'2021-12-31';
```

---

## 23. Display unique departments

```sql
SELECT DISTINCT department
FROM Employee;
```

---

## 24. Find employee and manager names (Self Join)

```sql
SELECT e.emp_name Employee,
m.emp_name Manager
FROM Employee e
LEFT JOIN Employee m
ON e.manager_id=m.emp_id;
```

---

## 25. Find employees without managers

```sql
SELECT *
FROM Employee
WHERE manager_id IS NULL;
```

---

## 26. Rank employees based on salary

```sql
SELECT emp_name,
salary,
DENSE_RANK() OVER(ORDER BY salary DESC) RankNo
FROM Employee;
```

---

## 27. Find Top 3 highest paid employees

```sql
SELECT *
FROM Employee
ORDER BY salary DESC
LIMIT 3;
```

---

## 28. Find latest hired employee

```sql
SELECT *
FROM Employee
ORDER BY hire_date DESC
LIMIT 1;
```

---

## 29. Update salary of employee

```sql
UPDATE Employee
SET salary=85000
WHERE emp_id=101;
```

---

## 30. Delete employees from HR

```sql
DELETE FROM Employee
WHERE department='HR';
```

---

# ⭐ Must-Know Theory Questions (Almost Every Interview)

1. Difference between WHERE and HAVING
2. INNER JOIN vs LEFT JOIN vs RIGHT JOIN vs FULL JOIN
3. UNION vs UNION ALL
4. DELETE vs TRUNCATE vs DROP
5. Primary Key vs Foreign Key
6. Clustered vs Non-Clustered Index
7. What is Normalization (1NF, 2NF, 3NF)?
8. What is a Composite Key?
9. What are ACID Properties?
10. Explain Indexing and when it improves/slows performance.

These 30 practical queries and the accompanying theory topics represent the **highest-frequency SQL questions** asked in Java backend interviews (roughly 2–5 years of experience). Practicing them thoroughly will prepare you for the vast majority of SQL rounds.
