We use @PreAuthorize when authorization can be decided before method execution, like role-based access. We use @PostAuthorize when authorization depends on the returned object or fetched data
| Feature               | @PreAuthorize           | @PostAuthorize         |
| --------------------- | ----------------------- | ---------------------- |
| Check Timing          | Before method execution | After method execution |
| Access return object? | No                      | Yes                    |
| Performance           | Better                  | Slightly slower        |
| Commonly used?        | Very common             | Less common            |
