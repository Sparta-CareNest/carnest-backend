package com.carenest.business.caregiverservice.exception;

import lombok.Getter;

@Getter
public class CaregiverException extends RuntimeException{

	private final ErrorCode errorCode;

	public CaregiverException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
