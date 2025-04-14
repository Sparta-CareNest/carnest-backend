package com.carenest.business.paymentservice.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySoCancelRequestDto {
    private String paymentKey;
    private BigDecimal cancelAmount;
    private String cancelReason;
    private String merchantId;
}