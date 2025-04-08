package com.carenest.business.paymentservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    @Column(name = "caregiver_id", nullable = false)
    private UUID caregiverId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 20, nullable = false)
    private String paymentMethod;

    @Column(name = "payment_method_detail", columnDefinition = "json")
    private String paymentMethodDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    @Column(name = "approval_number", length = 50)
    private String approvalNumber;

    @Column(name = "receipt_url", length = 200)
    private String receiptUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_bank", length = 50)
    private String refundBank;

    @Column(name = "refund_account", length = 50)
    private String refundAccount;

    @Column(name = "refund_account_owner", length = 50)
    private String refundAccountOwner;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "payment_gateway", length = 50, nullable = false)
    private String paymentGateway;

    @Column(name = "payment_key", length = 100)
    private String paymentKey;

    public Payment(UUID reservationId, UUID guardianId, UUID caregiverId, BigDecimal amount,
                   String paymentMethod, String paymentMethodDetail, String paymentGateway) {
        this.paymentId = UUID.randomUUID();
        this.reservationId = reservationId;
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentMethodDetail = paymentMethodDetail;
        this.paymentGateway = paymentGateway;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void completePayment(String approvalNumber, String pgTransactionId, String receiptUrl, String paymentKey) {
        this.status = PaymentStatus.COMPLETED;
        this.approvalNumber = approvalNumber;
        this.pgTransactionId = pgTransactionId;
        this.receiptUrl = receiptUrl;
        this.paymentKey = paymentKey;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelPayment(String cancelReason) {
        this.status = PaymentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void processRefund(BigDecimal refundAmount, String bank, String account, String owner) {
        this.status = PaymentStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundBank = bank;
        this.refundAccount = account;
        this.refundAccountOwner = owner;
        this.refundedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}