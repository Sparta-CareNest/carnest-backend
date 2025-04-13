package com.carenest.business.reviewservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "caregiver-service")
public interface CaregiverInternalClient {
    @GetMapping("/internal/v1/caregivers/{id}")
    Boolean isExistedCaregiver(@PathVariable("id") UUID caregiverId);
}
