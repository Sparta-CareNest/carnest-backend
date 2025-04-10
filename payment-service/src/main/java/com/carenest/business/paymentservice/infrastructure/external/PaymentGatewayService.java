package com.carenest.business.paymentservice.infrastructure.external;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface PaymentGatewayService {

    // 결제 준비
    Map<String, Object> preparePayment(UUID reservationId, BigDecimal amount, String paymentMethod);

    // 결제 승인 처리
    PaymentCompleteRequest approvePayment(String paymentKey, BigDecimal amount);

    // 결제 취소
    boolean cancelPayment(String paymentKey, BigDecimal amount, String reason);

    // 환불 처리
    boolean refundPayment(String paymentKey, RefundRequest request);

    // 결제 상태 조회
    Map<String, Object> getPaymentStatus(String paymentKey);

    // 거래 영수증 URL 조회
    String getReceiptUrl(String paymentKey);
}