package com.carenest.business.notificationservice.application.dto.kafka;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationCreatedEvent {
    private UUID userId;
    private UUID reservationId;
    private String reservationTime;
}
