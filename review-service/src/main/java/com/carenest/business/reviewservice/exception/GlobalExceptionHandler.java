package com.carenest.business.reviewservice.exception;

import com.carenest.business.common.response.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ResponseDto<?>> handleReviewException(ReviewException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ResponseDto.error(ex.getStatus(), ex.getMessage()));
    }

}
