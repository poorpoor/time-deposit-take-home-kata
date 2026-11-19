package org.ikigaidigital;

import org.ikigaidigital.domain.policy.InterestPolicyRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class TimeDepositCalculator {

    private static final int CURRENCY_SCALE = 2;

    private final InterestPolicyRegistry policyRegistry;

    public TimeDepositCalculator() {
        this(InterestPolicyRegistry.defaultRegistry());
    }

    public TimeDepositCalculator(InterestPolicyRegistry policyRegistry) {
        this.policyRegistry = Objects.requireNonNull(policyRegistry, "policyRegistry must not be null");
    }

    /**
     * Calculates the rounded monthly interest for a single deposit using the registered
     * {@code InterestPolicy} for its plan type.
     *
     * <p>Compatibility note: Intentionally preserves the original {@code double} calculation and
     * {@code new BigDecimal(double).setScale(2, RoundingMode.HALF_UP)} rounding semantics so that
     * {@link #updateBalance(List)} remains 100% behaviorally identical to the legacy implementation
     * (including binary IEEE-754 edge cases such as basic {@code 18.00} and student {@code 6.00}).
     */
    public BigDecimal calculateMonthlyInterest(TimeDeposit deposit) {
        double unroundedInterest = policyRegistry.findByPlanType(deposit.getPlanType())
            .map(policy -> policy.calculateMonthlyInterest(deposit))
            .orElse(0.0);
        return new BigDecimal(unroundedInterest).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    public void updateBalance(List<TimeDeposit> xs) {
        for (TimeDeposit deposit : xs) {
            BigDecimal roundedInterest = calculateMonthlyInterest(deposit);
            deposit.setBalance(deposit.getBalance() + roundedInterest.doubleValue());
        }
    }
}
