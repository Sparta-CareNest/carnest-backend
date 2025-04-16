package com.carenest.business.paymentservice.infrastructure.external.toss.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentConfirmRequest {
    private String orderId;
    private Long amount;
}