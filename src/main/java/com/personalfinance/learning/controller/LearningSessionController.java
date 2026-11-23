package com.personalfinance.learning.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.learning.dto.LearningSessionRequest;
import com.personalfinance.learning.dto.LearningSessionResponse;
import com.personalfinance.learning.dto.LearningStatisticsResponse;
import com.personalfinance.learning.service.LearningSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/learning/sessions")
@RequiredArgsConstructor
public class LearningSessionController {

    private final LearningSessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LearningSessionResponse> createSession(@Valid @RequestBody LearningSessionRequest request) {
        return ApiResponse.success("Ghi nhận phiên học thành công", sessionService.createSession(request));
    }

    @GetMapping
    public ApiResponse<Page<LearningSessionResponse>> listSessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) UUID skillId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "learningDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success(sessionService.searchSessions(from, to, courseId, bookId, skillId, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningSessionResponse> getSessionById(@PathVariable UUID id) {
        return ApiResponse.success(sessionService.getSessionById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<LearningSessionResponse> updateSession(
            @PathVariable UUID id,
            @Valid @RequestBody LearningSessionRequest request
    ) {
        return ApiResponse.success("Cập nhật phiên học thành công", sessionService.updateSession(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable UUID id) {
        sessionService.deleteSession(id);
    }

    @GetMapping("/statistics")
    public ApiResponse<LearningStatisticsResponse> getSessionStatistics() {
        return ApiResponse.success(sessionService.getStatistics());
    }
}
