                         CUSTOMER REQUEST
                               │
                               ▼
                    ┌─────────────────────┐
                    │  DB TRANSACTION     │
                    │     ATOMIC          │
                    └─────────┬───────────┘
                              │
                 ┌────────────┴────────────┐
                 │                         │
                 ▼                         ▼
       ┌──────────────────┐       ┌──────────────────┐
       │ document_processing│       │   outbox_event   │
       │------------------│       │------------------│
       │ id               │       │ id               │
       │ status=PROCESSING│       │ event_type       │
       │ version          │       │ status=PENDING   │
       └────────┬─────────┘       └────────┬─────────┘
                │                          │
                └────────────┬─────────────┘
                             │
                          COMMIT
                             │
                             ▼
                    ┌─────────────────┐
                    │ DB COMMIT SUCCESS│
                    └────────┬────────┘
                             │
                    💥 APPLICATION CRASH
                             │
                             ▼
              ┌────────────────────────────┐
              │ outbox_event = PENDING     │
              │ event is NOT LOST          │
              └──────────────┬─────────────┘
                             │
                    APPLICATION RECOVERS
                             │
                             ▼
                  ┌─────────────────────┐
                  │  OUTBOX PUBLISHER   │
                  │ reads PENDING event │
                  └──────────┬──────────┘
                             │
                             ▼
                        ┌─────────┐
                        │  KAFKA  │
                        └────┬────┘
                             │
                             ▼
                       ┌──────────┐
                       │ CONSUMER │
                       └────┬─────┘
                            │
                            ▼
                    ┌─────────────────┐
                    │      AZURE      │
                    │ Document        │
                    │ Intelligence    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ RESULT / STATUS │
                    │      UPDATE     │
                    └─────────────────┘


             ───────────────────────────────────
              CORE OUTBOX GUARANTEE
             ───────────────────────────────────

        DB STATE + OUTBOX EVENT
                 │
                 │ SAME TRANSACTION
                 ▼
        ┌───────────────────────┐
        │ BOTH COMMIT           │
        │        OR             │
        │ BOTH ROLLBACK         │
        └───────────────────────┘
                 │
                 ▼
        💥 Crash AFTER COMMIT
                 │
                 ▼
        EVENT STILL EXISTS
        IN OUTBOX TABLE
                 │
                 ▼
        EVENT CAN BE PUBLISHED
        AFTER RECOVERY