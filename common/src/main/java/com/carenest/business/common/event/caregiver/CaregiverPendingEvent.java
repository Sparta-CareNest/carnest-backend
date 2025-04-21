package com.carenest.business.common.event.caregiver;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaregiverPendingEvent {
	private UUID reservationId;
	private UUID caregiverId;
	private String message;
}
