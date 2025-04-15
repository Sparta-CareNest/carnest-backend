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

        notificationService.createNotificationWithType(
                new NotificationCreateRequestDto(event.getUserId(), null),
                NotificationType.RESERVATION_CREATED
        );
    }

    @KafkaListener(topics = "payment-completed", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 알림 수신: {}", event);

        notificationService.createNotificationWithType(
                new NotificationCreateRequestDto(event.getUserId(), null),
                NotificationType.PAYMENT_SUCCESS
        );
    }
}
