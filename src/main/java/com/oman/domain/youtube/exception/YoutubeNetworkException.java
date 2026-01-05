package com.oman.domain.youtube.exception;

import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;

public class YoutubeNetworkException extends YoutubeApiException {
    public YoutubeNetworkException() {
        super(ErrorCode.YOUTUBE_API_NETWORK_ERROR);
    }
    public YoutubeNetworkException(Throwable cause) {
        super(ErrorCode.YOUTUBE_API_NETWORK_ERROR, cause);
    }
}
