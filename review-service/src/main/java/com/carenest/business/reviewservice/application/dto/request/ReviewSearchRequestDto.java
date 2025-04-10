package com.carenest.business.reviewservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReviewSearchRequestDto {
    private UUID caregiverId;
    private Integer rating;
}