package com.carenest.business.paymentservice.infrastructure.service;

import com.carenest.business.paymentservice.domain.model.Payment;

public interface NotificationService {
    void sendPaymentSuccessNotification(Payment payment);

    void sendPaymentCancelNotification(Payment payment);

    void sendPaymentRefundNotification(Payment payment);
}