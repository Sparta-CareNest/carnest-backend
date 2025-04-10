package com.carenest.business.caregiverservice.infrastructure.repository.querydsl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.carenest.business.caregiverservice.domain.model.Caregiver;

public interface CaregiverCustomRepository {
	Page<Caregiver> searchByConditions(String location, String service, Pageable pageable);

	Optional<Caregiver> findCaregiverWithCategories(UUID caregiverId);
}
