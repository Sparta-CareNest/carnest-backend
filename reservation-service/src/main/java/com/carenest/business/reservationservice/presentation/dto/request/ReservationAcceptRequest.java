package com.carenest.business.reservationservice.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationAcceptRequest {

    @Size(max = 255, message = "간병인 메모는 최대 255자까지 입력 가능합니다")
    private String caregiverNote;
}