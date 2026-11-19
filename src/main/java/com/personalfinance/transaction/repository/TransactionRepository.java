package com.personalfinance.transaction.repository;

import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.Transaction;
import com.personalfinance.transaction.entity.TransactionSource;
import com.personalfinance.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    boolean existsBySourceAndExternalTransactionIdAndAccountId(
            TransactionSource source,
            String externalTransactionId,
            UUID accountId
    );

    Optional<Transaction> findBySourceAndExternalTransactionId(TransactionSource source, String externalTransactionId);

    List<Transaction> findAllByTransferGroupId(UUID transferGroupId);

    List<Transaction> findTop10ByStatusOrderByTransactionDateDesc(TransactionStatus status);

    long countByClassificationStatusAndStatus(ClassificationStatus classificationStatus, TransactionStatus status);

    // Tính tổng thu trong khoảng thời gian (chỉ các giao dịch POSTED)
    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE status = 'POSTED'
          AND direction = 'IN'
          AND type IN ('INCOME', 'DEBT_COLLECTION', 'BORROW_RECEIVED')
          AND transaction_date >= :startDate
          AND transaction_date <= :endDate
        """, nativeQuery = true)
    Long calculateTotalIncome(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Tính tổng chi gộp (gross expense) trong khoảng thời gian
    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE status = 'POSTED'
          AND direction = 'OUT'
          AND type IN ('EXPENSE', 'DEBT_REPAYMENT', 'LOAN_GIVEN')
          AND transaction_date >= :startDate
          AND transaction_date <= :endDate
        """, nativeQuery = true)
    Long calculateGrossExpense(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Tính tổng phân bổ nợ từ các giao dịch chi tiêu trong khoảng thời gian
    @Query(value = """
        SELECT COALESCE(SUM(da.allocated_amount), 0)
        FROM debt_allocations da
        JOIN transactions t ON da.transaction_id = t.id
        WHERE t.status = 'POSTED'
          AND t.direction = 'OUT'
          AND t.transaction_date >= :startDate
          AND t.transaction_date <= :endDate
        """, nativeQuery = true)
    Long calculateTotalDebtAllocations(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Phân rã chi tiêu theo danh mục
    @Query(value = """
        SELECT 
            c.id AS category_id,
            c.name AS category_name,
            c.icon AS category_icon,
            c.color AS category_color,
            COALESCE(SUM(t.amount), 0) AS total_amount
        FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.status = 'POSTED'
          AND t.direction = 'OUT'
          AND t.type = 'EXPENSE'
          AND t.transaction_date >= :startDate
          AND t.transaction_date <= :endDate
        GROUP BY c.id, c.name, c.icon, c.color
        ORDER BY total_amount DESC
        """, nativeQuery = true)
    List<Object[]> findExpenseByCategory(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );
}
