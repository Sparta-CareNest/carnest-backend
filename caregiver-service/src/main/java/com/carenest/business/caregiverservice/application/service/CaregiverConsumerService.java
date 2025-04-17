package com.carenest.business.caregiverservice.application.service;

import com.carenest.business.common.event.caregiver.CaregiverRatingMessage;

public interface CaregiverConsumerService{
	void handleCaregiverRatingUpdate(CaregiverRatingMessage message);
}
