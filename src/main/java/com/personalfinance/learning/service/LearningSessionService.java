package com.personalfinance.learning.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.learning.dto.CategoryTimeItem;
import com.personalfinance.learning.dto.LearningSessionRequest;
import com.personalfinance.learning.dto.LearningSessionResponse;
import com.personalfinance.learning.dto.LearningStatisticsResponse;
import com.personalfinance.learning.entity.Book;
import com.personalfinance.learning.entity.Course;
import com.personalfinance.learning.entity.LearningSession;
import com.personalfinance.learning.entity.SessionSource;
import com.personalfinance.learning.entity.Skill;
import com.personalfinance.learning.repository.BookRepository;
import com.personalfinance.learning.repository.CourseRepository;
import com.personalfinance.learning.repository.LearningSessionRepository;
import com.personalfinance.learning.repository.SkillRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningSessionService {

    private final LearningSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final BookRepository bookRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public LearningSessionResponse createSession(LearningSessionRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateSessionTimes(request.getStartTime(), request.getEndTime());
        validateEntitiesOwnership(request.getCourseId(), request.getBookId(), request.getSkillId(), userId);

        LearningSession session = LearningSession.builder()
                .userId(userId)
                .courseId(request.getCourseId())
                .bookId(request.getBookId())
                .skillId(request.getSkillId())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .learningDate(request.getLearningDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(request.getDurationMinutes())
                .source(request.getSource() != null ? request.getSource() : SessionSource.MANUAL)
                .notes(request.getNotes())
                .build();

        session = sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public LearningSessionResponse updateSession(UUID id, LearningSessionRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LearningSession session = sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên học"));

        validateSessionTimes(request.getStartTime(), request.getEndTime());
        validateEntitiesOwnership(request.getCourseId(), request.getBookId(), request.getSkillId(), userId);

        session.setCourseId(request.getCourseId());
        session.setBookId(request.getBookId());
        session.setSkillId(request.getSkillId());
        session.setTitle(request.getTitle().trim());
        session.setDescription(request.getDescription());
        session.setLearningDate(request.getLearningDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setDurationMinutes(request.getDurationMinutes());
        if (request.getSource() != null) {
            session.setSource(request.getSource());
        }
        session.setNotes(request.getNotes());

        session = sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public LearningSessionResponse updateSessionNotes(UUID id, String notes) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LearningSession session = sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên học"));

        session.setNotes(notes);
        session = sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public void deleteSession(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LearningSession session = sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên học"));

        sessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public LearningSessionResponse getSessionById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LearningSession session = sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên học"));

        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public Page<LearningSessionResponse> searchSessions(
            LocalDate from,
            LocalDate to,
            UUID courseId,
            UUID bookId,
            UUID skillId,
            String keyword,
            Pageable pageable
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Specification<LearningSession> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("learningDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("learningDate"), to));
            }
            if (courseId != null) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }
            if (bookId != null) {
                predicates.add(cb.equal(root.get("bookId"), bookId));
            }
            if (skillId != null) {
                predicates.add(cb.equal(root.get("skillId"), skillId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                Predicate notesLike = cb.like(cb.lower(root.get("notes")), pattern);
                predicates.add(cb.or(titleLike, descLike, notesLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return sessionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public LearningStatisticsResponse getStatistics() {
        UUID userId = SecurityUtils.getCurrentUserId();

        int totalDuration = sessionRepository.sumDurationByUserIdAndLearningDateBetween(
                userId,
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2099, 12, 31)
        );
        long totalSessions = sessionRepository.countByUserId(userId);
        long activeDays = sessionRepository.countDistinctLearningDateByUserId(userId);

        // Course breakdown
        List<Object[]> courseRows = sessionRepository.sumDurationGroupByCourse(userId);
        Map<UUID, Integer> courseDurations = new HashMap<>();
        for (Object[] row : courseRows) {
            courseDurations.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        List<CategoryTimeItem> timeByCourse = courseRepository.findAllById(courseDurations.keySet()).stream()
                .map(c -> CategoryTimeItem.builder()
                        .id(c.getId())
                        .name(c.getTitle())
                        .durationMinutes(courseDurations.getOrDefault(c.getId(), 0))
                        .build())
                .sorted((a, b) -> Integer.compare(b.getDurationMinutes(), a.getDurationMinutes()))
                .collect(Collectors.toList());

        // Book breakdown
        List<Object[]> bookRows = sessionRepository.sumDurationGroupByBook(userId);
        Map<UUID, Integer> bookDurations = new HashMap<>();
        for (Object[] row : bookRows) {
            bookDurations.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        List<CategoryTimeItem> timeByBook = bookRepository.findAllById(bookDurations.keySet()).stream()
                .map(b -> CategoryTimeItem.builder()
                        .id(b.getId())
                        .name(b.getTitle())
                        .durationMinutes(bookDurations.getOrDefault(b.getId(), 0))
                        .build())
                .sorted((a, b) -> Integer.compare(b.getDurationMinutes(), a.getDurationMinutes()))
                .collect(Collectors.toList());

        // Skill breakdown
        List<Object[]> skillRows = sessionRepository.sumDurationGroupBySkill(userId);
        Map<UUID, Integer> skillDurations = new HashMap<>();
        for (Object[] row : skillRows) {
            skillDurations.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        List<CategoryTimeItem> timeBySkill = skillRepository.findAllById(skillDurations.keySet()).stream()
                .map(s -> CategoryTimeItem.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .durationMinutes(skillDurations.getOrDefault(s.getId(), 0))
                        .build())
                .sorted((a, b) -> Integer.compare(b.getDurationMinutes(), a.getDurationMinutes()))
                .collect(Collectors.toList());

        return LearningStatisticsResponse.builder()
                .totalDurationMinutes(totalDuration)
                .totalSessionsCount(totalSessions)
                .activeDaysCount(activeDays)
                .timeByCourse(timeByCourse)
                .timeByBook(timeByBook)
                .timeBySkill(timeBySkill)
                .build();
    }

    public LearningSessionResponse toResponse(LearningSession session) {
        String courseTitle = null;
        if (session.getCourseId() != null) {
            courseTitle = courseRepository.findByIdAndUserId(session.getCourseId(), session.getUserId())
                    .map(Course::getTitle)
                    .orElse(null);
        }

        String bookTitle = null;
        if (session.getBookId() != null) {
            bookTitle = bookRepository.findByIdAndUserId(session.getBookId(), session.getUserId())
                    .map(Book::getTitle)
                    .orElse(null);
        }

        String skillName = null;
        if (session.getSkillId() != null) {
            skillName = skillRepository.findByIdAndUserId(session.getSkillId(), session.getUserId())
                    .map(Skill::getName)
                    .orElse(null);
        }

        return LearningSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .courseId(session.getCourseId())
                .courseTitle(courseTitle)
                .bookId(session.getBookId())
                .bookTitle(bookTitle)
                .skillId(session.getSkillId())
                .skillName(skillName)
                .title(session.getTitle())
                .description(session.getDescription())
                .learningDate(session.getLearningDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .durationMinutes(session.getDurationMinutes())
                .source(session.getSource())
                .notes(session.getNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private void validateSessionTimes(OffsetDateTime startTime, OffsetDateTime endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateEntitiesOwnership(UUID courseId, UUID bookId, UUID skillId, UUID userId) {
        if (courseId != null) {
            courseRepository.findByIdAndUserId(courseId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khóa học liên kết không tồn tại"));
        }
        if (bookId != null) {
            bookRepository.findByIdAndUserId(bookId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sách liên kết không tồn tại"));
        }
        if (skillId != null) {
            skillRepository.findByIdAndUserId(skillId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng liên kết không tồn tại"));
        }
    }
}
