package com.carenest.business.caregiverservice.infrastructure.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.GenderType;

public interface CaregiverCustomRepository {
	Page<Caregiver> searchByConditions(String location, String service, Pageable pageable);

	Optional<Caregiver> findCaregiverWithCategories(UUID userId);

	Page<Caregiver> findAllCaregivers(Pageable pageable);

	Optional<Caregiver> findCaregiverWithCategoriesById(UUID caregiverId);

	List<Caregiver> getCaregiverIdsByFilters(String location, GenderType gender, Integer experienceYears, Double rating);
}
