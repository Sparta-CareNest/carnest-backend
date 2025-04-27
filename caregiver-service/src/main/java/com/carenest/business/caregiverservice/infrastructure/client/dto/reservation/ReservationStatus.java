package com.carenest.business.caregiverservice.infrastructure.client.dto.reservation;

public enum ReservationStatus {
    PENDING_PAYMENT,
    PENDING_ACCEPTANCE,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    REJECTED
}