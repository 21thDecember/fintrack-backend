package com.personalfinance.habit.repository;

import com.personalfinance.habit.entity.HabitCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitCheckinRepository extends JpaRepository<HabitCheckin, UUID> {

    Optional<HabitCheckin> findByHabitIdAndCompletionDate(UUID habitId, LocalDate completionDate);

    List<HabitCheckin> findByHabitIdOrderByCompletionDateDesc(UUID habitId);

    List<HabitCheckin> findByHabitIdInAndCompletionDate(Collection<UUID> habitIds, LocalDate completionDate);

    List<HabitCheckin> findByHabitIdAndCompletionDateBetweenOrderByCompletionDateAsc(UUID habitId, LocalDate from, LocalDate to);

    void deleteByHabitIdAndCompletionDate(UUID habitId, LocalDate completionDate);

    void deleteByHabitId(UUID habitId);

    long countByHabitId(UUID habitId);
}
