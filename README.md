# XA Bank Time Deposit — Java 17 Solution

Refactored Java solution for the XA Bank Time Deposit kata built with **Spring Boot 3.4**, **Hexagonal Architecture (Ports & Adapters)**, **Contract-First OpenAPI 3.0**, **PostgreSQL + Flyway**, **Testcontainers**, and **ArchUnit**.

---

## 1. Quick Start & Triggering Endpoints via Swagger UI

### Prerequisites
- **Java 17+** (project compiles with `<maven.compiler.release>17</maven.compiler.release>`)
- **Maven 3.9+**
- **Docker** (for local PostgreSQL and Testcontainers integration tests)

### Step 1 — Start PostgreSQL
```bash
docker compose up -d
```

### Step 2 — Run the Application
```bash
mvn -f java/pom.xml spring-boot:run
```
On startup, Flyway automatically creates the schema (`V1__create_time_deposits_and_withdrawals_tables.sql`) and seeds sample deposits and withdrawals (`V2__seed_sample_time_deposits.sql`) so the endpoints can be tested immediately.

### Step 3 — Trigger Both Endpoints Using Swagger UI
1. Open Swagger UI in your browser: **http://localhost:8080/swagger-ui.html**
   - Swagger UI directly serves the contract-first OpenAPI specification from `/openapi/time-deposit-api.yaml` (`java/src/main/resources/static/openapi/time-deposit-api.yaml`).
2. **Retrieve all time deposits**:
   - Expand **`GET /api/v1/time-deposits`** → click **Try it out** → click **Execute**.
   - Verify `200 OK` returning all 6 seeded deposits (`basic`, `student`, `premium`) with their nested `withdrawals`.
3. **Update all time deposit balances**:
   - Expand **`POST /api/v1/time-deposits/balances/update`** → click **Try it out** → click **Execute**.
   - Verify **`204 No Content`** (recalculates and persists all balances in a single transaction).
4. **Verify updated balances**:
   - Re-run **`GET /api/v1/time-deposits`** and observe updated balances:
     - `basic` (`days = 45`): `1234567.00` → `1235595.81`
     - `basic` (`days = 20`): `5000.00` → `5000.00` (within 30-day grace period)
     - `student` (`days = 120`): `10000.00` → `10025.00`
     - `student` (`days = 366`): `8500.00` → `8500.00` (no interest after 1 year)
     - `premium` (`days = 60`): `50000.00` → `50208.33`
     - `premium` (`days = 45`): `25000.00` → `25000.00` (interest starts after day 45)

> **Alternative via `curl`**:
> ```bash
> curl -s http://localhost:8080/api/v1/time-deposits | jq
> curl -i -X POST http://localhost:8080/api/v1/time-deposits/balances/update
> curl -s http://localhost:8080/api/v1/time-deposits | jq
> ```

---

## 2. Running the Full Test Suite

Ensure Docker is running, then run:

```bash
mvn -f java/pom.xml clean verify
```

This executes all 30 tests across four layers:
1. **Domain Unit & Boundary Tests** (`TimeDepositCalculatorTest`): tests all plan boundary conditions (`days` = `0, 29, 30, 31, 44, 45, 46, 365, 366`), `HALF_UP` rounding, floating-point stability, empty/zero/unknown edge cases, and custom `InterestPolicy` extensibility.
2. **Application Service Tests** (`GetAllTimeDepositsServiceTest`, `UpdateTimeDepositBalancesServiceTest`): behavior-focused tests using a lightweight `InMemoryTimeDepositRepository` fake rather than brittle Mockito interaction verification.
3. **Architecture Enforcement Tests** (`HexagonalArchitectureTest` via ArchUnit): enforces pure Java domain, inward-pointing dependencies, interface-only ports, and adapter isolation.
4. **Vertical-Slice Integration Tests** (`TimeDepositApiIntegrationTest` via Testcontainers PostgreSQL 16): validates Flyway migrations, Hibernate schema validation (`ddl-auto: validate`), both HTTP endpoints, non-idempotent repeated updates, Swagger UI / OpenAPI contract availability, and single-query `LEFT JOIN FETCH` loading (asserting `prepareStatementCount == 1`, preventing N+1 queries).

---

## 3. Architecture (Hexagonal / Ports & Adapters)

```text
java/src/main/java/org/ikigaidigital/
├── TimeDeposit.java                             # Frozen shared class (100% untouched)
├── TimeDepositCalculator.java                   # Refactored (preserved signature & no-arg ctor)
├── TimeDepositApplication.java                  # Spring Boot entry point
├── domain/                                      # Pure Java — zero Spring/JPA/Jackson imports
│   ├── model/
│   │   ├── Withdrawal.java                      # Immutable Java 17 record
│   │   └── TimeDepositWithWithdrawals.java      # Read model preserving frozen TimeDeposit
│   └── policy/
│       ├── InterestPolicy.java                  # Strategy interface
│       ├── InterestPolicyRegistry.java          # O(1) lookup, duplicate guard
│       ├── BasicInterestPolicy.java             # 1%/12 when days > 30
│       ├── StudentInterestPolicy.java           # 3%/12 when 30 < days < 366
│       └── PremiumInterestPolicy.java           # 5%/12 when days > 45
├── application/                                 # Use cases & port contracts
│   ├── port/in/
│   │   ├── GetAllTimeDepositsUseCase.java
│   │   └── UpdateTimeDepositBalancesUseCase.java
│   ├── port/out/
│   │   └── TimeDepositRepository.java
│   └── service/
│       ├── GetAllTimeDepositsService.java       # @Transactional(readOnly = true)
│       └── UpdateTimeDepositBalancesService.java # @Transactional single unit of work
└── infrastructure/                              # Adapters & framework wiring
    ├── config/
    │   └── TimeDepositBeanConfig.java           # Explicit bean wiring (auto-collects InterestPolicy beans)
    └── adapter/
        ├── in/web/
        │   ├── TimeDepositController.java       # Implements generated OpenAPI TimeDepositsApi
        │   └── TimeDepositWebMapper.java
        └── out/persistence/
            ├── TimeDepositJpaEntity.java
            ├── WithdrawalJpaEntity.java
            ├── SpringDataTimeDepositRepository.java  # LEFT JOIN FETCH avoiding N+1
            ├── TimeDepositPersistenceAdapter.java
            └── TimeDepositPersistenceMapper.java
```

### Extensibility for Future Interest Rules
Adding a new plan type or more complex calculation rule (e.g., tiered balance thresholds or custom grace periods) requires only:
1. Implementing `InterestPolicy` in a new class, and
2. Registering it as a `@Bean` in `TimeDepositBeanConfig` (automatically injected into `InterestPolicyRegistry`).

`TimeDepositCalculator` does not need to be modified (Open/Closed Principle).

---

## 4. Key Engineering Decisions & Assumptions

1. **Frozen Contracts Preserved**:
   - `org.ikigaidigital.TimeDeposit` is completely unmodified.
   - `org.ikigaidigital.TimeDepositCalculator` keeps its package, public `updateBalance(List<TimeDeposit> xs)` signature, and public no-arg constructor `TimeDepositCalculator()` (plus an overloaded DI constructor accepting `InterestPolicyRegistry`).
2. **Strictly Two Business API Endpoints**:
   - `GET /api/v1/time-deposits` → `200 OK` with JSON array of `{ id, planType, balance, days, withdrawals }`.
   - `POST /api/v1/time-deposits/balances/update` → `204 No Content`, no request body.
   - **Why `POST` instead of `PUT`?** Accruing monthly interest mutates balances on every invocation and is intentionally **non-idempotent** (calling it twice accrues two months of interest). This behavior is explicitly documented in the OpenAPI contract.
   - No extra business or actuator endpoints were added.
3. **Monetary Precision (`BigDecimal`)**:
   - The original `TimeDepositCalculator` performed `double` arithmetic and used `new BigDecimal(interest)` (which captures binary IEEE-754 inaccuracies, e.g., `100.10 + 0.08 = 100.17999999999999` and `0.015` rounding down to `0.01`).
   - All internal interest calculations and `HALF_UP` rounding (scale 2) now use `BigDecimal`, converting back to `Double` only at the frozen `TimeDeposit.setBalance(Double)` boundary while preserving every business rule.
4. **Exact Database Schema & N+1 Prevention**:
   - PostgreSQL tables and columns match the specification verbatim (`"timeDeposits"`, `"planType"`, `withdrawals`, `"timeDepositId"`), enforced via Flyway `V1`, `PhysicalNamingStrategyStandardImpl`, and Hibernate `ddl-auto: validate`.
   - `spring.jpa.open-in-view` is disabled (`false`), and `GET /api/v1/time-deposits` fetches deposits and withdrawals in a single `LEFT JOIN FETCH` query (verified in `TimeDepositApiIntegrationTest`).
5. **Separated Seed Data**:
   - Because the API exposes only two endpoints (no creation endpoint), local startup loads sample data from `classpath:db/seed` so Swagger UI works out of the box. Integration tests override `spring.flyway.locations=classpath:db/migration` to test against a clean schema.

---

## 5. AI-Assisted Development Setup & Workflow

### Tools & Environment
- **Agentic IDE / Coding Assistant**: Google Antigravity with Gemini.
- **Verification Loop**: Maven (`mvn -f java/pom.xml test` / `clean verify`), ArchUnit, and PostgreSQL Testcontainers run after every stage before committing.

### Reproducible AI Harness (Committed in Repository)
To ensure consistent architectural constraints and reproducible AI behavior across sessions, the repository includes:
- [`AGENTS.md`](AGENTS.md) — repository-level rules encoding non-negotiable constraints (frozen `TimeDeposit`/`updateBalance`, strictly 2 endpoints, exact DB schema, Hexagonal layer boundaries, `BigDecimal` policy, testing standards, and atomic commit conventions).
- [`.ai/workflows/01-characterization-tests.md`](.ai/workflows/01-characterization-tests.md) — boundary test checklist executed before touching `TimeDepositCalculator`.
- [`.ai/workflows/02-hexagonal-review.md`](.ai/workflows/02-hexagonal-review.md) — architecture, persistence, and API contract review checklist.
- [`.ai/workflows/03-pre-submission-review.md`](.ai/workflows/03-pre-submission-review.md) — final dead-code, test suite, and submission verification checklist.

### Summary of AI-Assisted vs. Human-Directed Work
- **Human-directed decisions & review**: API contract design (`POST` non-idempotency semantics, `204 No Content`), Hexagonal package boundaries, Java 17 target compatibility, Docker/PostgreSQL setup, stage-by-stage approval, and final line-by-line review (including removing unused JPA constructors/getters and redundant mapping code).
- **AI-assisted implementation**: Drafting the characterization & boundary tests, scaffolding the `InterestPolicy` strategy classes, OpenAPI 3.0 contract and Maven codegen configuration, JPA/Flyway persistence adapter, ArchUnit rules, Testcontainers vertical-slice tests, and documentation — each verified immediately by Maven tests and committed atomically.

---

<details>
<summary>Original Assignment Requirements</summary>

### Context
A junior developer implemented domain logic for a time deposit system but did not complete the API functionality. Your task is to refactor the existing codebase to implement all required functionalities based on the provided business requirements, ensuring no breaking changes occur.

### Requirements
1. **API Endpoints**:
   - Create a RESTful API endpoint to update the balances of all time deposits in the database.
   - Create a RESTful API endpoint to retrieve all time deposits (`id`, `planType`, `balance`, `days`, `withdrawals`).
2. **Database Setup**:
   - `timeDeposits`: `id` (PK, Integer), `planType` (String, required), `days` (Integer, required), `balance` (Decimal, required).
   - `withdrawals`: `id` (PK, Integer), `timeDepositId` (FK, Integer, required), `amount` (Decimal, required), `date` (Date, required).
3. **Interest Calculation**:
   - Basic Plan: 1% interest; Student Plan: 3% interest (no interest after 1 year); Premium Plan: 5% interest (interest starts after 45 days).
   - No interest for the first 30 days for any existing plans.
4. **Refactoring Constraints**:
   - Do not introduce breaking changes to `TimeDeposit` or modify `updateBalance` method signature.
   - Ensure the design is extensible for future complexities in interest calculations.
5. **Code Quality**: Adhere to SOLID principles, design patterns, and clean code practices.
6. **AI-Assisted Development**: Set up and document a practical, reproducible AI harness/workflow.

</details>
