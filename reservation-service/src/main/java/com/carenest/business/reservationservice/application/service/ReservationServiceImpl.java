package com.carenest.business.reservationservice.application.service;

import com.carenest.business.reservationservice.application.dto.request.ReservationCreateRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationUpdateRequest;
import com.carenest.business.reservationservice.application.dto.response.ReservationResponse;
import com.carenest.business.reservationservice.domain.exception.*;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.repository.ReservationHistoryRepository;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.carenest.business.reservationservice.domain.service.ReservationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;
    private final ReservationDomainService reservationDomainService;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Reservation reservation = new Reservation(
                request.getGuardianId(),
                request.getCaregiverId(),
                request.getPatientName(),
                request.getPatientAge(),
                request.getPatientGender(),
                request.getPatientCondition(),
                request.getCareAddress(),
                request.getStartedAt(),
                request.getEndedAt(),
                request.getServiceType(),
                request.getServiceRequests(),
                request.getTotalAmount(),
                null
        );

        if (!reservationDomainService.validateReservationTime(reservation)) {
            throw new InvalidReservationTimeException();
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        reservationDomainService.createReservationHistory(savedReservation);

        return new ReservationResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservations(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        // TODO: 사용자가 보호자인지 간병인인지에 따라 다른 조회 로직 구현하기
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(UUID reservationId, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        // 예약 상태 확인
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT &&
                reservation.getStatus() != ReservationStatus.PENDING_ACCEPTANCE) {
            throw new InvalidReservationStatusException();
        }

        // TODO: 예약 정보 업데이트 로직 구현하기

        reservationDomainService.createReservationHistory(reservation);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse acceptReservation(UUID reservationId, String caregiverNote) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservation.isAcceptable()) {
            throw new InvalidReservationStatusException();
        }

        reservation.acceptByCaregiver();
        reservationRepository.save(reservation);

        reservationDomainService.createReservationHistory(reservation);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse rejectReservation(UUID reservationId, String rejectionReason, String suggestedAlternative) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservationDomainService.canRejectReservation(reservationId)) {
            throw new CannotRejectReservationException();
        }

        reservation.rejectByCaregiver(rejectionReason);
        reservationRepository.save(reservation);

        reservationDomainService.createReservationHistory(reservation);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(UUID reservationId, String cancelReason, String cancellationNote) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservationDomainService.canCancelReservation(reservationId)) {
            throw new CannotCancelReservationException();
        }

        reservation.cancelByGuardian(cancelReason);
        reservationRepository.save(reservation);

        reservationDomainService.createReservationHistory(reservation);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        // TODO: ReservationHistory 엔티티를 페이징하여 조회하는 로직 구현하기
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservationHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        // TODO: 특정 사용자의 ReservationHistory 엔티티를 페이징하여 조회하는 로직 구현하기
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        return reservations.map(ReservationResponse::new);
    }
}