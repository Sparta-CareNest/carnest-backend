package com.carenest.business.caregiverservice.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carenest.business.caregiverservice.domain.model.CaregiverApproval;

@Repository
public interface CaregiverApprovalRepository extends JpaRepository<CaregiverApproval, UUID> {

	List<CaregiverApproval> findByCaregiverId(UUID caregiverId);

	Optional<CaregiverApproval> findByReservationIdAndCaregiverId(UUID reservationId, UUID id);
}
