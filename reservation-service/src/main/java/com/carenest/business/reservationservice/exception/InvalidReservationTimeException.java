package com.carenest.business.reservationservice.exception;

public class InvalidReservationTimeException extends ReservationException {

    public InvalidReservationTimeException() {
        super(ReservationErrorCode.INVALID_RESERVATION_TIME);
    }
}