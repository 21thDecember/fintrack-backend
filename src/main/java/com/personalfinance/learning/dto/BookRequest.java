package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.BookStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "Tên sách không được để trống")
    @Size(max = 255, message = "Tên sách tối đa 255 ký tự")
    private String title;

    @Size(max = 255, message = "Tác giả tối đa 255 ký tự")
    private String author;

    private String description;

    @Size(max = 30, message = "Mã ISBN tối đa 30 ký tự")
    private String isbn;

    @Size(max = 100, message = "Danh mục tối đa 100 ký tự")
    private String category;

    @Size(max = 2048, message = "URL tối đa 2048 ký tự")
    private String url;

    private BookStatus status;

    @Min(value = 1, message = "Tổng số trang phải lớn hơn 0")
    private Integer totalPages;

    @Min(value = 0, message = "Số trang hiện tại phải >= 0")
    private Integer currentPage;

    private LocalDate startDate;

    private LocalDate targetDate;

    private UUID goalId;

    private String notes;
}
