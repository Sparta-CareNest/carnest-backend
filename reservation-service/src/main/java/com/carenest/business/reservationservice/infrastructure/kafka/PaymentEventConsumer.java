package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.carenest.business.reservationservice.infrastructure.service.ExternalServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ExternalServiceClient externalServiceClient;

    @Transactional
    @KafkaListener(
            topics = "payment-completed",
            groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCompletedEvent(PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        log.info("결제 완료 이벤트 수신: paymentId={}, reservationId={}",
                event.getPaymentId(), event.getReservationId());

        try {
            if (event.getReservationId() == null) {
                log.error("결제 완료 이벤트에 예약 ID가 없음");
                acknowledgment.acknowledge();
                return;
            }

            // 예약 정보 조회
            Optional<Reservation> reservationOpt = reservationRepository.findById(event.getReservationId());

            if (reservationOpt.isEmpty()) {
                log.error("예약 정보를 찾을 수 없음: reservationId={}", event.getReservationId());
                acknowledgment.acknowledge();
                return;
            }

            Reservation reservation = reservationOpt.get();

            // 이미 결제 정보가 연결된 경우 체크
            if (reservation.getPaymentId() != null &&
                    reservation.getPaymentId().equals(event.getPaymentId())) {
                log.info("이미 결제 정보가 연결되어 있음: reservationId={}, paymentId={}",
                        reservation.getReservationId(), reservation.getPaymentId());
                acknowledgment.acknowledge();
                return;
            }

            // 결제 완료 시 예약 상태 업데이트 및 결제 정보 연결
            reservationService.linkPayment(event.getReservationId(), event.getPaymentId());
            log.info("결제 정보 연결 완료: reservationId={}, paymentId={}",
                    event.getReservationId(), event.getPaymentId());

            // 결제 완료 후 간병인에게 새 예약 알림 전송
            reservation = reservationRepository.findById(event.getReservationId()).get(); // 최신 상태로 다시 조회

            String notificationMsg = String.format(
                    "새로운 예약이 들어왔습니다. 예약번호: %s, 환자명: %s, 시작일시: %s, 종료일시: %s. 확인 후 수락해주세요.",
                    reservation.getReservationId(),
                    reservation.getPatientName(),
                    reservation.getStartedAt(),
                    reservation.getEndedAt()
            );

            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getCaregiverId(), notificationMsg);

            log.info("간병인에게 새 예약 알림 전송 완료: caregiverId={}", reservation.getCaregiverId());

            // 보호자에게도 결제 완료 알림 발송
            String guardianMsg = String.format(
                    "결제가 완료되었습니다. 예약번호: %s, 금액: %s원, 간병인의 예약 수락을 기다리고 있습니다.",
                    reservation.getReservationId(),
                    reservation.getTotalAmount()
            );

            externalServiceClient.sendPaymentCompletedNotification(
                    reservation.getGuardianId(), guardianMsg);

            log.info("보호자에게 결제 완료 알림 전송 완료: guardianId={}", reservation.getGuardianId());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    @Transactional
    @KafkaListener(
            topics = "payment-cancelled",
            groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCancelledEvent(PaymentCancelledEvent event, Acknowledgment acknowledgment) {
        log.info("결제 취소 이벤트 수신: paymentId={}, reservationId={}",
                event.getPaymentId(), event.getReservationId());

        try {
            if (event.getReservationId() == null) {
                log.error("결제 취소 이벤트에 예약 ID가 없음");
                acknowledgment.acknowledge();
                return;
            }

            // 예약 정보 조회
            Optional<Reservation> optionalReservation = reservationRepository.findById(event.getReservationId());

            if (optionalReservation.isEmpty()) {
                log.error("예약 정보를 찾을 수 없음: reservationId={}", event.getReservationId());
                acknowledgment.acknowledge();
                return;
            }

            Reservation reservation = optionalReservation.get();

            // 예약이 이미 취소 상태가 아닌 경우에만 처리
            if (reservation.getStatus() != ReservationStatus.CANCELLED) {
                // 결제 취소로 인한 예약 취소 처리
                reservationService.cancelReservation(
                        event.getReservationId(),
                        "결제 취소로 인한 자동 예약 취소",
                        event.getCancelReason() != null ?
                                "결제 취소 이유: " + event.getCancelReason() : "결제 취소"
                );

                log.info("결제 취소로 인한 예약 취소 완료: reservationId={}", event.getReservationId());

                // 상태 변경 알림 발송
                String cancelMsg = String.format(
                        "결제 취소로 인해 예약이 자동으로 취소되었습니다. 예약번호: %s, 취소 사유: %s",
                        event.getReservationId(),
                        event.getCancelReason() != null ? event.getCancelReason() : "결제 취소"
                );

                // 보호자에게 알림
                externalServiceClient.sendReservationCreatedNotification(
                        event.getGuardianId(), cancelMsg);

                log.info("보호자에게 예약 취소 알림 전송 완료: guardianId={}", event.getGuardianId());

                // 간병인에게 알림
                externalServiceClient.sendReservationCreatedNotification(
                        event.getCaregiverId(), cancelMsg);

                log.info("간병인에게 예약 취소 알림 전송 완료: caregiverId={}", event.getCaregiverId());
            } else {
                log.info("이미 취소된 예약입니다: reservationId={}", event.getReservationId());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}