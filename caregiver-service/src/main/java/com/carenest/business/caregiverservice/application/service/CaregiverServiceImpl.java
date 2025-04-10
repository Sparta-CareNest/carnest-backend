package com.carenest.business.caregiverservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.business.caregiverservice.application.dto.mapper.CaregiverApplicationMapper;
import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverSearchResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CategoryService;
import com.carenest.business.caregiverservice.domain.service.CaregiverDomainService;
import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CategoryLocationRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CategoryServiceRepository;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaregiverServiceImpl implements CaregiverService {

	private final CaregiverRepository caregiverRepository;
	private final CategoryServiceRepository categoryServiceRepository;
	private final CategoryLocationRepository categoryLocationRepository;
	private final CaregiverDomainService caregiverDomainService;

	@Qualifier("caregiverApplicationMapper")
	private final CaregiverApplicationMapper applicationMapper;

	@Override
	@Transactional
	public CaregiverCreateResponseServiceDTO createCaregiver(CaregiverCreateRequestServiceDTO requestServiceDTO) {

		// TODO: userId() 검증 로직을 추가해야함.

		List<CategoryService> categoryServices = categoryServiceRepository.findAllById(
			requestServiceDTO.categoryServiceIds());
		if (categoryServices.size() != requestServiceDTO.categoryServiceIds().size()) {
			throw new CaregiverException(ErrorCode.NOT_FOUND_SERVICES);
		}

		List<CategoryLocation> categoryLocations = categoryLocationRepository.findAllById(
			requestServiceDTO.categoryLocationIds());
		if (categoryLocations.size() != requestServiceDTO.categoryLocationIds().size()) {
			throw new CaregiverException(ErrorCode.NOT_FOUND_LOCATION);
		}

		Caregiver caregiver = caregiverDomainService.createCaregiverWithCategories(requestServiceDTO, categoryServices,
			categoryLocations);

		Caregiver saveCaregiver = caregiverRepository.save(caregiver);

		return applicationMapper.toCreateResponseServiceDTO(saveCaregiver.getId());
	}

	// TODO: QueryDsl 로 수정 할 예정
	@Override
	public CaregiverReadResponseServiceDTO getCaregiver(UUID caregiverId) {

		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		List<String> categoryServiceNames = caregiver.getCaregiverCategoryServices()
			.stream()
			.map(n -> n.getCategoryService().getName())
			.toList();

		List<String> categoryLocationNames = caregiver.getCaregiverCategoryLocations()
			.stream()
			.map(n -> n.getCategoryLocation().getName())
			.toList();

		return new CaregiverReadResponseServiceDTO(caregiver.getId(), caregiver.getUserId(), caregiver.getDescription(),
			caregiver.getRating(), caregiver.getExperienceYears(), caregiver.getPricePerHour(),
			caregiver.getPricePerDay(), caregiver.getApprovalStatus(), caregiver.getGender(), categoryServiceNames,
			categoryLocationNames);
	}

	@Override
	@Transactional
	public CaregiverUpdateResponseServiceDTO updateCaregiver(UUID caregiverId, CaregiverUpdateRequestDTO dto) {
		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		// 필드 수정
		if (dto.description() != null)
			caregiver.setDescription(dto.description());
		if (dto.experienceYears() != null)
			caregiver.setExperienceYears(dto.experienceYears());
		if (dto.pricePerHour() != null)
			caregiver.setPricePerHour(dto.pricePerHour());
		if (dto.pricePerDay() != null)
			caregiver.setPricePerDay(dto.pricePerDay());

		// category 조회
		List<CategoryService> categoryServices =
			dto.categoryServiceIds() != null ? categoryServiceRepository.findAllById(dto.categoryServiceIds()) : null;

		List<CategoryLocation> categoryLocations =
			dto.categoryLocationIds() != null ? categoryLocationRepository.findAllById(dto.categoryLocationIds()) :
				null;

		caregiverDomainService.updateCaregiverCategories(caregiver, categoryServices, categoryLocations);

		// 응답 변환
		List<String> categoryServiceNames = caregiver.getCaregiverCategoryServices()
			.stream()
			.map(n -> n.getCategoryService().getName())
			.toList();

		List<String> categoryLocationNames = caregiver.getCaregiverCategoryLocations()
			.stream()
			.map(n -> n.getCategoryLocation().getName())
			.toList();

		return new CaregiverUpdateResponseServiceDTO(caregiver.getId(), caregiver.getUserId(),
			caregiver.getDescription(), caregiver.getRating(), caregiver.getExperienceYears(),
			caregiver.getPricePerHour(), caregiver.getPricePerDay(), caregiver.getApprovalStatus(),
			caregiver.getGender(), categoryServiceNames, categoryLocationNames);
	}

	@Override
	@Transactional
	public void deleteCaregiver(UUID caregiverId) {

		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));
		caregiverDomainService.deleteCaregiverWithAssociations(caregiverId, caregiver);
	}

	@Override
	public Page<CaregiverSearchResponseServiceDTO> searchCaregiver(String location, String service, Pageable pageable) {
		Page<Caregiver> caregivers = caregiverRepository.searchByConditions(location, service, pageable);


		return caregivers.map(caregiver -> new CaregiverSearchResponseServiceDTO(
			caregiver.getId(),
			caregiver.getDescription(),
			caregiver.getRating(),
			caregiver.getExperienceYears(),
			caregiver.getPricePerHour(),
			caregiver.getPricePerDay(),
			caregiver.getGender()
		));
	}

	@Override
	@Transactional
	public void updateCaregiverStatus(UUID id, boolean approvalStatusCheck) {
		Caregiver caregiver = caregiverRepository.findById(id).orElseThrow(()->
			new CaregiverException(ErrorCode.NOT_FOUND));

		caregiverDomainService.caregiverApprovalStatusCheck(caregiver,approvalStatusCheck);
	}

}
