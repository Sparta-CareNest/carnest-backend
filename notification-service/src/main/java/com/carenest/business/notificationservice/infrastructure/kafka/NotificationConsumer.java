package com.carenest.business.notificationservice.infrastructure.kafka;

import com.carenest.business.notificationservice.application.dto.kafka.PaymentCompletedEvent;
import com.carenest.business.notificationservice.application.dto.kafka.ReservationCreatedEvent;
import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.service.NotificationService;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationService notificationService;

    @KafkaListener(topics = "reservation-created", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleReservationCreated(ReservationCreatedEvent event) {
        log.info("예약 생성 알림 수신: {}", event);

        String content = "새 예약이 생성되었습니다. 예약 ID: " + event.getReservationId() +
                ", 사용자 ID: " + event.getUserId() + ", 예약 시간: " + event.getReservationTime();

        notificationService.createNotificationWithType(
                new NotificationCreateRequestDto(event.getUserId(), content),
                NotificationType.RESERVATION_CREATED
        );
    }

    @KafkaListener(topics = "payment-completed", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 알림 수신: {}", event);

        String content = "결제가 완료되었습니다. 결제 ID: " + event.getPaymentId() +
                ", 사용자 ID: " + event.getUserId() + ", 결제 금액: " + event.getAmount() +
                ", 결제 일시: " + event.getPaidAt();

        notificationService.createNotificationWithType(
                new NotificationCreateRequestDto(event.getUserId(), content),
                NotificationType.PAYMENT_SUCCESS
        );
    }
}
