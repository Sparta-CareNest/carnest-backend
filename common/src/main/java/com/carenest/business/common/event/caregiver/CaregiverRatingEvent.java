package com.carenest.business.common.event.caregiver;

import java.util.UUID;

import com.carenest.business.common.event.BaseEvent;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CaregiverRatingEvent extends BaseEvent {
	private UUID caregiverId;
	private Double rating;

	@Builder
	public CaregiverRatingEvent(UUID caregiverId, Double rating) {
		super("REVIEW_RATING_UPDATE");
		this.caregiverId = caregiverId;
		this.rating = rating;
	}
}
