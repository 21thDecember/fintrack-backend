package com.personalfinance.goal.repository;

import com.personalfinance.goal.entity.GoalMilestone;
import com.personalfinance.goal.entity.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalMilestoneRepository extends JpaRepository<GoalMilestone, UUID> {

    List<GoalMilestone> findByGoalIdAndUserIdOrderByDisplayOrderAscCreatedAtAsc(UUID goalId, UUID userId);

    Optional<GoalMilestone> findByIdAndGoalIdAndUserId(UUID id, UUID goalId, UUID userId);

    long countByGoalIdAndUserId(UUID goalId, UUID userId);

    long countByGoalIdAndUserIdAndStatus(UUID goalId, UUID userId, MilestoneStatus status);

    void deleteByGoalIdAndUserId(UUID goalId, UUID userId);
}
