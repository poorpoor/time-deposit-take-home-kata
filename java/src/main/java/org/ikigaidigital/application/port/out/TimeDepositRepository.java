package org.ikigaidigital.application.port.out;

import org.ikigaidigital.domain.model.TimeDepositBalance;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TimeDepositRepository {

    List<TimeDepositWithWithdrawals> findAllWithWithdrawals();

    List<TimeDepositBalance> findAllForBalanceUpdate();

    void updateBalances(Map<Integer, BigDecimal> updatedBalancesById);
}
