package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.domain.model.Withdrawal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight in-memory fake repository used to verify application service behavior
 * and persisted state changes without brittle Mockito interaction verification.
 */
final class InMemoryTimeDepositRepository implements TimeDepositRepository {

    private final Map<Integer, TimeDeposit> depositsById = new LinkedHashMap<>();
    private final Map<Integer, List<Withdrawal>> withdrawalsByDepositId = new LinkedHashMap<>();

    void seed(TimeDeposit deposit, List<Withdrawal> withdrawals) {
        depositsById.put(
            deposit.getId(),
            new TimeDeposit(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays())
        );
        withdrawalsByDepositId.put(deposit.getId(), List.copyOf(withdrawals));
    }

    @Override
    public List<TimeDepositWithWithdrawals> findAllWithWithdrawals() {
        List<TimeDepositWithWithdrawals> result = new ArrayList<>();
        for (TimeDeposit deposit : depositsById.values()) {
            result.add(new TimeDepositWithWithdrawals(
                deposit.getId(),
                deposit.getPlanType(),
                BigDecimal.valueOf(deposit.getBalance()),
                deposit.getDays(),
                withdrawalsByDepositId.getOrDefault(deposit.getId(), List.of())
            ));
        }
        return result;
    }

    @Override
    public List<TimeDeposit> findAll() {
        List<TimeDeposit> copy = new ArrayList<>();
        for (TimeDeposit deposit : depositsById.values()) {
            copy.add(new TimeDeposit(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays()));
        }
        return copy;
    }

    @Override
    public void saveAll(List<TimeDeposit> timeDeposits) {
        for (TimeDeposit deposit : timeDeposits) {
            depositsById.put(
                deposit.getId(),
                new TimeDeposit(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays())
            );
        }
    }

    Double storedBalanceOf(int id) {
        return depositsById.get(id).getBalance();
    }
}
