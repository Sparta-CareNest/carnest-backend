package com.carenest.business.caregiverservice.presentation.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.service.CaregiverService;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverReadResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.mapper.CaregiverPresentationMapper;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverCreateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverCreateResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverUpdateResponseDTO;
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

	// Read (개인 조회)
	@GetMapping("/{caregiverId}")
	public ResponseDto<CaregiverReadResponseDTO> getCaregiverDetail(@PathVariable UUID caregiverId){
		CaregiverReadResponseServiceDTO responseDTO = caregiverService.getCaregiver(caregiverId);
		return ResponseDto.success(presentationMapper.toReadResponseDto(responseDTO));
	}

	// Update
	@PatchMapping("/{caregiverId}")
	public ResponseDto<CaregiverUpdateResponseDTO> updateCaregiver(
		@PathVariable UUID caregiverId,
		@RequestBody CaregiverUpdateRequestDTO requestDTO
	){
		CaregiverUpdateResponseServiceDTO responseDTO = caregiverService.updateCaregiver(caregiverId,requestDTO);
		return ResponseDto.success("간병인 정보가 수정되었습니다.",presentationMapper.toUpdateResponseDto(responseDTO));
	}

	// Delete
	@DeleteMapping("/{caregiverId}")
	public ResponseDto<Void> deleteCaregiver(
		@PathVariable UUID caregiverId
	){
		caregiverService.deleteCaregiver(caregiverId);
		return ResponseDto.success("간병인 정보가 삭제되었습니다.",null);
	}


}
