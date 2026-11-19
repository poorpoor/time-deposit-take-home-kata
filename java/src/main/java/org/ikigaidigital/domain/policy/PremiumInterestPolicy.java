package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

import java.math.BigDecimal;

public final class PremiumInterestPolicy implements InterestPolicy {

    public static final String PLAN_TYPE = "premium";
    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.05");
    private static final int GRACE_PERIOD_DAYS = 45;

    @Override
    public String planType() {
        return PLAN_TYPE;
    }

    @Override
    public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= GRACE_PERIOD_DAYS) {
            return BigDecimal.ZERO;
        }
        return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), ANNUAL_RATE);
    }
}
