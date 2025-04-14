package com.carenest.business.caregiverservice.presentation.dto.response;

import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

public record CaregiverGetTop10ResponseDTO(
	UUID id,
	UUID userId,
	String description,
	Double averageRating,
	Long reviewCount,
	Integer experienceYears,
	Integer pricePerHour,
	Integer pricePerDay,
	GenderType gender
) {
}
