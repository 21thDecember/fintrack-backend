package com.personalfinance.budget.controller;

import com.personalfinance.budget.dto.BudgetResponse;
import com.personalfinance.budget.dto.CreateBudgetRequest;
import com.personalfinance.budget.dto.UpdateBudgetRequest;
import com.personalfinance.budget.service.BudgetService;
import com.personalfinance.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ApiResponse<List<BudgetResponse>> getBudgets(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<BudgetResponse> budgets = budgetService.getBudgets(year, month);
        return ApiResponse.success(budgets);
    }

    @GetMapping("/{id}")
    public ApiResponse<BudgetResponse> getBudgetById(@PathVariable UUID id) {
        BudgetResponse budget = budgetService.getBudgetById(id);
        return ApiResponse.success(budget);
    }

    @PostMapping
    public ApiResponse<BudgetResponse> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse budget = budgetService.createBudget(request);
        return ApiResponse.success("Thiết lập ngân sách thành công", budget);
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetResponse> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse budget = budgetService.updateBudget(id, request);
        return ApiResponse.success("Cập nhật ngân sách thành công", budget);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ApiResponse.success("Xóa ngân sách thành công", null);
    }
}
