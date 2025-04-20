package com.carenest.business.adminservice.presentation;

import com.carenest.business.adminservice.application.dto.response.SettlementResponseDto;
import com.carenest.business.adminservice.domain.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {
    // 엔티티 -> Response DTO 변환
    public SettlementResponseDto toDto(Settlement settlement) {
        return new SettlementResponseDto(
                settlement.getId(),
                settlement.getCareWorkerId(),
                settlement.getAmount(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getSettledAt(),
                settlement.getStatus()
        );
    }
}
