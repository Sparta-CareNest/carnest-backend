package com.carenest.business.caregiverservice.presentation.dto.request;

import java.util.List;
import java.util.UUID;

import com.carenest.business.caregiverservice.domain.model.GenderType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Using record for immutable DTO
public record CaregiverCreateRequestDTO(
	@NotBlank @Size(max = 500) String description,
	@NotNull @Min(0) Integer experienceYears,
	@NotNull @Min(0) Integer pricePerHour,
	@NotNull @Min(0) Integer pricePerDay,
	@NotNull GenderType gender,
	@NotNull @Size(min = 1) List<Long> categoryLocationIds,
	@NotNull @Size(min = 1) List<Long> categoryServiceIds
) {}