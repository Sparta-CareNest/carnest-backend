package com.carenest.business.paymentservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryDetailResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentListResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentResponse;
import com.carenest.business.paymentservice.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ResponseEntity<ResponseDto<PaymentResponse>> createPayment(@RequestBody PaymentCreateRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(ResponseDto.success("결제가 성공적으로 처리되었습니다.", response));
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ResponseDto<PaymentResponse>> getPayment(@PathVariable UUID paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ResponseDto.success("결제 상세 정보 조회 성공", response));
    }

    @GetMapping("/reservations/{reservationId}/payment")
    public ResponseEntity<ResponseDto<PaymentResponse>> getPaymentByReservation(@PathVariable UUID reservationId) {
        PaymentResponse response = paymentService.getPaymentByReservationId(reservationId);
        return ResponseEntity.ok(ResponseDto.success("예약에 대한 결제 정보 조회 성공", response));
    }

    @GetMapping("/payments")
    public ResponseEntity<ResponseDto<Page<PaymentListResponse>>> getPayments(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentListResponse> responses = paymentService.getPaymentList(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("결제 내역 조회 성공", responses));
    }

    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<ResponseDto<Page<PaymentListResponse>>> getUserPayments(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentListResponse> responses = paymentService.getUserPaymentList(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("사용자별 결제 내역 조회 성공", responses));
    }

    @PatchMapping("/payments/{paymentId}/complete")
    public ResponseEntity<ResponseDto<PaymentResponse>> completePayment(
            @PathVariable UUID paymentId,
            @RequestBody PaymentCompleteRequest request) {
        PaymentResponse response = paymentService.completePayment(paymentId, request);
        return ResponseEntity.ok(ResponseDto.success("결제가 성공적으로 완료되었습니다.", response));
    }

    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<ResponseDto<PaymentResponse>> cancelPayment(
            @PathVariable UUID paymentId,
            @RequestBody RefundRequest request) {
        PaymentResponse response = paymentService.cancelPayment(paymentId, request.getCancelReason());
        return ResponseEntity.ok(ResponseDto.success("결제 취소가 접수되었습니다.", response));
    }

    @PatchMapping("/payments/{paymentId}/refund")
    public ResponseEntity<ResponseDto<PaymentResponse>> refundPayment(
            @PathVariable UUID paymentId,
            @RequestBody RefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(ResponseDto.success("환불이 성공적으로 처리되었습니다.", response));
    }

    @GetMapping("/payments/{paymentId}/history")
    public ResponseEntity<ResponseDto<Page<PaymentHistoryResponse>>> getPaymentHistory(
            @PathVariable UUID paymentId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentHistoryResponse> responses = paymentService.getPaymentHistoryById(paymentId, pageable);
        return ResponseEntity.ok(ResponseDto.success("결제 이력 조회 성공", responses));
    }

    @GetMapping("/payments/history")
    public ResponseEntity<ResponseDto<Page<PaymentHistoryDetailResponse>>> getAllPaymentHistory(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentHistoryDetailResponse> responses = paymentService.getAllPaymentHistory(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("결제 이력 조회 성공", responses));
    }

    @GetMapping("/users/{userId}/payments/history")
    public ResponseEntity<ResponseDto<Page<PaymentHistoryDetailResponse>>> getUserPaymentHistory(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentHistoryDetailResponse> responses = paymentService.getUserPaymentHistory(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("사용자별 결제 이력 조회 성공", responses));
    }
}