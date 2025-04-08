package com.carenest.business.reviewservice.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReviewCreateRequestDto {
    @NotNull
    private UUID reservationId;

    @NotNull
    private UUID caregiverId;

    @Min(1)
    @Max(5)
    private int rating;

    @NotBlank
    private String content;

    }


