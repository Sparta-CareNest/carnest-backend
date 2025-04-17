package com.carenest.business.paymentservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.UserInfoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserInternalClient {

    @GetMapping("/internal/v1/users/{id}")
    Boolean isExistedUser(@PathVariable UUID id);

    @GetMapping("/internal/v1/users/{id}/details")
    ResponseDto<UserInfoResponseDTO> getUserInfo(@PathVariable UUID id);

    default UserInfoResponseDTO getUserDetails(UUID userId) {
        try {
            ResponseDto<UserInfoResponseDTO> response = getUserInfo(userId);
            if (response != null && response.getData() != null) {
                return response.getData();
            }
            // 값이 없는 경우 기본 정보 반환
            return new UserInfoResponseDTO(userId, "알 수 없음", "사용자", "이메일 없음", "알 수 없음", "전화번호 없음");
        } catch (Exception e) {
            // 예외 발생 시 기본 정보 반환
            return new UserInfoResponseDTO(userId, "알 수 없음", "사용자", "이메일 없음", "알 수 없음", "전화번호 없음");
        }
    }
}