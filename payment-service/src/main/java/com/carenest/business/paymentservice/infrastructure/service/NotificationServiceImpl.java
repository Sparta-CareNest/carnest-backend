package com.carenest.business.paymentservice.infrastructure.service;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.infrastructure.client.NotificationServiceClient;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.NotificationCreateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationServiceClient notificationServiceClient;

    @Override
    public void sendPaymentSuccessNotification(Payment payment) {
        try {
            log.info("결제 성공 알림 발송: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            NotificationCreateRequestDto request = NotificationCreateRequestDto.builder()
                    .receiverId(payment.getGuardianId())
                    .content(String.format("결제가 완료되었습니다. 금액: %s원", payment.getAmount()))
                    .build();

            notificationServiceClient.sendPaymentSuccessNotification(request);

            // 간병인에게도 알림
            NotificationCreateRequestDto caregiverRequest = NotificationCreateRequestDto.builder()
                    .receiverId(payment.getCaregiverId())
                    .content(String.format("새로운 예약의 결제가 완료되었습니다. 확인해주세요."))
                    .build();

            notificationServiceClient.sendPaymentSuccessNotification(caregiverRequest);

            log.info("결제 성공 알림 발송 완료");
        } catch (Exception e) {
            log.error("결제 성공 알림 발송 실패", e);
        }
    }

    @Override
    public void sendPaymentCancelNotification(Payment payment) {
        try {
            log.info("결제 취소 알림 발송: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            NotificationCreateRequestDto request = NotificationCreateRequestDto.builder()
                    .receiverId(payment.getGuardianId())
                    .content(String.format("결제가 취소되었습니다. 금액: %s원, 사유: %s",
                            payment.getAmount(), payment.getCancelReason()))
                    .build();

            notificationServiceClient.sendPaymentCanceledNotification(request);

            // 간병인에게 알림
            NotificationCreateRequestDto caregiverRequest = NotificationCreateRequestDto.builder()
                    .receiverId(payment.getCaregiverId())
                    .content("예약이 취소되었습니다.")
                    .build();

            notificationServiceClient.sendPaymentCanceledNotification(caregiverRequest);

            log.info("결제 취소 알림 발송 완료");
        } catch (Exception e) {
            log.error("결제 취소 알림 발송 실패", e);
        }
    }

    @Override
    public void sendPaymentRefundNotification(Payment payment) {
        try {
            log.info("결제 환불 알림 발송: paymentId={}, guardianId={}",
                    payment.getPaymentId(), payment.getGuardianId());

            NotificationCreateRequestDto request = NotificationCreateRequestDto.builder()
                    .receiverId(payment.getGuardianId())
                    .content(String.format("결제가 환불되었습니다. 금액: %s원",
                            payment.getRefundAmount()))
                    .build();

            notificationServiceClient.sendPaymentRefundedNotification(request);

            log.info("결제 환불 알림 발송 완료");
        } catch (Exception e) {
            log.error("결제 환불 알림 발송 실패", e);
        }
    }
}