package com.personalfinance.transaction.dto;

import com.personalfinance.transaction.entity.ClassificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassificationRequest {
    private UUID categoryId;
    private ClassificationStatus classificationStatus;
}
