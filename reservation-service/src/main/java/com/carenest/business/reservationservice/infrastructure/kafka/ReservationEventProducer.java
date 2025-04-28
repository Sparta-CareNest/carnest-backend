package com.carenest.business.reservationservice.infrastructure.kafka;

import com.carenest.business.common.event.reservation.ReservationCancelledEvent;
import com.carenest.business.common.event.reservation.ReservationCreatedEvent;
import com.carenest.business.common.event.reservation.ReservationStatusChangedEvent;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReservationCreatedEvent(Reservation reservation) {
        if (reservation == null) {
            log.error("예약 생성 이벤트 발행 실패: reservation 객체가 null입니다");
            return;
        }

        if (reservation.getReservationId() == null) {
            log.error("예약 생성 이벤트 발행 실패: reservationId가 null입니다");
            return;
        }

        ReservationCreatedEvent event = ReservationCreatedEvent.builder()
                .reservationId(reservation.getReservationId())
                .guardianId(reservation.getGuardianId())
                .caregiverId(reservation.getCaregiverId())
                .patientName(reservation.getPatientName())
                .startedAt(reservation.getStartedAt())
                .endedAt(reservation.getEndedAt())
                .serviceType(reservation.getServiceType().name())
                .totalAmount(reservation.getTotalAmount())
                .serviceFee(reservation.getServiceFee())
                .build();

        sendKafkaMessage(
                KafkaTopic.RESERVATION_CREATED.getTopicName(),
                reservation.getReservationId(),
                event,
                "예약 생성 이벤트"
        );
    }

    public void sendReservationCancelledEvent(Reservation reservation) {
        if (reservation == null) {
            log.error("예약 취소 이벤트 발행 실패: reservation 객체가 null입니다");
            return;
        }

        if (reservation.getReservationId() == null) {
            log.error("예약 취소 이벤트 발행 실패: reservationId가 null입니다");
            return;
        }

        if (reservation.getPaymentId() == null) {
            log.warn("예약 취소 이벤트 발행 생략: 결제 정보 없음 (reservationId={})", reservation.getReservationId());
            return;
        }

        if (reservation.getGuardianId() == null || reservation.getCaregiverId() == null ||
                reservation.getTotalAmount() == null) {
            log.error("예약 취소 이벤트 발행 실패: 필수 필드가 null입니다 (reservationId={})",
                    reservation.getReservationId());
            return;
        }

        ReservationCancelledEvent event = ReservationCancelledEvent.builder()
                .reservationId(reservation.getReservationId())
                .guardianId(reservation.getGuardianId())
                .caregiverId(reservation.getCaregiverId())
                .paymentId(reservation.getPaymentId())
                .amount(reservation.getTotalAmount())
                .cancelReason(reservation.getCancelReason() != null ?
                        reservation.getCancelReason() : "취소 사유 없음")
                .build();

        sendKafkaMessage(
                KafkaTopic.RESERVATION_CANCELLED.getTopicName(),
                reservation.getReservationId(),
                event,
                "예약 취소 이벤트"
        );
    }

    public void sendReservationStatusChangedEvent(Reservation reservation, ReservationStatus previousStatus) {
        if (reservation == null) {
            log.error("예약 상태 변경 이벤트 발행 실패: reservation 객체가 null입니다");
            return;
        }

        if (reservation.getReservationId() == null) {
            log.error("예약 상태 변경 이벤트 발행 실패: reservationId가 null입니다");
            return;
        }

        if (reservation.getStatus() == null || previousStatus == null) {
            log.error("예약 상태 변경 이벤트 발행 실패: 현재 상태 또는 이전 상태가 null입니다 (reservationId={})",
                    reservation.getReservationId());
            return;
        }

        if (reservation.getGuardianId() == null || reservation.getCaregiverId() == null) {
            log.error("예약 상태 변경 이벤트 발행 실패: 필수 필드가 null입니다 (reservationId={})",
                    reservation.getReservationId());
            return;
        }

        log.info("예약 상태 변경 이벤트 발행 시작: reservationId={}, 상태 변경={}→{}",
                reservation.getReservationId(), previousStatus, reservation.getStatus());

        ReservationStatusChangedEvent event = ReservationStatusChangedEvent.builder()
                .reservationId(reservation.getReservationId())
                .guardianId(reservation.getGuardianId())
                .caregiverId(reservation.getCaregiverId())
                .previousStatus(previousStatus.name())
                .newStatus(reservation.getStatus().name())
                .reason(getStatusChangeReason(reservation, previousStatus))
                .build();

        sendKafkaMessage(
                KafkaTopic.RESERVATION_STATUS_CHANGED.getTopicName(),
                reservation.getReservationId(),
                event,
                String.format("예약 상태 변경 이벤트 (%s→%s)", previousStatus, reservation.getStatus())
        );
    }

    private <T> void sendKafkaMessage(String topic, UUID id, T payload, String eventType) {
        Assert.notNull(topic, "토픽은 null일 수 없습니다");
        Assert.notNull(id, "ID는 null일 수 없습니다");
        Assert.notNull(payload, "Payload는 null일 수 없습니다");

        String key = id.toString();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("{} 발행 성공: id={}", eventType, id);
                log.debug("메시지 발행 완료: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("{} 발행 실패: id={}, 오류={}", eventType, id, ex.getMessage());
                log.debug("Stack trace:", ex);
            }
        });
    }

    private String getStatusChangeReason(Reservation reservation, ReservationStatus previousStatus) {
        if (reservation.getStatus() == null) {
            return "알 수 없는 상태 변경";
        }

        switch (reservation.getStatus()) {
            case PENDING_ACCEPTANCE:
                return "결제 완료";
            case CONFIRMED:
                return "간병인 수락 완료";
            case REJECTED:
                return reservation.getRejectionReason() != null ?
                        reservation.getRejectionReason() : "거절 사유 없음";
            case CANCELLED:
                return reservation.getCancelReason() != null ?
                        reservation.getCancelReason() : "취소 사유 없음";
            case COMPLETED:
                return "서비스 완료";
            default:
                return "상태 변경";
        }
    }
}