package org.ikigaidigital.application.service;

import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.domain.model.Withdrawal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetAllTimeDepositsServiceTest {

    @Test
    @DisplayName("returns all time deposits together with their withdrawals")
    void returnsAllTimeDepositsWithWithdrawals() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        Withdrawal withdrawal = new Withdrawal(10, new BigDecimal("250.00"), LocalDate.of(2026, 2, 10));
        repository.seed(1, "basic", new BigDecimal("1200.00"), 45, List.of(withdrawal));
        repository.seed(2, "student", new BigDecimal("2400.00"), 100, List.of());

        GetAllTimeDepositsService service = new GetAllTimeDepositsService(repository);

        List<TimeDepositWithWithdrawals> result = service.getAllTimeDeposits();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(new TimeDepositWithWithdrawals(
            1,
            "basic",
            new BigDecimal("1200.00"),
            45,
            List.of(withdrawal)
        ));
        assertThat(result.get(1).withdrawals()).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list when no time deposits exist")
    void returnsEmptyListWhenRepositoryIsEmpty() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        GetAllTimeDepositsService service = new GetAllTimeDepositsService(repository);

        assertThat(service.getAllTimeDeposits()).isEmpty();
    }
}
