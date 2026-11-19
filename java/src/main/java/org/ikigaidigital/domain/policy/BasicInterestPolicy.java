package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

public final class BasicInterestPolicy implements InterestPolicy {

    public static final String PLAN_TYPE = "basic";
    private static final double ANNUAL_RATE = 0.01;

    @Override
    public String planType() {
        return PLAN_TYPE;
    }

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= DEFAULT_GRACE_PERIOD_DAYS) {
            return 0.0;
        }
        return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), ANNUAL_RATE);
    }
}
