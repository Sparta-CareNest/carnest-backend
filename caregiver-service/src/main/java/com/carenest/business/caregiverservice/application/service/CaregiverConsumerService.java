package com.carenest.business.caregiverservice.application.service;

import com.carenest.business.common.event.caregiver.CaregiverRatingEvent;

public interface CaregiverConsumerService{
	void handleCaregiverRatingUpdate(CaregiverRatingEvent message);
}
