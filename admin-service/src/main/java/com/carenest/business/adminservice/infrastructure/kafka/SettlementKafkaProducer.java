package com.carenest.business.adminservice.infrastructure.kafka;

import com.carenest.business.common.event.admin.SettlementCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SettlementKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSettlementNotification(String topic, SettlementCreatedEvent event) {
        String message = String.format(
                "settlementId=%s, careWorkerId=%s, amount=%s, periodStart=%s, periodEnd=%s, settledAt=%s",
                event.getSettlementId(),
                event.getCareWorkerId(),
                event.getAmount(),
                event.getPeriodStart(),
                event.getPeriodEnd(),
                event.getSettledAt()
        );

        kafkaTemplate.send(topic, message);
        log.info("[Kafka] 메시지 전송 성공 - topic: {}, message: {}", topic, message);
    }
}
