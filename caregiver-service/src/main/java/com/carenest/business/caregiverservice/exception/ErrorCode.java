package com.carenest.business.caregiverservice.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	NOT_FOUND("C-001", "해당 간병인을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	NO_PERMISSION("C-002", "간병인 권한이 없습니다.", HttpStatus.FORBIDDEN),
	ALREADY_REGISTERED_COMPANY("C-003", "이미 등록된 간병인 입니다.", HttpStatus.CONFLICT),

	;

	private final String errorCode;
	private final String message;
	private final HttpStatus status;
}
