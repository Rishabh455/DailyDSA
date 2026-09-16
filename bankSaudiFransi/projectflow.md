  ┌────────────────────────────────────────────────────────────────────────┐
  │                        CUSTOMER-FACING CHANNELS                        │
  │  [Use Case 1]          [Use Case 2]          [Use Case 3]          [Use Case 4]   │
  │ Mobile/Web App      Transfers/Payments     Loan & Credit Engine   Profile Updates  │
  │ (Login/Pwd Reset)   (High-Value & AML)     (Fresh SAMA KYC)       (Mobile/Address) │
  └───────┬──────────────────────┬──────────────────────┬──────────────────────┬───┘
          │                      │                      │                      │
          │ (Request)            │ (Request)            │ (Request)            │ (Request)
          ▼                      ▼                      ▼                      ▼
  ┌────────────────────────────────────────────────────────────────────────┐
  │                 🔒 BSF ID VERIFICATION MICROSERVICE (API GATEWAY)       │
  │                        (The Central Identity Hub)                      │
  │   - Security & Validation  - Fraud Prevention  - SAMA Compliance Guard │
  └───────▲──────────────────────▲──────────────────────▲──────────────────────▲───┘
          ▲                      ▲                      ▲                      ▲
          │ (Request)            │ (Request)            │ (Request)            │ (Request)
          │                      │                      │                      │
  ┌───────┴──────────────────────┴──────────────────────┴──────────────────────┴───┘
  │ Branch CRM/Tellers    Call Center (IVR)     Open Banking Gateway    Batch Jobs     │
  │ (Walk-in Scan)       (Phone Authentication)  (Third-Party Fintech)  (Nightly Re-KYC)│
  │  [Use Case 5]          [Use Case 6]          [Use Case 7]          [Use Case 8]   │
  │                         INTERNAL & BACKEND SYSTEMS                             │
  └────────────────────────────────────────────────────────────────────────┘
                                         │
                                         ▼ (Secure Government Sync)
                    ┌──────────────────────────────────────────┐
                    │ 🏛️ GOVERNMENT ID REGISTRY (Yakeen/Absher) │
                    └──────────────────────────────────────────┘
