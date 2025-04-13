package com.carenest.business.reservationservice.infrastructure.service;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.infrastructure.client.CaregiverServiceClient;
import com.carenest.business.reservationservice.infrastructure.client.NotificationServiceClient;
import com.carenest.business.reservationservice.infrastructure.client.PaymentServiceClient;
import com.carenest.business.reservationservice.infrastructure.client.dto.request.NotificationCreateRequestDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.request.PaymentCreateRequestDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.CaregiverDetailResponseDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalServiceClientImpl implements ExternalServiceClient {

    private final PaymentServiceClient paymentServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final CaregiverServiceClient caregiverServiceClient;

    @Override
    public UUID requestPayment(Reservation reservation) {
        try {
            log.info("결제 요청 시작: reservationId={}", reservation.getReservationId());

            PaymentCreateRequestDto paymentRequest = PaymentCreateRequestDto.builder()
                    .reservationId(reservation.getReservationId())
                    .guardianId(reservation.getGuardianId())
                    .caregiverId(reservation.getCaregiverId())
                    .amount(reservation.getTotalAmount())
                    .paymentMethod("CARD") // 기본값
                    .paymentGateway("PaySo") // 기본값
                    .build();

            ResponseDto<PaymentResponseDto> response = paymentServiceClient.createPayment(paymentRequest);

            if (response.getData() != null && response.getData().getPaymentId() != null) {
                log.info("결제 요청 성공: paymentId={}", response.getData().getPaymentId());
                return response.getData().getPaymentId();
            } else {
                log.error("결제 요청 실패: 응답 데이터가 비어있습니다");
                throw new RuntimeException("결제 요청 처리 중 오류가 발생했습니다");
            }
        } catch (Exception e) {
            log.error("결제 요청 중 예외 발생", e);
            throw new RuntimeException("결제 서비스 연동 중 오류가 발생했습니다", e);
        }
    }

    @Override
    public boolean cancelPayment(UUID paymentId, String cancelReason) {
        try {
            log.info("결제 취소 요청 시작: paymentId={}, reason={}", paymentId, cancelReason);

            ResponseDto<PaymentResponseDto> response = paymentServiceClient.cancelPayment(
                    paymentId, cancelReason);

            if (response.getData() != null) {
                log.info("결제 취소 성공: paymentId={}", paymentId);
                return true;
            } else {
                log.error("결제 취소 실패: 응답 데이터가 비어있습니다");
                return false;
            }
        } catch (Exception e) {
            log.error("결제 취소 중 예외 발생", e);
            return false;
        }
    }

    @Override
    public void sendReservationCreatedNotification(UUID userId, String message) {
        try {
            log.info("예약 생성 알림 전송 시작: userId={}", userId);

            NotificationCreateRequestDto notificationRequest = NotificationCreateRequestDto.builder()
                    .receiverId(userId)
                    .content(message)
                    .build();

            notificationServiceClient.sendReservationCreatedNotification(notificationRequest);
            log.info("예약 생성 알림 전송 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("예약 생성 알림 전송 중 예외 발생", e);
        }
    }

    @Override
    public void sendPaymentCompletedNotification(UUID userId, String message) {
        try {
            log.info("결제 완료 알림 전송 시작: userId={}", userId);

            NotificationCreateRequestDto notificationRequest = NotificationCreateRequestDto.builder()
                    .receiverId(userId)
                    .content(message)
                    .build();

            notificationServiceClient.sendPaymentSuccessNotification(notificationRequest);
            log.info("결제 완료 알림 전송 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("결제 완료 알림 전송 중 예외 발생", e);
        }
    }

    @Override
    public CaregiverDetailResponseDto getCaregiverDetail(UUID caregiverId) {
        try {
            log.info("간병인 정보 조회 시작: caregiverId={}", caregiverId);

            ResponseDto<CaregiverDetailResponseDto> response = caregiverServiceClient.getCaregiverDetail(caregiverId);

            if (response.getData() != null) {
                log.info("간병인 정보 조회 성공: caregiverId={}", caregiverId);
                return response.getData();
            } else {
                log.error("간병인 정보 조회 실패: 응답 데이터가 비어있습니다");
                throw new RuntimeException("간병인 정보를 조회할 수 없습니다");
            }
        } catch (Exception e) {
            log.error("간병인 정보 조회 중 예외 발생", e);
            throw new RuntimeException("간병인 서비스 연동 중 오류가 발생했습니다", e);
        }
    }
}