package com.carenest.business.paymentservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "toss.payments")
public class TossPaymentsConfig {
    private String successUrl;
    private String failUrl;
    private String clientKey;
    private String secretKey;
    private String apiVersion;
    private String apiBaseUrl = "https://api.tosspayments.com/v1";
}