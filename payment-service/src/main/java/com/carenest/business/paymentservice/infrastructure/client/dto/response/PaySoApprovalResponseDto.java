package com.carenest.business.paymentservice.infrastructure.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaySoApprovalResponseDto {
    private String paymentKey;
    private String status;
    private String approvalNumber;
    private String pgTransactionId;
    private String receiptUrl;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime approvedAt;
    private String message;
}