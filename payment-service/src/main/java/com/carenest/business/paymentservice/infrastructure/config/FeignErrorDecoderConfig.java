package com.carenest.business.paymentservice.infrastructure.config;

import com.carenest.business.paymentservice.exception.PaymentErrorCode;
import com.carenest.business.paymentservice.exception.PaymentException;
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
                log.error("PaySo 서버 오류 발생: 메서드={}, 상태코드={}", methodKey, response.status());
                return new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            } else if (response.status() >= 400) {
                log.error("PaySo 클라이언트 오류 발생: 메서드={}, 상태코드={}", methodKey, response.status());
                return new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            log.error("알 수 없는 오류 발생: 메서드={}, 상태코드={}", methodKey, response.status());
            return new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }
}