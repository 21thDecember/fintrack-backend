package com.personalfinance.debt;

import com.personalfinance.account.entity.Account;
import com.personalfinance.account.entity.AccountType;
import com.personalfinance.account.repository.AccountRepository;
import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.debt.dto.DebtPaymentRequest;
import com.personalfinance.debt.dto.DebtResponse;
import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtOriginType;
import com.personalfinance.debt.entity.DebtPayment;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import com.personalfinance.debt.repository.DebtAllocationRepository;
import com.personalfinance.debt.repository.DebtPaymentRepository;
import com.personalfinance.debt.repository.DebtRepository;
import com.personalfinance.debt.service.DebtService;
import com.personalfinance.person.entity.Person;
import com.personalfinance.person.repository.PersonRepository;
import com.personalfinance.transaction.entity.Transaction;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionType;
import com.personalfinance.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtServiceTest {

    @Mock
    private DebtRepository debtRepository;
    @Mock
    private DebtAllocationRepository debtAllocationRepository;
    @Mock
    private DebtPaymentRepository debtPaymentRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DebtService debtService;

    private UUID debtId;
    private UUID accountId;
    private UUID personId;
    private Debt debt;
    private Account account;
    private Person person;

    @BeforeEach
    void setUp() {
        debtId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        personId = UUID.randomUUID();

        person = Person.builder()
                .id(personId)
                .name("Nguyễn Văn A")
                .build();

        account = Account.builder()
                .id(accountId)
                .name("Tài khoản chính")
                .accountType(AccountType.BANK)
                .isActive(true)
                .openingBalance(1000000L)
                .build();

        debt = Debt.builder()
                .id(debtId)
                .personId(personId)
                .type(DebtType.RECEIVABLE)
                .originType(DebtOriginType.MANUAL)
                .principalAmount(500000L)
                .status(DebtStatus.OPEN)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @Test
    void testRecordDebtPayment_Partial() {
        when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.of(account));
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(debtPaymentRepository.calculateTotalPaidByDebtId(debtId)).thenReturn(0L);

        Transaction mockTx = Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.DEBT_COLLECTION)
                .direction(TransactionDirection.IN)
                .amount(200000L)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTx);

        DebtPayment mockPayment = DebtPayment.builder()
                .id(UUID.randomUUID())
                .debtId(debtId)
                .transactionId(mockTx.getId())
                .amount(200000L)
                .build();
        when(debtPaymentRepository.save(any(DebtPayment.class))).thenReturn(mockPayment);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);
        when(debtPaymentRepository.findAllByDebtIdOrderByPaymentDateDesc(debtId)).thenReturn(List.of(mockPayment));

        DebtPaymentRequest request = DebtPaymentRequest.builder()
                .accountId(accountId)
                .amount(200000L)
                .note("Trả một phần")
                .build();

        DebtResponse response = debtService.recordDebtPayment(debtId, request);

        assertNotNull(response);
        assertEquals(DebtStatus.PARTIAL, debt.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
        verify(debtPaymentRepository).save(any(DebtPayment.class));
    }

    @Test
    void testRecordDebtPayment_ExceedRemaining_ThrowsException() {
        when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.of(account));
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(debtPaymentRepository.calculateTotalPaidByDebtId(debtId)).thenReturn(400000L);

        DebtPaymentRequest request = DebtPaymentRequest.builder()
                .accountId(accountId)
                .amount(200000L) // 400k + 200k > 500k
                .build();

        assertThrows(BusinessException.class, () -> debtService.recordDebtPayment(debtId, request));
    }
}
