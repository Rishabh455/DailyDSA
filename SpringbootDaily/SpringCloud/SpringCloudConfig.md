SPRING CLOUD CONFIG — SHORT INTERVIEW REVISION

1. What is Spring Cloud Config?

Spring Cloud Config is used for centralized configuration management in microservices.

Instead of storing configs inside every service:

* all configs are stored centrally
* services fetch configs from Config Server

---

2. Why Do We Need It?

* centralized config management
* easier environment handling
* no need to rebuild app for config changes
* dynamic config refresh
* better scalability

---

3. Basic Flow

Git Repository
↓
Config Server
↓
Microservices fetch configs

---

4. Main Components

* Config Server
* Config Client
* Git Repository

---

5. Dynamic Config Refresh

Use:
@RefreshScope

Refresh config without restarting service.

---

6. Secure Sensitive Properties

Sensitive configs:

* DB passwords
* API keys
* secrets

Never hardcode them.

Use:

* Encryption
* Vault
* AWS Secrets Manager

---

7. MOST IMPORTANT INTERVIEW ANSWER

Spring Cloud Config is used for centralized externalized configuration management in microservices architecture.

Configurations are stored centrally, usually in a Git repository, and microservices fetch them through the Config Server.

It supports dynamic configuration refresh and helps securely manage sensitive properties like passwords and API keys using encryption or secret management tools.
