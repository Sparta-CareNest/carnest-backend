package com.carenest.business.reservationservice.domain.model;

public enum ReservationStatus {
    PENDING_PAYMENT,
    PENDING_ACCEPTANCE,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    REJECTED
}