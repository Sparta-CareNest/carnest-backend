package com.carenest.business.notificationservice.infrastructure.client;

import com.carenest.business.notificationservice.application.dto.response.UserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {
    // 사용자 ID 유효성 검증
    @GetMapping("/api/users/validate")
    Boolean validateUser(@RequestParam("userId") UUID userId);

    // 사용자 기본 정보 조회 (닉네임 포함)
    @GetMapping("/api/users/info")
    UserInfoResponseDto getUserInfo(@RequestParam("userId") UUID userId);
}
