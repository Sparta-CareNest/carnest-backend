package com.carenest.business.caregiverservice.infrastructure.client.dto.reservation;

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
public class ReservationRejectRequest {

	@NotBlank(message = "거절 사유는 필수 입력 항목입니다")
	@Size(max = 255, message = "거절 사유는 최대 255자까지 입력 가능합니다")
	private String rejectionReason;

	@Size(max = 255, message = "대안 제안은 최대 255자까지 입력 가능합니다")
	private String suggestedAlternative;
}