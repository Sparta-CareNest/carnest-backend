package com.carenest.business.notificationservice.domain.model;

public enum NotificationType {
    RESERVATION_CREATED("예약이 생성되었습니다."),
    PAYMENT_SUCCESS("결제가 완료되었습니다."),
    SETTLEMENT_COMPLETE("정산이 완료되었습니다.");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
