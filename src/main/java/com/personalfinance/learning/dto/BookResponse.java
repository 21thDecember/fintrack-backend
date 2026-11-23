package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.BookStatus;
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
public class BookResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String goalTitle;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private String category;
    private String url;
    private BookStatus status;
    private Integer totalPages;
    private Integer currentPage;
    private Integer progressPercent;
    private Integer totalDurationMinutes;
    private LocalDate startDate;
    private LocalDate targetDate;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
