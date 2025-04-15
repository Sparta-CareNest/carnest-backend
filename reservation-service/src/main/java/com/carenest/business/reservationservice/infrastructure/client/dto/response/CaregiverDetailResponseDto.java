package com.carenest.business.reservationservice.infrastructure.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverDetailResponseDto {
    private UUID id;
    private UUID userId;
    private String description;
    private Double rating;
    private Integer experienceYears;
    private Integer pricePerHour;
    private Integer pricePerDay;
    private Boolean approvalStatus;
    private String gender;
    private List<String> categoryService;
    private List<String> categoryLocation;
}