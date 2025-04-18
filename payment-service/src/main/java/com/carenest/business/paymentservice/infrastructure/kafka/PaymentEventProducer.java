package com.carenest.business.paymentservice.infrastructure.kafka;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCompletedEvent(Payment payment) {
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

            String key = payment.getPaymentId().toString();

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("payment-completed", key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("결제 완료 이벤트 발행 성공: paymentId={}, reservationId={}",
                            payment.getPaymentId(), payment.getReservationId());
                    log.debug("메시지 발행 완료: topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("결제 완료 이벤트 발행 실패: paymentId={}, reservationId={}",
                            payment.getPaymentId(), payment.getReservationId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("결제 완료 이벤트 생성 중 예외 발생: paymentId={}", payment.getPaymentId(), e);
        }
    }

    public void sendPaymentCancelledEvent(Payment payment) {
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

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("payment-cancelled", key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("결제 취소 이벤트 발행 성공: paymentId={}, reservationId={}",
                            payment.getPaymentId(), payment.getReservationId());
                    log.debug("메시지 발행 완료: topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("결제 취소 이벤트 발행 실패: paymentId={}, reservationId={}",
                            payment.getPaymentId(), payment.getReservationId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("결제 취소 이벤트 생성 중 예외 발생: paymentId={}", payment.getPaymentId(), e);
        }
    }
}