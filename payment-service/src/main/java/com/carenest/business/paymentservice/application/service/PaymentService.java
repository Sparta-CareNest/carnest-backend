package com.carenest.business.paymentservice.application.service;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryDetailResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentListResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PaymentResponse getPayment(UUID paymentId);

    PaymentResponse getPaymentByReservationId(UUID reservationId);

    Page<PaymentListResponse> getPaymentList(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentListResponse> getUserPaymentList(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    PaymentResponse completePayment(UUID paymentId, PaymentCompleteRequest request);

    PaymentResponse cancelPayment(UUID paymentId, String cancelReason);

    PaymentResponse refundPayment(UUID paymentId, RefundRequest request);

    Page<PaymentHistoryResponse> getPaymentHistoryById(UUID paymentId, Pageable pageable);

    Page<PaymentHistoryDetailResponse> getAllPaymentHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentHistoryDetailResponse> getUserPaymentHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}