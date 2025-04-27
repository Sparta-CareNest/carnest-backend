package com.carenest.business.paymentservice.exception;

public class DuplicatePaymentException extends PaymentException {
    public DuplicatePaymentException() {
        super(PaymentErrorCode.DUPLICATE_PAYMENT);
    }
}