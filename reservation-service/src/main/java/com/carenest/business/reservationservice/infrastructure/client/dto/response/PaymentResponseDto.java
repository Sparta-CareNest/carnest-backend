package com.carenest.business.reservationservice.infrastructure.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    // 기본 정보
    private UUID paymentId;
    private UUID reservationId;
    private UUID guardianId;
    private String guardianName;
    private UUID caregiverId;
    private String caregiverName;
    private String serviceName;
    private String patientName;

    // 서비스 정보
    private LocalDateTime serviceStartDate;
    private LocalDateTime serviceEndDate;

    // 결제 정보
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private Map<String, Object> paymentMethodDetail;
    private String paymentGateway;
    private String paymentKey;
    private String approvalNumber;
    private String pgTransactionId;
    private String receiptUrl;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    // 취소/환불 정보
    private String cancelReason;
    private BigDecimal refundAmount;
    private String refundBank;
    private String refundAccount;
    private String refundAccountOwner;
}