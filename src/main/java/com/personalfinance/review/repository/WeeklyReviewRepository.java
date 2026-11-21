package com.personalfinance.review.repository;

import com.personalfinance.review.entity.WeeklyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyReviewRepository extends JpaRepository<WeeklyReview, UUID> {

    Optional<WeeklyReview> findByUserIdAndWeekStartDate(UUID userId, LocalDate weekStartDate);

    Optional<WeeklyReview> findByIdAndUserId(UUID id, UUID userId);

    Page<WeeklyReview> findByUserIdOrderByWeekStartDateDesc(UUID userId, Pageable pageable);

    Optional<WeeklyReview> findFirstByUserIdOrderByWeekStartDateDesc(UUID userId);
}
