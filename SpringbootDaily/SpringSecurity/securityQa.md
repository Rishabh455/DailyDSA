We use @PreAuthorize when authorization can be decided before method execution, like role-based access. We use @PostAuthorize when authorization depends on the returned object or fetched data
| Feature               | @PreAuthorize           | @PostAuthorize         |
| --------------------- | ----------------------- | ---------------------- |
| Check Timing          | Before method execution | After method execution |
| Access return object? | No                      | Yes                    |
| Performance           | Better                  | Slightly slower        |
| Commonly used?        | Very common             | Less common            |


"In a Flipkart-like application, I would use @PreAuthorize for role-based actions such as adding or deleting products, where only an Admin should have access. Since the decision can be made before executing the method, @PreAuthorize is the best choice.

I would use @PostAuthorize for scenarios like viewing an order. The order must first be fetched from the database, and then we verify whether the logged-in user is the owner of that order using the returned object."
