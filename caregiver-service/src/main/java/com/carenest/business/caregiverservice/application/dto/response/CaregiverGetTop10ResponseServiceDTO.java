package com.carenest.business.caregiverservice.application.dto.response;


import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

public record CaregiverGetTop10ResponseServiceDTO(
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
