package com.carenest.business.aiservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "AI001", "잘못된 요청입니다."),
    GEMINI_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "AI002", "Gemini 클라이언트 오류"),
    GEMINI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI003", "Gemini 서버 오류"),

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "AI004", "리뷰를 찾을 수 없습니다."),
    REVIEW_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "AI005", "리뷰 정보를 가져오는 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
