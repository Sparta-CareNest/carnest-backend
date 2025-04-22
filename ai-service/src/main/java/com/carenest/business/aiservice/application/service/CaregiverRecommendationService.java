package com.carenest.business.aiservice.application.service;

import com.carenest.business.aiservice.application.dto.CaregiverRecommendRequestDto;
import com.carenest.business.aiservice.application.dto.CaregiverSearchConditionRequestDto;
import com.carenest.business.aiservice.exception.AiException;
import com.carenest.business.aiservice.exception.ErrorCode;
import com.carenest.business.aiservice.infrastructure.client.CaregiverInternalClient;
import com.carenest.business.aiservice.infrastructure.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaregiverRecommendationService {

    private final GeminiClient geminiClient;
    private final CaregiverInternalClient caregiverInternalClient;

    public List<UUID> recommendCaregivers(CaregiverRecommendRequestDto requestDto) {
        try {
            CaregiverSearchConditionRequestDto condition = geminiClient.extractConditions(requestDto.getQuery());

            return caregiverInternalClient.searchCaregivers(
                    condition.getLocation(),
                    condition.getGender(),
                    condition.getExperienceYears(),
                    condition.getAverageRating()
            );
        } catch (Exception e){
            log.error("간병인 추천 과정 중 오류 발생", e);
            throw new AiException(ErrorCode.CAREGIVER_RECOMMENDATION_FAILED);
        }
    }
}
