package com.carenest.business.caregiverservice.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.caregiverservice.domain.model.Caregiver;

public interface CaregiverRepository extends JpaRepository<Caregiver, UUID> {

}
