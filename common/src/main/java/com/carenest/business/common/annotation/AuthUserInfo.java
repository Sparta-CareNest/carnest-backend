package com.carenest.business.common.annotation;

import lombok.Getter;

import java.util.UUID;

import com.carenest.business.common.model.UserRole;

@Getter
public class AuthUserInfo {
    private UUID userId;
    private String email;
    private UserRole role;
}
