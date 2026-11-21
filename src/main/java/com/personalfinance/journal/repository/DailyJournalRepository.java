package com.personalfinance.journal.repository;

import com.personalfinance.journal.entity.DailyJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyJournalRepository extends JpaRepository<DailyJournal, UUID>, JpaSpecificationExecutor<DailyJournal> {

    Optional<DailyJournal> findByUserIdAndJournalDate(UUID userId, LocalDate journalDate);

    Optional<DailyJournal> findByIdAndUserId(UUID id, UUID userId);

    List<DailyJournal> findByUserIdAndJournalDateBetweenOrderByJournalDateDesc(UUID userId, LocalDate from, LocalDate to);

    long countByUserIdAndJournalDateBetween(UUID userId, LocalDate from, LocalDate to);
}
