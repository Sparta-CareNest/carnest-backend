package com.carenest.business.caregiverservice.infrastructure.client.dto;

import java.util.UUID;

public class CaregiverTopRatingDto {

	private UUID caregiverId;
	private double averageRating;
	private long reviewCount;

	public CaregiverTopRatingDto(UUID caregiverId, double averageRating, long reviewCount) {
		this.caregiverId = caregiverId;
		this.averageRating = averageRating;
		this.reviewCount = reviewCount;
	}

	public UUID getCaregiverId() {
		return caregiverId;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public long getReviewCount() {
		return reviewCount;
	}
}
