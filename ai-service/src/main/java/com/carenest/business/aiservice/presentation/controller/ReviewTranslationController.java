package com.carenest.business.aiservice.presentation.controller;

import com.carenest.business.aiservice.application.service.ReviewTranslationService;
import com.carenest.business.common.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/translate")
@RequiredArgsConstructor
public class ReviewTranslationController {

    private final ReviewTranslationService translationService;

    @PostMapping
    public ResponseDto<String> translate(@RequestBody String reviewText) {
        String translated = translationService.translateReview(reviewText);
        return ResponseDto.success("번역이 완료되었습니다.", translated);
    }

    // 리뷰 ID로 번역
    @GetMapping("/{reviewId}")
    public ResponseDto<String> translateByReviewId(@PathVariable UUID reviewId) {
        String translated = translationService.translateReviewById(reviewId);
        return ResponseDto.success("리뷰 번역이 완료되었습니다.", translated);
    }
}
