package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossSuccessCallbackResponse {
    private String paymentKey;
    private String orderId;
    private BigDecimal amount;
}