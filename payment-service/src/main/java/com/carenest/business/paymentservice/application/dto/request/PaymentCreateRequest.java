package com.carenest.business.paymentservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentCreateRequest {
    private UUID reservationId;
    // guardianId 필드 제거 - 토큰에서 추출한 사용자 ID 사용
    private UUID caregiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentMethodDetail;
    private String paymentGateway;
}