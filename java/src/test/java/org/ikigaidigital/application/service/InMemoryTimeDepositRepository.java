package org.ikigaidigital.application.service;

import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDepositBalance;
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

    private final Map<Integer, TimeDepositBalance> depositsById = new LinkedHashMap<>();
    private final Map<Integer, List<Withdrawal>> withdrawalsByDepositId = new LinkedHashMap<>();
    private final List<Integer> updatedIds = new ArrayList<>();

    void seed(int id, String planType, BigDecimal balance, int days, List<Withdrawal> withdrawals) {
        depositsById.put(id, new TimeDepositBalance(id, planType, balance, days));
        withdrawalsByDepositId.put(id, List.copyOf(withdrawals));
    }

    @Override
    public List<TimeDepositWithWithdrawals> findAllWithWithdrawals() {
        List<TimeDepositWithWithdrawals> result = new ArrayList<>();
        for (TimeDepositBalance deposit : depositsById.values()) {
            result.add(new TimeDepositWithWithdrawals(
                deposit.id(),
                deposit.planType(),
                deposit.balance(),
                deposit.days(),
                withdrawalsByDepositId.getOrDefault(deposit.id(), List.of())
            ));
        }
        return result;
    }

    @Override
    public List<TimeDepositBalance> findAllForBalanceUpdate() {
        return List.copyOf(depositsById.values());
    }

    @Override
    public void updateBalances(Map<Integer, BigDecimal> updatedBalancesById) {
        for (Map.Entry<Integer, BigDecimal> entry : updatedBalancesById.entrySet()) {
            TimeDepositBalance existing = depositsById.get(entry.getKey());
            if (existing != null) {
                depositsById.put(
                    existing.id(),
                    new TimeDepositBalance(existing.id(), existing.planType(), entry.getValue(), existing.days())
                );
                updatedIds.add(existing.id());
            }
        }
    }

    BigDecimal storedBalanceOf(int id) {
        return depositsById.get(id).balance();
    }

    List<Integer> updatedIds() {
        return List.copyOf(updatedIds);
    }
}
