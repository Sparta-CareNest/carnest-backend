package com.carenest.business.reservationservice.application.dto.response;

import com.carenest.business.reservationservice.domain.model.Gender;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.model.ServiceType;
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
    private UUID caregiverId;
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
    private ReservationStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String cancelReason;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID paymentId;

    public ReservationResponse(Reservation reservation) {
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
        this.createdAt = reservation.getCreatedAt();
        this.updatedAt = reservation.getUpdatedAt();
        this.paymentId = reservation.getPaymentId();
    }
}