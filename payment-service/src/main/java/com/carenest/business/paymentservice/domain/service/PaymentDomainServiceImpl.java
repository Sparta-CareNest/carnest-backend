package com.carenest.business.paymentservice.domain.service;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentHistory;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentHistoryRepository;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentDomainServiceImpl implements PaymentDomainService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Override
    public void createPaymentHistory(Payment payment) {
        PaymentHistory history = new PaymentHistory(payment);
        paymentHistoryRepository.save(history);
    }

    @Override
    public boolean validatePaymentStatus(Payment payment, String targetStatus) {
        return payment.getStatus().name().equals(targetStatus);
    }

    @Override
    public boolean canCancelPayment(UUID paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

        if (paymentOpt.isEmpty()) {
            return false;
        }

        Payment payment = paymentOpt.get();
        // PENDING 상태인 경우에만 취소 가능
        return payment.getStatus() == PaymentStatus.PENDING;
    }

    @Override
    public boolean canRefundPayment(UUID paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

        if (paymentOpt.isEmpty()) {
            return false;
        }

        Payment payment = paymentOpt.get();
        // COMPLETED 상태인 경우에만 환불 가능
        return payment.getStatus() == PaymentStatus.COMPLETED;
    }
}