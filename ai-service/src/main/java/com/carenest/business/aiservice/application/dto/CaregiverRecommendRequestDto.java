package com.carenest.business.aiservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CaregiverRecommendRequestDto {
    @NotBlank(message = "검색 쿼리는 필수 입력값입니다")
    private String query;
}
