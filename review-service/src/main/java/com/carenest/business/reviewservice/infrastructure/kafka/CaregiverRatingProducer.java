package com.carenest.business.reviewservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaregiverRatingProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRatingUpdateMessage(Object message) {
        kafkaTemplate.send(KafkaTopic.REVIEW_RATING_UPDATE.getTopicName(), message);
    }
}
