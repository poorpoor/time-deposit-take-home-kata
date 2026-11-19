package org.ikigaidigital.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataTimeDepositRepository extends JpaRepository<TimeDepositJpaEntity, Integer> {

    /**
     * Locks all time deposit rows in deterministic primary-key order ({@code SELECT ... FOR UPDATE})
     * so concurrent balance-update requests serialize cleanly without lost updates or deadlocks.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT td FROM TimeDepositJpaEntity td ORDER BY td.id ASC")
    List<TimeDepositJpaEntity> findAllForUpdate();

    @Query("SELECT DISTINCT td FROM TimeDepositJpaEntity td LEFT JOIN FETCH td.withdrawals ORDER BY td.id ASC")
    List<TimeDepositJpaEntity> findAllWithWithdrawals();
}
