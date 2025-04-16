package com.carenest.business.notificationservice.exception;

import com.carenest.business.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    USER_NOT_FOUND("N-001", "존재하지 않는 유저에게 알림을 보낼 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND("N-002", "해당 알림이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ;

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
