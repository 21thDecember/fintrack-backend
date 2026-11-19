package com.personalfinance.budget.service;

import com.personalfinance.budget.dto.BudgetResponse;
import com.personalfinance.budget.dto.CreateBudgetRequest;
import com.personalfinance.budget.dto.UpdateBudgetRequest;
import com.personalfinance.budget.entity.Budget;
import com.personalfinance.budget.repository.BudgetRepository;
import com.personalfinance.category.entity.Category;
import com.personalfinance.category.repository.CategoryRepository;
import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Integer year, Integer month) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        List<Budget> budgets = budgetRepository.findAllByYearAndMonth(year, month);
        return budgets.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ngân sách", id));
        return toResponse(budget);
    }

    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));

        Optional<Budget> existing = budgetRepository.findByCategoryIdAndYearAndMonth(
                request.getCategoryId(), request.getYear(), request.getMonth());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setLimitAmount(request.getLimitAmount());
            log.info("Cập nhật hạn mức ngân sách đã tồn tại cho danh mục {}: {}", category.getName(), request.getLimitAmount());
        } else {
            budget = Budget.builder()
                    .categoryId(category.getId())
                    .year(request.getYear())
                    .month(request.getMonth())
                    .limitAmount(request.getLimitAmount())
                    .build();
            log.info("Tạo ngân sách mới cho danh mục {}: {}", category.getName(), request.getLimitAmount());
        }

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional
    public BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ngân sách", id));

        budget.setLimitAmount(request.getLimitAmount());
        Budget updated = budgetRepository.save(budget);
        log.info("Đã cập nhật ngân sách ID={}: hạn mức mới={}", updated.getId(), updated.getLimitAmount());
        return toResponse(updated);
    }

    @Transactional
    public void deleteBudget(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ngân sách", id));
        budgetRepository.delete(budget);
        log.info("Đã xóa ngân sách ID={}", id);
    }

    public BudgetResponse toResponse(Budget budget) {
        Category category = categoryRepository.findById(budget.getCategoryId()).orElse(null);

        Long spent = budgetRepository.calculateSpentAmount(budget.getCategoryId(), budget.getYear(), budget.getMonth());
        long spentAmount = spent != null ? spent : 0L;
        long remainingAmount = budget.getLimitAmount() - spentAmount;
        double percentage = budget.getLimitAmount() > 0
                ? ((double) spentAmount / budget.getLimitAmount()) * 100.0
                : 0.0;

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .categoryColor(category != null ? category.getColor() : null)
                .year(budget.getYear())
                .month(budget.getMonth())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .createdAt(budget.getCreatedAt())
                .build();
    }
}
