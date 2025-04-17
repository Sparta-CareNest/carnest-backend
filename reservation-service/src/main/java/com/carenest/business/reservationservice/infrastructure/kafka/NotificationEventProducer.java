package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotificationEvent(UUID receiverId, String notificationType, String content) {
        Assert.notNull(receiverId, "Receiver ID는 null일 수 없습니다");
        Assert.notNull(notificationType, "알림 타입은 null일 수 없습니다");
        Assert.notNull(content, "알림 내용은 null일 수 없습니다");

        NotificationEvent event = NotificationEvent.builder()
                .receiverId(receiverId)
                .notificationType(notificationType)
                .content(content)
                .build();

        String key = receiverId.toString();
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopic.NOTIFICATION_EVENT.getTopicName(),
                key,
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("알림 이벤트 발행 성공: receiverId={}, type={}", receiverId, notificationType);
                log.debug("메시지 발행 완료: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("알림 이벤트 발행 실패: receiverId={}, type={}", receiverId, notificationType, ex);
            }
        });
    }
}