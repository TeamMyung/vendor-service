package com.sparta.vendorservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    VENDOR_ERROR(4000, HttpStatus.INTERNAL_SERVER_ERROR, "업체 에러 발생"),
    VENDOR_JSON_PROCESSING_EXCEPTION(4001, HttpStatus.INTERNAL_SERVER_ERROR, "업체 응답 객체를 JSON으로 변환하지 못했습니다"),
    ;

    private final int code;
    private final HttpStatus status;
    private final String details;
}
