package com.carenest.business.reservationservice.domain.service;

import com.carenest.business.common.model.UserRole;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationHistory;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.repository.ReservationHistoryRepository;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationDomainServiceImpl implements ReservationDomainService {

    private final ReservationRepository reservationRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;

    @Override
    public void createReservationHistory(Reservation reservation) {
        // 이전 이력 가져오기
        Optional<ReservationHistory> lastHistoryOpt = reservationHistoryRepository.findByReservationId(reservation.getReservationId())
                .stream()
                .max((h1, h2) -> h1.getCreatedAt().compareTo(h2.getCreatedAt()));

        ReservationHistory history = new ReservationHistory(reservation);

        // 이전 상태 설정
        if (lastHistoryOpt.isPresent()) {
            history.setPrevStatus(lastHistoryOpt.get().getStatus());
        }

        // TODO: 실제 사용자 정보를 가져오는 로직 필요
        String createdBy = "system";
        UserRole role = UserRole.SYSTEM;

        // 상태에 따라 작성자 설정
        switch (reservation.getStatus()) {
            case PENDING_PAYMENT:
                createdBy = reservation.getGuardianId().toString();
                role = UserRole.GUARDIAN;
                break;
            case CONFIRMED:
            case REJECTED:
                createdBy = reservation.getCaregiverId().toString();
                role = UserRole.CAREGIVER;
                break;
            case CANCELLED:
                createdBy = reservation.getGuardianId().toString();
                role = UserRole.GUARDIAN;
                break;
        }

        // UserRole enum을 직접 전달
        history.setCreatedBy(createdBy, role);
        reservationHistoryRepository.save(history);
    }

    @Override
    public boolean validateReservationTime(Reservation reservation) {
        // 예약 시간이 현재 시간 이후인지 확인
        if (reservation.getStartedAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 시작 시간이 종료 시간보다 이전인지 확인
        if (reservation.getStartedAt().isAfter(reservation.getEndedAt())) {
            return false;
        }

        // 최소 서비스 시간 확인
        long hours = ChronoUnit.HOURS.between(reservation.getStartedAt(), reservation.getEndedAt());
        if (hours < 1) {
            return false;
        }

        // 서비스 타입에 따른 검증
        if (reservation.getServiceType() == com.carenest.business.reservationservice.domain.model.ServiceType.DAY) {
            // 일 단위 서비스인 경우 최소 24시간 확인
            if (hours < 24) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canCancelReservation(UUID reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isEmpty()) {
            return false;
        }

        Reservation reservation = reservationOpt.get();
        ReservationStatus status = reservation.getStatus();

        // 결제 대기, 수락 대기, 확정 상태일 때만 취소 가능
        if (status != ReservationStatus.PENDING_PAYMENT &&
                status != ReservationStatus.PENDING_ACCEPTANCE &&
                status != ReservationStatus.CONFIRMED) {
            return false;
        }

        // 서비스 시작 24시간 이내에는 취소 불가
        if (status == ReservationStatus.CONFIRMED &&
                reservation.getStartedAt().isBefore(LocalDateTime.now().plusHours(24))) {
            return false;
        }

        return true;
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

    @Override
    public boolean canCompleteReservation(UUID reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isEmpty()) {
            return false;
        }

        Reservation reservation = reservationOpt.get();

        // 확정 상태이고 종료 시간이 현재 시간 이전인 경우에만 완료 가능
        return reservation.getStatus() == ReservationStatus.CONFIRMED &&
                reservation.getEndedAt().isBefore(LocalDateTime.now());
    }

    @Override
    public boolean checkOverlappingReservations(UUID caregiverId, LocalDateTime startTime, LocalDateTime endTime) {
        // 해당 간병인의 같은 시간대 예약이 있는지 확인
        return reservationRepository.findByCaregiverId(caregiverId)
                .stream()
                .anyMatch(reservation -> {
                    // 이미 취소, 거절된 예약 제외
                    if (reservation.getStatus() == ReservationStatus.CANCELLED ||
                            reservation.getStatus() == ReservationStatus.REJECTED) {
                        return false;
                    }

                    // 새 예약이 기존 예약과 겹치는지 확인
                    return !(endTime.isBefore(reservation.getStartedAt()) ||
                            startTime.isAfter(reservation.getEndedAt()));
                });
    }
}