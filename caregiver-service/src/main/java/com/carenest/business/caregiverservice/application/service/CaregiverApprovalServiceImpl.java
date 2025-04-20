package com.carenest.business.caregiverservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.business.caregiverservice.domain.model.CaregiverApproval;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverApprovalRepository;
import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaregiverApprovalServiceImpl implements CaregiverApprovalService{
	private final CaregiverApprovalRepository caregiverApprovalRepository;

	@Override
	@Transactional
	public void createCaregiverApproval(CaregiverPendingEvent event) {
		// 중복 방지
		if (caregiverApprovalRepository.existsById(event.getReservationId())) {
			return;
		}
		CaregiverApproval  caregiverApproval = CaregiverApproval.builder()
			.reservationId(event.getReservationId())
			.caregiverId(event.getCaregiverId())
			.message(event.getMessage())
			.build();

		caregiverApprovalRepository.save(caregiverApproval);
	}
}
