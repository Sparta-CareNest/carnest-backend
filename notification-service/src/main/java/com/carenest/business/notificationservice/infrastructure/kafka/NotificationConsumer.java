package com.carenest.business.notificationservice.infrastructure.kafka;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationStatusChangedEvent;
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

    @KafkaListener(
            topics = "reservation-created",
            groupId = "notification-group",
            containerFactory = "reservationStatusChangedKafkaListenerContainerFactory"
    )
    public void handleReservationStatusChanged(ReservationStatusChangedEvent event) {
        try {
            log.info("예약 상태 변경 수신: {}", event);

            String content = String.format(
                    "[예약 상태 변경] 예약 ID: %s\n상태: %s → %s\n사유: %s",
                    event.getReservationId(), event.getPreviousStatus(), event.getNewStatus(), event.getReason()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.RESERVATION_STATUS_CHANGED
            );
        } catch (Exception e) {
            log.error("예약 상태 변경 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "payment-completed",
            groupId = "notification-group",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("결제 완료 수신: {}", event);

            String content = String.format(
                    "[결제 완료] 결제 ID: %s\n금액: %s원\n결제수단: %s\n승인번호: %s",
                    event.getPaymentId(), event.getAmount(), event.getPaymentMethod(), event.getApprovalNumber()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.PAYMENT_SUCCESS
            );
        } catch (Exception e) {
            log.error("결제 완료 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "payment-cancelled",
            groupId = "notification-group",
            containerFactory = "paymentCancelledKafkaListenerContainerFactory"
    )
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        try {
            log.info("결제 취소 수신: {}", event);

            String content = String.format(
                    "[결제 취소] 예약 ID: %s\n금액: %s원\n사유: %s",
                    event.getReservationId(), event.getAmount(), event.getCancelReason()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.PAYMENT_CANCELLED
            );
        } catch (Exception e) {
            log.error("결제 취소 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "reservation-cancelled",
            groupId = "notification-group",
            containerFactory = "reservationCancelledKafkaListenerContainerFactory"
    )
    public void handleReservationCancelled(ReservationCancelledEvent event) {
        try {
            log.info("예약 취소 수신: {}", event);

            String content = String.format(
                    "[예약 취소] 예약 ID: %s\n금액: %s원\n사유: %s",
                    event.getReservationId(), event.getAmount(), event.getCancelReason()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.RESERVATION_CANCELLED
            );
        } catch (Exception e) {
            log.error("예약 취소 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
