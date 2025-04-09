package com.carenest.business.userservice.application.dto.response;

import lombok.Getter;

@Getter
public class UserInfoResponseDTO {
    private final String email;
    private final String nickname;
    private final String phoneNumber;

    public UserInfoResponseDTO(String email, String nickname, String phoneNumber) {
        this.email = email;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }
}
