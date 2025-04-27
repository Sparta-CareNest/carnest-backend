package com.carenest.business.adminservice.exception;

import com.carenest.business.common.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SettlementExceptionHandler {
    // SettlementAccessDeniedException 처리
    @ExceptionHandler(SettlementAccessDeniedException.class)
    public ResponseEntity<ResponseDto<Void>> handleSettlementAccessDenied(SettlementAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseDto.error(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }
}
