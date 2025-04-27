package com.carenest.business.common.event.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SettlementCreatedEvent {
    private UUID settlementId;
    private UUID careWorkerId;
    private BigDecimal amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate settledAt;
}
