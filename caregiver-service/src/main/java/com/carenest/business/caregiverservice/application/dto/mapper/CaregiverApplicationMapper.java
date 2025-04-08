package com.carenest.business.caregiverservice.application.dto.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;

import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;

@Mapper(componentModel = "spring")
public interface CaregiverApplicationMapper {

	CaregiverCreateResponseServiceDTO toCreateResponseServiceDTO(UUID id);
}
