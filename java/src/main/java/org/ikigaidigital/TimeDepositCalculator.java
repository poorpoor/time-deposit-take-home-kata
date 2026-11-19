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
     * Recalculates balances in place for each time deposit using the registered {@code InterestPolicy}
     * for its plan type.
     *
     * <p>Assumption: Internal monetary arithmetic and final rounding ({@link RoundingMode#HALF_UP} to
     * 2 decimal places) are performed using {@link BigDecimal} to prevent IEEE-754 floating-point
     * artifacts (such as {@code 100.10 + 0.08 = 100.17999999999999}), converting back to {@code Double}
     * only at the frozen {@link TimeDeposit#setBalance(Double)} contract boundary. Unknown plan types
     * accrue zero interest, preserving the original behavior.
     */
    public void updateBalance(List<TimeDeposit> xs) {
        for (TimeDeposit deposit : xs) {
            BigDecimal monthlyInterest = policyRegistry.findByPlanType(deposit.getPlanType())
                .map(policy -> policy.calculateMonthlyInterest(deposit))
                .orElse(BigDecimal.ZERO)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);

            BigDecimal updatedBalance = BigDecimal.valueOf(deposit.getBalance()).add(monthlyInterest);
            deposit.setBalance(updatedBalance.doubleValue());
        }
    }
}
