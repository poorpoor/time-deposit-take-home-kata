package org.ikigaidigital.infrastructure.adapter.out.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDepositWithWithdrawals;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public List<TimeDeposit> findAll() {
        return springDataRepository.findAllByOrderByIdAsc().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void saveAll(List<TimeDeposit> timeDeposits) {
        List<Integer> ids = timeDeposits.stream()
            .map(TimeDeposit::getId)
            .toList();

        List<TimeDepositJpaEntity> entities = springDataRepository.findAllById(ids);
        Map<Integer, TimeDepositJpaEntity> entityById = entities.stream()
            .collect(Collectors.toMap(TimeDepositJpaEntity::getId, Function.identity()));

        for (TimeDeposit deposit : timeDeposits) {
            TimeDepositJpaEntity entity = entityById.get(deposit.getId());
            if (entity != null) {
                entity.setBalance(BigDecimal.valueOf(deposit.getBalance()).setScale(BALANCE_SCALE, RoundingMode.HALF_UP));
            }
        }
        springDataRepository.saveAll(entities);
    }
}
