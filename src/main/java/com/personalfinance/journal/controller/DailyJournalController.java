package com.personalfinance.journal.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.journal.dto.DailyJournalRequest;
import com.personalfinance.journal.dto.DailyJournalResponse;
import com.personalfinance.journal.dto.JournalCalendarDayResponse;
import com.personalfinance.journal.service.DailyJournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class DailyJournalController {

    private final DailyJournalService dailyJournalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DailyJournalResponse> createJournal(@Valid @RequestBody DailyJournalRequest request) {
        return ApiResponse.success("Lưu nhật ký thành công", dailyJournalService.createOrUpdateJournal(request));
    }

    @GetMapping
    public ApiResponse<Page<DailyJournalResponse>> listJournals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(dailyJournalService.searchJournals(from, to, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<DailyJournalResponse> getJournalById(@PathVariable UUID id) {
        return ApiResponse.success(dailyJournalService.getJournalById(id));
    }

    @GetMapping("/by-date/{date}")
    public ApiResponse<DailyJournalResponse> getJournalByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(dailyJournalService.getJournalByDate(date));
    }

    @PutMapping("/{id}")
    public ApiResponse<DailyJournalResponse> updateJournal(
            @PathVariable UUID id,
            @Valid @RequestBody DailyJournalRequest request
    ) {
        return ApiResponse.success("Cập nhật nhật ký thành công", dailyJournalService.updateJournalById(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteJournal(@PathVariable UUID id) {
        dailyJournalService.deleteJournal(id);
        return ApiResponse.success("Xóa nhật ký thành công", null);
    }

    @GetMapping("/calendar")
    public ApiResponse<List<JournalCalendarDayResponse>> getCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(dailyJournalService.getCalendarData(from, to));
    }
}
