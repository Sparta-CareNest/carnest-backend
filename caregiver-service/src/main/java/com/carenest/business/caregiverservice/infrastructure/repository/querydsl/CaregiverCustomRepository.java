package com.carenest.business.caregiverservice.infrastructure.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.carenest.business.caregiverservice.domain.model.Caregiver;

public interface CaregiverCustomRepository {
	Page<Caregiver> searchByConditions(String location, String service, Pageable pageable);
}
