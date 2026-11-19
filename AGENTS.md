# AGENTS.md — XA Bank Time Deposit Kata (Java)

This file defines the repository-level engineering rules, architectural boundaries, and verification workflow for AI-assisted development on the `java/` module.

## 1. Non-Negotiable Constraints

1. **Frozen Public Contracts**
   - `org.ikigaidigital.TimeDeposit` MUST NOT be modified (package, constructor, fields, getters, setter remain intact).
   - `org.ikigaidigital.TimeDepositCalculator` MUST remain in package `org.ikigaidigital`, retain its public no-arg constructor `TimeDepositCalculator()`, and retain the exact method signature `public void updateBalance(List<TimeDeposit> xs)`.
2. **Exact Interest Calculation Rules**
   - No interest for the first 30 days (`days <= 30`) for any existing plan.
   - `basic`: 1% annual / 12 monthly when `days > 30`.
   - `student`: 3% annual / 12 monthly when `days > 30 && days < 366` (no interest from day 366 onward).
   - `premium`: 5% annual / 12 monthly when `days > 45`.
   - Unknown plan types accrue `0` interest (balance unchanged).
   - Monthly interest is rounded to 2 decimal places using `RoundingMode.HALF_UP`.
   - `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` MUST preserve the exact legacy `double` interest calculation and `new BigDecimal(double).setScale(2, RoundingMode.HALF_UP)` behavior (e.g., `basic 18.00 / day 31 -> 18.01`, `student 6.00 / day 31 -> 6.01`).
   - Database persistence MUST preserve full `NUMERIC(19, 2)` `BigDecimal` principals (adding `BigDecimal` monthly interest directly to the persisted principal and skipping writes when interest is zero) and lock rows via `SELECT ... FOR UPDATE` (`ORDER BY id ASC`) to prevent lost updates under concurrent `POST` calls.
3. **Strictly Two Business API Endpoints**
   - `GET /api/v1/time-deposits` → `200 OK` returning JSON array of `{ id, planType, balance, days, withdrawals }`.
   - `POST /api/v1/time-deposits/balances/update` → `204 No Content`, recalculating and persisting all balances in a single transaction (explicitly documented as non-idempotent).
   - Do NOT add extra business endpoints or expose extra management/actuator endpoints.
4. **Exact Database Schema**
   - Table `"timeDeposits"`: `id` (PK, integer), `"planType"` (varchar, not null), `days` (integer, not null), `balance` (numeric/decimal, not null).
   - Table `withdrawals`: `id` (PK, integer), `"timeDepositId"` (FK -> `"timeDeposits"(id)`, integer, not null), `amount` (numeric/decimal, not null), `date` (date, not null).

## 2. Architecture (Hexagonal / Ports & Adapters)

Package root: `org.ikigaidigital`

- **Domain** (`org.ikigaidigital`, `org.ikigaidigital.domain.*`):
  - Pure Java only. Zero dependencies on Spring, Jakarta Persistence, Jackson, or OpenAPI-generated classes.
  - Extensible interest calculation via `InterestPolicy` strategy + `InterestPolicyRegistry`.
- **Application** (`org.ikigaidigital.application.*`):
  - `port.in.*`: inbound use case interfaces.
  - `port.out.*`: outbound repository ports.
  - `service.*`: use case implementations coordinating domain logic and outbound ports.
  - Must NOT depend on `infrastructure.*`.
- **Infrastructure** (`org.ikigaidigital.infrastructure.*`):
  - `config.*`: Spring `@Configuration` wiring domain and application beans.
  - `adapter.in.web.*`: REST controller implementing the OpenAPI-generated interface + mapper. Must NOT access persistence directly.
  - `adapter.out.persistence.*`: JPA entities, Spring Data repository (using `LEFT JOIN FETCH` to avoid N+1), persistence adapter, and mapper.

## 3. Coding & Testing Standards

- **Contract-First OpenAPI**: Maintain `java/src/main/resources/static/openapi/time-deposit-api.yaml` as the single source of truth. Generate server interfaces and DTOs with `openapi-generator-maven-plugin` (`interfaceOnly=true`) and serve the exact same spec via Swagger UI.
- **Target Java Version**: Compile with `<maven.compiler.release>17</maven.compiler.release>`. Prefer Java 17 `record`s over boilerplate POJOs or Lombok.
- **Test-First Refactoring**: Write boundary and characterization tests before refactoring existing domain code (e.g., days `29, 30, 31`, `44, 45, 46`, `365, 366`).
- **Behavior Over Mocks**: Prefer lightweight in-memory fakes for application service unit tests over Mockito interaction verification.
- **Real Infrastructure Tests**: Use Testcontainers (PostgreSQL) + Flyway for full vertical-slice integration tests.
- **Architecture Enforcement**: Keep `HexagonalArchitectureTest` (ArchUnit) green on every change.
- **No Over-Engineering**: Keep abstractions minimal, purposeful, and free of dead code.

## 4. Workflow & Commit Discipline

1. Work in small, verifiable stages following `.ai/workflows/`.
2. Run Maven tests after every stage (`mvn -f java/pom.xml test` / `mvn -f java/pom.xml verify`).
3. Commit each validated stage atomically using Conventional Commits (`chore:`, `test:`, `refactor:`, `build:`, `feat:`, `docs:`).
4. Review every generated line for correctness, clarity, and alignment with the constraints above before committing.
