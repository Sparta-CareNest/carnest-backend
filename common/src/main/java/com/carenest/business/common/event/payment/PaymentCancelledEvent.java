package com.carenest.business.common.event.payment;

import com.carenest.business.common.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PaymentCancelledEvent extends BaseEvent {
    private UUID paymentId;
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private BigDecimal amount;
    private String cancelReason;

    @Builder
    public PaymentCancelledEvent(UUID paymentId, UUID reservationId, UUID guardianId,
                                 UUID caregiverId, BigDecimal amount, String cancelReason) {
        super("PAYMENT_CANCELLED");
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.amount = amount;
        this.cancelReason = cancelReason;
    }
}