package com.personalfinance.goal.repository;

import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.entity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID>, JpaSpecificationExecutor<Goal> {

    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    List<Goal> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, GoalStatus status);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.userId = :userId AND g.status NOT IN :excludedStatuses AND g.targetDate IS NOT NULL AND g.targetDate < :today")
    long countOverdueGoals(
            @Param("userId") UUID userId,
            @Param("excludedStatuses") Collection<GoalStatus> excludedStatuses,
            @Param("today") LocalDate today
    );
}
