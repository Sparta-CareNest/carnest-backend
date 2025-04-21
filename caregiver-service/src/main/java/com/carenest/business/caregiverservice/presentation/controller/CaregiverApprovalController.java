package com.carenest.business.caregiverservice.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.caregiverservice.application.service.CaregiverApprovalService;
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

}
