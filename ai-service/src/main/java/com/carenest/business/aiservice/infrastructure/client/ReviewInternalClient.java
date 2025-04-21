package com.carenest.business.aiservice.infrastructure.client;

import com.carenest.business.aiservice.application.dto.ReviewResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "review-service")
public interface ReviewInternalClient {

    @GetMapping("/api/v1/reviews/{reviewId}")
    ReviewResponseDto getReviewById(@PathVariable("reviewId") UUID reviewId);
}
