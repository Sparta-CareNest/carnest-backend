package com.carenest.business.reservationservice.domain.exception;

public class CannotRejectReservationException extends ReservationException {

    public CannotRejectReservationException() {
        super(ReservationErrorCode.CANNOT_REJECT_RESERVATION);
    }
}