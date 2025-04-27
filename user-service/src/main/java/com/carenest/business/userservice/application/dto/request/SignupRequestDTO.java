package com.carenest.business.userservice.application.dto.request;

import com.carenest.business.common.model.UserRole;
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
    private UserRole role;
    private String secretKey;
}
