package com.carenest.business.notificationservice.domain.model;

public enum NotificationType {
    RESERVATION_CREATED("reservation.created"),
    PAYMENT_SUCCESS("payment.success"),
    SETTLEMENT_COMPLETE("settlement.complete"),
    RESERVATION_STATUS_CHANGED("reservation.status.changed"),
    RESERVATION_CANCELLED("reservation.cancelled"),
    PAYMENT_CANCELLED("payment.cancelled");

    private final String messageKey;

    NotificationType(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}