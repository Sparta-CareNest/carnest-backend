package com.carenest.business.aiservice.exception;

public class AiException extends RuntimeException{

    private final ErrorCode errorCode;

    public AiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
