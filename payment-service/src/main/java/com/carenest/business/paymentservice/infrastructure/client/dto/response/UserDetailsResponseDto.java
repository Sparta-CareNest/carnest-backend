package com.carenest.business.paymentservice.infrastructure.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    private UUID userId;
    private String username;
    private String nickname;
    private String email;
    private String name;
    private String phoneNumber;
}