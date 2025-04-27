package com.carenest.business.userservice.application.dto.response;

import com.carenest.business.userservice.domain.model.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UpdateUserResponseDTO {
    private UUID userId;
    private String nickname;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;

    public static UpdateUserResponseDTO from(User user) {
        return UpdateUserResponseDTO.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .name(user.getName())
            .phoneNumber(user.getPhoneNumber())
            .role(user.getRole().name())
            .build();
    }
}
