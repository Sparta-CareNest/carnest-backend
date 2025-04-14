package com.carenest.business.reservationservice.infrastructure.config;

import com.carenest.business.reservationservice.exception.IntegrationErrorException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignErrorDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            log.error("Feign client error: {} - {}", response.status(), methodKey);

            if (response.status() >= 500) {
                log.error("서버 오류 발생: 서비스={}, 상태코드={}", methodKey, response.status());
                return new IntegrationErrorException("외부 서비스 서버 오류가 발생했습니다");
            } else if (response.status() >= 400) {
                log.error("클라이언트 오류 발생: 서비스={}, 상태코드={}", methodKey, response.status());
                return new IntegrationErrorException("외부 서비스 요청 오류가 발생했습니다");
            }

            log.error("알 수 없는 오류 발생: 서비스={}, 상태코드={}", methodKey, response.status());
            return new IntegrationErrorException("외부 서비스 통신 중 오류가 발생했습니다");
        }
    }
}