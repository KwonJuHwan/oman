package com.oman.domain.youtube.exception;

import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;

public class YoutubeAccessDeniedException extends YoutubeApiException {
    public YoutubeAccessDeniedException() {
        super(ErrorCode.FORBIDDEN); // FORBIDDEN ErrorCode 사용
    }
    public YoutubeAccessDeniedException(Throwable cause) {
        super(ErrorCode.FORBIDDEN, cause);
    }
}
