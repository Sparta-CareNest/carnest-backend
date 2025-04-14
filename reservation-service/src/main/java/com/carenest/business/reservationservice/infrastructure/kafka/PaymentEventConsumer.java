package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.payment.PaymentCancelledEvent;
import com.carenest.business.common.event.payment.PaymentCompletedEvent;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "payment-completed", groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신: paymentId={}, reservationId={}",
                event.getPaymentId(), event.getReservationId());

        try {
            // 결제 완료 시 예약 상태 업데이트 및 결제 정보 연결
            reservationService.linkPayment(event.getReservationId(), event.getPaymentId());
            log.info("결제 정보 연결 완료: reservationId={}, paymentId={}",
                    event.getReservationId(), event.getPaymentId());
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional
    @KafkaListener(topics = "payment-cancelled", groupId = "reservation-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentCancelledEvent(PaymentCancelledEvent event) {
        log.info("결제 취소 이벤트 수신: paymentId={}, reservationId={}",
                event.getPaymentId(), event.getReservationId());

        try {
            // 결제 취소 시 예약도 취소 상태로 변경
            Optional<Reservation> optionalReservation = reservationRepository.findById(event.getReservationId());

            if (optionalReservation.isPresent()) {
                Reservation reservation = optionalReservation.get();

                // 예약이 이미 취소 상태가 아닌 경우에만 처리
                if (reservation.getStatus() != com.carenest.business.reservationservice.domain.model.ReservationStatus.CANCELLED) {
                    reservationService.cancelReservation(
                            event.getReservationId(),
                            "결제 취소로 인한 자동 예약 취소",
                            "결제 취소 이유: " + event.getCancelReason()
                    );
                    log.info("결제 취소로 인한 예약 취소 완료: reservationId={}", event.getReservationId());
                } else {
                    log.info("이미 취소된 예약입니다: reservationId={}", event.getReservationId());
                }
            } else {
                log.warn("결제 취소 이벤트 처리 중 예약 정보를 찾을 수 없음: reservationId={}", event.getReservationId());
            }
        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}