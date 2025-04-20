package com.carenest.business.notificationservice.application.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponseDto {
    private UUID userId;
    private String nickname;
    private String email;
}
