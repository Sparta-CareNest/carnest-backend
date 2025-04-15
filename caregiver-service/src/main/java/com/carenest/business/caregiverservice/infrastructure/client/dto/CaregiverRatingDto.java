package com.carenest.business.caregiverservice.infrastructure.client.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CaregiverRatingDto {

	private UUID caregiverId;
	private double averageRating;
	private long reviewCount;

}