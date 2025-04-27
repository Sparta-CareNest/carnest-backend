package com.carenest.business.reservationservice.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkRequest {

    @NotNull(message = "결제 ID는 필수 입력 항목입니다")
    private UUID paymentId;
}