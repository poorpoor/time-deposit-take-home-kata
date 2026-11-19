package org.ikigaidigital.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"timeDeposits\"")
public class TimeDepositJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "\"planType\"", nullable = false)
    private String planType;

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @OneToMany(mappedBy = "timeDeposit", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<WithdrawalJpaEntity> withdrawals = new ArrayList<>();

    protected TimeDepositJpaEntity() {
    }

    public Integer getId() {
        return id;
    }

    public String getPlanType() {
        return planType;
    }

    public int getDays() {
        return days;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<WithdrawalJpaEntity> getWithdrawals() {
        return withdrawals;
    }
}
