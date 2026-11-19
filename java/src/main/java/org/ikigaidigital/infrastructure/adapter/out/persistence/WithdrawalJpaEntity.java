package org.ikigaidigital.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "withdrawals")
public class WithdrawalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"timeDepositId\"", nullable = false)
    private TimeDepositJpaEntity timeDeposit;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    protected WithdrawalJpaEntity() {
    }

    public WithdrawalJpaEntity(TimeDepositJpaEntity timeDeposit, BigDecimal amount, LocalDate date) {
        this.timeDeposit = timeDeposit;
        this.amount = amount;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public TimeDepositJpaEntity getTimeDeposit() {
        return timeDeposit;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }
}
