package com.carenest.business.caregiverservice.presentation.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.caregiverservice.application.service.CaregiverService;
import com.carenest.business.common.response.ResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/caregivers")
public class CaregiverInternalController {

	private final CaregiverService caregiverService;
	// TODO: GateWay 에서 EndPoint 로 관리자 접근만 하게 적용


	// 관리자 승인 API
	@PatchMapping("/{id}/status")
	public ResponseDto<Void> updateCaregiverStatus(
		@PathVariable UUID id,
		@RequestParam boolean approvalStatusCheck
	){
		caregiverService.updateCaregiverStatus(id,approvalStatusCheck);
		return ResponseDto.success(null);
	}

	// 간병인이 존재하는지 확인하는 API
	@GetMapping("/{id}")
	public Boolean isExistedCaregiver(@PathVariable UUID id) {
		return caregiverService.existsById(id);
	}

	// 간병인 전체 조회 API


}
