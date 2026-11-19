package com.personalfinance.person.dto;

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
public class PersonResponse {

    private UUID id;
    private String name;
    private String phone;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
