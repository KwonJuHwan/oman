package com.oman.domain.youtube.exception;


import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;

public class YoutubeQuotaExceededException extends YoutubeApiException {
    public YoutubeQuotaExceededException() {
        super(ErrorCode.YOUTUBE_API_QUOTA_EXCEEDED);
    }
    public YoutubeQuotaExceededException(Throwable cause) {
        super(ErrorCode.YOUTUBE_API_QUOTA_EXCEEDED, cause);
    }
}
