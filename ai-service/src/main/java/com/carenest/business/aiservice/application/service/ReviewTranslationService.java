package com.carenest.business.aiservice.application.service;

import com.carenest.business.aiservice.application.dto.ReviewResponseDto;
import com.carenest.business.aiservice.exception.AiException;
import com.carenest.business.aiservice.exception.ErrorCode;
import com.carenest.business.aiservice.infrastructure.client.GeminiClient;
import com.carenest.business.aiservice.infrastructure.client.ReviewInternalClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewTranslationService {

    private final GeminiClient geminiClient;
    private final ReviewInternalClient reviewInternalClient;

    // 사용자가 직접 문자열을 남겼을 경우
    public String translateReview(String reviewText) {

        return geminiClient.translateToEnglish(reviewText);
    }

    // reviewId를 받아서 리뷰 내용 번역
    public String translateReviewById(UUID reviewId) {
        ReviewResponseDto review = reviewInternalClient.getReviewById(reviewId);

        if(review==null){
            throw new AiException(ErrorCode.REVIEW_NOT_FOUND);
        }

        String content = review.getContent();
        if(content==null || content.isBlank()){
            throw new AiException(ErrorCode.REVIEW_EMPTY_CONTENT);
        }
        return geminiClient.translateToEnglish(content);
    }


}
