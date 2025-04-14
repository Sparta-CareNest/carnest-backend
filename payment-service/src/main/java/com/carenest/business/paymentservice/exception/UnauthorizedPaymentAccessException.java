package com.carenest.business.paymentservice.exception;

public class UnauthorizedPaymentAccessException extends PaymentException {
    public UnauthorizedPaymentAccessException() {
        super(PaymentErrorCode.UNAUTHORIZED_PAYMENT_ACCESS);
    }
}