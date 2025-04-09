package com.carenest.business.paymentservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentCreateRequest {
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentMethodDetail;
    private String paymentGateway;
}