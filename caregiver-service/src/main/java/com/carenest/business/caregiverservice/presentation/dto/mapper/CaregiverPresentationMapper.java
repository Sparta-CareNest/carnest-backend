package com.carenest.business.caregiverservice.presentation.dto.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverSearchResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverCreateRequestDTO;
import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverCreateResponseDTO;
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

	default Page<CaregiverSearchResponseDTO> toSearchResponseDto(Page<CaregiverSearchResponseServiceDTO> serviceDTOs) {
		return serviceDTOs.map(this::toSearchResponseDto);
	}
}
