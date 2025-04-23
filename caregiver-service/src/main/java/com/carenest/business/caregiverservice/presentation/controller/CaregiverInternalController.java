package com.carenest.business.caregiverservice.presentation.controller;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.service.CaregiverService;
import com.carenest.business.caregiverservice.domain.model.GenderType;
import com.carenest.business.caregiverservice.presentation.dto.mapper.CaregiverPresentationMapper;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverReadResponseDTO;
import com.carenest.business.caregiverservice.util.PageableUtils;
import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.exception.BaseException;
import com.carenest.business.common.exception.CommonErrorCode;
import com.carenest.business.common.model.UserRole;
import com.carenest.business.common.response.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@Tag(name = "InternalCaregiver", description = "내부 관리자용 간병인 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/caregivers")
public class CaregiverInternalController {

	private final CaregiverService caregiverService;

	@Qualifier("caregiverPresentationMapper")
	private final CaregiverPresentationMapper presentationMapper;

	@Operation(summary = "간병인 상태 승인/거절", description = "관리자가 간병인의 활동 상태를 승인 또는 거절합니다.")
	@PatchMapping("/{id}/status")
	public ResponseDto<Void> updateCaregiverStatus(
		@Parameter(description = "간병인 ID", required = true) @PathVariable UUID id,
		@Parameter(description = "승인 여부 (true: 승인, false: 거절)", required = true) @RequestParam boolean approvalStatusCheck,
		@AuthUser AuthUserInfo authUserInfo
	){
		if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
			throw new BaseException(CommonErrorCode.FORBIDDEN);
		}
		caregiverService.updateCaregiverStatus(id, approvalStatusCheck);
		return ResponseDto.success(null);
	}

	@Operation(summary = "간병인 존재 여부 확인", description = "지정한 간병인 ID가 존재하는지 확인합니다.")
	@GetMapping("/{id}")
	public Boolean isExistedCaregiver(
		@Parameter(description = "간병인 ID", required = true) @PathVariable UUID id
	) {
		return caregiverService.existsById(id);
	}

	@Operation(summary = "간병인 전체 목록 조회", description = "관리자가 전체 간병인 목록을 페이지 단위로 조회합니다.")
	@GetMapping
	public ResponseDto<Page<CaregiverReadResponseDTO>> getCaregiverAll(
		@Parameter(description = "페이지 번호") @RequestParam(required = false) Integer page,
		@Parameter(description = "페이지 크기") @RequestParam(required = false) Integer size,
		@Parameter(description = "정렬 방향 (asc/desc)") @RequestParam(required = false) String sortDirection,
		@Parameter(description = "정렬 속성") @RequestParam(required = false) String sortProperty,
		@AuthUser AuthUserInfo authUserInfo
	){
		if (!authUserInfo.getRole().equals(UserRole.ADMIN)) {
			throw new BaseException(CommonErrorCode.FORBIDDEN);
		}
		Pageable pageable = PageableUtils.customPageable(page, size, sortDirection, sortProperty);
		Page<CaregiverReadResponseServiceDTO> responseDTO = caregiverService.getCaregiverAll(pageable);
		return ResponseDto.success(presentationMapper.toReadAllResponseDto(responseDTO));
	}

	@Operation(summary = "필터 기반 간병인 ID 리스트 조회", description = "조건에 맞는 간병인 ID 목록을 필터링하여 조회합니다.")
	@GetMapping("/search")
	public List<UUID> getCaregiverIdsByFilters(
		@Parameter(description = "지역") @RequestParam(required = false) String location,
		@Parameter(description = "성별 (MALE / FEMALE)") @RequestParam(required = false) GenderType gender,
		@Parameter(description = "경력 (년)") @RequestParam(required = false) Integer experienceYears,
		@Parameter(description = "평점") @RequestParam(required = false) Double rating
	){
		return caregiverService.getCaregiverIdsByFilters(location, gender, experienceYears, rating);
	}
}
