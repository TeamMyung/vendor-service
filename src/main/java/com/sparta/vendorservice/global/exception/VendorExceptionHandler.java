package com.sparta.vendorservice.global.exception;

import com.sparta.vendorservice.global.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class VendorExceptionHandler {

    @ExceptionHandler(VendorException.class)
    public ResponseEntity<ApiResponse<?>> handleVendorException(VendorException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity
                .status(ErrorCode.VENDOR_NOT_FOUND.getStatus().value())
                .body(new ApiResponse<>(ErrorCode.VENDOR_NOT_FOUND));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity
                .status(ErrorCode.VENDOR_DUPLICATE_NAME.getStatus().value())
                .body(new ApiResponse<>(ErrorCode.VENDOR_DUPLICATE_NAME));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception e) {
        log.error("VENDOR_ERROR 발생", e);
        return ResponseEntity
                .status(ErrorCode.VENDOR_ERROR.getStatus().value())
                .body(new ApiResponse<>(ErrorCode.VENDOR_ERROR));
    }
}
