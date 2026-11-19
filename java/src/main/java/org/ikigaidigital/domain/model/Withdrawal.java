package org.ikigaidigital.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Withdrawal(int id, BigDecimal amount, LocalDate date) {

    public Withdrawal {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(date, "date must not be null");
    }
}
