package com.personalfinance.learning.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.learning.dto.BookProgressUpdateRequest;
import com.personalfinance.learning.dto.BookRequest;
import com.personalfinance.learning.dto.BookResponse;
import com.personalfinance.learning.dto.BookStatusUpdateRequest;
import com.personalfinance.learning.entity.BookStatus;
import com.personalfinance.learning.service.BookService;
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
@RequestMapping("/api/learning/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponse> addBook(@Valid @RequestBody BookRequest request) {
        return ApiResponse.success("Thêm sách thành công", bookService.addBook(request));
    }

    @GetMapping
    public ApiResponse<Page<BookResponse>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BookStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success(bookService.searchBooks(keyword, status, category, goalId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable UUID id) {
        return ApiResponse.success(bookService.getBookById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable UUID id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.success("Cập nhật sách thành công", bookService.updateBook(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<BookResponse> updateBookStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BookStatusUpdateRequest request
    ) {
        return ApiResponse.success("Cập nhật trạng thái đọc thành công", bookService.updateBookStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/progress")
    public ApiResponse<BookResponse> updateReadingProgress(
            @PathVariable UUID id,
            @Valid @RequestBody BookProgressUpdateRequest request
    ) {
        return ApiResponse.success("Cập nhật tiến độ đọc thành công", bookService.updateReadingProgress(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
    }
}
