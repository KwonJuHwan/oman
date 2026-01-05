package com.oman.domain.youtube.exception;


import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;

public class YoutubeInvalidParameterException extends YoutubeApiException {
    public YoutubeInvalidParameterException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
    public YoutubeInvalidParameterException(Throwable cause) {
        super(ErrorCode.INVALID_INPUT_VALUE, cause);
    }
}
