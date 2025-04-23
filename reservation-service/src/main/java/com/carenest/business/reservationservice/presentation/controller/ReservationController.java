package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.model.UserRole;
import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.dto.request.*;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.carenest.business.reservationservice.exception.UnauthorizedReservationAccessException;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "예약 생성",
            description = "새로운 예약을 생성합니다. 보호자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping("/reservations")
    public ResponseDto<ReservationResponse> createReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 생성 요청 정보", required = true) @RequestBody @Valid ReservationCreateRequest request) {

        // 토큰에서 추출한 사용자 ID 사용
        ReservationResponse response = reservationService.createReservation(request, authUserInfo.getUserId());
        return ResponseDto.success("예약이 성공적으로 생성되었습니다.", response);
    }

    @Operation(
            summary = "예약 상세 조회",
            description = "예약 ID를 통해 예약 상세 정보를 조회합니다. 예약 당사자 또는 관리자만 조회 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 상세 정보 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> getReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회할 예약 ID", required = true) @PathVariable UUID reservationId) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 예약 당사자 또는 ADMIN만 조회 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        return ResponseDto.success("예약 상세 정보 조회 성공", reservation);
    }

    @Operation(
            summary = "예약 목록 조회",
            description = "조건에 맞는 예약 목록을 조회합니다. 관리자는 모든 예약을 볼 수 있고, 일반 사용자는 자신과 관련된 예약만 볼 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음")
            }
    )
    @GetMapping("/reservations")
    public ResponseDto<Page<ReservationResponse>> getReservations(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "예약 검색 조건") @ModelAttribute ReservationSearchRequest searchRequest,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        // ADMIN만 전체 예약 목록 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
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
                if (authUserInfo.getRole().equals(UserRole.GUARDIAN)) {
                    searchRequest.setGuardianId(authUserInfo.getUserId());
                } else if (authUserInfo.getRole().equals(UserRole.CAREGIVER)) {
                    searchRequest.setCaregiverId(authUserInfo.getUserId());
                }
            }
        }

        // 날짜 설정이 없으면 요청 파라미터의 값 사용
        if (searchRequest.getStartDate() == null) {
            searchRequest.setStartDate(startDate);
        }
        if (searchRequest.getEndDate() == null) {
            searchRequest.setEndDate(endDate);
        }

        // 검색 조건이 있거나 일반 사용자인 경우
        if (!authUserInfo.getRole().equals(UserRole.ADMIN) ||
                searchRequest.getGuardianId() != null ||
                searchRequest.getCaregiverId() != null ||
                searchRequest.getPatientName() != null ||
                searchRequest.getStatus() != null ||
                searchRequest.getStartDate() != null ||
                searchRequest.getEndDate() != null) {

            Page<ReservationResponse> responses = reservationService.searchReservations(searchRequest, pageable);
            return ResponseDto.success("예약 검색 성공", responses);
        }

        // ADMIN의 전체 예약 목록 조회
        Page<ReservationResponse> responses = reservationService.getReservations(startDate, endDate, pageable);
        return ResponseDto.success("예약 목록 조회 성공", responses);
    }

    @Operation(
            summary = "상태별 예약 목록 조회",
            description = "특정 상태의 예약 목록을 조회합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태별 예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
            }
    )
    @GetMapping("/reservations/status/{status}")
    public ResponseDto<Page<ReservationResponse>> getReservationsByStatus(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 상태", required = true) @PathVariable ReservationStatus status,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // ADMIN만 상태별 전체 예약 목록 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getReservationsByStatus(status, pageable);
        return ResponseDto.success("상태별 예약 목록 조회 성공", responses);
    }

    @Operation(
            summary = "내 예약 목록 조회",
            description = "현재 로그인한 사용자의 예약 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/my/reservations")
    public ResponseDto<Page<ReservationResponse>> getMyReservations(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // 토큰에서 추출한 사용자 ID 사용
        Page<ReservationResponse> responses = reservationService.getUserReservations(
                authUserInfo.getUserId(), startDate, endDate, pageable);
        return ResponseDto.success("내 예약 목록 조회 성공", responses);
    }

    @Operation(
            summary = "사용자별 예약 목록 조회 (관리자용)",
            description = "특정 사용자의 예약 목록을 조회합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자별 예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
            }
    )
    @GetMapping("/admin/users/{userId}/reservations")
    public ResponseDto<Page<ReservationResponse>> getUserReservationsAdmin(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "사용자 ID", required = true) @PathVariable UUID userId,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // ADMIN만 다른 사용자의 예약 목록 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getUserReservations(userId, startDate, endDate, pageable);
        return ResponseDto.success("사용자별 예약 목록 조회 성공", responses);
    }

    @Operation(
            summary = "예약 정보 수정",
            description = "예약 정보를 수정합니다. 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> updateReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "예약 수정 정보", required = true) @RequestBody @Valid ReservationUpdateRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 보호자 또는 ADMIN만 예약 수정 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.updateReservation(reservationId, request);
        return ResponseDto.success("예약이 성공적으로 수정되었습니다.", response);
    }

    @Operation(
            summary = "예약 수락",
            description = "간병인이 예약을 수락합니다. 간병인 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 수락 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}/accept")
    public ResponseDto<ReservationResponse> acceptReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "간병인 메모 정보", required = true) @RequestBody @Valid ReservationAcceptRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 간병인 또는 ADMIN만 예약 수락 가능
        if (!authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.acceptReservation(reservationId, request.getCaregiverNote());
        return ResponseDto.success("예약이 성공적으로 수락되었습니다.", response);
    }

    @Operation(
            summary = "예약 거절",
            description = "간병인이 예약을 거절합니다. 간병인 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 거절 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}/reject")
    public ResponseDto<ReservationResponse> rejectReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "거절 정보", required = true) @RequestBody @Valid ReservationRejectRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 간병인 또는 ADMIN만 예약 거절 가능
        if (!authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.rejectReservation(
                reservationId,
                request.getRejectionReason(),
                request.getSuggestedAlternative()
        );
        return ResponseDto.success("예약이 거절되었습니다.", response);
    }

    @Operation(
            summary = "예약 취소",
            description = "예약을 취소합니다. 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseDto<ReservationResponse> cancelReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "취소 정보", required = true) @RequestBody @Valid ReservationCancelRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 보호자 또는 ADMIN만 예약 취소 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.cancelReservation(
                reservationId,
                request.getCancelReason(),
                request.getCancellationNote()
        );
        return ResponseDto.success("예약이 성공적으로 취소되었습니다.", response);
    }

    @Operation(
            summary = "예약 완료 처리",
            description = "예약을 완료 상태로 변경합니다. 간병인, 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 완료 처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}/complete")
    public ResponseDto<ReservationResponse> completeReservation(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 간병인, 보호자 또는 ADMIN만 예약 완료 처리 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getUserId().equals(reservation.getCaregiverId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.completeReservation(reservationId);
        return ResponseDto.success("서비스가 성공적으로 완료되었습니다.", response);
    }

    @Operation(
            summary = "결제 정보 연결",
            description = "예약에 결제 정보를 연결합니다. 보호자 또는 관리자만 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 정보 연결 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "이미 결제 정보가 연결된 예약")
            }
    )
    @PatchMapping("/reservations/{reservationId}/payment")
    public ResponseDto<ReservationResponse> linkPayment(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "결제 연결 정보", required = true) @RequestBody @Valid PaymentLinkRequest request) {

        ReservationResponse reservation = reservationService.getReservation(reservationId);

        // 보호자 또는 ADMIN만 결제 연결 가능
        if (!authUserInfo.getUserId().equals(reservation.getGuardianId()) &&
                !authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        ReservationResponse response = reservationService.linkPayment(reservationId, request.getPaymentId());
        return ResponseDto.success("결제 정보가 연결되었습니다.", response);
    }

    @Operation(
            summary = "예약 이력 조회 (관리자용)",
            description = "시스템 내 모든 예약 이력을 조회합니다. 관리자 권한이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
            }
    )
    @GetMapping("/admin/reservations/history")
    public ResponseDto<Page<ReservationResponse>> getReservationHistory(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // ADMIN만 전체 예약 이력 조회 가능
        if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedReservationAccessException();
        }

        Page<ReservationResponse> responses = reservationService.getReservationHistory(startDate, endDate, pageable);
        return ResponseDto.success("예약 이력 조회 성공", responses);
    }

    @Operation(
            summary = "내 예약 이력 조회",
            description = "현재 로그인한 사용자의 예약 이력을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 예약 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/my/reservations/history")
    public ResponseDto<Page<ReservationResponse>> getMyReservationHistory(
            @Parameter(hidden = true) @AuthUser AuthUserInfo authUserInfo,
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 정보") @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        // 토큰에서 추출한 사용자 ID 사용
        Page<ReservationResponse> responses = reservationService.getUserReservationHistory(
                authUserInfo.getUserId(), startDate, endDate, pageable);
        return ResponseDto.success("내 예약 이력 조회 성공", responses);
    }
}