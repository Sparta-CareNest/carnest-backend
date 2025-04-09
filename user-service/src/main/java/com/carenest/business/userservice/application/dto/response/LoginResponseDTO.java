package com.carenest.business.userservice.application.dto.response;

import lombok.Getter;

@Getter
public class LoginResponseDTO {
    private final String message;
    private final String token;

    public LoginResponseDTO(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
