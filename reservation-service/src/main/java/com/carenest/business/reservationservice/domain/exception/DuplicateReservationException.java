package com.carenest.business.reservationservice.domain.exception;

public class DuplicateReservationException extends ReservationException {

    public DuplicateReservationException() {
        super(ReservationErrorCode.DUPLICATE_RESERVATION);
    }
}