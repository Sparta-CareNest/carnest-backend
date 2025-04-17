package com.carenest.business.common.event.reservation;

import com.carenest.business.common.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationCreatedEvent extends BaseEvent {
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private String patientName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String serviceType;
    private BigDecimal totalAmount;
    private BigDecimal serviceFee;

    @Builder
    public ReservationCreatedEvent(UUID reservationId, UUID guardianId, UUID caregiverId,
                                   String patientName, LocalDateTime startedAt, LocalDateTime endedAt,
                                   String serviceType, BigDecimal totalAmount, BigDecimal serviceFee) {
        super("RESERVATION_CREATED");
        this.reservationId = reservationId;
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.patientName = patientName;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.serviceType = serviceType;
        this.totalAmount = totalAmount;
        this.serviceFee = serviceFee;
    }
}