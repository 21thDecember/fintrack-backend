package com.personalfinance.learning.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.repository.GoalRepository;
import com.personalfinance.learning.dto.SkillHistoryResponse;
import com.personalfinance.learning.dto.SkillLevelUpdateRequest;
import com.personalfinance.learning.dto.SkillRequest;
import com.personalfinance.learning.dto.SkillResponse;
import com.personalfinance.learning.entity.Skill;
import com.personalfinance.learning.entity.SkillCategory;
import com.personalfinance.learning.entity.SkillHistory;
import com.personalfinance.learning.repository.LearningSessionRepository;
import com.personalfinance.learning.repository.SkillHistoryRepository;
import com.personalfinance.learning.repository.SkillRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillHistoryRepository skillHistoryRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateGoalOwnership(request.getGoalId(), userId);
        validateLevels(request.getCurrentLevel(), request.getTargetLevel());

        Skill skill = Skill.builder()
                .userId(userId)
                .goalId(request.getGoalId())
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : SkillCategory.TECHNICAL)
                .currentLevel(request.getCurrentLevel())
                .targetLevel(request.getTargetLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        skill = skillRepository.save(skill);

        // Record initial history entry
        SkillHistory initialHistory = SkillHistory.builder()
                .skillId(skill.getId())
                .userId(userId)
                .previousLevel(request.getCurrentLevel())
                .newLevel(request.getCurrentLevel())
                .note("Khởi tạo kỹ năng ban đầu")
                .build();
        skillHistoryRepository.save(initialHistory);

        return toResponse(skill, true);
    }

    @Transactional
    public SkillResponse updateSkill(UUID id, SkillRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Skill skill = skillRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỹ năng"));

        validateGoalOwnership(request.getGoalId(), userId);
        validateLevels(request.getCurrentLevel(), request.getTargetLevel());

        Short oldLevel = skill.getCurrentLevel();
        Short newLevel = request.getCurrentLevel();

        skill.setGoalId(request.getGoalId());
        skill.setName(request.getName().trim());
        skill.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            skill.setCategory(request.getCategory());
        }
        skill.setCurrentLevel(newLevel);
        skill.setTargetLevel(request.getTargetLevel());
        if (request.getIsActive() != null) {
            skill.setIsActive(request.getIsActive());
        }

        skill = skillRepository.save(skill);

        // Record history if level changed
        if (!Objects.equals(oldLevel, newLevel)) {
            SkillHistory history = SkillHistory.builder()
                    .skillId(skill.getId())
                    .userId(userId)
                    .previousLevel(oldLevel)
                    .newLevel(newLevel)
                    .note("Cập nhật thông tin kỹ năng")
                    .build();
            skillHistoryRepository.save(history);
        }

        return toResponse(skill, true);
    }

    @Transactional
    public SkillResponse updateSkillLevel(UUID id, SkillLevelUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Skill skill = skillRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỹ năng"));

        validateLevelValue(request.getNewLevel());

        Short oldLevel = skill.getCurrentLevel();
        Short newLevel = request.getNewLevel();

        if (!Objects.equals(oldLevel, newLevel)) {
            skill.setCurrentLevel(newLevel);
            skill = skillRepository.save(skill);

            SkillHistory history = SkillHistory.builder()
                    .skillId(skill.getId())
                    .userId(userId)
                    .previousLevel(oldLevel)
                    .newLevel(newLevel)
                    .note(request.getNote() != null && !request.getNote().isBlank() ? request.getNote().trim() : "Nâng cấp level kỹ năng")
                    .build();
            skillHistoryRepository.save(history);
        }

        return toResponse(skill, true);
    }

    @Transactional(readOnly = true)
    public List<SkillHistoryResponse> getSkillHistory(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        skillRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỹ năng"));

        return skillHistoryRepository.findBySkillIdAndUserIdOrderByChangedAtDesc(id, userId).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSkill(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Skill skill = skillRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỹ năng"));

        skillRepository.delete(skill);
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Skill skill = skillRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỹ năng"));

        return toResponse(skill, true);
    }

    @Transactional(readOnly = true)
    public Page<SkillResponse> searchSkills(String keyword, SkillCategory category, Boolean isActive, UUID goalId, Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Specification<Skill> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (goalId != null) {
                predicates.add(cb.equal(root.get("goalId"), goalId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return skillRepository.findAll(spec, pageable).map(skill -> toResponse(skill, false));
    }

    public SkillResponse toResponse(Skill skill, boolean includeHistory) {
        String goalTitle = null;
        if (skill.getGoalId() != null) {
            goalTitle = goalRepository.findByIdAndUserId(skill.getGoalId(), skill.getUserId())
                    .map(Goal::getTitle)
                    .orElse(null);
        }

        int totalDuration = learningSessionRepository.sumDurationBySkillIdAndUserId(skill.getId(), skill.getUserId());

        List<SkillHistoryResponse> history = null;
        if (includeHistory) {
            history = skillHistoryRepository.findBySkillIdAndUserIdOrderByChangedAtDesc(skill.getId(), skill.getUserId()).stream()
                    .map(this::toHistoryResponse)
                    .collect(Collectors.toList());
        }

        return SkillResponse.builder()
                .id(skill.getId())
                .userId(skill.getUserId())
                .goalId(skill.getGoalId())
                .goalTitle(goalTitle)
                .name(skill.getName())
                .description(skill.getDescription())
                .category(skill.getCategory())
                .currentLevel(skill.getCurrentLevel())
                .targetLevel(skill.getTargetLevel())
                .isActive(skill.getIsActive())
                .totalDurationMinutes(totalDuration)
                .history(history)
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    private SkillHistoryResponse toHistoryResponse(SkillHistory history) {
        return SkillHistoryResponse.builder()
                .id(history.getId())
                .skillId(history.getSkillId())
                .previousLevel(history.getPreviousLevel())
                .newLevel(history.getNewLevel())
                .note(history.getNote())
                .changedAt(history.getChangedAt())
                .build();
    }

    private void validateGoalOwnership(UUID goalId, UUID userId) {
        if (goalId != null) {
            goalRepository.findByIdAndUserId(goalId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }
    }

    private void validateLevels(Short currentLevel, Short targetLevel) {
        validateLevelValue(currentLevel);
        if (targetLevel != null) {
            validateLevelValue(targetLevel);
        }
    }

    private void validateLevelValue(Short level) {
        if (level == null || level < 1 || level > 5) {
            throw new BusinessException("Trình độ kỹ năng phải nằm trong khoảng từ 1 đến 5", HttpStatus.BAD_REQUEST);
        }
    }
}
