package com.sparta.vendorservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.vendorservice.config.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class VendorExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        log.error("[VendorExceptionHandler] Unexpected error: {}", ex.getMessage(), ex);

        ApiResponse<?> response = new ApiResponse<>(ErrorCode.VENDOR_ERROR);

        return ResponseEntity
                .status(ErrorCode.VENDOR_ERROR.getStatus())
                .body(response);
    }

    /**
     * JSON 직렬화/역직렬화 에러 처리
     */
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiResponse<?>> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("[VendorExceptionHandler] JSON processing failed: {}", ex.getMessage(), ex);

        ApiResponse<?> response = new ApiResponse<>(ErrorCode.VENDOR_JSON_PROCESSING_EXCEPTION);

        return ResponseEntity
                .status(ErrorCode.VENDOR_JSON_PROCESSING_EXCEPTION.getStatus())
                .body(response);
    }
}
