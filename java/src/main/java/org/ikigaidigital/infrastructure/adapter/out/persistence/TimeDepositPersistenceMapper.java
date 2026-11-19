package org.ikigaidigital.infrastructure.adapter.out.persistence;

import org.ikigaidigital.domain.model.TimeDepositBalance;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.domain.model.Withdrawal;

import java.util.List;

public class TimeDepositPersistenceMapper {

    public TimeDepositBalance toDomainBalance(TimeDepositJpaEntity entity) {
        return new TimeDepositBalance(
            entity.getId(),
            entity.getPlanType(),
            entity.getBalance(),
            entity.getDays()
        );
    }

    public TimeDepositWithWithdrawals toDomainWithWithdrawals(TimeDepositJpaEntity entity) {
        List<Withdrawal> withdrawals = entity.getWithdrawals().stream()
            .map(this::toDomainWithdrawal)
            .toList();

        return new TimeDepositWithWithdrawals(
            entity.getId(),
            entity.getPlanType(),
            entity.getBalance(),
            entity.getDays(),
            withdrawals
        );
    }

    private Withdrawal toDomainWithdrawal(WithdrawalJpaEntity entity) {
        return new Withdrawal(
            entity.getId(),
            entity.getAmount(),
            entity.getDate()
        );
    }
}
