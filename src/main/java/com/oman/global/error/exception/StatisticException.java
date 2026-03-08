package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;

public class StatisticException extends ApplicationException{

    public StatisticException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected StatisticException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
