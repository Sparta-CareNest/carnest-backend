package com.carenest.business.reservationservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.CaregiverDetailResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "caregiver-service", path = "/api/v1/caregivers")
public interface CaregiverServiceClient {

    @GetMapping("/{caregiverId}")
    ResponseDto<CaregiverDetailResponseDto> getCaregiverDetail(@PathVariable("caregiverId") UUID caregiverId);
}