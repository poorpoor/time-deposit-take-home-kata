package org.ikigaidigital.application.service;

import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

public class GetAllTimeDepositsService implements GetAllTimeDepositsUseCase {

    private final TimeDepositRepository timeDepositRepository;

    public GetAllTimeDepositsService(TimeDepositRepository timeDepositRepository) {
        this.timeDepositRepository = Objects.requireNonNull(
            timeDepositRepository,
            "timeDepositRepository must not be null"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositWithWithdrawals> getAllTimeDeposits() {
        return timeDepositRepository.findAllWithWithdrawals();
    }
}
