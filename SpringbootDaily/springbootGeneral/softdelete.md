In Spring Boot, I would implement soft delete by adding a flag like `deleted` and optional audit fields such as `deletedAt` and `deletedBy` in the entity. Instead of physically deleting the record from the database, I would update the flag to mark the record as deleted.

In Hibernate, we can implement this cleanly using `@SQLDelete` and `@Where`.

```java id="u9gg7s"
@Entity
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class User {
    
    @Id
    private Long id;

    private boolean deleted = false;
}
```

`@SQLDelete` converts the DELETE operation into an UPDATE query, and `@Where` automatically filters out deleted records from all SELECT queries.

This approach is useful for audit history, recovery, and compliance requirements because the data is still preserved in the database. In production, I would also add indexing on the `deleted` column for better query performance.
