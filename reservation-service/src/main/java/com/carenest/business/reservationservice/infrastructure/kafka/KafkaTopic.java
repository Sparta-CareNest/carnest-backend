package com.carenest.business.reservationservice.infrastructure.kafka;

public enum KafkaTopic {
    RESERVATION_CREATED("reservation-created"),
    RESERVATION_CANCELLED("reservation-cancelled"),
    RESERVATION_STATUS_CHANGED("reservation-status-changed"),
    NOTIFICATION_EVENT("notification-event"),
    PAYMENT_COMPLETED("payment-completed"),
    PAYMENT_CANCELLED("payment-cancelled");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}