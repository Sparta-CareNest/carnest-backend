package com.carenest.business.reservationservice.domain.exception;

public class UnauthorizedReservationAccessException extends ReservationException {

    public UnauthorizedReservationAccessException() {
        super(ReservationErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
    }
}