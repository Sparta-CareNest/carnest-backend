package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private String company;
    private String number;
    private String installmentPlanMonths;
    private String isInterestFree;
    private String approveNo;
    private String useCardPoint;
    private String cardType;
    private String ownerType;
    private String acquireStatus;
    private String receiptUrl;
}