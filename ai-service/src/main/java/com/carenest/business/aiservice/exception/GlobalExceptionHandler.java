package com.carenest.business.aiservice.exception;

import com.carenest.business.common.response.ResponseDto;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiException.class)
    public ResponseEntity<ResponseDto<?>> handleAiException(AiException ex) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity
                .status(code.getStatus())
                .body(ResponseDto.error(code.getStatus().value(), code.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ResponseDto<?>> handleFeignException(FeignException ex) {
        return ResponseEntity
                .status(ErrorCode.REVIEW_SERVICE_ERROR.getStatus())
                .body(ResponseDto.error(
                        ErrorCode.REVIEW_SERVICE_ERROR.getStatus().value(),
                        ErrorCode.REVIEW_SERVICE_ERROR.getMessage()
                ));
    }
}
