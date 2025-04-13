package com.carenest.business.reservationservice.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.carenest.business.reservationservice.infrastructure.client")
public class FeignClientConfig {
}