package com.carenest.business.reservationservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_reservations")
@Getter
@NoArgsConstructor
public class Reservation {

    @Id
    @Column(name = "reservation_id", nullable = false, updatable = false)
    private UUID reservationId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    @Column(name = "caregiver_id", nullable = false)
    private UUID caregiverId;

    @Column(name = "patient_name", length = 50, nullable = false)
    private String patientName;

    @Column(name = "patient_age", nullable = false)
    private Integer patientAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "patient_gender", nullable = false, length = 10)
    private Gender patientGender;

    @Column(name = "patient_condition", length = 255, nullable = false)
    private String patientCondition;

    @Column(name = "care_address", length = 255, nullable = false)
    private String careAddress;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 20, nullable = false)
    private ServiceType serviceType;

    @Column(name = "service_requests", length = 255, nullable = false)
    private String serviceRequests;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReservationStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "payment_id")
    private UUID paymentId;

    public Reservation(UUID guardianId, UUID caregiverId, String patientName, Integer patientAge,
                       Gender patientGender, String patientCondition, String careAddress,
                       LocalDateTime startedAt, LocalDateTime endedAt, ServiceType serviceType,
                       String serviceRequests, BigDecimal totalAmount, UUID paymentId) {
        this.reservationId = UUID.randomUUID();
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientCondition = patientCondition;
        this.careAddress = careAddress;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.serviceType = serviceType;
        this.serviceRequests = serviceRequests;
        this.totalAmount = totalAmount;
        this.status = ReservationStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
        this.paymentId = null;
    }

    public void linkPayment(UUID paymentId) {
        this.paymentId = paymentId;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAcceptable() {
        return this.status == ReservationStatus.PENDING_ACCEPTANCE;
    }

    public void acceptByCaregiver() {
        this.status = ReservationStatus.CONFIRMED;
        this.acceptedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void rejectByCaregiver(String rejectionReason) {
        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.rejectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelByGuardian(String cancelReason) {
        this.status = ReservationStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeService() {
        this.status = ReservationStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
}