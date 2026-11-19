package org.ikigaidigital.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataTimeDepositRepository extends JpaRepository<TimeDepositJpaEntity, Integer> {

    List<TimeDepositJpaEntity> findAllByOrderByIdAsc();

    @Query("SELECT DISTINCT td FROM TimeDepositJpaEntity td LEFT JOIN FETCH td.withdrawals ORDER BY td.id ASC")
    List<TimeDepositJpaEntity> findAllWithWithdrawals();
}
