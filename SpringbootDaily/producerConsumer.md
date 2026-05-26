[ Producer Sends Event ]
            |
            v
[ Consumer Receives Event ]
            |
            v
[ Validate Event ]
(Check schema + rules)
            |
      +-----+-----+
      |           |
   Valid       Invalid
      |           |
      v           v
[ Check       [ Reject Event ]
 Idempotency ]   (Log Error)
      |
 +----+----+
 |         |
New      Duplicate
 |         |
 v         v
[ Process ] [ Ignore ]
 Event       Event
      |
      v
[ Monitor Errors ]
      |
      v
Too Many Bad Events?
      |
 +----+----+
 |         |
 No       Yes
 |         |
 v         v
Continue  [ Circuit Breaker ]
Normal    Stop Consumption
Flow