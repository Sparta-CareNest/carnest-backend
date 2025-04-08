package com.carenest.business.reservationservice.domain.service;

import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationHistory;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.repository.ReservationHistoryRepository;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationDomainServiceImpl implements ReservationDomainService {

    private final ReservationRepository reservationRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;

    @Override
    public void createReservationHistory(Reservation reservation) {
        ReservationHistory history = new ReservationHistory(reservation);
        reservationHistoryRepository.save(history);
    }

    @Override
    public boolean validateReservationTime(Reservation reservation) {
        // 예약 시간이 현재 시간 이후인지 확인
        return reservation.getStartedAt().isAfter(LocalDateTime.now());
    }

    @Override
    public boolean canCancelReservation(UUID reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isEmpty()) {
            return false;
        }

        Reservation reservation = reservationOpt.get();
        ReservationStatus status = reservation.getStatus();

        // 결제 대기, 수락 대기일 때만 취소 가능
        return status == ReservationStatus.PENDING_PAYMENT ||
                status == ReservationStatus.PENDING_ACCEPTANCE;
    }

    @Override
    public boolean canRejectReservation(UUID reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isEmpty()) {
            return false;
        }

        Reservation reservation = reservationOpt.get();

        // 수락 대기 상태일 때만 거절 가능
        return reservation.getStatus() == ReservationStatus.PENDING_ACCEPTANCE;
    }
}