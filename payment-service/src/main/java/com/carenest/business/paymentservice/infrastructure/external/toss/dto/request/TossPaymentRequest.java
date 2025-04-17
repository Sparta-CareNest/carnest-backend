package com.carenest.business.paymentservice.infrastructure.external.toss.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentRequest {
    private String amount;
    private String orderId;
    private String orderName;
    private String successUrl;
    private String failUrl;
    private String customerName;
    private String customerEmail;
    private String method;
}