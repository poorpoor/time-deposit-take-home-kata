package org.ikigaidigital.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record TimeDepositWithWithdrawals(
    int id,
    String planType,
    BigDecimal balance,
    int days,
    List<Withdrawal> withdrawals
) {

    public TimeDepositWithWithdrawals {
        Objects.requireNonNull(planType, "planType must not be null");
        Objects.requireNonNull(balance, "balance must not be null");
        withdrawals = List.copyOf(Objects.requireNonNull(withdrawals, "withdrawals must not be null"));
    }
}
