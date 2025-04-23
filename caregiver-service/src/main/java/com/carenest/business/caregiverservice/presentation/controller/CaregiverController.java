package com.carenest.business.caregiverservice.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.BulkCaregiverTop10Response;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverSearchResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.service.CaregiverService;
import com.carenest.business.caregiverservice.presentation.dto.mapper.CaregiverPresentationMapper;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverCreateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverCreateResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverReadResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverSearchResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverUpdateResponseDTO;
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
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Caregiver", description = "간병인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/caregivers")
@Slf4j
public class CaregiverController {

	private final CaregiverService caregiverService;

	@Qualifier("caregiverPresentationMapper")
	private final CaregiverPresentationMapper presentationMapper;

	@Operation(summary = "간병인 등록", description = "간병인이 자신의 정보를 등록합니다. 관리자 승인 후 활성화됩니다.")
	@PostMapping(consumes = "multipart/form-data")
	public ResponseDto<CaregiverCreateResponseDTO> createCaregiver(
		@Parameter(description = "간병인 등록 요청 DTO") @RequestPart("data") CaregiverCreateRequestDTO createRequestDTO,
		@Parameter(description = "간병인 관련 문서 이미지 목록") @RequestPart(value = "images", required = false) List<MultipartFile> multipartFiles,
		@AuthUser AuthUserInfo authUserInfo) {
		if (!authUserInfo.getRole().equals(UserRole.CAREGIVER)) {
			throw new BaseException(CommonErrorCode.FORBIDDEN);
		}
		CaregiverCreateRequestServiceDTO requestServiceDTO = presentationMapper.toCreateServiceDto(createRequestDTO);
		CaregiverCreateResponseServiceDTO responseDTO = caregiverService.createCaregiver(requestServiceDTO,
			multipartFiles, authUserInfo.getUserId());
		return ResponseDto.success("서비스 등록 요청이 접수되었습니다. 관리자 승인 후 활성화됩니다.",
			presentationMapper.toCreateResponseDto(responseDTO));
	}

	@Operation(summary = "내 간병인 정보 조회", description = "현재 로그인된 간병인의 상세 정보를 조회합니다.")
	@GetMapping
	public ResponseDto<CaregiverReadResponseDTO> getCaregiverDetail(@AuthUser AuthUserInfo authUserInfo) {
		CaregiverReadResponseServiceDTO responseDTO = caregiverService.getCaregiver(authUserInfo.getUserId());
		return ResponseDto.success(presentationMapper.toReadResponseDto(responseDTO));
	}

	@Operation(summary = "간병인 상세 조회", description = "지정한 간병인 ID로 간병인 상세 정보를 조회합니다.")
	@GetMapping("/{caregiverId}")
	public ResponseDto<CaregiverReadResponseDTO> getCaregiverDetailUser(
		@Parameter(description = "간병인 ID") @PathVariable UUID caregiverId) {
		CaregiverReadResponseServiceDTO responseDTO = caregiverService.getCaregiverDetailUser(caregiverId);
		return ResponseDto.success(presentationMapper.toReadResponseDto(responseDTO));
	}

	@Operation(summary = "간병인 정보 수정", description = "간병인이 자신의 정보를 수정합니다.")
	@PatchMapping
	public ResponseDto<CaregiverUpdateResponseDTO> updateCaregiver(
		@Parameter(description = "간병인 수정 요청 DTO") @RequestBody CaregiverUpdateRequestDTO requestDTO,
		@AuthUser AuthUserInfo authUserInfo) {
		CaregiverUpdateResponseServiceDTO responseDTO = caregiverService.updateCaregiver(authUserInfo.getUserId(),
			requestDTO);
		return ResponseDto.success("간병인 정보가 수정되었습니다.", presentationMapper.toUpdateResponseDto(responseDTO));
	}

	@Operation(summary = "간병인 삭제", description = "지정한 간병인 정보를 삭제합니다.")
	@DeleteMapping("/{caregiverId}")
	public ResponseDto<Void> deleteCaregiver(@Parameter(description = "삭제할 간병인 ID") @PathVariable UUID caregiverId,
		@AuthUser AuthUserInfo authUserInfo) {
		if (!authUserInfo.getRole().equals(UserRole.CAREGIVER)) {
			throw new BaseException(CommonErrorCode.FORBIDDEN);
		}
		caregiverService.deleteCaregiver(caregiverId, authUserInfo.getUserId());
		return ResponseDto.success("간병인 정보가 삭제되었습니다.", null);
	}

	@Operation(summary = "간병인 검색", description = "지역, 서비스 타입 등으로 간병인을 검색합니다.")
	@GetMapping("/search")
	public ResponseDto<Page<CaregiverSearchResponseDTO>> searchCaregiver(
		@Parameter(description = "페이지 번호") @RequestParam(required = false) Integer page,
		@Parameter(description = "페이지 사이즈") @RequestParam(required = false) Integer size,
		@Parameter(description = "정렬 방향") @RequestParam(required = false) String sortDirection,
		@Parameter(description = "정렬 기준") @RequestParam(required = false) String sortProperty,
		@Parameter(description = "지역 필터") @RequestParam(required = false) String location,
		@Parameter(description = "서비스 필터") @RequestParam(required = false) String service) {
		Pageable pageable = PageableUtils.customPageable(page, size, sortDirection, sortProperty);
		Page<CaregiverSearchResponseServiceDTO> responseServiceDTO = caregiverService.searchCaregiver(location, service,
			pageable);
		return ResponseDto.success("간병인 검색을 완료했습니다.", presentationMapper.toSearchResponseDto(responseServiceDTO));
	}

	@Operation(summary = "평점 상위 간병인 조회", description = "평점 기준 상위 10명의 간병인 목록을 조회합니다.")
	@GetMapping("/rating/top")
	public ResponseDto<BulkCaregiverTop10Response> getTop10Caregiver() {
		BulkCaregiverTop10Response responseServiceDTO = caregiverService.getTop10Caregiver();
		return ResponseDto.success(responseServiceDTO);
	}
}