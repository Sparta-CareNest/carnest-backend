package com.carenest.business.paymentservice.infrastructure.external;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.exception.PaymentErrorCode;
import com.carenest.business.paymentservice.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Override
    public Map<String, Object> preparePayment(UUID reservationId, BigDecimal amount, String paymentMethod) {
        log.info("결제 준비 요청: reservationId={}, amount={}, method={}", reservationId, amount, paymentMethod);

        try {
            // TODO: PaySo API 호출 로직 구현하기

            // 성공 응답 Mock
            Map<String, Object> response = new HashMap<>();
            response.put("paymentKey", "key_" + UUID.randomUUID().toString().substring(0, 10));
            response.put("success", true);
            response.put("redirectUrl", "https://payments.example.com/checkout?key=" + response.get("paymentKey"));
            return response;
        } catch (Exception e) {
            log.error("결제 준비 중 오류 발생", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public PaymentCompleteRequest approvePayment(String paymentKey, BigDecimal amount) {
        log.info("결제 승인 요청: paymentKey={}, amount={}", paymentKey, amount);

        try {
            // TODO: PaySo API 호출 로직 구현하기

            // 성공 응답 Mock
            PaymentCompleteRequest response = new PaymentCompleteRequest();
            response.setApprovalNumber("AP" + System.currentTimeMillis());
            response.setPgTransactionId("TX" + System.currentTimeMillis());
            response.setReceiptUrl("https://payments.example.com/receipts/" + paymentKey);
            response.setPaymentKey(paymentKey);
            return response;
        } catch (Exception e) {
            log.error("결제 승인 중 오류 발생", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public boolean cancelPayment(String paymentKey, BigDecimal amount, String reason) {
        log.info("결제 취소 요청: paymentKey={}, amount={}, reason={}", paymentKey, amount, reason);

        try {
            // TODO: PaySo API 호출 로직 구현하기

            // 성공 응답 Mock
            return true;
        } catch (Exception e) {
            log.error("결제 취소 중 오류 발생", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public boolean refundPayment(String paymentKey, RefundRequest request) {
        log.info("환불 요청: paymentKey={}, amount={}", paymentKey, request.getRefundAmount());

        try {
            // TODO: PaySo API 호출 로직 구현하기

            // 성공 응답 Mock
            return true;
        } catch (Exception e) {
            log.error("환불 처리 중 오류 발생", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public Map<String, Object> getPaymentStatus(String paymentKey) {
        log.info("결제 상태 조회 요청: paymentKey={}", paymentKey);

        try {
            // TODO: PaySo API 호출 로직 구현하기

            // 성공 응답 Mock
            Map<String, Object> response = new HashMap<>();
            response.put("status", "COMPLETED");
            response.put("lastUpdated", System.currentTimeMillis());
            return response;
        } catch (Exception e) {
            log.error("결제 상태 조회 중 오류 발생", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public String getReceiptUrl(String paymentKey) {
        log.info("영수증 URL 조회 요청: paymentKey={}", paymentKey);

        try {
            // TODO: PaySo API 호출 로직 구현하기

            // 성공 응답 Mock
            return "https://payments.example.com/receipts/" + paymentKey;
        } catch (Exception e) {
            log.error("영수증 URL 조회 중 오류 발생", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }
}