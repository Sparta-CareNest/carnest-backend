package com.carenest.business.caregiverservice.application.service;

import java.util.List;
import java.util.UUID;

import com.carenest.business.caregiverservice.presentation.dto.response.PendingApprovalResponse;
import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;

public interface CaregiverApprovalService {

	void createCaregiverApproval(CaregiverPendingEvent event);

	List<PendingApprovalResponse> getPendingApprovals(UUID userId);
}
