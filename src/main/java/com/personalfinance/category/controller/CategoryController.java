package com.personalfinance.category.controller;

import com.personalfinance.category.dto.CategoryResponse;
import com.personalfinance.category.dto.CreateCategoryRequest;
import com.personalfinance.category.dto.UpdateCategoryRequest;
import com.personalfinance.category.entity.CategoryType;
import com.personalfinance.category.service.CategoryService;
import com.personalfinance.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.personalfinance.category.dto.UpdateCategoryStatusRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories(
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Boolean activeOnly) {
        List<CategoryResponse> categories = categoryService.getCategories(type, activeOnly);
        return ApiResponse.success(categories);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CategoryResponse> toggleCategoryStatus(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateCategoryStatusRequest request,
            @RequestParam(required = false) Boolean active) {
        Boolean isActive = null;
        if (request != null && request.getIsActive() != null) {
            isActive = request.getIsActive();
        } else if (active != null) {
            isActive = active;
        }
        CategoryResponse category = categoryService.toggleCategoryStatus(id, isActive);
        return ApiResponse.success("Cập nhật trạng thái danh mục thành công", category);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<CategoryResponse> updateCategoryStatusPut(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateCategoryStatusRequest request,
            @RequestParam(required = false) Boolean active) {
        return toggleCategoryStatus(id, request, active);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ApiResponse.success(category);
    }

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ApiResponse.success("Tạo danh mục thành công", category);
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ApiResponse.success("Cập nhật danh mục thành công", category);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("Xóa danh mục thành công", null);
    }
}
