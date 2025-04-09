package com.carenest.business.paymentservice.application.dto.response;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponse {
    private UUID paymentId;
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String pgTransactionId;
    private String approvalNumber;
    private String receiptUrl;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private BigDecimal refundAmount;
    private LocalDateTime refundedAt;
    private String paymentGateway;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.reservationId = payment.getReservationId();
        this.guardianId = payment.getGuardianId();
        this.caregiverId = payment.getCaregiverId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.pgTransactionId = payment.getPgTransactionId();
        this.approvalNumber = payment.getApprovalNumber();
        this.receiptUrl = payment.getReceiptUrl();
        this.createdAt = payment.getCreatedAt();
        this.cancelledAt = payment.getCancelledAt();
        this.cancelReason = payment.getCancelReason();
        this.refundAmount = payment.getRefundAmount();
        this.refundedAt = payment.getRefundedAt();
        this.paymentGateway = payment.getPaymentGateway();
    }
}