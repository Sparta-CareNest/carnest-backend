package com.carenest.business.common.event.reservation;

import com.carenest.business.common.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationStatusChangedEvent extends BaseEvent {
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private String previousStatus;
    private String newStatus;
    private String reason;

    @Builder
    public ReservationStatusChangedEvent(UUID reservationId, UUID guardianId, UUID caregiverId,
                                         String previousStatus, String newStatus, String reason) {
        super("RESERVATION_STATUS_CHANGED");
        this.reservationId = reservationId;
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }
}