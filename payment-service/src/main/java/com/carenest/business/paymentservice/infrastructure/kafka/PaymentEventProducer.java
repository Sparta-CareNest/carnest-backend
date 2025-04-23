package com.carenest.business.paymentservice.infrastructure.kafka;

import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;
import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.common.exception.KafkaPublishException;
import com.carenest.business.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 타임아웃 설정 (밀리초)
    private static final long SEND_TIMEOUT_MS = 5000;

    @Retryable(
            value = {KafkaPublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public void sendPaymentCompletedEvent(Payment payment) {
        log.info("결제 완료 이벤트 발행 시작: paymentId={}, reservationId={}",
                payment.getPaymentId(), payment.getReservationId());

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTimestamp(LocalDateTime.now())
                .eventType("PAYMENT_COMPLETED")
                .version("1.0")
                .paymentId(payment.getPaymentId())
                .reservationId(payment.getReservationId())
                .guardianId(payment.getGuardianId())
                .caregiverId(payment.getCaregiverId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .approvalNumber(payment.getApprovalNumber())
                .build();

        // 2. 메시지 키 (멱등성 및 파티셔닝 보장)
        String key = payment.getPaymentId().toString();
        String topic = "payment-completed";

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("결제 완료 이벤트 발행 성공: paymentId={}, topic={}, partition={}, offset={}",
                            payment.getPaymentId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("결제 완료 이벤트 발행 실패: paymentId={}, 에러={}",
                            payment.getPaymentId(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("결제 완료 이벤트 발행 중 예외 발생: paymentId={}", payment.getPaymentId(), e);
            throw new KafkaPublishException("결제 완료 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }

    @Retryable(
            value = {KafkaPublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public void sendPaymentCancelledEvent(Payment payment) {
        log.info("결제 취소 이벤트 발행 시작: paymentId={}, reservationId={}",
                payment.getPaymentId(), payment.getReservationId());

        try {
            PaymentCancelledEvent event = PaymentCancelledEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTimestamp(LocalDateTime.now())
                    .eventType("PAYMENT_CANCELLED")
                    .version("1.0")
                    .paymentId(payment.getPaymentId())
                    .reservationId(payment.getReservationId())
                    .guardianId(payment.getGuardianId())
                    .caregiverId(payment.getCaregiverId())
                    .amount(payment.getAmount())
                    .cancelReason(payment.getCancelReason())
                    .build();

            String key = payment.getPaymentId().toString();
            String topic = "payment-cancelled";

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("결제 취소 이벤트 발행 성공: paymentId={}, topic={}, partition={}, offset={}",
                            payment.getPaymentId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("결제 취소 이벤트 발행 실패: paymentId={}, reservationId={}, 에러={}",
                            payment.getPaymentId(), payment.getReservationId(), ex.getMessage(), ex);
                }
            });

            try {
                future.get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                log.info("결제 취소 이벤트 발행 완료 확인: paymentId={}", payment.getPaymentId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("결제 취소 이벤트 동기 확인 중 오류: paymentId={}", payment.getPaymentId(), e);
            }

        } catch (Exception e) {
            log.error("결제 취소 이벤트 생성 중 예외 발생: paymentId={}, 에러={}",
                    payment.getPaymentId(), e.getMessage(), e);
            throw new KafkaPublishException("결제 취소 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }

    @Retryable(
            value = {KafkaPublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    public void sendCaregiverPendingEvent(Payment payment) {
        try {
            log.info("간병인 승인/거절 이벤트 발행 시작: reservationId={}, caregiverId={}",
                    payment.getReservationId(), payment.getCaregiverId());

            CaregiverPendingEvent event = CaregiverPendingEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTimestamp(LocalDateTime.now())
                    .eventType("CAREGIVER_PENDING")
                    .version("1.0")
                    .reservationId(payment.getReservationId())
                    .caregiverId(payment.getCaregiverId())
                    .message("새 예약이 수락 대기 상태입니다. 예약 ID: " + payment.getReservationId())
                    .build();

            String key = payment.getPaymentId().toString();
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("caregiver-accept", key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("간병인 승인/거절 이벤트 발행 성공: reservationId={}, caregiverId={}, topic={}, partition={}, offset={}",
                            payment.getReservationId(),
                            payment.getCaregiverId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("간병인 승인/거절 이벤트 발행 실패: paymentId={}, 에러={}", payment.getPaymentId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("간병인 승인/거절 이벤트 생성 중 예외 발생: paymentId={}, 에러={}",
                    payment.getPaymentId(), e.getMessage(), e);
            throw new KafkaPublishException("간병인 승인/거절 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }
}