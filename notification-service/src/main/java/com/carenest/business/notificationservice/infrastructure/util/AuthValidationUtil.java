package com.carenest.business.notificationservice.infrastructure.util;

import com.carenest.business.common.exception.BaseException;
import com.carenest.business.common.exception.CommonErrorCode;

import java.util.UUID;

public class AuthValidationUtil {
    public static void validateUserAccess(UUID targetUserId, UUID authUserId) {
        if (!targetUserId.equals(authUserId)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
    }
}
