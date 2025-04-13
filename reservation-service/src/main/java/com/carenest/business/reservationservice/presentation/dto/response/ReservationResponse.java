package com.carenest.business.reservationservice.presentation.dto.response;

import com.carenest.business.reservationservice.domain.model.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
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
}