package com.personalfinance.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Ngoại lệ khi không tìm thấy tài nguyên (Entity không tồn tại)
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("Không tìm thấy %s với mã: %s", resourceName, identifier), HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
