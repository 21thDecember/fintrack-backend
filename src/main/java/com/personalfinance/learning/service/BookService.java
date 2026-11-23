package com.personalfinance.learning.service;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.common.util.SecurityUtils;
import com.personalfinance.goal.entity.Goal;
import com.personalfinance.goal.repository.GoalRepository;
import com.personalfinance.learning.dto.BookProgressUpdateRequest;
import com.personalfinance.learning.dto.BookRequest;
import com.personalfinance.learning.dto.BookResponse;
import com.personalfinance.learning.entity.Book;
import com.personalfinance.learning.entity.BookStatus;
import com.personalfinance.learning.repository.BookRepository;
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
public class BookService {

    private final BookRepository bookRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public BookResponse addBook(BookRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        validateGoalOwnership(request.getGoalId(), userId);
        validatePages(request.getTotalPages(), request.getCurrentPage());

        Book book = Book.builder()
                .userId(userId)
                .goalId(request.getGoalId())
                .title(request.getTitle().trim())
                .author(request.getAuthor())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .category(request.getCategory())
                .url(request.getUrl())
                .status(request.getStatus() != null ? request.getStatus() : BookStatus.WANT_TO_READ)
                .totalPages(request.getTotalPages())
                .currentPage(request.getCurrentPage() != null ? request.getCurrentPage() : 0)
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .notes(request.getNotes())
                .build();

        book = bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse updateBook(UUID id, BookRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        validateGoalOwnership(request.getGoalId(), userId);

        Integer totalPages = request.getTotalPages() != null ? request.getTotalPages() : book.getTotalPages();
        Integer currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : book.getCurrentPage();
        validatePages(totalPages, currentPage);

        book.setGoalId(request.getGoalId());
        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setUrl(request.getUrl());
        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }
        book.setTotalPages(request.getTotalPages());
        book.setCurrentPage(currentPage);
        book.setStartDate(request.getStartDate());
        book.setTargetDate(request.getTargetDate());
        book.setNotes(request.getNotes());

        book = bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse updateBookStatus(UUID id, BookStatus status) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        book.setStatus(status);
        book = bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse updateReadingProgress(UUID id, BookProgressUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        if (request.getTotalPages() != null) {
            book.setTotalPages(request.getTotalPages());
        }

        validatePages(book.getTotalPages(), request.getCurrentPage());
        book.setCurrentPage(request.getCurrentPage());

        if (book.getTotalPages() != null && book.getTotalPages() > 0 &&
                book.getCurrentPage().equals(book.getTotalPages())) {
            book.setStatus(BookStatus.COMPLETED);
        }

        book = bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse updateBookNotes(UUID id, String notes) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        book.setNotes(notes);
        book = bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional
    public void deleteBook(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String keyword, BookStatus status, String category, UUID goalId, Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Specification<Book> spec = (root, query, cb) -> {
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
                Predicate authorLike = cb.like(cb.lower(root.get("author")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, authorLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public BookResponse toResponse(Book book) {
        String goalTitle = null;
        if (book.getGoalId() != null) {
            goalTitle = goalRepository.findByIdAndUserId(book.getGoalId(), book.getUserId())
                    .map(Goal::getTitle)
                    .orElse(null);
        }

        Integer progressPercent = null;
        if (book.getTotalPages() != null && book.getTotalPages() > 0 && book.getCurrentPage() != null) {
            progressPercent = (int) Math.round((book.getCurrentPage() * 100.0) / book.getTotalPages());
            progressPercent = Math.min(100, Math.max(0, progressPercent));
        }

        int totalDuration = learningSessionRepository.sumDurationByBookIdAndUserId(book.getId(), book.getUserId());

        return BookResponse.builder()
                .id(book.getId())
                .userId(book.getUserId())
                .goalId(book.getGoalId())
                .goalTitle(goalTitle)
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .category(book.getCategory())
                .url(book.getUrl())
                .status(book.getStatus())
                .totalPages(book.getTotalPages())
                .currentPage(book.getCurrentPage())
                .progressPercent(progressPercent)
                .totalDurationMinutes(totalDuration)
                .startDate(book.getStartDate())
                .targetDate(book.getTargetDate())
                .notes(book.getNotes())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    private void validateGoalOwnership(UUID goalId, UUID userId) {
        if (goalId != null) {
            goalRepository.findByIdAndUserId(goalId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu liên kết không tồn tại"));
        }
    }

    private void validatePages(Integer totalPages, Integer currentPage) {
        if (totalPages != null && totalPages <= 0) {
            throw new BusinessException("Tổng số trang phải lớn hơn 0", HttpStatus.BAD_REQUEST);
        }
        if (currentPage != null && currentPage < 0) {
            throw new BusinessException("Số trang hiện tại không được âm", HttpStatus.BAD_REQUEST);
        }
        if (totalPages != null && currentPage != null && currentPage > totalPages) {
            throw new BusinessException("Số trang hiện tại không được vượt quá tổng số trang", HttpStatus.BAD_REQUEST);
        }
    }
}
