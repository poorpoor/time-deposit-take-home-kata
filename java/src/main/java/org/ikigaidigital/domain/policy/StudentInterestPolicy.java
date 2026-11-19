package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

import java.math.BigDecimal;

public final class StudentInterestPolicy implements InterestPolicy {

    public static final String PLAN_TYPE = "student";
    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.03");
    private static final int INTEREST_CUTOFF_DAY = 366;

    @Override
    public String planType() {
        return PLAN_TYPE;
    }

    @Override
    public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
        int days = timeDeposit.getDays();
        if (days <= DEFAULT_GRACE_PERIOD_DAYS || days >= INTEREST_CUTOFF_DAY) {
            return BigDecimal.ZERO;
        }
        return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), ANNUAL_RATE);
    }
}
