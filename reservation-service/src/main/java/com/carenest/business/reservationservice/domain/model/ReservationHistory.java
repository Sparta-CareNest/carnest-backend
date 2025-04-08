package com.carenest.business.reservationservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_reservation_history")
@Getter
@NoArgsConstructor
public class ReservationHistory {

    @Id
    @Column(name = "reservation_history_id", nullable = false, updatable = false)
    private UUID reservationHistoryId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    @Column(name = "caregiver_id", nullable = false)
    private UUID caregiverId;

    @Column(name = "patient_name", nullable = false, length = 50)
    private String patientName;

    @Column(name = "patient_age", nullable = false)
    private Integer patientAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "patient_gender", nullable = false, length = 10)
    private Gender patientGender;

    @Column(name = "patient_condition", nullable = false, length = 255)
    private String patientCondition;

    @Column(name = "care_address", nullable = false, length = 255)
    private String careAddress;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 20)
    private ServiceType serviceType;

    @Column(name = "service_requests", nullable = false, length = 255)
    private String serviceRequests;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
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

    public ReservationHistory(Reservation reservation) {
        this.reservationHistoryId = UUID.randomUUID();
        this.reservationId = reservation.getReservationId();
        this.guardianId = reservation.getGuardianId();
        this.caregiverId = reservation.getCaregiverId();
        this.patientName = reservation.getPatientName();
        this.patientAge = reservation.getPatientAge();
        this.patientGender = reservation.getPatientGender();
        this.patientCondition = reservation.getPatientCondition();
        this.careAddress = reservation.getCareAddress();
        this.startedAt = reservation.getStartedAt();
        this.endedAt = reservation.getEndedAt();
        this.serviceType = reservation.getServiceType();
        this.serviceRequests = reservation.getServiceRequests();
        this.totalAmount = reservation.getTotalAmount();
        this.status = reservation.getStatus();
        this.acceptedAt = reservation.getAcceptedAt();
        this.rejectedAt = reservation.getRejectedAt();
        this.cancelReason = reservation.getCancelReason();
        this.rejectionReason = reservation.getRejectionReason();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = reservation.getUpdatedAt();
    }
}