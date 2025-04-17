package com.carenest.business.notificationservice.infrastructure.kafka;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationCreatedEvent;
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
    public void handleReservationCreated(ReservationCreatedEvent event) {
        try {
            log.info("예약 생성 이벤트 수신: reservationId={}, guardianId={}",
                    event.getReservationId(), event.getGuardianId());

            String content = String.format(
                    "[예약 생성] 예약이 생성되었습니다.\n예약 ID: %s\n환자명: %s\n서비스 기간: %s ~ %s",
                    event.getReservationId(), event.getPatientName(),
                    event.getStartedAt(), event.getEndedAt()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.RESERVATION_CREATED
            );
        } catch (Exception e) {
            log.error("예약 생성 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "reservation-status-changed",
            groupId = "notification-group",
            containerFactory = "reservationStatusChangedKafkaListenerContainerFactory"
    )
    public void handleReservationStatusChanged(ReservationStatusChangedEvent event) {
        try {
            log.info("예약 상태 변경 수신: {}", event);

            String content = createStatusChangeContent(event);

            sendStatusChangeNotifications(event, content);

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

            String guardianContent = String.format(
                    "[결제 완료] 결제가 완료되었습니다.\n결제 ID: %s\n금액: %s원\n결제수단: %s\n승인번호: %s",
                    event.getPaymentId(), event.getAmount(), event.getPaymentMethod(), event.getApprovalNumber()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), guardianContent),
                    NotificationType.PAYMENT_SUCCESS
            );

            String caregiverContent = String.format(
                    "[새 예약 요청] 새로운 예약 요청이 있습니다.\n예약 ID: %s\n금액: %s원\n확인 후 수락 또는 거절해주세요.",
                    event.getReservationId(), event.getAmount()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), caregiverContent),
                    NotificationType.RESERVATION_STATUS_CHANGED
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
                    "[결제 취소] 예약 ID: %s에 대한 결제가 취소되었습니다.\n금액: %s원\n사유: %s",
                    event.getReservationId(), event.getAmount(), event.getCancelReason()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.PAYMENT_CANCELLED
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), content),
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
                    "[예약 취소] 예약 ID: %s가 취소되었습니다.\n금액: %s원\n사유: %s",
                    event.getReservationId(), event.getAmount(), event.getCancelReason()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.RESERVATION_CANCELLED
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), content),
                    NotificationType.RESERVATION_CANCELLED
            );
        } catch (Exception e) {
            log.error("예약 취소 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    private String createStatusChangeContent(ReservationStatusChangedEvent event) {
        String newStatus = event.getNewStatus();

        if ("PENDING_ACCEPTANCE".equals(newStatus)) {
            return String.format(
                    "[예약 상태 변경] 예약 ID: %s\n결제가 완료되어 간병인의 수락을 기다리고 있습니다.",
                    event.getReservationId()
            );
        } else if ("CONFIRMED".equals(newStatus)) {
            return String.format(
                    "[예약 확정] 예약 ID: %s\n간병인이 예약을 수락했습니다. 예약이 확정되었습니다.",
                    event.getReservationId()
            );
        } else if ("REJECTED".equals(newStatus)) {
            return String.format(
                    "[예약 거절] 예약 ID: %s\n간병인이 예약을 거절했습니다.\n사유: %s",
                    event.getReservationId(), event.getReason()
            );
        } else if ("COMPLETED".equals(newStatus)) {
            return String.format(
                    "[서비스 완료] 예약 ID: %s\n서비스가 완료되었습니다.",
                    event.getReservationId()
            );
        } else {
            return String.format(
                    "[예약 상태 변경] 예약 ID: %s\n상태: %s → %s\n사유: %s",
                    event.getReservationId(), event.getPreviousStatus(), newStatus, event.getReason()
            );
        }
    }

    private void sendStatusChangeNotifications(ReservationStatusChangedEvent event, String content) {
        notificationService.createNotificationWithType(
                new NotificationCreateRequestDto(event.getGuardianId(), content),
                NotificationType.RESERVATION_STATUS_CHANGED
        );

        notificationService.createNotificationWithType(
                new NotificationCreateRequestDto(event.getCaregiverId(), content),
                NotificationType.RESERVATION_STATUS_CHANGED
        );
    }
}