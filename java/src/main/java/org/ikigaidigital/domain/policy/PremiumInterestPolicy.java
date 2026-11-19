package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

public final class PremiumInterestPolicy implements InterestPolicy {

    public static final String PLAN_TYPE = "premium";
    private static final double ANNUAL_RATE = 0.05;
    private static final int GRACE_PERIOD_DAYS = 45;

    @Override
    public String planType() {
        return PLAN_TYPE;
    }

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        if (timeDeposit.getDays() <= GRACE_PERIOD_DAYS) {
            return 0.0;
        }
        return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), ANNUAL_RATE);
    }
}
