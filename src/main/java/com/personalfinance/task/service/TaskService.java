package com.personalfinance.task.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.response.PageResponse;
import com.personalfinance.common.util.DateTimeUtil;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.repository.GoalRepository;
import com.personalfinance.task.dto.TaskRequest;
import com.personalfinance.task.dto.TaskResponse;
import com.personalfinance.task.dto.TaskStatisticsResponse;
import com.personalfinance.task.dto.TaskStatusUpdateRequest;
import com.personalfinance.task.entity.Task;
import com.personalfinance.task.entity.TaskPriority;
import com.personalfinance.task.entity.TaskStatus;
import com.personalfinance.task.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateDates(request.getStartDate(), request.getDueDate());

        if (request.getParentTaskId() != null) {
            Task parent = taskRepository.findByIdAndUserId(request.getParentTaskId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Công việc cha không tồn tại"));
            if (parent.getParentTaskId() != null) {
                throw new BusinessException("Hệ thống chỉ hỗ trợ tối đa 1 cấp công việc phụ (subtask)", HttpStatus.BAD_REQUEST);
            }
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }

        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.TODO;
        OffsetDateTime completedAt = status == TaskStatus.COMPLETED ? OffsetDateTime.now(ZoneOffset.UTC) : null;

        Task task = Task.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .status(status)
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .estimatedMinutes(request.getEstimatedMinutes())
                .goalId(request.getGoalId())
                .parentTaskId(request.getParentTaskId())
                .completedAt(completedAt)
                .build();

        task = taskRepository.save(task);
        return toResponse(task, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasks(
            int page,
            int size,
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            OffsetDateTime dueDateFrom,
            OffsetDateTime dueDateTo,
            UUID goalId,
            String view,
            Boolean parentOnly
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("priority"),
                Sort.Order.asc("dueDate").nullsLast(),
                Sort.Order.desc("createdAt")
        ));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        LocalDate todayVn = LocalDate.now(DateTimeUtil.ZONE_VN);
        OffsetDateTime startOfToday = DateTimeUtil.startOfDay(todayVn);
        OffsetDateTime endOfToday = DateTimeUtil.endOfDay(todayVn);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            // Default to top-level tasks if parentOnly is true or not specified
            if (parentOnly == null || parentOnly) {
                predicates.add(cb.isNull(root.get("parentTaskId")));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (goalId != null) {
                predicates.add(cb.equal(root.get("goalId"), goalId));
            }

            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }

            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            // View presets
            if (view != null && !view.trim().isEmpty()) {
                switch (view.trim().toLowerCase()) {
                    case "today" -> predicates.add(cb.or(
                            // Due today or starting today
                            cb.between(root.get("dueDate"), startOfToday, endOfToday),
                            cb.between(root.get("startDate"), startOfToday, endOfToday),
                            // Or overdue
                            cb.and(
                                    cb.isNotNull(root.get("dueDate")),
                                    cb.lessThan(root.get("dueDate"), now),
                                    cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
                                    cb.notEqual(root.get("status"), TaskStatus.CANCELLED)
                            )
                    ));
                    case "upcoming" -> predicates.add(cb.and(
                            cb.isNotNull(root.get("dueDate")),
                            cb.greaterThan(root.get("dueDate"), endOfToday),
                            cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
                            cb.notEqual(root.get("status"), TaskStatus.CANCELLED)
                    ));
                    case "overdue" -> predicates.add(cb.and(
                            cb.isNotNull(root.get("dueDate")),
                            cb.lessThan(root.get("dueDate"), now),
                            cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
                            cb.notEqual(root.get("status"), TaskStatus.CANCELLED)
                    ));
                    case "completed" -> predicates.add(cb.equal(root.get("status"), TaskStatus.COMPLETED));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        List<Task> content = taskPage.getContent();

        if (content.isEmpty()) {
            return PageResponse.of(taskPage, Collections.emptyList());
        }

        // Batch fetch subtasks for current page parent tasks
        List<UUID> parentIds = content.stream().map(Task::getId).toList();
        List<Task> subtasks = taskRepository.findByParentTaskIdInAndUserId(parentIds, userId);
        Map<UUID, List<Task>> subtasksByParent = subtasks.stream()
                .collect(Collectors.groupingBy(Task::getParentTaskId));

        // Batch fetch goal titles
        List<UUID> goalIds = content.stream()
                .map(Task::getGoalId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> goalTitleMap = goalIds.isEmpty() ? Collections.emptyMap() :
                goalRepository.findAllById(goalIds).stream()
                        .collect(Collectors.toMap(Goal::getId, Goal::getTitle));

        List<TaskResponse> responses = content.stream()
                .map(t -> mapToResponseWithSubtasks(t, subtasksByParent.getOrDefault(t.getId(), Collections.emptyList()), goalTitleMap.get(t.getGoalId())))
                .toList();

        return PageResponse.of(taskPage, responses);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc không tồn tại"));

        List<Task> subtasks = taskRepository.findByParentTaskIdAndUserIdOrderByCreatedAtAsc(id, userId);
        String goalTitle = null;
        if (task.getGoalId() != null) {
            goalTitle = goalRepository.findByIdAndUserId(task.getGoalId(), userId)
                    .map(Goal::getTitle)
                    .orElse(null);
        }

        return mapToResponseWithSubtasks(task, subtasks, goalTitle);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc không tồn tại"));

        validateDates(request.getStartDate(), request.getDueDate());

        if (request.getParentTaskId() != null) {
            if (request.getParentTaskId().equals(id)) {
                throw new BusinessException("Công việc không thể làm công việc con của chính nó", HttpStatus.BAD_REQUEST);
            }
            Task parent = taskRepository.findByIdAndUserId(request.getParentTaskId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Công việc cha không tồn tại"));
            if (parent.getParentTaskId() != null) {
                throw new BusinessException("Hệ thống chỉ hỗ trợ tối đa 1 cấp công việc phụ (subtask)", HttpStatus.BAD_REQUEST);
            }
        }

        if (request.getGoalId() != null) {
            goalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            handleStatusTransition(task, request.getStatus());
        }
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setEstimatedMinutes(request.getEstimatedMinutes());
        task.setGoalId(request.getGoalId());
        task.setParentTaskId(request.getParentTaskId());

        task = taskRepository.save(task);
        return toResponse(task, true);
    }

    @Transactional
    public TaskResponse updateStatus(UUID id, TaskStatusUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc không tồn tại"));

        if (task.getStatus() == TaskStatus.CANCELLED && request.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessException("Không thể hoàn thành công việc đã bị hủy, vui lòng chuyển về Chưa thực hiện trước", HttpStatus.BAD_REQUEST);
        }

        handleStatusTransition(task, request.getStatus());
        task = taskRepository.save(task);
        return toResponse(task, true);
    }

    @Transactional
    public void deleteTask(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc không tồn tại"));

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public TaskStatisticsResponse getStatistics() {
        UUID userId = SecurityUtils.getCurrentUserId();
        long total = taskRepository.countByUserId(userId);
        long todo = taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO);
        long inProgress = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        long completed = taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        long cancelled = taskRepository.countByUserIdAndStatus(userId, TaskStatus.CANCELLED);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long overdue = taskRepository.countOverdueTasks(
                userId,
                List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED),
                now
        );

        long activeTotal = total - cancelled;
        double completionRate = activeTotal > 0 ? ((double) completed / activeTotal) * 100.0 : 0.0;
        completionRate = Math.round(completionRate * 10.0) / 10.0;

        return TaskStatisticsResponse.builder()
                .totalTasks(total)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .completedTasks(completed)
                .cancelledTasks(cancelled)
                .overdueTasks(overdue)
                .completionRate(completionRate)
                .build();
    }

    private void handleStatusTransition(Task task, TaskStatus newStatus) {
        if (task.getStatus() != TaskStatus.COMPLETED && newStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        } else if (task.getStatus() == TaskStatus.COMPLETED && newStatus != TaskStatus.COMPLETED) {
            task.setCompletedAt(null);
        }
        task.setStatus(newStatus);
    }

    private void validateDates(OffsetDateTime start, OffsetDateTime due) {
        if (start != null && due != null && due.isBefore(start)) {
            throw new BusinessException("Deadline không được trước ngày bắt đầu", HttpStatus.BAD_REQUEST);
        }
    }

    private TaskResponse toResponse(Task task, boolean loadGoalTitle) {
        String goalTitle = null;
        if (loadGoalTitle && task.getGoalId() != null) {
            goalTitle = goalRepository.findByIdAndUserId(task.getGoalId(), task.getUserId())
                    .map(Goal::getTitle)
                    .orElse(null);
        }
        return mapToResponseWithSubtasks(task, Collections.emptyList(), goalTitle);
    }

    private TaskResponse mapToResponseWithSubtasks(Task task, List<Task> subtasks, String goalTitle) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        boolean isOverdue = task.getDueDate() != null
                && task.getDueDate().isBefore(now)
                && task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.CANCELLED;

        List<TaskResponse> subtaskResponses = subtasks.stream()
                .map(s -> TaskResponse.builder()
                        .id(s.getId())
                        .userId(s.getUserId())
                        .parentTaskId(s.getParentTaskId())
                        .title(s.getTitle())
                        .status(s.getStatus())
                        .priority(s.getPriority())
                        .startDate(s.getStartDate())
                        .dueDate(s.getDueDate())
                        .estimatedMinutes(s.getEstimatedMinutes())
                        .completedAt(s.getCompletedAt())
                        .createdAt(s.getCreatedAt())
                        .updatedAt(s.getUpdatedAt())
                        .isOverdue(s.getDueDate() != null && s.getDueDate().isBefore(now) && s.getStatus() != TaskStatus.COMPLETED && s.getStatus() != TaskStatus.CANCELLED)
                        .build())
                .toList();

        int subtaskCount = subtaskResponses.size();
        int completedSubtaskCount = (int) subtaskResponses.stream()
                .filter(s -> s.getStatus() == TaskStatus.COMPLETED)
                .count();

        return TaskResponse.builder()
                .id(task.getId())
                .userId(task.getUserId())
                .parentTaskId(task.getParentTaskId())
                .goalId(task.getGoalId())
                .goalTitle(goalTitle)
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .estimatedMinutes(task.getEstimatedMinutes())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isOverdue(isOverdue)
                .subtasks(subtaskResponses)
                .subtaskCount(subtaskCount)
                .completedSubtaskCount(completedSubtaskCount)
                .build();
    }
}
