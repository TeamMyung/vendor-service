package com.sparta.vendorservice.global.exception;

import lombok.Getter;

@Getter
public class VendorException extends RuntimeException {
    private final ErrorCode errorCode;

    // ErrorCode만 받는 생성자
    public VendorException(ErrorCode errorCode) {
        super(errorCode.getDetails());
        this.errorCode = errorCode;
    }

    // 메시지를 추가로 지정하고 싶을 때
    public VendorException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
