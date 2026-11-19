package org.ikigaidigital.infrastructure.adapter.in.web;

import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.domain.model.Withdrawal;
import org.ikigaidigital.infrastructure.adapter.in.web.generated.model.TimeDepositResponse;
import org.ikigaidigital.infrastructure.adapter.in.web.generated.model.WithdrawalResponse;

import java.util.List;

public class TimeDepositWebMapper {

    public TimeDepositResponse toResponse(TimeDepositWithWithdrawals domain) {
        List<WithdrawalResponse> withdrawals = domain.withdrawals().stream()
            .map(this::toWithdrawalResponse)
            .toList();

        return new TimeDepositResponse(
            domain.id(),
            domain.planType(),
            domain.balance(),
            domain.days(),
            withdrawals
        );
    }

    private WithdrawalResponse toWithdrawalResponse(Withdrawal withdrawal) {
        return new WithdrawalResponse(
            withdrawal.id(),
            withdrawal.amount(),
            withdrawal.date()
        );
    }
}
