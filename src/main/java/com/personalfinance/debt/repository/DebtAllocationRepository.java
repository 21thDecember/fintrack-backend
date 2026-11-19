package com.personalfinance.debt.repository;

import com.personalfinance.debt.entity.DebtAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebtAllocationRepository extends JpaRepository<DebtAllocation, UUID> {

    List<DebtAllocation> findAllByTransactionId(UUID transactionId);

    List<DebtAllocation> findAllByDebtId(UUID debtId);

    List<DebtAllocation> findAllByPersonId(UUID personId);
}
