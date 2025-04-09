package com.carenest.business.caregiverservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.business.caregiverservice.application.dto.mapper.CaregiverApplicationMapper;
import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryService;
import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CategoryService;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CategoryLocationRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CategoryServiceRepository;
import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaregiverServiceImpl {

	private final CaregiverRepository caregiverRepository;
	private final CategoryLocationRepository categoryLocationRepository;
	private final CategoryServiceRepository categoryServiceRepository;

	@Qualifier("caregiverApplicationMapper")
	private final CaregiverApplicationMapper applicationMapper;

	@Transactional
	public CaregiverCreateResponseServiceDTO createCaregiver(CaregiverCreateRequestServiceDTO requestServiceDTO) {

		// TODO: userId() 검증 로직을 추가해야함.

		Caregiver caregiver = Caregiver.builder()
			.userId(requestServiceDTO.userId())
			.description(requestServiceDTO.description())
			.experienceYears(requestServiceDTO.experienceYears())
			.pricePerDay(requestServiceDTO.pricePerDay())
			.pricePerHour(requestServiceDTO.pricePerHour())
			.gender(requestServiceDTO.gender())
			.submittedDocuments(requestServiceDTO.submittedDocuments())
			.build();

		// categoryService 검증
		List<CaregiverCategoryService> caregiverCategoryServices = requestServiceDTO.categoryServiceIds().stream()
			.map(serviceId -> {
				CategoryService categoryService = categoryServiceRepository.findById(serviceId)
					.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND_SERVICES));

				return CaregiverCategoryService.builder()
					.caregiver(caregiver)
					.categoryService(categoryService)
					.build();
			}).toList();

		// categoryLocation 검증
		List<CaregiverCategoryLocation> caregiverCategoryLocations = requestServiceDTO.categoryLocationIds().stream()
			.map(locationId -> {
				CategoryLocation categoryLocation = categoryLocationRepository.findById(locationId)
					.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND_LOCATION));

				return CaregiverCategoryLocation.builder()
					.caregiver(caregiver)
					.categoryLocation(categoryLocation)
					.build();
			}).toList();

		// 연관관계 주입
		caregiver.getCaregiverCategoryServices().addAll(caregiverCategoryServices);
		caregiver.getCaregiverCategoryLocations().addAll(caregiverCategoryLocations);

		Caregiver saveCaregiver = caregiverRepository.save(caregiver);

		return applicationMapper.toCreateResponseServiceDTO(saveCaregiver.getId());
	}

	// TODO: QueryDsl 로 수정 할 예정
	public CaregiverReadResponseServiceDTO getCaregiver(UUID caregiverId) {

		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		List<String> categoryServiceNames = caregiver.getCaregiverCategoryServices().stream()
			.map(n -> n.getCategoryService().getName())
			.toList();

		List<String> categoryLocationNames = caregiver.getCaregiverCategoryLocations().stream()
			.map(n -> n.getCategoryLocation().getName())
			.toList();

		return new CaregiverReadResponseServiceDTO(
			caregiver.getId(),
			caregiver.getUserId(),
			caregiver.getDescription(),
			caregiver.getRating(),
			caregiver.getExperienceYears(),
			caregiver.getPricePerHour(),
			caregiver.getPricePerDay(),
			caregiver.getApprovalStatus(),
			caregiver.getGender(),
			categoryServiceNames,
			categoryLocationNames
		);
	}

	@Transactional
	public CaregiverUpdateResponseServiceDTO updateCaregiver(UUID caregiverId, CaregiverUpdateRequestDTO dto) {
		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		if (dto.description() != null)
			caregiver.setDescription(dto.description());
		if (dto.experienceYears() != null)
			caregiver.setExperienceYears(dto.experienceYears());
		if (dto.pricePerHour() != null)
			caregiver.setPricePerHour(dto.pricePerHour());
		if (dto.pricePerDay() != null)
			caregiver.setPricePerDay(dto.pricePerDay());

		if (dto.categoryServiceIds() != null) {

			// 기존 연관관계 제거
			caregiver.clearCategoryServices();
			List<CaregiverCategoryService> newServices = dto.categoryServiceIds().stream()
					.map(id -> {
						CategoryService categoryService = categoryServiceRepository.findById(id).orElseThrow(()->
							new CaregiverException(ErrorCode.NOT_FOUND_SERVICES));

						return CaregiverCategoryService.builder()
							.caregiver(caregiver)
							.categoryService(categoryService)
							.build();
					}).toList();
			caregiver.getCaregiverCategoryServices().addAll(newServices);
		}

		if(dto.categoryLocationIds() != null){
			caregiver.clearCategoryLocation();
			List<CaregiverCategoryLocation> newLocations = dto.categoryLocationIds().stream()
				.map(id->{
					CategoryLocation categoryLocation = categoryLocationRepository.findById(id).orElseThrow(
						()-> new CaregiverException(ErrorCode.NOT_FOUND_LOCATION));

					return CaregiverCategoryLocation.builder()
						.categoryLocation(categoryLocation)
						.caregiver(caregiver)
						.build();
				}).toList();

			caregiver.getCaregiverCategoryLocations().addAll(newLocations);
		}

		// responseDto 변경
		List<String> categoryServiceNames = caregiver.getCaregiverCategoryServices().stream()
			.map(n -> n.getCategoryService().getName())
			.toList();

		List<String> categoryLocationNames = caregiver.getCaregiverCategoryLocations().stream()
			.map(n -> n.getCategoryLocation().getName())
			.toList();

		return new CaregiverUpdateResponseServiceDTO(
			caregiver.getId(),
			caregiver.getUserId(),
			caregiver.getDescription(),
			caregiver.getRating(),
			caregiver.getExperienceYears(),
			caregiver.getPricePerHour(),
			caregiver.getPricePerDay(),
			caregiver.getApprovalStatus(),
			caregiver.getGender(),
			categoryServiceNames,
			categoryLocationNames
		);
	}

	@Transactional
	public void deleteCaregiver(UUID caregiverId) {
		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(()-> new CaregiverException(ErrorCode.NOT_FOUND));

		caregiver.clearCategoryServices();
		caregiver.clearCategoryLocation();

		caregiver.softDelete();
	}
}
