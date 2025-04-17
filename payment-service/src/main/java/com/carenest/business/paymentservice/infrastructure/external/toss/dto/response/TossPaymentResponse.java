package com.carenest.business.paymentservice.infrastructure.external.toss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentResponse {
    private String mId;
    private String version;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String currency;
    private String method;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private String useEscrow;
    private String cultureExpense;
    private Long totalAmount;
    private Long balanceAmount;
    private Long suppliedAmount;
    private Long vat;
    private Long taxFreeAmount;
    private String taxExemptionAmount;
    private Long cancelAmount;
    private String cancelReason;
    private String canceledAt;
    private Long remainingCancelAmount;
    private String transactionKey;
    private String receipt;
    private CardResponse card;
    private VirtualAccountResponse virtualAccount;
    private TransferResponse transfer;
    private MobilePhoneResponse mobilePhone;
    private GiftCertificateResponse giftCertificate;
    private CashReceiptResponse cashReceipt;
    private DiscountResponse discount;
    private EasyPayResponse easyPay;
    private String country;
    private String failure;
    private UrlResponse url;
}