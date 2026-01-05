package com.oman.global.error;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus; // import 추가

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    private String code;
    private String message;
    private HttpStatus httpStatus;
    private LocalDateTime timestamp;

    @Builder
    public ErrorResponse(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .httpStatus(errorCode.getHttpStatus())
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String additionalDetail) {
        return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(additionalDetail)
            .httpStatus(errorCode.getHttpStatus())
            .build();
    }
}