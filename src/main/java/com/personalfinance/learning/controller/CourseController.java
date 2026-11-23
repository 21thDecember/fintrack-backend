package com.personalfinance.learning.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.learning.dto.CourseProgressUpdateRequest;
import com.personalfinance.learning.dto.CourseRequest;
import com.personalfinance.learning.dto.CourseResponse;
import com.personalfinance.learning.dto.CourseStatusUpdateRequest;
import com.personalfinance.learning.entity.CourseStatus;
import com.personalfinance.learning.service.CourseService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/learning/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.success("Tạo khóa học thành công", courseService.createCourse(request));
    }

    @GetMapping
    public ApiResponse<Page<CourseResponse>> listCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success(courseService.searchCourses(keyword, status, category, goalId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable UUID id) {
        return ApiResponse.success(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> updateCourse(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.success("Cập nhật khóa học thành công", courseService.updateCourse(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CourseResponse> updateCourseStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CourseStatusUpdateRequest request
    ) {
        return ApiResponse.success("Cập nhật trạng thái thành công", courseService.updateCourseStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/progress")
    public ApiResponse<CourseResponse> updateCourseProgress(
            @PathVariable UUID id,
            @Valid @RequestBody CourseProgressUpdateRequest request
    ) {
        return ApiResponse.success("Cập nhật tiến độ thành công", courseService.updateCourseProgress(id, request));
    }

    @PatchMapping("/{id}/notes")
    public ApiResponse<CourseResponse> updateCourseNotes(
            @PathVariable UUID id,
            @RequestBody com.personalfinance.learning.dto.UpdateNotesRequest request
    ) {
        return ApiResponse.success("Cập nhật ghi chú khóa học thành công", courseService.updateCourseNotes(id, request.getNotes()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
    }
}
