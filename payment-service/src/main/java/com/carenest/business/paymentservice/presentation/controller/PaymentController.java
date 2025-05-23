package com.carenest.business.paymentservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.model.UserRole;
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
import com.carenest.business.paymentservice.infrastructure.config.TossPaymentsConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentsConfig tossConfig;

    // 결제 생성
    @Operation(
            summary = "결제 생성",
            description = "새로운 결제를 생성합니다. 보호자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping("/payments")
    public ResponseDto<PaymentResponse> createPayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "결제 생성 요청 정보", required = true) @RequestBody PaymentCreateRequest request) {

        // 토큰에서 추출한 사용자 ID 사용
        PaymentResponse response = paymentService.createPayment(request, authUserInfo.getUserId());
        return ResponseDto.success("결제가 성공적으로 처리되었습니다.", response);
    }

    // 결제 상세 조회 API
    @Operation(
            summary = "결제 상세 조회",
            description = "결제 ID를 통해 결제 상세 정보를 조회합니다. 결제 당사자 또는 관리자만 조회 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 상세 정보 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/payments/{paymentId}")
    public ResponseDto<PaymentResponse> getPayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회할 결제 ID", required = true) @PathVariable UUID paymentId) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 본인의 결제 정보 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getUserId().equals(payment.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        return ResponseDto.success("결제 상세 정보 조회 성공", payment);
    }

    // 예약 ID로 결제 조회
    @Operation(
            summary = "예약 ID로 결제 조회",
            description = "예약 ID를 통해 해당 예약의 결제 정보를 조회합니다. 결제 당사자 또는 관리자만 조회 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약에 대한 결제 정보 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/reservations/{reservationId}/payment")
    public ResponseDto<PaymentResponse> getPaymentByReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId) {

        PaymentResponse payment = paymentService.getPaymentByReservationId(reservationId);

        // 본인의 결제 정보 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getUserId().equals(payment.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        return ResponseDto.success("예약에 대한 결제 정보 조회 성공", payment);
    }

    // 관리자용 전체 결제 내역 조회
    @Operation(
            summary = "전체 결제 내역 조회 (관리자용)",
            description = "관리자가 시스템의 모든 결제 내역을 조회합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 내역 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
            }
    )
    @GetMapping("/admin/payments")
    public ResponseDto<Page<PaymentListResponse>> getPaymentsAdmin(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10) Pageable pageable) {

        // ADMIN만 전체 결제 내역 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        Page<PaymentListResponse> responses = paymentService.getPaymentList(startDate, endDate, pageable);
        return ResponseDto.success("결제 내역 조회 성공", responses);
    }

    // 내 결제 내역 조회
    @Operation(
            summary = "내 결제 내역 조회",
            description = "현재 로그인한 사용자의 결제 내역을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 결제 내역 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/my/payments")
    public ResponseDto<Page<PaymentListResponse>> getMyPayments(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10) Pageable pageable) {

        // 토큰에서 추출한 사용자 ID 사용
        Page<PaymentListResponse> responses = paymentService.getUserPaymentList(
                authUserInfo.getUserId(), startDate, endDate, pageable);

        return ResponseDto.success("내 결제 내역 조회 성공", responses);
    }

    // 결제 완료 처리
    @Operation(
            summary = "결제 완료 처리",
            description = "결제를 완료 상태로 처리합니다. 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 완료 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 결제 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/payments/{paymentId}/complete")
    public ResponseDto<PaymentResponse> completePayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "결제 ID", required = true) @PathVariable UUID paymentId,
            @Parameter(description = "결제 완료 요청 정보", required = true) @RequestBody PaymentCompleteRequest request) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 보호자 또는 ADMIN만 결제 완료 처리 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        PaymentResponse response = paymentService.completePayment(paymentId, request);
        return ResponseDto.success("결제가 성공적으로 완료되었습니다.", response);
    }

    // 결제 취소
    @Operation(
            summary = "결제 취소",
            description = "결제를 취소합니다. 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 결제 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseDto<PaymentResponse> cancelPayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "결제 ID", required = true) @PathVariable UUID paymentId,
            @Parameter(description = "환불 정보", required = true) @RequestBody RefundRequest request) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 보호자 또는 ADMIN만 결제 취소 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        PaymentResponse response = paymentService.cancelPayment(paymentId, request.getCancelReason());
        return ResponseDto.success("결제 취소가 접수되었습니다.", response);
    }

    // 결제 환불
    @Operation(
            summary = "결제 환불",
            description = "완료된 결제를 환불 처리합니다. 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "환불 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 결제 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/payments/{paymentId}/refund")
    public ResponseDto<PaymentResponse> refundPayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "결제 ID", required = true) @PathVariable UUID paymentId,
            @Parameter(description = "환불 정보", required = true) @RequestBody RefundRequest request) {

        PaymentResponse payment = paymentService.getPayment(paymentId);

        // 보호자 또는 ADMIN만 환불 처리 가능
        if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        PaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseDto.success("환불이 성공적으로 처리되었습니다.", response);
    }

    // 결제 이력 조회
    @Operation(
            summary = "결제 이력 조회",
            description = "특정 결제의 상태 변경 이력을 조회합니다. 결제 당사자 또는 관리자만 접근 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/payments/{paymentId}/history")
    public ResponseDto<Page<PaymentHistoryResponse>> getPaymentHistory(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "결제 ID", required = true) @PathVariable UUID paymentId,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10) Pageable pageable) {

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

    // 관리자용 전체 결제 이력 조회
    @Operation(
            summary = "전체 결제 이력 조회 (관리자용)",
            description = "시스템의 모든 결제 이력을 조회합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
            }
    )
    @GetMapping("/admin/payments/history")
    public ResponseDto<Page<PaymentHistoryDetailResponse>> getAllPaymentHistory(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10) Pageable pageable) {

        // ADMIN만 전체 결제 이력 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedPaymentAccessException();
        }

        Page<PaymentHistoryDetailResponse> responses = paymentService.getAllPaymentHistory(startDate, endDate, pageable);
        return ResponseDto.success("결제 이력 조회 성공", responses);
    }

    // 내 결제 이력 조회
    @Operation(
            summary = "내 결제 이력 조회",
            description = "현재 로그인한 사용자의 결제 이력을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 결제 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/my/payments/history")
    public ResponseDto<Page<PaymentHistoryDetailResponse>> getMyPaymentHistory(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10) Pageable pageable) {

        // 토큰에서 추출한 사용자 ID 사용
        Page<PaymentHistoryDetailResponse> responses = paymentService.getUserPaymentHistory(
                authUserInfo.getUserId(), startDate, endDate, pageable);

        return ResponseDto.success("내 결제 이력 조회 성공", responses);
    }

    // 토스페이먼츠 결제 준비
    @Operation(
            summary = "토스페이먼츠 결제 준비",
            description = "토스페이먼츠를 통한 결제 준비 정보를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토스페이먼츠 결제 준비 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping("/payments/prepare-toss")
    public ResponseDto<Map<String, Object>> prepareTossPayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "결제 생성 요청", required = true) @RequestBody PaymentCreateRequest request) {

        PaymentResponse response = paymentService.createPayment(request, authUserInfo.getUserId());

        // 토스페이먼츠 결제 정보가 있으면 그대로 반환
        if (response.getTossPaymentsInfo() != null) {
            return ResponseDto.success("토스페이먼츠 결제 준비 정보", response.getTossPaymentsInfo());
        }

        // 토스페이먼츠에 필요한 결제 정보 구성
        Map<String, Object> tossPaymentInfo = new HashMap<>();
        tossPaymentInfo.put("amount", response.getAmount());
        tossPaymentInfo.put("orderId", "CARENEST-" + response.getReservationId().toString().substring(0, 8));
        tossPaymentInfo.put("orderName", "CareNest 간병 서비스 예약");
        tossPaymentInfo.put("customerName", authUserInfo.getEmail().split("@")[0]);
        tossPaymentInfo.put("successUrl", tossConfig.getSuccessUrl() + "?paymentId=" + response.getPaymentId());
        tossPaymentInfo.put("failUrl", tossConfig.getFailUrl());
        tossPaymentInfo.put("paymentId", response.getPaymentId());
        tossPaymentInfo.put("clientKey", tossConfig.getClientKey());

        return ResponseDto.success("토스페이먼츠 결제 준비 정보", tossPaymentInfo);
    }

    // 토스페이먼츠 결제 정보 제공 (통합 엔드포인트)
    @Operation(
            summary = "토스페이먼츠 결제 정보 조회",
            description = "토스페이먼츠 결제에 필요한 클라이언트 정보와 예약 관련 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토스페이먼츠 결제 정보 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/payments/toss/client-info")
    public ResponseDto<Map<String, Object>> getTossClientInfo(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID (선택사항)") @RequestParam(required = false) UUID reservationId) {

        Map<String, Object> response = new HashMap<>();
        response.put("clientKey", tossConfig.getClientKey());
        response.put("successUrl", tossConfig.getSuccessUrl());
        response.put("failUrl", tossConfig.getFailUrl());

        // 예약 ID가 제공된 경우 결제 정보도 포함
        if (reservationId != null) {
            try {
                PaymentResponse payment = paymentService.getPaymentByReservationId(reservationId);

                // 본인의 결제 정보 또는 ADMIN만 조회 가능
                if (!authUserInfo.getUserId().equals(payment.getGuardianId()) &&
                        !authUserInfo.getUserId().equals(payment.getCaregiverId()) &&
                        !authUserInfo.getRole().equals(UserRole.ADMIN)) {
                    throw new UnauthorizedPaymentAccessException();
                }

                // 결제 정보 추가
                response.put("orderId", "CARENEST-" + payment.getReservationId().toString().substring(0, 8));
                response.put("orderName", "CareNest 간병 서비스 예약");
                response.put("amount", payment.getAmount());
                response.put("customerName", authUserInfo.getEmail().split("@")[0]);
                response.put("paymentId", payment.getPaymentId());
            } catch (Exception e) {
                // 결제 정보가 없는 경우는 무시하고 기본 정보만 반환
            }
        }

        return ResponseDto.success("토스페이먼츠 결제 정보", response);
    }
}