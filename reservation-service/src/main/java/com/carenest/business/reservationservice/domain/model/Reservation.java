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

    @Column(name = "guardian_name", length = 50)
    private String guardianName;

    @Column(name = "caregiver_id", nullable = false)
    private UUID caregiverId;

    @Column(name = "caregiver_name", length = 50)
    private String caregiverName;

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

    @Column(name = "service_fee", precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReservationStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "caregiver_note", length = 255)
    private String caregiverNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    public Reservation(UUID guardianId, String guardianName, UUID caregiverId, String caregiverName,
                       String patientName, Integer patientAge, Gender patientGender,
                       String patientCondition, String careAddress, LocalDateTime startedAt,
                       LocalDateTime endedAt, ServiceType serviceType, String serviceRequests,
                       BigDecimal totalAmount, BigDecimal serviceFee) {
        this.reservationId = UUID.randomUUID();
        this.guardianId = guardianId;
        this.guardianName = guardianName;
        this.caregiverId = caregiverId;
        this.caregiverName = caregiverName;
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
        this.serviceFee = serviceFee;
        this.status = ReservationStatus.PENDING_PAYMENT;
        this.paymentStatus = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.paymentId = null;
    }

    public void linkPayment(UUID paymentId) {
        this.paymentId = paymentId;
        this.paymentStatus = PaymentStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAcceptable() {
        return this.status == ReservationStatus.PENDING_ACCEPTANCE;
    }

    public void acceptByCaregiver(String caregiverNote) {
        this.status = ReservationStatus.CONFIRMED;
        this.caregiverNote = caregiverNote;
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
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePatientName(String patientName) {
        this.patientName = patientName;
    }

    public void updatePatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public void updatePatientGender(Gender patientGender) {
        this.patientGender = patientGender;
    }

    public void updatePatientCondition(String patientCondition) {
        this.patientCondition = patientCondition;
    }

    public void updateCareAddress(String careAddress) {
        this.careAddress = careAddress;
    }

    public void updateServicePeriod(LocalDateTime startedAt, LocalDateTime endedAt) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public void updateServiceRequests(String serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public void updateServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void changeStatusToPendingAcceptance() {
        this.status = ReservationStatus.PENDING_ACCEPTANCE;
    }
}