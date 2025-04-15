package com.carenest.business.caregiverservice.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user-service")
public interface UserClient {

	@GetMapping("/internal/v1/users/{id}")
	Boolean isExistedCaregiver(@PathVariable UUID id);
}
