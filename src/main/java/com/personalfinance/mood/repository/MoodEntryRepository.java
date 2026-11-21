package com.personalfinance.mood.repository;

import com.personalfinance.mood.entity.MoodEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, UUID> {

    Optional<MoodEntry> findByUserIdAndRecordedDate(UUID userId, LocalDate recordedDate);

    Optional<MoodEntry> findByIdAndUserId(UUID id, UUID userId);

    List<MoodEntry> findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc(UUID userId, LocalDate from, LocalDate to);

    Page<MoodEntry> findByUserIdOrderByRecordedDateDesc(UUID userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM MoodEntry m WHERE m.userId = :userId AND m.recordedDate BETWEEN :from AND :to")
    long countByUserIdAndRecordedDateBetween(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT AVG(m.moodScore) FROM MoodEntry m WHERE m.userId = :userId AND m.recordedDate BETWEEN :from AND :to")
    Double getAverageMoodScore(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT AVG(m.energyScore) FROM MoodEntry m WHERE m.userId = :userId AND m.recordedDate BETWEEN :from AND :to AND m.energyScore IS NOT NULL")
    Double getAverageEnergyScore(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
