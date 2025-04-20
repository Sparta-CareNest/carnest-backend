package com.carenest.business.adminservice.infrastructure.kafka;

import com.carenest.business.adminservice.application.service.SettlementService;
import com.carenest.business.adminservice.infrastructure.dto.SettlementCreatedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementKafkaConsumer { // 결제 서비스와 간병인 서비스에서 보낸 메시지를 처리

    private final SettlementService settlementService;
    private final NotificationKafkaProducer notificationKafkaProducer;

    @KafkaListener(topics = "settlement-created", groupId = "admin-service-consumer", containerFactory = "kafkaListenerContainerFactory")
    public void listenSettlementCreatedEvent(SettlementCreatedEventDto event) {
        try {
            log.info("Received Settlement: {}", event);

            // 정산 처리
            settlementService.createSettlementFromKafka(
                    event.getCareWorkerId(),
                    event.getAmount(),
                    event.getPeriodStart(),
                    event.getPeriodEnd(),
                    event.getSettledAt()
            );

            // 정산 완료 알림 메시지 구성
            String notificationMessage = String.format(
                    "정산이 완료되었습니다. 간병인 ID: %s, 금액: %s, 기간: %s ~ %s",
                    event.getCareWorkerId(),
                    event.getAmount(),
                    event.getPeriodStart(),
                    event.getPeriodEnd()
            );

            // 알림 서비스에 알림 메시지 전송
            notificationKafkaProducer.sendNotification("settlement-completion-notification", notificationMessage);

        } catch (Exception e) {
            log.error("Kafka 메시지 파싱 실패", e);
        }
    }
}
