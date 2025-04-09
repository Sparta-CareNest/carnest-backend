package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.dto.request.ReservationCreateRequest;
import com.carenest.business.reservationservice.application.dto.request.ReservationUpdateRequest;
import com.carenest.business.reservationservice.application.dto.response.ReservationResponse;
import com.carenest.business.reservationservice.application.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ResponseDto<ReservationResponse>> createReservation(@RequestBody ReservationCreateRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 생성되었습니다.", response));
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseDto<ReservationResponse>> getReservation(@PathVariable UUID reservationId) {
        ReservationResponse response = reservationService.getReservation(reservationId);
        return ResponseEntity.ok(ResponseDto.success("예약 상세 정보 조회 성공", response));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getReservations(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("예약 목록 조회 성공", responses));
    }

    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getUserReservations(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getUserReservations(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("사용자별 예약 목록 조회 성공", responses));
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseDto<ReservationResponse>> updateReservation(
            @PathVariable UUID reservationId,
            @RequestBody ReservationUpdateRequest request) {
        ReservationResponse response = reservationService.updateReservation(reservationId, request);
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 수정되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/accept")
    public ResponseEntity<ResponseDto<ReservationResponse>> acceptReservation(
            @PathVariable UUID reservationId,
            @RequestBody Map<String, String> request) {
        String caregiverNote = request.getOrDefault("caregiver_note", "");
        ReservationResponse response = reservationService.acceptReservation(reservationId, caregiverNote);
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 수락되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/reject")
    public ResponseEntity<ResponseDto<ReservationResponse>> rejectReservation(
            @PathVariable UUID reservationId,
            @RequestBody Map<String, String> request) {
        String rejectionReason = request.getOrDefault("rejection_reason", "");
        String suggestedAlternative = request.getOrDefault("suggested_alternative", "");
        ReservationResponse response = reservationService.rejectReservation(reservationId, rejectionReason, suggestedAlternative);
        return ResponseEntity.ok(ResponseDto.success("예약이 거절되었습니다.", response));
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ResponseDto<ReservationResponse>> cancelReservation(
            @PathVariable UUID reservationId,
            @RequestBody Map<String, String> request) {
        String cancelReason = request.getOrDefault("cancel_reason", "");
        String cancellationNote = request.getOrDefault("cancellation_note", "");
        ReservationResponse response = reservationService.cancelReservation(reservationId, cancelReason, cancellationNote);
        return ResponseEntity.ok(ResponseDto.success("예약이 성공적으로 취소되었습니다.", response));
    }

    @GetMapping("/reservations/history")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getReservationHistory(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getReservationHistory(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("예약 이력 조회 성공", responses));
    }

    @GetMapping("/users/{userId}/reservations/history")
    public ResponseEntity<ResponseDto<Page<ReservationResponse>>> getUserReservationHistory(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDateTime endDate,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReservationResponse> responses = reservationService.getUserReservationHistory(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseDto.success("사용자별 예약 이력 조회 성공", responses));
    }
}