package com.personalfinance.category.dto;

import com.personalfinance.category.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @NotNull(message = "Loại danh mục không được để trống")
    private CategoryType type;

    private String icon;

    private String color;

    private UUID parentId;
}
