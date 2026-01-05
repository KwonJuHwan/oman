package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;

public abstract class YoutubeApiException extends ApplicationException {

    protected YoutubeApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected YoutubeApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
