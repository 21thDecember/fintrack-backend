package com.personalfinance.habit.repository;

import com.personalfinance.habit.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID>, JpaSpecificationExecutor<Habit> {

    Optional<Habit> findByIdAndUserId(UUID id, UUID userId);

    List<Habit> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Habit> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);

    List<Habit> findByGoalIdAndUserId(UUID goalId, UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndIsActiveTrue(UUID userId);
}
