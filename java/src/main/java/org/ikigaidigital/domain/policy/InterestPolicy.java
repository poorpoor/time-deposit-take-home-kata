package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Strategy for calculating unrounded monthly interest for a specific time deposit plan.
 *
 * <p>Each implementation owns its eligibility criteria and interest formula so new plans
 * or future complexities (e.g., tiered balances or withdrawal-based penalties) can be added
 * without modifying {@link org.ikigaidigital.TimeDepositCalculator}.
 */
public interface InterestPolicy {

    int DEFAULT_GRACE_PERIOD_DAYS = 30;
    BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

    String planType();

    BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit);

    static BigDecimal monthlyInterest(Double balance, BigDecimal annualRate) {
        return BigDecimal.valueOf(balance)
            .multiply(annualRate)
            .divide(MONTHS_IN_YEAR, MathContext.DECIMAL128);
    }
}
