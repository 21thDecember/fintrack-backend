package com.personalfinance.learning.service;

import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.learning.dto.BookResponse;
import com.personalfinance.learning.dto.CourseResponse;
import com.personalfinance.learning.dto.DailyLearningTimeItem;
import com.personalfinance.learning.dto.LearningDashboardResponse;
import com.personalfinance.learning.dto.LearningSessionResponse;
import com.personalfinance.learning.dto.LearningStatisticsResponse;
import com.personalfinance.learning.dto.SkillResponse;
import com.personalfinance.learning.entity.BookStatus;
import com.personalfinance.learning.entity.CourseStatus;
import com.personalfinance.learning.repository.BookRepository;
import com.personalfinance.learning.repository.CourseRepository;
import com.personalfinance.learning.repository.LearningSessionRepository;
import com.personalfinance.learning.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningDashboardService {

    private final LearningSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final BookRepository bookRepository;
    private final SkillRepository skillRepository;
    private final CourseService courseService;
    private final BookService bookService;
    private final SkillService skillService;
    private final LearningSessionService sessionService;

    @Transactional(readOnly = true)
    public LearningDashboardResponse getDashboard() {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        int todayMinutes = sessionRepository.sumDurationByUserIdAndLearningDate(userId, today);
        int weekMinutes = sessionRepository.sumDurationByUserIdAndLearningDateBetween(userId, weekStart, weekEnd);
        int monthMinutes = sessionRepository.sumDurationByUserIdAndLearningDateBetween(userId, monthStart, monthEnd);

        long activeCoursesCount = courseRepository.countByUserIdAndStatus(userId, CourseStatus.IN_PROGRESS);
        long readingBooksCount = bookRepository.countByUserIdAndStatus(userId, BookStatus.READING);
        long activeSkillsCount = skillRepository.countByUserIdAndIsActive(userId, true);

        List<CourseResponse> inProgressCourses = courseRepository.findByUserIdAndStatus(userId, CourseStatus.IN_PROGRESS).stream()
                .limit(6)
                .map(courseService::toResponse)
                .collect(Collectors.toList());

        List<BookResponse> readingBooks = bookRepository.findByUserIdAndStatus(userId, BookStatus.READING).stream()
                .limit(6)
                .map(bookService::toResponse)
                .collect(Collectors.toList());

        List<SkillResponse> activeSkills = skillRepository.findByUserIdAndIsActive(userId, true).stream()
                .limit(6)
                .map(s -> skillService.toResponse(s, false))
                .collect(Collectors.toList());

        List<LearningSessionResponse> recentSessions = sessionRepository.findTop10ByUserIdOrderByLearningDateDescCreatedAtDesc(userId).stream()
                .map(sessionService::toResponse)
                .collect(Collectors.toList());

        // Weekly Activity: last 7 days (today - 6 days to today)
        LocalDate sevenDaysAgo = today.minusDays(6);
        List<Object[]> dateRows = sessionRepository.sumDurationGroupByDateBetween(userId, sevenDaysAgo, today);
        Map<LocalDate, Integer> durationMap = new HashMap<>();
        for (Object[] row : dateRows) {
            durationMap.put((LocalDate) row[0], ((Number) row[1]).intValue());
        }

        List<DailyLearningTimeItem> weeklyActivity = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            weeklyActivity.add(DailyLearningTimeItem.builder()
                    .date(d)
                    .durationMinutes(durationMap.getOrDefault(d, 0))
                    .build());
        }

        return LearningDashboardResponse.builder()
                .learningTimeTodayMinutes(todayMinutes)
                .learningTimeThisWeekMinutes(weekMinutes)
                .learningTimeThisMonthMinutes(monthMinutes)
                .activeCoursesCount(activeCoursesCount)
                .readingBooksCount(readingBooksCount)
                .activeSkillsCount(activeSkillsCount)
                .inProgressCourses(inProgressCourses)
                .readingBooks(readingBooks)
                .activeSkills(activeSkills)
                .recentSessions(recentSessions)
                .weeklyActivity(weeklyActivity)
                .build();
    }

    @Transactional(readOnly = true)
    public LearningStatisticsResponse getStatistics() {
        return sessionService.getStatistics();
    }
}
