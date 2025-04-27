package com.carenest.business.reservationservice.application.dto.request;

import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationSearchRequest {
    private UUID guardianId;
    private UUID caregiverId;
    private String patientName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private ReservationStatus status;
}