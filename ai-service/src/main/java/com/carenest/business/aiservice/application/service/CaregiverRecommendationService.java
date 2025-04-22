package com.carenest.business.aiservice.application.service;

import com.carenest.business.aiservice.application.dto.CaregiverRecommendRequestDto;
import com.carenest.business.aiservice.application.dto.CaregiverSearchConditionRequestDto;
import com.carenest.business.aiservice.infrastructure.client.CaregiverInternalClient;
import com.carenest.business.aiservice.infrastructure.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaregiverRecommendationService {

    private final GeminiClient geminiClient;
    private final CaregiverInternalClient caregiverInternalClient;

    public List<UUID> recommendCaregivers(CaregiverRecommendRequestDto requestDto) {
        CaregiverSearchConditionRequestDto condition = geminiClient.extractConditions(requestDto.getQuery());

        return caregiverInternalClient.searchCaregivers(
                condition.getRegion(),
                condition.getGender(),
                condition.getExperienceYears(),
                condition.getAverageRating()
        );
    }
}
