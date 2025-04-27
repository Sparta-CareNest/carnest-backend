package com.carenest.business.reservationservice.exception;

public class PaymentAlreadyProcessedException extends ReservationException {

    public PaymentAlreadyProcessedException() {
        super(ReservationErrorCode.PAYMENT_ALREADY_PROCESSED);
    }
}