package com.personalfinance.habit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinRequest {

    private LocalDate date;

    private Integer count;

    private String note;
}
