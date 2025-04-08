package com.carenest.business.caregiverservice.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.repository.CaregiverCustomRepository;
import com.carenest.business.caregiverservice.domain.repository.CaregiverRepository;

public interface JpaCaregiverRepository extends JpaRepository<Caregiver, UUID>, CaregiverCustomRepository,
	CaregiverRepository {
}
