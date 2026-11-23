package com.personalfinance.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningDashboardResponse {

    private int learningTimeTodayMinutes;
    private int learningTimeThisWeekMinutes;
    private int learningTimeThisMonthMinutes;

    private long activeCoursesCount;
    private long readingBooksCount;
    private long activeSkillsCount;

    private List<CourseResponse> inProgressCourses;
    private List<BookResponse> readingBooks;
    private List<SkillResponse> activeSkills;

    private List<LearningSessionResponse> recentSessions;
    private List<DailyLearningTimeItem> weeklyActivity;
}
