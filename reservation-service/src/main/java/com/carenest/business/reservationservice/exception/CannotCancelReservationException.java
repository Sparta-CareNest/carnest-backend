package com.carenest.business.reservationservice.exception;

public class CannotCancelReservationException extends ReservationException {

    public CannotCancelReservationException() {
        super(ReservationErrorCode.CANNOT_CANCEL_RESERVATION);
    }
}