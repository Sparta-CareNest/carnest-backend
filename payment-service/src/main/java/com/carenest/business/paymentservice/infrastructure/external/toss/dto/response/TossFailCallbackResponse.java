package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossFailCallbackResponse {
    private String code;
    private String message;
    private String orderId;
}