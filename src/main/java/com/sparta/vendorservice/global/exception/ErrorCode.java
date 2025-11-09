package com.sparta.vendorservice.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 각자 도메인에 맞게 에러 코드 작성
    USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."),

    VENDOR_ERROR(4000, HttpStatus.INTERNAL_SERVER_ERROR, "업체 서비스 요청 중 내부 서버 오류가 발생했습니다."),
    VENDOR_NOT_FOUND(4001, HttpStatus.NOT_FOUND, "존재하지 않는 업체입니다."),
    VENDOR_DUPLICATE_NAME(4002, HttpStatus.INTERNAL_SERVER_ERROR, "중복된 업체 이름입니다."),
    ;

    private final int code;
    private final HttpStatus status;
    private final String details;
}
