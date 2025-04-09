package com.carenest.business.paymentservice.exception;

public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException() {
        super(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }
}