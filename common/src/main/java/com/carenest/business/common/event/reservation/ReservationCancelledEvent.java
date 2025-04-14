package com.carenest.business.common.event.reservation;

import com.carenest.business.common.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationCancelledEvent extends BaseEvent {
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private UUID paymentId;
    private BigDecimal amount;
    private String cancelReason;

    @Builder
    public ReservationCancelledEvent(UUID reservationId, UUID guardianId, UUID caregiverId,
                                     UUID paymentId, BigDecimal amount, String cancelReason) {
        super("RESERVATION_CANCELLED");
        this.reservationId = reservationId;
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.cancelReason = cancelReason;
    }
}