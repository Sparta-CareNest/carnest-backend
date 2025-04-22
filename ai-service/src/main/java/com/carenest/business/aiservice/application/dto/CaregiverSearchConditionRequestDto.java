package com.carenest.business.aiservice.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaregiverSearchConditionRequestDto {
    private String location;
    private String gender;
    private Integer experienceYears;
    private Double averageRating;
}
