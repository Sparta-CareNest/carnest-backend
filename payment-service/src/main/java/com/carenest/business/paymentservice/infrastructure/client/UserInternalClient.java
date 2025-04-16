package com.carenest.business.paymentservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.UserDetailsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserInternalClient {

    @GetMapping("/internal/v1/users/{id}")
    Boolean isExistedUser(@PathVariable UUID id);

    @GetMapping("/internal/v1/users/{id}/details")
    ResponseDto<UserDetailsResponseDto> getUserInfo(@PathVariable UUID id);

    default UserDetailsResponseDto getUserDetails(UUID userId) {
        try {
            ResponseDto<UserDetailsResponseDto> response = getUserInfo(userId);
            if (response != null && response.getData() != null) {
                return response.getData();
            }
            // 값이 없는 경우 기본 정보 반환
            return new UserDetailsResponseDto(userId, "사용자", "이름 없음", "이메일 없음", "전화번호 없음");
        } catch (Exception e) {
            // 예외 발생 시 기본 정보 반환
            return new UserDetailsResponseDto(userId, "사용자", "이름 없음", "이메일 없음", "전화번호 없음");
        }
    }
}