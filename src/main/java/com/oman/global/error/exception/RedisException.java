package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;

public class RedisException extends ApplicationException {

    public RedisException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RedisException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}