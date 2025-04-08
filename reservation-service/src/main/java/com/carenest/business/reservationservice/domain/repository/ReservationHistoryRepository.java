package com.carenest.business.reservationservice.domain.repository;

import com.carenest.business.reservationservice.domain.model.ReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationHistoryRepository extends JpaRepository<ReservationHistory, UUID> {
    List<ReservationHistory> findByReservationId(UUID reservationId);

    List<ReservationHistory> findByGuardianId(UUID guardianId);

    List<ReservationHistory> findByCaregiverId(UUID caregiverId);
}