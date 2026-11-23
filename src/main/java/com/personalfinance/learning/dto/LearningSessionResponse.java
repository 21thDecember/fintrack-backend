package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.SessionSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSessionResponse {

    private UUID id;
    private UUID userId;
    private UUID courseId;
    private String courseTitle;
    private UUID bookId;
    private String bookTitle;
    private UUID skillId;
    private String skillName;
    private String title;
    private String description;
    private LocalDate learningDate;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer durationMinutes;
    private SessionSource source;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
