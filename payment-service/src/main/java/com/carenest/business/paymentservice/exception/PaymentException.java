package com.carenest.business.paymentservice.exception;

import com.carenest.business.common.exception.BaseErrorCode;
import com.carenest.business.common.exception.BaseException;

public class PaymentException extends BaseException {
    public PaymentException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}