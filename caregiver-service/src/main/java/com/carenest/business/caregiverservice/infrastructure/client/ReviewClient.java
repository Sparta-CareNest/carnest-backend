package com.carenest.business.caregiverservice.infrastructure.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.carenest.business.caregiverservice.infrastructure.client.dto.CaregiverTopRatingDto;

@FeignClient(name = "review-service")
public interface ReviewClient {

	@GetMapping("/api/v1/reviews/ratings/top")
	ResponseEntity<List<CaregiverTopRatingDto>> getTop10Caregivers();
}
