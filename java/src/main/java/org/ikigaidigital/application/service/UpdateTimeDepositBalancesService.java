package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.UpdateTimeDepositBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        List<TimeDeposit> timeDeposits = timeDepositRepository.findAll();
        if (timeDeposits.isEmpty()) {
            return;
        }
        timeDepositCalculator.updateBalance(timeDeposits);
        timeDepositRepository.saveAll(timeDeposits);
    }
}
