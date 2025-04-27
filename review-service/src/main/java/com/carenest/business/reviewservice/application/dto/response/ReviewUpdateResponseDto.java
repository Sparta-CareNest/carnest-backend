package com.carenest.business.reviewservice.application.dto.response;

import com.carenest.business.reviewservice.domain.model.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewUpdateResponseDto {
    private UUID reviewId;
    private double rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewUpdateResponseDto fromEntity(Review review) {
        return ReviewUpdateResponseDto.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
