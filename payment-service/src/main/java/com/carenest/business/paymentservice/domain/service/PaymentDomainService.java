package com.carenest.business.paymentservice.domain.service;

import com.carenest.business.paymentservice.domain.model.Payment;

import java.util.UUID;

public interface PaymentDomainService {
    void createPaymentHistory(Payment payment);

    boolean validatePaymentStatus(Payment payment, String targetStatus);

    boolean canCancelPayment(UUID paymentId);

    boolean canRefundPayment(UUID paymentId);
}