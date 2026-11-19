package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDepositCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTimeDepositBalancesServiceTest {

    @Test
    @DisplayName("recalculates and persists only deposits with non-zero monthly interest")
    void recalculatesAndPersistsEligibleBalancesOnly() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        repository.seed(1, "basic", new BigDecimal("1200.00"), 45, List.of());
        repository.seed(2, "student", new BigDecimal("1200.00"), 365, List.of());
        repository.seed(3, "student", new BigDecimal("1200.00"), 366, List.of());
        repository.seed(4, "premium", new BigDecimal("1200.00"), 45, List.of());
        repository.seed(5, "premium", new BigDecimal("1200.00"), 46, List.of());

        UpdateTimeDepositBalancesService service = new UpdateTimeDepositBalancesService(
            repository,
            new TimeDepositCalculator()
        );

        service.updateAllBalances();

        assertThat(repository.storedBalanceOf(1)).isEqualByComparingTo("1201.00");
        assertThat(repository.storedBalanceOf(2)).isEqualByComparingTo("1203.00");
        assertThat(repository.storedBalanceOf(3)).isEqualByComparingTo("1200.00");
        assertThat(repository.storedBalanceOf(4)).isEqualByComparingTo("1200.00");
        assertThat(repository.storedBalanceOf(5)).isEqualByComparingTo("1205.00");
        assertThat(repository.updatedIds()).containsExactly(1, 2, 5);
    }

    @Test
    @DisplayName("preserves full NUMERIC(19,2) BigDecimal precision for large principals")
    void preservesFullBigDecimalPrecisionForLargePrincipals() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        repository.seed(1, "basic", new BigDecimal("1000000000000000.01"), 20, List.of());
        repository.seed(2, "basic", new BigDecimal("1000000000000000.01"), 45, List.of());

        UpdateTimeDepositBalancesService service = new UpdateTimeDepositBalancesService(
            repository,
            new TimeDepositCalculator()
        );

        service.updateAllBalances();

        assertThat(repository.storedBalanceOf(1)).isEqualByComparingTo("1000000000000000.01");
        assertThat(repository.storedBalanceOf(2)).isEqualByComparingTo("1000833333333333.34");
        assertThat(repository.updatedIds()).containsExactly(2);
    }

    @Test
    @DisplayName("accrues another month of interest when invoked repeatedly (non-idempotent)")
    void accruesInterestOnRepeatedCalls() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
        repository.seed(1, "basic", new BigDecimal("12000.00"), 45, List.of());

        UpdateTimeDepositBalancesService service = new UpdateTimeDepositBalancesService(
            repository,
            new TimeDepositCalculator()
        );

        service.updateAllBalances();
        assertThat(repository.storedBalanceOf(1)).isEqualByComparingTo("12010.00");

        service.updateAllBalances();
        // 12010.00 * 0.01 / 12 = 10.0083... -> 10.01 -> 12020.01
        assertThat(repository.storedBalanceOf(1)).isEqualByComparingTo("12020.01");
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

        assertThat(repository.findAllForBalanceUpdate()).isEmpty();
        assertThat(repository.updatedIds()).isEmpty();
    }
}
