package com.carenest.business.caregiverservice.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PendingApprovalResponse (

	UUID reservationId,
	UUID caregiverId,
	String patientCondition,
	String careAddress,
	String serviceRequests,
	BigDecimal totalAmount,
	BigDecimal serviceFee


) {




}
