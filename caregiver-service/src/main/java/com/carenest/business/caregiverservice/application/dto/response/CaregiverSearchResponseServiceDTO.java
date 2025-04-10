package com.carenest.business.caregiverservice.application.dto.response;

import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

public record CaregiverSearchResponseServiceDTO(
	UUID id,
	String description,
	Double rating,
	Integer experienceYears,
	Integer pricePerHour,
	Integer pricePerDay,
	GenderType gender
) {
}
