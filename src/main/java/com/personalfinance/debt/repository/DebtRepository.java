package com.personalfinance.debt.repository;

import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID>, JpaSpecificationExecutor<Debt> {

    List<Debt> findAllByPersonId(UUID personId);

    List<Debt> findAllByTypeAndStatusIn(DebtType type, List<DebtStatus> statuses);

    Page<Debt> findAllByStatusIn(List<DebtStatus> statuses, Pageable pageable);

    long countByStatusIn(List<DebtStatus> statuses);

    @Query(value = """
        SELECT COALESCE(SUM(
            d.principal_amount - COALESCE(
                (SELECT SUM(dp.amount) FROM debt_payments dp WHERE dp.debt_id = d.id), 0
            )
        ), 0)
        FROM debts d
        WHERE d.type = :type AND d.status IN ('OPEN', 'PARTIAL')
        """, nativeQuery = true)
    Long calculateTotalRemainingByType(@Param("type") String type);

    @Query(value = """
        SELECT d.*
        FROM debts d
        WHERE d.status IN ('OPEN', 'PARTIAL')
          AND d.due_date IS NOT NULL
          AND d.due_date <= :targetDate
        ORDER BY d.due_date ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Debt> findUpcomingDebts(@Param("targetDate") LocalDate targetDate, @Param("limit") int limit);
}
