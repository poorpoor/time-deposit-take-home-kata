# Workflow 03: Pre-Submission Final Review

Run before final commit and submission.

## Verification Checklist
- [ ] Full Maven build and test suite passes (`mvn -f java/pom.xml clean verify`), including:
  - Unit & boundary tests (`TimeDepositCalculatorTest`, policy tests, application service tests)
  - Architecture tests (`HexagonalArchitectureTest`)
  - Integration tests (`TimeDepositApiIntegrationTest` via Testcontainers PostgreSQL)
- [ ] Compiled with `<maven.compiler.release>17</maven.compiler.release>` and verified on JDK 17+.
- [ ] Zero dead code, unused imports, unused methods, or debug logging.
- [ ] Git history consists of clean, atomic commits following Conventional Commits.
- [ ] Swagger UI serves the exact OpenAPI contract and both endpoints work end-to-end against PostgreSQL via `docker compose up -d`.
- [ ] `README.md` documents:
  - Architecture & key design assumptions
  - How to start the app and trigger both endpoints via Swagger UI
  - AI-assisted development tools, reproducible harness (`AGENTS.md`, `.ai/workflows/`), and summary of AI vs human contributions
