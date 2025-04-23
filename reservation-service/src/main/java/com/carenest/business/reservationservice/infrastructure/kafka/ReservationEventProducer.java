package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationCreatedEvent;
import com.carenest.business.common.event.reservation.ReservationStatusChangedEvent;
import com.carenest.business.common.exception.KafkaPublishException;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final long SEND_TIMEOUT_MS = 5000;

    @Retryable(
            value = {KafkaPublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public void sendReservationCreatedEvent(Reservation reservation) {
        if (reservation == null) {
            log.error("예약 생성 이벤트 발행 실패: reservation 객체가 null입니다");
            return;
        }

        if (reservation.getReservationId() == null) {
            log.error("예약 생성 이벤트 발행 실패: reservationId가 null입니다");
            return;
        }

        log.info("예약 생성 이벤트 발행 시작: reservationId={}, guardianId={}, totalAmount={}",
                reservation.getReservationId(), reservation.getGuardianId(), reservation.getTotalAmount());

        try {
            ReservationCreatedEvent event = ReservationCreatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTimestamp(LocalDateTime.now())
                    .eventType("RESERVATION_CREATED")
                    .version("1.0")
                    .reservationId(reservation.getReservationId())
                    .guardianId(reservation.getGuardianId())
                    .caregiverId(reservation.getCaregiverId())
                    .patientName(reservation.getPatientName())
                    .startedAt(reservation.getStartedAt())
                    .endedAt(reservation.getEndedAt())
                    .serviceType(reservation.getServiceType().name())
                    .totalAmount(reservation.getTotalAmount())
                    .serviceFee(reservation.getServiceFee())
                    .build();

            String key = reservation.getReservationId().toString();
            String topic = KafkaTopic.RESERVATION_CREATED.getTopicName();

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("예약 생성 이벤트 발행 성공: reservationId={}, topic={}, partition={}, offset={}",
                            reservation.getReservationId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("예약 생성 이벤트 발행 실패: reservationId={}, 에러={}",
                            reservation.getReservationId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("예약 생성 이벤트 생성 중 예외 발생: reservationId={}", reservation.getReservationId(), e);
            throw new KafkaPublishException("예약 생성 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }

    @Retryable(
            value = {KafkaPublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public void sendReservationCancelledEvent(Reservation reservation) {
        if (reservation == null) {
            log.error("예약 취소 이벤트 발행 실패: reservation 객체가 null입니다");
            return;
        }

        if (reservation.getReservationId() == null) {
            log.error("예약 취소 이벤트 발행 실패: reservationId가 null입니다");
            return;
        }

        log.info("예약 취소 이벤트 발행 시작: reservationId={}, paymentId={}",
                reservation.getReservationId(), reservation.getPaymentId());

        if (reservation.getPaymentId() == null) {
            log.warn("예약 취소 이벤트 발행 생략: 결제 정보 없음 (reservationId={})", reservation.getReservationId());
            return;
        }

        try {
            ReservationCancelledEvent event = ReservationCancelledEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTimestamp(LocalDateTime.now())
                    .eventType("RESERVATION_CANCELLED")
                    .version("1.0")
                    .reservationId(reservation.getReservationId())
                    .guardianId(reservation.getGuardianId())
                    .caregiverId(reservation.getCaregiverId())
                    .paymentId(reservation.getPaymentId())
                    .amount(reservation.getTotalAmount())
                    .cancelReason(reservation.getCancelReason() != null ?
                            reservation.getCancelReason() : "취소 사유 없음")
                    .build();

            String key = reservation.getReservationId().toString();
            String topic = KafkaTopic.RESERVATION_CANCELLED.getTopicName();

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("예약 취소 이벤트 발행 성공: reservationId={}, topic={}, partition={}, offset={}",
                            reservation.getReservationId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("예약 취소 이벤트 발행 실패: reservationId={}, 에러={}",
                            reservation.getReservationId(), ex.getMessage(), ex);
                }
            });

            try {
                future.get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                log.info("예약 취소 이벤트 발행 완료 확인: reservationId={}", reservation.getReservationId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("예약 취소 이벤트 동기 확인 중 오류: reservationId={}", reservation.getReservationId(), e);
                // 메시지는 비동기로 계속 처리될 것이므로 예외를 다시 던지지 않음
            }
        } catch (Exception e) {
            log.error("예약 취소 이벤트 생성 중 예외 발생: reservationId={}", reservation.getReservationId(), e);
            throw new KafkaPublishException("예약 취소 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }

    @Retryable(
            value = {KafkaPublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public void sendReservationStatusChangedEvent(Reservation reservation, ReservationStatus previousStatus) {
        Assert.notNull(reservation, "Reservation 객체는 null일 수 없습니다");
        Assert.notNull(reservation.getReservationId(), "ReservationId는 null일 수 없습니다");
        Assert.notNull(reservation.getStatus(), "현재 예약 상태는 null일 수 없습니다");
        Assert.notNull(previousStatus, "이전 예약 상태는 null일 수 없습니다");

        log.info("예약 상태 변경 이벤트 발행 시작: reservationId={}, 상태 변경={}→{}",
                reservation.getReservationId(), previousStatus, reservation.getStatus());

        try {
            ReservationStatusChangedEvent event = ReservationStatusChangedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTimestamp(LocalDateTime.now())
                    .eventType("RESERVATION_STATUS_CHANGED")
                    .version("1.0")
                    .reservationId(reservation.getReservationId())
                    .guardianId(reservation.getGuardianId())
                    .caregiverId(reservation.getCaregiverId())
                    .previousStatus(previousStatus.name())
                    .newStatus(reservation.getStatus().name())
                    .reason(getStatusChangeReason(reservation, previousStatus))
                    .paymentId(reservation.getPaymentId())
                    .build();

            String key = reservation.getReservationId().toString();
            String topic = KafkaTopic.RESERVATION_STATUS_CHANGED.getTopicName();

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("예약 상태 변경 이벤트 발행 성공: reservationId={}, 상태={}→{}, topic={}, partition={}, offset={}",
                            reservation.getReservationId(),
                            previousStatus,
                            reservation.getStatus(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("예약 상태 변경 이벤트 발행 실패: reservationId={}, 에러={}",
                            reservation.getReservationId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("예약 상태 변경 이벤트 생성 중 예외 발생: reservationId={}", reservation.getReservationId(), e);
            throw new KafkaPublishException("예약 상태 변경 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }

    private String getStatusChangeReason(Reservation reservation, ReservationStatus previousStatus) {
        if (reservation.getStatus() == null) {
            return "알 수 없는 상태 변경";
        }

        switch (reservation.getStatus()) {
            case PENDING_ACCEPTANCE:
                return "결제 완료";
            case CONFIRMED:
                return "간병인 수락 완료";
            case REJECTED:
                return reservation.getRejectionReason() != null ?
                        reservation.getRejectionReason() : "거절 사유 없음";
            case CANCELLED:
                return reservation.getCancelReason() != null ?
                        reservation.getCancelReason() : "취소 사유 없음";
            case COMPLETED:
                return "서비스 완료";
            default:
                return "상태 변경";
        }
    }
}