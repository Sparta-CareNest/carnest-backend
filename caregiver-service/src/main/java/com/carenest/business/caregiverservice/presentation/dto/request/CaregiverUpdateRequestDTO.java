package com.carenest.business.caregiverservice.presentation.dto.request;

import java.util.List;

public record CaregiverUpdateRequestDTO(
	String description,
	Integer experienceYears,
	Integer pricePerHour,
	Integer pricePerDay,
	List<Long> categoryServiceIds,
	List<Long> categoryLocationIds
) {}