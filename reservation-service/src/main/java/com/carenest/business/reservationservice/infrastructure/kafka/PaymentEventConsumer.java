package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.carenest.business.reservationservice.infrastructure.service.ExternalServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ExternalServiceClient externalServiceClient;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(
            topics = "payment-completed",
            groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCompletedEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment acknowledgment) {
        log.info("결제 완료 이벤트 수신: {}", record);

        try {
            Map<String, Object> payload = record.value();

            // 로그에 전체 페이로드 출력
            log.info("결제 완료 이벤트 페이로드: {}", payload);

            // Map에서 직접 값 추출
            Object reservationIdObj = payload.get("reservationId");
            Object paymentIdObj = payload.get("paymentId");

            log.info("추출된 값 - reservationId: {}, paymentId: {}", reservationIdObj, paymentIdObj);

            // UUID로 변환
            UUID reservationId = null;
            UUID paymentId = null;

            if (reservationIdObj != null) {
                reservationId = UUID.fromString(reservationIdObj.toString());
            }

            if (paymentIdObj != null) {
                paymentId = UUID.fromString(paymentIdObj.toString());
            }

            if (reservationId == null) {
                log.error("결제 완료 이벤트에 예약 ID가 없음");
                acknowledgment.acknowledge();
                return;
            }

            log.info("변환된 UUID - reservationId: {}, paymentId: {}", reservationId, paymentId);

            // 예약 정보 조회
            Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

            if (reservationOpt.isEmpty()) {
                log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                acknowledgment.acknowledge();
                return;
            }

            Reservation reservation = reservationOpt.get();
            log.info("예약 정보 조회 성공: reservationId={}, 현재 상태={}",
                    reservation.getReservationId(), reservation.getStatus());

            // 이미 결제 정보가 연결된 경우 체크
            if (reservation.getPaymentId() != null &&
                    reservation.getPaymentId().equals(paymentId)) {
                log.info("이미 결제 정보가 연결되어 있음: reservationId={}, paymentId={}",
                        reservation.getReservationId(), reservation.getPaymentId());
                acknowledgment.acknowledge();
                return;
            }

            // 결제 완료 시 예약 상태 업데이트 및 결제 정보 연결
            reservationService.linkPayment(reservationId, paymentId);
            log.info("결제 정보 연결 완료: reservationId={}, paymentId={}",
                    reservationId, paymentId);

            // 결제 완료 후 간병인에게 새 예약 알림 전송
            reservation = reservationRepository.findById(reservationId).get(); // 최신 상태로 다시 조회

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
            // 스택 트레이스 출력
            log.error("스택 트레이스:", e);
            acknowledgment.acknowledge();
        }
    }

    @Transactional
    @KafkaListener(
            topics = "payment-cancelled",
            groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCancelledEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment acknowledgment) {
        log.info("결제 취소 이벤트 수신: {}", record);

        try {
            Map<String, Object> payload = record.value();

            // Map에서 직접 값 추출
            Object reservationIdObj = payload.get("reservationId");
            Object paymentIdObj = payload.get("paymentId");
            Object cancelReasonObj = payload.get("cancelReason");

            // UUID로 변환
            UUID reservationId = null;
            UUID paymentId = null;
            String cancelReason = null;

            if (reservationIdObj != null) {
                reservationId = UUID.fromString(reservationIdObj.toString());
            }

            if (paymentIdObj != null) {
                paymentId = UUID.fromString(paymentIdObj.toString());
            }

            if (cancelReasonObj != null) {
                cancelReason = cancelReasonObj.toString();
            }

            if (reservationId == null) {
                log.error("결제 취소 이벤트에 예약 ID가 없음");
                acknowledgment.acknowledge();
                return;
            }

            // 예약 정보 조회
            Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);

            if (optionalReservation.isEmpty()) {
                log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                acknowledgment.acknowledge();
                return;
            }

            Reservation reservation = optionalReservation.get();

            // 예약이 이미 취소 상태가 아닌 경우에만 처리
            if (reservation.getStatus() != com.carenest.business.reservationservice.domain.model.ReservationStatus.CANCELLED) {
                // 결제 취소로 인한 예약 취소 처리
                reservationService.cancelReservation(
                        reservationId,
                        "결제 취소로 인한 자동 예약 취소",
                        cancelReason != null ?
                                "결제 취소 이유: " + cancelReason : "결제 취소"
                );

                log.info("결제 취소로 인한 예약 취소 완료: reservationId={}", reservationId);

                // 상태 변경 알림 발송
                String cancelMsg = String.format(
                        "결제 취소로 인해 예약이 자동으로 취소되었습니다. 예약번호: %s, 취소 사유: %s",
                        reservationId,
                        cancelReason != null ? cancelReason : "결제 취소"
                );

                // 보호자에게 알림
                externalServiceClient.sendReservationCreatedNotification(
                        reservation.getGuardianId(), cancelMsg);

                log.info("보호자에게 예약 취소 알림 전송 완료: guardianId={}", reservation.getGuardianId());

                // 간병인에게 알림
                externalServiceClient.sendReservationCreatedNotification(
                        reservation.getCaregiverId(), cancelMsg);

                log.info("간병인에게 예약 취소 알림 전송 완료: caregiverId={}", reservation.getCaregiverId());
            } else {
                log.info("이미 취소된 예약입니다: reservationId={}", reservationId);
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}