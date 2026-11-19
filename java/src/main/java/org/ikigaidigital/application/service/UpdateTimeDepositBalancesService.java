package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.UpdateTimeDepositBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDepositBalance;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpdateTimeDepositBalancesService implements UpdateTimeDepositBalancesUseCase {

    private final TimeDepositRepository timeDepositRepository;
    private final TimeDepositCalculator timeDepositCalculator;

    public UpdateTimeDepositBalancesService(
        TimeDepositRepository timeDepositRepository,
        TimeDepositCalculator timeDepositCalculator
    ) {
        this.timeDepositRepository = Objects.requireNonNull(
            timeDepositRepository,
            "timeDepositRepository must not be null"
        );
        this.timeDepositCalculator = Objects.requireNonNull(
            timeDepositCalculator,
            "timeDepositCalculator must not be null"
        );
    }

    @Override
    @Transactional
    public void updateAllBalances() {
        List<TimeDepositBalance> deposits = timeDepositRepository.findAllForBalanceUpdate();
        if (deposits.isEmpty()) {
            return;
        }

        Map<Integer, BigDecimal> updatedBalancesById = new LinkedHashMap<>();
        for (TimeDepositBalance deposit : deposits) {
            BigDecimal monthlyInterest = timeDepositCalculator.calculateMonthlyInterest(deposit.toTimeDeposit());
            if (monthlyInterest.signum() != 0) {
                updatedBalancesById.put(deposit.id(), deposit.balance().add(monthlyInterest));
            }
        }

        if (!updatedBalancesById.isEmpty()) {
            timeDepositRepository.updateBalances(updatedBalancesById);
        }
    }
}
