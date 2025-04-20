package com.carenest.business.caregiverservice.application.service;

import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;

public interface CaregiverApprovalService {
	void createCaregiverApproval(CaregiverPendingEvent event);
}
