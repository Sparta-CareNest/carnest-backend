package com.carenest.business.aiservice.application.service;

import com.carenest.business.aiservice.infrastructure.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewTranslationService {

    private final GeminiClient geminiClient;

    public String translateReview(String reviewText) {
        return geminiClient.translateToEnglish(reviewText);
    }
}
