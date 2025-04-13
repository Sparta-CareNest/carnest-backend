package com.carenest.business.paymentservice.infrastructure.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaySoCancelResponseDto {
    private String paymentKey;
    private String status;
    private BigDecimal cancelAmount;
    private String cancelReason;
    private LocalDateTime canceledAt;
    private String message;
}