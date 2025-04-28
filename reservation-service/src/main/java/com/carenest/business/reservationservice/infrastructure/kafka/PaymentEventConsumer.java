package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.common.kafka.KafkaRetryConfig.IdempotentMessageProcessor;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.carenest.business.reservationservice.infrastructure.service.ExternalServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ExternalServiceClient externalServiceClient;
    private final IdempotentMessageProcessor idempotentProcessor;
    private final NotificationEventProducer notificationEventProducer;

    @Transactional
    @KafkaListener(
            topics = "payment-completed",
            groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCompletedEvent(ConsumerRecord<String, PaymentCompletedEvent> record, Acknowledgment acknowledgment) {
        PaymentCompletedEvent event = record.value();
        String messageKey = generateMessageKey(record);

        log.info("결제 완료 이벤트 수신: reservationId={}, paymentId={}",
                event.getReservationId(), event.getPaymentId());

        try {
            // 멱등성 처리
            if (idempotentProcessor.isProcessed(messageKey)) {
                log.info("이미 처리된 결제 완료 이벤트: paymentId={}, 중복 메시지 무시", event.getPaymentId());
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
            log.info("예약 정보 조회 성공: reservationId={}, 현재 상태={}",
                    reservation.getReservationId(), reservation.getStatus());

            // 이미 결제 정보가 연결된 경우 체크
            if (reservation.getPaymentId() != null) {
                if (reservation.getPaymentId().equals(event.getPaymentId())) {
                    log.info("이미 결제 정보가 연결되어 있음: reservationId={}, paymentId={}",
                            reservation.getReservationId(), reservation.getPaymentId());
                    idempotentProcessor.markAsProcessed(messageKey);
                    acknowledgment.acknowledge();
                    return;
                }
                log.warn("다른 결제 정보가 이미 연결됨: reservationId={}, existingPaymentId={}, newPaymentId={}",
                        reservation.getReservationId(), reservation.getPaymentId(), event.getPaymentId());
                acknowledgment.acknowledge();
                return;
            }

            // 결제 완료 시 예약 상태 업데이트 및 결제 정보 연결
            reservationService.linkPayment(event.getReservationId(), event.getPaymentId());
            log.info("결제 정보 연결 완료: reservationId={}, paymentId={}",
                    event.getReservationId(), event.getPaymentId());

            // 간병인에게 알림 발송
            notificationEventProducer.sendNotificationEvent(
                    reservation.getCaregiverId(),
                    "NEW_RESERVATION",
                    String.format("새로운 예약이 들어왔습니다. 예약번호: %s, 시작일시: %s",
                            reservation.getReservationId(),
                            reservation.getStartedAt())
            );

            // 멱등성 처리를 위한 메시지 처리 기록
            idempotentProcessor.markAsProcessed(messageKey);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 중 오류 발생: reservationId={}, error={}",
                    event.getReservationId(), e.getMessage(), e);
            // CommonErrorHandler에서 처리하도록 예외 다시 throw
            throw e;
        }
    }

    @Transactional
    @KafkaListener(
            topics = "payment-cancelled",
            groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCancelledEvent(ConsumerRecord<String, PaymentCancelledEvent> record, Acknowledgment acknowledgment) {
        PaymentCancelledEvent event = record.value();
        String messageKey = generateMessageKey(record);

        log.info("결제 취소 이벤트 수신: reservationId={}, paymentId={}",
                event.getReservationId(), event.getPaymentId());

        try {
            // 멱등성 처리
            if (idempotentProcessor.isProcessed(messageKey)) {
                log.info("이미 처리된 결제 취소 이벤트: paymentId={}, 중복 메시지 무시", event.getPaymentId());
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
            if (reservation.getStatus() != com.carenest.business.reservationservice.domain.model.ReservationStatus.CANCELLED) {
                // 결제 취소로 인한 예약 취소 처리
                reservationService.cancelReservation(
                        event.getReservationId(),
                        "결제 취소로 인한 자동 예약 취소",
                        event.getCancelReason() != null ?
                                "결제 취소 이유: " + event.getCancelReason() : "결제 취소"
                );

                log.info("결제 취소로 인한 예약 취소 완료: reservationId={}", event.getReservationId());
            } else {
                log.info("이미 취소된 예약입니다: reservationId={}", event.getReservationId());
            }

            // 멱등성 처리
            idempotentProcessor.markAsProcessed(messageKey);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 중 오류 발생: reservationId={}, error={}",
                    event.getReservationId(), e.getMessage(), e);
            // CommonErrorHandler에서 처리하도록 예외 다시 throw
            throw e;
        }
    }

    @KafkaListener(
            topics = "payment-completed.dlq",
            groupId = "reservation-service-dlq-group",
            containerFactory = "deadLetterListenerContainerFactory")
    public void processDLQPaymentCompletedEvent(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        try {
            log.warn("DLQ에서 결제 완료 이벤트 처리: record={}", record);

            // DLQ 데이터 로깅 및 알림
            UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 관리자 ID
            notificationEventProducer.sendNotificationEvent(
                    adminId,
                    "DLQ_ALERT",
                    String.format("[중요] 결제 완료 이벤트 처리 실패: 토픽=%s, 파티션=%d, 오프셋=%d",
                            record.topic(), record.partition(), record.offset())
            );
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류: {}", e.getMessage(), e);
        } finally {
            // DLQ는 항상 acknowledge
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = "payment-cancelled.dlq",
            groupId = "reservation-service-dlq-group",
            containerFactory = "deadLetterListenerContainerFactory")
    public void processDLQPaymentCancelledEvent(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        try {
            log.warn("DLQ에서 결제 취소 이벤트 처리: record={}", record);

            // DLQ 데이터 로깅 및 알림
            UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 관리자 ID
            notificationEventProducer.sendNotificationEvent(
                    adminId,
                    "DLQ_ALERT",
                    String.format("[중요] 결제 취소 이벤트 처리 실패: 토픽=%s, 파티션=%d, 오프셋=%d",
                            record.topic(), record.partition(), record.offset())
            );
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류: {}", e.getMessage(), e);
        } finally {
            // DLQ는 항상 acknowledge
            acknowledgment.acknowledge();
        }
    }

    private <T> String generateMessageKey(ConsumerRecord<String, T> record) {
        return record.topic() + "-" + record.partition() + "-" + record.offset();
    }
}