package com.carenest.business.caregiverservice.presentation.dto.response;

import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

public record CaregiverSearchResponseDTO(
	UUID id,
	String description,
	Double rating,
	Integer experienceYears,
	Integer pricePerHour,
	Integer pricePerDay,
	GenderType gender
) {
}
