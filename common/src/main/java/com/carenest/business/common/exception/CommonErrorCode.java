package com.carenest.business.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

	FORBIDDEN("U-001", "권한이 없습니다.", HttpStatus.FORBIDDEN),
	UNAUTHORIZED("U-002", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
	USER_NOT_FOUND("U-003", "사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
	INVALID_USER_STATUS("U-004", "확인되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
	INVALID_ROLE("U-005", "해당 작업을 수행할 수 없는 역할입니다.", HttpStatus.FORBIDDEN),
	;

	private final String errorCode;
	private final String message;
	private final HttpStatus status;
}
