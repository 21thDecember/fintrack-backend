package com.personalfinance.category.service;

import com.personalfinance.category.dto.CategoryResponse;
import com.personalfinance.category.dto.CreateCategoryRequest;
import com.personalfinance.category.dto.UpdateCategoryRequest;
import com.personalfinance.category.entity.Category;
import com.personalfinance.category.entity.CategoryType;
import com.personalfinance.category.repository.CategoryRepository;
import com.personalfinance.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(CategoryType type) {
        List<Category> categories = (type != null)
                ? categoryRepository.findByTypeAndIsActiveTrueOrderByNameAsc(type)
                : categoryRepository.findAllByIsActiveTrueOrderByNameAsc();

        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public Category getEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName().trim())
                .type(request.getType())
                .icon(request.getIcon())
                .color(request.getColor())
                .parentId(request.getParentId())
                .isActive(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Đã tạo danh mục: {} (ID: {})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));

        category.setName(request.getName().trim());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setParentId(request.getParentId());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updated = categoryRepository.save(category);
        log.info("Đã cập nhật danh mục: {} (ID: {})", updated.getName(), updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));

        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Đã vô hiệu hóa (soft delete) danh mục: {} (ID: {})", category.getName(), category.getId());
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .parentId(category.getParentId())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
