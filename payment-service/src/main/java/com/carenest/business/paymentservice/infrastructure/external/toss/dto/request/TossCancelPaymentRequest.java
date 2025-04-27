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
public class TossCancelPaymentRequest {
    private String cancelReason;
    private Long cancelAmount;
    private Long taxFreeAmount;
    private Long taxExemptionAmount;
    private Long refundableAmount;
    private String refundReceiveAccount;
}