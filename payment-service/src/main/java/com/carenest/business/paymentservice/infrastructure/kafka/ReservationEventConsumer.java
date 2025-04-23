package com.carenest.business.paymentservice.infrastructure.kafka;

import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationCreatedEvent;
import com.carenest.business.common.kafka.KafkaRetryConfig.IdempotentMessageProcessor;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.service.PaymentService;
import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
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
public class ReservationEventConsumer {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final IdempotentMessageProcessor idempotentProcessor;

    @Transactional
    @KafkaListener(
            topics = "reservation-created",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeReservationCreatedEvent(ConsumerRecord<String, ReservationCreatedEvent> record, Acknowledgment acknowledgment) {
        ReservationCreatedEvent event = record.value();
        String messageKey = generateMessageKey(record);

        log.info("예약 생성 이벤트 수신: reservationId={}, guardianId={}, 금액={}",
                event.getReservationId(), event.getGuardianId(), event.getTotalAmount());

        try {
            // 이벤트 중복 처리 방지
            if (!idempotentProcessor.processIfNotDuplicate(messageKey)) {
                log.info("이미 처리된a 예약 이벤트: reservationId={}, 중복 메시지 무시", event.getReservationId());
                acknowledgment.acknowledge();
                return;
            }

            Optional<Payment> existingPayment = paymentRepository.findByReservationId(event.getReservationId());
            if (existingPayment.isPresent()) {
                log.info("이미 해당 예약에 대한 결제가 존재합니다: paymentId={}, 상태={}",
                        existingPayment.get().getPaymentId(), existingPayment.get().getStatus());
                acknowledgment.acknowledge();
                return;
            }

            // 자동으로 결제 생성 프로세스 시작
            PaymentCreateRequest request = new PaymentCreateRequest();
            request.setReservationId(event.getReservationId());
            request.setCaregiverId(event.getCaregiverId());
            request.setAmount(event.getTotalAmount());
            request.setPaymentMethod("CARD"); // 기본값
            request.setPaymentGateway("TOSS_PAYMENTS"); // 기본값

            paymentService.createPayment(request, event.getGuardianId());
            log.info("예약에 대한 결제 생성 완료: reservationId={}", event.getReservationId());

            idempotentProcessor.markAsProcessed(messageKey);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("예약 생성 이벤트 처리 중 오류 발생: reservationId={}, error={}",
                    event.getReservationId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @KafkaListener(
            topics = "reservation-cancelled",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeReservationCancelledEvent(ConsumerRecord<String, ReservationCancelledEvent> record, Acknowledgment acknowledgment) {
        ReservationCancelledEvent event = record.value();
        String messageKey = generateMessageKey(record);

        log.info("예약 취소 이벤트 수신: reservationId={}, paymentId={}",
                event.getReservationId(), event.getPaymentId());

        try {
            // 멱등성 처리
            if (!idempotentProcessor.processIfNotDuplicate(messageKey)) {
                log.info("이미 처리된 예약 취소 이벤트: reservationId={}, 중복 메시지 무시", event.getReservationId());
                acknowledgment.acknowledge();
                return;
            }

            if (event.getPaymentId() == null) {
                log.error("결제 취소 실패: 예약 취소 이벤트에 paymentId가 없음, reservationId={}", event.getReservationId());
                acknowledgment.acknowledge();
                return;
            }

            Optional<Payment> paymentOpt = paymentRepository.findById(event.getPaymentId());
            if (paymentOpt.isEmpty()) {
                log.error("결제 정보를 찾을 수 없음: paymentId={}", event.getPaymentId());
                acknowledgment.acknowledge();
                return;
            }

            Payment payment = paymentOpt.get();
            // 이미 취소된 결제가 아닌 경우에만 처리
            if (payment.getStatus() != PaymentStatus.CANCELLED && payment.getStatus() != PaymentStatus.REFUNDED) {
                log.info("예약 취소로 인한 결제 취소 처리: paymentId={}, 상태={}",
                        payment.getPaymentId(), payment.getStatus());
                paymentService.cancelPayment(event.getPaymentId(), event.getCancelReason());
                log.info("결제 취소 처리 완료: paymentId={}", event.getPaymentId());
            } else {
                log.info("이미 취소된 결제입니다: paymentId={}, 상태={}",
                        payment.getPaymentId(), payment.getStatus());
            }

            // 멱등성 처리
            idempotentProcessor.markAsProcessed(messageKey);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("예약 취소 이벤트 처리 중 오류 발생: reservationId={}, error={}",
                    event.getReservationId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(
            topics = "reservation-created.dlq",
            groupId = "payment-service-dlq-group",
            containerFactory = "deadLetterListenerContainerFactory")
    public void processDLQReservationCreatedEvent(ConsumerRecord<String, ReservationCreatedEvent> record, Acknowledgment acknowledgment) {
        ReservationCreatedEvent event = record.value();

        log.warn("DLQ에서 예약 생성 이벤트 처리: reservationId={}, 재처리 시도",
                event.getReservationId());

        try {
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류: reservationId={}, error={}",
                    event.getReservationId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = "reservation-cancelled.dlq",
            groupId = "payment-service-dlq-group",
            containerFactory = "deadLetterListenerContainerFactory")
    public void processDLQReservationCancelledEvent(ConsumerRecord<String, ReservationCancelledEvent> record, Acknowledgment acknowledgment) {
        ReservationCancelledEvent event = record.value();

        log.warn("DLQ에서 예약 취소 이벤트 처리: reservationId={}, paymentId={}, 재처리 시도",
                event.getReservationId(), event.getPaymentId());

        try {
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류: reservationId={}, error={}",
                    event.getReservationId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    private <T> String generateMessageKey(ConsumerRecord<String, T> record) {
        return record.topic() + "-" + record.partition() + "-" + record.offset();
    }
}