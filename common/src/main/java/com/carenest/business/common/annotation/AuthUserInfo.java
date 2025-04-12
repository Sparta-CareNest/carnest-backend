package com.carenest.business.common.annotation;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AuthUserInfo {
    private UUID userId;
    private String email;
    private String role;
}
