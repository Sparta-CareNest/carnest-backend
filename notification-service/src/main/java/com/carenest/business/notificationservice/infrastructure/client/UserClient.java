package com.carenest.business.notificationservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {
    // 사용자 ID 유효성 검증
    @GetMapping("/internal/v1/users/{id}")
    Boolean validateUser(@PathVariable("id") UUID id);
}
