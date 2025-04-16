package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MobilePhoneResponse {
    private String customerMobilePhone;
    private String settlementStatus;
    private String receiptUrl;
}