package com.carenest.business.adminservice.application.service;

import com.carenest.business.adminservice.application.dto.response.SettlementResponseDto;
import com.carenest.business.adminservice.domain.Settlement;
import com.carenest.business.adminservice.domain.SettlementStatus;
import com.carenest.business.adminservice.infrastructure.SettlementRepository;
import com.carenest.business.adminservice.presentation.SettlementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final SettlementMapper settlementMapper;

    // 정산 생성
    @Transactional
    public SettlementResponseDto createSettlement(UUID careWorkerId, BigDecimal amount, LocalDate periodStart, LocalDate periodEnd) {
        Settlement settlement = Settlement.builder()
                .careWorkerId(careWorkerId)
                .amount(amount)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .settledAt(LocalDate.now()) // 정산 수행일은 현재 날짜
                .status(SettlementStatus.PENDING) // 정산 상태는 우선 PENDING으로 설정
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        return settlementMapper.toDto(savedSettlement);
    }

    // 특정 간병인의 정산 내역 조회
    @Transactional(readOnly = true)
    public List<SettlementResponseDto> getSettlementsByCareWorkerId(UUID careWorkerId) {
        List<Settlement> settlements = settlementRepository.findByCareWorkerId(careWorkerId);
        return settlements.stream()
                .map(settlementMapper::toDto)
                .toList();
    }
}
