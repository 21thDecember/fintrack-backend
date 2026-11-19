package com.personalfinance.account.repository;

import com.personalfinance.account.entity.Account;
import com.personalfinance.account.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByIsActiveTrueOrderByCreatedAtAsc();

    Optional<Account> findByIdAndIsActiveTrue(UUID id);

    Optional<Account> findByAccountNumberAndIsActiveTrue(String accountNumber);

    Optional<Account> findFirstByAccountTypeAndIsActiveTrue(AccountType accountType);

    @Query(value = """
        SELECT COALESCE(
            SUM(
                CASE 
                    WHEN direction = 'IN' THEN amount 
                    WHEN direction = 'OUT' THEN -amount 
                    ELSE 0 
                END
            ), 0
        )
        FROM transactions 
        WHERE account_id = :accountId AND status = 'POSTED'
        """, nativeQuery = true)
    Long calculateTransactionNetDelta(@Param("accountId") UUID accountId);
}
