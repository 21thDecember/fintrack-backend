package com.personalfinance.notification.service;

import com.personalfinance.budget.dto.BudgetResponse;
import com.personalfinance.budget.service.BudgetService;
import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.repository.DebtPaymentRepository;
import com.personalfinance.debt.repository.DebtRepository;
import com.personalfinance.notification.dto.NotificationAlertDto;
import com.personalfinance.person.entity.Person;
import com.personalfinance.person.repository.PersonRepository;
import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.TransactionStatus;
import com.personalfinance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final PersonRepository personRepository;
    private final BudgetService budgetService;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<NotificationAlertDto> getActiveAlerts() {
        List<NotificationAlertDto> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 1. Cảnh báo các khoản nợ đến hạn và quá hạn
        List<Debt> openDebts = debtRepository.findUpcomingDebts(today.plusDays(3), 50);
        if (!openDebts.isEmpty()) {
            List<UUID> personIds = openDebts.stream().map(Debt::getPersonId).distinct().toList();
            Map<UUID, String> personNames = personRepository.findAllById(personIds).stream()
                    .collect(Collectors.toMap(Person::getId, Person::getName));

            for (Debt debt : openDebts) {
                if (debt.getDueDate() == null) continue;

                Long paid = debtPaymentRepository.calculateTotalPaidByDebtId(debt.getId());
                long remaining = debt.getPrincipalAmount() - (paid != null ? paid : 0L);
                if (remaining <= 0) continue;

                String personName = personNames.getOrDefault(debt.getPersonId(), "Đối tượng");
                long days = ChronoUnit.DAYS.between(today, debt.getDueDate());

                if (days < 0) {
                    alerts.add(NotificationAlertDto.builder()
                            .id("DEBT_OVERDUE_" + debt.getId())
                            .type("DEBT_OVERDUE")
                            .title("Khoản nợ quá hạn!")
                            .message(String.format("Khoản nợ %s với %s (%d VND) đã quá hạn %d ngày",
                                    debt.getType().name(), personName, remaining, Math.abs(days)))
                            .severity("CRITICAL")
                            .timestamp(now)
                            .payload(Map.of("debtId", debt.getId(), "remaining", remaining, "daysOverdue", Math.abs(days)))
                            .build());
                } else {
                    alerts.add(NotificationAlertDto.builder()
                            .id("DEBT_DUE_" + debt.getId())
                            .type("DEBT_DUE")
                            .title("Khoản nợ sắp đến hạn")
                            .message(String.format("Khoản nợ %s với %s (%d VND) sẽ đến hạn trong %d ngày",
                                    debt.getType().name(), personName, remaining, days))
                            .severity("WARNING")
                            .timestamp(now)
                            .payload(Map.of("debtId", debt.getId(), "remaining", remaining, "daysRemaining", days))
                            .build());
                }
            }
        }

        // 2. Cảnh báo vượt hoặc chạm ngưỡng ngân sách trong tháng này
        List<BudgetResponse> budgets = budgetService.getBudgets(today.getYear(), today.getMonthValue());
        for (BudgetResponse b : budgets) {
            if (b.getLimitAmount() <= 0) continue;

            if (b.getSpentAmount() > b.getLimitAmount()) {
                alerts.add(NotificationAlertDto.builder()
                        .id("BUDGET_EXCEEDED_" + b.getId())
                        .type("BUDGET_EXCEEDED")
                        .title("Vượt ngân sách chi tiêu!")
                        .message(String.format("Danh mục '%s' đã vượt hạn mức %d VND (Đã chi %d/%d VND - %.1f%%)",
                                b.getCategoryName(), b.getSpentAmount() - b.getLimitAmount(),
                                b.getSpentAmount(), b.getLimitAmount(), b.getPercentage()))
                        .severity("CRITICAL")
                        .timestamp(now)
                        .payload(b)
                        .build());
            } else if (b.getPercentage() >= 90.0) {
                alerts.add(NotificationAlertDto.builder()
                        .id("BUDGET_WARNING_" + b.getId())
                        .type("BUDGET_WARNING")
                        .title("Cảnh báo chạm ngưỡng ngân sách")
                        .message(String.format("Danh mục '%s' đã đạt %.1f%% hạn mức (Đã chi %d/%d VND)",
                                b.getCategoryName(), b.getPercentage(), b.getSpentAmount(), b.getLimitAmount()))
                        .severity("WARNING")
                        .timestamp(now)
                        .payload(b)
                        .build());
            }
        }

        // 3. Cảnh báo giao dịch chưa phân loại
        long unclassifiedCount = transactionRepository.countByClassificationStatusAndStatus(
                ClassificationStatus.UNCLASSIFIED, TransactionStatus.POSTED);
        if (unclassifiedCount > 0) {
            alerts.add(NotificationAlertDto.builder()
                    .id("UNCLASSIFIED_TRANSACTIONS")
                    .type("UNCLASSIFIED_TX")
                    .title("Giao dịch chưa phân loại")
                    .message(String.format("Bạn có %d giao dịch ngân hàng cần phân loại danh mục", unclassifiedCount))
                    .severity("INFO")
                    .timestamp(now)
                    .payload(Map.of("count", unclassifiedCount))
                    .build());
        }

        return alerts;
    }
}
