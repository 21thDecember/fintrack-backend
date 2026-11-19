package com.personalfinance.debt.service;

import com.personalfinance.account.entity.Account;
import com.personalfinance.account.repository.AccountRepository;
import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.response.PageResponse;
import com.personalfinance.debt.dto.CancelDebtRequest;
import com.personalfinance.debt.dto.CreateDebtFromTransactionRequest;
import com.personalfinance.debt.dto.CreateDebtRequest;
import com.personalfinance.debt.dto.DebtAllocationItemRequest;
import com.personalfinance.debt.dto.DebtAllocationResponse;
import com.personalfinance.debt.dto.DebtPaymentRequest;
import com.personalfinance.debt.dto.DebtPaymentResponse;
import com.personalfinance.debt.dto.DebtResponse;
import com.personalfinance.debt.dto.DebtSummaryResponse;
import com.personalfinance.debt.dto.PersonDebtSummaryResponse;
import com.personalfinance.debt.dto.UpdateDebtRequest;
import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtAllocation;
import com.personalfinance.debt.entity.DebtOriginType;
import com.personalfinance.debt.entity.DebtPayment;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import com.personalfinance.debt.repository.DebtAllocationRepository;
import com.personalfinance.debt.repository.DebtPaymentRepository;
import com.personalfinance.debt.repository.DebtRepository;
import com.personalfinance.person.entity.Person;
import com.personalfinance.person.repository.PersonRepository;
import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.Transaction;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionSource;
import com.personalfinance.transaction.entity.TransactionStatus;
import com.personalfinance.transaction.entity.TransactionType;
import com.personalfinance.transaction.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtAllocationRepository debtAllocationRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final PersonRepository personRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Tạo khoản nợ độc lập (Thủ công / MANUAL)
     */
    @Transactional
    public DebtResponse createStandaloneDebt(CreateDebtRequest request) {
        Person person = personRepository.findById(request.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Người", request.getPersonId()));

        Debt debt = Debt.builder()
                .personId(person.getId())
                .type(request.getType())
                .originType(DebtOriginType.MANUAL)
                .description(request.getDescription())
                .principalAmount(request.getPrincipalAmount())
                .dueDate(request.getDueDate())
                .status(DebtStatus.OPEN)
                .build();

        Debt saved = debtRepository.save(debt);
        log.info("Đã tạo khoản nợ độc lập: ID={}, Loại={}, Số tiền gốc={}",
                saved.getId(), saved.getType(), saved.getPrincipalAmount());

        return getDebtById(saved.getId());
    }

    /**
     * Tạo các khoản nợ từ một giao dịch chi tiêu (Chia tiền ăn uống, chi hộ bạn bè)
     */
    @Transactional
    public List<DebtResponse> createDebtFromTransaction(CreateDebtFromTransactionRequest request) {
        Transaction tx = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", request.getTransactionId()));

        if (tx.getStatus() == TransactionStatus.VOIDED) {
            throw new BusinessException("Không thể phân bổ nợ từ giao dịch đã bị hủy");
        }

        long totalAllocated = request.getAllocations().stream()
                .mapToLong(DebtAllocationItemRequest::getAllocatedAmount)
                .sum();

        if (totalAllocated > tx.getAmount()) {
            throw new BusinessException("Tổng tiền phân bổ nợ (" + totalAllocated + ") không được lớn hơn số tiền giao dịch (" + tx.getAmount() + ")");
        }

        List<DebtResponse> responses = new ArrayList<>();

        for (DebtAllocationItemRequest allocReq : request.getAllocations()) {
            Person person = personRepository.findById(allocReq.getPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người", allocReq.getPersonId()));

            String desc = allocReq.getNote() != null && !allocReq.getNote().trim().isEmpty()
                    ? allocReq.getNote().trim()
                    : "Chi hộ: " + (tx.getDescription() != null ? tx.getDescription() : "Khoản chi");

            Debt debt = Debt.builder()
                    .personId(person.getId())
                    .type(DebtType.RECEIVABLE)
                    .originType(DebtOriginType.TRANSACTION)
                    .originTransactionId(tx.getId())
                    .description(desc)
                    .principalAmount(allocReq.getAllocatedAmount())
                    .dueDate(allocReq.getDueDate())
                    .status(DebtStatus.OPEN)
                    .build();

            Debt savedDebt = debtRepository.save(debt);

            DebtAllocation allocation = DebtAllocation.builder()
                    .transactionId(tx.getId())
                    .debtId(savedDebt.getId())
                    .personId(person.getId())
                    .allocatedAmount(allocReq.getAllocatedAmount())
                    .note(allocReq.getNote())
                    .build();

            debtAllocationRepository.save(allocation);
            responses.add(getDebtById(savedDebt.getId()));
        }

        log.info("Đã tạo {} khoản nợ phân bổ từ giao dịch ID={}", responses.size(), tx.getId());
        return responses;
    }

    /**
     * Ghi nhận thanh toán / trả nợ:
     * - Kiểm tra số tiền còn lại
     * - Tự động tạo giao dịch tài chính (DEBT_COLLECTION khi thu nợ / DEBT_REPAYMENT khi trả nợ)
     * - Tạo bản ghi DebtPayment
     * - Tự động cập nhật trạng thái nợ (OPEN -> PARTIAL -> PAID)
     * Tất cả được thực hiện đồng bộ trong một Transaction CSDL (@Transactional)
     */
    @Transactional
    public DebtResponse recordDebtPayment(UUID debtId, DebtPaymentRequest request) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoản nợ", debtId));

        if (debt.getStatus() == DebtStatus.PAID) {
            throw new BusinessException("Khoản nợ này đã được thanh toán hoàn tất (PAID)");
        }
        if (debt.getStatus() == DebtStatus.CANCELLED) {
            throw new BusinessException("Khoản nợ này đã bị hủy bỏ (CANCELLED)");
        }

        Account account = accountRepository.findByIdAndIsActiveTrue(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", request.getAccountId()));

        Person person = personRepository.findById(debt.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Người", debt.getPersonId()));

        Long paidAmount = debtPaymentRepository.calculateTotalPaidByDebtId(debt.getId());
        long remaining = debt.getPrincipalAmount() - (paidAmount != null ? paidAmount : 0L);

        if (request.getAmount() > remaining) {
            throw new BusinessException(String.format(
                    "Số tiền thanh toán (%d) vượt quá số nợ còn lại (%d)",
                    request.getAmount(), remaining));
        }

        OffsetDateTime paymentDate = request.getPaymentDate() != null
                ? request.getPaymentDate()
                : OffsetDateTime.now(ZoneOffset.UTC);

        // Xác định loại giao dịch và chiều dòng tiền
        TransactionType txType;
        TransactionDirection txDirection;
        String txDescription;

        if (debt.getType() == DebtType.RECEIVABLE) {
            // Người khác trả tiền cho mình => Dòng tiền vào tài khoản
            txType = TransactionType.DEBT_COLLECTION;
            txDirection = TransactionDirection.IN;
            txDescription = "Thu nợ từ " + person.getName() +
                    (request.getNote() != null ? ": " + request.getNote() : (debt.getDescription() != null ? ": " + debt.getDescription() : ""));
        } else {
            // Mình trả tiền cho người khác => Dòng tiền ra khỏi tài khoản
            txType = TransactionType.DEBT_REPAYMENT;
            txDirection = TransactionDirection.OUT;
            txDescription = "Trả nợ cho " + person.getName() +
                    (request.getNote() != null ? ": " + request.getNote() : (debt.getDescription() != null ? ": " + debt.getDescription() : ""));
        }

        // Tạo giao dịch tài chính tự động
        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(txType)
                .amount(request.getAmount())
                .direction(txDirection)
                .source(TransactionSource.MANUAL)
                .description(txDescription)
                .note(request.getNote())
                .transactionDate(paymentDate)
                .classificationStatus(ClassificationStatus.CLASSIFIED)
                .status(TransactionStatus.POSTED)
                .build();

        Transaction savedTx = transactionRepository.save(transaction);

        // Lưu bản ghi DebtPayment
        DebtPayment payment = DebtPayment.builder()
                .debtId(debt.getId())
                .transactionId(savedTx.getId())
                .amount(request.getAmount())
                .paymentDate(paymentDate)
                .note(request.getNote())
                .build();

        debtPaymentRepository.save(payment);

        // Cập nhật trạng thái nợ
        long newTotalPaid = (paidAmount != null ? paidAmount : 0L) + request.getAmount();
        if (newTotalPaid >= debt.getPrincipalAmount()) {
            debt.setStatus(DebtStatus.PAID);
        } else {
            debt.setStatus(DebtStatus.PARTIAL);
        }
        debtRepository.save(debt);

        log.info("Ghi nhận thanh toán nợ thành công: DebtID={}, Số tiền={}, Trạng thái mới={}",
                debt.getId(), request.getAmount(), debt.getStatus());

        return getDebtById(debt.getId());
    }

    @Transactional
    public DebtResponse updateDebt(UUID id, UpdateDebtRequest request) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khoản nợ", id));

        if (debt.getStatus() == DebtStatus.CANCELLED) {
            throw new BusinessException("Không thể sửa khoản nợ đã bị hủy");
        }

        if (request.getDescription() != null) {
            debt.setDescription(request.getDescription().trim());
        }
        if (request.getDueDate() != null) {
            debt.setDueDate(request.getDueDate());
        }

        Debt updated = debtRepository.save(debt);
        log.info("Đã cập nhật thông tin khoản nợ ID={}", updated.getId());
        return getDebtById(updated.getId());
    }

    @Transactional
    public DebtResponse cancelDebt(UUID id, CancelDebtRequest request) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khoản nợ", id));

        if (debt.getStatus() == DebtStatus.PAID) {
            throw new BusinessException("Không thể hủy khoản nợ đã thanh toán hoàn tất");
        }

        Long paidAmount = debtPaymentRepository.calculateTotalPaidByDebtId(debt.getId());
        if (paidAmount != null && paidAmount > 0) {
            throw new BusinessException("Khoản nợ đã có lịch sử thanh toán, không thể hủy trực tiếp");
        }

        debt.setStatus(DebtStatus.CANCELLED);
        if (request != null && request.getReason() != null) {
            debt.setDescription((debt.getDescription() != null ? debt.getDescription() + " | " : "") +
                    "[HỦY: " + request.getReason().trim() + "]");
        }

        Debt saved = debtRepository.save(debt);
        log.info("Đã hủy khoản nợ ID={}", saved.getId());
        return getDebtById(saved.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<DebtResponse> getDebts(
            DebtType type,
            DebtStatus status,
            UUID personId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Debt> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (personId != null) {
                predicates.add(cb.equal(root.get("personId"), personId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Debt> debtPage = debtRepository.findAll(spec, pageable);
        List<DebtResponse> responses = debtPage.getContent().stream()
                .map(this::mapToDebtResponse)
                .toList();

        return PageResponse.of(debtPage, responses);
    }

    @Transactional(readOnly = true)
    public DebtResponse getDebtById(UUID id) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khoản nợ", id));
        return mapToDebtResponse(debt);
    }

    @Transactional(readOnly = true)
    public List<PersonDebtSummaryResponse> getDebtSummaryByPerson() {
        List<Person> persons = personRepository.findAll();
        List<Debt> activeDebts = debtRepository.findAllByStatusIn(List.of(DebtStatus.OPEN, DebtStatus.PARTIAL));

        Map<UUID, List<Debt>> debtsByPerson = activeDebts.stream()
                .collect(Collectors.groupingBy(Debt::getPersonId));

        List<PersonDebtSummaryResponse> result = new ArrayList<>();

        for (Person person : persons) {
            List<Debt> personDebts = debtsByPerson.getOrDefault(person.getId(), List.of());
            long totalRec = 0L;
            long totalPay = 0L;

            for (Debt d : personDebts) {
                Long paid = debtPaymentRepository.calculateTotalPaidByDebtId(d.getId());
                long remaining = Math.max(0L, d.getPrincipalAmount() - (paid != null ? paid : 0L));
                if (d.getType() == DebtType.RECEIVABLE) {
                    totalRec += remaining;
                } else {
                    totalPay += remaining;
                }
            }

            if (!personDebts.isEmpty() || totalRec > 0 || totalPay > 0) {
                result.add(PersonDebtSummaryResponse.builder()
                        .personId(person.getId())
                        .personName(person.getName())
                        .phone(person.getPhone())
                        .totalReceivable(totalRec)
                        .totalPayable(totalPay)
                        .netAmount(totalRec - totalPay)
                        .openDebtsCount(personDebts.size())
                        .build());
            }
        }

        result.sort((a, b) -> Long.compare(
                Math.abs(b.getNetAmount()),
                Math.abs(a.getNetAmount())
        ));

        return result;
    }

    @Transactional(readOnly = true)
    public DebtSummaryResponse getDebtSummary() {
        Long totalReceivables = debtRepository.calculateTotalRemainingByType(DebtType.RECEIVABLE.name());
        Long totalPayables = debtRepository.calculateTotalRemainingByType(DebtType.PAYABLE.name());

        long openReceivables = debtRepository.findAllByTypeAndStatusIn(
                DebtType.RECEIVABLE, List.of(DebtStatus.OPEN, DebtStatus.PARTIAL)).size();
        long openPayables = debtRepository.findAllByTypeAndStatusIn(
                DebtType.PAYABLE, List.of(DebtStatus.OPEN, DebtStatus.PARTIAL)).size();

        long rec = totalReceivables != null ? totalReceivables : 0L;
        long pay = totalPayables != null ? totalPayables : 0L;

        List<PersonDebtSummaryResponse> personSummaries = getDebtSummaryByPerson();

        return DebtSummaryResponse.builder()
                .totalReceivables(rec)
                .totalPayables(pay)
                .netDebt(rec - pay)
                .openReceivablesCount(openReceivables)
                .openPayablesCount(openPayables)
                .personSummaries(personSummaries)
                .build();
    }

    private DebtResponse mapToDebtResponse(Debt debt) {
        Person person = personRepository.findById(debt.getPersonId()).orElse(null);

        Long paidAmount = debtPaymentRepository.calculateTotalPaidByDebtId(debt.getId());
        long paid = paidAmount != null ? paidAmount : 0L;
        long remaining = Math.max(0L, debt.getPrincipalAmount() - paid);

        List<DebtPaymentResponse> payments = debtPaymentRepository.findAllByDebtIdOrderByPaymentDateDesc(debt.getId())
                .stream()
                .map(p -> DebtPaymentResponse.builder()
                        .id(p.getId())
                        .debtId(p.getDebtId())
                        .transactionId(p.getTransactionId())
                        .amount(p.getAmount())
                        .paymentDate(p.getPaymentDate())
                        .note(p.getNote())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();

        List<DebtAllocationResponse> allocations = debtAllocationRepository.findAllByDebtId(debt.getId())
                .stream()
                .map(a -> DebtAllocationResponse.builder()
                        .id(a.getId())
                        .transactionId(a.getTransactionId())
                        .debtId(a.getDebtId())
                        .personId(a.getPersonId())
                        .personName(person != null ? person.getName() : "N/A")
                        .allocatedAmount(a.getAllocatedAmount())
                        .note(a.getNote())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();

        return DebtResponse.builder()
                .id(debt.getId())
                .personId(debt.getPersonId())
                .personName(person != null ? person.getName() : null)
                .personPhone(person != null ? person.getPhone() : null)
                .type(debt.getType())
                .originType(debt.getOriginType())
                .originTransactionId(debt.getOriginTransactionId())
                .description(debt.getDescription())
                .principalAmount(debt.getPrincipalAmount())
                .paidAmount(paid)
                .remainingAmount(remaining)
                .dueDate(debt.getDueDate())
                .status(debt.getStatus())
                .createdAt(debt.getCreatedAt())
                .updatedAt(debt.getUpdatedAt())
                .payments(payments)
                .allocations(allocations)
                .build();
    }
}
