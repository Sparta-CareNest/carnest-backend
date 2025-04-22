package com.carenest.business.adminservice.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaymentListResponse {
    private UUID careWorkerId;  // 간병인 ID
    private BigDecimal amount;  // 결제 금액
    private String status;      // 결제 상태 (예: COMPLETED, PENDING)
    private LocalDateTime createdAt; // 결제 생성 시간
    private LocalDateTime completedAt; // 결제 완료 시간 (완료된 경우)

    // 필요시 생성자 추가
    public PaymentListResponse(UUID careWorkerId, BigDecimal amount, String status,
                               LocalDateTime createdAt, LocalDateTime completedAt) {
        this.careWorkerId = careWorkerId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // 결제 상태가 "COMPLETED"일 경우만 완료 시간을 설정
    public void setCompletedAt(LocalDateTime completedAt) {
        if ("COMPLETED".equals(this.status)) {
            this.completedAt = completedAt;
        }
    }
}
