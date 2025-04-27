package com.carenest.business.reservationservice.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequestDto {
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentMethodDetail;
    private String paymentGateway;
}