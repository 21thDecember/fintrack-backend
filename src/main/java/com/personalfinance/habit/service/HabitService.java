package com.personalfinance.habit.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.repository.GoalRepository;
import com.personalfinance.habit.dto.CheckinRequest;
import com.personalfinance.habit.dto.CheckinResponse;
import com.personalfinance.habit.dto.HabitRequest;
import com.personalfinance.habit.dto.HabitResponse;
import com.personalfinance.habit.dto.HabitStatisticsResponse;
import com.personalfinance.habit.dto.TodayHabitResponse;
import com.personalfinance.habit.entity.Habit;
import com.personalfinance.habit.entity.HabitCheckin;
import com.personalfinance.habit.entity.HabitFrequency;
import com.personalfinance.habit.repository.HabitCheckinRepository;
import com.personalfinance.habit.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCheckinRepository habitCheckinRepository;
    private final GoalRepository goalRepository;

    private static final List<String> ALL_DAYS = List.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    @Transactional
    public HabitResponse createHabit(HabitRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now(DateTimeUtil.ZONE_VN);
        if (request.getEndDate() != null && request.getEndDate().isBefore(startDate)) {
            throw new BusinessException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu", HttpStatus.BAD_REQUEST);
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }

        List<String> activeDays = request.getActiveDays();
        if (activeDays == null || activeDays.isEmpty()) {
            activeDays = ALL_DAYS;
        } else {
            activeDays = activeDays.stream().map(String::toUpperCase).toList();
        }

        Habit habit = Habit.builder()
                .userId(userId)
                .name(request.getName().trim())
                .description(request.getDescription())
                .frequency(request.getFrequency() != null ? request.getFrequency() : HabitFrequency.DAILY)
                .targetCount(request.getTargetCount() != null ? request.getTargetCount() : 1)
                .activeDays(activeDays)
                .startDate(startDate)
                .endDate(request.getEndDate())
                .goalId(request.getGoalId())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        habit = habitRepository.save(habit);
        return toHabitResponse(habit, Collections.emptyList(), null);
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> getHabits(UUID goalId, Boolean activeOnly) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<Habit> habits = (activeOnly != null && activeOnly) ?
                habitRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId) :
                habitRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (goalId != null) {
            habits = habits.stream().filter(h -> goalId.equals(h.getGoalId())).toList();
        }

        if (habits.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> goalIds = habits.stream().map(Habit::getGoalId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<UUID, String> goalTitleMap = goalIds.isEmpty() ? Collections.emptyMap() :
                goalRepository.findAllById(goalIds).stream().collect(Collectors.toMap(Goal::getId, Goal::getTitle));

        return habits.stream()
                .map(h -> {
                    List<HabitCheckin> checkins = habitCheckinRepository.findByHabitIdOrderByCompletionDateDesc(h.getId());
                    return toHabitResponse(h, checkins, goalTitleMap.get(h.getGoalId()));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse getHabitById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thói quen không tồn tại"));

        List<HabitCheckin> checkins = habitCheckinRepository.findByHabitIdOrderByCompletionDateDesc(id);
        String goalTitle = null;
        if (habit.getGoalId() != null) {
            goalTitle = goalRepository.findByIdAndUserId(habit.getGoalId(), userId).map(Goal::getTitle).orElse(null);
        }

        return toHabitResponse(habit, checkins, goalTitle);
    }

    @Transactional
    public HabitResponse updateHabit(UUID id, HabitRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thói quen không tồn tại"));

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : habit.getStartDate();
        if (request.getEndDate() != null && request.getEndDate().isBefore(startDate)) {
            throw new BusinessException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu", HttpStatus.BAD_REQUEST);
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }

        habit.setName(request.getName().trim());
        habit.setDescription(request.getDescription());
        if (request.getFrequency() != null) {
            habit.setFrequency(request.getFrequency());
        }
        if (request.getTargetCount() != null && request.getTargetCount() > 0) {
            habit.setTargetCount(request.getTargetCount());
        }
        if (request.getActiveDays() != null && !request.getActiveDays().isEmpty()) {
            habit.setActiveDays(request.getActiveDays().stream().map(String::toUpperCase).toList());
        }
        if (request.getStartDate() != null) {
            habit.setStartDate(request.getStartDate());
        }
        habit.setEndDate(request.getEndDate());
        habit.setGoalId(request.getGoalId());
        if (request.getIsActive() != null) {
            habit.setIsActive(request.getIsActive());
        }

        habit = habitRepository.save(habit);
        List<HabitCheckin> checkins = habitCheckinRepository.findByHabitIdOrderByCompletionDateDesc(id);
        String goalTitle = habit.getGoalId() != null ?
                goalRepository.findByIdAndUserId(habit.getGoalId(), userId).map(Goal::getTitle).orElse(null) : null;

        return toHabitResponse(habit, checkins, goalTitle);
    }

    @Transactional
    public void deleteHabit(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thói quen không tồn tại"));

        habitCheckinRepository.deleteByHabitId(id);
        habitRepository.delete(habit);
    }

    @Transactional
    public HabitResponse checkin(UUID habitId, CheckinRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thói quen không tồn tại"));

        LocalDate date = (request != null && request.getDate() != null) ? request.getDate() : LocalDate.now(DateTimeUtil.ZONE_VN);

        Optional<HabitCheckin> existingOpt = habitCheckinRepository.findByHabitIdAndCompletionDate(habitId, date);
        HabitCheckin checkin;
        if (existingOpt.isPresent()) {
            checkin = existingOpt.get();
            int currentCount = checkin.getCount() != null ? checkin.getCount() : 0;
            int newCount = (request != null && request.getCount() != null) ? request.getCount() : currentCount + 1;
            checkin.setCount(newCount);
            if (request != null && request.getNote() != null) {
                checkin.setNote(request.getNote());
            }
        } else {
            int initialCount = (request != null && request.getCount() != null) ? request.getCount() : 1;
            checkin = HabitCheckin.builder()
                    .habitId(habitId)
                    .userId(userId)
                    .completionDate(date)
                    .count(initialCount)
                    .note(request != null ? request.getNote() : null)
                    .build();
        }

        habitCheckinRepository.save(checkin);

        List<HabitCheckin> checkins = habitCheckinRepository.findByHabitIdOrderByCompletionDateDesc(habitId);
        String goalTitle = habit.getGoalId() != null ?
                goalRepository.findByIdAndUserId(habit.getGoalId(), userId).map(Goal::getTitle).orElse(null) : null;

        return toHabitResponse(habit, checkins, goalTitle);
    }

    @Transactional
    public void removeCheckin(UUID habitId, LocalDate date) {
        UUID userId = SecurityUtils.getCurrentUserId();
        habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thói quen không tồn tại"));

        LocalDate targetDate = date != null ? date : LocalDate.now(DateTimeUtil.ZONE_VN);
        habitCheckinRepository.deleteByHabitIdAndCompletionDate(habitId, targetDate);
    }

    @Transactional(readOnly = true)
    public List<TodayHabitResponse> getTodayHabits() {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now(DateTimeUtil.ZONE_VN);
        String todayDayOfWeek = today.getDayOfWeek().name();

        List<Habit> activeHabits = habitRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        if (activeHabits.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter habits active today
        List<Habit> todayHabits = activeHabits.stream()
                .filter(h -> {
                    if (h.getStartDate().isAfter(today)) return false;
                    if (h.getEndDate() != null && h.getEndDate().isBefore(today)) return false;
                    List<String> days = h.getActiveDays() != null ? h.getActiveDays() : ALL_DAYS;
                    return h.getFrequency() == HabitFrequency.WEEKLY || days.contains(todayDayOfWeek);
                })
                .toList();

        if (todayHabits.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> habitIds = todayHabits.stream().map(Habit::getId).toList();
        Map<UUID, HabitCheckin> todayCheckinMap = habitCheckinRepository.findByHabitIdInAndCompletionDate(habitIds, today)
                .stream().collect(Collectors.toMap(HabitCheckin::getHabitId, c -> c));

        List<UUID> goalIds = todayHabits.stream().map(Habit::getGoalId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<UUID, String> goalTitleMap = goalIds.isEmpty() ? Collections.emptyMap() :
                goalRepository.findAllById(goalIds).stream().collect(Collectors.toMap(Goal::getId, Goal::getTitle));

        return todayHabits.stream()
                .map(h -> {
                    HabitCheckin c = todayCheckinMap.get(h.getId());
                    int todayCount = c != null ? c.getCount() : 0;
                    boolean isCompletedToday = todayCount >= h.getTargetCount();
                    List<HabitCheckin> checkins = habitCheckinRepository.findByHabitIdOrderByCompletionDateDesc(h.getId());
                    int streak = calculateCurrentStreak(h, checkins, today);

                    return TodayHabitResponse.builder()
                            .id(h.getId())
                            .name(h.getName())
                            .description(h.getDescription())
                            .frequency(h.getFrequency())
                            .targetCount(h.getTargetCount())
                            .todayCount(todayCount)
                            .isCompletedToday(isCompletedToday)
                            .currentStreak(streak)
                            .goalId(h.getGoalId())
                            .goalTitle(goalTitleMap.get(h.getGoalId()))
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitStatisticsResponse getStatistics() {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now(DateTimeUtil.ZONE_VN);

        long total = habitRepository.countByUserId(userId);
        long active = habitRepository.countByUserIdAndIsActiveTrue(userId);

        List<TodayHabitResponse> todayList = getTodayHabits();
        long todayScheduled = todayList.size();
        long todayCompleted = todayList.stream().filter(TodayHabitResponse::isCompletedToday).count();

        List<Habit> activeHabits = habitRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        double avgRate = 0.0;
        if (!activeHabits.isEmpty()) {
            double sumRates = 0.0;
            for (Habit h : activeHabits) {
                List<HabitCheckin> checkins = habitCheckinRepository.findByHabitIdOrderByCompletionDateDesc(h.getId());
                sumRates += calculateCompletionRate(h, checkins, today);
            }
            avgRate = Math.round((sumRates / activeHabits.size()) * 10.0) / 10.0;
        }

        return HabitStatisticsResponse.builder()
                .totalHabits(total)
                .activeHabits(active)
                .todayScheduledCount(todayScheduled)
                .completedTodayCount(todayCompleted)
                .overallCompletionRate(avgRate)
                .build();
    }

    private HabitResponse toHabitResponse(Habit habit, List<HabitCheckin> checkins, String goalTitle) {
        LocalDate today = LocalDate.now(DateTimeUtil.ZONE_VN);

        int currentStreak = calculateCurrentStreak(habit, checkins, today);
        int longestStreak = calculateLongestStreak(habit, checkins);
        double completionRate = calculateCompletionRate(habit, checkins, today);

        HabitCheckin todayCheckin = checkins.stream()
                .filter(c -> c.getCompletionDate().isEqual(today))
                .findFirst()
                .orElse(null);

        int todayCount = todayCheckin != null ? todayCheckin.getCount() : 0;
        boolean isCompletedToday = todayCount >= habit.getTargetCount();

        List<CheckinResponse> recent = checkins.stream()
                .limit(30)
                .map(c -> CheckinResponse.builder()
                        .id(c.getId())
                        .habitId(c.getHabitId())
                        .completionDate(c.getCompletionDate())
                        .count(c.getCount())
                        .note(c.getNote())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        return HabitResponse.builder()
                .id(habit.getId())
                .userId(habit.getUserId())
                .goalId(habit.getGoalId())
                .goalTitle(goalTitle)
                .name(habit.getName())
                .description(habit.getDescription())
                .frequency(habit.getFrequency())
                .targetCount(habit.getTargetCount())
                .activeDays(habit.getActiveDays() != null ? habit.getActiveDays() : ALL_DAYS)
                .startDate(habit.getStartDate())
                .endDate(habit.getEndDate())
                .isActive(habit.getIsActive())
                .createdAt(habit.getCreatedAt())
                .updatedAt(habit.getUpdatedAt())
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .totalCompletedDays(checkins.size())
                .completionRate(completionRate)
                .isCompletedToday(isCompletedToday)
                .todayCount(todayCount)
                .recentCheckins(recent)
                .build();
    }

    private int calculateCurrentStreak(Habit habit, List<HabitCheckin> checkins, LocalDate today) {
        if (checkins.isEmpty()) return 0;

        Set<LocalDate> completedDates = checkins.stream()
                .filter(c -> c.getCount() != null && c.getCount() >= habit.getTargetCount())
                .map(HabitCheckin::getCompletionDate)
                .collect(Collectors.toSet());

        List<String> activeDays = habit.getActiveDays() != null ? habit.getActiveDays() : ALL_DAYS;

        LocalDate cursor = today;
        int streak = 0;

        // If today is scheduled and completed, count today and walk backward
        // If today is scheduled and NOT completed, don't break streak yet, check yesterday
        if (activeDays.contains(cursor.getDayOfWeek().name())) {
            if (completedDates.contains(cursor)) {
                streak++;
            }
            cursor = cursor.minusDays(1);
        } else {
            cursor = cursor.minusDays(1);
        }

        LocalDate startBound = habit.getStartDate();
        while (!cursor.isBefore(startBound)) {
            String dayName = cursor.getDayOfWeek().name();
            if (activeDays.contains(dayName)) {
                if (completedDates.contains(cursor)) {
                    streak++;
                } else {
                    break; // Scheduled day was missed
                }
            }
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    private int calculateLongestStreak(Habit habit, List<HabitCheckin> checkins) {
        if (checkins.isEmpty()) return 0;

        Set<LocalDate> completedDates = checkins.stream()
                .filter(c -> c.getCount() != null && c.getCount() >= habit.getTargetCount())
                .map(HabitCheckin::getCompletionDate)
                .collect(Collectors.toSet());

        List<String> activeDays = habit.getActiveDays() != null ? habit.getActiveDays() : ALL_DAYS;

        LocalDate cursor = habit.getStartDate();
        LocalDate today = LocalDate.now(DateTimeUtil.ZONE_VN);
        LocalDate endBound = habit.getEndDate() != null && habit.getEndDate().isBefore(today) ? habit.getEndDate() : today;

        int longest = 0;
        int current = 0;

        while (!cursor.isAfter(endBound)) {
            String dayName = cursor.getDayOfWeek().name();
            if (activeDays.contains(dayName)) {
                if (completedDates.contains(cursor)) {
                    current++;
                    if (current > longest) longest = current;
                } else {
                    current = 0;
                }
            }
            cursor = cursor.plusDays(1);
        }

        return longest;
    }

    private double calculateCompletionRate(Habit habit, List<HabitCheckin> checkins, LocalDate today) {
        LocalDate start = habit.getStartDate();
        LocalDate end = habit.getEndDate() != null && habit.getEndDate().isBefore(today) ? habit.getEndDate() : today;

        if (start.isAfter(end)) return 0.0;

        List<String> activeDays = habit.getActiveDays() != null ? habit.getActiveDays() : ALL_DAYS;
        Set<LocalDate> completedDates = checkins.stream()
                .filter(c -> c.getCount() != null && c.getCount() >= habit.getTargetCount())
                .map(HabitCheckin::getCompletionDate)
                .collect(Collectors.toSet());

        int scheduledDays = 0;
        int completedDays = 0;

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (activeDays.contains(cursor.getDayOfWeek().name())) {
                scheduledDays++;
                if (completedDates.contains(cursor)) {
                    completedDays++;
                }
            }
            cursor = cursor.plusDays(1);
        }

        if (scheduledDays == 0) return 0.0;
        double rate = ((double) completedDays / scheduledDays) * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }
}
