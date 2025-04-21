package com.carenest.business.caregiverservice.infrastructure.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.carenest.business.caregiverservice.application.service.CaregiverApprovalService;
import com.carenest.business.caregiverservice.application.service.CaregiverConsumerService;
import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;
import com.carenest.business.common.event.caregiver.CaregiverRatingEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaregiverConsumer {

	private final CaregiverConsumerService caregiverConsumerService;
	private final CaregiverApprovalService caregiverApprovalService;

	@KafkaListener(
		topics = "review-rating-update",
		groupId = "caregiver-service-group",
		containerFactory = "kafkaListenerConsumerContainerFactory"
	)
	public void handleCaregiverRatingUpdate(CaregiverRatingEvent message) {
		log.info("간병인 평점 업데이트 이벤트 수신: {}", message);
		caregiverConsumerService.handleCaregiverRatingUpdate(message);
	}


	@KafkaListener(
		topics = "caregiver-accept",
		groupId = "caregiver-service-group",
		containerFactory = "caregiverPendingKafkaListenerContainerFactory"
	)
	public void handleCaregiverAccept(CaregiverPendingEvent event){
		try {
			log.info("간병인 승인 요청 이벤트 수신: {}", event);
			caregiverApprovalService.createCaregiverApproval(event);
		} catch (Exception ex) {
			log.error("CaregiverPendingEvent 처리 실패: {}", event, ex);
			throw ex;
		}
	}


}
