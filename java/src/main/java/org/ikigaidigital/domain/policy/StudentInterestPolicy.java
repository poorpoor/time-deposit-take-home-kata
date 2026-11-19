package org.ikigaidigital.domain.policy;

import org.ikigaidigital.TimeDeposit;

public final class StudentInterestPolicy implements InterestPolicy {

    public static final String PLAN_TYPE = "student";
    private static final double ANNUAL_RATE = 0.03;
    private static final int INTEREST_CUTOFF_DAY = 366;

    @Override
    public String planType() {
        return PLAN_TYPE;
    }

    @Override
    public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        int days = timeDeposit.getDays();
        if (days <= DEFAULT_GRACE_PERIOD_DAYS || days >= INTEREST_CUTOFF_DAY) {
            return 0.0;
        }
        return InterestPolicy.monthlyInterest(timeDeposit.getBalance(), ANNUAL_RATE);
    }
}
