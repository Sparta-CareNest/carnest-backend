package com.carenest.business.caregiverservice.application.dto.request;

import java.util.List;
import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

public record CaregiverCreateRequestServiceDTO(
	UUID userId,
	String description,
	Integer experienceYears,
	Integer pricePerHour,
	Integer pricePerDay,
	GenderType gender,
	List<Long> categoryLocationIds,
	List<Long> categoryServiceIds,
	String submittedDocuments
) {

}

