package com.oman.domain.youtube.exception;

import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;
public class YoutubeApiGeneralException extends YoutubeApiException {
    public YoutubeApiGeneralException() {
        super(ErrorCode.FORBIDDEN); // FORBIDDEN ErrorCode 사용
    }
    public YoutubeApiGeneralException(Throwable cause) {
        super(ErrorCode.FORBIDDEN, cause);
    }
}
