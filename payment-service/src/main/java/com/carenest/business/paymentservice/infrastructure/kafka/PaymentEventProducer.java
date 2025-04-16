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
                log.info("Payment completed event sent successfully for paymentId: {}", payment.getPaymentId());
                log.debug("Message sent to topic: {}, partition: {}, offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send payment completed event for paymentId: {}", payment.getPaymentId(), ex);
            }
        });
    }

    public void sendPaymentCancelledEvent(Payment payment) {
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
                log.info("Payment cancelled event sent successfully for paymentId: {}", payment.getPaymentId());
                log.debug("Message sent to topic: {}, partition: {}, offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send payment cancelled event for paymentId: {}", payment.getPaymentId(), ex);
            }
        });
    }
}