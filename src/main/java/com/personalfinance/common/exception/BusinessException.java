package com.personalfinance.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Ngoại lệ xử lý các lỗi nghiệp vụ trong hệ thống
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
