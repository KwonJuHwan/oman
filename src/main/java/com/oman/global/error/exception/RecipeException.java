package com.oman.global.error.exception;

import com.oman.global.error.ErrorCode;

public class RecipeException extends ApplicationException{

    public RecipeException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected RecipeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
