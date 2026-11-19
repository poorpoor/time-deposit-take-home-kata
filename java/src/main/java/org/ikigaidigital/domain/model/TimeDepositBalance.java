package org.ikigaidigital.domain.model;

import org.ikigaidigital.TimeDeposit;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Full-precision snapshot of a persisted time deposit ({@code NUMERIC(19, 2)}).
 * Preserves the exact {@link BigDecimal} principal so balances exceeding IEEE-754
 * {@code double} precision never lose cents during persistence updates.
 */
public record TimeDepositBalance(int id, String planType, BigDecimal balance, int days) {

    public TimeDepositBalance {
        Objects.requireNonNull(planType, "planType must not be null");
        Objects.requireNonNull(balance, "balance must not be null");
    }

    public TimeDeposit toTimeDeposit() {
        return new TimeDeposit(id, planType, balance.doubleValue(), days);
    }
}
