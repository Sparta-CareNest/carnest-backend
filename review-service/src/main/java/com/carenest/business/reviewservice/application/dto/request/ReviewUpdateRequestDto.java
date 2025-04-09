package com.carenest.business.reviewservice.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewUpdateRequestDto {

    @Min(1)
    @Max(5)
    private double rating;

    @NotBlank
    private String content;

}
