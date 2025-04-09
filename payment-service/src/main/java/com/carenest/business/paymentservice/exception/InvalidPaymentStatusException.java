package com.carenest.business.paymentservice.exception;

public class InvalidPaymentStatusException extends PaymentException {
    public InvalidPaymentStatusException() {
        super(PaymentErrorCode.INVALID_PAYMENT_STATUS);
    }
}