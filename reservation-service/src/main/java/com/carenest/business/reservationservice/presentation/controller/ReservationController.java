package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.dto.request.*;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.exception.UnauthorizedReservationAccessException;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import com.carenest.business.common.model.UserRole;
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
            @AuthUser AuthUserInfo authUserInfo,
            @RequestBody @Valid ReservationCreateRequest request) {

        // 요청자가 보호자인지 확인
        if (!authUserInfo.getUserId().equals(request.getGuardianId())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.createReservation(request);
        return ResponseDto.success("예약이 성공적으로 생성되었습니다.", response);
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> getReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 예약 당사자 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        return ResponseDto.success("예약 상세 정보 조회 성공", reservation);
    }

    @GetMapping("/reservations")
    public ResponseDto<Page<ReservationResponse>> getReservations(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ModelAttribute ReservationSearchRequest searchRequest,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // ADMIN만 전체 예약 목록 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            // 일반 사용자는 자신과 관련된 예약만 검색 가능하도록 제한
            if (searchRequest.getGuardianId() != null &&
                    !searchRequest.getGuardianId().equals(authUserInfo.getUserId())) {
                throw new UnauthorizedReservationAccessException();
            }

            if (searchRequest.getCaregiverId() != null &&
                    !searchRequest.getCaregiverId().equals(authUserInfo.getUserId())) {
                throw new UnauthorizedReservationAccessException();
            }

            // 사용자 ID로 필터링 설정
            if (searchRequest.getGuardianId() == null && searchRequest.getCaregiverId() == null) {
                // 보호자 또는 간병인 여부
                if (authUserInfo.getRole().equals(UserRole.GUARDIAN.getValue())) {
                    searchRequest.setGuardianId(authUserInfo.getUserId());
                } else if (authUserInfo.getRole().equals(UserRole.CAREGIVER.getValue())) {
                    searchRequest.setCaregiverId(authUserInfo.getUserId());
                }
            }
        }

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

        // ADMIN만 전체 예약 목록 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getReservations(startDate, endDate, pageable);
        return ResponseDto.success("예약 목록 조회 성공", responses);
    }

    @GetMapping("/reservations/status/{status}")
    public ResponseDto<Page<ReservationResponse>> getReservationsByStatus(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable ReservationStatus status,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // ADMIN만 상태별 전체 예약 목록 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getReservationsByStatus(status, pageable);
        return ResponseDto.success("상태별 예약 목록 조회 성공", responses);
    }

    @GetMapping("/users/{userId}/reservations")
    public ResponseDto<Page<ReservationResponse>> getUserReservations(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // 본인의 예약 목록 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(userId) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getUserReservations(userId, startDate, endDate, pageable);
        return ResponseDto.success("사용자별 예약 목록 조회 성공", responses);
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> updateReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationUpdateRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 보호자 또는 ADMIN만 예약 수정 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.updateReservation(reservationId, request);
        return ResponseDto.success("예약이 성공적으로 수정되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/accept")
    public ResponseDto<ReservationResponse> acceptReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationAcceptRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 간병인 또는 ADMIN만 예약 수락 가능
        if (!authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.acceptReservation(reservationId, request.getCaregiverNote());
        return ResponseDto.success("예약이 성공적으로 수락되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/reject")
    public ResponseDto<ReservationResponse> rejectReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationRejectRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 간병인 또는 ADMIN만 예약 거절 가능
        if (!authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.rejectReservation(
                reservationId,
                request.getRejectionReason(),
                request.getSuggestedAlternative()
        );
        return ResponseDto.success("예약이 거절되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseDto<ReservationResponse> cancelReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationCancelRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 보호자 또는 ADMIN만 예약 취소 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.cancelReservation(
                reservationId,
                request.getCancelReason(),
                request.getCancellationNote()
        );
        return ResponseDto.success("예약이 성공적으로 취소되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/complete")
    public ResponseDto<ReservationResponse> completeReservation(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 간병인, 보호자 또는 ADMIN만 예약 완료 처리 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.completeReservation(reservationId);
        return ResponseDto.success("서비스가 성공적으로 완료되었습니다.", response);
    }

    @PatchMapping("/reservations/{reservationId}/payment")
    public ResponseDto<ReservationResponse> linkPayment(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reservationId,
            @RequestBody @Valid PaymentLinkRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 보호자 또는 ADMIN만 결제 연결 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.linkPayment(reservationId, request.getPaymentId());
        return ResponseDto.success("결제 정보가 연결되었습니다.", response);
    }

    @GetMapping("/reservations/history")
    public ResponseDto<Page<ReservationResponse>> getReservationHistory(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // ADMIN만 전체 예약 이력 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getReservationHistory(startDate, endDate, pageable);
        return ResponseDto.success("예약 이력 조회 성공", responses);
    }

    @GetMapping("/users/{userId}/reservations/history")
    public ResponseDto<Page<ReservationResponse>> getUserReservationHistory(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // 본인의 예약 이력 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(userId) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN.getValue())) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getUserReservationHistory(userId, startDate, endDate, pageable);
        return ResponseDto.success("사용자별 예약 이력 조회 성공", responses);
    }
}