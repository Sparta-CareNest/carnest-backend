package com.carenest.business.paymentservice.infrastructure.service;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.infrastructure.kafka.NotificationEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationEventProducer notificationEventProducer;

    @Override
    public void sendPaymentSuccessNotification(Payment payment) {
        try {
            log.info("결제 성공 알림 발행: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            // 보호자에게 알림 전송
            notificationEventProducer.sendNotificationEvent(
                    payment.getGuardianId(),
                    "PAYMENT_SUCCESS",
                    String.format("결제가 완료되었습니다. 금액: %s원", payment.getAmount())
            );

            // 간병인에게도 알림
            notificationEventProducer.sendNotificationEvent(
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
            notificationEventProducer.sendNotificationEvent(
                    payment.getGuardianId(),
                    "PAYMENT_CANCELED",
                    String.format("결제가 취소되었습니다. 금액: %s원, 사유: %s",
                            payment.getAmount(), payment.getCancelReason())
            );

            // 간병인에게 알림
            notificationEventProducer.sendNotificationEvent(
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

            notificationEventProducer.sendNotificationEvent(
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
}