package com.carenest.business.caregiverservice.domain.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryService;
import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaregiverDomainServiceImpl implements CaregiverDomainService {

	@Override
	public Caregiver createCaregiverWithCategories(
		CaregiverCreateRequestServiceDTO request,
		List<CategoryService> categoryServices,
		List<CategoryLocation> categoryLocations) {

		Caregiver caregiver = Caregiver.builder()
			.userId(request.userId())
			.description(request.description())
			.experienceYears(request.experienceYears())
			.pricePerDay(request.pricePerDay())
			.pricePerHour(request.pricePerHour())
			.gender(request.gender())
			.submittedDocuments(request.submittedDocuments())
			.build();

		// ID 리스트 기준으로 categoryService 연결
		List<CaregiverCategoryService> services = categoryServices.stream()
			.filter(service -> request.categoryServiceIds().contains(service.getId()))
			.map(service -> CaregiverCategoryService.builder()
				.caregiver(caregiver)
				.categoryService(service)
				.build())
			.toList();

		List<CaregiverCategoryLocation> locations = categoryLocations.stream()
			.filter(location -> request.categoryLocationIds().contains(location.getId()))
			.map(location -> CaregiverCategoryLocation.builder()
				.caregiver(caregiver)
				.categoryLocation(location)
				.build())
			.toList();

		caregiver.getCaregiverCategoryServices().addAll(services);
		caregiver.getCaregiverCategoryLocations().addAll(locations);

		return caregiver;
	}

	@Override
	public void updateCaregiverCategories(Caregiver caregiver,
		List<CategoryService> categoryServices,
		List<CategoryLocation> categoryLocations) {

		if (categoryServices != null) {
			caregiver.clearCategoryServices();

			List<CaregiverCategoryService> newServices = categoryServices.stream()
				.map(service -> CaregiverCategoryService.builder()
					.caregiver(caregiver)
					.categoryService(service)
					.build())
				.toList();

			caregiver.getCaregiverCategoryServices().addAll(newServices);
		}

		if (categoryLocations != null) {
			caregiver.clearCategoryLocation();

			List<CaregiverCategoryLocation> newLocations = categoryLocations.stream()
				.map(location -> CaregiverCategoryLocation.builder()
					.caregiver(caregiver)
					.categoryLocation(location)
					.build())
				.toList();

			caregiver.getCaregiverCategoryLocations().addAll(newLocations);
		}
	}

	@Override
	public void caregiverApprovalStatusCheck(Caregiver caregiver, boolean check) {
		caregiver.updateApprovalStatus(check);
	}

	@Override
	public void deleteCaregiverWithAssociations(UUID caregiverId, Caregiver caregiver) {
		caregiver.clearCategoryServices();
		caregiver.clearCategoryLocation();
		caregiver.softDelete();
	}

}
