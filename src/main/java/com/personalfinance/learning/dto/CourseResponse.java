package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.CoursePriority;
import com.personalfinance.learning.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String goalTitle;
    private String title;
    private String description;
    private String provider;
    private String url;
    private String category;
    private CourseStatus status;
    private CoursePriority priority;
    private LocalDate startDate;
    private LocalDate targetDate;
    private BigDecimal estimatedHours;
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer progressPercent;
    private Integer totalDurationMinutes;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
