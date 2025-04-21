package com.carenest.business.adminservice.application.dto.response;

import com.carenest.business.adminservice.domain.model.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SettlementResponseDto {
    private UUID id;
    private UUID careWorkerId;
    private BigDecimal amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate settledAt;
    private SettlementStatus status;
}
