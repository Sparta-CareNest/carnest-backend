package com.carenest.business.reservationservice.exception;

public class InvalidReservationStatusException extends ReservationException {

    public InvalidReservationStatusException() {
        super(ReservationErrorCode.INVALID_RESERVATION_STATUS);
    }
}