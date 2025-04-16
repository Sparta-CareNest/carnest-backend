package com.carenest.business.reservationservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserInternalClient {
    @GetMapping("/internal/v1/users/{id}")
    Boolean isExistedUser(@PathVariable UUID id);
}