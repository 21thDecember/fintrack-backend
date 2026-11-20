package com.personalfinance.dashboard.service;

import com.personalfinance.account.service.AccountService;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.dashboard.dto.CategoryExpenseChartItem;
import com.personalfinance.dashboard.dto.DashboardResponse;
import com.personalfinance.dashboard.dto.IncomeExpenseChartItem;
import com.personalfinance.dashboard.dto.PeriodComparisonResponse;
import com.personalfinance.dashboard.dto.UpcomingDebtItem;
import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtType;
import com.personalfinance.debt.repository.DebtPaymentRepository;
import com.personalfinance.debt.repository.DebtRepository;
import com.personalfinance.person.entity.Person;
import com.personalfinance.person.repository.PersonRepository;
import com.personalfinance.transaction.dto.TransactionResponse;
import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.Transaction;
import com.personalfinance.transaction.entity.TransactionStatus;
import com.personalfinance.transaction.repository.TransactionRepository;
import com.personalfinance.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String periodStr) {
        if (periodStr == null || periodStr.trim().isEmpty()) {
            periodStr = "THIS_MONTH";
        }
        periodStr = periodStr.trim().toUpperCase();

        LocalDate now = LocalDate.now(DateTimeUtil.ZONE_VN);
        OffsetDateTime startDate;
        OffsetDateTime endDate;
        OffsetDateTime prevStartDate;
        OffsetDateTime prevEndDate;

        switch (periodStr) {
            case "TODAY":
                startDate = DateTimeUtil.startOfDay(now);
                endDate = DateTimeUtil.endOfDay(now);
                prevStartDate = startDate.minusDays(1);
                prevEndDate = endDate.minusDays(1);
                break;
            case "THIS_WEEK":
                startDate = DateTimeUtil.startOfWeek(now);
                endDate = DateTimeUtil.endOfWeek(now);
                prevStartDate = startDate.minusWeeks(1);
                prevEndDate = endDate.minusWeeks(1);
                break;
            case "LAST_MONTH":
                int lastMonthVal = (now.getMonthValue() == 1) ? 12 : now.getMonthValue() - 1;
                int lastMonthYear = (now.getMonthValue() == 1) ? now.getYear() - 1 : now.getYear();
                startDate = DateTimeUtil.startOfMonth(lastMonthYear, lastMonthVal);
                endDate = DateTimeUtil.endOfMonth(lastMonthYear, lastMonthVal);
                int prevLastMonthVal = (lastMonthVal == 1) ? 12 : lastMonthVal - 1;
                int prevLastMonthYear = (lastMonthVal == 1) ? lastMonthYear - 1 : lastMonthYear;
                prevStartDate = DateTimeUtil.startOfMonth(prevLastMonthYear, prevLastMonthVal);
                prevEndDate = DateTimeUtil.endOfMonth(prevLastMonthYear, prevLastMonthVal);
                break;
            case "THIS_YEAR":
                startDate = DateTimeUtil.startOfDay(LocalDate.of(now.getYear(), 1, 1));
                endDate = DateTimeUtil.endOfDay(LocalDate.of(now.getYear(), 12, 31));
                prevStartDate = DateTimeUtil.startOfDay(LocalDate.of(now.getYear() - 1, 1, 1));
                prevEndDate = DateTimeUtil.endOfDay(LocalDate.of(now.getYear() - 1, 12, 31));
                break;
            case "THIS_MONTH":
            default:
                periodStr = "THIS_MONTH";
                startDate = DateTimeUtil.startOfMonth(now.getYear(), now.getMonthValue());
                endDate = DateTimeUtil.endOfMonth(now.getYear(), now.getMonthValue());
                int prevMonth = (now.getMonthValue() == 1) ? 12 : now.getMonthValue() - 1;
                int prevYear = (now.getMonthValue() == 1) ? now.getYear() - 1 : now.getYear();
                prevStartDate = DateTimeUtil.startOfMonth(prevYear, prevMonth);
                prevEndDate = DateTimeUtil.endOfMonth(prevYear, prevMonth);
                break;
        }

        // 1. Số dư và Tài sản ròng
        Long totalBalance = accountService.getAccountsSummary().getTotalBalance();
        Long totalReceivables = debtRepository.calculateTotalRemainingByType(DebtType.RECEIVABLE.name());
        Long totalPayables = debtRepository.calculateTotalRemainingByType(DebtType.PAYABLE.name());

        long balance = totalBalance != null ? totalBalance : 0L;
        long receivables = totalReceivables != null ? totalReceivables : 0L;
        long payables = totalPayables != null ? totalPayables : 0L;
        long netWorth = balance + receivables - payables;

        // 2. Thu chi trong kỳ
        Long inc = transactionRepository.calculateTotalIncome(startDate, endDate);
        Long grossExp = transactionRepository.calculateGrossExpense(startDate, endDate);
        Long alloc = transactionRepository.calculateTotalDebtAllocations(startDate, endDate);

        long income = inc != null ? inc : 0L;
        long grossExpense = grossExp != null ? grossExp : 0L;
        long debtAllocations = alloc != null ? alloc : 0L;
        long personalExpense = Math.max(0L, grossExpense - debtAllocations);
        long netIncome = income - personalExpense;

        // 3. So sánh với kỳ trước
        Long prevInc = transactionRepository.calculateTotalIncome(prevStartDate, prevEndDate);
        Long prevExp = transactionRepository.calculateGrossExpense(prevStartDate, prevEndDate);
        long previousIncome = prevInc != null ? prevInc : 0L;
        long previousExpense = prevExp != null ? prevExp : 0L;

        Double incChange = previousIncome > 0
                ? ((double) (income - previousIncome) / previousIncome) * 100.0
                : (income > 0 ? 100.0 : 0.0);

        Double expChange = previousExpense > 0
                ? ((double) (grossExpense - previousExpense) / previousExpense) * 100.0
                : (grossExpense > 0 ? 100.0 : 0.0);

        PeriodComparisonResponse periodComparison = PeriodComparisonResponse.builder()
                .previousIncome(previousIncome)
                .previousExpense(previousExpense)
                .incomeChangePercentage(Math.round(incChange * 10.0) / 10.0)
                .expenseChangePercentage(Math.round(expChange * 10.0) / 10.0)
                .build();

        // 4. Số lượng giao dịch chưa phân loại
        long unclassifiedCount = transactionRepository.countByClassificationStatusAndStatus(
                ClassificationStatus.UNCLASSIFIED, TransactionStatus.POSTED);

        // 5. Giao dịch gần đây (10 giao dịch mới nhất)
        List<Transaction> recentTxList = transactionRepository.findTop10ByStatusOrderByTransactionDateDesc(TransactionStatus.POSTED);
        List<TransactionResponse> recentTransactions = recentTxList.stream()
                .map(tx -> transactionService.getTransactionById(tx.getId()))
                .toList();

        // 6. Các khoản nợ sắp đến hạn / quá hạn (trong vòng 14 ngày tới)
        List<Debt> upcomingDebtList = debtRepository.findUpcomingDebts(now.plusDays(14), 10);
        List<UpcomingDebtItem> upcomingDebts = mapUpcomingDebts(upcomingDebtList, now);

        // 7. Biểu đồ thu chi và cơ cấu chi tiêu
        List<IncomeExpenseChartItem> incomeExpenseChart = buildIncomeExpenseChart(periodStr, startDate, endDate);

        List<Object[]> catRows = transactionRepository.findExpenseByCategory(startDate, endDate);
        List<CategoryExpenseChartItem> categoryExpenseChart = catRows.stream()
                .map(row -> CategoryExpenseChartItem.builder()
                        .name(String.valueOf(row[1]))
                        .icon(row[2] != null ? String.valueOf(row[2]) : "folder")
                        .color(row[3] != null ? String.valueOf(row[3]) : "#94A3B8")
                        .amount(((Number) row[4]).longValue())
                        .build())
                .filter(item -> item.getAmount() > 0)
                .toList();

        return DashboardResponse.builder()
                .period(periodStr)
                .totalBalance(balance)
                .totalReceivables(receivables)
                .totalPayables(payables)
                .netWorth(netWorth)
                .income(income)
                .grossExpense(grossExpense)
                .personalExpense(personalExpense)
                .netIncome(netIncome)
                .unclassifiedCount(unclassifiedCount)
                .periodComparison(periodComparison)
                .recentTransactions(recentTransactions)
                .upcomingDebts(upcomingDebts)
                .incomeExpenseChart(incomeExpenseChart)
                .categoryExpenseChart(categoryExpenseChart)
                .build();
    }

    private List<IncomeExpenseChartItem> buildIncomeExpenseChart(String periodStr, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<IncomeExpenseChartItem> chartItems = new ArrayList<>();
        if ("TODAY".equals(periodStr)) {
            List<Object[]> hourRows = transactionRepository.findHourlyIncomeExpense(startDate, endDate);
            long[] slotIncome = new long[4];
            long[] slotExpense = new long[4];
            for (Object[] r : hourRows) {
                int hour = ((Number) r[0]).intValue();
                long inc = ((Number) r[1]).longValue();
                long exp = ((Number) r[2]).longValue();
                int slotIdx = Math.min(3, hour / 6);
                slotIncome[slotIdx] += inc;
                slotExpense[slotIdx] += exp;
            }
            String[] labels = {"00:00 - 06:00", "06:00 - 12:00", "12:00 - 18:00", "18:00 - 24:00"};
            for (int i = 0; i < 4; i++) {
                chartItems.add(IncomeExpenseChartItem.builder()
                        .label(labels[i])
                        .income(slotIncome[i])
                        .expense(slotExpense[i])
                        .build());
            }
        } else if ("THIS_YEAR".equals(periodStr)) {
            List<Object[]> monthRows = transactionRepository.findMonthlyIncomeExpense(startDate, endDate);
            Map<Integer, long[]> monthMap = new HashMap<>();
            for (Object[] r : monthRows) {
                int m = ((Number) r[0]).intValue();
                long inc = ((Number) r[1]).longValue();
                long exp = ((Number) r[2]).longValue();
                monthMap.put(m, new long[]{inc, exp});
            }
            for (int m = 1; m <= 12; m++) {
                long[] val = monthMap.getOrDefault(m, new long[]{0, 0});
                chartItems.add(IncomeExpenseChartItem.builder()
                        .label("Th" + m)
                        .income(val[0])
                        .expense(val[1])
                        .build());
            }
        } else {
            // THIS_WEEK, THIS_MONTH, LAST_MONTH (daily grouping)
            List<Object[]> dailyRows = transactionRepository.findDailyIncomeExpense(startDate, endDate);
            Map<String, long[]> dateMap = new HashMap<>();
            for (Object[] r : dailyRows) {
                String dStr = String.valueOf(r[0]);
                long inc = ((Number) r[1]).longValue();
                long exp = ((Number) r[2]).longValue();
                dateMap.put(dStr, new long[]{inc, exp});
            }

            LocalDate startLocal = startDate.atZoneSameInstant(DateTimeUtil.ZONE_VN).toLocalDate();
            LocalDate endLocal = endDate.atZoneSameInstant(DateTimeUtil.ZONE_VN).toLocalDate();

            DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd/MM");
            LocalDate cur = startLocal;
            while (!cur.isAfter(endLocal)) {
                String key = cur.format(DateTimeFormatter.ISO_LOCAL_DATE);
                long[] val = dateMap.getOrDefault(key, new long[]{0, 0});

                String label;
                if ("THIS_WEEK".equals(periodStr)) {
                    String dow = switch (cur.getDayOfWeek()) {
                        case MONDAY -> "T2";
                        case TUESDAY -> "T3";
                        case WEDNESDAY -> "T4";
                        case THURSDAY -> "T5";
                        case FRIDAY -> "T6";
                        case SATURDAY -> "T7";
                        case SUNDAY -> "CN";
                    };
                    label = dow + " " + cur.format(labelFormatter);
                } else {
                    label = cur.format(labelFormatter);
                }

                chartItems.add(IncomeExpenseChartItem.builder()
                        .label(label)
                        .income(val[0])
                        .expense(val[1])
                        .build());
                cur = cur.plusDays(1);
            }
        }
        return chartItems;
    }

    private List<UpcomingDebtItem> mapUpcomingDebts(List<Debt> debts, LocalDate now) {
        if (debts.isEmpty()) return List.of();

        List<UUID> personIds = debts.stream().map(Debt::getPersonId).distinct().toList();
        Map<UUID, String> personNameMap = personRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(Person::getId, Person::getName));

        List<UpcomingDebtItem> items = new ArrayList<>();
        for (Debt d : debts) {
            Long paid = debtPaymentRepository.calculateTotalPaidByDebtId(d.getId());
            long remaining = d.getPrincipalAmount() - (paid != null ? paid : 0L);

            Long days = (d.getDueDate() != null) ? ChronoUnit.DAYS.between(now, d.getDueDate()) : null;

            items.add(UpcomingDebtItem.builder()
                    .debtId(d.getId())
                    .personName(personNameMap.getOrDefault(d.getPersonId(), "N/A"))
                    .type(d.getType())
                    .remainingAmount(remaining)
                    .dueDate(d.getDueDate())
                    .daysRemaining(days)
                    .status(d.getStatus())
                    .build());
        }
        return items;
    }
}
