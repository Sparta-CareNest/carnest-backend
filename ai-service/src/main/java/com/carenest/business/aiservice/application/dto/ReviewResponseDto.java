package com.carenest.business.aiservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private UUID reviewId;
    private UUID caregiverId;
    private UUID userId;
    private int rating;
    private String content;
}
