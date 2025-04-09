package com.carenest.business.reservationservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelRequest {

    @NotBlank(message = "취소 사유는 필수 입력 항목입니다")
    @Size(max = 255, message = "취소 사유는 최대 255자까지 입력 가능합니다")
    private String cancelReason;

    @Size(max = 255, message = "취소 메모는 최대 255자까지 입력 가능합니다")
    private String cancellationNote;
}