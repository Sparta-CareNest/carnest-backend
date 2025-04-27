package com.carenest.business.paymentservice.infrastructure.kafka;

import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationCreatedEvent;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.service.PaymentService;
import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @Transactional
    @KafkaListener(
            topics = "reservation-created",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeReservationCreatedEvent(ReservationCreatedEvent event) {
        log.info("예약 생성 이벤트 수신: reservationId={}, guardianId={}, 금액={}",
                event.getReservationId(), event.getGuardianId(), event.getTotalAmount());

        try {
            // 해당 예약에 대한 결제가 존재하는지 확인
            Optional<Payment> existingPayment = paymentRepository.findByReservationId(event.getReservationId());

            if (existingPayment.isPresent()) {
                log.info("이미 해당 예약에 대한 결제가 존재합니다: paymentId={}, 상태={}",
                        existingPayment.get().getPaymentId(), existingPayment.get().getStatus());
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

        } catch (Exception e) {
            log.error("예약 생성 이벤트 처리 중 오류 발생", e);
        }
    }

    @Transactional
    @KafkaListener(
            topics = "reservation-cancelled",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeReservationCancelledEvent(ReservationCancelledEvent event) {
        log.info("예약 취소 이벤트 수신: reservationId={}, paymentId={}",
                event.getReservationId(), event.getPaymentId());

        try {
            if (event.getPaymentId() != null) {
                Optional<Payment> paymentOpt = paymentRepository.findById(event.getPaymentId());

                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    // 이미 취소된 결제가 아닌 경우에만 처리
                    if (payment.getStatus() != PaymentStatus.CANCELLED &&
                            payment.getStatus() != PaymentStatus.REFUNDED) {
                        log.info("예약 취소로 인한 결제 취소 처리: paymentId={}, 상태={}",
                                payment.getPaymentId(), payment.getStatus());
                        paymentService.cancelPayment(event.getPaymentId(), event.getCancelReason());
                        log.info("결제 취소 처리 완료: paymentId={}", event.getPaymentId());
                    } else {
                        log.info("이미 취소된 결제입니다: paymentId={}, 상태={}",
                                payment.getPaymentId(), payment.getStatus());
                    }
                } else {
                    log.warn("결제 정보를 찾을 수 없음: paymentId={}", event.getPaymentId());
                }
            } else {
                log.warn("예약 취소 이벤트에 결제 ID가 없음: reservationId={}", event.getReservationId());
            }
        } catch (Exception e) {
            log.error("예약 취소 이벤트 처리 중 오류 발생", e);
        }
    }
}