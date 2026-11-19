# Workflow 02: Hexagonal Architecture & Quality Review

Run this checklist after implementing the domain, application, and infrastructure layers.

## Dependency Direction
- [ ] `org.ikigaidigital.TimeDeposit` is completely untouched.
- [ ] `org.ikigaidigital.TimeDepositCalculator` keeps its exact `updateBalance(List<TimeDeposit>)` signature and no-arg constructor.
- [ ] Domain layer has zero imports from `org.springframework.*`, `jakarta.persistence.*`, or `infrastructure.*`.
- [ ] Application layer depends only on `domain.*` and `application.port.*`.
- [ ] Inbound web adapter depends only on `application.port.in.*` and domain read models.
- [ ] Outbound persistence adapter implements `application.port.out.*` and never leaks JPA entities outside `infrastructure.adapter.out.persistence`.

## Persistence & API Hygiene
- [ ] `GET /api/v1/time-deposits` loads deposits and withdrawals in a single query (`LEFT JOIN FETCH`) — no N+1 queries.
- [ ] `POST /api/v1/time-deposits/balances/update` runs in a single `@Transactional` unit of work and returns `204 No Content`.
- [ ] OpenAPI spec defines strictly **2** business endpoints and documents non-idempotency of balance updates.
- [ ] Database tables and columns match the specification (`"timeDeposits"`, `withdrawals`, `"planType"`, `"timeDepositId"`).
- [ ] Local seed data is separated from schema migration so integration tests run on a clean schema.
