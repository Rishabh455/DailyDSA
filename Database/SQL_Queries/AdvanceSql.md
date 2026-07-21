# SQL Interview Prep — Questions 31–50
> Window Functions & Advanced SQL

---

## Q31 — Delete duplicate employees while keeping only one record.
**Topic:** ROW_NUMBER | **Difficulty:** ⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH cte AS (
  SELECT emp_id, emp_name, dept, salary,
    ROW_NUMBER() OVER (
      PARTITION BY emp_name, dept, salary
      ORDER BY emp_id
    ) AS rn
  FROM employees
)
DELETE FROM cte WHERE rn > 1;
```

> **Key concept:** ROW_NUMBER() assigns unique row number to each duplicate — keep rn=1, delete rest. Works in SQL Server/PostgreSQL.

---

## Q32 — Display duplicate employee records.
**Topic:** GROUP BY + HAVING | **Difficulty:** ⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
SELECT emp_name, dept, salary, COUNT(*) AS cnt
FROM employees
GROUP BY emp_name, dept, salary
HAVING COUNT(*) > 1;
```

> **Key concept:** HAVING filters after GROUP BY — use it to find groups with more than 1 occurrence.

---

## Q33 — Find the highest-paid employee from each department.
**Topic:** ROW_NUMBER / RANK | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH ranked AS (
  SELECT emp_id, emp_name, dept, salary,
    ROW_NUMBER() OVER (
      PARTITION BY dept ORDER BY salary DESC
    ) AS rn
  FROM employees
)
SELECT emp_id, emp_name, dept, salary
FROM ranked WHERE rn = 1;
```

> **Key concept:** PARTITION BY dept resets rank per department. ROW_NUMBER gives exactly 1 top earner even on ties.

---

## Q34 — Find the second highest salary in every department.
**Topic:** DENSE_RANK | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH ranked AS (
  SELECT emp_id, emp_name, dept, salary,
    DENSE_RANK() OVER (
      PARTITION BY dept ORDER BY salary DESC
    ) AS dr
  FROM employees
)
SELECT emp_id, emp_name, dept, salary
FROM ranked WHERE dr = 2;
```

> **Key concept:** Use DENSE_RANK (not ROW_NUMBER) so ties at rank 1 don't skip rank 2. E.g. two people earn 90k → both rank 1, next salary = rank 2.

---

## Q35 — Display the Top 3 highest-paid employees from each department.
**Topic:** ROW_NUMBER PARTITION BY | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH ranked AS (
  SELECT emp_id, emp_name, dept, salary,
    ROW_NUMBER() OVER (
      PARTITION BY dept ORDER BY salary DESC
    ) AS rn
  FROM employees
)
SELECT emp_id, emp_name, dept, salary, rn
FROM ranked WHERE rn <= 3;
```

> **Key concept:** Classic pattern: window rank → filter. For ties to be included, switch to DENSE_RANK instead.

---

## Q36 — Find employees whose salary is greater than their manager's salary.
**Topic:** Self Join | **Difficulty:** ⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
SELECT e.emp_id, e.emp_name,
       e.salary AS emp_salary,
       m.emp_name AS manager_name,
       m.salary AS mgr_salary
FROM employees e
JOIN employees m ON e.manager_id = m.emp_id
WHERE e.salary > m.salary;
```

> **Key concept:** Self Join: same table aliased twice (e=employee, m=manager). Join condition links manager_id → emp_id.

---

## Q37 — Find employees who earn the same salary within the same department.
**Topic:** Self Join / GROUP BY | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐

```sql
-- Approach 1: Self Join
SELECT a.emp_name, b.emp_name AS peer, a.dept, a.salary
FROM employees a
JOIN employees b
  ON a.dept = b.dept
  AND a.salary = b.salary
  AND a.emp_id <> b.emp_id;

-- Approach 2: GROUP BY + HAVING
SELECT dept, salary, COUNT(*) AS cnt
FROM employees
GROUP BY dept, salary
HAVING COUNT(*) > 1;
```

> **Key concept:** a.emp_id <> b.emp_id prevents self-match. Approach 2 gives a summary; Approach 1 lists actual pairs.

---

## Q38 — Calculate the running total of salaries ordered by hire date.
**Topic:** SUM() OVER() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
SELECT emp_id, emp_name, hire_date, salary,
  SUM(salary) OVER (
    ORDER BY hire_date
    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
  ) AS running_total
FROM employees
ORDER BY hire_date;
```

> **Key concept:** ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW = cumulative sum up to current row. Default for ORDER BY in window function.

---

## Q39 — Display cumulative salary department-wise.
**Topic:** Window Aggregate | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
SELECT emp_id, emp_name, dept, salary,
  SUM(salary) OVER (
    PARTITION BY dept
    ORDER BY emp_id
    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
  ) AS cumulative_salary
FROM employees
ORDER BY dept, emp_id;
```

> **Key concept:** PARTITION BY dept resets the running total per department. Without PARTITION BY it would be a single global running total.

---

## Q40 — Find the previous employee's salary and next employee's salary.
**Topic:** LAG() / LEAD() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
SELECT emp_id, emp_name, salary,
  LAG(salary, 1, 0)  OVER (ORDER BY emp_id) AS prev_salary,
  LEAD(salary, 1, 0) OVER (ORDER BY emp_id) AS next_salary
FROM employees;
```

> **Key concept:** LAG(col, offset, default) — looks back. LEAD(col, offset, default) — looks ahead. Third arg is default when no prev/next row exists.

---

## Q41 — Calculate salary difference from previous employee.
**Topic:** LAG() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐

```sql
SELECT emp_id, emp_name, salary,
  LAG(salary) OVER (ORDER BY emp_id) AS prev_salary,
  salary - LAG(salary) OVER (ORDER BY emp_id) AS salary_diff
FROM employees;
```

> **Key concept:** Salary diff = current - previous. NULL for first row since no previous exists. Wrap in COALESCE if needed.

---

## Q42 — Find employees having salary greater than the department average.
**Topic:** Window AVG() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH dept_avg AS (
  SELECT emp_id, emp_name, dept, salary,
    AVG(salary) OVER (PARTITION BY dept) AS dept_avg_sal
  FROM employees
)
SELECT emp_id, emp_name, dept, salary, dept_avg_sal
FROM dept_avg
WHERE salary > dept_avg_sal;
```

> **Key concept:** AVG() OVER(PARTITION BY dept) computes dept average on the fly per row — no GROUP BY needed. CTE keeps query readable.

---

## Q43 — Display department-wise highest, lowest and average salary in one query.
**Topic:** Window Functions | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐

```sql
SELECT DISTINCT dept,
  MAX(salary) OVER (PARTITION BY dept) AS max_sal,
  MIN(salary) OVER (PARTITION BY dept) AS min_sal,
  ROUND(AVG(salary) OVER (PARTITION BY dept), 2) AS avg_sal
FROM employees
ORDER BY dept;
```

> **Key concept:** Multiple window functions in one SELECT. DISTINCT removes row-level duplicates since window functions don't collapse rows.

---

## Q44 — Find employees hired immediately after another employee.
**Topic:** LEAD() / LAG() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐

```sql
WITH ordered AS (
  SELECT emp_id, emp_name, hire_date,
    LAG(emp_name) OVER (ORDER BY hire_date) AS prev_emp,
    LAG(hire_date) OVER (ORDER BY hire_date) AS prev_hire
  FROM employees
)
SELECT emp_name, hire_date, prev_emp, prev_hire
FROM ordered
WHERE prev_emp IS NOT NULL;
```

> **Key concept:** LAG gives the row before current row ordered by hire_date. 'Immediately after' = next row in hire_date order.

---

## Q45 — Display employees with continuous salary ranking (no gaps).
**Topic:** DENSE_RANK() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
SELECT emp_id, emp_name, dept, salary,
  DENSE_RANK() OVER (
    PARTITION BY dept ORDER BY salary DESC
  ) AS salary_rank
FROM employees
ORDER BY dept, salary_rank;
```

> **Key concept:** DENSE_RANK: 1,1,2,3 (no gaps). RANK: 1,1,3,4 (gaps). ROW_NUMBER: 1,2,3,4 (unique). Key interview distinction!

---

## Q46 — Find departments where every employee earns more than 60,000.
**Topic:** HAVING + MIN() | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐

```sql
SELECT dept
FROM employees
GROUP BY dept
HAVING MIN(salary) > 60000;
```

> **Key concept:** If MIN salary in dept > 60k, then ALL employees earn more than 60k. Elegant use of MIN with HAVING.

---

## Q47 — Find employees whose salary is above the company median salary.
**Topic:** Window Functions / Median | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐

```sql
WITH median_cte AS (
  SELECT PERCENTILE_CONT(0.5)
    WITHIN GROUP (ORDER BY salary) AS median_sal
  FROM employees
)
SELECT e.emp_id, e.emp_name, e.salary
FROM employees e, median_cte m
WHERE e.salary > m.median_sal;
```

> **Key concept:** PERCENTILE_CONT(0.5) = median. Available in PostgreSQL/SQL Server. MySQL: use subquery with ROW_NUMBER approach.

---

## Q48 — Find the longest-serving employee in each department.
**Topic:** ROW_NUMBER + PARTITION BY | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH ranked AS (
  SELECT emp_id, emp_name, dept, hire_date,
    ROW_NUMBER() OVER (
      PARTITION BY dept ORDER BY hire_date ASC
    ) AS rn
  FROM employees
)
SELECT emp_id, emp_name, dept, hire_date
FROM ranked WHERE rn = 1;
```

> **Key concept:** Oldest hire_date = longest serving. ORDER BY hire_date ASC → earliest date gets rn=1. PARTITION BY dept = per department.

---

## Q49 — Write a CTE to find employees earning above department average.
**Topic:** CTE | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
WITH dept_avg AS (
  SELECT dept,
    AVG(salary) AS avg_sal
  FROM employees
  GROUP BY dept
)
SELECT e.emp_id, e.emp_name, e.dept,
       e.salary, d.avg_sal
FROM employees e
JOIN dept_avg d ON e.dept = d.dept
WHERE e.salary > d.avg_sal
ORDER BY e.dept, e.salary DESC;
```

> **Key concept:** CTE (WITH clause) = temporary named result set. Cleaner than nested subqueries. CTE is evaluated once and reusable.

---

## Q50 — Find employees who share the same manager.
**Topic:** GROUP BY / Self Join | **Difficulty:** ⭐⭐⭐⭐⭐ | **Frequency:** ⭐⭐⭐⭐⭐

```sql
-- Approach 1: Self Join
SELECT a.emp_name AS emp1, b.emp_name AS emp2,
       a.manager_id
FROM employees a
JOIN employees b
  ON a.manager_id = b.manager_id
  AND a.emp_id < b.emp_id;

-- Approach 2: GROUP BY
SELECT manager_id, COUNT(*) AS reportees,
       STRING_AGG(emp_name, ', ') AS emp_list
FROM employees
WHERE manager_id IS NOT NULL
GROUP BY manager_id
HAVING COUNT(*) > 1;
```

> **Key concept:** a.emp_id < b.emp_id prevents duplicate pairs (A,B) and (B,A). STRING_AGG lists employees per manager.

---