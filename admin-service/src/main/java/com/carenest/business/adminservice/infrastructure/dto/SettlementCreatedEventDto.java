package com.carenest.business.adminservice.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SettlementCreatedEventDto {
    private UUID settlementId;
    private UUID careWorkerId;
    private BigDecimal amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate settledAt;
}
