package com.personalfinance.transaction.service;

import com.personalfinance.account.entity.Account;
import com.personalfinance.account.repository.AccountRepository;
import com.personalfinance.category.entity.Category;
import com.personalfinance.category.repository.CategoryRepository;
import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.response.PageResponse;
import com.personalfinance.debt.dto.DebtAllocationItemRequest;
import com.personalfinance.debt.dto.DebtAllocationResponse;
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
import com.personalfinance.transaction.dto.CreateTransactionRequest;
import com.personalfinance.transaction.dto.TransactionFilterRequest;
import com.personalfinance.transaction.dto.TransactionResponse;
import com.personalfinance.transaction.dto.TransferRequest;
import com.personalfinance.transaction.dto.TransferResponse;
import com.personalfinance.transaction.dto.UpdateTransactionRequest;
import com.personalfinance.transaction.dto.VoidTransactionRequest;
import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.Transaction;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionSource;
import com.personalfinance.transaction.entity.TransactionStatus;
import com.personalfinance.transaction.entity.TransactionType;
import com.personalfinance.transaction.repository.TransactionRepository;
import com.personalfinance.transaction.repository.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PersonRepository personRepository;
    private final DebtRepository debtRepository;
    private final DebtAllocationRepository debtAllocationRepository;
    private final DebtPaymentRepository debtPaymentRepository;

    /**
     * Xác định chiều dòng tiền (IN/OUT) dựa trên loại giao dịch để đảm bảo tính nhất quán
     */
    public TransactionDirection resolveDirection(TransactionType type, TransactionDirection requestedDirection) {
        switch (type) {
            case INCOME:
            case BORROW_RECEIVED:
            case DEBT_COLLECTION:
                return TransactionDirection.IN;
            case EXPENSE:
            case LOAN_GIVEN:
            case DEBT_REPAYMENT:
                return TransactionDirection.OUT;
            case ADJUSTMENT:
                if (requestedDirection == null || requestedDirection == TransactionDirection.NONE) {
                    throw new BusinessException("Giao dịch điều chỉnh số dư (ADJUSTMENT) cần xác định rõ hướng tiền IN hoặc OUT");
                }
                return requestedDirection;
            case TRANSFER:
                if (requestedDirection == null || requestedDirection == TransactionDirection.NONE) {
                    throw new BusinessException("Giao dịch chuyển khoản cần xác định rõ hướng tiền IN hoặc OUT");
                }
                return requestedDirection;
            default:
                return requestedDirection != null ? requestedDirection : TransactionDirection.NONE;
        }
    }

    /**
     * Tạo mới giao dịch thu chi / vay mượn
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // Kiểm tra tài khoản
        Account account = accountRepository.findByIdAndIsActiveTrue(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", request.getAccountId()));

        // Kiểm tra danh mục nếu có
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));
        }

        // Xác thực chiều dòng tiền
        TransactionDirection direction = resolveDirection(request.getType(), request.getDirection());

        // Xử lý ngày giao dịch
        OffsetDateTime txDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : OffsetDateTime.now(ZoneOffset.UTC);

        // Trạng thái phân loại
        ClassificationStatus classificationStatus = (category != null)
                ? ClassificationStatus.CLASSIFIED
                : ClassificationStatus.UNCLASSIFIED;

        // Lưu giao dịch
        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .categoryId(category != null ? category.getId() : null)
                .type(request.getType())
                .amount(request.getAmount())
                .direction(direction)
                .source(request.getSource() != null ? request.getSource() : TransactionSource.MANUAL)
                .description(request.getDescription())
                .note(request.getNote())
                .transactionDate(txDate)
                .classificationStatus(classificationStatus)
                .status(TransactionStatus.POSTED)
                .build();

        Transaction savedTx = transactionRepository.save(transaction);

        // Xử lý ghi nợ sổ nợ: Chi tiêu chia tiền (EXPENSE), Cho vay (LOAN_GIVEN), Đi vay (BORROW_RECEIVED)
        List<DebtAllocationResponse> allocationResponses = new ArrayList<>();
        if (savedTx.getType() == TransactionType.EXPENSE || savedTx.getType() == TransactionType.LOAN_GIVEN || savedTx.getType() == TransactionType.BORROW_RECEIVED) {
            allocationResponses = applyDebtAllocations(savedTx, request.getDebtAllocations(), request.getPersonId(), request.getDueDate());
        }

        List<UUID> linkedDebtIds = new ArrayList<>();
        if (savedTx.getType() == TransactionType.DEBT_COLLECTION || savedTx.getType() == TransactionType.DEBT_REPAYMENT) {
            applyDebtPayments(savedTx, request.getDebtIds(), request.getDebtId());
            linkedDebtIds = debtPaymentRepository.findAllByTransactionId(savedTx.getId()).stream()
                    .map(DebtPayment::getDebtId)
                    .distinct()
                    .toList();
        }

        log.info("Đã tạo giao dịch: ID={}, Loại={}, Số tiền={}, Hướng={}",
                savedTx.getId(), savedTx.getType(), savedTx.getAmount(), savedTx.getDirection());

        return toResponse(savedTx, account, category, allocationResponses, linkedDebtIds);
    }

    /**
     * Chuyển khoản giữa 2 tài khoản (Tạo cặp giao dịch liên kết OUT và IN cùng transferGroupId)
     */
    @Transactional
    public TransferResponse createTransfer(TransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BusinessException("Tài khoản nguồn và tài khoản đích không được trùng nhau");
        }

        Account fromAccount = accountRepository.findByIdAndIsActiveTrue(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản nguồn", request.getFromAccountId()));

        Account toAccount = accountRepository.findByIdAndIsActiveTrue(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản đích", request.getToAccountId()));

        OffsetDateTime txDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : OffsetDateTime.now(ZoneOffset.UTC);

        UUID transferGroupId = UUID.randomUUID();

        String fromDesc = "Chuyển tiền đến " + toAccount.getName() +
                (request.getDescription() != null ? ": " + request.getDescription() : "");
        String toDesc = "Nhận tiền từ " + fromAccount.getName() +
                (request.getDescription() != null ? ": " + request.getDescription() : "");

        // 1. Giao dịch rút tiền từ tài khoản nguồn
        Transaction fromTx = Transaction.builder()
                .accountId(fromAccount.getId())
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .direction(TransactionDirection.OUT)
                .source(TransactionSource.MANUAL)
                .description(fromDesc)
                .note(request.getNote())
                .transactionDate(txDate)
                .classificationStatus(ClassificationStatus.CLASSIFIED)
                .transferGroupId(transferGroupId)
                .status(TransactionStatus.POSTED)
                .build();
        Transaction savedFromTx = transactionRepository.save(fromTx);

        // 2. Giao dịch nhận tiền tại tài khoản đích
        Transaction toTx = Transaction.builder()
                .accountId(toAccount.getId())
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .direction(TransactionDirection.IN)
                .source(TransactionSource.MANUAL)
                .description(toDesc)
                .note(request.getNote())
                .transactionDate(txDate)
                .classificationStatus(ClassificationStatus.CLASSIFIED)
                .transferGroupId(transferGroupId)
                .status(TransactionStatus.POSTED)
                .build();
        Transaction savedToTx = transactionRepository.save(toTx);

        // 3. Phí chuyển khoản nếu có (> 0)
        Transaction savedFeeTx = null;
        if (request.getFee() != null && request.getFee() > 0) {
            Transaction feeTx = Transaction.builder()
                    .accountId(fromAccount.getId())
                    .type(TransactionType.EXPENSE)
                    .amount(request.getFee())
                    .direction(TransactionDirection.OUT)
                    .source(TransactionSource.MANUAL)
                    .description("Phí chuyển tiền: " + fromDesc)
                    .note(request.getNote())
                    .transactionDate(txDate)
                    .classificationStatus(ClassificationStatus.CLASSIFIED)
                    .transferGroupId(transferGroupId)
                    .status(TransactionStatus.POSTED)
                    .build();
            savedFeeTx = transactionRepository.save(feeTx);
        }

        log.info("Chuyển tiền thành công: {} VND từ {} sang {} (GroupID: {})",
                request.getAmount(), fromAccount.getName(), toAccount.getName(), transferGroupId);

        return TransferResponse.builder()
                .transferGroupId(transferGroupId)
                .fromTransaction(toResponse(savedFromTx, fromAccount, null, List.of()))
                .toTransaction(toResponse(savedToTx, toAccount, null, List.of()))
                .feeTransaction(savedFeeTx != null ? toResponse(savedFeeTx, fromAccount, null, List.of()) : null)
                .build();
    }

    /**
     * Cập nhật thông tin giao dịch tuân thủ quy tắc bất biến của giao dịch ngân hàng
     */
    @Transactional
    public TransactionResponse updateTransaction(UUID id, UpdateTransactionRequest request) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", id));

        if (tx.getStatus() == TransactionStatus.VOIDED) {
            throw new BusinessException("Không thể cập nhật giao dịch đã bị hủy (VOIDED)");
        }

        // Quy tắc bảo vệ dữ liệu đối với giao dịch đồng bộ ngân hàng (BANK)
        if (tx.getSource() == TransactionSource.BANK) {
            if (request.getAmount() != null && !request.getAmount().equals(tx.getAmount())) {
                throw new BusinessException("Không được phép sửa đổi số tiền của giao dịch từ ngân hàng");
            }
            if (request.getTransactionDate() != null && !request.getTransactionDate().equals(tx.getTransactionDate())) {
                throw new BusinessException("Không được phép sửa đổi ngày giao dịch từ ngân hàng");
            }
            if (request.getAccountId() != null && !request.getAccountId().equals(tx.getAccountId())) {
                throw new BusinessException("Không được phép đổi tài khoản của giao dịch từ ngân hàng");
            }
        } else {
            // Đối với giao dịch thủ công MANUAL
            if (request.getAccountId() != null) {
                Account acc = accountRepository.findByIdAndIsActiveTrue(request.getAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", request.getAccountId()));
                tx.setAccountId(acc.getId());
            }
            if (request.getAmount() != null) {
                if (request.getAmount() <= 0) {
                    throw new BusinessException("Số tiền giao dịch phải lớn hơn 0");
                }
                tx.setAmount(request.getAmount());
            }
            if (request.getTransactionDate() != null) {
                tx.setTransactionDate(request.getTransactionDate());
            }
        }

        // Cập nhật loại giao dịch (type) cho cả MANUAL và BANK (với điều kiện không làm đổi chiều tiền IN/OUT của sao kê ngân hàng)
        if (request.getType() != null && !request.getType().equals(tx.getType())) {
            TransactionDirection newDirection = resolveDirection(request.getType(), request.getDirection());
            if (tx.getSource() == TransactionSource.BANK && newDirection != tx.getDirection()) {
                throw new BusinessException("Không thể đổi loại giao dịch làm thay đổi chiều dòng tiền (Thu/Chi) của sao kê ngân hàng");
            }
            tx.setType(request.getType());
            tx.setDirection(newDirection);
        } else if (tx.getSource() != TransactionSource.BANK && request.getDirection() != null) {
            tx.setDirection(resolveDirection(tx.getType(), request.getDirection()));
        }

        // Cập nhật danh mục
        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));
            tx.setCategoryId(cat.getId());
            tx.setClassificationStatus(ClassificationStatus.CLASSIFIED);
        } else if (request.getType() != null && request.getType() != TransactionType.INCOME && request.getType() != TransactionType.EXPENSE) {
            // Các loại giao dịch nợ (DEBT_COLLECTION, DEBT_REPAYMENT,...) hoặc chuyển khoản không bắt buộc danh mục
            tx.setCategoryId(null);
            tx.setClassificationStatus(ClassificationStatus.CLASSIFIED);
        } else if (request.getClassificationStatus() != null) {
            tx.setClassificationStatus(request.getClassificationStatus());
            if (request.getClassificationStatus() == ClassificationStatus.UNCLASSIFIED) {
                tx.setCategoryId(null);
            }
        }

        // Tự động liên kết và cấn trừ sổ nợ cho một hoặc nhiều khoản nợ được chọn
        if (tx.getType() == TransactionType.DEBT_COLLECTION || tx.getType() == TransactionType.DEBT_REPAYMENT) {
            if (request.getDebtIds() != null || request.getDebtId() != null) {
                List<DebtPayment> existingPayments = debtPaymentRepository.findAllByTransactionId(tx.getId());
                for (DebtPayment p : existingPayments) {
                    debtPaymentRepository.delete(p);
                    debtRepository.findById(p.getDebtId()).ifPresent(d -> {
                        Long paid = debtPaymentRepository.calculateTotalPaidByDebtId(d.getId());
                        long rem = d.getPrincipalAmount() - (paid != null ? paid : 0L);
                        d.setStatus(rem <= 0 ? DebtStatus.PAID : (paid != null && paid > 0 ? DebtStatus.PARTIAL : DebtStatus.OPEN));
                        debtRepository.save(d);
                    });
                }
                applyDebtPayments(tx, request.getDebtIds(), request.getDebtId());
            }
        }

        // Tự động liên kết và phân bổ ghi nợ cho EXPENSE, LOAN_GIVEN, BORROW_RECEIVED
        if (tx.getType() == TransactionType.EXPENSE || tx.getType() == TransactionType.LOAN_GIVEN || tx.getType() == TransactionType.BORROW_RECEIVED) {
            if (request.getDebtAllocations() != null || request.getPersonId() != null) {
                List<DebtAllocation> existingAllocations = debtAllocationRepository.findAllByTransactionId(tx.getId());
                for (DebtAllocation a : existingAllocations) {
                    debtRepository.findById(a.getDebtId()).ifPresent(d -> {
                        if (d.getOriginType() == DebtOriginType.TRANSACTION && d.getStatus() == DebtStatus.OPEN) {
                            debtRepository.delete(d);
                        }
                    });
                    debtAllocationRepository.delete(a);
                }
                applyDebtAllocations(tx, request.getDebtAllocations(), request.getPersonId(), request.getDueDate());
            }
        }

        if (request.getDescription() != null) {
            tx.setDescription(request.getDescription().trim());
        }
        if (request.getNote() != null) {
            tx.setNote(request.getNote().trim());
        }

        Transaction updated = transactionRepository.save(tx);
        log.info("Đã cập nhật giao dịch ID: {}", updated.getId());
        return getTransactionById(updated.getId());
    }

    /**
     * Hủy giao dịch (VOID) thay vì xóa cứng dữ liệu tài chính
     */
    @Transactional
    public TransactionResponse voidTransaction(UUID id, VoidTransactionRequest request) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", id));

        if (tx.getStatus() == TransactionStatus.VOIDED) {
            throw new BusinessException("Giao dịch này đã bị hủy trước đó");
        }

        String reason = (request != null && request.getReason() != null) ? request.getReason().trim() : "Hủy giao dịch";
        String updatedNote = (tx.getNote() != null ? tx.getNote() + " | " : "") + "[ĐÃ HỦY: " + reason + "]";

        tx.setStatus(TransactionStatus.VOIDED);
        tx.setNote(updatedNote);
        transactionRepository.save(tx);

        // Nếu là giao dịch thuộc nhóm chuyển khoản (TRANSFER), hủy luôn các giao dịch trong cùng nhóm
        if (tx.getTransferGroupId() != null) {
            List<Transaction> groupTxs = transactionRepository.findAllByTransferGroupId(tx.getTransferGroupId());
            for (Transaction relatedTx : groupTxs) {
                if (!relatedTx.getId().equals(tx.getId()) && relatedTx.getStatus() != TransactionStatus.VOIDED) {
                    relatedTx.setStatus(TransactionStatus.VOIDED);
                    relatedTx.setNote((relatedTx.getNote() != null ? relatedTx.getNote() + " | " : "") +
                            "[ĐÃ HỦY theo giao dịch liên kết " + tx.getId() + ": " + reason + "]");
                    transactionRepository.save(relatedTx);
                }
            }
        }

        // Nếu có phân bổ nợ gắn với giao dịch này, cập nhật trạng thái các khoản nợ tương ứng
        List<DebtAllocation> allocations = debtAllocationRepository.findAllByTransactionId(tx.getId());
        for (DebtAllocation alloc : allocations) {
            debtRepository.findById(alloc.getDebtId()).ifPresent(debt -> {
                if (debt.getStatus() == DebtStatus.OPEN) {
                    debt.setStatus(DebtStatus.CANCELLED);
                    debtRepository.save(debt);
                }
            });
        }

        log.info("Đã hủy giao dịch ID: {}, Lý do: {}", tx.getId(), reason);
        return getTransactionById(tx.getId());
    }

    /**
     * Xóa vĩnh viễn giao dịch khỏi hệ thống
     */
    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", id));

        // Nếu là giao dịch chuyển khoản kép (TRANSFER), xóa luôn giao dịch liên kết
        if (tx.getTransferGroupId() != null) {
            List<Transaction> groupTxs = transactionRepository.findAllByTransferGroupId(tx.getTransferGroupId());
            for (Transaction relatedTx : groupTxs) {
                if (!relatedTx.getId().equals(tx.getId())) {
                    List<DebtAllocation> relatedAllocations = debtAllocationRepository.findAllByTransactionId(relatedTx.getId());
                    if (!relatedAllocations.isEmpty()) {
                        debtAllocationRepository.deleteAll(relatedAllocations);
                    }
                    transactionRepository.delete(relatedTx);
                }
            }
        }

        List<DebtAllocation> allocations = debtAllocationRepository.findAllByTransactionId(tx.getId());
        if (!allocations.isEmpty()) {
            debtAllocationRepository.deleteAll(allocations);
        }

        transactionRepository.delete(tx);
        log.info("Đã xóa hoàn toàn giao dịch ID: {}", id);
    }

    /**
     * Lọc và phân trang danh sách giao dịch
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(TransactionFilterRequest filter) {
        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.getSortBy() != null ? filter.getSortBy() : "transactionDate"
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Transaction> spec = TransactionSpecification.withFilter(filter);
        Page<Transaction> page = transactionRepository.findAll(spec, pageable);

        // Pre-fetch related accounts and categories for efficiency
        List<TransactionResponse> responses = mapTransactionsToResponses(page.getContent());
        return PageResponse.of(page, responses);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", id));

        Account account = accountRepository.findById(tx.getAccountId()).orElse(null);
        Category category = tx.getCategoryId() != null ? categoryRepository.findById(tx.getCategoryId()).orElse(null) : null;
        List<DebtAllocationResponse> allocations = getDebtAllocationResponses(tx.getId());
        List<UUID> debtIds = debtPaymentRepository.findAllByTransactionId(tx.getId()).stream()
                .map(DebtPayment::getDebtId)
                .distinct()
                .toList();

        return toResponse(tx, account, category, allocations, debtIds);
    }

    private List<DebtAllocationResponse> getDebtAllocationResponses(UUID transactionId) {
        List<DebtAllocation> allocations = debtAllocationRepository.findAllByTransactionId(transactionId);
        if (allocations.isEmpty()) return List.of();

        List<UUID> personIds = allocations.stream().map(DebtAllocation::getPersonId).toList();
        Map<UUID, String> personNameMap = personRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(Person::getId, Person::getName));

        return allocations.stream().map(a -> DebtAllocationResponse.builder()
                .id(a.getId())
                .transactionId(a.getTransactionId())
                .debtId(a.getDebtId())
                .personId(a.getPersonId())
                .personName(personNameMap.getOrDefault(a.getPersonId(), "N/A"))
                .allocatedAmount(a.getAllocatedAmount())
                .note(a.getNote())
                .createdAt(a.getCreatedAt())
                .build()
        ).toList();
    }

    private List<TransactionResponse> mapTransactionsToResponses(List<Transaction> transactions) {
        if (transactions.isEmpty()) return List.of();

        List<UUID> accountIds = transactions.stream().map(Transaction::getAccountId).distinct().toList();
        Map<UUID, Account> accountMap = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<UUID> categoryIds = transactions.stream().map(Transaction::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<UUID, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<UUID> txIds = transactions.stream().map(Transaction::getId).toList();
        Map<UUID, List<UUID>> txDebtIdsMap = debtPaymentRepository.findAllByTransactionIdIn(txIds).stream()
                .collect(Collectors.groupingBy(
                        DebtPayment::getTransactionId,
                        Collectors.mapping(DebtPayment::getDebtId, Collectors.toList())
                ));

        return transactions.stream().map(tx -> {
            Account acc = accountMap.get(tx.getAccountId());
            Category cat = tx.getCategoryId() != null ? categoryMap.get(tx.getCategoryId()) : null;
            List<UUID> debtIds = txDebtIdsMap.getOrDefault(tx.getId(), List.of());
            return toResponse(tx, acc, cat, List.of(), debtIds);
        }).toList();
    }

    private TransactionResponse toResponse(
            Transaction tx,
            Account account,
            Category category,
            List<DebtAllocationResponse> allocations) {
        return toResponse(tx, account, category, allocations, List.of());
    }

    private TransactionResponse toResponse(
            Transaction tx,
            Account account,
            Category category,
            List<DebtAllocationResponse> allocations,
            List<UUID> debtIds) {

        return TransactionResponse.builder()
                .id(tx.getId())
                .accountId(tx.getAccountId())
                .accountName(account != null ? account.getName() : null)
                .categoryId(tx.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .categoryColor(category != null ? category.getColor() : null)
                .type(tx.getType())
                .amount(tx.getAmount())
                .direction(tx.getDirection())
                .source(tx.getSource())
                .externalTransactionId(tx.getExternalTransactionId())
                .externalReference(tx.getExternalReference())
                .originalDescription(tx.getOriginalDescription())
                .description(tx.getDescription())
                .note(tx.getNote())
                .transactionDate(tx.getTransactionDate())
                .classificationStatus(tx.getClassificationStatus())
                .transferGroupId(tx.getTransferGroupId())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .debtAllocations(allocations)
                .debtIds(debtIds != null ? debtIds : List.of())
                .personId(allocations != null && !allocations.isEmpty() ? allocations.get(0).getPersonId() : null)
                .personName(allocations != null && !allocations.isEmpty() ? allocations.get(0).getPersonName() : null)
                .build();
    }

    private List<DebtAllocationResponse> applyDebtAllocations(
            Transaction tx,
            List<DebtAllocationItemRequest> allocations,
            UUID personId,
            LocalDate dueDate) {

        List<DebtAllocationResponse> responses = new ArrayList<>();

        if (allocations != null && !allocations.isEmpty()) {
            long totalAllocated = allocations.stream()
                    .mapToLong(DebtAllocationItemRequest::getAllocatedAmount)
                    .sum();

            if (totalAllocated > tx.getAmount()) {
                throw new BusinessException("Tổng số tiền phân bổ (" + totalAllocated + ") không được vượt quá số tiền giao dịch (" + tx.getAmount() + ")");
            }

            DebtType debtType = (tx.getType() == TransactionType.BORROW_RECEIVED)
                    ? DebtType.PAYABLE
                    : DebtType.RECEIVABLE;

            String defaultPrefix = (tx.getType() == TransactionType.LOAN_GIVEN)
                    ? "Cho vay: "
                    : (tx.getType() == TransactionType.BORROW_RECEIVED ? "Đi vay: " : "Chi hộ: ");

            for (DebtAllocationItemRequest allocReq : allocations) {
                Person person = personRepository.findById(allocReq.getPersonId())
                        .orElseThrow(() -> new ResourceNotFoundException("Người", allocReq.getPersonId()));

                String desc = (allocReq.getNote() != null && !allocReq.getNote().trim().isEmpty())
                        ? allocReq.getNote().trim()
                        : defaultPrefix + (tx.getDescription() != null ? tx.getDescription() : "Khoản tiền");

                Debt debt = Debt.builder()
                        .personId(person.getId())
                        .type(debtType)
                        .originType(DebtOriginType.TRANSACTION)
                        .originTransactionId(tx.getId())
                        .description(desc)
                        .principalAmount(allocReq.getAllocatedAmount())
                        .dueDate(allocReq.getDueDate() != null ? allocReq.getDueDate() : dueDate)
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

                DebtAllocation savedAlloc = debtAllocationRepository.save(allocation);

                responses.add(DebtAllocationResponse.builder()
                        .id(savedAlloc.getId())
                        .transactionId(savedAlloc.getTransactionId())
                        .debtId(savedAlloc.getDebtId())
                        .personId(person.getId())
                        .personName(person.getName())
                        .allocatedAmount(savedAlloc.getAllocatedAmount())
                        .note(savedAlloc.getNote())
                        .createdAt(savedAlloc.getCreatedAt())
                        .build());
            }
        } else if (personId != null) {
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người", personId));

            DebtType debtType;
            String desc;
            if (tx.getType() == TransactionType.LOAN_GIVEN) {
                debtType = DebtType.RECEIVABLE;
                desc = (tx.getDescription() != null && !tx.getDescription().trim().isEmpty())
                        ? tx.getDescription().trim()
                        : "Cho " + person.getName() + " vay tiền";
            } else if (tx.getType() == TransactionType.BORROW_RECEIVED) {
                debtType = DebtType.PAYABLE;
                desc = (tx.getDescription() != null && !tx.getDescription().trim().isEmpty())
                        ? tx.getDescription().trim()
                        : "Vay tiền từ " + person.getName();
            } else {
                debtType = DebtType.RECEIVABLE;
                desc = tx.getDescription() != null ? tx.getDescription() : "Khoản nợ";
            }

            Debt debt = Debt.builder()
                    .personId(person.getId())
                    .type(debtType)
                    .originType(DebtOriginType.TRANSACTION)
                    .originTransactionId(tx.getId())
                    .description(desc)
                    .principalAmount(tx.getAmount())
                    .dueDate(dueDate)
                    .status(DebtStatus.OPEN)
                    .build();

            Debt savedDebt = debtRepository.save(debt);

            DebtAllocation allocation = DebtAllocation.builder()
                    .transactionId(tx.getId())
                    .debtId(savedDebt.getId())
                    .personId(person.getId())
                    .allocatedAmount(tx.getAmount())
                    .note(desc)
                    .build();

            DebtAllocation savedAlloc = debtAllocationRepository.save(allocation);

            responses.add(DebtAllocationResponse.builder()
                    .id(savedAlloc.getId())
                    .transactionId(savedAlloc.getTransactionId())
                    .debtId(savedAlloc.getDebtId())
                    .personId(person.getId())
                    .personName(person.getName())
                    .allocatedAmount(savedAlloc.getAllocatedAmount())
                    .note(savedAlloc.getNote())
                    .createdAt(savedAlloc.getCreatedAt())
                    .build());
        }

        return responses;
    }

    private void applyDebtPayments(Transaction tx, List<UUID> debtIds, UUID singleDebtId) {
        List<UUID> targetDebtIds = new ArrayList<>();
        if (debtIds != null && !debtIds.isEmpty()) {
            for (UUID dId : debtIds) {
                if (dId != null && !targetDebtIds.contains(dId)) {
                    targetDebtIds.add(dId);
                }
            }
        } else if (singleDebtId != null) {
            targetDebtIds.add(singleDebtId);
        }

        if (targetDebtIds.isEmpty()) {
            return;
        }

        long remainingTxAmount = tx.getAmount() != null ? tx.getAmount() : 0L;
        if (remainingTxAmount <= 0) {
            return;
        }

        for (int i = 0; i < targetDebtIds.size(); i++) {
            UUID debtId = targetDebtIds.get(i);
            boolean isLast = (i == targetDebtIds.size() - 1);

            Debt debt = debtRepository.findById(debtId).orElse(null);
            if (debt == null || debt.getStatus() == DebtStatus.CANCELLED) {
                continue;
            }

            Long paidSoFar = debtPaymentRepository.calculateTotalPaidByDebtId(debt.getId());
            long debtRemaining = Math.max(0L, debt.getPrincipalAmount() - (paidSoFar != null ? paidSoFar : 0L));

            long payAmount;
            if (isLast) {
                payAmount = remainingTxAmount;
            } else {
                payAmount = Math.min(remainingTxAmount, debtRemaining);
            }

            if (payAmount <= 0) {
                continue;
            }

            DebtPayment payment = DebtPayment.builder()
                    .debtId(debt.getId())
                    .transactionId(tx.getId())
                    .amount(payAmount)
                    .paymentDate(tx.getTransactionDate() != null ? tx.getTransactionDate() : OffsetDateTime.now(ZoneOffset.UTC))
                    .note("Thanh toán liên kết giao dịch: " + (tx.getDescription() != null ? tx.getDescription() : ""))
                    .build();
            debtPaymentRepository.save(payment);

            remainingTxAmount -= payAmount;

            long totalPaidAfter = (paidSoFar != null ? paidSoFar : 0L) + payAmount;
            if (totalPaidAfter >= debt.getPrincipalAmount()) {
                debt.setStatus(DebtStatus.PAID);
            } else if (totalPaidAfter > 0) {
                debt.setStatus(DebtStatus.PARTIAL);
            }
            debtRepository.save(debt);

            if (remainingTxAmount <= 0) {
                break;
            }
        }
    }
}
