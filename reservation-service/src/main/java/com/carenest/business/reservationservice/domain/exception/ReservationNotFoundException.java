package com.carenest.business.reservationservice.domain.exception;

public class ReservationNotFoundException extends ReservationException {

    public ReservationNotFoundException() {
        super(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }
}