package com.personalfinance.report.service;

import com.personalfinance.account.dto.AccountResponse;
import com.personalfinance.account.service.AccountService;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import com.personalfinance.debt.repository.DebtPaymentRepository;
import com.personalfinance.debt.repository.DebtRepository;
import com.personalfinance.person.entity.Person;
import com.personalfinance.person.repository.PersonRepository;
import com.personalfinance.report.dto.AccountBalanceItem;
import com.personalfinance.report.dto.CategoryBreakdownResponse;
import com.personalfinance.report.dto.DebtReportItem;
import com.personalfinance.report.dto.MonthlyReportResponse;
import com.personalfinance.report.dto.NetWorthResponse;
import com.personalfinance.report.dto.WeeklyReportResponse;
import com.personalfinance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(LocalDate date) {
        if (date == null) {
            date = LocalDate.now(ZoneOffset.UTC);
        }
        OffsetDateTime startDate = DateTimeUtil.startOfWeek(date);
        OffsetDateTime endDate = DateTimeUtil.endOfWeek(date);

        Long income = transactionRepository.calculateTotalIncome(startDate, endDate);
        Long grossExpense = transactionRepository.calculateGrossExpense(startDate, endDate);
        Long debtAllocations = transactionRepository.calculateTotalDebtAllocations(startDate, endDate);

        long totalIncome = income != null ? income : 0L;
        long totalGrossExpense = grossExpense != null ? grossExpense : 0L;
        long totalDebtAlloc = debtAllocations != null ? debtAllocations : 0L;
        long personalExpense = Math.max(0L, totalGrossExpense - totalDebtAlloc);
        long netCashFlow = totalIncome - totalGrossExpense;

        List<CategoryBreakdownResponse> categoryBreakdown = getCategoryBreakdown(startDate, endDate, personalExpense);

        return WeeklyReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalIncome(totalIncome)
                .grossExpense(totalGrossExpense)
                .debtAllocations(totalDebtAlloc)
                .personalExpense(personalExpense)
                .netCashFlow(netCashFlow)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Integer year, Integer month) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();

        OffsetDateTime startDate = DateTimeUtil.startOfMonth(year, month);
        OffsetDateTime endDate = DateTimeUtil.endOfMonth(year, month);

        Long income = transactionRepository.calculateTotalIncome(startDate, endDate);
        Long grossExpense = transactionRepository.calculateGrossExpense(startDate, endDate);
        Long debtAllocations = transactionRepository.calculateTotalDebtAllocations(startDate, endDate);

        long totalIncome = income != null ? income : 0L;
        long totalGrossExpense = grossExpense != null ? grossExpense : 0L;
        long totalDebtAlloc = debtAllocations != null ? debtAllocations : 0L;
        long personalExpense = Math.max(0L, totalGrossExpense - totalDebtAlloc);
        long netCashFlow = totalIncome - totalGrossExpense;

        List<CategoryBreakdownResponse> categoryBreakdown = getCategoryBreakdown(startDate, endDate, personalExpense);

        // Tính toán tháng trước để so sánh tăng giảm (%)
        int prevMonth = (month == 1) ? 12 : month - 1;
        int prevYear = (month == 1) ? year - 1 : year;
        OffsetDateTime prevStart = DateTimeUtil.startOfMonth(prevYear, prevMonth);
        OffsetDateTime prevEnd = DateTimeUtil.endOfMonth(prevYear, prevMonth);

        Long prevInc = transactionRepository.calculateTotalIncome(prevStart, prevEnd);
        Long prevExp = transactionRepository.calculateGrossExpense(prevStart, prevEnd);
        long prevIncome = prevInc != null ? prevInc : 0L;
        long prevExpense = prevExp != null ? prevExp : 0L;

        Double incomeChange = prevIncome > 0
                ? ((double) (totalIncome - prevIncome) / prevIncome) * 100.0
                : (totalIncome > 0 ? 100.0 : 0.0);

        Double expenseChange = prevExpense > 0
                ? ((double) (totalGrossExpense - prevExpense) / prevExpense) * 100.0
                : (totalGrossExpense > 0 ? 100.0 : 0.0);

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .grossExpense(totalGrossExpense)
                .debtAllocations(totalDebtAlloc)
                .personalExpense(personalExpense)
                .netCashFlow(netCashFlow)
                .categoryBreakdown(categoryBreakdown)
                .previousMonthIncome(prevIncome)
                .previousMonthExpense(prevExpense)
                .incomeChangePercentage(Math.round(incomeChange * 10.0) / 10.0)
                .expenseChangePercentage(Math.round(expenseChange * 10.0) / 10.0)
                .build();
    }

    @Transactional(readOnly = true)
    public NetWorthResponse getNetWorth() {
        // 1. Tổng số dư từ tất cả các tài khoản đang hoạt động
        List<AccountResponse> accounts = accountService.getAllAccounts();
        long totalBalance = accounts.stream().mapToLong(AccountResponse::getCurrentBalance).sum();

        List<AccountBalanceItem> accountItems = accounts.stream()
                .map(a -> AccountBalanceItem.builder()
                        .accountId(a.getId())
                        .accountName(a.getName())
                        .accountType(a.getAccountType())
                        .balance(a.getCurrentBalance())
                        .build())
                .toList();

        // 2. Tổng nợ phải thu (Receivables)
        Long rec = debtRepository.calculateTotalRemainingByType(DebtType.RECEIVABLE.name());
        long totalReceivables = rec != null ? rec : 0L;

        // 3. Tổng nợ phải trả (Payables)
        Long pay = debtRepository.calculateTotalRemainingByType(DebtType.PAYABLE.name());
        long totalPayables = pay != null ? pay : 0L;

        // Danh sách chi tiết các khoản nợ
        List<Debt> openReceivables = debtRepository.findAllByTypeAndStatusIn(
                DebtType.RECEIVABLE, List.of(DebtStatus.OPEN, DebtStatus.PARTIAL));
        List<Debt> openPayables = debtRepository.findAllByTypeAndStatusIn(
                DebtType.PAYABLE, List.of(DebtStatus.OPEN, DebtStatus.PARTIAL));

        List<UUID> personIds = new ArrayList<>();
        openReceivables.forEach(d -> personIds.add(d.getPersonId()));
        openPayables.forEach(d -> personIds.add(d.getPersonId()));
        Map<UUID, String> personNameMap = personRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(Person::getId, Person::getName));

        List<DebtReportItem> receivableItems = openReceivables.stream()
                .map(d -> {
                    Long paid = debtPaymentRepository.calculateTotalPaidByDebtId(d.getId());
                    long rem = d.getPrincipalAmount() - (paid != null ? paid : 0L);
                    return DebtReportItem.builder()
                            .debtId(d.getId())
                            .personName(personNameMap.getOrDefault(d.getPersonId(), "N/A"))
                            .type(d.getType())
                            .remainingAmount(rem)
                            .dueDate(d.getDueDate())
                            .status(d.getStatus())
                            .build();
                }).toList();

        List<DebtReportItem> payableItems = openPayables.stream()
                .map(d -> {
                    Long paid = debtPaymentRepository.calculateTotalPaidByDebtId(d.getId());
                    long rem = d.getPrincipalAmount() - (paid != null ? paid : 0L);
                    return DebtReportItem.builder()
                            .debtId(d.getId())
                            .personName(personNameMap.getOrDefault(d.getPersonId(), "N/A"))
                            .type(d.getType())
                            .remainingAmount(rem)
                            .dueDate(d.getDueDate())
                            .status(d.getStatus())
                            .build();
                }).toList();

        long netWorth = totalBalance + totalReceivables - totalPayables;

        return NetWorthResponse.builder()
                .netWorth(netWorth)
                .totalBalance(totalBalance)
                .totalReceivables(totalReceivables)
                .totalPayables(totalPayables)
                .accounts(accountItems)
                .receivables(receivableItems)
                .payables(payableItems)
                .build();
    }

    private List<CategoryBreakdownResponse> getCategoryBreakdown(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            long totalExpense) {

        List<Object[]> rows = transactionRepository.findExpenseByCategory(startDate, endDate);
        List<CategoryBreakdownResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            UUID catId = (UUID) row[0];
            String name = (String) row[1];
            String icon = (String) row[2];
            String color = (String) row[3];
            long amount = ((Number) row[4]).longValue();

            double percentage = totalExpense > 0
                    ? ((double) amount / totalExpense) * 100.0
                    : 0.0;

            result.add(CategoryBreakdownResponse.builder()
                    .categoryId(catId)
                    .categoryName(name)
                    .icon(icon)
                    .color(color)
                    .amount(amount)
                    .percentage(Math.round(percentage * 10.0) / 10.0)
                    .build());
        }

        return result;
    }
}
