package com.carenest.business.paymentservice.infrastructure.kafka;

import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.paymentservice.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "reservation-cancelled",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeReservationCancelledEvent(ReservationCancelledEvent event) {
        log.info("Received reservation cancelled event for reservationId: {}, paymentId: {}",
                event.getReservationId(), event.getPaymentId());

        try {
            if (event.getPaymentId() != null) {
                log.info("Cancelling payment due to reservation cancellation: paymentId={}", event.getPaymentId());
                paymentService.cancelPayment(event.getPaymentId(), event.getCancelReason());
                log.info("Payment successfully cancelled: paymentId={}", event.getPaymentId());
            } else {
                log.warn("No payment ID found in reservation cancelled event: reservationId={}", event.getReservationId());
            }
        } catch (Exception e) {
            log.error("Error processing reservation cancelled event", e);
        }
    }
}