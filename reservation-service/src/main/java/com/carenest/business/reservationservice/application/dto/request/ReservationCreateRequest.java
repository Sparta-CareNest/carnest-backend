package com.carenest.business.reservationservice.application.dto.request;

import com.carenest.business.reservationservice.domain.model.Gender;
import com.carenest.business.reservationservice.domain.model.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationCreateRequest {
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
}