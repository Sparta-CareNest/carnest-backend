
 package com.carenest.business.caregiverservice.presentation.dto.mapper;

 import org.mapstruct.Mapper;

 import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
 import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
 import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverCreateRequestDTO;
 import com.carenest.business.caregiverservice.presentation.dto.response.CaregiverCreateResponseDTO;

 @Mapper(componentModel = "spring")
public interface CaregiverPresentationMapper {

	// presentation -> application
	CaregiverCreateRequestServiceDTO toCreateServiceDto(CaregiverCreateRequestDTO createRequestDTO);
	// application -> presentation (ResponseServiceDto -> ResponseDto)
	CaregiverCreateResponseDTO toCreateResponseDto(CaregiverCreateResponseServiceDTO responseDTO);
}
