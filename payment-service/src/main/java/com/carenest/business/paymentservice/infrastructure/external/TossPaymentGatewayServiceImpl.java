package com.carenest.business.paymentservice.infrastructure.external;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.exception.PaymentErrorCode;
import com.carenest.business.paymentservice.exception.PaymentException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class TossPaymentGatewayServiceImpl implements PaymentGatewayService {

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.api-base-url}")
    private String apiBaseUrl;

    @Value("${toss.payments.api-version}")
    private String apiVersion;

    @Value("${toss.payments.success-url}")
    private String successUrl;

    @Value("${toss.payments.fail-url}")
    private String failUrl;

    @Override
    public Map<String, Object> preparePayment(UUID reservationId, BigDecimal amount, String paymentMethod) {
        log.info("토스페이먼츠 결제 준비 요청: reservationId={}, amount={}, method={}", reservationId, amount, paymentMethod);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("preparePayment");

            // 클라이언트가 결제창을 띄울 때 필요한 정보 반환
            String orderId = "CARENEST-" + reservationId.toString().substring(0, 8);

            Map<String, Object> result = new HashMap<>();
            result.put("amount", amount);
            result.put("orderId", orderId);
            result.put("orderName", "CareNest 간병 서비스 예약");
            result.put("customerName", "보호자");  // TODO: 사용자 정보 사용
            result.put("successUrl", successUrl);
            result.put("failUrl", failUrl);
            result.put("method", convertPaymentMethod(paymentMethod));
            result.put("clientKey", System.getenv("TOSS_CLIENT_KEY"));

            // 클라이언트에서 토스페이먼츠 결제창을 띄울 때 필요한 URL
            result.put("tossPaymentsUrl", "https://js.tosspayments.com/v1");

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
        log.info("토스페이먼츠 결제 승인 요청: paymentKey={}, amount={}", paymentKey, amount);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("approvePayment");

            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> responseEntity = circuitBreaker.run(
                    () -> restTemplate.exchange(
                            apiBaseUrl + "/payments/" + paymentKey + "/confirm",
                            HttpMethod.POST,
                            requestEntity,
                            Map.class
                    ),
                    throwable -> {
                        log.error("토스페이먼츠 결제 승인 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            Map response = responseEntity.getBody();

            if (response == null) {
                throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            String status = (String) response.get("status");
            if (!"DONE".equals(status)) {
                log.error("토스페이먼츠 결제 승인 실패: {}", status);
                throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            PaymentCompleteRequest result = new PaymentCompleteRequest();

            // 카드 정보가 있는 경우 승인번호 설정
            Object card = response.get("card");
            if (card instanceof Map) {
                result.setApprovalNumber((String) ((Map) card).get("approveNo"));
                result.setReceiptUrl((String) ((Map) card).get("receiptUrl"));
            } else {
                result.setApprovalNumber(UUID.randomUUID().toString().substring(0, 8));
                result.setReceiptUrl("https://receipt.tosspayments.com/" + UUID.randomUUID().toString().substring(0, 10));
            }

            result.setPgTransactionId((String) response.get("transactionKey"));
            result.setPaymentKey(paymentKey);

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
        log.info("토스페이먼츠 결제 취소 요청: paymentKey={}, amount={}, reason={}", paymentKey, amount, reason);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("cancelPayment");

            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", reason);

            if (amount != null) {
                requestBody.put("cancelAmount", amount.intValue());
            }

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> responseEntity = circuitBreaker.run(
                    () -> restTemplate.exchange(
                            apiBaseUrl + "/payments/" + paymentKey + "/cancel",
                            HttpMethod.POST,
                            requestEntity,
                            Map.class
                    ),
                    throwable -> {
                        log.error("토스페이먼츠 결제 취소 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            Map response = responseEntity.getBody();

            if (response == null) {
                return false;
            }

            String status = (String) response.get("status");
            return "CANCELED".equals(status);
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
        log.info("토스페이먼츠 결제 상태 조회 요청: paymentKey={}", paymentKey);

        try {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("getPaymentStatus");

            HttpHeaders headers = createHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = circuitBreaker.run(
                    () -> restTemplate.exchange(
                            apiBaseUrl + "/payments/" + paymentKey,
                            HttpMethod.GET,
                            requestEntity,
                            Map.class
                    ),
                    throwable -> {
                        log.error("토스페이먼츠 결제 상태 조회 요청 실패", throwable);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                    }
            );

            Map response = responseEntity.getBody();

            if (response == null) {
                throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            // 필요한 정보만 추출하여 반환
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.get("status"));
            result.put("paymentKey", response.get("paymentKey"));
            result.put("amount", response.get("totalAmount"));
            result.put("orderId", response.get("orderId"));
            result.put("approvedAt", response.get("approvedAt"));

            // 카드 정보가 있는 경우 추가 정보 설정
            Object card = response.get("card");
            if (card instanceof Map) {
                result.put("approvalNumber", ((Map) card).get("approveNo"));
                result.put("cardCompany", ((Map) card).get("company"));
                result.put("cardNumber", ((Map) card).get("number"));
                result.put("receiptUrl", ((Map) card).get("receiptUrl"));
            }

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
        return (String) status.getOrDefault("receiptUrl", "");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes()));
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        headers.set("Toss-Payments-Version", apiVersion);
        return headers;
    }

    private String convertPaymentMethod(String paymentMethod) {
        switch (paymentMethod.toUpperCase()) {
            case "CARD":
                return "카드";
            case "BANK_TRANSFER":
                return "계좌이체";
            case "VIRTUAL_ACCOUNT":
                return "가상계좌";
            case "PHONE":
                return "휴대폰";
            default:
                return "카드"; // 기본값
        }
    }
}