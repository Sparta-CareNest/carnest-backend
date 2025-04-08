package com.carenest.business.paymentservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCompleteRequest {
    private String approvalNumber;
    private String pgTransactionId;
    private String receiptUrl;
    private String paymentKey;
}