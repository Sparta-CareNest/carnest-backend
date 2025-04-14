package com.carenest.business.caregiverservice.presentation.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverGetTop10ResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverSearchResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverCreateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverCreateResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverGetTop10ResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverReadResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverSearchResponseDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverUpdateResponseDTO;

@Mapper(componentModel = "spring")
public interface CaregiverPresentationMapper {

	// presentation -> application
	CaregiverCreateRequestServiceDTO toCreateServiceDto(CaregiverCreateRequestDTO createRequestDTO);


	// application -> presentation (ResponseServiceDto -> ResponseDto)
	CaregiverCreateResponseDTO toCreateResponseDto(CaregiverCreateResponseServiceDTO responseDTO);

	CaregiverReadResponseDTO toReadResponseDto(CaregiverReadResponseServiceDTO responseDTO);

	CaregiverUpdateResponseDTO toUpdateResponseDto(CaregiverUpdateResponseServiceDTO responseDTO);


	CaregiverSearchResponseDTO toSearchResponseDto(CaregiverSearchResponseServiceDTO dto);

	// Page 처리를 위해
	default Page<CaregiverSearchResponseDTO> toSearchResponseDto(Page<CaregiverSearchResponseServiceDTO> serviceDTOs) {
		return serviceDTOs.map(this::toSearchResponseDto);
	}

	// Page 처리를 위해
	default Page<CaregiverReadResponseDTO> toReadAllResponseDto(Page<CaregiverReadResponseServiceDTO> responseDTO) {
		return responseDTO.map(this::toReadResponseDto);
	}

	List<CaregiverGetTop10ResponseDTO> toGetTop10CaregiverDto(List<CaregiverGetTop10ResponseServiceDTO> responseServiceDTO);
}
