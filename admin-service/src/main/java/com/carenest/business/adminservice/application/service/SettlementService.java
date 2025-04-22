package com.carenest.business.adminservice.application.service;

import com.carenest.business.adminservice.application.dto.response.PaymentListResponse;
import com.carenest.business.adminservice.application.dto.response.SettlementResponseDto;
import com.carenest.business.adminservice.domain.model.Settlement;
import com.carenest.business.adminservice.domain.model.SettlementStatus;
import com.carenest.business.adminservice.infrastructure.client.PaymentClient;
import com.carenest.business.adminservice.infrastructure.kafka.SettlementKafkaProducer;
import com.carenest.business.adminservice.infrastructure.repository.SettlementRepository;
import com.carenest.business.adminservice.presentation.SettlementMapper;
import com.carenest.business.common.event.admin.SettlementCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final SettlementKafkaProducer settlementKafkaProducer;
    private final PaymentClient paymentClient;

    // 정산 생성
    @Transactional
    public SettlementResponseDto createSettlement(
            UUID careWorkerId,
            BigDecimal amount,
            LocalDate periodStart,
            LocalDate periodEnd) {
        Settlement settlement = Settlement.builder()
                .careWorkerId(careWorkerId)
                .amount(amount)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .settledAt(LocalDate.now()) // 정산 수행일은 현재 날짜
                .status(SettlementStatus.PENDING) // 정산 상태는 우선 PENDING으로 설정
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        // 이벤트 발해
        SettlementCreatedEvent event = new SettlementCreatedEvent(
                savedSettlement.getId(),
                savedSettlement.getCareWorkerId(),
                savedSettlement.getAmount(),
                savedSettlement.getPeriodStart(),
                savedSettlement.getPeriodEnd(),
                savedSettlement.getSettledAt()
        );

        settlementKafkaProducer.sendSettlementNotification("settlement-completion-notification", event);

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

    // Kafka 메시지를 받을 때 사용하는 메서드
    @Transactional
    public Settlement createSettlementFromKafka(
            UUID careWorkerId,
            BigDecimal amount,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate settledAt) {
        // Kafka에서 받은 데이터로 Settlement 생성 (여기서는 이벤트 발행 없이 DB만 저장)
        Settlement settlement = Settlement.builder()
                .careWorkerId(careWorkerId)
                .amount(amount)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .settledAt(settledAt)
                .status(SettlementStatus.PENDING)
                .build();

        return settlementRepository.save(settlement); // 이벤트 발행 없이 DB에만 저장
    }

    // 결제이력 정보로 정산 생성
    @Transactional
    public SettlementResponseDto createSettlementFromPaymentData(
            UUID careWorkerId, LocalDate periodStart, LocalDate periodEnd) {
        Pageable pageable = PageRequest.of(0, 10);

        // 결제 서비스에서 결제 내역 조회
        Page<PaymentListResponse> payments = paymentClient.getPayments(periodStart, periodEnd, pageable.getPageNumber(), pageable.getPageSize());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 결제 내역을 기반으로 금액 합산
        for (PaymentListResponse payment : payments) {
            if (payment.getStatus().equals("COMPLETED") && payment.getCareWorkerId().equals(careWorkerId)) {
                totalAmount = totalAmount.add(payment.getAmount());
            }
        }

        // 정산 생성
        Settlement settlement = Settlement.builder()
                .careWorkerId(careWorkerId)
                .amount(totalAmount)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .settledAt(LocalDate.now())  // 정산 수행일은 현재 날짜
                .status(SettlementStatus.PENDING)
                .build();

        // 정산 저장
        Settlement savedSettlement = settlementRepository.save(settlement);

        // Kafka 메시지 발송
        SettlementCreatedEvent event = new SettlementCreatedEvent(
                savedSettlement.getId(),
                savedSettlement.getCareWorkerId(),
                savedSettlement.getAmount(),
                savedSettlement.getPeriodStart(),
                savedSettlement.getPeriodEnd(),
                savedSettlement.getSettledAt()
        );

        settlementKafkaProducer.sendSettlementNotification("settlement-completion-notification", event);

        return settlementMapper.toDto(savedSettlement);
    }

}
