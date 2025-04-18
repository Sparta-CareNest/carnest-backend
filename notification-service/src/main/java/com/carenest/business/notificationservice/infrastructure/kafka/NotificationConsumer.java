package com.carenest.business.notificationservice.infrastructure.kafka;

import com.carenest.business.common.event.notification.NotificationEvent;
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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "reservation-created",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
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

            log.info("예약 생성 알림 전송 완료: guardianId={}", event.getGuardianId());
        } catch (Exception e) {
            log.error("예약 생성 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "reservation-status-changed",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReservationStatusChanged(ConsumerRecord<String, Object> record) {
        try {
            log.info("예약 상태 변경 이벤트 수신: {}", record.value());

            Map<String, Object> data = (Map<String, Object>) record.value();

            String reservationId = data.get("reservationId").toString();
            String previousStatus = data.get("previousStatus").toString();
            String newStatus = data.get("newStatus").toString();
            String reason = data.get("reason") != null ? data.get("reason").toString() : null;

            UUID guardianId = UUID.fromString(data.get("guardianId").toString());
            UUID caregiverId = UUID.fromString(data.get("caregiverId").toString());

            String content = createStatusChangeContent(reservationId, previousStatus, newStatus, reason);

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(guardianId, content),
                    NotificationType.RESERVATION_STATUS_CHANGED
            );

            log.info("보호자 상태 변경 알림 전송 완료: guardianId={}", guardianId);

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(caregiverId, content),
                    NotificationType.RESERVATION_STATUS_CHANGED
            );

            log.info("간병인 상태 변경 알림 전송 완료: caregiverId={}", caregiverId);
        } catch (Exception e) {
            log.error("예약 상태 변경 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "notification-event",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("일반 알림 이벤트 수신: receiverId={}, type={}", event.getReceiverId(), event.getNotificationType());

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getReceiverId(), event.getContent()),
                    NotificationType.valueOf(event.getNotificationType()) // ENUM 매핑 주의
            );

            log.info("일반 알림 저장 완료: receiverId={}", event.getReceiverId());
        } catch (Exception e) {
            log.error("일반 알림 처리 실패: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "payment-completed",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("결제 완료 이벤트 수신: paymentId={}, reservationId={}",
                    event.getPaymentId(), event.getReservationId());

            String guardianContent = String.format(
                    "[결제 완료] 결제가 완료되었습니다.\n결제 ID: %s\n예약 ID: %s\n금액: %s원\n결제수단: %s",
                    event.getPaymentId(), event.getReservationId(),
                    event.getAmount(), event.getPaymentMethod()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), guardianContent),
                    NotificationType.PAYMENT_SUCCESS
            );

            log.info("보호자 결제 완료 알림 전송 완료: guardianId={}", event.getGuardianId());

            String caregiverContent = String.format(
                    "[새 예약 요청] 새로운 예약 요청이 있습니다.\n예약 ID: %s\n금액: %s원\n확인 후 수락 또는 거절해주세요.",
                    event.getReservationId(), event.getAmount()
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), caregiverContent),
                    NotificationType.RESERVATION_STATUS_CHANGED
            );

            log.info("간병인 새 예약 알림 전송 완료: caregiverId={}", event.getCaregiverId());
        } catch (Exception e) {
            log.error("결제 완료 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "payment-cancelled",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        try {
            log.info("결제 취소 이벤트 수신: paymentId={}, reservationId={}",
                    event.getPaymentId(), event.getReservationId());

            String content = String.format(
                    "[결제 취소] 예약 ID: %s에 대한 결제가 취소되었습니다.\n금액: %s원\n사유: %s",
                    event.getReservationId(), event.getAmount(),
                    event.getCancelReason() != null ? event.getCancelReason() : "취소 사유 없음"
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.PAYMENT_CANCELLED
            );

            log.info("보호자 결제 취소 알림 전송 완료: guardianId={}", event.getGuardianId());

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), content),
                    NotificationType.PAYMENT_CANCELLED
            );

            log.info("간병인 결제 취소 알림 전송 완료: caregiverId={}", event.getCaregiverId());
        } catch (Exception e) {
            log.error("결제 취소 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "reservation-cancelled",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReservationCancelled(ReservationCancelledEvent event) {
        try {
            log.info("예약 취소 이벤트 수신: reservationId={}, paymentId={}",
                    event.getReservationId(), event.getPaymentId());

            String content = String.format(
                    "[예약 취소] 예약 ID: %s가 취소되었습니다.\n금액: %s원\n사유: %s",
                    event.getReservationId(), event.getAmount(),
                    event.getCancelReason() != null ? event.getCancelReason() : "취소 사유 없음"
            );

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.RESERVATION_CANCELLED
            );

            log.info("보호자 예약 취소 알림 전송 완료: guardianId={}", event.getGuardianId());

            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), content),
                    NotificationType.RESERVATION_CANCELLED
            );

            log.info("간병인 예약 취소 알림 전송 완료: caregiverId={}", event.getCaregiverId());
        } catch (Exception e) {
            log.error("예약 취소 알림 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    private String createStatusChangeContent(String reservationId, String previousStatus, String newStatus, String reason) {
        if ("PENDING_ACCEPTANCE".equals(newStatus)) {
            return String.format(
                    "[예약 상태 변경] 예약 ID: %s\n결제가 완료되어 간병인의 수락을 기다리고 있습니다.",
                    reservationId
            );
        } else if ("CONFIRMED".equals(newStatus)) {
            return String.format(
                    "[예약 확정] 예약 ID: %s\n간병인이 예약을 수락했습니다. 예약이 확정되었습니다.",
                    reservationId
            );
        } else if ("REJECTED".equals(newStatus)) {
            return String.format(
                    "[예약 거절] 예약 ID: %s\n간병인이 예약을 거절했습니다.\n사유: %s",
                    reservationId, reason != null ? reason : "거절 사유 없음"
            );
        } else if ("COMPLETED".equals(newStatus)) {
            return String.format(
                    "[서비스 완료] 예약 ID: %s\n서비스가 완료되었습니다.",
                    reservationId
            );
        } else {
            return String.format(
                    "[예약 상태 변경] 예약 ID: %s\n상태: %s → %s\n%s",
                    reservationId,
                    previousStatus, newStatus,
                    reason != null ? "사유: " + reason : ""
            );
        }
    }

    private void sendStatusChangeNotifications(ReservationStatusChangedEvent event, String content) {
        try {
            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getGuardianId(), content),
                    NotificationType.RESERVATION_STATUS_CHANGED
            );

            log.info("보호자 상태 변경 알림 전송 완료: guardianId={}", event.getGuardianId());
        } catch (Exception e) {
            log.error("보호자 알림 전송 실패: guardianId={}, error={}", event.getGuardianId(), e.getMessage());
        }

        try {
            notificationService.createNotificationWithType(
                    new NotificationCreateRequestDto(event.getCaregiverId(), content),
                    NotificationType.RESERVATION_STATUS_CHANGED
            );

            log.info("간병인 상태 변경 알림 전송 완료: caregiverId={}", event.getCaregiverId());
        } catch (Exception e) {
            log.error("간병인 알림 전송 실패: caregiverId={}, error={}", event.getCaregiverId(), e.getMessage());
        }
    }
}