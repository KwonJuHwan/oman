package com.oman.domain.youtube.exception;

import com.oman.global.error.exception.YoutubeApiException;
import com.oman.global.error.ErrorCode;

public class YoutubeVideoNotFoundException extends YoutubeApiException {
    public YoutubeVideoNotFoundException() {
        super(ErrorCode.YOUTUBE_API_VIDEO_NOT_FOUND);
    }

    public YoutubeVideoNotFoundException(Throwable cause) {
        super(ErrorCode.YOUTUBE_API_VIDEO_NOT_FOUND, cause);
    }
}
