package com.carenest.business.caregiverservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.service.CaregiverDomainService;
import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverRepository;
import com.carenest.business.common.event.caregiver.CaregiverRatingMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaregiverConsumerServiceImpl implements CaregiverConsumerService {

	private final CaregiverRepository caregiverRepository;
	private final CaregiverDomainService caregiverDomainService;

	@Override
	@Transactional
	public void handleCaregiverRatingUpdate(CaregiverRatingMessage message) {
		// 도메인 서비스로 위임해서 갱신 로직 실행
		Caregiver caregiver = caregiverRepository.findById(message.getCaregiverId())
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		caregiverDomainService.updateCaregiverRating(caregiver, message.getRating());

	}
}
