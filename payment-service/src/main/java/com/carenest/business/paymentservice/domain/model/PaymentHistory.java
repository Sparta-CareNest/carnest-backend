package com.carenest.business.paymentservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_payment_histories")
@Getter
@NoArgsConstructor
public class PaymentHistory {

    @Id
    @Column(name = "payment_history_id", nullable = false, updatable = false)
    private UUID paymentHistoryId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    @Column(name = "caregiver_id", nullable = false)
    private UUID caregiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PaymentHistory(Payment payment) {
        this.paymentHistoryId = UUID.randomUUID();
        this.paymentId = payment.getPaymentId();
        this.reservationId = payment.getReservationId();
        this.guardianId = payment.getGuardianId();
        this.caregiverId = payment.getCaregiverId();
        this.status = payment.getStatus();
        this.amount = payment.getAmount();
        this.createdAt = LocalDateTime.now();
    }
}