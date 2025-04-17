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
    private String guardianName;
    private UUID caregiverId;
    private String caregiverName;
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

        setDescriptionBasedOnStatus(history.getStatus());

        // 시스템 정보 설정
        this.createdBy = "system";
        this.ipAddress = "127.0.0.1";
        this.userAgent = "Server";
    }

    public PaymentHistoryResponse(PaymentHistory history,
                                  String guardianName,
                                  String caregiverName,
                                  PaymentStatus prevStatus) {
        this(history);
        this.guardianName = guardianName;
        this.caregiverName = caregiverName;
        this.prevStatus = prevStatus;

        if (prevStatus != null && prevStatus != history.getStatus()) {
            updateDescriptionWithStatusChange(prevStatus, history.getStatus());
        }
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

    private void updateDescriptionWithStatusChange(PaymentStatus prevStatus, PaymentStatus newStatus) {
        this.description = String.format("결제 상태가 [%s]에서 [%s]로 변경되었습니다.",
                getStatusDisplayName(prevStatus),
                getStatusDisplayName(newStatus));
    }

    private String getStatusDisplayName(PaymentStatus status) {
        switch (status) {
            case PENDING:
                return "결제 대기";
            case COMPLETED:
                return "결제 완료";
            case CANCELLED:
                return "결제 취소";
            case REFUNDED:
                return "환불 완료";
            default:
                return "알 수 없음";
        }
    }
}