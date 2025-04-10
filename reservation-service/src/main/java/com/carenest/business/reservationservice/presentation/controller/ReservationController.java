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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ResponseDto<ReservationResponse>> createReservation(
            @RequestBody @Valid ReservationCreateRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 생성되었습니다.", response));
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseDto<ReservationResponse>> getReservation(
            @PathVariable UUID reservationId) {
        ReservationResponse response = reservationService.getReservation(reservationId);
        return ResponseEntity.ok(ResponseDto.success("예약 상세 정보 조회 성공", response));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getReservations(
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
            return ResponseEntity.ok(ResponseDto.success("예약 검색 성공", responses));
        }

        // 기본 조회
        Page<ReservationResponse> responses = reservationService.getReservations(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("예약 목록 조회 성공", responses));
    }

    @GetMapping("/reservations/status/{status}")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getReservationsByStatus(
            @PathVariable ReservationStatus status,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getReservationsByStatus(status, pageable);
        return ResponseEntity.ok(ResponseDto.success("상태별 예약 목록 조회 성공", responses));
    }

    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getUserReservations(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getUserReservations(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("사용자별 예약 목록 조회 성공", responses));
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseDto<ReservationResponse>> updateReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationUpdateRequest request) {
        ReservationResponse response = reservationService.updateReservation(reservationId, request);
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 수정되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/accept")
    public ResponseEntity<ResponseDto<ReservationResponse>> acceptReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationAcceptRequest request) {
        ReservationResponse response = reservationService.acceptReservation(reservationId, request.getCaregiverNote());
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 수락되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/reject")
    public ResponseEntity<ResponseDto<ReservationResponse>> rejectReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationRejectRequest request) {
        ReservationResponse response = reservationService.rejectReservation(
                reservationId,
                request.getRejectionReason(),
                request.getSuggestedAlternative()
        );
        return ResponseEntity.ok(ResponseDto.success("예약이 거절되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ResponseDto<ReservationResponse>> cancelReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationCancelRequest request) {
        ReservationResponse response = reservationService.cancelReservation(
                reservationId,
                request.getCancelReason(),
                request.getCancellationNote()
        );
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 취소되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/complete")
    public ResponseEntity<ResponseDto<ReservationResponse>> completeReservation(
            @PathVariable UUID reservationId) {
        ReservationResponse response = reservationService.completeReservation(reservationId);
        return ResponseEntity.ok(ResponseDto.success("서비스가 성공적으로 완료되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/payment")
    public ResponseEntity<ResponseDto<ReservationResponse>> linkPayment(
            @PathVariable UUID reservationId,
            @RequestBody @Valid PaymentLinkRequest request) {
        ReservationResponse response = reservationService.linkPayment(reservationId, request.getPaymentId());
        return ResponseEntity.ok(ResponseDto.success("결제 정보가 연결되었습니다.", response));
    }

    @GetMapping("/reservations/history")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getReservationHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getReservationHistory(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("예약 이력 조회 성공", responses));
    }

    @GetMapping("/users/{userId}/reservations/history")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getUserReservationHistory(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getUserReservationHistory(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("사용자별 예약 이력 조회 성공", responses));
    }
}