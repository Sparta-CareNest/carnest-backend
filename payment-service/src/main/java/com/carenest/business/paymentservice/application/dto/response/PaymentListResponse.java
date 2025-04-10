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
public class PaymentListResponse {
    private UUID paymentId;
    private UUID reservationId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String paymentGateway;
    private String caregiverName;
    private String servicePeriod;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public PaymentListResponse(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.reservationId = payment.getReservationId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentGateway = payment.getPaymentGateway();
        this.createdAt = payment.getCreatedAt();

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            this.completedAt = payment.getUpdatedAt();
        }

        // TODO: caregiverName과 servicePeriod는 서비스 연동 시 설정 구현하기
        this.caregiverName = "간병인 이름";
        this.servicePeriod = "서비스 기간";
    }
}