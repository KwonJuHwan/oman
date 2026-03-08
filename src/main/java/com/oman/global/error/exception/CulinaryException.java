package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;

public class CulinaryException extends ApplicationException{

    public CulinaryException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected CulinaryException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
