package com.carenest.business.adminservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "p_settlement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID careWorkerId; // 간병인 ID

    @Column(nullable = false)
    private BigDecimal amount; // 총 정산 금액

    @Column(nullable = false)
    private LocalDate periodStart; // 정산 기간 시작일

    @Column(nullable = false)
    private LocalDate periodEnd;   // 정산 기간 종료일

    @Column(nullable = false)
    private LocalDate settledAt;   // 정산 수행일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;
}
