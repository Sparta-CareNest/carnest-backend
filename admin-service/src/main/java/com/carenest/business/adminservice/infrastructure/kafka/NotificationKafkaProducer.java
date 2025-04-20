package com.carenest.business.adminservice.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 알림 메시지 전송
    public void sendNotification(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
