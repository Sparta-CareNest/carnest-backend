package com.carenest.business.reservationservice.application.service;

import com.carenest.business.reservationservice.application.dto.request.ReservationCreateRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationSearchRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationUpdateRequest;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(ReservationCreateRequest request, UUID guardianId);

    ReservationResponse getReservation(UUID reservationId);

    Page<ReservationResponse> getReservations(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ReservationResponse> searchReservations(ReservationSearchRequest request, Pageable pageable);

    Page<ReservationResponse> getUserReservations(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    ReservationResponse updateReservation(UUID reservationId, ReservationUpdateRequest request);

    ReservationResponse acceptReservation(UUID reservationId, String caregiverNote);

    ReservationResponse rejectReservation(UUID reservationId, String rejectionReason, String suggestedAlternative);

    ReservationResponse cancelReservation(UUID reservationId, String cancelReason, String cancellationNote);

    Page<ReservationResponse> getReservationHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ReservationResponse> getUserReservationHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ReservationResponse> getReservationsByStatus(ReservationStatus status, Pageable pageable);

    ReservationResponse completeReservation(UUID reservationId);

    ReservationResponse linkPayment(UUID reservationId, UUID paymentId);
}