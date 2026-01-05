package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;
import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;
    // 원본 예외를 저장하여 로그나 추가 정보 분석에 활용
    private final Throwable sourceException;

    protected ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.sourceException = null;
    }

    protected ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.sourceException = cause;
    }
}
