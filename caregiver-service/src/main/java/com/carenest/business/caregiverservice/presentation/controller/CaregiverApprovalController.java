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

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/caregiver-approvals")
public class CaregiverApprovalController {

	private final CaregiverApprovalService caregiverApprovalService;

	@GetMapping("/pending")
	public ResponseDto<List<PendingApprovalResponse>> getPendingApprovals(
		@AuthUser AuthUserInfo authUserInfo
	){
		List<PendingApprovalResponse> responses = caregiverApprovalService.getPendingApprovals(authUserInfo.getUserId());
		return ResponseDto.success(responses);
	}

	@PatchMapping("/{reservationId}/accept")
	public ResponseDto<Void> acceptCaregiverReservation(
		@PathVariable UUID reservationId,
		@RequestBody ReservationAcceptRequest request,
		@AuthUser AuthUserInfo authUserInfo
	){
		caregiverApprovalService.acceptCaregiverReservation(reservationId,request,authUserInfo.getUserId());
		return ResponseDto.success("예약을 수락하셨습니다.");
	}

	@PatchMapping("/{reservationId}/reject")
	public ResponseDto<Void> rejectCaregiverReservation(
		@PathVariable UUID reservationId,
		@RequestBody ReservationRejectRequest request,
		@AuthUser AuthUserInfo authUserInfo
	){
		caregiverApprovalService.rejectCaregiverReservation(reservationId,request,authUserInfo.getUserId());
		return ResponseDto.success("예약을 수락하셨습니다.");
	}

}
