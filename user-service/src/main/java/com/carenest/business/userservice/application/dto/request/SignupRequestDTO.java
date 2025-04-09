package com.carenest.business.userservice.application.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String name;
    private String phoneNumber;
}
