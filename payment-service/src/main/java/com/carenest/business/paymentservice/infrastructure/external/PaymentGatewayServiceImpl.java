package com.carenest.business.paymentservice.infrastructure.external;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.exception.PaymentErrorCode;
import com.carenest.business.paymentservice.exception.PaymentException;
import com.carenest.business.paymentservice.infrastructure.client.PaymentGatewayClient;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.PaySoApprovalRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.PaySoCancelRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.PaySoPrepareRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.PaySoApprovalResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.PaySoCancelResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.PaySoPrepareResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final PaymentGatewayClient paymentGatewayClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Value("${payso.merchant-id}")
    private String merchantId;

    @Value("${payso.success-url}")
    private String successUrl;

    @Value("${payso.failure-url}")
    private String failureUrl;

    @Override
    public Map<String, Object> preparePayment(UUID reservationId, BigDecimal amount, String paymentMethod) {
        log.info("결제 준비 요청: reservationId={}, amount={}, method={}", reservationId, amount, paymentMethod);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("preparePayment");

            PaySoPrepareRequestDto requestDto = PaySoPrepareRequestDto.builder()
                    .merchantId(merchantId)
                    .orderReference(reservationId.toString())
                    .amount(amount)
                    .currency("KRW")
                    .paymentMethod(paymentMethod)
                    .successUrl(successUrl)
                    .failureUrl(failureUrl)
                    .description("CareNest 간병 서비스 예약")
                    .build();

            PaySoPrepareResponseDto response = circuitBreaker.run(
                    () -> paymentGatewayClient.preparePayment(requestDto),
                    throwable -> {
                        log.error("PaySo 결제 준비 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            if (!"SUCCESS".equals(response.getStatus())) {
                log.error("PaySo 결제 준비 실패: {}", response.getMessage());
                throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("paymentKey", response.getPaymentKey());
            result.put("success", true);
            result.put("redirectUrl", response.getNextRedirectUrl());

            return result;
        } catch (Exception e) {
            log.error("결제 준비 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public PaymentCompleteRequest approvePayment(String paymentKey, BigDecimal amount) {
        log.info("결제 승인 요청: paymentKey={}, amount={}", paymentKey, amount);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("approvePayment");

            PaySoApprovalRequestDto requestDto = PaySoApprovalRequestDto.builder()
                    .paymentKey(paymentKey)
                    .amount(amount)
                    .merchantId(merchantId)
                    .build();

            PaySoApprovalResponseDto response = circuitBreaker.run(
                    () -> paymentGatewayClient.approvePayment(requestDto),
                    throwable -> {
                        log.error("PaySo 결제 승인 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            if (!"DONE".equals(response.getStatus())) {
                log.error("PaySo 결제 승인 실패: {}", response.getMessage());
                throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            PaymentCompleteRequest result = new PaymentCompleteRequest();
            result.setApprovalNumber(response.getApprovalNumber());
            result.setPgTransactionId(response.getPgTransactionId());
            result.setReceiptUrl(response.getReceiptUrl());
            result.setPaymentKey(response.getPaymentKey());

            return result;
        } catch (Exception e) {
            log.error("결제 승인 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public boolean cancelPayment(String paymentKey, BigDecimal amount, String reason) {
        log.info("결제 취소 요청: paymentKey={}, amount={}, reason={}", paymentKey, amount, reason);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("cancelPayment");

            PaySoCancelRequestDto requestDto = PaySoCancelRequestDto.builder()
                    .paymentKey(paymentKey)
                    .cancelAmount(amount)
                    .cancelReason(reason)
                    .merchantId(merchantId)
                    .build();

            PaySoCancelResponseDto response = circuitBreaker.run(
                    () -> paymentGatewayClient.cancelPayment(requestDto),
                    throwable -> {
                        log.error("PaySo 결제 취소 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            if (!"CANCELED".equals(response.getStatus())) {
                log.error("PaySo 결제 취소 실패: {}", response.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("결제 취소 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public boolean refundPayment(String paymentKey, RefundRequest request) {
        return cancelPayment(paymentKey, request.getRefundAmount(), request.getCancelReason());
    }

    @Override
    public Map<String, Object> getPaymentStatus(String paymentKey) {
        log.info("결제 상태 조회 요청: paymentKey={}", paymentKey);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("getPaymentStatus");

            PaySoApprovalResponseDto response = circuitBreaker.run(
                    () -> paymentGatewayClient.getPaymentStatus(paymentKey),
                    throwable -> {
                        log.error("PaySo 결제 상태 조회 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatus());
            result.put("paymentKey", response.getPaymentKey());
            result.put("approvalNumber", response.getApprovalNumber());
            result.put("amount", response.getAmount());
            result.put("lastUpdated", response.getApprovedAt());

            return result;
        } catch (Exception e) {
            log.error("결제 상태 조회 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public String getReceiptUrl(String paymentKey) {
        Map<String, Object> status = getPaymentStatus(paymentKey);
        PaySoApprovalResponseDto response = paymentGatewayClient.getPaymentStatus(paymentKey);
        return response.getReceiptUrl();
    }
}