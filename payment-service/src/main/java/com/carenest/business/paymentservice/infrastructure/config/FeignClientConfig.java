package com.carenest.business.paymentservice.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.carenest.business.paymentservice.infrastructure.client")
public class FeignClientConfig {
}