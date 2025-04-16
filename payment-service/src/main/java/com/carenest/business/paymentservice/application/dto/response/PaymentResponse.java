package com.carenest.business.paymentservice.application.dto.response;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponse {
    // 기본 정보
    private UUID paymentId;
    private UUID reservationId;
    private UUID guardianId;
    private String guardianName;
    private UUID caregiverId;
    private String caregiverName;
    private String serviceName;
    private String patientName;

    // 서비스 정보
    private LocalDateTime serviceStartDate;
    private LocalDateTime serviceEndDate;

    // 결제 정보
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private Map<String, Object> paymentMethodDetail;
    private String paymentGateway;
    private String paymentKey;
    private String approvalNumber;
    private String pgTransactionId;
    private String receiptUrl;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    // 취소/환불 정보
    private String cancelReason;
    private BigDecimal refundAmount;
    private String refundBank;
    private String refundAccount;
    private String refundAccountOwner;

    private Map<String, Object> refundInfo;

    private Map<String, Object> cancellationPolicy;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.reservationId = payment.getReservationId();
        this.guardianId = payment.getGuardianId();
        this.caregiverId = payment.getCaregiverId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.pgTransactionId = payment.getPgTransactionId();
        this.approvalNumber = payment.getApprovalNumber();
        this.receiptUrl = payment.getReceiptUrl();
        this.createdAt = payment.getCreatedAt();
        this.cancelledAt = payment.getCancelledAt();
        this.cancelReason = payment.getCancelReason();
        this.refundAmount = payment.getRefundAmount();
        this.refundBank = payment.getRefundBank();
        this.refundAccount = payment.getRefundAccount();
        this.refundAccountOwner = payment.getRefundAccountOwner();
        this.updatedAt = payment.getUpdatedAt();
        this.refundedAt = payment.getRefundedAt();
        this.paymentGateway = payment.getPaymentGateway();
        this.paymentKey = payment.getPaymentKey();
        
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            this.completedAt = payment.getUpdatedAt();
        }

        // TODO: 결제 방식 세부 정보 설정 구현하기(JSON 파싱)
        this.paymentMethodDetail = new HashMap<>();
        if (payment.getPaymentMethod().equals("CARD")) {
            this.paymentMethodDetail.put("card_number", "--****" + getLastFourDigits(payment.getPaymentMethodDetail()));
            this.paymentMethodDetail.put("card_type", "신용카드");
            this.paymentMethodDetail.put("issuer", "우리카드"); // 예시 데이터
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.REFUNDED) {
            this.refundInfo = new HashMap<>();
            this.refundInfo.put("refund_id", UUID.randomUUID().toString());
            this.refundInfo.put("refund_amount", payment.getRefundAmount() != null ? payment.getRefundAmount() : payment.getAmount());
            this.refundInfo.put("refund_status", "PROCESSING");
            this.refundInfo.put("estimated_refund_date", LocalDateTime.now().plusDays(2));

            this.cancellationPolicy = new HashMap<>();
            this.cancellationPolicy.put("cancellation_period", "서비스 시작 3일 전");
            this.cancellationPolicy.put("refund_percentage", 100);
            this.cancellationPolicy.put("refund_amount", payment.getRefundAmount() != null ? payment.getRefundAmount() : payment.getAmount());
        }

        // TODO: 다른 서비스에서 정보 가져오기
        this.guardianName = "보호자 이름";
        this.caregiverName = "간병인 이름";
        this.serviceName = "서비스 이름";
        this.patientName = "환자 이름";
        this.serviceStartDate = LocalDateTime.now().plusDays(5);
        this.serviceEndDate = LocalDateTime.now().plusDays(8);
    }

    // 카드번호 마스킹 메서드
    private String getLastFourDigits(String cardDetails) {
        // TODO: JSON에서 카드번호 파싱 구현하기
        return "3456";
    }

    public Map<String, Object> getPaymentGatewayInfo() {
        Map<String, Object> gatewayInfo = new HashMap<>();
        gatewayInfo.put("paymentId", this.paymentId);
        gatewayInfo.put("amount", this.amount);
        gatewayInfo.put("orderId", "CARENEST-" + this.reservationId.toString().substring(0, 8));
        gatewayInfo.put("orderName", "CareNest 간병 서비스 예약");
        gatewayInfo.put("successUrl", "http://localhost:9040/api/v1/payments/toss/success");
        gatewayInfo.put("failUrl", "http://localhost:9040/api/v1/payments/toss/fail");
        return gatewayInfo;
    }
}