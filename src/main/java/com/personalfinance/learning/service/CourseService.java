package com.personalfinance.learning.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.repository.GoalRepository;
import com.personalfinance.learning.dto.CourseProgressUpdateRequest;
import com.personalfinance.learning.dto.CourseRequest;
import com.personalfinance.learning.dto.CourseResponse;
import com.personalfinance.learning.entity.Course;
import com.personalfinance.learning.entity.CoursePriority;
import com.personalfinance.learning.entity.CourseStatus;
import com.personalfinance.learning.repository.CourseRepository;
import com.personalfinance.learning.repository.LearningSessionRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateGoalOwnership(request.getGoalId(), userId);
        validateLessons(request.getTotalLessons(), request.getCompletedLessons());

        Course course = Course.builder()
                .userId(userId)
                .goalId(request.getGoalId())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .provider(request.getProvider())
                .url(request.getUrl())
                .category(request.getCategory())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.PLANNED)
                .priority(request.getPriority() != null ? request.getPriority() : CoursePriority.MEDIUM)
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .estimatedHours(request.getEstimatedHours())
                .totalLessons(request.getTotalLessons())
                .completedLessons(request.getCompletedLessons() != null ? request.getCompletedLessons() : 0)
                .notes(request.getNotes())
                .build();

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse updateCourse(UUID id, CourseRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        validateGoalOwnership(request.getGoalId(), userId);

        Integer totalLessons = request.getTotalLessons() != null ? request.getTotalLessons() : course.getTotalLessons();
        Integer completedLessons = request.getCompletedLessons() != null ? request.getCompletedLessons() : course.getCompletedLessons();
        validateLessons(totalLessons, completedLessons);

        course.setGoalId(request.getGoalId());
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription());
        course.setProvider(request.getProvider());
        course.setUrl(request.getUrl());
        course.setCategory(request.getCategory());
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            course.setPriority(request.getPriority());
        }
        course.setStartDate(request.getStartDate());
        course.setTargetDate(request.getTargetDate());
        course.setEstimatedHours(request.getEstimatedHours());
        course.setTotalLessons(request.getTotalLessons());
        course.setCompletedLessons(completedLessons);
        course.setNotes(request.getNotes());

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse updateCourseStatus(UUID id, CourseStatus status) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        course.setStatus(status);
        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse updateCourseProgress(UUID id, CourseProgressUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        if (request.getTotalLessons() != null) {
            course.setTotalLessons(request.getTotalLessons());
        }

        validateLessons(course.getTotalLessons(), request.getCompletedLessons());
        course.setCompletedLessons(request.getCompletedLessons());

        if (course.getTotalLessons() != null && course.getTotalLessons() > 0 &&
                course.getCompletedLessons().equals(course.getTotalLessons())) {
            course.setStatus(CourseStatus.COMPLETED);
        }

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Transactional
    public void deleteCourse(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> searchCourses(String keyword, CourseStatus status, String category, UUID goalId, Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (goalId != null) {
                predicates.add(cb.equal(root.get("goalId"), goalId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate providerLike = cb.like(cb.lower(root.get("provider")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, providerLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return courseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public CourseResponse toResponse(Course course) {
        String goalTitle = null;
        if (course.getGoalId() != null) {
            goalTitle = goalRepository.findByIdAndUserId(course.getGoalId(), course.getUserId())
                    .map(Goal::getTitle)
                    .orElse(null);
        }

        Integer progressPercent = null;
        if (course.getTotalLessons() != null && course.getTotalLessons() > 0 && course.getCompletedLessons() != null) {
            progressPercent = (int) Math.round((course.getCompletedLessons() * 100.0) / course.getTotalLessons());
            progressPercent = Math.min(100, Math.max(0, progressPercent));
        }

        int totalDuration = learningSessionRepository.sumDurationByCourseIdAndUserId(course.getId(), course.getUserId());

        return CourseResponse.builder()
                .id(course.getId())
                .userId(course.getUserId())
                .goalId(course.getGoalId())
                .goalTitle(goalTitle)
                .title(course.getTitle())
                .description(course.getDescription())
                .provider(course.getProvider())
                .url(course.getUrl())
                .category(course.getCategory())
                .status(course.getStatus())
                .priority(course.getPriority())
                .startDate(course.getStartDate())
                .targetDate(course.getTargetDate())
                .estimatedHours(course.getEstimatedHours())
                .totalLessons(course.getTotalLessons())
                .completedLessons(course.getCompletedLessons())
                .progressPercent(progressPercent)
                .totalDurationMinutes(totalDuration)
                .notes(course.getNotes())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private void validateGoalOwnership(UUID goalId, UUID userId) {
        if (goalId != null) {
            goalRepository.findByIdAndUserId(goalId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }
    }

    private void validateLessons(Integer totalLessons, Integer completedLessons) {
        if (totalLessons != null && totalLessons < 0) {
            throw new BusinessException("Tổng số bài học không được âm", HttpStatus.BAD_REQUEST);
        }
        if (completedLessons != null && completedLessons < 0) {
            throw new BusinessException("Số bài học hoàn thành không được âm", HttpStatus.BAD_REQUEST);
        }
        if (totalLessons != null && completedLessons != null && completedLessons > totalLessons) {
            throw new BusinessException("Số bài học hoàn thành không được vượt quá tổng số bài học", HttpStatus.BAD_REQUEST);
        }
    }
}
