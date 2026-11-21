package com.personalfinance.journal.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.journal.dto.DailyJournalRequest;
import com.personalfinance.journal.dto.DailyJournalResponse;
import com.personalfinance.journal.dto.JournalCalendarDayResponse;
import com.personalfinance.journal.entity.DailyJournal;
import com.personalfinance.journal.repository.DailyJournalRepository;
import com.personalfinance.mood.entity.MoodEntry;
import com.personalfinance.mood.repository.MoodEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyJournalService {

    private final DailyJournalRepository dailyJournalRepository;
    private final MoodEntryRepository moodEntryRepository;

    @Transactional
    public DailyJournalResponse createOrUpdateJournal(DailyJournalRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate date = request.getJournalDate();
        if (date == null) {
            throw new BusinessException("Ngày nhật ký không được để trống", HttpStatus.BAD_REQUEST);
        }

        DailyJournal journal = dailyJournalRepository.findByUserIdAndJournalDate(userId, date)
                .orElseGet(() -> DailyJournal.builder()
                        .userId(userId)
                        .journalDate(date)
                        .build());

        journal.setTitle(request.getTitle());
        journal.setContent(request.getContent());
        journal.setHighlight(request.getHighlight());
        journal.setGratitude(request.getGratitude());
        journal.setImprovement(request.getImprovement());
        journal.setTomorrowPlan(request.getTomorrowPlan());

        DailyJournal saved = dailyJournalRepository.save(journal);
        return mapToResponse(saved, userId);
    }

    @Transactional
    public DailyJournalResponse updateJournalById(UUID id, DailyJournalRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        DailyJournal journal = dailyJournalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký"));

        // If date changed, ensure no conflict
        if (request.getJournalDate() != null && !request.getJournalDate().isEqual(journal.getJournalDate())) {
            Optional<DailyJournal> existing = dailyJournalRepository.findByUserIdAndJournalDate(userId, request.getJournalDate());
            if (existing.isPresent()) {
                throw new BusinessException("Đã có nhật ký cho ngày " + request.getJournalDate(), HttpStatus.CONFLICT);
            }
            journal.setJournalDate(request.getJournalDate());
        }

        journal.setTitle(request.getTitle());
        journal.setContent(request.getContent());
        journal.setHighlight(request.getHighlight());
        journal.setGratitude(request.getGratitude());
        journal.setImprovement(request.getImprovement());
        journal.setTomorrowPlan(request.getTomorrowPlan());

        DailyJournal saved = dailyJournalRepository.save(journal);
        return mapToResponse(saved, userId);
    }

    @Transactional(readOnly = true)
    public DailyJournalResponse getJournalById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        DailyJournal journal = dailyJournalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký"));
        return mapToResponse(journal, userId);
    }

    @Transactional(readOnly = true)
    public DailyJournalResponse getJournalByDate(LocalDate date) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Optional<DailyJournal> journalOpt = dailyJournalRepository.findByUserIdAndJournalDate(userId, date);
        if (journalOpt.isEmpty()) {
            // Also check if mood exists for this date, so user can see it even without a journal
            Optional<MoodEntry> moodOpt = moodEntryRepository.findByUserIdAndRecordedDate(userId, date);
            if (moodOpt.isPresent()) {
                MoodEntry mood = moodOpt.get();
                return DailyJournalResponse.builder()
                        .userId(userId)
                        .journalDate(date)
                        .moodScore(mood.getMoodScore())
                        .energyScore(mood.getEnergyScore())
                        .moodNote(mood.getNote())
                        .build();
            }
            return null;
        }
        return mapToResponse(journalOpt.get(), userId);
    }

    @Transactional(readOnly = true)
    public Page<DailyJournalResponse> searchJournals(LocalDate from, LocalDate to, String keyword, Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Specification<DailyJournal> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("journalDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("journalDate"), to));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), pattern);
                Predicate contentPred = cb.like(cb.lower(root.get("content")), pattern);
                Predicate highlightPred = cb.like(cb.lower(root.get("highlight")), pattern);
                Predicate gratitudePred = cb.like(cb.lower(root.get("gratitude")), pattern);
                predicates.add(cb.or(titlePred, contentPred, highlightPred, gratitudePred));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return dailyJournalRepository.findAll(spec, pageable)
                .map(j -> mapToResponse(j, userId));
    }

    @Transactional
    public void deleteJournal(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        DailyJournal journal = dailyJournalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký"));
        dailyJournalRepository.delete(journal);
    }

    @Transactional(readOnly = true)
    public List<JournalCalendarDayResponse> getCalendarData(LocalDate from, LocalDate to) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = from.plusMonths(1).minusDays(1);

        List<DailyJournal> journals = dailyJournalRepository.findByUserIdAndJournalDateBetweenOrderByJournalDateDesc(userId, from, to);
        Map<LocalDate, DailyJournal> journalMap = journals.stream()
                .collect(Collectors.toMap(DailyJournal::getJournalDate, Function.identity(), (a, b) -> a));

        List<MoodEntry> moods = moodEntryRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc(userId, from, to);
        Map<LocalDate, MoodEntry> moodMap = moods.stream()
                .collect(Collectors.toMap(MoodEntry::getRecordedDate, Function.identity(), (a, b) -> a));

        List<JournalCalendarDayResponse> result = new ArrayList<>();
        LocalDate curr = from;
        while (!curr.isAfter(to)) {
            DailyJournal j = journalMap.get(curr);
            MoodEntry m = moodMap.get(curr);

            boolean hasContent = j != null && (
                    (j.getContent() != null && !j.getContent().isBlank()) ||
                    (j.getHighlight() != null && !j.getHighlight().isBlank()) ||
                    (j.getGratitude() != null && !j.getGratitude().isBlank())
            );

            result.add(JournalCalendarDayResponse.builder()
                    .date(curr)
                    .hasJournal(j != null)
                    .journalId(j != null ? j.getId() : null)
                    .title(j != null ? j.getTitle() : null)
                    .hasContent(hasContent)
                    .moodScore(m != null ? m.getMoodScore() : null)
                    .energyScore(m != null ? m.getEnergyScore() : null)
                    .build());

            curr = curr.plusDays(1);
        }

        return result;
    }

    private DailyJournalResponse mapToResponse(DailyJournal journal, UUID userId) {
        Optional<MoodEntry> moodOpt = moodEntryRepository.findByUserIdAndRecordedDate(userId, journal.getJournalDate());
        return DailyJournalResponse.builder()
                .id(journal.getId())
                .userId(journal.getUserId())
                .journalDate(journal.getJournalDate())
                .title(journal.getTitle())
                .content(journal.getContent())
                .highlight(journal.getHighlight())
                .gratitude(journal.getGratitude())
                .improvement(journal.getImprovement())
                .tomorrowPlan(journal.getTomorrowPlan())
                .createdAt(journal.getCreatedAt())
                .updatedAt(journal.getUpdatedAt())
                .moodScore(moodOpt.map(MoodEntry::getMoodScore).orElse(null))
                .energyScore(moodOpt.map(MoodEntry::getEnergyScore).orElse(null))
                .moodNote(moodOpt.map(MoodEntry::getNote).orElse(null))
                .build();
    }
}
