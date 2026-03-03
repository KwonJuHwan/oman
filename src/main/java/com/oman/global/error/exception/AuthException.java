package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;

public class AuthException extends ApplicationException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected AuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

