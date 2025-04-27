package com.carenest.business.reservationservice.domain.model;

import com.carenest.business.common.model.UserRole;
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

    @Column(name = "guardian_name", length = 50)
    private String guardianName;

    @Column(name = "caregiver_id", nullable = false)
    private UUID caregiverId;

    @Column(name = "caregiver_name", length = 50)
    private String caregiverName;

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

    @Column(name = "service_fee", precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "prev_status", length = 20)
    private ReservationStatus prevStatus;

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

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_by_role", length = 20)
    private String createdByRole;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    public ReservationHistory(Reservation reservation) {
        this.reservationHistoryId = UUID.randomUUID();
        this.reservationId = reservation.getReservationId();
        this.guardianId = reservation.getGuardianId();
        this.guardianName = reservation.getGuardianName();
        this.caregiverId = reservation.getCaregiverId();
        this.caregiverName = reservation.getCaregiverName();
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
        this.serviceFee = reservation.getServiceFee();
        this.status = reservation.getStatus();
        // prevStatus는 별도 설정 필요
        this.acceptedAt = reservation.getAcceptedAt();
        this.rejectedAt = reservation.getRejectedAt();
        this.completedAt = reservation.getCompletedAt();
        this.cancelReason = reservation.getCancelReason();
        this.rejectionReason = reservation.getRejectionReason();
        this.caregiverNote = reservation.getCaregiverNote();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = reservation.getUpdatedAt();
        this.paymentId = reservation.getPaymentId();
        this.paymentStatus = reservation.getPaymentStatus();

        // 상태 변경에 따른 설명 설정
        setDescriptionBasedOnStatus(reservation.getStatus());
    }

    public void setPrevStatus(ReservationStatus prevStatus) {
        this.prevStatus = prevStatus;
    }

    public void setCreatedBy(String createdBy, UserRole role) {
        this.createdBy = createdBy;
        this.createdByRole = role.getValue();
    }

    private void setDescriptionBasedOnStatus(ReservationStatus status) {
        switch (status) {
            case PENDING_PAYMENT:
                this.description = "예약이 생성되었습니다. 결제 대기 중입니다.";
                break;
            case PENDING_ACCEPTANCE:
                this.description = "결제가 완료되었습니다. 간병인의 수락을 기다리고 있습니다.";
                break;
            case CONFIRMED:
                this.description = "간병인이 예약을 수락했습니다.";
                break;
            case COMPLETED:
                this.description = "서비스가 완료되었습니다.";
                break;
            case CANCELLED:
                this.description = "예약이 취소되었습니다.";
                break;
            case REJECTED:
                this.description = "간병인이 예약을 거절했습니다.";
                break;
            default:
                this.description = "예약 상태가 변경되었습니다.";
        }
    }
}