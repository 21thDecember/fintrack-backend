package com.personalfinance.learning.repository;

import com.personalfinance.learning.entity.SkillHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillHistoryRepository extends JpaRepository<SkillHistory, UUID> {

    List<SkillHistory> findBySkillIdAndUserIdOrderByChangedAtDesc(UUID skillId, UUID userId);
}
