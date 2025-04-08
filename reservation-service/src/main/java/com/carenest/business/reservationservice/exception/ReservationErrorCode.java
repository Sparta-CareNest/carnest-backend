package com.carenest.business.reservationservice.exception;

import com.carenest.business.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements BaseErrorCode {

    // 예약
    RESERVATION_NOT_FOUND("R-001", "예약 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_RESERVATION_STATUS("R-002", "유효하지 않은 예약 상태입니다.", HttpStatus.BAD_REQUEST),
    INVALID_RESERVATION_TIME("R-003", "유효하지 않은 예약 시간입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESERVATION("R-004", "해당 시간에 이미 예약이 존재합니다.", HttpStatus.CONFLICT),
    RESERVATION_TIME_PAST("R-005", "과거 시간으로 예약할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 취소/거절
    CANNOT_CANCEL_RESERVATION("R-101", "예약을 취소할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST),
    CANNOT_REJECT_RESERVATION("R-102", "예약을 거절할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST),
    CANCEL_DEADLINE_PASSED("R-103", "예약 취소 기한이 지났습니다.", HttpStatus.BAD_REQUEST),

    // 권한
    UNAUTHORIZED_RESERVATION_ACCESS("R-201", "예약에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_USER_ROLE("R-202", "유효하지 않은 사용자 역할입니다.", HttpStatus.FORBIDDEN),

    // 결제
    PAYMENT_REQUIRED("R-301", "결제가 필요합니다.", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_FAILED("R-302", "결제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED("R-303", "이미 결제가 완료된 예약입니다.", HttpStatus.CONFLICT),

    // 시스템
    RESERVATION_SYSTEM_ERROR("R-901", "예약 시스템 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTEGRATION_ERROR("R-902", "외부 시스템 연동 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}