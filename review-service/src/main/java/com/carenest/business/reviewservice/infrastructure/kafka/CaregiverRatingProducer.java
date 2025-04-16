package com.carenest.business.reviewservice.infrastructure.kafka;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.carenest.business.common.event.caregiver.CaregiverRatingMessage;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaregiverRatingProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendReviewUpdateEvent(UUID caregiverId, Double rating){

        CaregiverRatingMessage message = CaregiverRatingMessage.builder()
            .caregiverId(caregiverId)
            .rating(rating)
            .build();


        sendKafkaMessage(
            KafkaTopic.REVIEW_RATING_UPDATE.getTopicName(),
            caregiverId,
            message,
            "점수 업데이트 이벤트"
        );
    }


    private <T> void sendKafkaMessage(String topic, UUID id, T payload, String eventType) {
        Assert.notNull(topic, "토픽은 null일 수 없습니다");
        Assert.notNull(id, "ID는 null일 수 없습니다");
        Assert.notNull(payload, "Payload는 null일 수 없습니다");

        String key = id.toString();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("{} 발행 성공: id={}", eventType, id);
                log.debug("메시지 발행 완료: topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                log.error("{} 발행 실패: id={}", eventType, id, ex);
            }
        });
    }
}
