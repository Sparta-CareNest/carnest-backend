package com.carenest.business.caregiverservice.application.dto.response;

import java.util.List;
import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

public record CaregiverUpdateResponseServiceDTO (
	UUID id,
	UUID userId,
	String description,
	Double rating,
	Integer experienceYears,
	Integer pricePerHour,
	Integer pricePerDay,
	Boolean approvalStatus,
	GenderType gender,
	List<String> categoryService,
	List<String> categoryLocation
) {
}
