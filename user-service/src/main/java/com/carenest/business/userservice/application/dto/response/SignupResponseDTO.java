package com.carenest.business.userservice.application.dto.response;

import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.domain.model.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDTO {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String name;
    private String phoneNumber;

    public static SignupResponseDTO from(User user) {
        return SignupResponseDTO.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}