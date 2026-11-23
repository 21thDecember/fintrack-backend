package com.personalfinance.learning.repository;

import com.personalfinance.learning.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {

    Optional<Skill> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndIsActive(UUID userId, Boolean isActive);

    List<Skill> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    List<Skill> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
