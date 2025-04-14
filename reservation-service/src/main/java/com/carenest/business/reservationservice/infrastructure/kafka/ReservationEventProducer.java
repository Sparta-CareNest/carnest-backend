package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationStatusChangedEvent;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReservationCancelledEvent(Reservation reservation) {
        if (reservation.getPaymentId() == null) {
            log.warn("예약 취소 이벤트 발행 생략: 결제 정보 없음 (reservationId={})", reservation.getReservationId());
            return;
        }

        ReservationCancelledEvent event = ReservationCancelledEvent.builder()
                .reservationId(reservation.getReservationId())
                .guardianId(reservation.getGuardianId())
                .caregiverId(reservation.getCaregiverId())
                .paymentId(reservation.getPaymentId())
                .amount(reservation.getTotalAmount())
                .cancelReason(reservation.getCancelReason())
                .build();

        String key = reservation.getReservationId().toString();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("reservation-cancelled", key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("예약 취소 이벤트 발행 성공: reservationId={}", reservation.getReservationId());
                log.debug("메시지 발행 완료: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("예약 취소 이벤트 발행 실패: reservationId={}", reservation.getReservationId(), ex);
            }
        });
    }

    public void sendReservationStatusChangedEvent(Reservation reservation, ReservationStatus previousStatus) {
        ReservationStatusChangedEvent event = ReservationStatusChangedEvent.builder()
                .reservationId(reservation.getReservationId())
                .guardianId(reservation.getGuardianId())
                .caregiverId(reservation.getCaregiverId())
                .previousStatus(previousStatus.name())
                .newStatus(reservation.getStatus().name())
                .reason(getStatusChangeReason(reservation, previousStatus))
                .build();

        String key = reservation.getReservationId().toString();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("reservation-status-changed", key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("예약 상태 변경 이벤트 발행 성공: reservationId={}, {}({}→{})",
                        reservation.getReservationId(),
                        event.getReason(),
                        previousStatus,
                        reservation.getStatus());
            } else {
                log.error("예약 상태 변경 이벤트 발행 실패: reservationId={}", reservation.getReservationId(), ex);
            }
        });
    }

    private String getStatusChangeReason(Reservation reservation, ReservationStatus previousStatus) {
        switch (reservation.getStatus()) {
            case PENDING_ACCEPTANCE:
                return "결제 완료";
            case CONFIRMED:
                return "간병인 수락";
            case REJECTED:
                return reservation.getRejectionReason();
            case CANCELLED:
                return reservation.getCancelReason();
            case COMPLETED:
                return "서비스 완료";
            default:
                return "상태 변경";
        }
    }
}