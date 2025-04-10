package com.carenest.business.paymentservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequest {
    private String cancelReason;
    private String refundBank;
    private String refundAccount;
    private String refundAccountOwner;
    private BigDecimal refundAmount;
}