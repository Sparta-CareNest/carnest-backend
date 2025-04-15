package com.carenest.business.reviewservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    REVIEW_NOT_FOUND(404, "리뷰를 찾을 수 없습니다."),
    INVALID_REVIEW_CONTENT(400, "리뷰 내용이 유효하지 않습니다."),
    UNAUTHORIZED_REVIEW_DELETE(403, "리뷰 삭제 권한이 없습니다."),
    INVALID_USER(404, "존재하지 않는 사용자입니다."),
    INVALID_CAREGIVER(404, "존재하지 않은 간병인입니다.");


    private final int status;
    private final String message;

}
