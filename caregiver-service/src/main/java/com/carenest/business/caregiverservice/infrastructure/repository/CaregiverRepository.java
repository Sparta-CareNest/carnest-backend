package com.carenest.business.caregiverservice.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.infrastructure.repository.querydsl.CaregiverCustomRepository;

public interface CaregiverRepository extends JpaRepository<Caregiver, UUID>, CaregiverCustomRepository {

	boolean existsByUserId(UUID uuid);

	Optional<Caregiver> findByUserId(UUID userId);
}
