package com.carenest.business.reservationservice.application.service;

import com.carenest.business.reservationservice.application.dto.request.ReservationCreateRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationSearchRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationUpdateRequest;
import com.carenest.business.reservationservice.application.dto.response.ReservationResponse;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.model.PaymentStatus;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.carenest.business.reservationservice.domain.service.ReservationDomainService;
import com.carenest.business.reservationservice.exception.*;
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
    private final ReservationDomainService reservationDomainService;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Reservation reservation = new Reservation(
                request.getGuardianId(),
                request.getGuardianName(),
                request.getCaregiverId(),
                request.getCaregiverName(),
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
                request.getServiceFee()
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
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        Page<Reservation> reservations = reservationRepository.findByStartedAtBetween(startDate, endDate, pageable);
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> searchReservations(ReservationSearchRequest request, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findBySearchCriteria(
                request.getGuardianId(),
                request.getCaregiverId(),
                request.getPatientName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                pageable
        );
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservations(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        // 기본 날짜 범위 설정
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        // 보호자 ID로 조회
        Page<Reservation> reservationsByGuardian = reservationRepository.findByGuardianIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        if (!reservationsByGuardian.isEmpty()) {
            return reservationsByGuardian.map(ReservationResponse::new);
        }

        // 간병인 ID로 조회
        Page<Reservation> reservationsByCaregiver = reservationRepository.findByCaregiverIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        return reservationsByCaregiver.map(ReservationResponse::new);
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

        // 예약 정보 업데이트
        if (request.getPatientName() != null) reservation.updatePatientName(request.getPatientName());
        if (request.getPatientAge() != null) reservation.updatePatientAge(request.getPatientAge());
        if (request.getPatientGender() != null) reservation.updatePatientGender(request.getPatientGender());
        if (request.getPatientCondition() != null) reservation.updatePatientCondition(request.getPatientCondition());
        if (request.getCareAddress() != null) reservation.updateCareAddress(request.getCareAddress());
        if (request.getStartedAt() != null && request.getEndedAt() != null) {
            if (!reservationDomainService.validateReservationTime(reservation)) {
                throw new InvalidReservationTimeException();
            }
            reservation.updateServicePeriod(request.getStartedAt(), request.getEndedAt());
        }
        if (request.getServiceRequests() != null) reservation.updateServiceRequests(request.getServiceRequests());

        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.changeStatusToPendingAcceptance();

        Reservation updatedReservation = reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(updatedReservation);

        return new ReservationResponse(updatedReservation);
    }

    @Override
    @Transactional
    public ReservationResponse acceptReservation(UUID reservationId, String caregiverNote) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservation.isAcceptable()) {
            throw new InvalidReservationStatusException();
        }

        reservation.acceptByCaregiver(caregiverNote);
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

        // 결제 상태 업데이트
        if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
            // TODO: 환불 처리 로직 추가하기
        }

        reservationRepository.save(reservation);

        reservationDomainService.createReservationHistory(reservation);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        Page<Reservation> reservations = reservationRepository.findByStartedAtBetween(startDate, endDate, pageable);
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservationHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        // 보호자 ID로 조회
        Page<Reservation> reservationsByGuardian = reservationRepository.findByGuardianIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        if (!reservationsByGuardian.isEmpty()) {
            return reservationsByGuardian.map(ReservationResponse::new);
        }

        // 간병인 ID로 조회
        Page<Reservation> reservationsByCaregiver = reservationRepository.findByCaregiverIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        return reservationsByCaregiver.map(ReservationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByStatus(ReservationStatus status, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByStatus(status, pageable);
        return reservations.map(ReservationResponse::new);
    }

    @Override
    @Transactional
    public ReservationResponse completeReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStatusException();
        }

        reservation.completeService();
        reservationRepository.save(reservation);

        reservationDomainService.createReservationHistory(reservation);

        return new ReservationResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse linkPayment(UUID reservationId, UUID paymentId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (reservation.getPaymentId() != null) {
            throw new PaymentAlreadyProcessedException();
        }

        reservation.linkPayment(paymentId);
        reservation.changeStatusToPendingAcceptance();

        Reservation updatedReservation = reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(updatedReservation);

        return new ReservationResponse(updatedReservation);
    }
}