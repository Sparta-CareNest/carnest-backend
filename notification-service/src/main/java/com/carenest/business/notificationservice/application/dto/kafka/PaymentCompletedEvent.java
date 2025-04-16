package com.carenest.business.notificationservice.application.dto.kafka;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class PaymentCompletedEvent {
    private UUID userId;
    private UUID paymentId;
    private int amount;
    private String paidAt;
}
