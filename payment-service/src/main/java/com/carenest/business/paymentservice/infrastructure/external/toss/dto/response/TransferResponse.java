package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String bankCode;
    private String settlementStatus;
}