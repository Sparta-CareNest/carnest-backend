package com.carenest.business.caregiverservice.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.caregiverservice.application.service.CaregiverApprovalService;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationAcceptRequest;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationRejectRequest;
import com.carenest.business.caregiverservice.presentation.dto.response.PendingApprovalResponse;
import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.response.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import lombok.RequiredArgsConstructor;

@Tag(name = "CaregiverApproval", description = "간병인 예약 승인/거절 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/caregiver-approvals")
public class CaregiverApprovalController {

	private final CaregiverApprovalService caregiverApprovalService;

	@Operation(
		summary = "대기 중인 예약 목록 조회",
		description = "간병인 계정으로 로그인한 유저의 예약 승인 대기 목록을 조회합니다."
	)
	@GetMapping("/pending")
	public ResponseDto<List<PendingApprovalResponse>> getPendingApprovals(
		@AuthUser AuthUserInfo authUserInfo
	){
		List<PendingApprovalResponse> responses = caregiverApprovalService.getPendingApprovals(authUserInfo.getUserId());
		return ResponseDto.success(responses);
	}

	@Operation(
		summary = "예약 수락",
		description = "예약 ID를 기반으로 예약을 수락합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "예약 수락 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 처리된 예약"),
			@ApiResponse(responseCode = "403", description = "접근 권한 없음")
		}
	)
	@PatchMapping("/{reservationId}/accept")
	public ResponseDto<Void> acceptCaregiverReservation(
		@Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
		@Parameter(description = "예약 수락 요청 바디", required = true) @RequestBody ReservationAcceptRequest request,
		@AuthUser AuthUserInfo authUserInfo
	){
		caregiverApprovalService.acceptCaregiverReservation(reservationId, request, authUserInfo.getUserId());
		return ResponseDto.success("예약을 수락하셨습니다.");
	}

	@Operation(
		summary = "예약 거절",
		description = "예약 ID를 기반으로 예약을 거절합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "예약 거절 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 처리된 예약"),
			@ApiResponse(responseCode = "403", description = "접근 권한 없음")
		}
	)
	@PatchMapping("/{reservationId}/reject")
	public ResponseDto<Void> rejectCaregiverReservation(
		@Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
		@Parameter(description = "예약 거절 요청 바디", required = true) @RequestBody ReservationRejectRequest request,
		@AuthUser AuthUserInfo authUserInfo
	){
		caregiverApprovalService.rejectCaregiverReservation(reservationId, request, authUserInfo.getUserId());
		return ResponseDto.success("예약을 거절하셨습니다.");
	}
}