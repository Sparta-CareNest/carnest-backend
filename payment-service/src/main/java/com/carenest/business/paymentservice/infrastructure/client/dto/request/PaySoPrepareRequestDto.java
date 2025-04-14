package com.carenest.business.paymentservice.infrastructure.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySoPrepareRequestDto {
    private String merchantId;
    private String orderReference;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String successUrl;
    private String failureUrl;
    private String description;
}