package org.ikigaidigital.infrastructure.adapter.out.persistence;

import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDepositBalance;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class TimeDepositPersistenceAdapter implements TimeDepositRepository {

    private static final int BALANCE_SCALE = 2;

    private final SpringDataTimeDepositRepository springDataRepository;
    private final TimeDepositPersistenceMapper mapper;

    public TimeDepositPersistenceAdapter(
        SpringDataTimeDepositRepository springDataRepository,
        TimeDepositPersistenceMapper mapper
    ) {
        this.springDataRepository = Objects.requireNonNull(
            springDataRepository,
            "springDataRepository must not be null"
        );
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public List<TimeDepositWithWithdrawals> findAllWithWithdrawals() {
        return springDataRepository.findAllWithWithdrawals().stream()
            .map(mapper::toDomainWithWithdrawals)
            .toList();
    }

    @Override
    public List<TimeDepositBalance> findAllForBalanceUpdate() {
        return springDataRepository.findAllForUpdate().stream()
            .map(mapper::toDomainBalance)
            .toList();
    }

    @Override
    public void updateBalances(Map<Integer, BigDecimal> updatedBalancesById) {
        for (Map.Entry<Integer, BigDecimal> entry : updatedBalancesById.entrySet()) {
            // Within the active transaction, getReferenceById returns the already-loaded managed entity
            // from the persistence context without issuing a second SELECT; Hibernate flushes dirty rows on commit.
            TimeDepositJpaEntity entity = springDataRepository.getReferenceById(entry.getKey());
            entity.setBalance(entry.getValue().setScale(BALANCE_SCALE, RoundingMode.HALF_UP));
        }
    }
}
