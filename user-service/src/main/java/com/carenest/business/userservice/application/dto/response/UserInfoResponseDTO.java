package com.carenest.business.userservice.application.dto.response;

import com.carenest.business.userservice.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserInfoResponseDTO {
    private final UUID userId;
    private final String username;
    private final String nickname;
    private final String email;
    private final String name;
    private final String phoneNumber;

    public static UserInfoResponseDTO from(User user) {
        return new UserInfoResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber()
        );
    }
}
