package com.carenest.business.reviewservice.application.dto.response;

import com.carenest.business.reviewservice.domain.model.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewCreateResponseDto {
    private UUID reviewId;
    private UUID reservationId;
    private UUID caregiverId;
    private UUID userId;
    private double rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewCreateResponseDto fromEntity(Review review) {
        return ReviewCreateResponseDto.builder()
                .reviewId(review.getReviewId())
                .reservationId(review.getReservationId())
                .caregiverId(review.getCaregiverId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

