package com.carenest.business.paymentservice.exception;

import com.carenest.business.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {
    PAYMENT_NOT_FOUND("P-001", "결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_COMPLETED("P-002", "이미 완료된 결제입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS("P-003", "유효하지 않은 결제 상태입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_PROCESSING_ERROR("P-004", "결제 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REFUND_AMOUNT_EXCEEDS_PAYMENT("P-005", "환불 금액이 결제 금액을 초과합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT("P-006", "이미 결제가 진행 중인 예약입니다.", HttpStatus.CONFLICT),
    PAYMENT_GATEWAY_ERROR("P-007", "결제 게이트웨이 연동 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    UNAUTHORIZED_PAYMENT_ACCESS("P-008", "결제 정보에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}