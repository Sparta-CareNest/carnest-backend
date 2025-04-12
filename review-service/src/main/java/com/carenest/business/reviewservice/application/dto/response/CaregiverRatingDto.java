package com.carenest.business.reviewservice.application.dto.response;

import java.util.UUID;

public class CaregiverRatingDto {

    private UUID caregiverId;
    private double averageRating;
    private long reviewCount;

    public CaregiverRatingDto(UUID caregiverId, double averageRating, long reviewCount) {
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
