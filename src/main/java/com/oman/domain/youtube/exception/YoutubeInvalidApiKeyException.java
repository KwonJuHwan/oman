package com.oman.domain.youtube.exception;

import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;

public class YoutubeInvalidApiKeyException extends YoutubeApiException {
    public YoutubeInvalidApiKeyException() {
        super(ErrorCode.YOUTUBE_API_INVALID_KEY);
    }
    public YoutubeInvalidApiKeyException(Throwable cause) {
        super(ErrorCode.YOUTUBE_API_INVALID_KEY, cause);
    }

}
