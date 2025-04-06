package com.carenest.business.matchingservice.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.matchingservice.domain.model.Caregiver;
import com.carenest.business.matchingservice.domain.repository.CaregiverCustomRepository;
import com.carenest.business.matchingservice.domain.repository.CaregiverRepository;

public interface JpaCaregiverRepository extends JpaRepository<Caregiver, UUID>, CaregiverCustomRepository,
	CaregiverRepository {
}
