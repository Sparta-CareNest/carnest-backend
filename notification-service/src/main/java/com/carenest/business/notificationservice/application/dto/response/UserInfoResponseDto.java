package com.carenest.business.notificationservice.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoResponseDto {
    private UUID userId;
    private String nickname;
    private String email;
}
