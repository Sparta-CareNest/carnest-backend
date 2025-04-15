package com.carenest.business.reservationservice.exception;

public class IntegrationErrorException extends ReservationException {

    public IntegrationErrorException() {
        super(ReservationErrorCode.INTEGRATION_ERROR);
    }

    public IntegrationErrorException(String message) {
        super(ReservationErrorCode.INTEGRATION_ERROR);
    }
}