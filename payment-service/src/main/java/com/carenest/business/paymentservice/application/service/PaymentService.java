package com.carenest.business.paymentservice.application.service;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.application.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(PaymentCreateRequest request);

    PaymentResponse getPayment(UUID paymentId);

    PaymentResponse getPaymentByReservationId(UUID reservationId);

    Page<PaymentResponse> getPayments(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentResponse> getUserPayments(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    PaymentResponse completePayment(UUID paymentId, PaymentCompleteRequest request);

    PaymentResponse cancelPayment(UUID paymentId, String cancelReason);

    PaymentResponse refundPayment(UUID paymentId, RefundRequest request);

    Page<PaymentResponse> getPaymentHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentResponse> getUserPaymentHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}