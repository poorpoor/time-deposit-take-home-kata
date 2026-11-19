package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTimeDepositBalancesServiceTest {

    @Test
    @DisplayName("recalculates and persists updated balances for all eligible deposits")
    void recalculatesAndPersistsBalances() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        repository.seed(new TimeDeposit(1, "basic", 1200.00, 45), List.of());
        repository.seed(new TimeDeposit(2, "student", 1200.00, 365), List.of());
        repository.seed(new TimeDeposit(3, "student", 1200.00, 366), List.of());
        repository.seed(new TimeDeposit(4, "premium", 1200.00, 45), List.of());
        repository.seed(new TimeDeposit(5, "premium", 1200.00, 46), List.of());

        UpdateTimeDepositBalancesService service = new UpdateTimeDepositBalancesService(
            repository,
            new TimeDepositCalculator()
        );

        service.updateAllBalances();

        assertThat(repository.storedBalanceOf(1)).isEqualTo(1201.00);
        assertThat(repository.storedBalanceOf(2)).isEqualTo(1203.00);
        assertThat(repository.storedBalanceOf(3)).isEqualTo(1200.00);
        assertThat(repository.storedBalanceOf(4)).isEqualTo(1200.00);
        assertThat(repository.storedBalanceOf(5)).isEqualTo(1205.00);
    }

    @Test
    @DisplayName("accrues another month of interest when invoked repeatedly (non-idempotent)")
    void accruesInterestOnRepeatedCalls() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        repository.seed(new TimeDeposit(1, "basic", 12000.00, 45), List.of());

        UpdateTimeDepositBalancesService service = new UpdateTimeDepositBalancesService(
            repository,
            new TimeDepositCalculator()
        );

        service.updateAllBalances();
        assertThat(repository.storedBalanceOf(1)).isEqualTo(12010.00);

        service.updateAllBalances();
        // 12010.00 * 0.01 / 12 = 10.0083... -> 10.01 -> 12020.01
        assertThat(repository.storedBalanceOf(1)).isEqualTo(12020.01);
    }

    @Test
    @DisplayName("handles an empty repository cleanly")
    void handlesEmptyRepository() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        UpdateTimeDepositBalancesService service = new UpdateTimeDepositBalancesService(
            repository,
            new TimeDepositCalculator()
        );

        service.updateAllBalances();

        assertThat(repository.findAll()).isEmpty();
    }
}
