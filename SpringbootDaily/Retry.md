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
   +---- Service Responded ----> [ Success ]
   |                              (Normal response returned)
   |
   +---- No Response / Failure ---->
                    |
                    v
           [ Retry Logic ]
           (Retry 2-3 times)
                    |
                    +---- Success ----> [ Response Returned ]
                    |DF
                    +--- Still Failing ---->
                                   |
                                   v
                         [ Circuit Breaker OPEN ]
                         (Stop useless calls temporarily)
                                   |
                                   v
                         [ Fallback Response ]
                         (Return cached/default data)
                                   |
                                   v
                            Client Gets Response

After Some Time
        |
        v
[ Circuit Breaker HALF-OPEN ]
(Try service again carefully)
        |
        +---- Success ----> [ CLOSE Circuit ]
        |                    (Resume normal traffic)
        |
        +---- Failure ----> [ OPEN Again ]
                             (Block calls again)