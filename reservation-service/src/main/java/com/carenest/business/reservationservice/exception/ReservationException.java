package com.carenest.business.reservationservice.exception;

import com.carenest.business.common.exception.BaseErrorCode;
import com.carenest.business.common.exception.BaseException;

public class ReservationException extends BaseException {

    public ReservationException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}