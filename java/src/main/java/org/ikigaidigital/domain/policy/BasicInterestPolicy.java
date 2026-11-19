package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

import java.math.BigDecimal;

public final class BasicInterestPolicy implements InterestPolicy {

    public static final String PLAN_TYPE = "basic";
    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.01");

    @Override
    public String planType() {
        return PLAN_TYPE;
    }

    @Override
    public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= DEFAULT_GRACE_PERIOD_DAYS) {
            return BigDecimal.ZERO;
        }
        return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), ANNUAL_RATE);
    }
}
