package com.carenest.business.caregiverservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.CaregiverApproval;
import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;
import com.carenest.business.caregiverservice.infrastructure.client.ReservationClient;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationAcceptRequest;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationResponse;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverApprovalRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverRepository;
import com.carenest.business.caregiverservice.presentation.dto.response.PendingApprovalResponse;
import com.carenest.business.common.event.caregiver.CaregiverPendingEvent;
import com.carenest.business.common.exception.BaseException;
import com.carenest.business.common.exception.CommonErrorCode;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaregiverApprovalServiceImpl implements CaregiverApprovalService{
	private final CaregiverRepository caregiverRepository;
	private final CaregiverApprovalRepository caregiverApprovalRepository;
	private final ReservationClient reservationClient;

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

	@Override
	@Transactional(readOnly = true)
	public List<PendingApprovalResponse> getPendingApprovals(UUID userId) {

		// 1. 간병인 찾기
		Caregiver caregiver = caregiverRepository.findByUserId(userId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		// 2. 간병인 ID로 존재하는 승인/거부 조회
		List<CaregiverApproval> approvals = caregiverApprovalRepository.findByCaregiverId(caregiver.getId());

		return approvals.stream()
			.map(approval -> {
				try{
					// 예약 정보 조회
					ReservationResponse resp = reservationClient.getReservationDetails(approval.getReservationId()).getData();
					return new PendingApprovalResponse(
						approval.getReservationId(),
						approval.getCaregiverId(),
						resp.getPatientCondition(),
						resp.getCareAddress(),
						resp.getServiceRequests(),
						resp.getTotalAmount(),
						resp.getServiceFee()
					);
				}catch (FeignException e){
					log.warn("예약 정보 조회 실패: userId={}, error={}", userId, e.getMessage());
					throw  new CaregiverException(ErrorCode.EXTERNAL_API_ERROR);
				}
			}).toList();
	}

	@Override
	public void acceptCaregiverReservation(UUID reservationId, ReservationAcceptRequest request, UUID userId) {
		try {

			// 1. 간병인 정보 조회
			Caregiver caregiver = caregiverRepository.findByUserId(userId)
				.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

			// 2. 간병인 검증
			CaregiverApproval caregiverApproval = caregiverApprovalRepository.findByReservationIdAndCaregiverId(reservationId, caregiver.getId())
				.orElseThrow(() -> new BaseException(CommonErrorCode.FORBIDDEN));

			reservationClient.acceptReservation(caregiverApproval.getReservationId(),request);
			log.info("예약 수락 요청이 완료되었습니다.");

		} catch (FeignException e){
			log.warn("예약 수락 실패: userId={}, error={}", userId, e.getMessage());
			throw  new CaregiverException(ErrorCode.EXTERNAL_API_ERROR);
		}
	}
}
