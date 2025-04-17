package com.carenest.business.paymentservice.application.dto.response;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaymentHistoryDetailResponse {
    private UUID paymentId;
    private UUID lastHistoryId;
    private UUID reservationId;
    private String caregiverName;
    private String servicePeriod;
    private PaymentStatus status;
    private PaymentStatus originalStatus;
    private BigDecimal amount;
    private String paymentMethod;
    private int changeCount;
    private LocalDateTime firstCreatedAt;
    private LocalDateTime lastUpdatedAt;

    public PaymentHistoryDetailResponse(Payment payment, int historyCount, LocalDateTime firstCreatedAt, LocalDateTime lastUpdatedAt) {
        this.paymentId = payment.getPaymentId();
        this.reservationId = payment.getReservationId();
        this.status = payment.getStatus();
        this.originalStatus = PaymentStatus.PENDING;
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.changeCount = historyCount;
        this.firstCreatedAt = firstCreatedAt;
        this.lastUpdatedAt = lastUpdatedAt;

        this.caregiverName = "간병인 정보 로딩 중";
        this.servicePeriod = "서비스 기간 정보 로딩 중";
    }
}