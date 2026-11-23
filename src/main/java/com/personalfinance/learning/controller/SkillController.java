package com.personalfinance.learning.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.learning.dto.SkillHistoryResponse;
import com.personalfinance.learning.dto.SkillLevelUpdateRequest;
import com.personalfinance.learning.dto.SkillRequest;
import com.personalfinance.learning.dto.SkillResponse;
import com.personalfinance.learning.entity.SkillCategory;
import com.personalfinance.learning.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/learning/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        return ApiResponse.success("Tạo kỹ năng thành công", skillService.createSkill(request));
    }

    @GetMapping
    public ApiResponse<Page<SkillResponse>> listSkills(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SkillCategory category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success(skillService.searchSkills(keyword, category, isActive, goalId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<SkillResponse> getSkillById(@PathVariable UUID id) {
        return ApiResponse.success(skillService.getSkillById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<SkillResponse> updateSkill(@PathVariable UUID id, @Valid @RequestBody SkillRequest request) {
        return ApiResponse.success("Cập nhật kỹ năng thành công", skillService.updateSkill(id, request));
    }

    @PatchMapping("/{id}/level")
    public ApiResponse<SkillResponse> updateSkillLevel(
            @PathVariable UUID id,
            @Valid @RequestBody SkillLevelUpdateRequest request
    ) {
        return ApiResponse.success("Cập nhật trình độ kỹ năng thành công", skillService.updateSkillLevel(id, request));
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<SkillHistoryResponse>> getSkillHistory(@PathVariable UUID id) {
        return ApiResponse.success(skillService.getSkillHistory(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable UUID id) {
        skillService.deleteSkill(id);
    }
}
