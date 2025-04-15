package com.carenest.business.reviewservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaregiverRatingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "caregiver-rating-updated";

    public void sendRatingUpdateMessage(String caregiverId) {
        kafkaTemplate.send(TOPIC, caregiverId);
    }
}
