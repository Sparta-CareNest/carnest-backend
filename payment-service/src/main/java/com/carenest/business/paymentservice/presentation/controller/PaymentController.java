package com.carenest.business.paymentservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryDetailResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentListResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentResponse;
import com.carenest.business.paymentservice.application.service.PaymentService;
import com.carenest.business.paymentservice.exception.UnauthorizedPaymentAccessException;
import com.carenest.business.common.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ResponseDto<PaymentResponse> createPayment(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestBody PaymentCreateRequest request) {

        // 토큰에서 추출한 사용자 ID 사용
        PaymentResponse response = paymentService.createPayment(request, authUserInfo.getUserId());
        return ResponseDto.success("결제가 성공적으로 처리되었습니다.", response);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseDto<PaymentResponse> getPayment(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID paymentId) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 본인의 결제 정보 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getUserId().equals(payment.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        return ResponseDto.success("결제 상세 정보 조회 성공", payment);
    }

    @GetMapping("/reservations/{reservationId}/payment")
    public ResponseDto<PaymentResponse> getPaymentByReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId) {

        PaymentResponse payment = paymentService.getPaymentByReservationId(reservationId);

        // 본인의 결제 정보 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getUserId().equals(payment.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        return ResponseDto.success("예약에 대한 결제 정보 조회 성공", payment);
    }

    @GetMapping("/admin/payments")
    public ResponseDto<Page<PaymentListResponse>> getPaymentsAdmin(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {

        // ADMIN만 전체 결제 내역 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        Page<PaymentListResponse> responses = paymentService.getPaymentList(startDate, endDate, pageable);
        return ResponseDto.success("결제 내역 조회 성공", responses);
    }

    @GetMapping("/my/payments")
    public ResponseDto<Page<PaymentListResponse>> getMyPayments(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {

        // 토큰에서 추출한 사용자 ID 사용
        Page<PaymentListResponse> responses = paymentService.getUserPaymentList(
                authUserInfo.getUserId(), startDate, endDate, pageable);

        return ResponseDto.success("내 결제 내역 조회 성공", responses);
    }

    @PatchMapping("/payments/{paymentId}/complete")
    public ResponseDto<PaymentResponse> completePayment(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID paymentId,
            @RequestBody PaymentCompleteRequest request) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 보호자 또는 ADMIN만 결제 완료 처리 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        PaymentResponse response = paymentService.completePayment(paymentId, request);
        return ResponseDto.success("결제가 성공적으로 완료되었습니다.", response);
    }

    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseDto<PaymentResponse> cancelPayment(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID paymentId,
            @RequestBody RefundRequest request) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 보호자 또는 ADMIN만 결제 취소 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        PaymentResponse response = paymentService.cancelPayment(paymentId, request.getCancelReason());
        return ResponseDto.success("결제 취소가 접수되었습니다.", response);
    }

    @PatchMapping("/payments/{paymentId}/refund")
    public ResponseDto<PaymentResponse> refundPayment(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID paymentId,
            @RequestBody RefundRequest request) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 보호자 또는 ADMIN만 환불 처리 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        PaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseDto.success("환불이 성공적으로 처리되었습니다.", response);
    }

    @GetMapping("/payments/{paymentId}/history")
    public ResponseDto<Page<PaymentHistoryResponse>> getPaymentHistory(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID paymentId,
            @PageableDefault(size = 10) Pageable pageable) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 결제 당사자 또는 ADMIN만 이력 조회 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getUserId().equals(payment.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        Page<PaymentHistoryResponse> responses = paymentService.getPaymentHistoryById(paymentId, pageable);
        return ResponseDto.success("결제 이력 조회 성공", responses);
    }

    @GetMapping("/admin/payments/history")
    public ResponseDto<Page<PaymentHistoryDetailResponse>> getAllPaymentHistory(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {

        // ADMIN만 전체 결제 이력 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        Page<PaymentHistoryDetailResponse> responses = paymentService.getAllPaymentHistory(startDate, endDate, pageable);
        return ResponseDto.success("결제 이력 조회 성공", responses);
    }

    @GetMapping("/my/payments/history")
    public ResponseDto<Page<PaymentHistoryDetailResponse>> getMyPaymentHistory(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {

        // 토큰에서 추출한 사용자 ID 사용
        Page<PaymentHistoryDetailResponse> responses = paymentService.getUserPaymentHistory(
                authUserInfo.getUserId(), startDate, endDate, pageable);

        return ResponseDto.success("내 결제 이력 조회 성공", responses);
    }
}