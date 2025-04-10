package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.dto.request.*;
import com.carenest.business.reservationservice.application.dto.response.ReservationResponse;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import jakarta.validation.Valid;
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
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseDto<ReservationResponse> createReservation(
            @RequestBody @Valid ReservationCreateRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseDto.success("예약이 성공적으로 생성되었습니다.", response);
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> getReservation(
            @PathVariable UUID reservationId) {
        ReservationResponse response = reservationService.getReservation(reservationId);
        return ResponseDto.success("예약 상세 정보 조회 성공", response);
    }

    @GetMapping("/reservations")
    public ResponseDto<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ModelAttribute ReservationSearchRequest searchRequest,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // 검색 조건이 있는 경우
        if (searchRequest.getGuardianId() != null ||
                searchRequest.getCaregiverId() != null ||
                searchRequest.getPatientName() != null ||
                searchRequest.getStatus() != null) {

            // 날짜 설정이 없으면 요청 파라미터의 값 사용
            if (searchRequest.getStartDate() == null) {
                searchRequest.setStartDate(startDate);
            }
            if (searchRequest.getEndDate() == null) {
                searchRequest.setEndDate(endDate);
            }

            Page<ReservationResponse> responses = reservationService.searchReservations(searchRequest, pageable);
            return ResponseDto.success("예약 검색 성공", responses);
        }

        // 기본 조회
        Page<ReservationResponse> responses = reservationService.getReservations(startDate, endDate, pageable);
        return ResponseDto.success("예약 목록 조회 성공", responses);
    }

    @GetMapping("/reservations/status/{status}")
    public ResponseDto<Page<ReservationResponse>> getReservationsByStatus(
            @PathVariable ReservationStatus status,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getReservationsByStatus(status, pageable);
        return ResponseDto.success("상태별 예약 목록 조회 성공", responses);
    }

    @GetMapping("/users/{userId}/reservations")
    public ResponseDto<Page<ReservationResponse>> getUserReservations(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getUserReservations(userId, startDate, endDate, pageable);
        return ResponseDto.success("사용자별 예약 목록 조회 성공", responses);
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> updateReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationUpdateRequest request) {
        ReservationResponse response = reservationService.updateReservation(reservationId, request);
        return ResponseDto.success("예약이 성공적으로 수정되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/accept")
    public ResponseDto<ReservationResponse> acceptReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationAcceptRequest request) {
        ReservationResponse response = reservationService.acceptReservation(reservationId, request.getCaregiverNote());
        return ResponseDto.success("예약이 성공적으로 수락되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/reject")
    public ResponseDto<ReservationResponse> rejectReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationRejectRequest request) {
        ReservationResponse response = reservationService.rejectReservation(
                reservationId,
                request.getRejectionReason(),
                request.getSuggestedAlternative()
        );
        return ResponseDto.success("예약이 거절되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseDto<ReservationResponse> cancelReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationCancelRequest request) {
        ReservationResponse response = reservationService.cancelReservation(
                reservationId,
                request.getCancelReason(),
                request.getCancellationNote()
        );
        return ResponseDto.success("예약이 성공적으로 취소되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/complete")
    public ResponseDto<ReservationResponse> completeReservation(
            @PathVariable UUID reservationId) {
        ReservationResponse response = reservationService.completeReservation(reservationId);
        return ResponseDto.success("서비스가 성공적으로 완료되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/payment")
    public ResponseDto<ReservationResponse> linkPayment(
            @PathVariable UUID reservationId,
            @RequestBody @Valid PaymentLinkRequest request) {
        ReservationResponse response = reservationService.linkPayment(reservationId, request.getPaymentId());
        return ResponseDto.success("결제 정보가 연결되었습니다.", response);
    }

    @GetMapping("/reservations/history")
    public ResponseDto<Page<ReservationResponse>> getReservationHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getReservationHistory(startDate, endDate, pageable);
        return ResponseDto.success("예약 이력 조회 성공", responses);
    }

    @GetMapping("/users/{userId}/reservations/history")
    public ResponseDto<Page<ReservationResponse>> getUserReservationHistory(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getUserReservationHistory(userId, startDate, endDate, pageable);
        return ResponseDto.success("사용자별 예약 이력 조회 성공", responses);
    }
}