# iSLDevs Banking Platform

**An evolving enterprise banking platform built with Java, Spring Boot, microservices, double-entry accounting, event-driven architecture, and secure APIs.**

This repository explores how a banking backend can grow from core customer, account, and transfer workflows into a broader enterprise banking architecture. The goal is to model realistic financial processes, clear service ownership, reliable money movement, and operational controls while keeping the implementation understandable and independently testable.

> [!IMPORTANT]
> **Project status: active learning and architecture development.** The features described under **Implemented today** exist in the repository. The enterprise banking capabilities under **Target roadmap** are design goals, not implemented integrations or claims of production readiness. This platform must not be used for real customer funds.

## Implemented today

Eight independent Spring Boot services are coordinated through a Gradle composite build.

| Service | Port | Responsibility | Database |
| --- | ---: | --- | --- |
| `gateway-service` | `8080` | API routing, JWT validation, correlation IDs, Redis-backed rate limiting | None |
| `auth-service` | `8081` | Identity, OAuth2/OIDC authorization and token issuance | `auth` |
| `customer-service` | `8082` | Customer profiles, basic KYC review and customer status | `customer` |
| `account-service` | `8083` | Account products, account opening, funding instructions, balances, financial operations and ledger entries | `account` |
| `transfer-service` | `8084` | Transfer lifecycle, idempotency, recovery and transactional outbox | `transfer` |
| `notification-service` | `8085` | Kafka-driven in-app notifications | `notification` |
| `audit-service` | `8086` | Kafka-driven transfer audit history | `audit` |
| `config-service` | `8888` | Spring Cloud Config using bundled environment configuration | None |

Each stateful business service owns its logical PostgreSQL database and Flyway migrations; services do not query one another's tables directly. Local Docker Compose runs these logical databases on one PostgreSQL instance.

### Current system architecture

```text
                            Web / Mobile / Postman
                                     |
                                     v
                             Gateway :8080
                          JWT / routing / limits
                              |          |
                              |          +------> Redis
                 +------------+------------------+
                 |            |                  |
                 v            v                  v
           Auth :8081    Customer :8082     Transfer :8084
           auth DB       customer DB        transfer DB
                              |                  |
                              | customer checks  | financial command (HTTP)
                              v                  v
                         Account :8083 <---------+
                         account DB / ledger
                                                 
            Transfer outbox ---> Kafka ---> Notification :8085
                                  |
                                  +-------> Audit :8086

           Config :8888 -> environment configuration for services
```

**Financial posting boundary:** `account-service` owns account balances and ledger entries. `transfer-service` owns the transfer request, workflow state, client idempotency, recovery, and outbox. Notification and audit events are downstream effects; they are not the accounting source of truth.

Read the canonical [architecture guide](docs/wiki/Architecture.md), [service catalogue](docs/wiki/Services.md), and [database ownership and reconciliation guide](docs/DATABASES.md) for details.

## Banking flows available in the demo

### 1. Customer onboarding and account opening

1. Register/authenticate a user using the OAuth2/OIDC flow.
2. Create a customer record, initially pending basic KYC.
3. Approve KYC with an authorized user.
4. Open a product-based USD or KHR account with an `Idempotency-Key`.
5. When an initial deposit is requested, create an account in `PENDING_FUNDING` and a funding instruction; do not immediately increase its balance.
6. Post the simulated settled instruction with a trusted service credential and external settlement reference, creating financial entries and activating the account within a database transaction.

### 2. Internal account-to-account transfer

```text
Client request + Idempotency-Key
             |
             v
     Transfer Service
     persist PENDING state
             |
             v
     Account Service
     validate accounts / balances
     post debit + credit
     commit financial operation
             |
             v
     Transfer COMPLETED
     + outbox event (same transfer DB transaction)
             |
             v
           Kafka
           /   \
 Notification   Audit
```

The Account Service uses the transfer/operation identity for posting idempotency. Ambiguous calls remain eligible for recovery/reconciliation instead of blindly generating a new financial operation. Current transfer currencies are **USD** and **KHR**, not arbitrary international currencies.

### 3. Ledger and account operations

The current demo stores debit and credit entries for financial operations, updates ledger/available balances inside transactional account operations, and exposes account/ledger queries. The roadmap includes a stronger immutable journal model, a full chart of accounts, general ledger, holds, reversals, accounting periods, and formal end-of-day reconciliation. The existing ledger should not be confused with a completed enterprise general ledger.

See the [API reference](docs/wiki/API-Reference.md) and [security guide](docs/wiki/Security-and-Operations.md).

## Target roadmap

The following capabilities are **planned**, not part of the present runtime. They are grouped by business responsibility rather than assuming every box must become a separate microservice.

| Stage | Target banking capabilities |
| --- | --- |
| **1. Financial foundation** | Strengthen authorization/idempotency, immutable journals, posting rules, holds, reversals, accounting invariants, concurrency testing, and reconciliation |
| **2. Core banking** | Chart of accounts, general ledger, product/fee and interest rules, account lifecycle, value dates, business dates, and end-of-day processing |
| **3. Customer and risk** | Enhanced KYC/CDD, sanctions and PEP screening, transaction limits, fraud monitoring, case management, maker-checker approval |
| **4. Domestic payments** | Payment initiation/approval, payment orchestration, clearing and settlement adapters, rejects/returns, external settlement and reconciliation |
| **5. International banking** | ISO 20022/SWIFT integration, correspondent banking, Nostro/Vostro accounting, FX, multi-currency rules, legal entities, branches and booking centers |
| **6. Banking expansion** | Lending and repayment schedules, regulatory reporting, operational resilience, broader audit coverage and reporting |

### Intended international payment flow

```text
Payment initiation
   -> ownership, limits and fraud checks
   -> sanctions / AML screening and approval
   -> FX quote (when applicable)
   -> payment orchestration
   -> payment message / rail adapter (e.g. ISO 20022)
   -> external clearing and settlement confirmation
   -> financial posting and reconciliation
   -> customer notification and regulatory/audit records
```

The sequence is a **target business workflow**. Actual bank/payment-rail rules vary by jurisdiction and scheme.

## Technology and design principles

- **Backend:** Java 17 toolchain, Spring Boot 3.3.x, Spring Security, Spring Authorization Server, Spring Cloud Config.
- **Persistence:** PostgreSQL, Spring Data JPA/JDBC, Flyway; database-per-service ownership.
- **Messaging:** Apache Kafka, transactional outbox and idempotent downstream processing.
- **Cache / edge:** Redis for gateway rate limiting.
- **Delivery:** independent Gradle builds (composite build), Docker Compose, GitHub Actions, CodeQL and Spotless.
- **Observability:** Spring Actuator, Micrometer tracing/metrics and OpenTelemetry dependencies.
- **Architecture:** business bounded contexts, explicit API/event contracts, synchronous calls for selected commands and asynchronous events for downstream work.

**Design rules:** financial postings must balance, retries must not duplicate money movement, immutable history must be corrected with reversals rather than deletion, and failure recovery must use stable operation identifiers. Use security boundaries in each service, not solely at the gateway.

## Getting started

### Requirements

- JDK **17** (current service Gradle toolchain and CI build).
- Docker Engine with Docker Compose for the full local runtime.
- Git.
- The committed **Gradle wrapper** (currently configured for Gradle 8.13); no separate Gradle installation is required for normal local commands.

> Some older documentation still mentions Java 21 or an absent Gradle wrapper. The build files, wrapper and CI are the current source of truth. The CI workflow currently uses Gradle 8.10.2; these versions should be aligned in a follow-up maintenance change.

### Clone and build

```bash
git clone https://github.com/isldevs/banking-platform.git
cd banking-platform
./gradlew buildAll
./gradlew testAll
./gradlew checkAll
```

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`.

### Configure the local environment

On **Windows PowerShell**, the included setup helper creates local environment values and JWT signing material:

```powershell
.\scripts\setup-local.ps1
```

This helper prepares local configuration; **start Docker Compose separately**.

For other environments, create `infrastructure/.env` from `infrastructure/.env.example`, replace all placeholder secrets with local-only values, and provision the expected JWT keystore/public certificate under `infrastructure/secrets/` before startup. Do not commit either secrets or generated credentials.

### Start the services

```bash
cd infrastructure
docker compose up --build -d
docker compose ps
```

Local host endpoints include:

| Component | Address |
| --- | --- |
| Gateway | `http://localhost:8080` |
| Auth | `http://localhost:8081` |
| Config | `http://localhost:8888` |
| PostgreSQL | `localhost:5439` |
| Kafka | `localhost:29092` |
| Redis | `localhost:6379` |

Other business services are reachable inside the Compose network. Their APIs are routed through the Gateway. See [Getting Started](docs/wiki/Getting-Started.md) for service startup and application-level examples.

## API development and testing

- Use the [API reference](docs/wiki/API-Reference.md) for the current OAuth2 endpoints, permissions, request/response contracts and banking operations.
- The intended API development workflow is **Postman Native Git**. Open the cloned repository in Postman Desktop, connect a workspace to the repository root, let Postman generate its own local resource structure, review changes in Git, and synchronize through pull requests.
- **Repository state:** the current tree does not contain committed `docs/postman/` exports or Postman Native Git resource directories. Do not assume the collection is available until those files are added.
- Treat `Idempotency-Key` and financial operation identity as part of request contract testing. Never commit bearer tokens, signing material, client secrets or service keys.

## Repository structure

```text
banking-platform/
├── auth-service/
├── customer-service/
├── account-service/
├── transfer-service/
├── notification-service/
├── audit-service/
├── gateway-service/
├── config-service/
├── contracts/
├── infrastructure/
├── scripts/
├── docs/
├── gradle/
├── gradlew
├── gradlew.bat
├── build.gradle
├── settings.gradle
└── README.md
```

Each service has its own `build.gradle` and `settings.gradle`. The root Gradle composite build orchestrates independent service builds; it is not a shared-JPA-model monolith.

## Documentation

The canonical versioned documentation lives in `docs/wiki/`:

- [Project Wiki](docs/wiki/Home.md)
- [Architecture](docs/wiki/Architecture.md)
- [Services](docs/wiki/Services.md)
- [Getting Started](docs/wiki/Getting-Started.md)
- [Authentication and OAuth2](docs/wiki/Authentication-and-OAuth2.md)
- [API Reference](docs/wiki/API-Reference.md)
- [Configuration](docs/wiki/Configuration.md)
- [Security and Operations](docs/wiki/Security-and-Operations.md)
- [Development and Delivery](docs/wiki/Development-and-Delivery.md)
- [Database ownership and reconciliation](docs/DATABASES.md)
- [Gradle composite build](docs/GRADLE_COMPOSITE_BUILD.md)
- [Architecture/package conventions](docs/architecture/package-structure.md)

## Security and current limitations

This is a **development/demo system**, not a production banking solution. In particular:

- The local funding/settlement configuration uses seeded demo clearing accounts; it does not integrate with real external settlement.
- `X-Service-Key` is a demonstration service-to-service credential, not a substitute for workload identity, mTLS, scoped authorization and proper key management.
- A complete enterprise posting engine/general ledger, AML screening, clearing rails, regulated reporting, and international payment settlement **are not implemented**.
- More integration, concurrency, security and disaster-recovery testing is required. A successful unit build alone does not establish financial correctness.
- Some existing documentation and CI references are out of sync with the current tree (for example, missing Postman exports and demo-flow scripts); track and resolve these before relying on end-to-end automation.

Never use actual banking credentials, regulated customer data or real funds with this project.

---

**Maintained by [iSLDevs](https://github.com/isldevs)** · [Repository](https://github.com/isldevs/banking-platform) · Learning, design and implementation of realistic banking backend workflows.
