package com.carenest.business.aiservice.presentation.controller;

import com.carenest.business.aiservice.application.service.ReviewTranslationService;
import com.carenest.business.common.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Ai-service", description = "Ai 서비스 API")
@RestController
@RequestMapping("/api/v1/ai/translate")
@RequiredArgsConstructor
public class ReviewTranslationController {

    private final ReviewTranslationService translationService;

    @Operation(summary = "Review 번역", description = "리뷰 번역 api ")
    @PostMapping
    public ResponseDto<String> translate(@RequestBody String reviewText) {
        String translated = translationService.translateReview(reviewText);
        return ResponseDto.success("번역이 완료되었습니다.", translated);
    }

    // 리뷰 ID로 번역
    @Operation(summary = "등록된 리뷰 조회해서 번역", description = "등록된 리뷰를 조회해서 번역하는 api ")
    @GetMapping("/{reviewId}")
    public ResponseDto<String> translateByReviewId(@PathVariable UUID reviewId) {
        String translated = translationService.translateReviewById(reviewId);
        return ResponseDto.success("리뷰 번역이 완료되었습니다.", translated);
    }
}
