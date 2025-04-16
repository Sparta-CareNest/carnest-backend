package com.carenest.business.paymentservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.service.PaymentService;
import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
import com.carenest.business.paymentservice.exception.PaymentErrorCode;
import com.carenest.business.paymentservice.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    // 결제 성공 후 토스페이먼츠에서 호출하는 엔드포인트
    @GetMapping("/success")
    public RedirectView paymentSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "paymentId", required = false) String paymentIdStr) {

        log.info("토스페이먼츠 결제 성공 콜백: paymentKey={}, orderId={}, amount={}, paymentId={}",
                paymentKey, orderId, amount, paymentIdStr);

        try {
            UUID paymentId;
            Payment payment;

            // paymentId가 전달된 경우
            if (paymentIdStr != null && !paymentIdStr.isEmpty()) {
                paymentId = UUID.fromString(paymentIdStr);
                Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

                if (paymentOpt.isEmpty()) {
                    log.error("결제 정보를 찾을 수 없음: paymentId={}", paymentId);
                    throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                }

                payment = paymentOpt.get();
            }
            // paymentId가 없는 경우 orderId에서 예약 ID 추출
            else {
                String reservationIdStr = orderId.replace("CARENEST-", "");
                try {
                    // reservationId로 결제 정보 조회
                    UUID reservationId = UUID.fromString(reservationIdStr + "-0000-0000-000000000000");
                    Optional<Payment> paymentOpt = paymentRepository.findByReservationId(reservationId);

                    if (paymentOpt.isEmpty()) {
                        log.error("결제 정보를 찾을 수 없음: orderId={}, reservationId={}", orderId, reservationId);
                        throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                    }

                    payment = paymentOpt.get();
                    paymentId = payment.getPaymentId();
                } catch (IllegalArgumentException e) {
                    log.error("잘못된 UUID 형식: {}", reservationIdStr, e);
                    throw new PaymentException(PaymentErrorCode.PAYMENT_PROCESSING_ERROR);
                }
            }

            if (payment.getAmount().compareTo(amount) != 0) {
                log.error("결제 금액 불일치: expected={}, actual={}", payment.getAmount(), amount);
                throw new PaymentException(PaymentErrorCode.PAYMENT_PROCESSING_ERROR);
            }

            // 결제 완료 처리
            PaymentCompleteRequest request = new PaymentCompleteRequest();
            request.setPaymentKey(paymentKey);
            paymentService.completePayment(paymentId, request);

            // 프론트엔드 성공 페이지로 리다이렉트
            return new RedirectView("/payment/success?reservationId=" + payment.getReservationId());
        } catch (Exception e) {
            log.error("결제 성공 처리 중 오류 발생", e);
            // 프론트엔드 에러 페이지로 리다이렉트
            return new RedirectView("/payment/error?message=" + e.getMessage());
        }
    }

    // 결제 실패 시 토스페이먼츠에서 호출하는 엔드포인트
    @GetMapping("/fail")
    public RedirectView paymentFail(
            @RequestParam("code") String errorCode,
            @RequestParam("message") String errorMessage,
            @RequestParam("orderId") String orderId) {

        log.error("토스페이먼츠 결제 실패 콜백: orderId={}, errorCode={}, errorMessage={}",
                orderId, errorCode, errorMessage);

        // 프론트엔드 실패 페이지로 리다이렉트
        return new RedirectView("/payment/fail?code=" + errorCode + "&message=" + errorMessage);
    }

    // 토스페이먼츠 웹훅 처리 엔드포인트
    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void webhookHandler(@RequestBody Map<String, Object> webhookPayload) {
        log.info("토스페이먼츠 웹훅 수신: {}", webhookPayload);

        // 웹훅 이벤트 타입 확인
        String eventType = (String) webhookPayload.get("eventType");

        if (eventType == null) {
            log.warn("웹훅 이벤트 타입이 없음");
            return;
        }

        // 결제 정보
        Map<String, Object> payment = (Map<String, Object>) webhookPayload.get("payment");
        if (payment == null) {
            log.warn("웹훅 결제 정보가 없음");
            return;
        }

        String paymentKey = (String) payment.get("paymentKey");
        String status = (String) payment.get("status");
        String orderId = (String) payment.get("orderId");

        log.info("웹훅 결제 정보: paymentKey={}, status={}, orderId={}", paymentKey, status, orderId);

        // 웹훅 이벤트 처리
        switch (eventType) {
            case "PAYMENT_CONFIRMED":
                handlePaymentConfirmed(paymentKey, status, orderId);
                break;
            case "PAYMENT_CANCELED":
                handlePaymentCanceled(paymentKey, status, orderId);
                break;
            default:
                log.info("처리되지 않은 웹훅 이벤트 타입: {}", eventType);
        }
    }

    // 결제 승인 처리
    private void handlePaymentConfirmed(String paymentKey, String status, String orderId) {
        log.info("결제 승인됨: paymentKey={}, status={}, orderId={}", paymentKey, status, orderId);

        try {
            String reservationIdStr = orderId.replace("CARENEST-", "");

            Optional<Payment> paymentOpt = paymentRepository.findByPaymentKey(paymentKey);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();

                // 이미 완료 상태인 경우 처리 중단
                if (payment.getStatus() == com.carenest.business.paymentservice.domain.model.PaymentStatus.COMPLETED) {
                    log.info("이미 처리된 결제: paymentKey={}, status={}", paymentKey, payment.getStatus());
                    return;
                }

                // 결제 완료 처리
                PaymentCompleteRequest request = new PaymentCompleteRequest();
                request.setPaymentKey(paymentKey);
                paymentService.completePayment(payment.getPaymentId(), request);
            } else {
                log.warn("웹훅 처리: 결제 정보를 찾을 수 없음: paymentKey={}", paymentKey);
            }
        } catch (Exception e) {
            log.error("웹훅 결제 승인 처리 중 오류 발생", e);
        }
    }

    private void handlePaymentCanceled(String paymentKey, String status, String orderId) {
        log.info("결제 취소됨: paymentKey={}, status={}, orderId={}", paymentKey, status, orderId);

        try {
            // paymentKey로 결제 정보 조회
            Optional<Payment> paymentOpt = paymentRepository.findByPaymentKey(paymentKey);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();

                // 이미 취소 상태인 경우 처리 중단
                if (payment.getStatus() == com.carenest.business.paymentservice.domain.model.PaymentStatus.CANCELLED ||
                        payment.getStatus() == com.carenest.business.paymentservice.domain.model.PaymentStatus.REFUNDED) {
                    log.info("이미 취소된 결제: paymentKey={}, status={}", paymentKey, payment.getStatus());
                    return;
                }

                paymentService.cancelPayment(payment.getPaymentId(), "토스페이먼츠 웹훅에 의한 자동 취소");
            } else {
                log.warn("웹훅 처리: 결제 정보를 찾을 수 없음: paymentKey={}", paymentKey);
            }
        } catch (Exception e) {
            log.error("웹훅 결제 취소 처리 중 오류 발생", e);
        }
    }

    // 클라이언트에서 결제 위젯 초기화에 필요한 정보 제공
    @GetMapping("/client-key")
    public ResponseDto<Map<String, String>> getClientKey(@RequestParam UUID reservationId) {
        // 프론트엔드에서 토스페이먼츠 결제창을 띄우기 위한 정보 제공
        Map<String, Object> paymentInfo = paymentService
                .getPaymentByReservationId(reservationId)
                .getPaymentGatewayInfo();

        return ResponseDto.success("토스페이먼츠 클라이언트 정보", Map.of(
                "clientKey", System.getenv("TOSS_CLIENT_KEY"),
                "successUrl", paymentInfo.get("successUrl").toString(),
                "failUrl", paymentInfo.get("failUrl").toString()
        ));
    }
}