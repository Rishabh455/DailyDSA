                    Client Request
                           |
                           v
              [ Service A Calls Service B ]
                 (Main API call starts)
                           |
                           v
                    [ Timeout Check ]
                 (Don’t wait forever)
                           |
          +----------------+----------------+
          |                                 |
          | Service Responded               | Failure / Timeout
          |                                 |
          v                                 v
      [ Success ]                  [ Retry Logic Starts ]
 (Normal response returned)        (Retry only 2-3 times)
                                             |
                                             v
                              [ Exponential Backoff ]
                     (Increase wait time after each retry)
                              Example:
                              Retry 1 -> wait 1 sec
                              Retry 2 -> wait 2 sec
                              Retry 3 -> wait 4 sec
                                             |
                                             v
                                   [ Add Jitter ]
                    (Add random delay to avoid retry storm)
                    Example:
                    Retry 2 -> wait 2.3 sec instead of exact 2 sec
                                             |
                          +------------------+------------------+
                          |                                     |
                          | Retry Success                       | Still Failing
                          |                                     |
                          v                                     v
                 [ Response Returned ]            [ Circuit Breaker OPEN ]
                                                    (Stop useless retries
                                                     temporarily)
                                                               |
                                                               v
                                                   [ Fallback Response ]
                                               (Cached/default response)
                                                               |
                                                               v
                                                    Client Gets Response


                  After Cooldown / Recovery Time
                               |
                               v
                  [ Circuit Breaker HALF-OPEN ]
                 (Allow limited test requests)
                               |
                 +-------------+-------------+
                 |                           |
                 | Success                   | Failure
                 |                           |
                 v                           v
          [ CLOSE Circuit ]           [ OPEN Again ]
        (Resume normal traffic)     (Block calls again)






        Retries can accidentally overload an already failing service because all clients retry together, creating a retry storm. To fix this, we use exponential backoff to increase retry delays gradually, jitter to randomize retry timing, retry limits to avoid infinite retries, and circuit breakers to stop unnecessary calls when the downstream service is unhealthy.