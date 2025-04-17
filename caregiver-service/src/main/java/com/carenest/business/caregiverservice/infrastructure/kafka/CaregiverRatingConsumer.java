package com.carenest.business.caregiverservice.infrastructure.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.carenest.business.caregiverservice.application.service.CaregiverConsumerService;
import com.carenest.business.common.event.caregiver.CaregiverRatingMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaregiverRatingConsumer {

	private final CaregiverConsumerService caregiverConsumerService;

	@KafkaListener(
		topics = "review-rating-update",
		groupId = "review-service-group",
		containerFactory = "kafkaListenerConsumerContainerFactory"
	)
	public void handleCaregiverRatingUpdate(CaregiverRatingMessage message) {
		log.info("받은 메시지: {}", message);
		caregiverConsumerService.handleCaregiverRatingUpdate(message);
	}
}
