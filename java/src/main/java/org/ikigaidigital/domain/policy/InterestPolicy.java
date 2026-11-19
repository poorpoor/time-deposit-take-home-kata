package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

/**
 * Strategy for calculating unrounded monthly interest for a specific time deposit plan.
 *
 * <p>Each implementation owns its eligibility criteria and interest formula so new plans
 * or future complexities (e.g., tiered balances or withdrawal-based penalties) can be added
 * without modifying {@link org.ikigaidigital.TimeDepositCalculator}.
 */
public interface InterestPolicy {

    int DEFAULT_GRACE_PERIOD_DAYS = 30;
    double MONTHS_IN_YEAR = 12.0;

    String planType();

    double calculateMonthlyInterest(TimeDeposit timeDeposit);

    static double monthlyInterest(Double balance, double annualRate) {
        return balance * annualRate / MONTHS_IN_YEAR;
    }
}
