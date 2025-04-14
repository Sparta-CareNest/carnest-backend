package com.carenest.business.userservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequestDTO {
    private String nickname;
    private String email;
    private String name;
    private String phoneNumber;
}
