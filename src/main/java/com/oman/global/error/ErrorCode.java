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
    //Culinary & Ingredient
    CULINARY_NOT_FOUND(HttpStatus.NOT_FOUND, "CU001", "요청하신 요리 정보를 찾을 수 없습니다."),
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CU002", "요청하신 재료 정보를 찾을 수 없습니다."),
    // Recipe Error
    RECIPE_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "해당 요리와 매칭되는 유튜브 영상을 찾을 수 없습니다."),
    RECIPE_DATA_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R002", "레시피 데이터 처리 중 오류가 발생했습니다."),
    // Statistic Error
    STATISTIC_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "해당 요리에 대한 통계 데이터가 존재하지 않습니다."),
    STATISTIC_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "통계 데이터 생성 중 오류가 발생했습니다."),
    // YouTube API Error
    YOUTUBE_API_GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Y001", "YouTube API 호출 중 알 수 없는 오류가 발생했습니다."),
    YOUTUBE_API_QUOTA_EXCEEDED(HttpStatus.FORBIDDEN, "Y002", "YouTube API 할당량이 초과되었습니다."),
    YOUTUBE_API_INVALID_KEY(HttpStatus.UNAUTHORIZED, "Y003", "YouTube API 키가 유효하지 않습니다."),
    YOUTUBE_API_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "Y004", "요청하신 YouTube 동영상을 찾을 수 없습니다."),
    YOUTUBE_API_NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Y005", "YouTube API 네트워크 연결에 실패했습니다."),
    MODEL_INFERENCE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M001", "FastAPI 모델 추론 서버와의 통신에 실패했습니다."),
    // Redis Error
    REDIS_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R001", "Redis 데이터 직렬화/역직렬화에 실패했습니다."),
    REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R002", "Redis 작업 수행 중 오류가 발생했습니다."),
    // Auth & Token Error
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않거나 만료된 토큰입니다."),
    LOGGED_OUT_USER(HttpStatus.UNAUTHORIZED, "A002", "로그아웃된 사용자입니다. 다시 로그인해주세요."),
    STOLEN_TOKEN_DETECTED(HttpStatus.FORBIDDEN, "A003", "비정상적인 접근이 감지되어 보안을 위해 로그아웃되었습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "A004", "존재하지 않는 사용자입니다."),
    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "A005", "Refresh Token이 필요합니다."),
    UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "A006", "지원하지 않는 소셜 로그인 제공자입니다."),
    //Google login Error
    GOOGLE_LOGIN_VERIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "구글 로그인 토큰 검증 중 서버 오류가 발생했습니다."),
    INVALID_GOOGLE_TOKEN(HttpStatus.UNAUTHORIZED, "G001", "유효하지 않은 구글 ID 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
