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
        if (payment == null || payment.getPaymentId() == null) {
            log.error("결제 완료 이벤트 발행 실패: 유효하지 않은 payment 객체");
            return;
        }

        log.info("결제 완료 이벤트 발행 시작: paymentId={}, reservationId={}",
                payment.getPaymentId(), payment.getReservationId());

        try {
            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .paymentId(payment.getPaymentId())
                    .reservationId(payment.getReservationId())
                    .guardianId(payment.getGuardianId())
                    .caregiverId(payment.getCaregiverId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .approvalNumber(payment.getApprovalNumber())
                    .build();

            // 메시지 키
            String key = payment.getPaymentId().toString();
            String topic = "payment-completed";

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

            try {
                future.get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                log.debug("결제 완료 이벤트 전송 확인 완료: paymentId={}", payment.getPaymentId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.warn("결제 완료 이벤트 전송 확인 타임아웃: paymentId={}", payment.getPaymentId(), e);
            }

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
        if (payment == null || payment.getPaymentId() == null) {
            log.error("결제 취소 이벤트 발행 실패: 유효하지 않은 payment 객체");
            return;
        }

        log.info("결제 취소 이벤트 발행 시작: paymentId={}, reservationId={}",
                payment.getPaymentId(), payment.getReservationId());

        try {
            PaymentCancelledEvent event = PaymentCancelledEvent.builder()
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
                log.debug("결제 취소 이벤트 발행 완료 확인: paymentId={}", payment.getPaymentId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.warn("결제 취소 이벤트 동기 확인 중 타임아웃: paymentId={}", payment.getPaymentId(), e);
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
        if (payment == null || payment.getReservationId() == null || payment.getCaregiverId() == null) {
            log.error("간병인 승인/거절 이벤트 발행 실패: 유효하지 않은 payment 객체");
            return;
        }

        try {
            log.info("간병인 승인/거절 이벤트 발행 시작: reservationId={}, caregiverId={}",
                    payment.getReservationId(), payment.getCaregiverId());

            CaregiverPendingEvent event = CaregiverPendingEvent.builder()
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

            try {
                future.get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                log.debug("간병인 이벤트 전송 확인 완료: reservationId={}", payment.getReservationId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.warn("간병인 이벤트 전송 확인 타임아웃: reservationId={}", payment.getReservationId(), e);
            }
        } catch (Exception e) {
            log.error("간병인 승인/거절 이벤트 생성 중 예외 발생: paymentId={}, 에러={}",
                    payment.getPaymentId(), e.getMessage(), e);
            throw new KafkaPublishException("간병인 승인/거절 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }
}