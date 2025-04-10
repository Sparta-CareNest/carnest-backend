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
	List<String> imageUrls
) {
	public CaregiverCreateRequestServiceDTO withImageUrls(List<String> newImageUrls) {
		return new CaregiverCreateRequestServiceDTO(
			userId,
			description,
			experienceYears,
			pricePerHour,
			pricePerDay,
			gender,
			categoryLocationIds,
			categoryServiceIds,
			newImageUrls
		);
	}

}

