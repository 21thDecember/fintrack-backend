package com.personalfinance.goal.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.goal.dto.GoalProgressResponse;
import com.personalfinance.goal.dto.GoalRequest;
import com.personalfinance.goal.dto.GoalResponse;
import com.personalfinance.goal.dto.GoalStatisticsResponse;
import com.personalfinance.goal.dto.MilestoneRequest;
import com.personalfinance.goal.dto.MilestoneResponse;
import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.entity.GoalCategory;
import com.personalfinance.goal.entity.GoalMilestone;
import com.personalfinance.goal.entity.GoalPriority;
import com.personalfinance.goal.entity.GoalStatus;
import com.personalfinance.goal.entity.MilestoneStatus;
import com.personalfinance.goal.repository.GoalMilestoneRepository;
import com.personalfinance.goal.repository.GoalRepository;
import com.personalfinance.habit.dto.HabitResponse;
import com.personalfinance.habit.entity.Habit;
import com.personalfinance.habit.repository.HabitRepository;
import com.personalfinance.task.dto.TaskResponse;
import com.personalfinance.task.entity.Task;
import com.personalfinance.task.entity.TaskStatus;
import com.personalfinance.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalMilestoneRepository goalMilestoneRepository;
    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;

    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateDates(request.getStartDate(), request.getTargetDate());

        GoalStatus status = request.getStatus() != null ? request.getStatus() : GoalStatus.NOT_STARTED;
        OffsetDateTime completedAt = status == GoalStatus.COMPLETED ? OffsetDateTime.now(ZoneOffset.UTC) : null;

        Goal goal = Goal.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : GoalCategory.PERSONAL)
                .priority(request.getPriority() != null ? request.getPriority() : GoalPriority.MEDIUM)
                .status(status)
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .completedAt(completedAt)
                .build();

        goal = goalRepository.save(goal);
        return toGoalResponse(goal, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(GoalCategory category, GoalStatus status, GoalPriority priority) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (category != null) {
            goals = goals.stream().filter(g -> category.equals(g.getCategory())).toList();
        }
        if (status != null) {
            goals = goals.stream().filter(g -> status.equals(g.getStatus())).toList();
        }
        if (priority != null) {
            goals = goals.stream().filter(g -> priority.equals(g.getPriority())).toList();
        }

        if (goals.isEmpty()) {
            return Collections.emptyList();
        }

        return goals.stream().map(g -> {
            List<GoalMilestone> milestones = goalMilestoneRepository.findByGoalIdAndUserIdOrderByDisplayOrderAscCreatedAtAsc(g.getId(), userId);
            return toGoalResponse(g, milestones, Collections.emptyList(), Collections.emptyList());
        }).toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoalById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu không tồn tại"));

        List<GoalMilestone> milestones = goalMilestoneRepository.findByGoalIdAndUserIdOrderByDisplayOrderAscCreatedAtAsc(id, userId);
        List<Task> tasks = taskRepository.findByGoalIdAndUserId(id, userId);
        List<Habit> habits = habitRepository.findByGoalIdAndUserId(id, userId);

        List<TaskResponse> taskResponses = tasks.stream().map(t -> TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus())
                .priority(t.getPriority())
                .dueDate(t.getDueDate())
                .build()).toList();

        List<HabitResponse> habitResponses = habits.stream().map(h -> HabitResponse.builder()
                .id(h.getId())
                .name(h.getName())
                .frequency(h.getFrequency())
                .targetCount(h.getTargetCount())
                .build()).toList();

        return toGoalResponse(goal, milestones, taskResponses, habitResponses);
    }

    @Transactional
    public GoalResponse updateGoal(UUID id, GoalRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu không tồn tại"));

        validateDates(request.getStartDate(), request.getTargetDate());

        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            goal.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            goal.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            if (goal.getStatus() != GoalStatus.COMPLETED && request.getStatus() == GoalStatus.COMPLETED) {
                goal.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            } else if (goal.getStatus() == GoalStatus.COMPLETED && request.getStatus() != GoalStatus.COMPLETED) {
                goal.setCompletedAt(null);
            }
            goal.setStatus(request.getStatus());
        }
        goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());

        goal = goalRepository.save(goal);
        List<GoalMilestone> milestones = goalMilestoneRepository.findByGoalIdAndUserIdOrderByDisplayOrderAscCreatedAtAsc(id, userId);
        return toGoalResponse(goal, milestones, Collections.emptyList(), Collections.emptyList());
    }

    @Transactional
    public void deleteGoal(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu không tồn tại"));

        // Unlink tasks and habits (though DB FK ON DELETE SET NULL also handles it)
        List<Task> tasks = taskRepository.findByGoalIdAndUserId(id, userId);
        tasks.forEach(t -> t.setGoalId(null));
        taskRepository.saveAll(tasks);

        List<Habit> habits = habitRepository.findByGoalIdAndUserId(id, userId);
        habits.forEach(h -> h.setGoalId(null));
        habitRepository.saveAll(habits);

        goalMilestoneRepository.deleteByGoalIdAndUserId(id, userId);
        goalRepository.delete(goal);
    }

    @Transactional
    public MilestoneResponse createMilestone(UUID goalId, MilestoneRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu không tồn tại"));

        MilestoneStatus status = request.getStatus() != null ? request.getStatus() : MilestoneStatus.TODO;
        OffsetDateTime completedAt = status == MilestoneStatus.COMPLETED ? OffsetDateTime.now(ZoneOffset.UTC) : null;

        int order = request.getDisplayOrder() != null ? request.getDisplayOrder() :
                (int) goalMilestoneRepository.countByGoalIdAndUserId(goalId, userId);

        GoalMilestone milestone = GoalMilestone.builder()
                .goalId(goalId)
                .userId(userId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(status)
                .displayOrder(order)
                .completedAt(completedAt)
                .build();

        milestone = goalMilestoneRepository.save(milestone);
        return toMilestoneResponse(milestone);
    }

    @Transactional
    public MilestoneResponse updateMilestone(UUID goalId, UUID milestoneId, MilestoneRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        GoalMilestone milestone = goalMilestoneRepository.findByIdAndGoalIdAndUserId(milestoneId, goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cột mốc không tồn tại"));

        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());
        if (request.getDisplayOrder() != null) {
            milestone.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getStatus() != null) {
            if (milestone.getStatus() != MilestoneStatus.COMPLETED && request.getStatus() == MilestoneStatus.COMPLETED) {
                milestone.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            } else if (milestone.getStatus() == MilestoneStatus.COMPLETED && request.getStatus() != MilestoneStatus.COMPLETED) {
                milestone.setCompletedAt(null);
            }
            milestone.setStatus(request.getStatus());
        }

        milestone = goalMilestoneRepository.save(milestone);
        return toMilestoneResponse(milestone);
    }

    @Transactional
    public void deleteMilestone(UUID goalId, UUID milestoneId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        GoalMilestone milestone = goalMilestoneRepository.findByIdAndGoalIdAndUserId(milestoneId, goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cột mốc không tồn tại"));

        goalMilestoneRepository.delete(milestone);
    }

    @Transactional(readOnly = true)
    public GoalProgressResponse getProgress(UUID goalId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu không tồn tại"));

        long total = goalMilestoneRepository.countByGoalIdAndUserId(goalId, userId);
        long completed = goalMilestoneRepository.countByGoalIdAndUserIdAndStatus(goalId, userId, MilestoneStatus.COMPLETED);

        double pct = total > 0 ? ((double) completed / total) * 100.0 : 0.0;
        pct = Math.round(pct * 10.0) / 10.0;

        List<Task> tasks = taskRepository.findByGoalIdAndUserId(goalId, userId);
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long linkedHabits = habitRepository.findByGoalIdAndUserId(goalId, userId).size();

        return GoalProgressResponse.builder()
                .goalId(goalId)
                .totalMilestones((int) total)
                .completedMilestones((int) completed)
                .progressPercentage(pct)
                .linkedTasksCount(tasks.size())
                .completedTasksCount((int) completedTasks)
                .linkedHabitsCount((int) linkedHabits)
                .build();
    }

    @Transactional(readOnly = true)
    public GoalStatisticsResponse getStatistics() {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now(DateTimeUtil.ZONE_VN);

        long total = goalRepository.countByUserId(userId);
        long notStarted = goalRepository.countByUserIdAndStatus(userId, GoalStatus.NOT_STARTED);
        long inProgress = goalRepository.countByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS);
        long completed = goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED);
        long cancelled = goalRepository.countByUserIdAndStatus(userId, GoalStatus.CANCELLED);

        long overdue = goalRepository.countOverdueGoals(userId, List.of(GoalStatus.COMPLETED, GoalStatus.CANCELLED), today);

        List<Goal> allGoals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        double avgProgress = 0.0;
        if (!allGoals.isEmpty()) {
            double sumPct = 0.0;
            for (Goal g : allGoals) {
                long totalM = goalMilestoneRepository.countByGoalIdAndUserId(g.getId(), userId);
                long compM = goalMilestoneRepository.countByGoalIdAndUserIdAndStatus(g.getId(), userId, MilestoneStatus.COMPLETED);
                if (totalM > 0) {
                    sumPct += ((double) compM / totalM) * 100.0;
                }
            }
            avgProgress = Math.round((sumPct / allGoals.size()) * 10.0) / 10.0;
        }

        return GoalStatisticsResponse.builder()
                .totalGoals(total)
                .notStartedGoals(notStarted)
                .inProgressGoals(inProgress)
                .completedGoals(completed)
                .cancelledGoals(cancelled)
                .overdueGoals(overdue)
                .averageProgressPercentage(avgProgress)
                .build();
    }

    private void validateDates(LocalDate start, LocalDate target) {
        if (start != null && target != null && target.isBefore(start)) {
            throw new BusinessException("Ngày mục tiêu hoàn thành không được trước ngày bắt đầu", HttpStatus.BAD_REQUEST);
        }
    }

    private GoalResponse toGoalResponse(Goal goal, List<GoalMilestone> milestones, List<TaskResponse> tasks, List<HabitResponse> habits) {
        LocalDate today = LocalDate.now(DateTimeUtil.ZONE_VN);
        boolean isOverdue = goal.getTargetDate() != null
                && goal.getTargetDate().isBefore(today)
                && goal.getStatus() != GoalStatus.COMPLETED
                && goal.getStatus() != GoalStatus.CANCELLED;

        int totalM = milestones.size();
        int compM = (int) milestones.stream().filter(m -> m.getStatus() == MilestoneStatus.COMPLETED).count();
        double pct = totalM > 0 ? ((double) compM / totalM) * 100.0 : 0.0;
        pct = Math.round(pct * 10.0) / 10.0;

        List<MilestoneResponse> milestoneResponses = milestones.stream().map(this::toMilestoneResponse).toList();

        return GoalResponse.builder()
                .id(goal.getId())
                .userId(goal.getUserId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .priority(goal.getPriority())
                .status(goal.getStatus())
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .completedAt(goal.getCompletedAt())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .isOverdue(isOverdue)
                .totalMilestones(totalM)
                .completedMilestones(compM)
                .progressPercentage(pct)
                .milestones(milestoneResponses)
                .linkedTasks(tasks)
                .linkedHabits(habits)
                .build();
    }

    private MilestoneResponse toMilestoneResponse(GoalMilestone m) {
        return MilestoneResponse.builder()
                .id(m.getId())
                .goalId(m.getGoalId())
                .title(m.getTitle())
                .description(m.getDescription())
                .dueDate(m.getDueDate())
                .status(m.getStatus())
                .displayOrder(m.getDisplayOrder())
                .completedAt(m.getCompletedAt())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
