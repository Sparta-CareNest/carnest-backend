package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CashReceiptResponse {
    private String type;
    private String receiptKey;
    private String issueNumber;
    private String receiptUrl;
}