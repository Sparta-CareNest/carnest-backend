package com.carenest.business.paymentservice.infrastructure.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaySoPrepareResponseDto {
    private String paymentKey;
    private String nextRedirectUrl;
    private String status;
    private String message;
}