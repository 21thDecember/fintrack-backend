package com.personalfinance.debt.repository;

import com.personalfinance.debt.entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, UUID> {

    List<DebtPayment> findAllByDebtIdOrderByPaymentDateDesc(UUID debtId);

    List<DebtPayment> findAllByTransactionId(UUID transactionId);

    List<DebtPayment> findAllByTransactionIdIn(List<UUID> transactionIds);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM debt_payments WHERE debt_id = :debtId", nativeQuery = true)
    Long calculateTotalPaidByDebtId(@Param("debtId") UUID debtId);
}
