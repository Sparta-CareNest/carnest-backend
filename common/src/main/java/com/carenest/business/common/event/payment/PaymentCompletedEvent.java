package com.carenest.business.common.event.payment;

import com.carenest.business.common.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PaymentCompletedEvent extends BaseEvent {
    private UUID paymentId;
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private String approvalNumber;

    @Builder
    public PaymentCompletedEvent(UUID paymentId, UUID reservationId, UUID guardianId,
                                 UUID caregiverId, BigDecimal amount, String paymentMethod,
                                 String approvalNumber) {
        super("PAYMENT_COMPLETED");
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.guardianId = guardianId;
        this.caregiverId = caregiverId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.approvalNumber = approvalNumber;
    }
}