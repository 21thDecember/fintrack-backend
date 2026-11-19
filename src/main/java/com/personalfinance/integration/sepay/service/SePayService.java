package com.personalfinance.integration.sepay.service;

import com.personalfinance.account.entity.Account;
import com.personalfinance.account.entity.AccountType;
import com.personalfinance.account.repository.AccountRepository;
import com.personalfinance.category.entity.Category;
import com.personalfinance.category.entity.CategoryType;
import com.personalfinance.category.repository.CategoryRepository;
import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.integration.sepay.dto.SePayWebhookPayload;
import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.Transaction;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionSource;
import com.personalfinance.transaction.entity.TransactionStatus;
import com.personalfinance.transaction.entity.TransactionType;
import com.personalfinance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SePayService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.sepay.webhook-api-key:}")
    private String webhookApiKey;

    /**
     * Xác thực Webhook API key từ SePay (nếu có cấu hình)
     */
    public void verifyWebhookAuth(String authHeader) {
        if (webhookApiKey != null && !webhookApiKey.trim().isEmpty()) {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                throw new BusinessException("Thiếu thông tin xác thực SePay Webhook", HttpStatus.UNAUTHORIZED);
            }
            String cleanedHeader = authHeader.replace("Apikey", "").replace("Bearer", "").trim();
            if (!webhookApiKey.trim().equals(cleanedHeader)) {
                log.warn("SePay Webhook Authorization header không khớp");
                throw new BusinessException("Mã xác thực SePay Webhook không hợp lệ", HttpStatus.UNAUTHORIZED);
            }
        }
    }

    /**
     * Xử lý webhook từ SePay đảm bảo tính Idempotent (chống trùng lặp giao dịch)
     */
    @Transactional
    public Transaction processWebhook(SePayWebhookPayload payload) {
        if (payload == null || payload.getId() == null) {
            throw new BusinessException("Dữ liệu webhook không hợp lệ: thiếu mã giao dịch");
        }

        String externalId = String.valueOf(payload.getId());

        // 1. Kiểm tra tính Idempotent: Nếu đã tồn tại giao dịch ngân hàng này thì bỏ qua
        Optional<Transaction> existingTx = transactionRepository.findBySourceAndExternalTransactionId(
                TransactionSource.BANK, externalId);

        if (existingTx.isPresent()) {
            log.info("Bỏ qua giao dịch SePay trùng lặp (Idempotent): ExternalID={}", externalId);
            return existingTx.get();
        }

        // 2. Tìm hoặc tạo tài khoản tương ứng
        Account account = resolveAccount(payload);

        // 3. Xác định chiều dòng tiền và loại giao dịch
        boolean isIncome = "in".equalsIgnoreCase(payload.getTransferType());
        TransactionDirection direction = isIncome ? TransactionDirection.IN : TransactionDirection.OUT;
        TransactionType type = isIncome ? TransactionType.INCOME : TransactionType.EXPENSE;

        // 4. Phân tích nội dung và tự động phân loại danh mục (Auto-classify)
        Category matchedCategory = classifyCategory(payload.getContent(), type);

        ClassificationStatus classificationStatus = (matchedCategory != null)
                ? ClassificationStatus.CLASSIFIED
                : ClassificationStatus.UNCLASSIFIED;

        // 5. Chuẩn hóa thời gian
        OffsetDateTime txDate = DateTimeUtil.parseDateTimeString(payload.getTransactionDate());

        // 6. Tạo và lưu bản ghi giao dịch
        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .categoryId(matchedCategory != null ? matchedCategory.getId() : null)
                .type(type)
                .amount(payload.getTransferAmount() != null ? payload.getTransferAmount() : 0L)
                .direction(direction)
                .source(TransactionSource.BANK)
                .externalTransactionId(externalId)
                .externalReference(payload.getReferenceCode())
                .originalDescription(payload.getContent())
                .description(payload.getDescription() != null && !payload.getDescription().trim().isEmpty()
                        ? payload.getDescription().trim()
                        : payload.getContent())
                .note(String.format("Cổng: %s | Tích lũy: %s",
                        payload.getGateway(),
                        payload.getAccumulated() != null ? payload.getAccumulated().toString() : "N/A"))
                .transactionDate(txDate)
                .classificationStatus(classificationStatus)
                .status(TransactionStatus.POSTED)
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("Xử lý thành công SePay webhook: ID={}, Số tiền={}, Hướng={}, Danh mục={}",
                savedTx.getId(), savedTx.getAmount(), savedTx.getDirection(),
                matchedCategory != null ? matchedCategory.getName() : "Chưa phân loại");

        return savedTx;
    }

    private Account resolveAccount(SePayWebhookPayload payload) {
        if (payload.getAccountNumber() != null && !payload.getAccountNumber().trim().isEmpty()) {
            Optional<Account> accOpt = accountRepository.findByAccountNumberAndIsActiveTrue(payload.getAccountNumber().trim());
            if (accOpt.isPresent()) {
                return accOpt.get();
            }
        }

        // Nếu không tìm thấy số tài khoản cụ thể, lấy tài khoản ngân hàng đầu tiên đang kích hoạt
        Optional<Account> defaultBankAcc = accountRepository.findFirstByAccountTypeAndIsActiveTrue(AccountType.BANK);
        if (defaultBankAcc.isPresent()) {
            return defaultBankAcc.get();
        }

        // Tạo tài khoản ngân hàng mặc định nếu hệ thống chưa có
        String accName = (payload.getGateway() != null && !payload.getGateway().trim().isEmpty())
                ? "Tài khoản " + payload.getGateway().trim()
                : "Tài khoản Ngân hàng";

        Account newAcc = Account.builder()
                .name(accName)
                .accountType(AccountType.BANK)
                .bankCode(payload.getGateway())
                .accountNumber(payload.getAccountNumber())
                .openingBalance(0L)
                .isActive(true)
                .build();

        Account saved = accountRepository.save(newAcc);
        log.info("Đã tự động khởi tạo tài khoản ngân hàng: {} (ID={})", saved.getName(), saved.getId());
        return saved;
    }

    /**
     * Tự động nhận diện danh mục dựa trên từ khóa trong nội dung chuyển khoản
     */
    private Category classifyCategory(String content, TransactionType type) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        String lower = content.toLowerCase();

        if (type == TransactionType.INCOME) {
            if (lower.contains("luong") || lower.contains("salary") || lower.contains("cong ty tra luong")) {
                return findCategoryByName("Lương", CategoryType.INCOME);
            }
            if (lower.contains("thuong") || lower.contains("bonus") || lower.contains("khen thuong")) {
                return findCategoryByName("Thưởng", CategoryType.INCOME);
            }
            return findCategoryByName("Thu nhập khác", CategoryType.INCOME);
        } else {
            // Chi tiêu
            if (lower.contains("an") || lower.contains("com") || lower.contains("pho") ||
                    lower.contains("coffee") || lower.contains("cafe") || lower.contains("highlands") ||
                    lower.contains("food") || lower.contains("grabfood") || lower.contains("shopeefood") ||
                    lower.contains("baemin") || lower.contains("quan an") || lower.contains("nha hang")) {
                return findCategoryByName("Ăn uống", CategoryType.EXPENSE);
            }
            if (lower.contains("xang") || lower.contains("grab") || lower.contains("be") ||
                    lower.contains("taxi") || lower.contains("gojek") || lower.contains("gui xe")) {
                return findCategoryByName("Di chuyển", CategoryType.EXPENSE);
            }
            if (lower.contains("shopee") || lower.contains("lazada") || lower.contains("tiki") ||
                    lower.contains("tiktok") || lower.contains("sieu thi") || lower.contains("vinmart") ||
                    lower.contains("winmart") || lower.contains("bach hoa")) {
                return findCategoryByName("Mua sắm", CategoryType.EXPENSE);
            }
            if (lower.contains("rap") || lower.contains("cgv") || lower.contains("cinema") ||
                    lower.contains("game") || lower.contains("netflix") || lower.contains("spotify")) {
                return findCategoryByName("Giải trí", CategoryType.EXPENSE);
            }
            if (lower.contains("thuoc") || lower.contains("benh vien") || lower.contains("kham") ||
                    lower.contains("pharmacy") || lower.contains("nha thuoc")) {
                return findCategoryByName("Sức khỏe", CategoryType.EXPENSE);
            }
            if (lower.contains("hoc phi") || lower.contains("khoa hoc") || lower.contains("sach")) {
                return findCategoryByName("Giáo dục", CategoryType.EXPENSE);
            }
            if (lower.contains("tien nha") || lower.contains("phong tro") || lower.contains("sua nha")) {
                return findCategoryByName("Nhà ở", CategoryType.EXPENSE);
            }
            if (lower.contains("dien") || lower.contains("nuoc") || lower.contains("internet") ||
                    lower.contains("viettel") || lower.contains("fpt") || lower.contains("vnpt") || lower.contains("hoa don")) {
                return findCategoryByName("Hóa đơn & Tiện ích", CategoryType.EXPENSE);
            }
        }
        return null;
    }

    private Category findCategoryByName(String name, CategoryType type) {
        return categoryRepository.findFirstByNameIgnoreCaseAndTypeAndIsActiveTrue(name, type)
                .orElse(null);
    }
}
