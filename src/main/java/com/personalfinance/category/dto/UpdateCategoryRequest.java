package com.personalfinance.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String icon;

    private String color;

    private UUID parentId;

    private Boolean isActive;
}
