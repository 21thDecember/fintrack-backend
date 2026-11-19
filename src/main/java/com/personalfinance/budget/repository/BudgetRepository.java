package com.personalfinance.budget.repository;

import com.personalfinance.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByCategoryIdAndYearAndMonth(UUID categoryId, Integer year, Integer month);

    List<Budget> findAllByYearAndMonth(Integer year, Integer month);

    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE category_id = :categoryId
          AND status = 'POSTED'
          AND direction = 'OUT'
          AND type = 'EXPENSE'
          AND EXTRACT(YEAR FROM transaction_date) = :year
          AND EXTRACT(MONTH FROM transaction_date) = :month
        """, nativeQuery = true)
    Long calculateSpentAmount(
            @Param("categoryId") UUID categoryId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
}
