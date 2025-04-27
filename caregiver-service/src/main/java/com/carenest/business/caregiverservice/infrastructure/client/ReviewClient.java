package com.carenest.business.caregiverservice.infrastructure.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.carenest.business.caregiverservice.infrastructure.client.dto.CaregiverRatingDto;
import com.carenest.business.caregiverservice.infrastructure.client.dto.CaregiverTopRatingDto;
import com.carenest.business.common.response.ResponseDto;

@FeignClient(name = "review-service")
public interface ReviewClient {

	@GetMapping("/api/v1/reviews/ratings/top")
	ResponseEntity<List<CaregiverTopRatingDto>> getTop10Caregivers();

	@GetMapping("/api/v1/reviews/ratings/calculate")
	ResponseDto<CaregiverRatingDto> calculateRating(@RequestParam UUID caregiverId);
}
