package com.carenest.business.reservationservice.infrastructure.exception;

import com.carenest.business.reservationservice.exception.IntegrationErrorException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FeignClientExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public void handleFeignException(FeignException e) {
        log.error("Feign Client 호출 중 오류 발생: {}", e.getMessage());
        log.error("호출 URL: {}", e.request().url());
        log.error("상태 코드: {}", e.status());

        throw new IntegrationErrorException(
                String.format("외부 서비스 연동 중 오류가 발생했습니다. 상태 코드: %d", e.status())
        );
    }
}