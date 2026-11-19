package org.ikigaidigital.application.port.out;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;

import java.util.List;

public interface TimeDepositRepository {

    List<TimeDepositWithWithdrawals> findAllWithWithdrawals();

    List<TimeDeposit> findAll();

    void saveAll(List<TimeDeposit> timeDeposits);
}
