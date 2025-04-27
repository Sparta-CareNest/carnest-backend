package com.carenest.business.aiservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "caregiver-service")
public interface CaregiverInternalClient {

    @GetMapping("/caregivers/search")
    List<UUID> searchCaregivers(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) Double rating
    );

}
