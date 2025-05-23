package com.carenest.business.paymentservice.infrastructure.kafka;

import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;
import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.paymentservice.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCompletedEvent(Payment payment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", UUID.randomUUID());
            event.put("eventType", "PAYMENT_COMPLETED");
            event.put("timestamp", LocalDateTime.now());
            event.put("paymentId", payment.getPaymentId());
            event.put("reservationId", payment.getReservationId());
            event.put("guardianId", payment.getGuardianId());
            event.put("caregiverId", payment.getCaregiverId());
            event.put("amount", payment.getAmount());
            event.put("paymentMethod", payment.getPaymentMethod());
            event.put("approvalNumber", payment.getApprovalNumber());

            String key = payment.getPaymentId().toString();
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("payment-completed", key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("결제 완료 이벤트 발행 성공: paymentId={}, reservationId={}",
                            payment.getPaymentId(), payment.getReservationId());
                } else {
                    log.error("결제 완료 이벤트 발행 실패: paymentId={}", payment.getPaymentId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("결제 완료 이벤트 생성 중 예외 발생: paymentId={}", payment.getPaymentId(), e);
        }
    }

    public void sendPaymentCancelledEvent(Payment payment) {
        try {
            log.info("결제 취소 이벤트 발행 시작: paymentId={}, reservationId={}",
                    payment.getPaymentId(), payment.getReservationId());

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
                    log.error("결제 취소 이벤트 발행 실패: paymentId={}, reservationId={}, 에러={}",
                            payment.getPaymentId(), payment.getReservationId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("결제 취소 이벤트 생성 중 예외 발생: paymentId={}, 에러={}",
                    payment.getPaymentId(), e.getMessage(), e);
        }
    }

	public void sendCaregiverPendingEvent(Payment payment) {
        try{

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
                    log.info("간병인 승인/거절 이벤트 발행 성공: reservationId={}, caregiverId={}",
                        payment.getReservationId(), payment.getCaregiverId());
                } else {
                    log.error("간병인 승인/거절 이벤트 발행 실패: paymentId={}", payment.getPaymentId(), ex);
                }
            });

        }catch (Exception e){
            log.error("간병인 승인/거절 이벤트 생성 중 예외 발생: paymentId={}, 에러={}",
                payment.getPaymentId(), e.getMessage(), e);
        }
	}
}