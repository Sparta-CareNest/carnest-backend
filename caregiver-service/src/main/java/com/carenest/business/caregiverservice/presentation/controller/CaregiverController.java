package com.carenest.business.caregiverservice.presentation.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.service.CaregiverService;
import com.carenest.business.caregiverservice.presentation.dto.mapper.CaregiverPresentationMapper;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverCreateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverCreateResponseDTO;
import com.carenest.business.common.response.ResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/caregivers")
public class CaregiverController {

	private final CaregiverService caregiverService;

	@Qualifier("caregiverPresentationMapper")
	private final CaregiverPresentationMapper presentationMapper;

	// Create
	@PostMapping
	public ResponseDto<CaregiverCreateResponseDTO> createCaregiver(
		@RequestBody CaregiverCreateRequestDTO createRequestDTO
	){
		CaregiverCreateRequestServiceDTO requestServiceDTO = presentationMapper.toCreateServiceDto(createRequestDTO);
		CaregiverCreateResponseServiceDTO responseDTO = caregiverService.createCaregiver(requestServiceDTO);
		return ResponseDto.success("서비스 등록 요청이 접수되었습니다. 관리자 승인 후 활성화됩니다.",presentationMapper.toCreateResponseDto(responseDTO));
	}

	// Read

	// Update

	// Delete


}
