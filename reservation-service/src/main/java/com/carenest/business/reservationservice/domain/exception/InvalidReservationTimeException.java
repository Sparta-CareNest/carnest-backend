package com.carenest.business.reservationservice.domain.exception;

public class InvalidReservationTimeException extends ReservationException {

    public InvalidReservationTimeException() {
        super(ReservationErrorCode.INVALID_RESERVATION_TIME);
    }
}