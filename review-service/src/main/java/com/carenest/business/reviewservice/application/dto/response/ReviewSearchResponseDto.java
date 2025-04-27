package com.carenest.business.reviewservice.application.dto.response;

import com.carenest.business.reviewservice.domain.model.Review;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReviewSearchResponseDto {

    private UUID reviewId;
    private UUID caregiverId;
    private double rating;
    private String content;

    public static ReviewSearchResponseDto fromEntity(Review review) {
        return ReviewSearchResponseDto.builder()
                .reviewId(review.getReviewId())
                .caregiverId(review.getCaregiverId())
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }
}
