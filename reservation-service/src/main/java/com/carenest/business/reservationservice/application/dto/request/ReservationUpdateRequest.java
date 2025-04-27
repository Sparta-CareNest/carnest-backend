package com.carenest.business.reservationservice.application.dto.request;

import com.carenest.business.reservationservice.domain.model.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationUpdateRequest {
    private String patientName;
    private Integer patientAge;
    private Gender patientGender;
    private String patientCondition;
    private String careAddress;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String serviceRequests;
}