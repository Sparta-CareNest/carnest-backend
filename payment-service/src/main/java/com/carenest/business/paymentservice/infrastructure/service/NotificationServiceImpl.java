package com.carenest.business.paymentservice.infrastructure.service;

import com.carenest.business.common.event.notification.NotificationEvent;
import com.carenest.business.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendPaymentSuccessNotification(Payment payment) {
        try {
            log.info("결제 성공 알림 발행: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            // 보호자에게 알림 전송
            sendNotificationEvent(
                    payment.getGuardianId(),
                    "PAYMENT_SUCCESS",
                    String.format("결제가 완료되었습니다. 금액: %s원", payment.getAmount())
            );

            // 간병인에게도 알림
            sendNotificationEvent(
                    payment.getCaregiverId(),
                    "PAYMENT_SUCCESS",
                    "새로운 예약의 결제가 완료되었습니다. 확인해주세요."
            );

            log.info("결제 성공 알림 이벤트 발행 완료");
        } catch (Exception e) {
            log.error("결제 성공 알림 이벤트 발행 실패", e);
        }
    }

    @Override
    public void sendPaymentCancelNotification(Payment payment) {
        try {
            log.info("결제 취소 알림 발행: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            // 보호자에게 알림
            sendNotificationEvent(
                    payment.getGuardianId(),
                    "PAYMENT_CANCELED",
                    String.format("결제가 취소되었습니다. 금액: %s원, 사유: %s",
                            payment.getAmount(), payment.getCancelReason())
            );

            // 간병인에게 알림
            sendNotificationEvent(
                    payment.getCaregiverId(),
                    "PAYMENT_CANCELED",
                    "예약이 취소되었습니다."
            );

            log.info("결제 취소 알림 이벤트 발행 완료");
        } catch (Exception e) {
            log.error("결제 취소 알림 이벤트 발행 실패", e);
        }
    }

    @Override
    public void sendPaymentRefundNotification(Payment payment) {
        try {
            log.info("결제 환불 알림 발행: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            sendNotificationEvent(
                    payment.getGuardianId(),
                    "PAYMENT_REFUNDED",
                    String.format("결제가 환불되었습니다. 금액: %s원",
                            payment.getRefundAmount())
            );

            log.info("결제 환불 알림 이벤트 발행 완료");
        } catch (Exception e) {
            log.error("결제 환불 알림 이벤트 발행 실패", e);
        }
    }

    private void sendNotificationEvent(UUID receiverId, String notificationType, String content) {
        NotificationEvent event = NotificationEvent.builder()
                .receiverId(receiverId)
                .notificationType(notificationType)
                .content(content)
                .build();

        String key = receiverId.toString();
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                "notification-event", key, event);

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