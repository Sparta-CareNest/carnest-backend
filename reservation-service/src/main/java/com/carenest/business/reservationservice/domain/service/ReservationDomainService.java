package com.carenest.business.reservationservice.domain.service;

import com.carenest.business.reservationservice.domain.model.Reservation;

import java.util.UUID;

public interface ReservationDomainService {
    void createReservationHistory(Reservation reservation);

    boolean validateReservationTime(Reservation reservation);

    boolean canCancelReservation(UUID reservationId);

    boolean canRejectReservation(UUID reservationId);
}