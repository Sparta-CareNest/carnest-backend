package com.carenest.business.adminservice.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class SettlementRequestDto {
    @NotNull(message = "간병인 ID는 필수입니다.")
    private UUID careWorkerId;

    @NotNull(message = "정산 금액은 필수입니다.")
    private BigDecimal amount;

    @NotNull(message = "정산 시작일은 필수입니다.")
    private LocalDate periodStart;

    @NotNull(message = "정산 종료일은 필수입니다.")
    private LocalDate periodEnd;
}
