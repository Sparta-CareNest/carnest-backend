package com.carenest.business.caregiverservice.application.service;

import java.util.List;
import java.util.UUID;

import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationAcceptRequest;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationRejectRequest;
import com.carenest.business.caregiverservice.presentation.dto.response.PendingApprovalResponse;
import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;

public interface CaregiverApprovalService {

	void createCaregiverApproval(CaregiverPendingEvent event);

	List<PendingApprovalResponse> getPendingApprovals(UUID userId);

	void acceptCaregiverReservation(UUID reservationId, ReservationAcceptRequest request, UUID userId);

	void rejectCaregiverReservation(UUID reservationId, ReservationRejectRequest request, UUID userId);
}
