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
public class PaySoApprovalRequestDto {
    private String paymentKey;
    private BigDecimal amount;
    private String merchantId;
}