package com.carenest.business.caregiverservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

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
				return new CaregiverException(ErrorCode.EXTERNAL_API_ERROR);
			} else if (response.status() >= 400) {
				log.error("클라이언트 오류 발생: 서비스={}, 상태코드={}", methodKey, response.status());
				return new CaregiverException(ErrorCode.EXTERNAL_API_ERROR);
			}

			log.error("알 수 없는 오류 발생: 서비스={}, 상태코드={}", methodKey, response.status());
			return new CaregiverException(ErrorCode.EXTERNAL_API_ERROR);
		}
	}
}