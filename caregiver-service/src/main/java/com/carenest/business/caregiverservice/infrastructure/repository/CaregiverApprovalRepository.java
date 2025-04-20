package com.carenest.business.caregiverservice.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carenest.business.caregiverservice.domain.model.CaregiverApproval;

@Repository
public interface CaregiverApprovalRepository extends JpaRepository<CaregiverApproval, UUID> {

}
