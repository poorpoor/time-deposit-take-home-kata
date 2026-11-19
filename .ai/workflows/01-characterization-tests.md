# Workflow 01: Characterization & Boundary Testing

Run this workflow **before** refactoring `TimeDepositCalculator`.

## Objective
Create a comprehensive, behavior-focused safety net in `TimeDepositCalculatorTest` that locks down existing domain behavior and exposes any accidental regressions during refactoring.

## Required Test Cases
1. **Common 30-day grace period** (for `basic`, `student`, `premium`):
   - `days = 0`, `days = 29`, `days = 30` -> zero interest accrued.
2. **Basic plan (`basic`)**:
   - `days = 31` -> 1% annual / 12 monthly accrued.
   - `days = 45`, `days = 366`, `days = 1000` -> continues accruing 1% / 12.
3. **Student plan (`student`)**:
   - `days = 31`, `days = 365` -> 3% annual / 12 monthly accrued.
   - `days = 366`, `days = 400` -> zero interest accrued ("no interest after 1 year").
4. **Premium plan (`premium`)**:
   - `days = 31`, `days = 44`, `days = 45` -> zero interest accrued.
   - `days = 46`, `days = 366` -> 5% annual / 12 monthly accrued.
5. **Edge cases**:
   - Empty list -> no error.
   - Zero balance -> remains `0.0`.
   - Unknown plan type -> zero interest accrued, balance unchanged.
   - Batch with mixed plans -> each deposit updated independently without state leaking across iterations.
   - Rounding precision (`HALF_UP` to 2 decimal places).

## Verification
- Run `mvn -f java/pom.xml test` against the **unmodified** `TimeDepositCalculator`.
- Every test must pass before refactoring begins.
