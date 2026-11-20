package com.personalfinance.category.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryStatusRequest {

    @JsonAlias({"active", "status"})
    private Boolean isActive;
}
