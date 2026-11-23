package com.personalfinance.learning.repository;

import com.personalfinance.learning.entity.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, UUID>, JpaSpecificationExecutor<LearningSession> {

    Optional<LearningSession> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM LearningSession s WHERE s.courseId = :courseId AND s.userId = :userId")
    int sumDurationByCourseIdAndUserId(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM LearningSession s WHERE s.bookId = :bookId AND s.userId = :userId")
    int sumDurationByBookIdAndUserId(@Param("bookId") UUID bookId, @Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM LearningSession s WHERE s.skillId = :skillId AND s.userId = :userId")
    int sumDurationBySkillIdAndUserId(@Param("skillId") UUID skillId, @Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM LearningSession s WHERE s.userId = :userId AND s.learningDate = :date")
    int sumDurationByUserIdAndLearningDate(@Param("userId") UUID userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM LearningSession s WHERE s.userId = :userId AND s.learningDate BETWEEN :from AND :to")
    int sumDurationByUserIdAndLearningDateBetween(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(DISTINCT s.learningDate) FROM LearningSession s WHERE s.userId = :userId")
    long countDistinctLearningDateByUserId(@Param("userId") UUID userId);

    long countByUserId(UUID userId);

    List<LearningSession> findTop10ByUserIdOrderByLearningDateDescCreatedAtDesc(UUID userId);

    @Query("SELECT s.courseId, SUM(s.durationMinutes) FROM LearningSession s WHERE s.userId = :userId AND s.courseId IS NOT NULL GROUP BY s.courseId")
    List<Object[]> sumDurationGroupByCourse(@Param("userId") UUID userId);

    @Query("SELECT s.bookId, SUM(s.durationMinutes) FROM LearningSession s WHERE s.userId = :userId AND s.bookId IS NOT NULL GROUP BY s.bookId")
    List<Object[]> sumDurationGroupByBook(@Param("userId") UUID userId);

    @Query("SELECT s.skillId, SUM(s.durationMinutes) FROM LearningSession s WHERE s.userId = :userId AND s.skillId IS NOT NULL GROUP BY s.skillId")
    List<Object[]> sumDurationGroupBySkill(@Param("userId") UUID userId);

    @Query("SELECT s.learningDate, SUM(s.durationMinutes) FROM LearningSession s WHERE s.userId = :userId AND s.learningDate BETWEEN :from AND :to GROUP BY s.learningDate ORDER BY s.learningDate ASC")
    List<Object[]> sumDurationGroupByDateBetween(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
