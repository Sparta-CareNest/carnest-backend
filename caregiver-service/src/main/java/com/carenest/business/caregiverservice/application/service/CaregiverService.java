package com.carenest.business.caregiverservice.application.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.carenest.business.caregiverservice.application.dto.mapper.CaregiverApplicationMapper;
import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryService;
import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CategoryService;
import com.carenest.business.caregiverservice.domain.repository.CaregiverRepository;
import com.carenest.business.caregiverservice.domain.repository.CategoryLocationRepository;
import com.carenest.business.caregiverservice.domain.repository.CategoryServiceRepository;
import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaregiverService {

	private final CaregiverRepository caregiverRepository;
	private final CategoryLocationRepository categoryLocationRepository;
	private final CategoryServiceRepository categoryServiceRepository;

	@Qualifier("caregiverApplicationMapper")
	private final CaregiverApplicationMapper applicationMapper;

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
			.map(serviceId ->{
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
}
