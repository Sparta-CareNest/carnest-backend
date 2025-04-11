package com.carenest.business.reservationservice.application.dto.response;

import com.carenest.business.reservationservice.domain.model.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationResponse {
    private UUID reservationId;
    private UUID guardianId;
    private String guardianName;
    private UUID caregiverId;
    private String caregiverName;
    private String patientName;
    private Integer patientAge;
    private Gender patientGender;
    private String patientCondition;
    private String careAddress;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private ServiceType serviceType;
    private String serviceRequests;
    private BigDecimal totalAmount;
    private BigDecimal serviceFee;
    private ReservationStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
    private String cancelReason;
    private String rejectionReason;
    private String caregiverNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID paymentId;
    private PaymentStatus paymentStatus;

    public ReservationResponse(Reservation reservation) {
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
        this.acceptedAt = reservation.getAcceptedAt();
        this.rejectedAt = reservation.getRejectedAt();
        this.completedAt = reservation.getCompletedAt();
        this.cancelReason = reservation.getCancelReason();
        this.rejectionReason = reservation.getRejectionReason();
        this.caregiverNote = reservation.getCaregiverNote();
        this.createdAt = reservation.getCreatedAt();
        this.updatedAt = reservation.getUpdatedAt();
        this.paymentId = reservation.getPaymentId();
        this.paymentStatus = reservation.getPaymentStatus();
    }
}