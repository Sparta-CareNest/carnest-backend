package com.carenest.business.aiservice.presentation.controller;

import com.carenest.business.aiservice.application.dto.CaregiverRecommendRequestDto;
import com.carenest.business.aiservice.application.service.CaregiverRecommendationService;
import com.carenest.business.common.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Ai-service", description = "Ai 서비스 API")
@RestController
@RequestMapping("/api/v1/ai/recommend-caregivers")
@RequiredArgsConstructor
public class CaregiverRecommendationController {

    private final CaregiverRecommendationService recommendationService;

    @Operation(summary = "간병인 추천", description = "간병인 추천 api ")
    @PostMapping
    public ResponseDto<List<UUID>> recommendCaregivers(
           @Valid @RequestBody CaregiverRecommendRequestDto requestDto
    ) {
        List<UUID> caregiverIds = recommendationService.recommendCaregivers(requestDto);
        return ResponseDto.success("간병인 추천이 완료되었습니다.", caregiverIds);
    }
}
