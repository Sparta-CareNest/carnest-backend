package com.carenest.business.reservationservice.domain.service;

import com.carenest.business.reservationservice.domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationDomainService {

    void createReservationHistory(Reservation reservation);

    boolean validateReservationTime(Reservation reservation);

    boolean canCancelReservation(UUID reservationId);

    boolean canRejectReservation(UUID reservationId);

    boolean canCompleteReservation(UUID reservationId);

    boolean checkOverlappingReservations(UUID caregiverId, LocalDateTime startTime, LocalDateTime endTime);
}