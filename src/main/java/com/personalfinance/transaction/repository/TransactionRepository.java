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

    // Phân rã chi tiêu theo danh mục (theo chi phí cá nhân thực tế, đã trừ đi phần chia tiền bạn bè)
    @Query(value = """
        SELECT 
            COALESCE(c.id, '00000000-0000-0000-0000-000000000000'::uuid) AS category_id,
            COALESCE(c.name, 'Chưa phân loại') AS category_name,
            COALESCE(c.icon, 'folder') AS category_icon,
            COALESCE(c.color, '#94A3B8') AS category_color,
            COALESCE(SUM(GREATEST(0, t.amount - COALESCE(alloc.total_allocated, 0))), 0) AS total_amount
        FROM transactions t
        LEFT JOIN categories c ON t.category_id = c.id
        LEFT JOIN (
            SELECT transaction_id, SUM(allocated_amount) AS total_allocated
            FROM debt_allocations
            GROUP BY transaction_id
        ) alloc ON alloc.transaction_id = t.id
        WHERE t.status = 'POSTED'
          AND t.direction = 'OUT'
          AND t.type = 'EXPENSE'
          AND t.transaction_date >= :startDate
          AND t.transaction_date <= :endDate
        GROUP BY c.id, c.name, c.icon, c.color
        HAVING COALESCE(SUM(GREATEST(0, t.amount - COALESCE(alloc.total_allocated, 0))), 0) > 0
        ORDER BY total_amount DESC
        """, nativeQuery = true)
    List<Object[]> findExpenseByCategory(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Thống kê thu chi theo ngày trong khoảng thời gian
    @Query(value = """
        SELECT 
            TO_CHAR(t.transaction_date AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM-DD') AS tx_date,
            COALESCE(SUM(CASE WHEN t.direction = 'IN' AND t.type IN ('INCOME', 'DEBT_COLLECTION', 'BORROW_RECEIVED') THEN t.amount ELSE 0 END), 0) AS daily_income,
            COALESCE(SUM(CASE WHEN t.direction = 'OUT' AND t.type IN ('EXPENSE', 'DEBT_REPAYMENT', 'LOAN_GIVEN') THEN t.amount ELSE 0 END), 0) AS daily_expense
        FROM transactions t
        WHERE t.status = 'POSTED'
          AND t.transaction_date >= :startDate
          AND t.transaction_date <= :endDate
        GROUP BY TO_CHAR(t.transaction_date AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM-DD')
        ORDER BY tx_date ASC
        """, nativeQuery = true)
    List<Object[]> findDailyIncomeExpense(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Thống kê thu chi theo giờ trong ngày
    @Query(value = """
        SELECT 
            EXTRACT(HOUR FROM t.transaction_date AT TIME ZONE 'Asia/Ho_Chi_Minh') AS tx_hour,
            COALESCE(SUM(CASE WHEN t.direction = 'IN' AND t.type IN ('INCOME', 'DEBT_COLLECTION', 'BORROW_RECEIVED') THEN t.amount ELSE 0 END), 0) AS hour_income,
            COALESCE(SUM(CASE WHEN t.direction = 'OUT' AND t.type IN ('EXPENSE', 'DEBT_REPAYMENT', 'LOAN_GIVEN') THEN t.amount ELSE 0 END), 0) AS hour_expense
        FROM transactions t
        WHERE t.status = 'POSTED'
          AND t.transaction_date >= :startDate
          AND t.transaction_date <= :endDate
        GROUP BY EXTRACT(HOUR FROM t.transaction_date AT TIME ZONE 'Asia/Ho_Chi_Minh')
        ORDER BY tx_hour ASC
        """, nativeQuery = true)
    List<Object[]> findHourlyIncomeExpense(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Thống kê thu chi theo tháng trong năm
    @Query(value = """
        SELECT 
            EXTRACT(MONTH FROM t.transaction_date AT TIME ZONE 'Asia/Ho_Chi_Minh') AS tx_month,
            COALESCE(SUM(CASE WHEN t.direction = 'IN' AND t.type IN ('INCOME', 'DEBT_COLLECTION', 'BORROW_RECEIVED') THEN t.amount ELSE 0 END), 0) AS month_income,
            COALESCE(SUM(CASE WHEN t.direction = 'OUT' AND t.type IN ('EXPENSE', 'DEBT_REPAYMENT', 'LOAN_GIVEN') THEN t.amount ELSE 0 END), 0) AS month_expense
        FROM transactions t
        WHERE t.status = 'POSTED'
          AND t.transaction_date >= :startDate
          AND t.transaction_date <= :endDate
        GROUP BY EXTRACT(MONTH FROM t.transaction_date AT TIME ZONE 'Asia/Ho_Chi_Minh')
        ORDER BY tx_month ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyIncomeExpense(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );
}
