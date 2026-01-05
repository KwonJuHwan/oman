package com.oman.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Error (Global)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부에 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않은 HTTP 메서드입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C005", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C006", "접근 권한이 없습니다."),
    // YouTube API Error
    YOUTUBE_API_GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Y001", "YouTube API 호출 중 알 수 없는 오류가 발생했습니다."),
    YOUTUBE_API_QUOTA_EXCEEDED(HttpStatus.FORBIDDEN, "Y002", "YouTube API 할당량이 초과되었습니다."),
    YOUTUBE_API_INVALID_KEY(HttpStatus.UNAUTHORIZED, "Y003", "YouTube API 키가 유효하지 않습니다."),
    YOUTUBE_API_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "Y004", "요청하신 YouTube 동영상을 찾을 수 없습니다."),
    YOUTUBE_API_NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Y005", "YouTube API 네트워크 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
