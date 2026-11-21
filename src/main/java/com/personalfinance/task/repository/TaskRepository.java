package com.personalfinance.task.repository;

import com.personalfinance.task.entity.Task;
import com.personalfinance.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    List<Task> findByParentTaskIdAndUserIdOrderByCreatedAtAsc(UUID parentTaskId, UUID userId);

    List<Task> findByParentTaskIdInAndUserId(List<UUID> parentTaskIds, UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.userId = :userId AND t.status NOT IN :excludedStatuses AND t.dueDate IS NOT NULL AND t.dueDate < :now")
    long countOverdueTasks(
            @Param("userId") UUID userId,
            @Param("excludedStatuses") Collection<TaskStatus> excludedStatuses,
            @Param("now") OffsetDateTime now
    );

    long countByUserIdAndCompletedAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);

    long countByUserIdAndDueDateBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);

    List<Task> findByGoalIdAndUserId(UUID goalId, UUID userId);
}
