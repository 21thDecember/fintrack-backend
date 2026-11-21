package com.personalfinance.mood.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.journal.entity.DailyJournal;
import com.personalfinance.journal.repository.DailyJournalRepository;
import com.personalfinance.mood.dto.MoodCalendarDayResponse;
import com.personalfinance.mood.dto.MoodEntryRequest;
import com.personalfinance.mood.dto.MoodEntryResponse;
import com.personalfinance.mood.dto.MoodStatisticsResponse;
import com.personalfinance.mood.entity.MoodEntry;
import com.personalfinance.mood.repository.MoodEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodEntryRepository moodEntryRepository;
    private final DailyJournalRepository dailyJournalRepository;

    @Transactional
    public MoodEntryResponse createOrUpdateMood(MoodEntryRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        LocalDate date = request.getRecordedDate();
        if (date == null) {
            throw new BusinessException("Ngày ghi nhận không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (request.getMoodScore() == null || request.getMoodScore() < 1 || request.getMoodScore() > 5) {
            throw new BusinessException("Điểm tâm trạng phải từ 1 đến 5", HttpStatus.BAD_REQUEST);
        }
        if (request.getEnergyScore() != null && (request.getEnergyScore() < 1 || request.getEnergyScore() > 5)) {
            throw new BusinessException("Điểm năng lượng phải từ 1 đến 5", HttpStatus.BAD_REQUEST);
        }

        MoodEntry entry = moodEntryRepository.findByUserIdAndRecordedDate(userId, date)
                .orElseGet(() -> MoodEntry.builder()
                        .userId(userId)
                        .recordedDate(date)
                        .build());

        entry.setMoodScore(request.getMoodScore());
        entry.setEnergyScore(request.getEnergyScore());
        entry.setNote(request.getNote());

        MoodEntry saved = moodEntryRepository.save(entry);
        return mapToResponse(saved);
    }

    @Transactional
    public MoodEntryResponse updateMoodById(UUID id, MoodEntryRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        MoodEntry entry = moodEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu tâm trạng"));

        if (request.getRecordedDate() != null && !request.getRecordedDate().isEqual(entry.getRecordedDate())) {
            Optional<MoodEntry> existing = moodEntryRepository.findByUserIdAndRecordedDate(userId, request.getRecordedDate());
            if (existing.isPresent()) {
                throw new BusinessException("Đã có dữ liệu tâm trạng cho ngày " + request.getRecordedDate(), HttpStatus.CONFLICT);
            }
            entry.setRecordedDate(request.getRecordedDate());
        }

        if (request.getMoodScore() != null) {
            if (request.getMoodScore() < 1 || request.getMoodScore() > 5) {
                throw new BusinessException("Điểm tâm trạng phải từ 1 đến 5", HttpStatus.BAD_REQUEST);
            }
            entry.setMoodScore(request.getMoodScore());
        }

        if (request.getEnergyScore() != null) {
            if (request.getEnergyScore() < 1 || request.getEnergyScore() > 5) {
                throw new BusinessException("Điểm năng lượng phải từ 1 đến 5", HttpStatus.BAD_REQUEST);
            }
            entry.setEnergyScore(request.getEnergyScore());
        } else {
            entry.setEnergyScore(null);
        }

        entry.setNote(request.getNote());
        MoodEntry saved = moodEntryRepository.save(entry);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public MoodEntryResponse getMoodById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        MoodEntry entry = moodEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu tâm trạng"));
        return mapToResponse(entry);
    }

    @Transactional(readOnly = true)
    public MoodEntryResponse getMoodByDate(LocalDate date) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return moodEntryRepository.findByUserIdAndRecordedDate(userId, date)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MoodEntryResponse> listMoods(LocalDate from, LocalDate to) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        return moodEntryRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc(userId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMood(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        MoodEntry entry = moodEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu tâm trạng"));
        moodEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<MoodCalendarDayResponse> getCalendarData(LocalDate from, LocalDate to) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = from.plusMonths(1).minusDays(1);

        List<MoodEntry> moods = moodEntryRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc(userId, from, to);
        Map<LocalDate, MoodEntry> moodMap = moods.stream()
                .collect(Collectors.toMap(MoodEntry::getRecordedDate, Function.identity(), (a, b) -> a));

        List<DailyJournal> journals = dailyJournalRepository.findByUserIdAndJournalDateBetweenOrderByJournalDateDesc(userId, from, to);
        Set<LocalDate> journalDates = journals.stream()
                .map(DailyJournal::getJournalDate)
                .collect(Collectors.toSet());

        List<MoodCalendarDayResponse> result = new ArrayList<>();
        LocalDate curr = from;
        while (!curr.isAfter(to)) {
            MoodEntry m = moodMap.get(curr);
            result.add(MoodCalendarDayResponse.builder()
                    .date(curr)
                    .hasEntry(m != null)
                    .id(m != null ? m.getId() : null)
                    .moodScore(m != null ? m.getMoodScore() : null)
                    .energyScore(m != null ? m.getEnergyScore() : null)
                    .note(m != null ? m.getNote() : null)
                    .hasJournal(journalDates.contains(curr))
                    .build());
            curr = curr.plusDays(1);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public MoodStatisticsResponse getStatistics(LocalDate from, LocalDate to) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        List<MoodEntry> list = moodEntryRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc(userId, from, to);
        long totalDays = list.size();

        Map<Integer, Long> moodDist = new HashMap<>();
        Map<Integer, Long> energyDist = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            moodDist.put(i, 0L);
            energyDist.put(i, 0L);
        }

        double moodSum = 0;
        double energySum = 0;
        int energyCount = 0;

        for (MoodEntry m : list) {
            if (m.getMoodScore() != null) {
                moodDist.put(m.getMoodScore(), moodDist.getOrDefault(m.getMoodScore(), 0L) + 1);
                moodSum += m.getMoodScore();
            }
            if (m.getEnergyScore() != null) {
                energyDist.put(m.getEnergyScore(), energyDist.getOrDefault(m.getEnergyScore(), 0L) + 1);
                energySum += m.getEnergyScore();
                energyCount++;
            }
        }

        Double avgMood = totalDays > 0 ? Math.round((moodSum / totalDays) * 100.0) / 100.0 : null;
        Double avgEnergy = energyCount > 0 ? Math.round((energySum / energyCount) * 100.0) / 100.0 : null;

        // Determine predominant mood
        String predominant = "Chưa có";
        if (totalDays > 0) {
            long maxCount = -1;
            int maxScore = 3;
            for (Map.Entry<Integer, Long> entry : moodDist.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    maxScore = entry.getKey();
                }
            }
            predominant = switch (maxScore) {
                case 1 -> "Rất tệ";
                case 2 -> "Không tốt";
                case 3 -> "Bình thường";
                case 4 -> "Tốt";
                case 5 -> "Rất tốt";
                default -> "Bình thường";
            };
        }

        return MoodStatisticsResponse.builder()
                .from(from)
                .to(to)
                .totalRecordedDays(totalDays)
                .averageMood(avgMood)
                .averageEnergy(avgEnergy)
                .moodDistribution(moodDist)
                .energyDistribution(energyDist)
                .predominantMood(predominant)
                .build();
    }

    private MoodEntryResponse mapToResponse(MoodEntry entry) {
        return MoodEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .recordedDate(entry.getRecordedDate())
                .moodScore(entry.getMoodScore())
                .energyScore(entry.getEnergyScore())
                .note(entry.getNote())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
