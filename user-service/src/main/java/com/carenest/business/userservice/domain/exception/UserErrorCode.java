package com.carenest.business.userservice.domain.exception;

import com.carenest.business.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    DUPLICATED_EMAIL("U-001", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("U-002", "해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("U-003", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("U-004", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    DUPLICATED_USERNAME("U-005", "이미 사용 중인 아이디입니다.", HttpStatus.BAD_REQUEST),
    USERNAME_NOT_FOUND("U-006", "존재하지 않는 사용자명입니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }
}