package com.personalfinance.review.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.habit.repository.HabitCheckinRepository;
import com.personalfinance.journal.repository.DailyJournalRepository;
import com.personalfinance.mood.repository.MoodEntryRepository;
import com.personalfinance.review.dto.WeeklyReviewRequest;
import com.personalfinance.review.dto.WeeklyReviewResponse;
import com.personalfinance.review.dto.WeeklySummaryResponse;
import com.personalfinance.review.entity.WeeklyReview;
import com.personalfinance.review.repository.WeeklyReviewRepository;
import com.personalfinance.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReviewService {

    private final WeeklyReviewRepository weeklyReviewRepository;
    private final DailyJournalRepository dailyJournalRepository;
    private final MoodEntryRepository moodEntryRepository;
    private final TaskRepository taskRepository;
    private final HabitCheckinRepository habitCheckinRepository;

    @Transactional
    public WeeklyReviewResponse createOrUpdateReview(WeeklyReviewRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate weekStart = request.getWeekStartDate();
        if (weekStart == null) {
            throw new BusinessException("Ngày bắt đầu tuần không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BusinessException("Ngày bắt đầu tuần phải là thứ Hai (Monday)", HttpStatus.BAD_REQUEST);
        }
        LocalDate weekEnd = weekStart.plusDays(6);

        WeeklyReview review = weeklyReviewRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                .orElseGet(() -> WeeklyReview.builder()
                        .userId(userId)
                        .weekStartDate(weekStart)
                        .weekEndDate(weekEnd)
                        .status("DRAFT")
                        .build());

        review.setSummary(request.getSummary());
        review.setWins(request.getWins());
        review.setChallenges(request.getChallenges());
        review.setLessonsLearned(request.getLessonsLearned());
        review.setImprovements(request.getImprovements());
        review.setNextWeekPriorities(request.getNextWeekPriorities());
        review.setAdditionalNotes(request.getAdditionalNotes());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            review.setStatus(request.getStatus().toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(request.getStatus()) && review.getCompletedAt() == null) {
                review.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
        }

        WeeklyReview saved = weeklyReviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional
    public WeeklyReviewResponse updateReviewById(UUID id, WeeklyReviewRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        WeeklyReview review = weeklyReviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản đánh giá tuần"));

        if (request.getWeekStartDate() != null && !request.getWeekStartDate().isEqual(review.getWeekStartDate())) {
            if (request.getWeekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
                throw new BusinessException("Ngày bắt đầu tuần phải là thứ Hai (Monday)", HttpStatus.BAD_REQUEST);
            }
            Optional<WeeklyReview> existing = weeklyReviewRepository.findByUserIdAndWeekStartDate(userId, request.getWeekStartDate());
            if (existing.isPresent()) {
                throw new BusinessException("Đã tồn tại review cho tuần này", HttpStatus.CONFLICT);
            }
            review.setWeekStartDate(request.getWeekStartDate());
            review.setWeekEndDate(request.getWeekStartDate().plusDays(6));
        }

        review.setSummary(request.getSummary());
        review.setWins(request.getWins());
        review.setChallenges(request.getChallenges());
        review.setLessonsLearned(request.getLessonsLearned());
        review.setImprovements(request.getImprovements());
        review.setNextWeekPriorities(request.getNextWeekPriorities());
        review.setAdditionalNotes(request.getAdditionalNotes());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            review.setStatus(request.getStatus().toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(request.getStatus()) && review.getCompletedAt() == null) {
                review.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
        }

        WeeklyReview saved = weeklyReviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional
    public WeeklyReviewResponse completeReview(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        WeeklyReview review = weeklyReviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản đánh giá tuần"));

        review.setStatus("COMPLETED");
        review.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        WeeklyReview saved = weeklyReviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public WeeklyReviewResponse getReviewById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        WeeklyReview review = weeklyReviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản đánh giá tuần"));
        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public WeeklyReviewResponse getReviewByWeek(LocalDate date) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (date == null) date = LocalDate.now();
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return weeklyReviewRepository.findByUserIdAndWeekStartDate(userId, monday)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReviewResponse> listReviews(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return weeklyReviewRepository.findByUserIdOrderByWeekStartDateDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void deleteReview(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        WeeklyReview review = weeklyReviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản đánh giá tuần"));
        weeklyReviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public WeeklySummaryResponse getWeeklySummary(LocalDate date) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (date == null) date = LocalDate.now();
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        long journalCount = dailyJournalRepository.countByUserIdAndJournalDateBetween(userId, monday, sunday);
        long moodCount = moodEntryRepository.countByUserIdAndRecordedDateBetween(userId, monday, sunday);

        Double avgMood = moodEntryRepository.getAverageMoodScore(userId, monday, sunday);
        Double avgEnergy = moodEntryRepository.getAverageEnergyScore(userId, monday, sunday);
        if (avgMood != null) avgMood = Math.round(avgMood * 100.0) / 100.0;
        if (avgEnergy != null) avgEnergy = Math.round(avgEnergy * 100.0) / 100.0;

        OffsetDateTime startOfWeek = DateTimeUtil.startOfDay(monday);
        OffsetDateTime endOfWeek = DateTimeUtil.endOfDay(sunday);

        long completedTasks = taskRepository.countByUserIdAndCompletedAtBetween(userId, startOfWeek, endOfWeek);
        long totalTasks = taskRepository.countByUserIdAndDueDateBetween(userId, startOfWeek, endOfWeek);
        if (totalTasks < completedTasks) {
            totalTasks = completedTasks;
        }

        long habitCount = habitCheckinRepository.findByUserIdAndCompletionDateBetweenOrderByCompletionDateAsc(userId, monday, sunday).size();

        return WeeklySummaryResponse.builder()
                .weekStartDate(monday)
                .weekEndDate(sunday)
                .journalDaysCount(journalCount)
                .moodDaysCount(moodCount)
                .averageMood(avgMood)
                .averageEnergy(avgEnergy)
                .completedTasksCount(completedTasks)
                .totalTasksCount(totalTasks)
                .habitCheckinsCount(habitCount)
                .build();
    }

    private WeeklyReviewResponse mapToResponse(WeeklyReview review) {
        return WeeklyReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .weekStartDate(review.getWeekStartDate())
                .weekEndDate(review.getWeekEndDate())
                .summary(review.getSummary())
                .wins(review.getWins())
                .challenges(review.getChallenges())
                .lessonsLearned(review.getLessonsLearned())
                .improvements(review.getImprovements())
                .nextWeekPriorities(review.getNextWeekPriorities())
                .additionalNotes(review.getAdditionalNotes())
                .status(review.getStatus())
                .completedAt(review.getCompletedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
