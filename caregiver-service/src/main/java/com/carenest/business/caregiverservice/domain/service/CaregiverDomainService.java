package com.carenest.business.caregiverservice.domain.service;

import java.util.List;
import java.util.UUID;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CategoryService;

public interface CaregiverDomainService {

	Caregiver createCaregiverWithCategories(CaregiverCreateRequestServiceDTO request,
		List<CategoryService> categoryServices, List<CategoryLocation> categoryLocations, List<String> uploadUrls,
		UUID userId);

	void deleteCaregiverWithAssociations(UUID caregiverId, Caregiver caregiver);

	void updateCaregiverCategories(Caregiver caregiver, List<CategoryService> categoryServices,
		List<CategoryLocation> categoryLocations);

	void caregiverApprovalStatusCheck(Caregiver caregiver, boolean check);
}
