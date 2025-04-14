package com.carenest.business.reservationservice.application.service;

import com.carenest.business.reservationservice.application.dto.request.ReservationCreateRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationSearchRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationUpdateRequest;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.domain.model.PaymentStatus;
import com.carenest.business.reservationservice.domain.repository.ReservationRepository;
import com.carenest.business.reservationservice.domain.service.ReservationDomainService;
import com.carenest.business.reservationservice.exception.*;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.CaregiverDetailResponseDto;
import com.carenest.business.reservationservice.infrastructure.kafka.ReservationEventProducer;
import com.carenest.business.reservationservice.infrastructure.service.ExternalServiceClient;
import com.carenest.business.reservationservice.presentation.dto.mapper.ReservationMapper;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDomainService reservationDomainService;
    private final ReservationMapper reservationMapper;
    private final ExternalServiceClient externalServiceClient;
    private final ReservationEventProducer reservationEventProducer;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        log.info("예약 생성 시작: guardianId={}, caregiverId={}", request.getGuardianId(), request.getCaregiverId());

        // 간병인 정보를 조회해서 가격 검증
        try {
            CaregiverDetailResponseDto caregiverDetail = externalServiceClient.getCaregiverDetail(request.getCaregiverId());
            log.info("간병인 정보 조회 성공: caregiverId={}, 일일가격={}, 시간당가격={}",
                    caregiverDetail.getId(), caregiverDetail.getPricePerDay(), caregiverDetail.getPricePerHour());
        } catch (Exception e) {
            log.warn("간병인 정보 조회 실패. 기본 정보로 진행합니다.", e);
        }

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

        // 보호자에게 예약 생성 알림 전송
        try {
            externalServiceClient.sendReservationCreatedNotification(
                    savedReservation.getGuardianId(),
                    String.format("예약이 생성되었습니다. 예약번호: %s", savedReservation.getReservationId())
            );
        } catch (Exception e) {
            log.error("예약 생성 알림 전송 실패", e);
            // 알림 전송 실패는 예약 생성에 영향X
        }

        log.info("예약 생성 완료: reservationId={}", savedReservation.getReservationId());
        return reservationMapper.toDto(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId) {
        log.info("예약 상세 조회 요청: reservationId={}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        log.info("예약 상세 조회 완료: reservationId={}", reservationId);
        return reservationMapper.toDto(reservation);
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

        log.info("예약 목록 조회: startDate={}, endDate={}", startDate, endDate);
        Page<Reservation> reservations = reservationRepository.findByStartedAtBetween(startDate, endDate, pageable);

        log.info("예약 목록 조회 완료: count={}", reservations.getTotalElements());
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> searchReservations(ReservationSearchRequest request, Pageable pageable) {
        log.info("예약 검색 요청: guardianId={}, caregiverId={}, status={}",
                request.getGuardianId(), request.getCaregiverId(), request.getStatus());

        Page<Reservation> reservations = reservationRepository.findBySearchCriteria(
                request.getGuardianId(),
                request.getCaregiverId(),
                request.getPatientName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                pageable
        );

        log.info("예약 검색 완료: count={}", reservations.getTotalElements());
        return reservations.map(reservationMapper::toDto);
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

        log.info("사용자 예약 목록 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 보호자 ID로 조회
        Page<Reservation> reservationsByGuardian = reservationRepository.findByGuardianIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        if (!reservationsByGuardian.isEmpty()) {
            log.info("보호자 예약 목록 조회 완료: userId={}, count={}", userId, reservationsByGuardian.getTotalElements());
            return reservationsByGuardian.map(reservationMapper::toDto);
        }

        // 간병인 ID로 조회
        Page<Reservation> reservationsByCaregiver = reservationRepository.findByCaregiverIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        log.info("간병인 예약 목록 조회 완료: userId={}, count={}", userId, reservationsByCaregiver.getTotalElements());
        return reservationsByCaregiver.map(reservationMapper::toDto);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(UUID reservationId, ReservationUpdateRequest request) {
        log.info("예약 수정 요청: reservationId={}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        // 예약 상태 확인
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT &&
                reservation.getStatus() != ReservationStatus.PENDING_ACCEPTANCE) {
            log.error("예약 수정 불가: reservationId={}, currentStatus={}",
                    reservationId, reservation.getStatus());
            throw new InvalidReservationStatusException();
        }

        // 예약 정보 업데이트
        boolean updated = false;

        if (request.getPatientName() != null) {
            reservation.updatePatientName(request.getPatientName());
            updated = true;
        }
        if (request.getPatientAge() != null) {
            reservation.updatePatientAge(request.getPatientAge());
            updated = true;
        }
        if (request.getPatientGender() != null) {
            reservation.updatePatientGender(request.getPatientGender());
            updated = true;
        }
        if (request.getPatientCondition() != null) {
            reservation.updatePatientCondition(request.getPatientCondition());
            updated = true;
        }
        if (request.getCareAddress() != null) {
            reservation.updateCareAddress(request.getCareAddress());
            updated = true;
        }
        if (request.getStartedAt() != null && request.getEndedAt() != null) {
            reservation.updateServicePeriod(request.getStartedAt(), request.getEndedAt());

            if (!reservationDomainService.validateReservationTime(reservation)) {
                throw new InvalidReservationTimeException();
            }
            updated = true;
        }
        if (request.getServiceRequests() != null) {
            reservation.updateServiceRequests(request.getServiceRequests());
            updated = true;
        }

        if (updated) {
            reservation.setUpdatedAt(LocalDateTime.now());
            reservation.changeStatusToPendingAcceptance();

            Reservation updatedReservation = reservationRepository.save(reservation);
            reservationDomainService.createReservationHistory(updatedReservation);

            // 간병인에게 예약 수정 알림 전송
            try {
                externalServiceClient.sendReservationCreatedNotification(
                        updatedReservation.getCaregiverId(),
                        String.format("예약이 수정되었습니다. 예약번호: %s", updatedReservation.getReservationId())
                );
            } catch (Exception e) {
                log.error("예약 수정 알림 전송 실패", e);
            }

            log.info("예약 수정 완료: reservationId={}", reservationId);
            return reservationMapper.toDto(updatedReservation);
        } else {
            log.info("예약 수정 내용이 없음: reservationId={}", reservationId);
            return reservationMapper.toDto(reservation);
        }
    }

    @Override
    @Transactional
    public ReservationResponse acceptReservation(UUID reservationId, String caregiverNote) {
        log.info("예약 수락 요청: reservationId={}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        if (!reservation.isAcceptable()) {
            log.error("예약 수락 불가: reservationId={}, currentStatus={}",
                    reservationId, reservation.getStatus());
            throw new InvalidReservationStatusException();
        }

        reservation.acceptByCaregiver(caregiverNote);
        reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(reservation);

        // 예약 수락 알림 발송
        try {
            // 보호자에게 알림
            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getGuardianId(),
                    String.format("예약이 간병인에 의해 수락되었습니다. 예약번호: %s, 간병인: %s",
                            reservation.getReservationId(), reservation.getCaregiverName())
            );
        } catch (Exception e) {
            log.error("예약 수락 알림 전송 실패", e);
        }

        log.info("예약 수락 완료: reservationId={}", reservationId);
        return reservationMapper.toDto(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse rejectReservation(UUID reservationId, String rejectionReason, String suggestedAlternative) {
        log.info("예약 거절 요청: reservationId={}, reason={}", reservationId, rejectionReason);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        if (!reservationDomainService.canRejectReservation(reservationId)) {
            log.error("예약 거절 불가: reservationId={}, currentStatus={}",
                    reservationId, reservation.getStatus());
            throw new CannotRejectReservationException();
        }

        ReservationStatus previousStatus = reservation.getStatus();
        reservation.rejectByCaregiver(rejectionReason);
        reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(reservation);

        // 상태 변경 이벤트 발행
        reservationEventProducer.sendReservationStatusChangedEvent(reservation, previousStatus);

        // 예약 거절 알림 발송
        try {
            // 보호자에게 알림
            String message = String.format("예약이 간병인에 의해 거절되었습니다. 예약번호: %s, 거절사유: %s",
                    reservation.getReservationId(), rejectionReason);

            if (suggestedAlternative != null && !suggestedAlternative.isEmpty()) {
                message += String.format(", 대안 제안: %s", suggestedAlternative);
            }

            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getGuardianId(), message);
        } catch (Exception e) {
            log.error("예약 거절 알림 전송 실패", e);
        }

        log.info("예약 거절 완료: reservationId={}", reservationId);
        return reservationMapper.toDto(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(UUID reservationId, String cancelReason, String cancellationNote) {
        log.info("예약 취소 요청: reservationId={}, reason={}", reservationId, cancelReason);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        if (!reservationDomainService.canCancelReservation(reservationId)) {
            log.error("예약 취소 불가: reservationId={}, status={}", reservationId, reservation.getStatus());
            throw new CannotCancelReservationException();
        }

        ReservationStatus previousStatus = reservation.getStatus();
        reservation.cancelByGuardian(cancelReason);

        // 저장 전에 Kafka 이벤트 발행 (결제 취소 요청을 위해)
        if (reservation.getPaymentStatus() == PaymentStatus.PAID && reservation.getPaymentId() != null) {
            reservationEventProducer.sendReservationCancelledEvent(reservation);
            log.info("예약 취소 이벤트 발행: reservationId={}, paymentId={}",
                    reservation.getReservationId(), reservation.getPaymentId());
        }

        reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(reservation);

        // 상태 변경 이벤트 발행
        reservationEventProducer.sendReservationStatusChangedEvent(reservation, previousStatus);

        // 예약 취소 알림 발송
        try {
            // 보호자에게 알림
            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getGuardianId(),
                    String.format("예약이 취소되었습니다. 예약번호: %s, 취소사유: %s",
                            reservation.getReservationId(), cancelReason)
            );

            // 간병인에게 알림
            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getCaregiverId(),
                    String.format("예약이 취소되었습니다. 예약번호: %s",
                            reservation.getReservationId())
            );
        } catch (Exception e) {
            log.error("예약 취소 알림 전송 실패", e);
        }

        log.info("예약 취소 완료: reservationId={}", reservationId);
        return reservationMapper.toDto(reservation);
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

        log.info("예약 이력 조회: startDate={}, endDate={}", startDate, endDate);

        Page<Reservation> reservations = reservationRepository.findByStartedAtBetween(startDate, endDate, pageable);

        log.info("예약 이력 조회 완료: count={}", reservations.getTotalElements());
        return reservations.map(reservationMapper::toDto);
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

        log.info("사용자 예약 이력 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 보호자 ID로 조회
        Page<Reservation> reservationsByGuardian = reservationRepository.findByGuardianIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        if (!reservationsByGuardian.isEmpty()) {
            log.info("보호자 예약 이력 조회 완료: userId={}, count={}", userId, reservationsByGuardian.getTotalElements());
            return reservationsByGuardian.map(reservationMapper::toDto);
        }

        // 간병인 ID로 조회
        Page<Reservation> reservationsByCaregiver = reservationRepository.findByCaregiverIdAndStartedAtBetween(
                userId, startDate, endDate, pageable);

        log.info("간병인 예약 이력 조회 완료: userId={}, count={}", userId, reservationsByCaregiver.getTotalElements());
        return reservationsByCaregiver.map(reservationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByStatus(ReservationStatus status, Pageable pageable) {
        log.info("상태별 예약 목록 조회: status={}", status);

        Page<Reservation> reservations = reservationRepository.findByStatus(status, pageable);

        log.info("상태별 예약 목록 조회 완료: status={}, count={}", status, reservations.getTotalElements());
        return reservations.map(reservationMapper::toDto);
    }

    @Override
    @Transactional
    public ReservationResponse completeReservation(UUID reservationId) {
        log.info("예약 완료 요청: reservationId={}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            log.error("예약 완료 불가: reservationId={}, currentStatus={}",
                    reservationId, reservation.getStatus());
            throw new InvalidReservationStatusException();
        }

        ReservationStatus previousStatus = reservation.getStatus();
        reservation.completeService();
        reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(reservation);

        // 상태 변경 이벤트 발행
        reservationEventProducer.sendReservationStatusChangedEvent(reservation, previousStatus);

        // 예약 완료 알림 발송
        try {
            // 보호자에게 알림
            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getGuardianId(),
                    String.format("서비스가 완료되었습니다. 예약번호: %s", reservation.getReservationId())
            );

            // 간병인에게 알림
            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getCaregiverId(),
                    String.format("서비스가 완료되었습니다. 예약번호: %s", reservation.getReservationId())
            );

            // 정산 완료 알림은 정산 시스템에서 처리
        } catch (Exception e) {
            log.error("예약 완료 알림 전송 실패", e);
        }

        log.info("예약 완료 처리 완료: reservationId={}", reservationId);
        return reservationMapper.toDto(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse linkPayment(UUID reservationId, UUID paymentId) {
        log.info("결제 정보 연결 요청: reservationId={}, paymentId={}", reservationId, paymentId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 정보를 찾을 수 없음: reservationId={}", reservationId);
                    return new ReservationNotFoundException();
                });

        if (reservation.getPaymentId() != null) {
            log.error("이미 결제가 처리된 예약: reservationId={}, existingPaymentId={}",
                    reservationId, reservation.getPaymentId());
            throw new PaymentAlreadyProcessedException();
        }

        ReservationStatus previousStatus = reservation.getStatus();
        reservation.linkPayment(paymentId);
        reservation.changeStatusToPendingAcceptance();

        Reservation updatedReservation = reservationRepository.save(reservation);
        reservationDomainService.createReservationHistory(updatedReservation);

        // 상태 변경 이벤트 발행
        reservationEventProducer.sendReservationStatusChangedEvent(updatedReservation, previousStatus);

        // 결제 완료 알림 발송
        try {
            // 보호자에게 알림
            externalServiceClient.sendPaymentCompletedNotification(
                    reservation.getGuardianId(),
                    String.format("결제가 완료되었습니다. 예약번호: %s, 결제금액: %s원",
                            reservation.getReservationId(), reservation.getTotalAmount())
            );

            // 간병인에게 알림
            externalServiceClient.sendReservationCreatedNotification(
                    reservation.getCaregiverId(),
                    String.format("새로운 예약이 들어왔습니다. 확인해주세요. 예약번호: %s",
                            reservation.getReservationId())
            );
        } catch (Exception e) {
            log.error("결제 완료 알림 전송 실패", e);
        }

        log.info("결제 정보 연결 완료: reservationId={}, paymentId={}", reservationId, paymentId);
        return reservationMapper.toDto(updatedReservation);
    }
}