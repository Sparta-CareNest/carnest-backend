package com.carenest.business.caregiverservice.presentation.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.service.CaregiverService;
import com.carenest.business.caregiverservice.presentation.dto.mapper.CaregiverPresentationMapper;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverReadResponseDTO;
import com.carenest.business.caregiverservice.util.PageableUtils;
import com.carenest.business.common.response.ResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/caregivers")
public class CaregiverInternalController {

	private final CaregiverService caregiverService;

	@Qualifier("caregiverPresentationMapper")
	private final CaregiverPresentationMapper presentationMapper;

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

	// (관리자) 간병인 전체 조회 API
	@GetMapping
	public ResponseDto<Page<CaregiverReadResponseDTO>> getCaregiverAll(
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size,
		@RequestParam(required = false) String sortDirection,
		@RequestParam(required = false) String sortProperty
	){
		Pageable pageable = PageableUtils.customPageable(page,size,sortDirection,sortProperty);
		Page<CaregiverReadResponseServiceDTO> responseDTO = caregiverService.getCaregiverAll(pageable);

		return ResponseDto.success(presentationMapper.toReadAllResponseDto(responseDTO));
	}


}
