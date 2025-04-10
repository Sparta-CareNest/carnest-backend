package com.carenest.business.paymentservice.application.dto.response;

import com.carenest.business.paymentservice.domain.model.PaymentHistory;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaymentHistoryResponse {
    private UUID paymentHistoryId;
    private UUID paymentId;
    private UUID reservationId;
    private UUID guardianId;
    private UUID caregiverId;
    private PaymentStatus status;
    private PaymentStatus prevStatus;
    private BigDecimal amount;
    private String description;
    private String transactionId;
    private String gatewayResponse;
    private LocalDateTime createdAt;
    private String createdBy;
    private String ipAddress;
    private String userAgent;

    public PaymentHistoryResponse(PaymentHistory history) {
        this.paymentHistoryId = history.getPaymentHistoryId();
        this.paymentId = history.getPaymentId();
        this.reservationId = history.getReservationId();
        this.guardianId = history.getGuardianId();
        this.caregiverId = history.getCaregiverId();
        this.status = history.getStatus();
        this.amount = history.getAmount();
        this.createdAt = history.getCreatedAt();

        // TODO: 이전 상태와 비교하여 설정하도록 구현하기
        setDescriptionBasedOnStatus(history.getStatus());

        // TODO: 시스템 정보 설정하도록 구현하기
        this.createdBy = "system";
        this.ipAddress = "127.0.0.1";
        this.userAgent = "Server";
    }

    private void setDescriptionBasedOnStatus(PaymentStatus status) {
        switch (status) {
            case PENDING:
                this.description = "결제가 요청되었습니다.";
                break;
            case COMPLETED:
                this.description = "결제가 완료되었습니다.";
                break;
            case CANCELLED:
                this.description = "결제가 취소되었습니다.";
                break;
            case REFUNDED:
                this.description = "결제가 환불되었습니다.";
                break;
            default:
                this.description = "결제 상태가 변경되었습니다.";
        }
    }
}