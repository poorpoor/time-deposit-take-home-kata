# XA Bank Time Deposit — Java 17 Solution

Java 17 / Spring Boot 3.4 solution for the XA Bank Time Deposit kata using **Hexagonal Architecture (Ports & Adapters)**, **Contract-First OpenAPI 3.0**, **PostgreSQL + Flyway**, **Testcontainers**, and **ArchUnit**.

---

## 1. Quick Start & Swagger UI

### Prerequisites
- **Java 17+** (compiles with `<maven.compiler.release>17</maven.compiler.release>`)
- **Maven 3.9+**
- **Docker** (for local PostgreSQL and Testcontainers integration tests)

### Step 1 — Start PostgreSQL
```bash
docker compose up -d
```

### Step 2 — Start the Application with Demo Data
```bash
mvn -f java/pom.xml spring-boot:run -Dspring-boot.run.profiles=demo
```
- The default profile applies **schema migrations only** (`classpath:db/migration`), so pointing `DB_URL` at any database never inserts demo rows unexpectedly.
- Enabling the `demo` profile additionally loads sample deposits and withdrawals from `classpath:db/seed`.

### Step 3 — Trigger Both Endpoints via Swagger UI
1. Open **http://localhost:8080/swagger-ui.html** (serves `java/src/main/resources/static/openapi/time-deposit-api.yaml` directly).
2. Expand **`GET /api/v1/time-deposits`** → **Try it out** → **Execute** (`200 OK` with 6 seeded deposits and their nested `withdrawals`).
3. Expand **`POST /api/v1/time-deposits/balances/update`** → **Try it out** → **Execute** (`204 No Content`, updates all eligible balances in a single transaction).
4. Re-run **`GET /api/v1/time-deposits`** to verify the updated balances:
   - `basic` (`days = 45`): `1234567.00` → `1235595.81`
   - `basic` (`days = 20`): `5000.00` → `5000.00` (first 30 days)
   - `student` (`days = 120`): `10000.00` → `10025.00`
   - `student` (`days = 366`): `8500.00` → `8500.00` (no interest after 1 year)
   - `premium` (`days = 60`): `50000.00` → `50208.33`
   - `premium` (`days = 45`): `25000.00` → `25000.00` (interest starts after day 45)

> **Via `curl`**:
> ```bash
> curl -s http://localhost:8080/api/v1/time-deposits | jq
> curl -i -X POST http://localhost:8080/api/v1/time-deposits/balances/update
> curl -s http://localhost:8080/api/v1/time-deposits | jq
> ```

---

## 2. Running Tests

With Docker running:

```bash
mvn -f java/pom.xml clean verify
```

Runs **35 tests** across four layers:
- **Domain (`TimeDepositCalculatorTest`, 15)**: boundary days (`0, 29, 30, 31, 44, 45, 46, 365, 366`), legacy rounding compatibility (`basic 18.00 -> 18.01`, `student 6.00 -> 6.01`), edge cases, and custom `InterestPolicy` registration.
- **Application (`GetAllTimeDepositsServiceTest`, `UpdateTimeDepositBalancesServiceTest`, 6)**: in-memory fake repository verifying `BigDecimal` principal updates, zero-interest write skipping, and repeated non-idempotent accruals.
- **Architecture (`HexagonalArchitectureTest`, 5)**: ArchUnit checks enforcing a pure Java domain, inward-only dependencies, interface ports, and adapter isolation.
- **PostgreSQL Integration (`TimeDepositApiIntegrationTest`, 9)**: Testcontainers PostgreSQL 16 + Flyway + Hibernate `ddl-auto: validate` testing both HTTP endpoints, Swagger/OpenAPI serving, single-query `LEFT JOIN FETCH` (`prepareStatementCount == 1`, no N+1), `NUMERIC(19, 2)` precision (`1000000000000000.01`), concurrent `POST` row locking (`SELECT ... FOR UPDATE`), and full transaction rollback on `NUMERIC(19, 2)` overflow.

---

## 3. Architecture & Key Decisions

### Hexagonal Layout (`org.ikigaidigital`)
- **Domain** (`TimeDeposit`, `TimeDepositCalculator`, `domain.model.*`, `domain.policy.*`): pure Java with zero Spring/JPA/Jackson dependencies. `TimeDeposit` is completely untouched; `TimeDepositCalculator` retains its package, no-arg constructor, and `updateBalance(List<TimeDeposit>)` signature.
- **Application** (`application.port.in.*`, `application.port.out.*`, `application.service.*`): inbound use cases (`GetAllTimeDepositsUseCase`, `UpdateTimeDepositBalancesUseCase`), outbound `TimeDepositRepository` port, and transactional services.
- **Infrastructure** (`infrastructure.config.*`, `infrastructure.adapter.in.web.*`, `infrastructure.adapter.out.persistence.*`): contract-first OpenAPI controller implementing generated `TimeDepositsApi`, JPA/PostgreSQL adapter, and explicit bean wiring.

### Key Decisions & Assumptions
1. **Strictly Two Business Endpoints & Non-Idempotent `POST`**:
   - Exposes only `GET /api/v1/time-deposits` (`200 OK`) and `POST /api/v1/time-deposits/balances/update` (`204 No Content`).
   - `POST` is used (and documented in OpenAPI) because repeated calls accrue another month of interest.
2. **Extensible Single-Source Plan Registry**:
   - Interest rules live in `InterestPolicy` implementations (`BasicInterestPolicy`, `StudentInterestPolicy`, `PremiumInterestPolicy`) registered in one place: `InterestPolicyRegistry.defaultRegistry()`, shared by both `new TimeDepositCalculator()` and Spring Boot wiring. Adding a new built-in plan requires adding one policy class and registering it there—without modifying `TimeDepositCalculator`.
3. **Interest Calculation & Database Precision**:
   - Monthly interest follows the original calculator's `double` arithmetic and rounding rules. The rounded interest is then added to the original database balance using `BigDecimal`.
   - Deposits accruing zero interest are not rewritten, and managed entities are updated via `getReferenceById` inside the active transaction without issuing a redundant `SELECT`.
4. **Concurrency & Atomicity**:
   - `findAllForUpdate()` locks rows in deterministic primary-key order (`SELECT ... ORDER BY id ASC FOR UPDATE`) so concurrent `POST` calls serialize without lost updates or deadlocks.
   - Any database failure during flush rolls back all balance changes in the batch (verified in `TimeDepositApiIntegrationTest`).
5. **Exact Schema & No N+1**:
   - Flyway `V1`, `PhysicalNamingStrategyStandardImpl`, and `ddl-auto: validate` enforce exact quoted identifiers (`"timeDeposits"`, `"planType"`, `withdrawals`, `"timeDepositId"`).
   - `open-in-view` is disabled and `GET` loads deposits with withdrawals in a single `LEFT JOIN FETCH` query.

---

## 4. AI-Assisted Development

- **Setup**: Google Antigravity (Gemini) guided by a reproducible repository harness: [`AGENTS.md`](AGENTS.md) (frozen contracts, hexagonal boundaries, schema/API constraints, commit conventions) and workflow checklists in [`.ai/workflows/`](.ai/workflows/).
- **Division of Work**: Human direction covered API design (`POST` + `204`, non-idempotency), hexagonal structure, Java 17 target compatibility, demo-profile isolation, and critical review. AI assisted with generating characterization/integration tests, strategy and adapter scaffolding, OpenAPI YAML, and documentation. Independent review and Maven/Testcontainers runs caught and fixed legacy rounding differences (`basic 18.00` / `student 6.00`), `NUMERIC(19, 2)` precision loss, concurrent lost updates, redundant persistence queries, and dead code—preserved as separate `fix:` / `refactor:` commits in git history.

---

<details>
<summary>Original Assignment Requirements</summary>

- **API Endpoints**: Two REST endpoints — update balances of all time deposits, and retrieve all time deposits (`id`, `planType`, `balance`, `days`, `withdrawals`).
- **Database Schema**: `timeDeposits` (`id`, `planType`, `days`, `balance`) and `withdrawals` (`id`, `timeDepositId`, `amount`, `date`).
- **Interest Rules**: Basic 1%, Student 3% (no interest after 1 year), Premium 5% (starts after 45 days); no interest for the first 30 days for any existing plan.
- **Constraints**: No breaking changes to `TimeDeposit` or `TimeDepositCalculator.updateBalance`; extensible design; SOLID; reproducible AI-assisted workflow.

</details>
