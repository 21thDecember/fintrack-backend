package com.personalfinance.category.dto;

import com.personalfinance.category.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;
    private String name;
    private CategoryType type;
    private String icon;
    private String color;
    private UUID parentId;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}
