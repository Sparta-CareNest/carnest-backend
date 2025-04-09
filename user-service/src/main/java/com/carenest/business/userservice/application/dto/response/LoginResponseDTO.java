package com.carenest.business.userservice.application.dto.response;

import com.carenest.business.userservice.domain.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private String userId;
        private String username;
        private String nickname;
        private String email;
        private String role;
        private String name;
        private String phoneNumber;
    }

    public static LoginResponseDTO of(String accessToken, String refreshToken, User user) {
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserInfo.builder()
                        .userId(user.getUserId().toString())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .name(user.getName())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .build();
    }
}